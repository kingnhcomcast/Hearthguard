package io.drahlek.hearthguard;

import io.drahlek.hearthguard.config.HearthguardConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO config defaults, add default mobs - only overworld hostiles
//TODO sound not heard when running in MC launcher
//TODO test with other mod hostiles
//TODO dont work in nether
public class Hearthguard implements ModInitializer {
    public static final String MOD_ID = "hearthguard";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        HearthguardConfig.init();
    }
}
