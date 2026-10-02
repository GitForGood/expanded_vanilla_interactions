package net.expandedvanilla.mixin.boat_mooring;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.expandedvanilla.interactions.boat_mooring.BoatMooringHandler;
import net.expandedvanilla.interactions.boat_mooring.BoatMooringPhysics;
import net.expandedvanilla.interactions.boat_mooring.LeashableBoat;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoatEntity.class)
public abstract class BoatEntityMixin extends Entity implements LeashableBoat {

    @Unique
    @Nullable
    private Entity evi$holdingEntity;

    @Unique
    private int evi$holdingEntityId;

    @Unique
    @Nullable
    private NbtCompound evi$leashNbt;

    public BoatEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public boolean evi$isLeashed() {
        return this.evi$holdingEntity != null || this.evi$holdingEntityId != 0 || this.evi$leashNbt != null;
    }

    @Override
    @Nullable
    public Entity evi$getHoldingEntity() {
        if (this.evi$holdingEntity == null && this.evi$holdingEntityId != 0 && this.getWorld().isClient()) {
            this.evi$holdingEntity = this.getWorld().getEntityById(this.evi$holdingEntityId);
        }
        return this.evi$holdingEntity;
    }

    @Override
    public void evi$setHoldingEntity(@Nullable Entity holdingEntity) {
        this.evi$holdingEntity = holdingEntity;
        this.evi$holdingEntityId = holdingEntity != null ? holdingEntity.getId() : 0;
    }

    @Override
    public void evi$attachLeash(Entity holdingEntity, boolean sendPacket) {
        this.evi$holdingEntity = holdingEntity;
        this.evi$holdingEntityId = holdingEntity.getId();
        this.evi$leashNbt = null;

        if (!this.getWorld().isClient() && sendPacket && this.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.getChunkManager().sendToOtherNearbyPlayers(this, new EntityAttachS2CPacket(this, this.evi$holdingEntity));
        }
    }

    @Override
    public void evi$detachLeash(boolean sendPacket, boolean dropItem) {
        if (this.evi$isLeashed()) {
            if (dropItem && !this.getWorld().isClient()) {
                this.dropItem(Items.LEAD);
            }

            this.evi$holdingEntity = null;
            this.evi$holdingEntityId = 0;
            this.evi$leashNbt = null;

            if (!this.getWorld().isClient() && sendPacket && this.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.getChunkManager().sendToOtherNearbyPlayers(this, new EntityAttachS2CPacket(this, null));
            }
        }
    }

    @Override
    public void evi$writeLeashNbt(NbtCompound nbt) {
        if (this.evi$holdingEntity != null) {
            NbtCompound leashTag = new NbtCompound();
            if (this.evi$holdingEntity instanceof LeashKnotEntity leashKnot) {
                BlockPos blockPos = leashKnot.getDecorationBlockPos();
                leashTag.putInt("X", blockPos.getX());
                leashTag.putInt("Y", blockPos.getY());
                leashTag.putInt("Z", blockPos.getZ());
            } else {
                leashTag.putUuid("UUID", this.evi$holdingEntity.getUuid());
            }
            nbt.put("Leash", leashTag);
        } else if (this.evi$leashNbt != null) {
            nbt.put("Leash", this.evi$leashNbt.copy());
        }
    }

    @Override
    public void evi$readLeashNbt(NbtCompound nbt) {
        if (nbt.contains("Leash", NbtElement.COMPOUND_TYPE)) {
            this.evi$leashNbt = nbt.getCompound("Leash");
        }
    }

    @Override
    public int evi$getHoldingEntityId() {
        return this.evi$holdingEntityId;
    }

    @Override
    public void evi$setHoldingEntityId(int id) {
        this.evi$holdingEntityId = id;
        this.evi$holdingEntity = (id != 0 && this.getWorld() != null) ? this.getWorld().getEntityById(id) : null;
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void evi$onInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ActionResult result = BoatMooringHandler.handleBoatInteract((BoatEntity) (Object) this, player, hand);
        if (result.isAccepted()) {
            cir.setReturnValue(result);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void evi$onTick(CallbackInfo ci) {
        if (!this.getWorld().isClient()) {
            if (this.evi$leashNbt != null) {
                BoatMooringHandler.resolveLeashNbt((BoatEntity) (Object) this, this.evi$leashNbt);
            }

            if (this.evi$holdingEntity != null) {
                BoatMooringPhysics.tickLeashPhysics((BoatEntity) (Object) this, this.evi$holdingEntity);
            }
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void evi$onWriteCustomData(NbtCompound nbt, CallbackInfo ci) {
        this.evi$writeLeashNbt(nbt);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void evi$onReadCustomData(NbtCompound nbt, CallbackInfo ci) {
        this.evi$readLeashNbt(nbt);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.getWorld().isClient() && this.evi$isLeashed() && (reason == RemovalReason.KILLED || reason == RemovalReason.DISCARDED)) {
            this.evi$detachLeash(true, true);
        }
        super.remove(reason);
    }
}
