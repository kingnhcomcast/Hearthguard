package io.drahlek.hearthguard.client;

import io.drahlek.hearthguard.config.screen.HearthguardConfigScreen;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public final class NeoForgeClientInit {
    private NeoForgeClientInit() {
    }

    public static void init(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (IConfigScreenFactory) (modContainer, parent) -> new HearthguardConfigScreen(parent, NeoForgeClientInit::resolveModName));
    }

    private static String resolveModName(String modId) {
        return ModList.get().getModContainerById(modId)
                .map(modContainer -> modContainer.getModInfo().getDisplayName())
                .orElse(modId);
    }
}
