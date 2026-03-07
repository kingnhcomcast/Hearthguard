package io.drahlek.hearthguard.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HearthguardModMenu implements ModMenuApi {
    Screen parent;

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::createConfigScreen;
    }

    private Screen createConfigScreen(Screen parent) {
        this.parent = parent;

        HearthguardConfig config = HearthguardConfig.getInstance();

        HearthguardConfig.Mode currentMode = config.getModeEnum() != null
                ? config.getModeEnum() : HearthguardConfig.Mode.WHITELIST;

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("HearthGuard Options"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // ===== General Settings =====
        buildGeneralTab(builder, entryBuilder, config, currentMode);

        Map<String, List<String>> mobsByMod = new HashMap<>();

        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (type.getCategory() != MobCategory.MONSTER)
                continue;

            Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(type);

            //String modid = BuiltInRegistries.ENTITY_TYPE.getKey(type).getNamespace();
            String modid = key.getNamespace();
            ModContainer mod = FabricLoader.getInstance().getModContainer(modid).orElse(null);
            if (mod != null) {
                modid = mod.getMetadata().getName(); // "Minecraft" or "Better Animals"
            }

            String name = type.getDescription().getString();

            mobsByMod.computeIfAbsent(modid, k -> new ArrayList<>()).add(name);
        }

        List<String> sortedMods = new ArrayList<>(mobsByMod.keySet());
        Collections.sort(sortedMods);

        for (String modid : sortedMods) {

            // ===== Mob List =====
            Map<String, BooleanListEntry> mobEntries = new HashMap<>();

            ConfigCategory modCategory = builder.getOrCreateCategory(Component.literal(modid));

            for (String mobName : mobsByMod.get(modid)) {

                String idString = mobName;
                boolean selected = config.getMobs().contains(idString);

                BooleanListEntry entry =
                        entryBuilder.startBooleanToggle(Component.literal(idString), selected)
                                .setDefaultValue(false)
                                .setSaveConsumer(sel -> {
                                    if (sel) {
                                        config.getMobs().add(idString);
                                    }
                                    else {
                                        config.getMobs().remove(idString);
                                    }
                                })
                                .build();

                mobEntries.put(idString, entry);

                modCategory.addEntry(entry);
            }

            boolean allSelected = !mobEntries.isEmpty() && mobEntries.values().stream().allMatch(BooleanListEntry::getValue);
            String label = allSelected ? "Deselect All" : "Select All";
            ButtonEntry toggleBtn = new ButtonEntry(Component.literal(label), !allSelected, (shouldSelect) -> {
                // 1. Update the actual config data immediately
                if (shouldSelect) {
                    config.getMobs().addAll(getMobIds()); // Helper you already have
                } else {
                    config.getMobs().clear();
                }

                // 2. Cloth Config doesn't know the data changed.
                // We MUST tell the entries to look at the config again.
                mobEntries.forEach((id, entry) -> {
                    setBooleanEntryValue(entry, shouldSelect);
                });
            });

            modCategory.getEntries().add(0, toggleBtn);
        }

        // Save config when pressing Done
        builder.setSavingRunnable(config::save);


// Return the builder directly
        return builder.build();
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

        // Mode selector
        general.addEntry(
                entryBuilder.startEnumSelector(Component.literal("Mode"), HearthguardConfig.Mode.class, currentMode)
                        .setDefaultValue(HearthguardConfig.Mode.WHITELIST)
                        .setSaveConsumer(config::setModeEnum)
                        .build()
        );
    }

    private List<String> getMobIds() {
        List<String> ids = new ArrayList<>();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (type.getCategory() != MobCategory.MONSTER) continue;

            ids.add(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
        }

        return ids;
    }

    private void setBooleanEntryValue(BooleanListEntry entry, boolean value) {
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