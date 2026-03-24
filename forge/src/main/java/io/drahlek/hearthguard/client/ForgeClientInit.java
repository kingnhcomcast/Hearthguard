package io.drahlek.hearthguard.client;

import net.minecraftforge.fml.ModList;

public final class ForgeClientInit {
    private ForgeClientInit() {
    }

    public static void init() {
    }

    private static String resolveModName(String modId) {
        return ModList.get().getModContainerById(modId)
                .map(container -> container.getModInfo().getDisplayName())
                .orElse(modId);
    }
}
