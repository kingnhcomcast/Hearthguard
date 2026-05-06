package io.drahlek.hearthguard.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Makes these methods accessible to our code, otherwise they can not be invoked
 */
@Mixin(targets = "net.minecraft.world.entity.monster.Slime$SlimeMoveControl")
public interface SlimeMoveControlInvokerMixin {
    @Invoker("setDirection")
    void hearthguard$invokeSetDirection(float yRot, boolean isAggressive);

    @Invoker("setWantedMovement")
    void hearthguard$invokeSetWantedMovement(double speedModifier);
}
