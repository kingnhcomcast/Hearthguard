package io.drahlek.hearthguard.platform;

import io.drahlek.hearthguard.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public String getModName(String modId) {
        ModContainer mod = FabricLoader.getInstance().getModContainer(modId).orElse(null);
        if (mod != null) {
            return mod.getMetadata().getName();
        } else {
            return modId;
        }
    }
}
