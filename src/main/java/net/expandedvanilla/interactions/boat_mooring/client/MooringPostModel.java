package net.expandedvanilla.interactions.boat_mooring.client;

import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

public class MooringPostModel extends Model {
    private final ModelPart post;
    private final ModelPart knot;

    public MooringPostModel(ModelPart root) {
        super(RenderLayer::getEntityCutoutNoCull);
        this.post = root.getChild("post");
        this.knot = root.getChild("knot");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        // Little mooring bollard/post emerging at the bow of the boat (4x8x4)
        root.addChild("post", ModelPartBuilder.create()
                .uv(0, 0)
                .cuboid(-2.0F, -8.0F, -2.0F, 4.0F, 8.0F, 4.0F),
                ModelTransform.NONE);

        // Rope knot wrapped around the mooring post, similar to the fence leash knot (6x6x6)
        root.addChild("knot", ModelPartBuilder.create()
                .uv(0, 0)
                .cuboid(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F),
                ModelTransform.NONE);

        return TexturedModelData.of(modelData, 32, 32);
    }

    public void renderPost(MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
        post.render(matrices, vertices, light, overlay);
    }

    public void renderKnot(MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
        knot.render(matrices, vertices, light, overlay);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        post.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        knot.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
}
