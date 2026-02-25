package io.drahlek.hearthguard.ai.goal;

import io.drahlek.hearthguard.Hearthguard;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

public class FleeCampfireGoal extends Goal {
    public static final Logger LOGGER = LoggerFactory.getLogger(Hearthguard.MOD_ID);

    private final PathfinderMob mob;
    private final double fastSpeed;
    private final double slowSpeed;
    private final float radius;
    private BlockPos nearestFire;

    public FleeCampfireGoal(PathfinderMob mob, float radius, double fastSpeed, double slowSpeed) {
        this.mob = mob;
        this.radius = radius;
        this.fastSpeed = fastSpeed;
        this.slowSpeed = slowSpeed;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        nearestFire = findNearestLitCampfire();
        boolean foundCampfire = nearestFire != null;
        if(foundCampfire) log("Found lit campfire");
        return foundCampfire;
    }

    @Override
    public void start() {
        Vec3 target = DefaultRandomPos.getPosAway(
                mob,
                16,   //horizontal distance
                7,      //vertical distance
                Vec3.atCenterOf(nearestFire)
        );

        if (target != null) {
            mob.getNavigation().moveTo(target.x, target.y, target.z, fastSpeed);
        }


        log("Setting navigation to %s".formatted(target));


    }

    @Override
    public boolean canContinueToUse() {
        return !mob.getNavigation().isDone();
    }

    private BlockPos findNearestLitCampfire() {
        BlockPos mobPos = mob.blockPosition();
        Level level = mob.level();

        int r = (int) radius;

        for (BlockPos pos : BlockPos.betweenClosed(
                mobPos.offset(-r, -2, -r),
                mobPos.offset(r, 2, r))) {

            BlockState state = level.getBlockState(pos);

            if (state.getBlock() instanceof CampfireBlock
                    && state.getValue(CampfireBlock.LIT)) {
                return pos.immutable();
            }
        }

        return null;
    }

    @Override
    public void tick() {
        if (this.mob.distanceToSqr(nearestFire.getCenter()) < radius) {
            this.mob.getNavigation().setSpeedModifier(this.fastSpeed);
            log("running");
        } else {
            log("walking");
            this.mob.getNavigation().setSpeedModifier(this.slowSpeed);
        }
    }

    private void log(String msg) {
        String fullMsg = "%s:%s %s".formatted(mob.getDisplayName().getString(), mob.getId(), msg);
        LOGGER.info(fullMsg);
    }
}
