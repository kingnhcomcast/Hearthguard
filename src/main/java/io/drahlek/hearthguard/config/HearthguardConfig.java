package io.drahlek.hearthguard.config;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.registries.BuiltInRegistries;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static com.terraformersmc.modmenu.ModMenu.GSON;

public class HearthguardConfig {
    public enum Mode implements Supplier<Mode> { WHITELIST, BLACKLIST;

        @Override
        public Mode get() {
            return null;
        }
    }

    public String mode;
    public Set<String> mobs = new HashSet<>();

    public Mode modeEnum;

    private static HearthguardConfig INSTANCE;

    public static HearthguardConfig getInstance() {
        return INSTANCE;
    }

    public static void init() {
        try {
            Path configDir = FabricLoader.getInstance().getConfigDir();
            Path file = configDir.resolve("hearthguard/moblist.json");

            if (!Files.exists(file)) {
                Files.createDirectories(file.getParent());
                try (InputStream in = HearthguardConfig.class.getResourceAsStream("/assets/hearthguard/moblist.json")) {
                    if (in != null) Files.copy(in, file);
                }
            }

            try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(file))) {
                Gson gson = new Gson();
                INSTANCE = gson.fromJson(reader, HearthguardConfig.class);
                if (INSTANCE == null) INSTANCE = new HearthguardConfig();
                if (INSTANCE.mobs == null) INSTANCE.mobs = new HashSet<>();

                if (INSTANCE.modeEnum == null && INSTANCE.mode != null) {
                    try {
                        INSTANCE.modeEnum = Mode.valueOf(INSTANCE.mode.toUpperCase());
                    } catch (Exception e) {
                        INSTANCE.modeEnum = Mode.WHITELIST; // fallback
                    }
                }
            }
        } catch (Exception e) {
            INSTANCE = new HearthguardConfig();
        }
    }

    public boolean shouldApply(EntityType<?> type) {
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);

        String idStr = id.toString(); // e.g. "minecraft:zombie"

        if (modeEnum == Mode.WHITELIST) {
            return mobs.contains(idStr);
        } else {
            return !mobs.contains(idStr);
        }
    }

    public void save() {
        try {
            Path configDir = FabricLoader.getInstance().getConfigDir().resolve("hearthguard");
            Files.createDirectories(configDir);
            Path file = configDir.resolve("moblist.json");

            // Keep the string in sync
            if (modeEnum != null) mode = modeEnum.name();

            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
