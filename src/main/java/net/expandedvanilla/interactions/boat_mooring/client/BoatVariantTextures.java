package net.expandedvanilla.interactions.boat_mooring.client;

import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class BoatVariantTextures {
    private static final Map<BoatEntity.Type, Identifier> WOOD_TEXTURES = new HashMap<>();
    public static final Identifier KNOT_TEXTURE = new Identifier("minecraft", "textures/entity/lead_knot.png");

    static {
        WOOD_TEXTURES.put(BoatEntity.Type.OAK, new Identifier("minecraft", "textures/block/oak_planks.png"));
        WOOD_TEXTURES.put(BoatEntity.Type.SPRUCE, new Identifier("minecraft", "textures/block/spruce_planks.png"));
        WOOD_TEXTURES.put(BoatEntity.Type.BIRCH, new Identifier("minecraft", "textures/block/birch_planks.png"));
        WOOD_TEXTURES.put(BoatEntity.Type.JUNGLE, new Identifier("minecraft", "textures/block/jungle_planks.png"));
        WOOD_TEXTURES.put(BoatEntity.Type.ACACIA, new Identifier("minecraft", "textures/block/acacia_planks.png"));
        WOOD_TEXTURES.put(BoatEntity.Type.CHERRY, new Identifier("minecraft", "textures/block/cherry_planks.png"));
        WOOD_TEXTURES.put(BoatEntity.Type.DARK_OAK, new Identifier("minecraft", "textures/block/dark_oak_planks.png"));
        WOOD_TEXTURES.put(BoatEntity.Type.MANGROVE, new Identifier("minecraft", "textures/block/mangrove_planks.png"));
        WOOD_TEXTURES.put(BoatEntity.Type.BAMBOO, new Identifier("minecraft", "textures/block/bamboo_planks.png"));
    }

    public static Identifier getWoodTexture(BoatEntity.Type type) {
        return WOOD_TEXTURES.getOrDefault(type, WOOD_TEXTURES.get(BoatEntity.Type.OAK));
    }
}
