package io.drahlek.hearthguard.ai;

import io.drahlek.hearthguard.ai.goal.FleeCampfireGoal;
import io.drahlek.hearthguard.config.HearthguardConfig;
import io.drahlek.hearthguard.mixin.MobAccessorMixin;
import io.drahlek.hearthguard.util.MobUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Monster;

public final class FearGoalManager {
    private FearGoalManager() {
    }

    public static void refreshLoadedMonsters(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof Monster monster) {
                    refreshMonster(monster);
                }
            }
        }
    }

    public static void refreshMonster(Monster monster) {
        if (MobUtil.isBossMob(monster.getType())) {
            removeFearGoal(monster);
            return;
        }

        boolean shouldApplyFear = HearthguardConfig.getInstance().shouldApply(monster.getType());
        Goal fearGoal = getFearGoal(monster);
        GoalSelector goalSelector = ((MobAccessorMixin) monster).hearthguard$getGoalSelector();

        if (shouldApplyFear) {
            if (fearGoal == null) {
                goalSelector.addGoal(1, new FleeCampfireGoal(monster));
            }
            return;
        }

        if (fearGoal != null) {
            goalSelector.removeGoal(fearGoal);
        }
    }

    private static void removeFearGoal(Monster monster) {
        GoalSelector goalSelector = ((MobAccessorMixin) monster).hearthguard$getGoalSelector();
        Goal fearGoal = getFearGoal(monster);
        if (fearGoal != null) {
            goalSelector.removeGoal(fearGoal);
        }
    }

    private static Goal getFearGoal(Monster monster) {
        GoalSelector goalSelector = ((MobAccessorMixin) monster).hearthguard$getGoalSelector();
        for (WrappedGoal wrappedGoal : goalSelector.getAvailableGoals()) {
            Goal goal = wrappedGoal.getGoal();
            if (goal instanceof FleeCampfireGoal) {
                return goal;
            }
        }
        return null;
    }
}
