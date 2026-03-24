package io.drahlek.hearthguard.config;

import com.google.gson.annotations.SerializedName;
import io.drahlek.hearthguard.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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


    private String mode = Mode.WHITELIST.name();
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
    private transient Mode modeEnum = Mode.WHITELIST;
    private int range = 8;
    @SerializedName("flee_fast_speed")
    private double fleeFastSpeed = 1.2;
    @SerializedName("flee_slow_speed")
    private double fleeSlowSpeed = 1.0;
    @SerializedName("drop_item_chance")
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

    //TODO get path from IPlatformHelper
    public static void init(Path path) {
        HearthguardConfig.path = path;
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

        if (INSTANCE.mode != null) {
            try {
                INSTANCE.modeEnum = Mode.valueOf(INSTANCE.mode);
            } catch (IllegalArgumentException e) {
                INSTANCE.modeEnum = Mode.WHITELIST;
                Constants.LOG.warn("Invalid config mode '{}', defaulting to {}", INSTANCE.mode, INSTANCE.modeEnum);
            }
        }

        // Save once to ensure the directory and file exist for the player
        INSTANCE.save();
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

            if (modeEnum != null) mode = modeEnum.name();

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
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
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
}
