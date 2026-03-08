package io.drahlek.hearthguard.ai.goal;

import io.drahlek.hearthguard.Hearthguard;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
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

//BUG if they are far they might shiver, but never flee, just stay there
public class FleeCampfireGoal extends Goal {
    public static final Logger LOGGER = LoggerFactory.getLogger(Hearthguard.MOD_ID);

    private final PathfinderMob mob;
    private final double fastSpeed;
    private final double slowSpeed;
    private final float radius;
    private boolean isFleeing = false;
    private BlockPos nearestFire;

    //Variable to track the startle duration (20 ticks = 1 second)
    private int startleTicks;

    public FleeCampfireGoal(PathfinderMob mob, float radius, double fastSpeed, double slowSpeed) {
        this.mob = mob;
        this.radius = radius;
        this.fastSpeed = fastSpeed;
        this.slowSpeed = slowSpeed;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // If we are already fleeing, don't try to "re-start" the goal
        if (isFleeing) return false;

        nearestFire = findNearestLitCampfire();
        if(nearestFire != null) {
            return hasLineOfSight(nearestFire);
        }

        return false;
    }

    @Override
    public void start() {
        log("start");
        this.isFleeing = true;
        this.startleTicks = 20;

        playMobHurtSound(mob);
        recoil(mob);
        mob.setSilent(true);
        this.mob.animateHurt(0.0F);
    }

    @Override
    public void stop() {
        log("stop");
        this.isFleeing = false; // Unlock
        this.nearestFire = null;
        this.mob.getNavigation().stop();
        this.startleTicks = 0;
        mob.setSilent(false);
    }

    @Override
    public boolean canContinueToUse() {
        // 1. If the fire is gone or unlit, we stop fleeing.
        //TODO this will crash if the campfire has been broken - blockstate at nearesetFire is AIR and .LIT does not exist
        if (nearestFire == null || !this.mob.level().getBlockState(nearestFire).getValue(CampfireBlock.LIT)) {
            log("canContinueToUse false no campfire");
            return false;
        }

        // 2. While we are in the startle phase, we MUST continue.
        if (this.startleTicks > 0) {
            return true;
        }

        // 3. Once startle is over, we stay in the goal as long as we are still
        // within the danger radius, even if navigation is "thinking".
        double distSqr = this.mob.distanceToSqr(Vec3.atCenterOf(nearestFire));
        boolean stillInRange = distSqr < (radius * radius);

        log("canContinueToUse: startleDone, inRange: %s, navDone: %s".formatted(stillInRange, mob.getNavigation().isDone()));

        // Continue if we haven't finished moving OR if we are still too close to the fire
        return stillInRange || !this.mob.getNavigation().isDone();
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

        // 1. STARTLE PHASE (The "Frozen" Moment)
        if (this.startleTicks > 0) {
            this.startleTicks--;
            showFearParticles();
            shiver();
            faceFire(mob);
            this.mob.getNavigation().stop();
        }

        // 2. TRANSITION: The moment they bolt
        else if (this.startleTicks == 0) {
            playMobHurtSound(mob);
            jump();
            dropItem();
            showPoofParticles();
            this.startleTicks--; // Set to -1 so this block only runs ONCE
        } else {
            flee();
        }
    }

    private void flee() {
        // Only calculate a new path if we aren't already moving
        if (this.mob.getNavigation().isDone()) {
            Vec3 target = DefaultRandomPos.getPosAway(mob, 16, 7, Vec3.atCenterOf(nearestFire));
            if (target != null) {
                this.mob.getNavigation().moveTo(target.x, target.y, target.z, this.fastSpeed);
            }
        }

        // Handle Dynamic Speed
        double distSqr = this.mob.distanceToSqr(nearestFire.getCenter());
        this.mob.getNavigation().setSpeedModifier(distSqr < (radius * radius) ? this.fastSpeed : this.slowSpeed);

        // Handle Dynamic Looking (Occasional peeks back)
        //TODO is this working
        if (this.mob.tickCount % 40 == 0) {
            // Look back at the fire every 2 seconds
            log("look back at fire");
            this.mob.getLookControl().setLookAt(Vec3.atCenterOf(nearestFire));
        } else {
            // Otherwise look where we are going
            Vec3 moveVec = this.mob.getDeltaMovement();
            if (moveVec.lengthSqr() > 0.01) {
                this.mob.getLookControl().setLookAt(
                        this.mob.getX() + moveVec.x,
                        this.mob.getY() + this.mob.getEyeHeight(),
                        this.mob.getZ() + moveVec.z
                );
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
                    ItemStack stack = lootTable.getRandomItems(lootParams).getFirst();
                    stack.setCount(1);

                    // Create the ItemEntity manually
                    net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                            serverLevel,
                            this.mob.getX(), this.mob.getY(), this.mob.getZ(),
                            stack
                    );

                    // This is the key: 100 ticks = 5 seconds where NO ONE (especially the zombie) can pick it up
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

    //TODO instead of jumping straight up, jump in direction they are going to flee
    private void jump() {
        if (this.mob.onGround()) {
            log("jumping");
            this.mob.jumpFromGround();
        }
    }

    private void shiver() {
        double t = mob.tickCount * 1.1; // increase multiplier to go faster
        double dx = Math.sin(t) * 0.05;  //distance they shake
        double dz = Math.sin(t * 1.3) * 0.01; // slightly different frequency for natural effect
        mob.setDeltaMovement(dx, 0, dz);
    }

    private void showFearParticles() {
        if (this.startleTicks == 19 && this.mob.level() instanceof ServerLevel serverLevel) {
            log("Fear Particles");
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.ANGRY_VILLAGER,
                    this.mob.getX(), this.mob.getEyeY() + 0.5, this.mob.getZ(),
                    3, 0.2, 0.2, 0.2, 0.0);
        }
    }

    private void log(String msg) {
        String fullMsg = "%s:%s %s".formatted(mob.getDisplayName().getString(), mob.getId(), msg);
        LOGGER.info(fullMsg);
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
}
