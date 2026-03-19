package io.drahlek.hearthguard.mixin;

import io.drahlek.hearthguard.entity.FearDropTracker;
import io.drahlek.hearthguard.entity.SilentStateTracker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityFearDropMixin implements FearDropTracker, SilentStateTracker {
    @Unique
    private static final String HEARTHGUARD_DROPPED_FEAR_ITEM_TAG = "HearthguardDroppedFearItem";
    @Unique
    private static final String HEARTHGUARD_ORIGINAL_SILENT_TAG = "HearthguardOriginalSilent";
    @Unique
    private static final String HEARTHGUARD_FORCED_SILENT_TAG = "HearthguardForcedSilent";

    @Unique
    private boolean hearthguard$droppedFearItem;
    @Unique
    private boolean hearthguard$originalSilent;
    @Unique
    private boolean hearthguard$forcedSilent;

    @Override
    public boolean hearthguard$hasDroppedFearItem() {
        return this.hearthguard$droppedFearItem;
    }

    @Override
    public void hearthguard$setDroppedFearItem(boolean droppedFearItem) {
        this.hearthguard$droppedFearItem = droppedFearItem;
    }

    @Override
    public boolean hearthguard$getOriginalSilent() {
        return this.hearthguard$originalSilent;
    }

    @Override
    public void hearthguard$setOriginalSilent(boolean originalSilent) {
        this.hearthguard$originalSilent = originalSilent;
    }

    @Override
    public boolean hearthguard$isForcedSilent() {
        return this.hearthguard$forcedSilent;
    }

    @Override
    public void hearthguard$setForcedSilent(boolean forcedSilent) {
        this.hearthguard$forcedSilent = forcedSilent;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void hearthguard$saveFearDropFlag(CompoundTag output, CallbackInfo ci) {
        if (((Object) this) instanceof net.minecraft.world.entity.monster.Monster) {
            output.putBoolean(HEARTHGUARD_DROPPED_FEAR_ITEM_TAG, this.hearthguard$droppedFearItem);
            output.putBoolean(HEARTHGUARD_ORIGINAL_SILENT_TAG, this.hearthguard$originalSilent);
            output.putBoolean(HEARTHGUARD_FORCED_SILENT_TAG, this.hearthguard$forcedSilent);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void hearthguard$readFearDropFlag(CompoundTag input, CallbackInfo ci) {
        if (((Object) this) instanceof net.minecraft.world.entity.monster.Monster) {
            this.hearthguard$droppedFearItem = input.getBoolean(HEARTHGUARD_DROPPED_FEAR_ITEM_TAG);
            this.hearthguard$originalSilent = input.getBoolean(HEARTHGUARD_ORIGINAL_SILENT_TAG);
            this.hearthguard$forcedSilent = input.getBoolean(HEARTHGUARD_FORCED_SILENT_TAG);

            if (this.hearthguard$forcedSilent) {
                ((Entity) (Object) this).setSilent(this.hearthguard$originalSilent);
                this.hearthguard$forcedSilent = false;
            }
        }
    }
}
