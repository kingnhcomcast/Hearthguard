package io.drahlek.hearthguard;

import io.drahlek.hearthguard.config.HearthguardConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.drahlek.hearthguard.Constants.MOD_ID;

public class Hearthguard implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        HearthguardConfig.init(FabricLoader.getInstance().getConfigDir().resolve(MOD_ID));
    }
}
