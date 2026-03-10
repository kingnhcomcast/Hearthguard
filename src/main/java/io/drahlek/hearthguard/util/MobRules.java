package io.drahlek.hearthguard.util;

import net.minecraft.world.entity.EntityType;

public final class MobRules {
    private MobRules() {
    }

    public static boolean isBossMob(EntityType<?> type) {
        return type == EntityType.WITHER
                || type == EntityType.ENDER_DRAGON
                || type == EntityType.ELDER_GUARDIAN
                || type == EntityType.WARDEN;
    }
}
