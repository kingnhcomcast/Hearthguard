package io.drahlek.hearthguard;

import io.drahlek.hearthguard.config.HearthguardConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO config defaults, add default mobs - only overworld hostiles
//TODO add config for chance to drop item - allow 0
//TODO make sure they only ever drop 1 item
//TODO sound not heard when running in MC launcher
//TODO test with other mod hostiles
public class Hearthguard implements ModInitializer {
    public static final String MOD_ID = "hearthguard";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        HearthguardConfig.init();
    }
}