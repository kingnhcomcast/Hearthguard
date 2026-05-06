package io.drahlek.hearthguard.ai.goal;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class FleeCampfireGoal extends AbstractCampfireFleeGoal<PathfinderMob> {
    public FleeCampfireGoal(PathfinderMob mob) {
        super(mob);
    }

    @Override
    protected void startFleeing() {
        setFleeDestinationAndFlee();
    }

    @Override
    protected void continueFleeing() {
        if (this.mob.getNavigation().isDone()) {
            setFleeDestinationAndFlee();
            return;
        }

        double distSqr = this.mob.distanceToSqr(this.nearestFire.getCenter());
        int startleDistance = getStartleDistance();
        this.mob.getNavigation().setSpeedModifier(
                distSqr < (startleDistance * 1.5D * startleDistance * 1.5D) ? getFastSpeed() : getSlowSpeed()
        );
    }

    private void setFleeDestinationAndFlee() {
        if (!this.mob.getNavigation().isDone()) {
            return;
        }

        Vec3 target = findValidFleeTarget();
        if (target != null) {
            this.mob.getNavigation().moveTo(target.x, target.y, target.z, getFastSpeed());
            return;
        }

        log("No target found");
        startRecovering();
    }

    private Vec3 findValidFleeTarget() {
        Vec3 firePos = Vec3.atCenterOf(this.nearestFire);
        int dangerDistance = getDangerDistance();
        double minDistanceSqr = (double) dangerDistance * dangerDistance;

        for (int i = 0; i < 10; i++) {
            Vec3 target = LandRandomPos.getPosAway(this.mob, dangerDistance * 2, 7, firePos);

            if (target != null && target.distanceToSqr(firePos) >= minDistanceSqr) {
                return target;
            }
        }

        return null;
    }
}
