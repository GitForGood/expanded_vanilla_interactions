package net.expandedvanilla.interactions.boat_mooring.client;

import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.expandedvanilla.interactions.boat_mooring.LeashableBoat;
import org.joml.Matrix4f;

public class MooringPostRenderer {
    private final MooringPostModel postModel;

    public MooringPostRenderer(EntityRendererFactory.Context context) {
        this.postModel = new MooringPostModel(context.getPart(BoatMooringClient.MOORING_POST_LAYER));
    }

    /**
     * Renders the wooden mooring bollard and lead knot at the bow of the boat.
     * Called inside BoatEntityRenderer after model transformations.
     */
    public void renderMooringPost(BoatEntity boat, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        LeashableBoat leashable = (LeashableBoat) boat;
        if (!leashable.evi$isLeashed()) {
            return;
        }

        matrices.push();
        // The boat bow in the model coordinate space is at X = 13.5F, Y = 3.0F, Z = 0.0F
        matrices.translate(13.5F / 16.0F, 3.0F / 16.0F, 0.0F);

        // 1. Render the wooden post with the matching boat wood variant texture
        Identifier woodTexture = BoatVariantTextures.getWoodTexture(boat.getVariant());
        VertexConsumer woodConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(woodTexture));
        postModel.renderPost(matrices, woodConsumer, light, OverlayTexture.DEFAULT_UV);

        // 2. Render the lead knot wrapped around the post (using the vanilla lead knot texture)
        VertexConsumer knotConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(BoatVariantTextures.KNOT_TEXTURE));
        postModel.renderKnot(matrices, knotConsumer, light, OverlayTexture.DEFAULT_UV);

        matrices.pop();
    }

    /**
     * Renders the 3D leash rope between the emergent mooring post on the boat and the holding entity.
     * Called in BoatEntityRenderer before or after model rendering in world space.
     */
    public void renderLeashRope(BoatEntity boat, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        LeashableBoat leashable = (LeashableBoat) boat;
        if (!leashable.evi$isLeashed()) {
            return;
        }

        Entity holdingEntity = leashable.evi$getHoldingEntity();
        if (holdingEntity == null) {
            return;
        }

        // Boat yaw in radians
        float yaw = MathHelper.lerp(tickDelta, boat.prevYaw, boat.getYaw());
        float rad = yaw * ((float) Math.PI / 180.0F);

        // Offset to the top of the mooring post at the bow of the boat
        double forwardOffset = 0.85;
        double postLocalX = -Math.sin(rad) * forwardOffset;
        double postLocalY = 0.55;
        double postLocalZ = Math.cos(rad) * forwardOffset;

        double postWorldX = MathHelper.lerp((double) tickDelta, boat.prevX, boat.getX()) + postLocalX;
        double postWorldY = MathHelper.lerp((double) tickDelta, boat.prevY, boat.getY()) + postLocalY;
        double postWorldZ = MathHelper.lerp((double) tickDelta, boat.prevZ, boat.getZ()) + postLocalZ;

        // Position where the leash attaches on the holder (fence knot or player hand)
        Vec3d holderPos = holdingEntity.getLeashPos(tickDelta);
        double dx = holderPos.x - postWorldX;
        double dy = holderPos.y - postWorldY;
        double dz = holderPos.z - postWorldZ;

        double lenXZ = Math.sqrt(dx * dx + dz * dz);
        if (lenXZ < 0.001) {
            lenXZ = 0.001;
        }

        float ropeRadius = 0.025F;
        float nx = (float) (-dz / lenXZ * ropeRadius);
        float nz = (float) (dx / lenXZ * ropeRadius);

        matrices.push();
        // Translate to the mooring post attachment point
        matrices.translate(postLocalX, postLocalY, postLocalZ);

        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLeash());

        // Light levels at boat and holder
        BlockPos postPos = BlockPos.ofFloored(postWorldX, postWorldY, postWorldZ);
        BlockPos holderBlockPos = BlockPos.ofFloored(holderPos.x, holderPos.y, holderPos.z);
        int lightBoat = WorldRenderer.getLightmapCoordinates(boat.getWorld(), postPos);
        int lightHolder = WorldRenderer.getLightmapCoordinates(holdingEntity.getWorld(), holderBlockPos);

        int boatBlockLight = LightmapTextureManager.getBlockLightCoordinates(lightBoat);
        int boatSkyLight = LightmapTextureManager.getSkyLightCoordinates(lightBoat);
        int holderBlockLight = LightmapTextureManager.getBlockLightCoordinates(lightHolder);
        int holderSkyLight = LightmapTextureManager.getSkyLightCoordinates(lightHolder);

        final int segments = 24;
        for (int i = 0; i < segments; i++) {
            float t1 = (float) i / (float) segments;
            float t2 = (float) (i + 1) / (float) segments;

            float x1 = (float) (dx * t1);
            float z1 = (float) (dz * t1);
            float x2 = (float) (dx * t2);
            float z2 = (float) (dz * t2);

            // Catenary sag approximation
            float sag1 = t1 * t1;
            float sag2 = t2 * t2;
            float y1 = (float) (dy > 0.0 ? dy * sag1 : dy - dy * (1.0F - t1) * (1.0F - t1));
            float y2 = (float) (dy > 0.0 ? dy * sag2 : dy - dy * (1.0F - t2) * (1.0F - t2));

            // Light interpolation
            int blockLight = (int) MathHelper.lerp(t1, (float) boatBlockLight, (float) holderBlockLight);
            int skyLight = (int) MathHelper.lerp(t1, (float) boatSkyLight, (float) holderSkyLight);
            int packedLight = LightmapTextureManager.pack(blockLight, skyLight);

            // Alternating rope color pattern for vanilla-style twist look
            float r = (i % 2 == 0) ? 0.44F : 0.52F;
            float g = (i % 2 == 0) ? 0.33F : 0.40F;
            float b = (i % 2 == 0) ? 0.22F : 0.27F;

            // Horizontal strip
            vertexConsumer.vertex(matrix4f, x1 - nx, y1, z1 - nz).color(r, g, b, 1.0F).light(packedLight).next();
            vertexConsumer.vertex(matrix4f, x2 - nx, y2, z2 - nz).color(r, g, b, 1.0F).light(packedLight).next();
            vertexConsumer.vertex(matrix4f, x2 + nx, y2, z2 + nz).color(r, g, b, 1.0F).light(packedLight).next();
            vertexConsumer.vertex(matrix4f, x1 + nx, y1, z1 + nz).color(r, g, b, 1.0F).light(packedLight).next();

            // Vertical strip for 3D cross profile
            float rDim = r * 0.75F;
            float gDim = g * 0.75F;
            float bDim = b * 0.75F;
            vertexConsumer.vertex(matrix4f, x1, y1 - ropeRadius, z1).color(rDim, gDim, bDim, 1.0F).light(packedLight).next();
            vertexConsumer.vertex(matrix4f, x2, y2 - ropeRadius, z2).color(rDim, gDim, bDim, 1.0F).light(packedLight).next();
            vertexConsumer.vertex(matrix4f, x2, y2 + ropeRadius, z2).color(rDim, gDim, bDim, 1.0F).light(packedLight).next();
            vertexConsumer.vertex(matrix4f, x1, y1 + ropeRadius, z1).color(rDim, gDim, bDim, 1.0F).light(packedLight).next();
        }

        matrices.pop();
    }
}
