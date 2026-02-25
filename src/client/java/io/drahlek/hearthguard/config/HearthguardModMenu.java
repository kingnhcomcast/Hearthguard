package io.drahlek.hearthguard.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

@Environment(EnvType.CLIENT)
public class HearthguardModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        // Returns a factory that creates your config screen
        return this::createConfigScreen;
    }

    private Screen createConfigScreen(Screen parent) {
        // You can use your own screen class here
        return new HearthguardConfigScreen(parent);
    }
}
