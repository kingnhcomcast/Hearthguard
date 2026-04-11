package io.drahlek.hearthguard.mixin;

import io.drahlek.hearthguard.ai.FearGoalManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Monster.class)
public class MonsterMixin extends PathfinderMob {
    protected MonsterMixin(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void hearthguard_addCampfireFear(CallbackInfo ci) {
        FearGoalManager.refreshMonster((Monster) (Object) this);
    }
}
