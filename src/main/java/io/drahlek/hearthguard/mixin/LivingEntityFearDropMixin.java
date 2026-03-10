package io.drahlek.hearthguard.mixin;

import io.drahlek.hearthguard.entity.FearDropTracker;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityFearDropMixin implements FearDropTracker {
    @Unique
    private static final String HEARTHGUARD_DROPPED_FEAR_ITEM_TAG = "HearthguardDroppedFearItem";

    @Unique
    private boolean hearthguard$droppedFearItem;

    @Override
    public boolean hearthguard$hasDroppedFearItem() {
        return this.hearthguard$droppedFearItem;
    }

    @Override
    public void hearthguard$setDroppedFearItem(boolean droppedFearItem) {
        this.hearthguard$droppedFearItem = droppedFearItem;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void hearthguard$saveFearDropFlag(ValueOutput output, CallbackInfo ci) {
        if (((Object) this) instanceof net.minecraft.world.entity.monster.Monster) {
            output.putBoolean(HEARTHGUARD_DROPPED_FEAR_ITEM_TAG, this.hearthguard$droppedFearItem);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void hearthguard$readFearDropFlag(ValueInput input, CallbackInfo ci) {
        if (((Object) this) instanceof net.minecraft.world.entity.monster.Monster) {
            this.hearthguard$droppedFearItem = input.getBooleanOr(HEARTHGUARD_DROPPED_FEAR_ITEM_TAG, false);
        }
    }
}
