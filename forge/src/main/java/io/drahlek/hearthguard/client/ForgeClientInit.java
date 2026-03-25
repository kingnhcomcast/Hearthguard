package io.drahlek.hearthguard.client;

import io.drahlek.hearthguard.config.screen.HearthguardConfigScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;

public final class ForgeClientInit {
    private ForgeClientInit() {
    }

    public static void init() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(parent -> new HearthguardConfigScreen(parent, ForgeClientInit::resolveModName)));
    }

    private static String resolveModName(String modId) {
        return ModList.get().getModContainerById(modId)
                .map(container -> container.getModInfo().getDisplayName())
                .orElse(modId);
    }
}
