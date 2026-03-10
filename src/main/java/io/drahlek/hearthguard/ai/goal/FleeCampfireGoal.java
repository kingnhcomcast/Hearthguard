package io.drahlek.hearthguard.ai.goal;

import io.drahlek.hearthguard.Hearthguard;
import io.drahlek.hearthguard.config.HearthguardConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.EnumSet;

public class FleeCampfireGoal extends Goal {
    public static final Logger LOGGER = LoggerFactory.getLogger(Hearthguard.MOD_ID);
    private static final int CAMPFIRE_SCAN_COOLDOWN = 10;
    private static final int STARTLED_TIME = 20;    //how long to stay startled
    private static final int RECOVERY_TIME = 20;    //how long to wait after fleeing before mob can be started

    private final PathfinderMob mob;
    private BlockPos nearestFire;
    private FleeState fleeState = FleeState.IDLE;
    private int stateTimer = 0;
    private int scanCooldown = 0;


    public FleeCampfireGoal(PathfinderMob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!HearthguardConfig.getInstance().shouldApply(this.mob.getType())) {
            return false;
        }

        if (this.mob.level().dimension() != Level.OVERWORLD) {
            return false;
        }

        if (fleeState != FleeState.IDLE) return false;
        if (this.scanCooldown > 0) {
            this.scanCooldown--;
            return false;
        }

        nearestFire = findNearestLitCampfire();
        this.scanCooldown = CAMPFIRE_SCAN_COOLDOWN;
        if(nearestFire != null) {
            return hasLineOfSight(nearestFire);
        }

