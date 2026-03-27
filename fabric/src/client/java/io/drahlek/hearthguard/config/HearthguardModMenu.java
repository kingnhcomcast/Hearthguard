package io.drahlek.hearthguard.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.drahlek.hearthguard.client.config.ConfigScreen;
import io.drahlek.hearthguard.networking.FabricClientNetworking;
import io.drahlek.hearthguard.platform.FabricPlatformHelper;
import io.drahlek.hearthguard.platform.services.IPlatformHelper;


public class HearthguardModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            IPlatformHelper platform = new FabricPlatformHelper();
            return ConfigScreen.createConfigScreen(parent, platform, new FabricClientNetworking());
        };
    }
}

