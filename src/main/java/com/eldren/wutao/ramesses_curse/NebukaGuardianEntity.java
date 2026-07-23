package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

public class NebukaGuardianEntity extends Monster implements GeoEntity {
    private static final int RITUAL_ACTIVITY_RADIUS = 48;
    private static final EntityDataAccessor<Integer> ACTION = SynchedEntityData.defineId(
            NebukaGuardianEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> PHASE_TWO = SynchedEntityData.defineId(
            NebukaGuardianEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation IDLE_ANIMATION = loop("animation.nebuka_guardian.idle");
    private static final RawAnimation WALK_ANIMATION = loop("animation.nebuka_guardian.walk");
    private static final RawAnimation RUN_ANIMATION = loop("animation.nebuka_guardian.run");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossEvent = new ServerBossEvent(
            getBossName(), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);
    private int actionTicks;
    private int attackCooldown;
    private boolean deathExperienceDropped;
    private BlockPos ritualHome = BlockPos.ZERO;
    private float ritualHomeYaw;
    private boolean hasRitualHome;

    public NebukaGuardianEntity(EntityType<? extends NebukaGuardianEntity> type, Level level) {
        super(type, level);
        this.xpReward = 250;
        this.setPersistenceRequired();
        this.bossEvent.setPlayBossMusic(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 650.0D)
                .add(Attributes.ARMOR, 18.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 12.0D)
                .add(Attributes.ATTACK_DAMAGE, 18.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.4D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D)
                .add(Attributes.MOVEMENT_SPEED, 0.29D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new NebukaCombatGoal());
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.75D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 24.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ACTION, BossAction.NONE.id);
        this.entityData.define(PHASE_TWO, false);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData,
                                        @Nullable CompoundTag spawnTag) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData, spawnTag);
        if (!level.isClientSide()) {
            startAction(BossAction.INTRO);
        }
        return result;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }

        this.bossEvent.setProgress(Mth.clamp(getHealth() / getMaxHealth(), 0.0F, 1.0F));
        if (attackCooldown > 0) {
            attackCooldown--;
        }

        enforceRitualHome();

        if (!isPhaseTwo() && isAlive() && getHealth() <= getMaxHealth() * 0.5F && getAction() == BossAction.NONE) {
            this.entityData.set(PHASE_TWO, true);
            this.bossEvent.setColor(BossEvent.BossBarColor.RED);
            startAction(BossAction.PHASE_TRANSITION);
        }

        BossAction action = getAction();
        if (action == BossAction.NONE || action == BossAction.DEATH) {
            return;
        }

        actionTicks++;
        tickAction(action);
        if (actionTicks >= action.durationTicks) {
            finishAction(action);
        }
    }

    private void tickAction(BossAction action) {
        LivingEntity target = getTarget();
        if (target != null && target.isAlive() && action.tracksTarget && actionTicks <= action.trackingTicks) {
            getLookControl().setLookAt(target, 30.0F, 30.0F);
            lookAt(target, 30.0F, 30.0F);
        }

        switch (action) {
            case SWEEP -> hitTargetAt(target, 14, 6.0D, 1.0F);
            case COMBO_THREEFOLD -> {
                hitTargetAt(target, 15, 6.0D, 0.75F);
                hitTargetAt(target, 30, 6.5D, 0.85F);
                hitTargetAt(target, 50, 7.0D, 1.25F);
            }
            case DELAYED_HEAVY -> hitTargetAt(target, 41, 7.5D, 1.8F);
            case DASH_SLASH -> tickDashSlash(target);
            case CAST -> hitMagicTargetAt(target, 49, 18.0D, 16.0F);
            case SANDSTORM_RITE -> {
                hitNearbyPlayersAt(30, 7.0D, 10.0F);
                hitNearbyPlayersAt(52, 8.0D, 12.0F);
                hitNearbyPlayersAt(74, 9.0D, 14.0F);
            }
            case ROYAL_JUDGMENT -> hitMagicTargetAt(target, 57, 24.0D, 28.0F);
            default -> {
            }
        }
    }

    private void tickDashSlash(@Nullable LivingEntity target) {
        if (target != null && actionTicks >= 8 && actionTicks <= 18) {
            Vec3 direction = target.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D);
            if (direction.lengthSqr() > 1.0E-5D) {
                direction = direction.normalize().scale(isPhaseTwo() ? 0.8D : 0.68D);
                setDeltaMovement(direction.x, getDeltaMovement().y, direction.z);
            }
        }
        hitTargetAt(target, 20, 8.0D, 1.35F);
    }

    private void hitTargetAt(@Nullable LivingEntity target, int hitTick, double range, float damageMultiplier) {
        if (actionTicks != hitTick || target == null || !target.isAlive() || distanceToSqr(target) > range * range) {
            return;
        }
        float damage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier;
        if (target.hurt(damageSources().mobAttack(this), damage)) {
            knockbackTarget(target, damageMultiplier);
            playAttackImpactSound();
        }
    }

    private void hitMagicTargetAt(@Nullable LivingEntity target, int hitTick, double range, float damage) {
        if (actionTicks != hitTick || target == null || !target.isAlive() || distanceToSqr(target) > range * range) {
            return;
        }
        if (target.hurt(damageSources().indirectMagic(this, this), damage)) {
            playAttackImpactSound();
        }
    }

    private void hitNearbyPlayersAt(int hitTick, double radius, float damage) {
        if (actionTicks != hitTick) {
            return;
        }
        AABB area = getBoundingBox().inflate(radius, 3.0D, radius);
        for (Player player : level().getEntitiesOfClass(Player.class, area, Player::isAlive)) {
            if (player.hurt(damageSources().indirectMagic(this, this), damage)) {
                Vec3 away = player.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D);
                if (away.lengthSqr() > 1.0E-5D) {
                    away = away.normalize().scale(0.9D);
                    player.push(away.x, 0.35D, away.z);
                }
            }
        }
        playAttackImpactSound();
    }

    private void knockbackTarget(LivingEntity target, float strength) {
        Vec3 away = target.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D);
        if (away.lengthSqr() > 1.0E-5D) {
            away = away.normalize().scale(0.35D + strength * 0.18D);
            target.push(away.x, 0.12D + strength * 0.08D, away.z);
        }
    }

    private void playAttackImpactSound() {
        level().playSound(null, blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 0.8F, 0.65F);
    }

    private void finishAction(BossAction action) {
        if (action == BossAction.PHASE_TRANSITION) {
            heal(getMaxHealth() * 0.08F);
        }
        this.entityData.set(ACTION, BossAction.NONE.id);
        this.actionTicks = 0;
    }

    private void startAction(BossAction action) {
        if (getAction() == BossAction.DEATH || action == BossAction.NONE) {
            return;
        }
        getNavigation().stop();
        this.entityData.set(ACTION, action.id);
        this.actionTicks = 0;
        if (action.isAttack) {
            this.attackCooldown = isPhaseTwo() ? 12 : 20;
        }
    }

    public BossAction getAction() {
        return BossAction.byId(this.entityData.get(ACTION));
    }

    public boolean isPhaseTwo() {
        return this.entityData.get(PHASE_TWO);
    }

    public void setRitualHome(BlockPos homePos, float homeYaw) {
        this.ritualHome = homePos.immutable();
        this.ritualHomeYaw = homeYaw;
        this.hasRitualHome = true;
        restrictTo(this.ritualHome, RITUAL_ACTIVITY_RADIUS);
        setYRot(homeYaw);
        setYBodyRot(homeYaw);
        setYHeadRot(homeYaw);
    }

    private void enforceRitualHome() {
        if (!hasRitualHome || !isAlive()) {
            return;
        }

        LivingEntity target = getTarget();
        if (target != null && target.distanceToSqr(Vec3.atCenterOf(ritualHome))
                > RITUAL_ACTIVITY_RADIUS * RITUAL_ACTIVITY_RADIUS) {
            setTarget(null);
            target = null;
        }

        double distanceHome = distanceToSqr(Vec3.atCenterOf(ritualHome));
        if (distanceHome > RITUAL_ACTIVITY_RADIUS * RITUAL_ACTIVITY_RADIUS) {
            setTarget(null);
            if (getAction() == BossAction.NONE) {
                getNavigation().moveTo(
                        ritualHome.getX() + 0.5D,
                        ritualHome.getY() + 1.0D,
                        ritualHome.getZ() + 0.5D,
                        isPhaseTwo() ? 1.3D : 1.1D);
            }
            return;
        }

        if (target == null && distanceHome <= 6.25D && getNavigation().isDone()) {
            float yaw = Mth.rotLerp(0.12F, getYRot(), ritualHomeYaw);
            setYRot(yaw);
            setYBodyRot(yaw);
            setYHeadRot(yaw);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (getAction() == BossAction.PHASE_TRANSITION || getAction() == BossAction.INTRO) {
            return false;
        }
        boolean hurt = super.hurt(source, amount);
        if (hurt && !level().isClientSide && isAlive() && getAction() == BossAction.NONE) {
            startAction(amount >= 32.0F ? BossAction.STAGGER : BossAction.HURT);
        }
        return hurt;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        this.entityData.set(ACTION, BossAction.DEATH.id);
        this.actionTicks = 0;
        this.getNavigation().stop();
        this.bossEvent.setProgress(0.0F);
    }

    @Override
    protected void tickDeath() {
        deathTime++;
        if (!deathExperienceDropped && deathTime >= 105 && level() instanceof ServerLevel serverLevel) {
            deathExperienceDropped = true;
            if (serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
                ExperienceOrb.award(serverLevel, position(), this.xpReward);
            }
        }
        if (deathTime >= BossAction.DEATH.durationTicks) {
            remove(Entity.RemovalReason.KILLED);
        }
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(getBossName());
    }

    private Component getBossName() {
        return getCustomName() != null
                ? getCustomName()
                : Component.translatable("entity.ramesses_curse.nebuka_guardian");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("PhaseTwo", isPhaseTwo());
        tag.putBoolean("HasRitualHome", hasRitualHome);
        if (hasRitualHome) {
            tag.putLong("RitualHome", ritualHome.asLong());
            tag.putFloat("RitualHomeYaw", ritualHomeYaw);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        boolean phaseTwo = tag.getBoolean("PhaseTwo");
        this.entityData.set(PHASE_TWO, phaseTwo);
        this.entityData.set(ACTION, BossAction.NONE.id);
        this.actionTicks = 0;
        this.bossEvent.setName(getBossName());
        this.bossEvent.setColor(phaseTwo ? BossEvent.BossBarColor.RED : BossEvent.BossBarColor.YELLOW);
        this.hasRitualHome = tag.getBoolean("HasRitualHome");
        if (hasRitualHome) {
            this.ritualHome = BlockPos.of(tag.getLong("RitualHome"));
            this.ritualHomeYaw = tag.getFloat("RitualHomeYaw");
            restrictTo(this.ritualHome, RITUAL_ACTIVITY_RADIUS);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "locomotion", 6, this::locomotionAnimation));
        controllers.add(new AnimationController<>(this, "action", 2, this::actionAnimation));
    }

    private PlayState locomotionAnimation(AnimationState<NebukaGuardianEntity> state) {
        if (getAction() != BossAction.NONE) {
            return PlayState.STOP;
        }
        if (!state.isMoving()) {
            return state.setAndContinue(IDLE_ANIMATION);
        }
        double horizontalSpeed = getDeltaMovement().horizontalDistanceSqr();
        return state.setAndContinue(horizontalSpeed > 0.075D ? RUN_ANIMATION : WALK_ANIMATION);
    }

    private PlayState actionAnimation(AnimationState<NebukaGuardianEntity> state) {
        BossAction action = getAction();
        if (action == BossAction.NONE) {
            return PlayState.STOP;
        }
        return state.setAndContinue(action.animation);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    private static RawAnimation play(String name) {
        return RawAnimation.begin().thenPlay(name);
    }

    private static RawAnimation loop(String name) {
        return RawAnimation.begin().thenLoop(name);
    }

    public enum BossAction {
        NONE(0, 0, 0, false, false, null),
        INTRO(1, 124, 0, false, false, play("animation.nebuka_guardian.intro")),
        SWEEP(2, 35, 14, true, true, play("animation.nebuka_guardian.attack_sweep")),
        COMBO_THREEFOLD(3, 65, 50, true, true, play("animation.nebuka_guardian.combo_threefold")),
        DELAYED_HEAVY(4, 69, 41, true, true, play("animation.nebuka_guardian.delayed_heavy")),
        DASH_SLASH(5, 38, 20, true, true, play("animation.nebuka_guardian.dash_slash")),
        CAST(6, 67, 49, true, true, play("animation.nebuka_guardian.cast")),
        SANDSTORM_RITE(7, 97, 30, true, true, play("animation.nebuka_guardian.sandstorm_rite")),
        ROYAL_JUDGMENT(8, 83, 57, true, true, play("animation.nebuka_guardian.royal_judgment")),
        PHASE_TRANSITION(9, 132, 0, false, false, play("animation.nebuka_guardian.phase_transition")),
        HURT(10, 15, 0, false, false, play("animation.nebuka_guardian.hurt")),
        STAGGER(11, 41, 0, false, false, play("animation.nebuka_guardian.stagger")),
        DEATH(12, 118, 0, false, false, play("animation.nebuka_guardian.death"));

        private final int id;
        private final int durationTicks;
        private final int trackingTicks;
        private final boolean tracksTarget;
        private final boolean isAttack;
        private final RawAnimation animation;

        BossAction(int id, int durationTicks, int trackingTicks, boolean tracksTarget,
                   boolean isAttack, @Nullable RawAnimation animation) {
            this.id = id;
            this.durationTicks = durationTicks;
            this.trackingTicks = trackingTicks;
            this.tracksTarget = tracksTarget;
            this.isAttack = isAttack;
            this.animation = animation;
        }

        private static BossAction byId(int id) {
            for (BossAction action : values()) {
                if (action.id == id) {
                    return action;
                }
            }
            return NONE;
        }
    }

    private final class NebukaCombatGoal extends Goal {
        private NebukaCombatGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = getTarget();
            return target != null && target.isAlive() && isInsideRitualActivityArea(target);
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void stop() {
            getNavigation().stop();
        }

        @Override
        public void tick() {
            LivingEntity target = getTarget();
            if (target == null || !target.isAlive() || !isInsideRitualActivityArea(target)) {
                if (target != null && !isInsideRitualActivityArea(target)) {
                    setTarget(null);
                }
                return;
            }

            BossAction action = getAction();
            if (action != BossAction.NONE) {
                getNavigation().stop();
                return;
            }

            getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distanceSqr = distanceToSqr(target);
            double speed = isPhaseTwo() ? 1.25D : 1.05D;

            if (attackCooldown > 0) {
                if (distanceSqr > 20.0D) {
                    getNavigation().moveTo(target, speed);
                }
                return;
            }

            if (distanceSqr > 90.0D && distanceSqr < 400.0D) {
                float roll = getRandom().nextFloat();
                if (isPhaseTwo() && roll < 0.22F) {
                    startAction(BossAction.ROYAL_JUDGMENT);
                } else if (isPhaseTwo() && roll < 0.42F) {
                    startAction(BossAction.SANDSTORM_RITE);
                } else if (roll < 0.72F) {
                    startAction(BossAction.DASH_SLASH);
                } else {
                    startAction(BossAction.CAST);
                }
                return;
            }

            if (distanceSqr <= 42.25D) {
                float roll = getRandom().nextFloat();
                if (roll < (isPhaseTwo() ? 0.42F : 0.30F)) {
                    startAction(BossAction.COMBO_THREEFOLD);
                } else if (roll < 0.62F) {
                    startAction(BossAction.DELAYED_HEAVY);
                } else {
                    startAction(BossAction.SWEEP);
                }
                return;
            }

            getNavigation().moveTo(target, speed);
        }

        private boolean isInsideRitualActivityArea(LivingEntity entity) {
            return !hasRitualHome
                    || entity.distanceToSqr(Vec3.atCenterOf(ritualHome))
                    <= RITUAL_ACTIVITY_RADIUS * RITUAL_ACTIVITY_RADIUS;
        }
    }
}