        return false;
    }

    @Override
    public void start() {
        this.mob.getNavigation().stop();
        this.scanCooldown = 0;

        fleeState = FleeState.STARTLED;
        log(fleeState.name());
        showFearParticles();
        playMobHurtSound(mob);
        recoil(mob);
        mob.setSilent(true);
        this.mob.animateHurt(0.0F);
    }

    @Override
    public void stop() {
        this.nearestFire = null;
        this.mob.getNavigation().stop();
        mob.setSilent(false);
        stateTimer = 0;
        scanCooldown = 0;
        fleeState = FleeState.IDLE;
        log(fleeState.name());
    }

    @Override
    public boolean canContinueToUse() {
        if (this.nearestFire == null) {
            return false;
        }

        if (!HearthguardConfig.getInstance().shouldApply(this.mob.getType())) {
            return false;
        }

        if (this.mob.level().dimension() != Level.OVERWORLD) {
            return false;
        }

        // 1. If the fire is gone or unlit, we stop fleeing.
        BlockState state = this.mob.level().getBlockState(nearestFire);
        if (state.isAir() || !state.hasProperty(CampfireBlock.LIT) || !state.getValue(CampfireBlock.LIT)) {
            return false; // Exit goal safely
        }

        return fleeState != FleeState.IDLE;
    }

    private BlockPos findNearestLitCampfire() {
        BlockPos mobPos = mob.blockPosition();
        Level level = mob.level();
        int r = getStartleDistance();
        BlockPos nearestPos = null;
        double nearestDistanceSqr = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(
                mobPos.offset(-r, -2, -r),
                mobPos.offset(r, 2, r))) {

            BlockState state = level.getBlockState(pos);

            if (state.getBlock() instanceof CampfireBlock
                    && state.getValue(CampfireBlock.LIT)) {
                double distanceSqr = pos.distSqr(mobPos);
                if (distanceSqr < nearestDistanceSqr) {
                    nearestDistanceSqr = distanceSqr;
                    nearestPos = pos.immutable();
                }
            }
        }

        return nearestPos;
    }

    private boolean hasLineOfSight(BlockPos pos) {
        Vec3 start = mob.getEyePosition();
        Vec3 end = Vec3.atCenterOf(pos);

        BlockHitResult result = mob.level().clip(
                new ClipContext(
                        start,
                        end,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        mob
                )
        );

        return result.getType() == HitResult.Type.MISS || result.getBlockPos().equals(pos);
    }

    @Override
    public void tick() {
        // 1. Safety check - if the fire was broken while fleeing
        if (this.nearestFire == null) return;

        switch(fleeState) {
            case IDLE -> {
            }
            case STARTLED -> {
                shiver();
                faceFire(mob);
                if(++stateTimer >= STARTLED_TIME) {
                    fleeState = FleeState.START_FLEEING;
                    log(fleeState.name());
                }
            }
            case START_FLEEING -> {
                playMobHurtSound(mob);
                jump();
                dropItem();
                showPoofParticles();
                setFleeDestinationAndFlee();
                fleeState = FleeState.FLEEING;
                log(fleeState.name());
            }
            case FLEEING -> {
                // Check if we are far enough away from the fire to stop fleeing
                double distSqr = this.mob.distanceToSqr(Vec3.atCenterOf(nearestFire));
                int dangerDistance = getDangerDistance();
                boolean stillInDanger = distSqr < (dangerDistance * dangerDistance);

                // transition to recovering once out of danger
                if (!stillInDanger) {
                    fleeState = FleeState.RECOVERING;
                    log(fleeState.name());
                    stateTimer = 0;
                }
                //else if we are still in danger, but no longer moving, find another destination
                else if(mob.getNavigation().isDone()) {
                    setFleeDestinationAndFlee();
                } else {
                    flee();
                }
            }
            case RECOVERING -> {
                if(++stateTimer >= RECOVERY_TIME) {
                    fleeState = FleeState.IDLE;
                    log(fleeState.name());
                }
            }
        }
    }

    private void flee() {
        // Handle Dynamic Speed
        double distSqr = this.mob.distanceToSqr(nearestFire.getCenter());
        int dangerDistance = getDangerDistance();

        if(distSqr >= dangerDistance * dangerDistance) {
            this.mob.getNavigation().stop();
            fleeState = FleeState.RECOVERING;
            log(fleeState.name());
            stateTimer = 0;
            return;
        }

        int startleDistance = getStartleDistance();
        this.mob.getNavigation().setSpeedModifier(
                distSqr < (startleDistance * 1.5 * startleDistance * 1.5) ? getFastSpeed() : getSlowSpeed()
        );
    }

    private void setFleeDestinationAndFlee() {
        if (this.mob.getNavigation().isDone()) {
            //Vec3 target = LandRandomPos.getPosAway(mob, 16, 7, Vec3.atCenterOf(nearestFire));
            Vec3 target = findValidFleeTarget();

            if (target != null) {
                this.mob.getNavigation().moveTo(target.x, target.y, target.z, getFastSpeed());
            } else {
                log("No target found");
                fleeState = FleeState.RECOVERING;
                log(fleeState.name());
            }
        }
    }

    private void showPoofParticles() {
        if (this.mob.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            // VISUAL: "Shock" Poof
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF,
                    this.mob.getX(), this.mob.getY() + 0.5, this.mob.getZ(),
                    5, 0.1, 0.1, 0.1, 0.05);
        }
    }

    private void dropItem() {
        if (this.mob.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            if (this.mob.getRandom().nextFloat() < 0.25F) {
                // 1. Create the LootParams (Context for the drop)
                // In 1.21.1, we use LootParams.Builder
                net.minecraft.world.level.storage.loot.LootParams lootParams = new net.minecraft.world.level.storage.loot.LootParams.Builder(serverLevel)
                        .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, this.mob.position())
                        .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY, this.mob)
                        .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.DAMAGE_SOURCE, this.mob.damageSources().generic())
                        .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.ENTITY);

                // 2. Retrieve the Loot Table using Reloadable Registries
                // Note: .getLootTables() is deprecated in 1.21; use reloadableRegistries()
                this.mob.getLootTable().ifPresent(lootTableKey -> {
                    LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(lootTableKey);

                    // 3. Generate and spawn the items
                    java.util.List<ItemStack> drops = lootTable.getRandomItems(lootParams);
                    if (drops.isEmpty()) {
                        return;
                    }

                    ItemStack stack = drops.getFirst();
                    stack.setCount(1);

                    // Create the ItemEntity manually
                    net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                            serverLevel,
                            this.mob.getX(), this.mob.getY(), this.mob.getZ(),
                            stack
                    );

                    // dont allow item pick up for 100 ticks, zombies might pick up what they just dropped
                    itemEntity.setPickUpDelay(100);

                    // Give it a little "toss" so it doesn't just sit under the mob's feet
                    double xVel = (this.mob.getRandom().nextDouble() - 0.5) * 0.2;
                    double zVel = (this.mob.getRandom().nextDouble() - 0.5) * 0.2;
                    itemEntity.setDeltaMovement(xVel, 0.4, zVel);

                    serverLevel.addFreshEntity(itemEntity);
                });
            }
        }
    }

    private void jump() {
        if (this.mob.onGround()) {
            Vec3 firePos = Vec3.atCenterOf(nearestFire);
            // 1. Direction: From the fire TO the mob
            Vec3 awayDir = this.mob.position().subtract(firePos).normalize();

            // 2. Strength: Horizontal leap + Vertical lift
            double horizontalStrength = 0.5;
            double verticalStrength = 0.42;

            // 3. Apply the velocity
            this.mob.setDeltaMovement(
                    awayDir.x * horizontalStrength,
                    verticalStrength,
                    awayDir.z * horizontalStrength
            );

            // 4. This is the replacement for hasImpulse
            // It tells the server to sync this motion to all nearby players
            this.mob.hurtMarked = true;
        }
    }

    private void shiver() {
        double t = mob.tickCount * 1.1; // increase multiplier to go faster
        double dx = Math.sin(t) * 0.05;  //distance they shake
        double dz = Math.sin(t * 1.3) * 0.01; // slightly different frequency for natural effect
        mob.setDeltaMovement(dx, 0, dz);
    }

    private void showFearParticles() {
        if (this.mob.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.ANGRY_VILLAGER,
                    this.mob.getX(), this.mob.getEyeY() + 0.5, this.mob.getZ(),
                    3, 0.2, 0.2, 0.2, 0.0);
        }
    }

    private void log(String msg) {
        String fullMsg = "%s:%s %s".formatted(mob.getDisplayName().getString(), mob.getId(), msg);
        LOGGER.debug(fullMsg);
    }

    public static void playMobHurtSound(Mob mob) {
        mob.setSilent(false);
        try {
            Method method = mob.getClass().getDeclaredMethod("getHurtSound", DamageSource.class);
            method.setAccessible(true);

            SoundEvent sound = (SoundEvent) method.invoke(
                    mob,
                    mob.damageSources().generic()
            );

            if (sound != null) {
                mob.playSound(sound, 1.0F, 1.0F);
            }

        } catch (Exception e) {
            Hearthguard.LOGGER.error("Error while playing mob hurt sound: {}", e.getMessage());
        }
        mob.setSilent(true);
    }

    private void faceFire(Mob mob) {
        if (this.nearestFire == null) return;

        // 1. Calculate the angle (Yaw) to the fire
        double dX = nearestFire.getX() + 0.5 - mob.getX();
        double dZ = nearestFire.getZ() + 0.5 - mob.getZ();

        // atan2 gives the angle in radians, we convert to degrees and adjust by -90 for MC's coordinate system
        float targetYaw = (float) (net.minecraft.util.Mth.atan2(dZ, dX) * (180.0 / Math.PI)) - 90.0F;

        // 2. Snap all rotation variables to that angle immediately
        mob.setYRot(targetYaw);
        mob.yHeadRot = targetYaw;
        mob.yBodyRot = targetYaw;

        // 3. Update the 'Old' rotations to prevent the "interpolation slide" (the slow turn)
        mob.yRotO = targetYaw;
        mob.yHeadRotO = targetYaw;
        mob.yBodyRotO = targetYaw;

        // Optional: Still call this so the AI "internalizes" where it's looking
        mob.getLookControl().setLookAt(Vec3.atCenterOf(nearestFire));
    }

    private void recoil(Mob mob) {
        Vec3 firePos = Vec3.atCenterOf(nearestFire);
        Vec3 away = mob.position().subtract(firePos).normalize();
        mob.push(away.x * 0.3, 0.1, away.z * 0.3); // backstep slightly
    }

    private Vec3 findValidFleeTarget() {
        Vec3 firePos = Vec3.atCenterOf(nearestFire);
        int dangerDistance = getDangerDistance();
        double minDistanceSqr = dangerDistance * dangerDistance;

        // Try up to 10 times to find a "good enough" spot
        for (int i = 0; i < 10; i++) {
            // Find a position a max of double dangerDistance away and 7 vertical
            Vec3 target = LandRandomPos.getPosAway(this.mob, (dangerDistance * 2), 7, firePos);

            if (target != null) {
                double distToFireSqr = target.distanceToSqr(firePos);

                // Validate: Is this target actually far enough away?
                if (distToFireSqr >= minDistanceSqr) {
                    return target; // Success!
                }
            }
        }

        // Fallback: If 10 tries fail, return null (mob is likely cornered)
        return null;
    }

    private int getStartleDistance() {
        return HearthguardConfig.getInstance().getRange();
    }

    private int getDangerDistance() {
        return getStartleDistance() * 2;
    }

    private double getFastSpeed() {
        return HearthguardConfig.getInstance().getFleeFastSpeed();
    }

    private double getSlowSpeed() {
        return HearthguardConfig.getInstance().getFleeSlowSpeed();
    }

    private enum FleeState {
        IDLE,
        STARTLED,
        START_FLEEING,
        FLEEING,
        RECOVERING
    }
}
