package io.drahlek.hearthguard;

import io.drahlek.hearthguard.config.HearthguardConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO make so friendly name is displayed, but minecrat_zombie is saved to config
//TODO config defaults, add default mobs - only overworld hostiles
//TODO have mobs do something before fleeing
public class Hearthguard implements ModInitializer {
    public static final String MOD_ID = "hearthguard";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        HearthguardConfig.init();

        LOGGER.info("Hello Fabric world!");
    }
}