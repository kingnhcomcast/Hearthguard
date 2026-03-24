package io.drahlek.hearthguard.config;

import io.drahlek.hearthguard.platform.services.IPlatformHelper;
import io.drahlek.hearthguard.util.MobRules;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigScreen {
    private static Map<String, List<EntityType<?>>> mobsByMod = null;

    public static Screen createConfigScreen(Screen parent, IPlatformHelper platformHelper) {
        HearthguardConfig config = HearthguardConfig.getInstance();
        Set<String> selectedMobs = new HashSet<>(config.getMobs());
        HearthguardConfig.Mode currentMode = config.getModeEnum() != null
                ? config.getModeEnum() : HearthguardConfig.Mode.WHITELIST;
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("HearthGuard Options"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        //build general tab
        buildGeneralTab(builder, entryBuilder, config, currentMode);

        //generate map of mobs per mod
        Map<String, List<EntityType<?>>> mobsByMod = getMobsByMod(platformHelper);

        //for each mod, build a tab of mobs
        for (String modid : mobsByMod.keySet()) {
            buildMobSelectionTab(modid, builder, mobsByMod, selectedMobs, entryBuilder);
        }

        // Save config when pressing Done
        builder.setSavingRunnable(() -> {
            config.getMobs().clear();
            config.getMobs().addAll(selectedMobs);
            config.save();
        });

        return builder.build();
    }

    private static void buildMobSelectionTab(String modid, ConfigBuilder builder, Map<String, List<EntityType<?>>> mobsByMod, Set<String> selectedMobs, ConfigEntryBuilder entryBuilder) {
        // ===== Mob List =====
        List<BooleanListEntry> mobEntries = new ArrayList<>();

        ConfigCategory modCategory = builder.getOrCreateCategory(Component.literal(modid));

        for (EntityType<?> type : mobsByMod.get(modid)) {
            String displayName = type.getDescription().getString();
            String longName = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();

            boolean selected = selectedMobs.contains(longName);

            BooleanListEntry entry =
                    entryBuilder.startBooleanToggle(Component.literal(displayName), selected)
                            .setDefaultValue(false)
                            .setSaveConsumer(sel -> {
                                if (sel) {
                                    selectedMobs.add(longName);
                                }
                                else {
                                    selectedMobs.remove(longName);
                                }
                            })
                            .build();

            mobEntries.add(entry);
            modCategory.addEntry(entry);
        }

        boolean allSelected = !mobEntries.isEmpty() && mobEntries.stream().allMatch(BooleanListEntry::getValue);
        String label = allSelected ? "Deselect All" : "Select All";
        ButtonEntry toggleBtn = new ButtonEntry(Component.literal(label), !allSelected, (shouldSelect) -> {
            // 1. Update the actual config data immediately
            List<EntityType<?>> mobs = mobsByMod.get(modid);
            if (shouldSelect) {
                mobs.forEach(mobType -> {
                    selectedMobs.add(BuiltInRegistries.ENTITY_TYPE.getKey(mobType).toString());
                });
            } else {
                mobs.forEach(mobType -> {
                    selectedMobs.remove(BuiltInRegistries.ENTITY_TYPE.getKey(mobType).toString());
                });
            }

            // 2. Cloth Config doesn't know the data changed.
            // We MUST tell the entries to look at the config again.
            mobEntries.forEach(entry -> {
                setBooleanEntryValue(entry, shouldSelect);
            });
        });

        modCategory.getEntries().addFirst(toggleBtn);
    }

    private static @NonNull Map<String, List<EntityType<?>>> getMobsByMod(IPlatformHelper platformHelper) {
        if(mobsByMod == null) {
            mobsByMod = new HashMap<>();

            for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
                if (type.getCategory() != MobCategory.MONSTER)
                    continue;

                if (MobRules.isBossMob(type)) {
                    continue;
                }

                Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(type);

                String modid = platformHelper.getModName(key.getNamespace());
                mobsByMod.computeIfAbsent(modid, k -> new ArrayList<>()).add(type);
            }

            //sort the lists
            for (Map.Entry<String, List<EntityType<?>>> entry : mobsByMod.entrySet()) {
                entry.getValue().sort(Comparator.comparing(type -> type.getDescription().getString()));
            }
        }

        return mobsByMod;
    }

    private static void buildGeneralTab(ConfigBuilder builder, ConfigEntryBuilder entryBuilder, HearthguardConfig config, HearthguardConfig.Mode currentMode) {
        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));

        // Range slider (int)
        general.addEntry(
                entryBuilder.startIntSlider(Component.literal("Range"), config.getRange(), 3, 32)
                        .setDefaultValue(8)
                        .setSaveConsumer(config::setRange)
                        .build()
        );

        // Slow speed (double field)
        general.addEntry(
                entryBuilder.startDoubleField(Component.literal("Flee Slow Speed"), config.getFleeSlowSpeed())
                        .setDefaultValue(1.0)
                        .setSaveConsumer(value -> config.setFleeSlowSpeed(Math.min(Math.max(value, 0.1), 2.0)))
                        .build()
        );

        // Fast speed (double field)
        general.addEntry(
                entryBuilder.startDoubleField(Component.literal("Flee Fast Speed"), config.getFleeFastSpeed())
                        .setDefaultValue(1.2)
                        .setSaveConsumer(value -> config.setFleeFastSpeed(Math.min(Math.max(value, 0.1), 2.0)))
                        .build()
        );

        general.addEntry(
                entryBuilder.startIntSlider(Component.literal("Drop Item Chance"), config.getDropItemChance(), 0, 100)
                        .setDefaultValue(25)
                        .setTextGetter(value -> Component.literal(value + "%"))
                        .setSaveConsumer(config::setDropItemChance)
                        .build()
        );

        // Mode selector
        general.addEntry(
                entryBuilder.startEnumSelector(Component.literal("Mode"), HearthguardConfig.Mode.class, currentMode)
                        .setDefaultValue(HearthguardConfig.Mode.WHITELIST)
                        .setSaveConsumer(config::setModeEnum)
                        .build()
        );
    }

    private static void setBooleanEntryValue(BooleanListEntry entry, boolean value) {
        try {
            // We look for the field named "bool" in the BooleanListEntry class
            java.lang.reflect.Field boolField = BooleanListEntry.class.getDeclaredField("bool");
            boolField.setAccessible(true);

            // Get the AtomicBoolean instance from this specific entry
            java.util.concurrent.atomic.AtomicBoolean atomic = (java.util.concurrent.atomic.AtomicBoolean) boolField.get(entry);

            // Update the value inside the AtomicBoolean
            atomic.set(value);

            // IMPORTANT: Cloth Config checks "isEdited" by comparing the
            // AtomicBoolean to the "original" primitive.
            // This will automatically light up the "Save" button!
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

