package io.drahlek.hearthguard.util;

import io.drahlek.hearthguard.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class MobUtil {
    private MobUtil() {
    }

    public static boolean isBossMob(EntityType<?> type) {
        return type == EntityType.WITHER
                || type == EntityType.ENDER_DRAGON
                || type == EntityType.ELDER_GUARDIAN
                || type == EntityType.WARDEN;
    }

    public static @NonNull Map<String, List<EntityType<?>>> getMobsByMod() {
        Map<String, List<EntityType<?>>> mobsByMod = new HashMap<>();

        for (EntityType<?> type : getConfigurableMonsterTypes()) {
            Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (key == null) {
                continue;
            }

            String modid = Services.PLATFORM.getModName(key.getNamespace());
            mobsByMod.computeIfAbsent(modid, k -> new ArrayList<>()).add(type);
        }

        //sort the lists
        for (Map.Entry<String, List<EntityType<?>>> entry : mobsByMod.entrySet()) {
            entry.getValue().sort(Comparator.comparing(type -> type.getDescription().getString()));
        }

        return mobsByMod;
    }

    public static @NonNull List<String> getMonsterMobIds() {
        List<String> mobIds = new ArrayList<>();

        for (EntityType<?> type : getConfigurableMonsterTypes()) {
            Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (key == null) {
                continue;
            }

            mobIds.add(key.toString().toLowerCase(Locale.ROOT));
        }

        Collections.sort(mobIds);
        return mobIds;
    }

    private static @NonNull List<EntityType<?>> getConfigurableMonsterTypes() {
        List<EntityType<?>> types = new ArrayList<>();

        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (type.getCategory() != MobCategory.MONSTER) {
                continue;
            }

            if (isBossMob(type)) {
                continue;
            }

            types.add(type);
        }

        return types;
    }
}
