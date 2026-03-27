package io.drahlek.hearthguard.client;

import io.drahlek.hearthguard.client.config.ConfigScreen;
import io.drahlek.hearthguard.networking.NeoForgeClientNetworking;
import io.drahlek.hearthguard.platform.NeoForgePlatformHelper;
import io.drahlek.hearthguard.platform.services.IPlatformHelper;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public final class NeoForgeClientInit {
    private NeoForgeClientInit() {
    }

    public static void init(ModContainer container) {
        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class,
                () -> (container2, parent) -> {
                    IPlatformHelper platform = new NeoForgePlatformHelper();
                    return ConfigScreen.createConfigScreen(parent, platform, new NeoForgeClientNetworking());
                }
        );
    }
}
