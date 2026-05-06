package io.drahlek.hearthguard.ai.goal;

import io.drahlek.hearthguard.Constants;
import io.drahlek.hearthguard.config.HearthguardConfig;
import io.drahlek.hearthguard.entity.FearDropTracker;
import io.drahlek.hearthguard.entity.SilentStateTracker;
import io.drahlek.hearthguard.mixin.MobInvokerMixin;
import io.drahlek.hearthguard.util.MobUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public abstract class AbstractCampfireFleeGoal<T extends Mob> extends Goal {
    private static final int CAMPFIRE_SCAN_COOLDOWN = 10;
    private static final int STARTLED_TIME = 20;
    private static final int RECOVERY_TIME = 20;
    private static final TagKey<Block> HEARTHFIRES = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath("cinderstride", "hearthfires")
    );

    protected final T mob;
    protected BlockPos nearestFire;

    private FleeState fleeState = FleeState.IDLE;
    private int stateTimer = 0;
    private int scanCooldown = 0;

    protected AbstractCampfireFleeGoal(T mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (MobUtil.isBossMob(this.mob.getType())) {
            return false;
        }

        if (!HearthguardConfig.getInstance().shouldApply(this.mob.getType())) {
            return false;
        }

        if (this.mob.level().dimension() != Level.OVERWORLD) {
            return false;
        }

        if (this.fleeState != FleeState.IDLE) {
            return false;
        }

        if (!canUseMovementControl()) {
            return false;
        }

        if (this.scanCooldown > 0) {
            this.scanCooldown--;
            return false;
        }

        this.nearestFire = findNearestLitCampfire();
        this.scanCooldown = CAMPFIRE_SCAN_COOLDOWN;
        return this.nearestFire != null && hasLineOfSight(this.nearestFire);
    }

    @Override
    public void start() {
        stopMovement();
        this.scanCooldown = 0;

        if (this.mob instanceof SilentStateTracker silentStateTracker) {
            silentStateTracker.hearthguard$setOriginalSilent(this.mob.isSilent());
            silentStateTracker.hearthguard$setForcedSilent(true);
        }

        setFleeState(FleeState.STARTLED);
        showFearParticles();
        playMobHurtSound(this.mob);
        recoil();
        this.mob.setSilent(true);
        this.mob.animateHurt(0.0F);
    }

    @Override
    public void stop() {
        stopMovement();
        this.nearestFire = null;
        if (this.mob instanceof SilentStateTracker silentStateTracker) {
            this.mob.setSilent(silentStateTracker.hearthguard$getOriginalSilent());
            silentStateTracker.hearthguard$setForcedSilent(false);
        }
        this.stateTimer = 0;
        this.scanCooldown = 0;
        setFleeState(FleeState.IDLE);
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

        if (!canUseMovementControl()) {
            return false;
        }

        BlockState state = this.mob.level().getBlockState(this.nearestFire);
        if (state.isAir() || !state.hasProperty(CampfireBlock.LIT) || !state.getValue(CampfireBlock.LIT)) {
            return false;
        }

        return this.fleeState != FleeState.IDLE;
    }

    @Override
    public void tick() {
        if (this.nearestFire == null) {
            return;
        }

        switch (this.fleeState) {
            case IDLE -> {
            }
            case STARTLED -> {
                shiver();
                faceFire();
                if (++this.stateTimer >= STARTLED_TIME) {
                    setFleeState(FleeState.START_FLEEING);
                }
            }
            case START_FLEEING -> {
                playMobHurtSound(this.mob);
                jumpAway();
                dropItem();
                showPoofParticles();
                startFleeing();
                setFleeState(FleeState.FLEEING);
            }
            case FLEEING -> {
                double distSqr = this.mob.distanceToSqr(Vec3.atCenterOf(this.nearestFire));
                int dangerDistance = getDangerDistance();
                boolean stillInDanger = distSqr < (double) dangerDistance * dangerDistance;

                if (!stillInDanger) {
                    startRecovering();
                } else {
                    continueFleeing();
                }
            }
            case RECOVERING -> {
                if (++this.stateTimer >= RECOVERY_TIME) {
                    setFleeState(FleeState.IDLE);
                }
            }
        }
    }

    protected boolean canUseMovementControl() {
        return true;
    }

    protected void stopMovement() {
        this.mob.getNavigation().stop();
    }

    protected abstract void startFleeing();

    protected abstract void continueFleeing();

    protected void startRecovering() {
        setFleeState(FleeState.RECOVERING);
        this.stateTimer = 0;
        onStartRecovering();
    }

    protected void onStartRecovering() {
    }

    protected void log(String msg) {
        Identifier typeId = BuiltInRegistries.ENTITY_TYPE.getKey(this.mob.getType());
        String typeStr = typeId != null ? typeId.toString() : "<unknown>";
        String fullMsg = "%s:%s [%s] %s".formatted(this.mob.getDisplayName().getString(), this.mob.getId(), typeStr, msg);
        Constants.LOG.debug(fullMsg);
    }

    protected int getStartleDistance() {
        if (this.nearestFire == null) {
            return getMaxStartleDistance();
        }

        return getStartleDistance(this.mob.level().getBlockState(this.nearestFire));
    }

    protected int getDangerDistance() {
        return getStartleDistance() * 2;
    }

    protected double getFastSpeed() {
        return HearthguardConfig.getInstance().getFleeFastSpeed();
    }

    protected double getSlowSpeed() {
        return HearthguardConfig.getInstance().getFleeSlowSpeed();
    }

    protected ItemStack getFallbackFearDrop() {
        return ItemStack.EMPTY;
    }

    protected ItemStack chooseFearDrop(List<ItemStack> drops) {
        return drops.get(this.mob.getRandom().nextInt(drops.size()));
    }

    protected double getJumpHorizontalStrength() {
        return 0.5D;
    }

    private void setFleeState(FleeState fleeState) {
        this.fleeState = fleeState;
        log(fleeState.name());
    }

    private BlockPos findNearestLitCampfire() {
        BlockPos mobPos = this.mob.blockPosition();
        Level level = this.mob.level();
        int r = getMaxStartleDistance();
        BlockPos nearestPos = null;
        double nearestDistanceSqr = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(
                mobPos.offset(-r, -2, -r),
                mobPos.offset(r, 2, r))) {

            BlockState state = level.getBlockState(pos);

            if (state.getBlock() instanceof CampfireBlock
                    && state.getValue(CampfireBlock.LIT)) {
                int startleDistance = getStartleDistance(state);
                double distanceSqr = pos.distSqr(mobPos);
                if (distanceSqr <= (double) startleDistance * startleDistance
                        && distanceSqr < nearestDistanceSqr) {
                    nearestDistanceSqr = distanceSqr;
                    nearestPos = pos.immutable();
                }
            }
        }

        return nearestPos;
    }

    private boolean hasLineOfSight(BlockPos pos) {
        Vec3 start = this.mob.getEyePosition();
        Vec3 end = Vec3.atCenterOf(pos);

        BlockHitResult result = this.mob.level().clip(
                new ClipContext(
                        start,
                        end,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        this.mob
                )
        );

        return result.getType() == HitResult.Type.MISS || result.getBlockPos().equals(pos);
    }

    private void jumpAway() {
        if (this.mob.onGround()) {
            Vec3 firePos = Vec3.atCenterOf(this.nearestFire);
            Vec3 awayDir = this.mob.position().subtract(firePos).normalize();
            double horizontalStrength = getJumpHorizontalStrength();
            double verticalStrength = 0.42D;
            this.mob.setDeltaMovement(
                    awayDir.x * horizontalStrength,
                    verticalStrength,
                    awayDir.z * horizontalStrength
            );
            this.mob.hurtMarked = true;
        }
    }

    private void shiver() {
        double t = this.mob.tickCount * 1.1D;
        double dx = Math.sin(t) * 0.05D;
        double dz = Math.sin(t * 1.3D) * 0.01D;
        this.mob.setDeltaMovement(dx, 0.0D, dz);
    }

    private void recoil() {
        Vec3 firePos = Vec3.atCenterOf(this.nearestFire);
        Vec3 away = this.mob.position().subtract(firePos).normalize();
        this.mob.push(away.x * 0.3D, 0.1D, away.z * 0.3D);
    }

    private void faceFire() {
        double dX = this.nearestFire.getX() + 0.5D - this.mob.getX();
        double dZ = this.nearestFire.getZ() + 0.5D - this.mob.getZ();
        float targetYaw = (float) (net.minecraft.util.Mth.atan2(dZ, dX) * (180.0D / Math.PI)) - 90.0F;

        this.mob.setYRot(targetYaw);
        this.mob.yHeadRot = targetYaw;
        this.mob.yBodyRot = targetYaw;
        this.mob.yRotO = targetYaw;
        this.mob.yHeadRotO = targetYaw;
        this.mob.yBodyRotO = targetYaw;
        this.mob.getLookControl().setLookAt(Vec3.atCenterOf(this.nearestFire));
    }

    private void showFearParticles() {
        if (this.mob.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.ANGRY_VILLAGER,
                    this.mob.getX(), this.mob.getEyeY() + 0.5D, this.mob.getZ(),
                    3, 0.2D, 0.2D, 0.2D, 0.0D);
        }
    }

    private void showPoofParticles() {
        if (this.mob.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF,
                    this.mob.getX(), this.mob.getY() + 0.5D, this.mob.getZ(),
                    5, 0.1D, 0.1D, 0.1D, 0.05D);
        }
    }

    private void dropItem() {
        if (!(this.mob.level() instanceof ServerLevel serverLevel)
                || !(this.mob instanceof FearDropTracker fearDropTracker)) {
            return;
        }

        if (fearDropTracker.hearthguard$hasDroppedFearItem()) {
            return;
        }

        float dropChance = HearthguardConfig.getInstance().getDropItemChance() / 100.0F;
        if (dropChance <= 0.0F || this.mob.getRandom().nextFloat() >= dropChance) {
            return;
        }

        LootParams lootParams = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, this.mob.position())
                .withParameter(LootContextParams.THIS_ENTITY, this.mob)
                .withParameter(LootContextParams.DAMAGE_SOURCE, this.mob.damageSources().generic())
                .create(LootContextParamSets.ENTITY);

        List<ItemStack> drops = new ArrayList<>();
        this.mob.getLootTable().ifPresent(lootTableKey -> {
            LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(lootTableKey);
            drops.addAll(lootTable.getRandomItems(lootParams));
        });

        if (drops.isEmpty()) {
            ItemStack fallbackDrop = getFallbackFearDrop();
            if (fallbackDrop.isEmpty()) {
                return;
            }
            drops.add(fallbackDrop);
        }

        ItemStack stack = chooseFearDrop(drops).copyWithCount(1);
        ItemEntity itemEntity = new ItemEntity(serverLevel, this.mob.getX(), this.mob.getY(), this.mob.getZ(), stack);
        itemEntity.setPickUpDelay(100);
        double xVel = (this.mob.getRandom().nextDouble() - 0.5D) * 0.2D;
        double zVel = (this.mob.getRandom().nextDouble() - 0.5D) * 0.2D;
        itemEntity.setDeltaMovement(xVel, 0.4D, zVel);

        serverLevel.addFreshEntity(itemEntity);
        fearDropTracker.hearthguard$setDroppedFearItem(true);
    }

    private int getStartleDistance(BlockState state) {
        int range = HearthguardConfig.getInstance().getRange();
        if (state.is(HEARTHFIRES)) {
            return (int) Math.ceil(range * 1.5D);
        }

        return range;
    }

    private int getMaxStartleDistance() {
        return (int) Math.ceil(HearthguardConfig.getInstance().getRange() * 1.5D);
    }

    private static void playMobHurtSound(Mob mob) {
        mob.setSilent(false);
        try {
            SoundEvent sound = ((MobInvokerMixin) mob).hearthguard$invokeGetHurtSound(mob.damageSources().generic());

            if (sound != null) {
                mob.playSound(sound, 1.0F, 1.0F);
            }

        } catch (Exception e) {
            Constants.LOG.error("Error while playing mob hurt sound: {}", e.getMessage());
        }
        mob.setSilent(true);
    }

    private enum FleeState {
        IDLE,
        STARTLED,
        START_FLEEING,
        FLEEING,
        RECOVERING
    }
}
