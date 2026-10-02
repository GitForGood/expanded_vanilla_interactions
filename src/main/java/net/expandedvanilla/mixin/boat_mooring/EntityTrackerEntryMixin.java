package net.expandedvanilla.mixin.boat_mooring;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.expandedvanilla.interactions.boat_mooring.LeashableBoat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTrackerEntry.class)
public abstract class EntityTrackerEntryMixin {

    @Shadow
    @Final
    private Entity entity;

    @Inject(method = "startTracking", at = @At("TAIL"))
    private void evi$onStartTracking(ServerPlayerEntity player, CallbackInfo ci) {
        if (this.entity instanceof LeashableBoat leashable && leashable.evi$isLeashed()) {
            player.networkHandler.sendPacket(new EntityAttachS2CPacket(this.entity, leashable.evi$getHoldingEntity()));
        }
    }
}
