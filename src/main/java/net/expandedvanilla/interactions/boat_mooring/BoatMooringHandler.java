package net.expandedvanilla.interactions.boat_mooring;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class BoatMooringHandler {

    public static void init() {
        // Initialization if needed for events/registries
    }

    /**
     * Handles player interaction with a boat for leash attachment and detachment.
     */
    public static ActionResult handleBoatInteract(BoatEntity boat, PlayerEntity player, Hand hand) {
        World world = boat.getWorld();
        ItemStack stack = player.getStackInHand(hand);
        LeashableBoat leashable = (LeashableBoat) boat;

        // If player is holding a Lead and the boat is not leashed: attach leash!
        if (stack.isOf(Items.LEAD) && !leashable.evi$isLeashed()) {
            if (!world.isClient) {
                leashable.evi$attachLeash(player, true);
                if (!player.getAbilities().creativeMode) {
                    stack.decrement(1);
                }
                world.playSound(null, boat.getX(), boat.getY(), boat.getZ(),
                        SoundEvents.ENTITY_LEASH_KNOT_PLACE, SoundCategory.NEUTRAL, 0.5f, 1.0f);
                boat.emitGameEvent(GameEvent.ENTITY_INTERACT, player);
            }
            return ActionResult.success(world.isClient);
        }

        // If boat is leashed and player sneaks with empty hand (or lead): detach leash!
        if (leashable.evi$isLeashed() && player.isSneaking()) {
            if (!world.isClient) {
                leashable.evi$detachLeash(true, !player.getAbilities().creativeMode);
                world.playSound(null, boat.getX(), boat.getY(), boat.getZ(),
                        SoundEvents.ENTITY_LEASH_KNOT_BREAK, SoundCategory.NEUTRAL, 0.5f, 1.0f);
                boat.emitGameEvent(GameEvent.ENTITY_INTERACT, player);
            }
            return ActionResult.success(world.isClient);
        }

        return ActionResult.PASS;
    }

    /**
     * Resolves pending leash holder on world load / tick when restored from NBT.
     */
    public static void resolveLeashNbt(BoatEntity boat, @Nullable NbtCompound leashNbt) {
        if (leashNbt == null || !(boat.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        LeashableBoat leashable = (LeashableBoat) boat;

        if (leashNbt.containsUuid("UUID")) {
            UUID uuid = leashNbt.getUuid("UUID");
            Entity entity = serverWorld.getEntity(uuid);
            if (entity != null) {
                leashable.evi$attachLeash(entity, true);
                return;
            }
        } else if (leashNbt.contains("X", NbtElement.INT_TYPE) &&
                   leashNbt.contains("Y", NbtElement.INT_TYPE) &&
                   leashNbt.contains("Z", NbtElement.INT_TYPE)) {
            BlockPos pos = new BlockPos(leashNbt.getInt("X"), leashNbt.getInt("Y"), leashNbt.getInt("Z"));
            LeashKnotEntity knot = LeashKnotEntity.getOrCreate(serverWorld, pos);
            if (knot != null) {
                leashable.evi$attachLeash(knot, true);
                return;
            }
        }

        if (boat.age > 100) {
            leashable.evi$detachLeash(true, true);
        }
    }

    /**
     * Attaches boats currently held by the player within range of the given block pos to a LeashKnotEntity.
     */
    public static boolean attachHeldBoatsToBlock(PlayerEntity player, World world, BlockPos pos) {
        boolean attachedAny = false;
        double range = 7.0;
        Box searchBox = new Box(
                pos.getX() - range, pos.getY() - range, pos.getZ() - range,
                pos.getX() + range, pos.getY() + range, pos.getZ() + range
        );

        List<BoatEntity> boats = world.getNonSpectatingEntities(BoatEntity.class, searchBox);
        LeashKnotEntity knot = null;

        for (BoatEntity boat : boats) {
            LeashableBoat leashable = (LeashableBoat) boat;
            if (leashable.evi$getHoldingEntity() == player) {
                if (knot == null) {
                    knot = LeashKnotEntity.getOrCreate(world, pos);
                    knot.onPlace();
                }
                leashable.evi$attachLeash(knot, true);
                attachedAny = true;
            }
        }

        return attachedAny;
    }

    /**
     * Attaches boats held by the player to the given knot, or detaches all boats tied to it.
     */
    public static boolean interactWithKnot(LeashKnotEntity knot, PlayerEntity player) {
        World world = knot.getWorld();
        double range = 7.0;
        Box searchBox = new Box(
                knot.getX() - range, knot.getY() - range, knot.getZ() - range,
                knot.getX() + range, knot.getY() + range, knot.getZ() + range
        );

        boolean attached = false;
        List<BoatEntity> boats = world.getNonSpectatingEntities(BoatEntity.class, searchBox);

        // First check if player is holding boats to attach to this knot
        for (BoatEntity boat : boats) {
            LeashableBoat leashable = (LeashableBoat) boat;
            if (leashable.evi$getHoldingEntity() == player) {
                leashable.evi$attachLeash(knot, true);
                attached = true;
            }
        }

        // If player wasn't holding any boats, untie all boats attached to this knot
        boolean detached = false;
        if (!attached) {
            for (BoatEntity boat : boats) {
                LeashableBoat leashable = (LeashableBoat) boat;
                if (leashable.evi$getHoldingEntity() == knot) {
                    leashable.evi$detachLeash(true, !player.getAbilities().creativeMode);
                    detached = true;
                }
            }
        }

        return attached || detached;
    }

    /**
     * Called when a fence knot is broken to release all attached boats.
     */
    public static void detachAllFromKnot(LeashKnotEntity knot) {
        World world = knot.getWorld();
        double range = 7.0;
        Box searchBox = new Box(
                knot.getX() - range, knot.getY() - range, knot.getZ() - range,
                knot.getX() + range, knot.getY() + range, knot.getZ() + range
        );

        List<BoatEntity> boats = world.getNonSpectatingEntities(BoatEntity.class, searchBox);
        for (BoatEntity boat : boats) {
            LeashableBoat leashable = (LeashableBoat) boat;
            if (leashable.evi$getHoldingEntity() == knot) {
                leashable.evi$detachLeash(true, true);
            }
        }
    }
}
