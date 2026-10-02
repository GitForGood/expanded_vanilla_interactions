package net.expandedvanilla;

import net.fabricmc.api.ModInitializer;
import net.expandedvanilla.interactions.boat_mooring.BoatMooringHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExpandedVanillaInteractions implements ModInitializer {
    public static final String MOD_ID = "expanded_vanilla_interactions";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[Expanded Vanilla Interactions] Initializing...");

        // Initialize modular interactions
        // Future interaction modules will be initialized here as sibling features
        BoatMooringHandler.init();

        LOGGER.info("[Expanded Vanilla Interactions] Initialized successfully.");
    }
}
