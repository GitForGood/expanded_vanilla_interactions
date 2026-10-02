package net.expandedvanilla.mixin.boat_mooring.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.expandedvanilla.interactions.boat_mooring.LeashableBoat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Shadow
    private ClientWorld world;

    @Inject(method = "onEntityAttach", at = @At("HEAD"))
    private void evi$onEntityAttach(EntityAttachS2CPacket packet, CallbackInfo ci) {
        if (this.world != null) {
            Entity entity = this.world.getEntityById(packet.getAttachedEntityId());
            if (entity instanceof LeashableBoat leashable) {
                leashable.evi$setHoldingEntityId(packet.getHoldingEntityId());
            }
        }
    }
}
