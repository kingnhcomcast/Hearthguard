package io.drahlek.hearthguard;

import io.drahlek.hearthguard.commands.FabricCommands;
import io.drahlek.hearthguard.config.HearthguardConfig;
import io.drahlek.hearthguard.networking.HearthGuardNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.drahlek.hearthguard.Constants.MOD_ID;

public class Hearthguard implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        HearthguardConfig.load(FabricLoader.getInstance().getConfigDir());
        HearthGuardNetworking.init();
        FabricCommands.init();
    }
}
