package io.drahlek.hearthguard.ai.goal;

import io.drahlek.hearthguard.mixin.SlimeMoveControlInvokerMixin;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class SlimeFleeCampfireGoal extends AbstractCampfireFleeGoal<Slime> {
    public SlimeFleeCampfireGoal(Slime slime) {
        super(slime);
    }

    @Override
    protected boolean canUseMovementControl() {
        return this.mob.getMoveControl() instanceof SlimeMoveControlInvokerMixin;
    }

    @Override
    protected void stopMovement() {
        super.stopMovement();
        this.mob.getMoveControl().setWait();
    }

    @Override
    protected void startFleeing() {
        setFleeMovement();
    }

    @Override
    protected void continueFleeing() {
        setFleeMovement();
    }

    @Override
    protected void onStartRecovering() {
        this.mob.getMoveControl().setWait();
    }

    @Override
    protected double getJumpHorizontalStrength() {
        return 0.35D + this.mob.getSize() * 0.05D;
    }

    @Override
    protected ItemStack getFallbackFearDrop() {
        if (this.mob.getType() == EntityType.SLIME) {
            return new ItemStack(Items.SLIME_BALL);
        }

        if (this.mob.getType() == EntityType.MAGMA_CUBE) {
            return new ItemStack(Items.MAGMA_CREAM);
        }

        return ItemStack.EMPTY;
    }

    private void setFleeMovement() {
        MoveControl moveControl = this.mob.getMoveControl();
        if (!(moveControl instanceof SlimeMoveControlInvokerMixin slimeMoveControl)) {
            return;
        }

        Vec3 away = this.mob.position().subtract(Vec3.atCenterOf(this.nearestFire));
        if (away.horizontalDistanceSqr() < 1.0E-4D) {
            away = new Vec3(this.mob.getRandom().nextDouble() - 0.5D, 0.0D, this.mob.getRandom().nextDouble() - 0.5D);
        }

        float awayYaw = (float) (net.minecraft.util.Mth.atan2(away.z, away.x) * (180.0D / Math.PI)) - 90.0F;
        double distSqr = this.mob.distanceToSqr(this.nearestFire.getCenter());
        int startleDistance = getStartleDistance();
        double speed = distSqr < (startleDistance * 1.5D * startleDistance * 1.5D)
                ? getFastSpeed()
                : getSlowSpeed();

        slimeMoveControl.hearthguard$invokeSetDirection(awayYaw, true);
        slimeMoveControl.hearthguard$invokeSetWantedMovement(speed);
    }
}
