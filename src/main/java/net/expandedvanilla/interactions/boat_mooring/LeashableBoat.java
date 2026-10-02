package net.expandedvanilla.interactions.boat_mooring;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

/**
 * Interface implemented on {@link net.minecraft.entity.vehicle.BoatEntity}
 * to support leash attachment, holding entity tracking, and mooring post rendering.
 */
public interface LeashableBoat {
    boolean evi$isLeashed();

    @Nullable
    Entity evi$getHoldingEntity();

    void evi$setHoldingEntity(@Nullable Entity holdingEntity);

    void evi$attachLeash(Entity holdingEntity, boolean sendPacket);

    void evi$detachLeash(boolean sendPacket, boolean dropItem);

    void evi$writeLeashNbt(NbtCompound nbt);

    void evi$readLeashNbt(NbtCompound nbt);

    int evi$getHoldingEntityId();

    void evi$setHoldingEntityId(int id);
}
