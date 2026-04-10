package io.drahlek.hearthguard.config;

import com.google.gson.annotations.SerializedName;
import io.drahlek.hearthguard.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class HearthguardConfig {
    private static Path path;

    private static final com.google.gson.Gson GSON = new com.google.gson.GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static HearthguardConfig INSTANCE;

    @ConfigSetting(value = "mode", defaultValue = "WHITELIST")
    private String mode = Mode.WHITELIST.name();
    private transient Mode modeEnum = Mode.WHITELIST;

    @ConfigSetting("mobs")
    private Set<String> mobs = new HashSet<>(Set.of(
            "minecraft:zombie",
            "minecraft:zombie_villager",
            "minecraft:husk",
            "minecraft:drowned",
            "minecraft:skeleton",
            "minecraft:stray",
            "minecraft:bogged",
            "minecraft:creeper",
            "minecraft:spider",
            "minecraft:cave_spider",
            "minecraft:slime"
    ));
    @ConfigSetting(value = "range", min = 3, max = 32, defaultValue = "8")
    private int range = 8;
    @SerializedName("flee_fast_speed")
    @ConfigSetting(value = "fleeFastSpeed", min = 0.1, max = 2.0, defaultValue = "1.5")
    private double fleeFastSpeed = 1.5;
    @SerializedName("flee_slow_speed")
    @ConfigSetting(value = "fleeSlowSpeed", min = 0.1, max = 2.0, defaultValue = "1.0")
    private double fleeSlowSpeed = 1.0;
    @SerializedName("drop_item_chance")
    @ConfigSetting(value = "dropItemChance", min = 0.0, max = 1.0, defaultValue = "0.25")
    private int dropItemChance = 25;

    // =====================
    // Singleton access
    // =====================
    public static HearthguardConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HearthguardConfig();
        }
        return INSTANCE;
    }

    public static void setInstance(HearthguardConfig config) {
        INSTANCE = config != null ? config : new HearthguardConfig();
        INSTANCE.syncModeEnum();
    }

    //TODO get path from IPlatformHelper
    public static void load(Path path) {
        HearthguardConfig.path = path;
        load();
    }

    public static void load() {
        if(path != null) {
            Path configDir = path.resolve(Constants.MOD_ID);
            Path file = configDir.resolve("config.json");

            try {
                if (Files.exists(file)) {
                    try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(file))) {
                        INSTANCE = GSON.fromJson(reader, HearthguardConfig.class);
                    }
                }
            } catch (Exception e) {
                Constants.LOG.error("Failed to load config from {}", file, e);
            }

            // If file didn't exist or loading failed, use defaults
            if (INSTANCE == null) {
                INSTANCE = new HearthguardConfig();
            }

            INSTANCE.syncModeEnum();

            // Save once to ensure the directory and file exist for the player
            INSTANCE.save();
        } else {
            Constants.LOG.error("Failed to load config from {} path not set", Constants.MOD_ID);
        }
    }

    // =====================
    // Getters / Setters
    // =====================
    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public double getFleeFastSpeed() {
        return fleeFastSpeed;
    }

    public void setFleeFastSpeed(double speed) {
        this.fleeFastSpeed = speed;
    }

    public double getFleeSlowSpeed() {
        return fleeSlowSpeed;
    }

    public void setFleeSlowSpeed(double speed) {
        this.fleeSlowSpeed = speed;
    }

    public Mode getModeEnum() {
        if (modeEnum == null) modeEnum = Mode.WHITELIST;
        return modeEnum;
    }

    public void setModeEnum(Mode modeEnum) {
        this.modeEnum = modeEnum;
        this.mode = modeEnum.name();
    }

    public void setMode(String mode) {
        this.mode = mode;
        syncModeEnum();
    }

    public Set<String> getMobs() {
        if (mobs == null) {
            mobs = new HashSet<>();
        }
        return mobs;
    }

    public int getDropItemChance() {
        return dropItemChance;
    }

    public void setDropItemChance(int dropItemChance) {
        this.dropItemChance = Math.max(0, Math.min(dropItemChance, 100));
    }

    // =====================
    // JSON Save
    // =====================
    public void save() {
        Path file = null;
        try {
            Path configDir = path.resolve(Constants.MOD_ID);
            Files.createDirectories(configDir);
            file = configDir.resolve("config.json");

            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception e) {
            Constants.LOG.error("Failed to save config to {}", file, e);
        }
    }

    // =====================
    // Utility
    // =====================
    public boolean shouldApply(EntityType<?> type) {
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (id == null) return false;

        String idStr = id.toString();

        if (getModeEnum() == Mode.WHITELIST) {
            return mobs.contains(idStr);
        } else {
            return !mobs.contains(idStr);
        }
    }

    // =====================
    // Mode enum
    // =====================
    public enum Mode {
        WHITELIST, BLACKLIST
    }

    private void syncModeEnum() {
        if (mode != null) {
            try {
                modeEnum = Mode.valueOf(mode);
            } catch (IllegalArgumentException e) {
                modeEnum = Mode.WHITELIST;
                Constants.LOG.warn("Invalid config mode '{}', defaulting to {}", mode, modeEnum);
            }
        } else {
            modeEnum = Mode.WHITELIST;
        }
    }
}
