package net.expandedvanilla.mixin.boat_mooring.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.BoatEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.BoatEntity;
import net.expandedvanilla.interactions.boat_mooring.client.MooringPostRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(BoatEntityRenderer.class)
public abstract class BoatEntityRendererMixin {

    @Unique
    private MooringPostRenderer evi$mooringPostRenderer;

    @Inject(method = "<init>(Lnet/minecraft/client/render/entity/EntityRendererFactory$Context;Z)V", at = @At("TAIL"))
    private void evi$init(EntityRendererFactory.Context ctx, boolean chest, CallbackInfo ci) {
        this.evi$mooringPostRenderer = new MooringPostRenderer(ctx);
    }

    @Inject(
        method = "render(Lnet/minecraft/entity/vehicle/BoatEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V", ordinal = 0)
    )
    private void evi$renderMooringPost(BoatEntity boat, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (this.evi$mooringPostRenderer != null) {
            this.evi$mooringPostRenderer.renderMooringPost(boat, matrices, vertexConsumers, light);
        }
    }

    @Inject(
        method = "render(Lnet/minecraft/entity/vehicle/BoatEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At("TAIL")
    )
    private void evi$renderLeash(BoatEntity boat, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (this.evi$mooringPostRenderer != null) {
            this.evi$mooringPostRenderer.renderLeashRope(boat, tickDelta, matrices, vertexConsumers);
        }
    }
}
