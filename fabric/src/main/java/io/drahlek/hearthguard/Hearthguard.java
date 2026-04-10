package io.drahlek.hearthguard;

import io.drahlek.hearthguard.commands.FabricCommands;
import io.drahlek.hearthguard.config.HearthguardConfig;
import io.drahlek.hearthguard.networking.HearthGuardNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
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
        ServerLifecycleEvents.SERVER_STARTED.register(HearthguardConfig::setActiveServer);
        ServerLifecycleEvents.SERVER_STOPPED.register(HearthguardConfig::clearActiveServer);
        HearthGuardNetworking.init();
        FabricCommands.init();
    }
}
