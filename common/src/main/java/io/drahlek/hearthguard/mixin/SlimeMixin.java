package io.drahlek.hearthguard.mixin;

import io.drahlek.hearthguard.ai.FearGoalManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Slime.class)
public class SlimeMixin extends Mob {
    protected SlimeMixin(EntityType<? extends Mob> type, Level level) {
        super(type, level);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void hearthguard$addCampfireFear(CallbackInfo ci) {
        FearGoalManager.refreshSlime((Slime) (Object) this);
    }
}
