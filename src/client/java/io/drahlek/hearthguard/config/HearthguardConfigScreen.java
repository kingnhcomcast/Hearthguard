package io.drahlek.hearthguard.config;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;


@Environment(EnvType.CLIENT)
public class HearthguardConfigScreen extends Screen {

    private final Screen parent;

    protected HearthguardConfigScreen(Screen parent) {
        super(Component.literal("Hearthguard Config")); // screen title
        this.parent = parent;
    }

    @Override
    protected void init() {
        HearthguardConfig config = HearthguardConfig.getInstance();

        // CycleButton for WHITELIST / BLACKLIST
        this.addRenderableWidget(
                new CycleButton.Builder<>(
                        mode -> Component.literal(mode == HearthguardConfig.Mode.WHITELIST ? "Whitelist" : "Blacklist"),
                        config.modeEnum
                )
                        .withValues(HearthguardConfig.Mode.values())
                        .create(
                                this.width / 2 - 75, this.height / 2 - 10,
                                150, 20,
                                Component.literal("Mode"),
                                (button, value) -> {
                                    config.modeEnum = value;
                                    config.mode = value.name();
                                }
                        )
        );

        // Done button
        this.addRenderableWidget(
                Button.builder(Component.literal("Done"), button -> {
                            saveConfig();          // Save to disk only when Done is pressed
                            minecraft.setScreen(parent); // return to parent screen
                            HearthguardConfig.init();
                        })
                        .bounds(this.width / 2 - 50, this.height / 2 + 20, 100, 20)
                        .build()
        );
    }

    private void saveConfig() {
        try {
            HearthguardConfig config = HearthguardConfig.getInstance();
            config.mode = config.modeEnum.name(); // keep string in sync
            // Save to file
            java.nio.file.Path configDir = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("hearthguard");
            java.nio.file.Files.createDirectories(configDir);
            java.nio.file.Path file = configDir.resolve("moblist.json");
            try (java.io.Writer writer = java.nio.file.Files.newBufferedWriter(file)) {
                com.google.gson.Gson gson = new com.google.gson.Gson();
                gson.toJson(config, writer);
            }
        } catch (Exception ignored) {
        }
    }
}