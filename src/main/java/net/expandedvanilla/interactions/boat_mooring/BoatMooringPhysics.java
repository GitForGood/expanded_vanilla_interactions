package net.expandedvanilla.interactions.boat_mooring;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;

public class BoatMooringPhysics {
    public static final double MAX_LEASH_DISTANCE = 10.0;
    public static final double TETHER_SLACK_DISTANCE = 3.0;
    public static final double FENCE_MAX_TETHER = 4.0;

    /**
     * Applies physics to a leashed boat each tick.
     * Handles pulling when towed by players/mobs, and anchor tethering when tied to a fence knot.
     */
    public static void tickLeashPhysics(BoatEntity boat, Entity holdingEntity) {
        if (holdingEntity == null || holdingEntity.isRemoved()) {
            ((LeashableBoat) boat).evi$detachLeash(true, true);
            return;
        }

        double distance = boat.distanceTo(holdingEntity);

        // Snap leash if stretched too far (> 10 blocks)
        if (distance > MAX_LEASH_DISTANCE) {
            ((LeashableBoat) boat).evi$detachLeash(true, true);
            return;
        }

        boolean isAnchoredToFence = holdingEntity instanceof LeashKnotEntity;

        if (isAnchoredToFence) {
            // Mooring anchor physics: boat is tethered to the fence post
            if (distance > FENCE_MAX_TETHER) {
                Vec3d toAnchor = holdingEntity.getPos().subtract(boat.getPos());
                double excess = distance - FENCE_MAX_TETHER;
                Vec3d pullForce = toAnchor.normalize().multiply(excess * 0.15);

                // Apply soft restraining pull to keep boat within mooring range
                Vec3d currentVelocity = boat.getVelocity();
                boat.setVelocity(
                    currentVelocity.x * 0.7 + pullForce.x,
                    currentVelocity.y * 0.9,
                    currentVelocity.z * 0.7 + pullForce.z
                );
                boat.velocityModified = true;
            }
        } else {
            // Towed by player or mob
            if (distance > TETHER_SLACK_DISTANCE) {
                Vec3d toHolder = holdingEntity.getPos().subtract(boat.getPos());
                double stretch = distance - TETHER_SLACK_DISTANCE;
                double strength = Math.min(stretch * 0.05, 0.35);
                Vec3d pullForce = toHolder.normalize().multiply(strength);

                Vec3d currentVelocity = boat.getVelocity();
                boat.setVelocity(
                    currentVelocity.x * 0.85 + pullForce.x,
                    currentVelocity.y,
                    currentVelocity.z * 0.85 + pullForce.z
                );
                boat.velocityModified = true;
            }
        }
    }
}
