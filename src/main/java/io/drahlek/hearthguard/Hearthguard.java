package io.drahlek.hearthguard;

import io.drahlek.hearthguard.config.HearthguardConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO add support for mobs created via datapacks that use vanilla mobids
public class Hearthguard implements ModInitializer {
    public static final String MOD_ID = "hearthguard";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        HearthguardConfig.init();
    }
}
