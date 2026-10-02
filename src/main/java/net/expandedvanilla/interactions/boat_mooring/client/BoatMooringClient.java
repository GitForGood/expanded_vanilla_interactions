package net.expandedvanilla.interactions.boat_mooring.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;
import net.expandedvanilla.ExpandedVanillaInteractions;

@Environment(EnvType.CLIENT)
public class BoatMooringClient {
    public static final EntityModelLayer MOORING_POST_LAYER =
            new EntityModelLayer(new Identifier(ExpandedVanillaInteractions.MOD_ID, "mooring_post"), "main");

    public static void init() {
        EntityModelLayerRegistry.registerModelLayer(MOORING_POST_LAYER, MooringPostModel::getTexturedModelData);
    }
}
