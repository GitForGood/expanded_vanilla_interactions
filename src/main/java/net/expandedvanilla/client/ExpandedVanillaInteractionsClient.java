package net.expandedvanilla.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.expandedvanilla.interactions.boat_mooring.client.BoatMooringClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class ExpandedVanillaInteractionsClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("expanded_vanilla_interactions/client");

    @Override
    public void onInitializeClient() {
        LOGGER.info("[Expanded Vanilla Interactions - Client] Initializing...");

        // Initialize modular client features
        BoatMooringClient.init();

        LOGGER.info("[Expanded Vanilla Interactions - Client] Initialized successfully.");
    }
}
