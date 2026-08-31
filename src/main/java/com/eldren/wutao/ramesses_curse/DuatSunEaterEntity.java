package com.eldren.wutao.ramesses_curse;

import com.eldren.wutao.ramesses_curse.integration.goety.GoetyObsidianMonolithCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonChargePlayerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.BinaryHeap;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class DuatSunEaterEntity extends EnderDragon {
    private static final EntityDataAccessor<Boolean> OBSIDIAN_SHIELD =
            SynchedEntityData.defineId(DuatSunEaterEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> COMBAT_STAGE =
            SynchedEntityData.defineId(DuatSunEaterEntity.class, EntityDataSerializers.INT);
    private static final String COMBAT_STAGE_KEY = "RamessesSunEaterCombatStage";
    private static final String STAGE_TRANSITION_TICKS_KEY = "RamessesSunEaterStageTransitionTicks";
    private static final int FINAL_COMBAT_STAGE = 3;
    private static final int STAGE_ENTRY_TICKS = 80;
    private static final double[] STAGE_MAX_HEALTH = {0.0D, 330.0D, 550.0D, 1024.0D};
    private static final float[] STAGE_DAMAGE_TAKEN_MULTIPLIER = {0.0F, 0.85F, 0.75F, 0.65F};
    private static final double ARMOR = 20.0D;
    private static final double KNOCKBACK_RESISTANCE = 1.0D;
    private static final float LOG_SHIELD_THRESHOLD = 16.0F;
    private static final double LOG_SHIELD_SCALE = 24.0D;
    private static final int DAMAGE_IMMUNITY_TICKS = 6;
    private static final float HIT_INVISIBILITY_CHANCE = 0.25F;
    private static final int HIT_INVISIBILITY_TICKS = 60;
    private static final double ISLAND_COMBAT_RADIUS = 112.0D;
    private static final int ATTACK_IDLE = 0;
    private static final int ATTACK_ASCEND = 1;
    private static final int ATTACK_DIVE = 2;
    private static final int ATTACK_BURROW = 3;
    private static final int ATTACK_EMERGE = 4;
    private static final int[] NODE_ADJACENCY = {
            6146, 8197, 8202, 16404, 32808, 32848, 65696, 131392,
            131712, 263424, 526848, 525313, 1581057, 3166214, 2138120,
            6373424, 4358208, 12910976, 9044480, 9706496, 15216640,
            13688832, 11763712, 8257536
    };
    private final Node[] islandNodes = new Node[24];
    private final BinaryHeap islandOpenSet = new BinaryHeap();
    private final Set<UUID> burrowTargetsHit = new HashSet<>();
    private final ServerBossEvent bossEvent = new ServerBossEvent(
            this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
    private int attackState = ATTACK_IDLE;
    private int attackTicks;
    private int attackCooldown = 160;
    private double impactX;
    private double impactY;
    private double impactZ;
    @Nullable
    private UUID lockedTargetId;
    private long damageImmuneUntil;
    private int hitInvisibilityTicks;
    private int combatStage = 1;
    private int stageTransitionTicks = -1;
    private boolean stageTransitionsEnabled;

    public DuatSunEaterEntity(EntityType<? extends EnderDragon> type, Level level) {
        super(type, level);
        this.setFightOrigin(new BlockPos(
                RamessesEndIslandStructure.CENTER_X,
                RamessesEndIslandPiece.CENTER_Y,
                RamessesEndIslandStructure.CENTER_Z));
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(STAGE_MAX_HEALTH[this.combatStage]);
        this.getAttribute(Attributes.ARMOR).setBaseValue(ARMOR);
        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(KNOCKBACK_RESISTANCE);
        this.setHealth(this.getMaxHealth());
        this.stageTransitionsEnabled = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return EnderDragon.createAttributes()
                .add(Attributes.MAX_HEALTH, STAGE_MAX_HEALTH[FINAL_COMBAT_STAGE])
                .add(Attributes.ARMOR, ARMOR)
                .add(Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_RESISTANCE);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OBSIDIAN_SHIELD, false);
        builder.define(COMBAT_STAGE, 1);
    }

    public boolean isObsidianShieldActive() {
        return this.entityData.get(OBSIDIAN_SHIELD);
    }

    public int getCombatStage() {
        return this.level().isClientSide ? this.entityData.get(COMBAT_STAGE) : this.combatStage;
    }

    public void onCrystalDestroyed(RamessesEndCrystalEntity crystal, BlockPos position, DamageSource source) {
        if (this.level() instanceof ServerLevel level && this.isAlive()) {
            EndCrystalFirestormManager.trigger(level);
        }
    }

    @Override
    public boolean hurt(EnderDragonPart part, DamageSource source, float amount) {
        if (!this.level().isClientSide && this.isStageTransitionActive()) {
            return false;
        }
        if (!this.level().isClientSide && this.refreshObsidianShield()) {
            this.blockShieldHit();
            return false;
        }

        DragonPhaseInstance phase = this.getPhaseManager().getCurrentPhase();
        if (phase.getPhase() == EnderDragonPhase.DYING) {
            return false;
        }

        float adjustedAmount = phase.onHurt(source, amount);
        if (part != this.head) {
            adjustedAmount = adjustedAmount / 4.0F + Math.min(adjustedAmount, 1.0F);
        }
        if (adjustedAmount < 0.01F) {
            return false;
        }

        // Vanilla only damages dragons for player attacks and specially tagged sources.
        // Route every DamageSource through the same shield, reduction and immunity logic.
        boolean damaged = this.reallyHurt(source, adjustedAmount);
        if (damaged && this.isDeadOrDying() && !phase.isSitting()) {
            this.setHealth(1.0F);
            this.getPhaseManager().setPhase(EnderDragonPhase.DYING);
        }
        return damaged;
    }

    @Override
    protected boolean reallyHurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && this.level().getGameTime() < this.damageImmuneUntil) {
            return false;
        }

        float shieldedAmount = applyLogarithmicShield(amount)
                * STAGE_DAMAGE_TAKEN_MULTIPLIER[this.combatStage];
        int stageBeforeHit = this.combatStage;
        boolean damaged = super.reallyHurt(source, shieldedAmount);
        if (damaged && !this.level().isClientSide) {
            if (this.combatStage != stageBeforeHit) {
                return true;
            }
            this.damageImmuneUntil = this.level().getGameTime() + DAMAGE_IMMUNITY_TICKS;
            this.invulnerableTime = DAMAGE_IMMUNITY_TICKS;
            if (this.isAlive() && this.random.nextFloat() < HIT_INVISIBILITY_CHANCE) {
                this.hitInvisibilityTicks = HIT_INVISIBILITY_TICKS;
                this.setInvisible(true);
            }
        }
        return damaged;
    }

    @Override
    public void setHealth(float health) {
        if (health <= 0.0F
                && this.stageTransitionsEnabled
                && this.combatStage < FINAL_COMBAT_STAGE
                && this.level() instanceof ServerLevel serverLevel
                && this.getPhaseManager().getCurrentPhase().getPhase() != EnderDragonPhase.DYING) {
            this.advanceCombatStage(serverLevel);
            return;
        }
        super.setHealth(health);
    }

    private void advanceCombatStage(ServerLevel level) {
        this.combatStage++;
        this.entityData.set(COMBAT_STAGE, this.combatStage);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(STAGE_MAX_HEALTH[this.combatStage]);
        this.setHealth(this.getMaxHealth());
        this.deathTime = 0;
        this.dragonDeathTime = 0;
        this.clearHitInvisibility();
        this.attackState = ATTACK_IDLE;
        this.attackTicks = 0;
        this.attackCooldown = 80;
        this.lockedTargetId = null;
        this.burrowTargetsHit.clear();
        this.bossEvent.setProgress(1.0F);
        this.startCombatStageTransition(level);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(COMBAT_STAGE_KEY, this.combatStage);
        tag.putInt(STAGE_TRANSITION_TICKS_KEY, this.stageTransitionTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        this.stageTransitionsEnabled = false;
        super.readAdditionalSaveData(tag);
        this.combatStage = Mth.clamp(tag.contains(COMBAT_STAGE_KEY)
                ? tag.getInt(COMBAT_STAGE_KEY) : 1, 1, FINAL_COMBAT_STAGE);
        this.stageTransitionTicks = tag.contains(STAGE_TRANSITION_TICKS_KEY)
                ? tag.getInt(STAGE_TRANSITION_TICKS_KEY) : -1;
        this.entityData.set(COMBAT_STAGE, this.combatStage);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(STAGE_MAX_HEALTH[this.combatStage]);
        this.setHealth(Math.min(this.getHealth(), this.getMaxHealth()));
        if (this.isStageTransitionActive()) {
            this.setInvisible(this.stageTransitionTicks < RamessesEndIslandPiece.CRYSTAL_CAGE_BUILD_TICKS);
        }
        this.stageTransitionsEnabled = true;
    }

    private static float applyLogarithmicShield(float amount) {
        if (amount <= LOG_SHIELD_THRESHOLD) {
            return amount;
        }
        double finiteAmount = Float.isFinite(amount) ? amount : Float.MAX_VALUE;
        double compressed = LOG_SHIELD_THRESHOLD + LOG_SHIELD_SCALE * Math.log1p(
                (finiteAmount - LOG_SHIELD_THRESHOLD) / LOG_SHIELD_SCALE);
        return (float) Math.min(finiteAmount, compressed);
    }

    public boolean startTestDeathSequence() {
        if (this.level().isClientSide
                || !this.isAlive()
                || this.getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.DYING) {
            return false;
        }
        // Match vanilla's airborne lethal-hit state so its full death sequence still runs.
        this.clearHitInvisibility();
        this.damageImmuneUntil = 0L;
        this.setHealth(1.0F);
        this.getPhaseManager().setPhase(EnderDragonPhase.DYING);
        return true;
    }

    public boolean setCombatStageForTesting(int stage) {
        if (!(this.level() instanceof ServerLevel serverLevel)
                || !this.isAlive()
                || this.getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.DYING) {
            return false;
        }
        this.combatStage = Mth.clamp(stage, 1, FINAL_COMBAT_STAGE);
        this.entityData.set(COMBAT_STAGE, this.combatStage);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(STAGE_MAX_HEALTH[this.combatStage]);
        this.setHealth(this.getMaxHealth());
        this.deathTime = 0;
        this.dragonDeathTime = 0;
        this.clearHitInvisibility();
        this.attackState = ATTACK_IDLE;
        this.attackTicks = 0;
        this.attackCooldown = 80;
        this.lockedTargetId = null;
        this.burrowTargetsHit.clear();
        if (this.combatStage == 1) {
            this.stageTransitionTicks = -1;
            this.setInvisible(false);
            this.damageImmuneUntil = serverLevel.getGameTime() + 20L;
            this.invulnerableTime = 20;
            this.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
            RamessesEndIslandPiece.restorePhaseCrystals(
                    serverLevel, this.getFightOrigin(), this.combatStage);
        } else {
            this.startCombatStageTransition(serverLevel);
        }
        this.refreshObsidianShield();
        return true;
    }

    private void startCombatStageTransition(ServerLevel level) {
        this.stageTransitionTicks = 0;
        this.clearHitInvisibility();
        this.setInvisible(true);
        this.damageImmuneUntil = level.getGameTime()
                + RamessesEndIslandPiece.CRYSTAL_CAGE_BUILD_TICKS + STAGE_ENTRY_TICKS + 40L;
        this.invulnerableTime = RamessesEndIslandPiece.CRYSTAL_CAGE_BUILD_TICKS + STAGE_ENTRY_TICKS;
        Vec3 entry = this.stageEntryPosition();
        this.setPos(entry.x, entry.y, entry.z);
        this.setDeltaMovement(Vec3.ZERO);
        this.getPhaseManager().setPhase(EnderDragonPhase.HOVERING);
        RamessesEndIslandPiece.beginAnimatedPhaseCrystalRestore(
                level, this.getFightOrigin(), this.combatStage);
        this.refreshObsidianShield();
    }

    private boolean isStageTransitionActive() {
        return this.stageTransitionTicks >= 0;
    }

    private Vec3 stageEntryPosition() {
        BlockPos center = this.getFightOrigin();
        double angle = this.combatStage == 2 ? Math.toRadians(28.0D) : Math.toRadians(208.0D);
        return new Vec3(
                center.getX() + 0.5D + Math.cos(angle) * 168.0D,
                center.getY() + 58.0D,
                center.getZ() + 0.5D + Math.sin(angle) * 168.0D);
    }

    private void prepareStageTransitionTick() {
        if (this.stageTransitionTicks < RamessesEndIslandPiece.CRYSTAL_CAGE_BUILD_TICKS) {
            Vec3 entry = this.stageEntryPosition();
            this.setInvisible(true);
            this.setPos(entry.x, entry.y, entry.z);
            this.setDeltaMovement(Vec3.ZERO);
            this.getPhaseManager().setPhase(EnderDragonPhase.HOVERING);
            return;
        }
        this.setInvisible(false);
        BlockPos center = this.getFightOrigin();
        this.chargeToward(new Vec3(center.getX() + 0.5D, center.getY() + 42.0D,
                center.getZ() + 0.5D));
    }

    private void tickStageTransition(ServerLevel level) {
        if (this.stageTransitionTicks < RamessesEndIslandPiece.CRYSTAL_CAGE_BUILD_TICKS) {
            RamessesEndIslandPiece.tickAnimatedPhaseCrystalRestore(
                    level, this.getFightOrigin(), this.stageTransitionTicks);
            Vec3 entry = this.stageEntryPosition();
            this.setPos(entry.x, entry.y, entry.z);
            this.setDeltaMovement(Vec3.ZERO);
            this.stageTransitionTicks++;
            if (this.stageTransitionTicks == RamessesEndIslandPiece.CRYSTAL_CAGE_BUILD_TICKS) {
                RamessesEndIslandPiece.finishAnimatedPhaseCrystalRestore(
                        level, this.getFightOrigin(), this.combatStage);
            }
            return;
        }

        this.stageTransitionTicks++;
        if (this.stageTransitionTicks
                < RamessesEndIslandPiece.CRYSTAL_CAGE_BUILD_TICKS + STAGE_ENTRY_TICKS) {
            return;
        }
        this.stageTransitionTicks = -1;
        this.setInvisible(false);
        this.damageImmuneUntil = level.getGameTime() + 20L;
        this.invulnerableTime = 20;
        this.attackCooldown = 80;
        this.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
        level.playSound(null, this.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL,
                SoundSource.HOSTILE, 8.0F, this.combatStage == 2 ? 0.7F : 0.56F);
        level.sendParticles(ParticleTypes.FLAME,
                this.getX(), this.getY() + this.getBbHeight() * 0.5D, this.getZ(),
                70, 4.0D, 2.8D, 4.0D, 0.13D);
        level.sendParticles(ParticleTypes.END_ROD,
                this.getX(), this.getY() + this.getBbHeight() * 0.5D, this.getZ(),
                55, 3.2D, 2.2D, 3.2D, 0.1D);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return !effect.getEffect().is(MobEffects.MOVEMENT_SLOWDOWN) && super.canBeAffected(effect);
    }

    @Override
    public void knockback(double strength, double x, double z) {
        // Attribute resistance handles standard knockback; this also blocks forced calls from other mods.
    }

    // Vanilla's constructor hard-codes EntityType.ENDER_DRAGON, so expose the registered type for saving and spawning.
    @Override
    public EntityType<?> getType() {
        return ModEntities.DUAT_SUN_EATER.get();
    }

    @Override
    public void aiStep() {
        boolean stageTransition = !this.level().isClientSide && this.isStageTransitionActive();
        if (!this.level().isClientSide && this.getPhaseManager().getCurrentPhase().getPhase() != EnderDragonPhase.DYING) {
            if (stageTransition) {
                this.prepareStageTransitionTick();
            } else {
                this.enforcePermanentFlight();
                this.tickBurrowingLightningAttack((ServerLevel) this.level());
            }
        }
        super.aiStep();
        if (!this.level().isClientSide) {
            if (stageTransition) {
                this.tickStageTransition((ServerLevel) this.level());
            } else {
                this.tickHitInvisibility();
            }
            this.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            if (!this.isStageTransitionActive()) {
                this.enforcePermanentFlight();
            }
            if (this.tickCount % 10 == 0) {
                this.refreshObsidianShield();
            }
            this.bossEvent.setProgress(Mth.clamp(this.getHealth() / this.getMaxHealth(), 0.0F, 1.0F));
            this.bossEvent.setName(this.getDisplayName());
        }
    }

    @Override
    protected void tickDeath() {
        if (!this.level().isClientSide) {
            this.clearHitInvisibility();
        }
        ServerLevel serverLevel = this.level() instanceof ServerLevel level ? level : null;
        BlockPos blastOrigin = serverLevel != null ? this.getFightOrigin() : null;
        boolean deathCompletesThisTick = this.dragonDeathTime == 199;
        super.tickDeath();
        if (deathCompletesThisTick && serverLevel != null && blastOrigin != null) {
            double crownY = Mth.clamp(this.getY(), blastOrigin.getY() + 6.0D, blastOrigin.getY() + 18.0D);
            serverLevel.addFreshEntity(new DuatSunEaterCrownEntity(
                    serverLevel, this.getX(), crownY, this.getZ(), blastOrigin));
        }
    }

    private void tickHitInvisibility() {
        if (this.hitInvisibilityTicks > 0) {
            this.hitInvisibilityTicks--;
            if (this.hitInvisibilityTicks == 0) {
                this.setInvisible(false);
            }
        } else if (this.isInvisible()) {
            this.setInvisible(false);
        }
    }

    private void clearHitInvisibility() {
        this.hitInvisibilityTicks = 0;
        this.setInvisible(false);
    }

    private void enforcePermanentFlight() {
        DragonPhaseInstance phase = this.getPhaseManager().getCurrentPhase();
        if (phase.getPhase() == EnderDragonPhase.DYING || this.attackState != ATTACK_IDLE) {
            return;
        }
        if (phase.isSitting()
                || phase.getPhase() == EnderDragonPhase.LANDING
                || phase.getPhase() == EnderDragonPhase.LANDING_APPROACH) {
            this.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
        }
    }

    private void tickBurrowingLightningAttack(ServerLevel level) {
        if (this.attackState == ATTACK_IDLE) {
            if (this.attackCooldown-- <= 0) {
                Entity target = this.selectIslandTarget(level);
                if (target == null) {
                    this.attackCooldown = 40;
                    return;
                }
                this.impactX = target.getX();
                this.impactZ = target.getZ();
                this.impactY = this.findImpactSurface(level, target.blockPosition());
                this.lockedTargetId = target.getUUID();
                this.attackState = ATTACK_ASCEND;
                this.attackTicks = 0;
                this.burrowTargetsHit.clear();
                level.playSound(null, this.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL,
                        SoundSource.HOSTILE, 3.0F, 0.62F);
            }
            return;
        }

        this.attackTicks++;
        this.spawnLockedTargetArrow(level);
        if (this.attackState == ATTACK_ASCEND) {
            this.chargeToward(new Vec3(this.impactX, this.impactY + 68.0D, this.impactZ));
            if (this.attackTicks % 4 == 0) {
                this.spawnAscendCharge(level);
            }
            if (this.attackTicks >= 42 || this.getY() >= this.impactY + 54.0D) {
                this.attackState = ATTACK_DIVE;
                this.attackTicks = 0;
                level.playSound(null, BlockPos.containing(this.impactX, this.impactY, this.impactZ),
                        SoundEvents.TRIDENT_THUNDER.value(), SoundSource.HOSTILE, 2.8F, 0.55F);
            }
            return;
        }
        if (this.attackState == ATTACK_DIVE) {
            this.chargeToward(new Vec3(this.impactX, this.impactY - 22.0D, this.impactZ));
            if (this.attackTicks % 2 == 0) {
                this.spawnDiveTrail(level);
            }
            double horizontalDistance = Mth.square(this.getX() - this.impactX)
                    + Mth.square(this.getZ() - this.impactZ);
            if ((horizontalDistance <= 100.0D && this.getY() <= this.impactY + 5.0D)
                    || this.attackTicks >= 80) {
                this.performBurrowImpact(level);
                this.attackState = ATTACK_BURROW;
                this.attackTicks = 0;
            }
            return;
        }
        if (this.attackState == ATTACK_BURROW) {
            this.chargeToward(new Vec3(this.impactX, this.impactY - 28.0D, this.impactZ));
            if (this.attackTicks == 7) {
                this.performGroundPulse(level, 13.0D, 12, 3, 0.78D);
            } else if (this.attackTicks == 15) {
                this.performGroundPulse(level, 20.0D, 16, 4, 0.62D);
            }
            if (this.attackTicks >= 28) {
                this.attackState = ATTACK_EMERGE;
                this.attackTicks = 0;
            }
            return;
        }
        this.chargeToward(new Vec3(this.impactX + 22.0D, this.impactY + 62.0D, this.impactZ + 22.0D));
        if (this.attackTicks >= 50 || this.getY() >= this.impactY + 46.0D) {
            this.attackState = ATTACK_IDLE;
            this.attackTicks = 0;
            this.attackCooldown = 150 + this.random.nextInt(91);
            this.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
        }
    }

    private void chargeToward(Vec3 target) {
        this.getPhaseManager().setPhase(EnderDragonPhase.CHARGING_PLAYER);
        DragonChargePlayerPhase charge = this.getPhaseManager().getPhase(EnderDragonPhase.CHARGING_PLAYER);
        charge.setTarget(target);
    }

    @Nullable
    private Entity selectIslandTarget(ServerLevel level) {
        BlockPos center = this.getFightOrigin();
        AABB bounds = new AABB(
                center.getX() - ISLAND_COMBAT_RADIUS, level.getMinBuildHeight(), center.getZ() - ISLAND_COMBAT_RADIUS,
                center.getX() + ISLAND_COMBAT_RADIUS, level.getMaxBuildHeight(), center.getZ() + ISLAND_COMBAT_RADIUS);
        List<Entity> targets = level.getEntities(this, bounds, this::canAttackIslandEntity);
        if (targets.isEmpty()) {
            return null;
        }
        return targets.get(this.random.nextInt(targets.size()));
    }

    private boolean canAttackIslandEntity(Entity entity) {
        BlockPos center = this.getFightOrigin();
        double dx = entity.getX() - center.getX();
        double dz = entity.getZ() - center.getZ();
        return dx * dx + dz * dz <= ISLAND_COMBAT_RADIUS * ISLAND_COMBAT_RADIUS
                && entity != this
                && entity.isAlive()
                && (!(entity instanceof Player player) || !player.isSpectator())
                && !(entity instanceof EndCrystal)
                && !(entity instanceof EnderDragonPart)
                && !(entity instanceof LightningBolt)
                && !(entity instanceof FallingBlockEntity)
                && !GoetyObsidianMonolithCompat.isIslandMonolith(entity);
    }

    private double findImpactSurface(ServerLevel level, BlockPos targetPos) {
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                targetPos.getX(), targetPos.getZ());
        return Math.max(RamessesEndIslandPiece.CENTER_Y, surfaceY);
    }

    private void performBurrowImpact(ServerLevel level) {
        BlockPos impact = BlockPos.containing(this.impactX, this.impactY, this.impactZ);
        level.playSound(null, impact, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 3.5F, 0.55F);
        level.playSound(null, impact, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 2.6F, 0.72F);
        level.sendParticles(ParticleTypes.EXPLOSION, this.impactX, this.impactY + 0.5D, this.impactZ,
                5, 2.0D, 0.8D, 2.0D, 0.05D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, this.impactX, this.impactY + 0.3D, this.impactZ,
                90, 5.0D, 0.8D, 5.0D, 0.18D);
        this.performGroundPulse(level, 7.0D, 10, 2, 1.0D);
    }

    private void spawnAscendCharge(ServerLevel level) {
        double orbit = 5.5D + (this.attackTicks % 12) * 0.18D;
        double angle = this.attackTicks * 0.72D;
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                this.getX() + Math.cos(angle) * orbit, this.getY() + 2.0D,
                this.getZ() + Math.sin(angle) * orbit, 5, 0.35D, 0.8D, 0.35D, 0.16D);
        level.sendParticles(ParticleTypes.DRAGON_BREATH,
                this.getX(), this.getY() + 2.0D, this.getZ(), 6, 3.0D, 1.4D, 3.0D, 0.04D);
        this.spawnGroundParticleRing(level, Math.min(3.0D + this.attackTicks * 0.16D, 9.0D),
                ParticleTypes.END_ROD, 14);
    }

    private void spawnDiveTrail(ServerLevel level) {
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                this.getX(), this.getY() + 1.5D, this.getZ(), 12, 3.2D, 2.0D, 3.2D, 0.24D);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                this.getX(), this.getY() + 1.5D, this.getZ(), 7, 2.0D, 1.3D, 2.0D, 0.08D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                this.getX(), this.getY() + 1.5D, this.getZ(), 10, 2.7D, 1.6D, 2.7D, 0.12D);
        if (this.attackTicks % 6 == 0) {
            this.spawnGroundParticleRing(level, 10.0D + (this.attackTicks % 12) * 0.35D,
                    ParticleTypes.ELECTRIC_SPARK, 20);
        }
    }

    private void performGroundPulse(ServerLevel level, double radius, int liftedBlockCount,
                                    int lightningCount, double powerScale) {
        BlockPos impact = BlockPos.containing(this.impactX, this.impactY, this.impactZ);
        level.playSound(null, impact, SoundEvents.LIGHTNING_BOLT_IMPACT,
                SoundSource.HOSTILE, 2.2F, (float) (0.52D + radius * 0.012D));
        this.sendImpactShockwave(level);
        this.spawnGroundParticleRing(level, radius, ParticleTypes.ELECTRIC_SPARK,
                Math.max(24, Mth.floor(radius * 3.0D)));
        this.spawnGroundParticleRing(level, radius * 0.82D, ParticleTypes.REVERSE_PORTAL,
                Math.max(18, Mth.floor(radius * 2.0D)));
        this.heaveIslandBlockLayer(level, radius, liftedBlockCount, powerScale);
        this.spawnLightningCrown(level);
        this.strikeNewTargets(level, radius);
    }

    private void strikeNewTargets(ServerLevel level, double radius) {
        Entity target = this.lockedTargetId == null ? null : level.getEntity(this.lockedTargetId);
        if (target == null || !this.canAttackIslandEntity(target) || !this.burrowTargetsHit.add(target.getUUID())) {
            return;
        }
        double dx = target.getX() - this.impactX;
        double dz = target.getZ() - this.impactZ;
        if (dx * dx + dz * dz > radius * radius) {
            return;
        }
        target.hurt(this.damageSources().mobAttack(this), 18.0F);
        Vec3 push = new Vec3(dx, 0.0D, dz);
        if (push.horizontalDistanceSqr() < 0.01D) {
            push = new Vec3(0.0D, 0.0D, 1.0D);
        }
        push = push.normalize();
        target.push(push.x * 1.15D, 1.0D, push.z * 1.15D);
        target.hurtMarked = true;
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(),
                42, target.getBbWidth() * 0.7D, target.getBbHeight() * 0.6D,
                target.getBbWidth() * 0.7D, 0.22D);
    }

    private void spawnGroundParticleRing(ServerLevel level, double radius,
                                         net.minecraft.core.particles.ParticleOptions particle, int points) {
        for (int index = 0; index < points; index++) {
            double angle = Mth.TWO_PI * index / points + this.random.nextDouble() * 0.08D;
            double x = this.impactX + Math.cos(angle) * radius;
            double z = this.impactZ + Math.sin(angle) * radius;
            double y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    Mth.floor(x), Mth.floor(z)) + 0.15D;
            level.sendParticles(particle, x, y, z, 1, 0.08D, 0.18D, 0.08D, 0.025D);
        }
    }

    private void spawnLightningCrown(ServerLevel level) {
        this.spawnLockedLightning(level);
    }

    private void spawnLockedTargetArrow(ServerLevel level) {
        Entity target = this.lockedTargetId == null ? null : level.getEntity(this.lockedTargetId);
        if (target == null || !target.isAlive()) {
            return;
        }
        double x = target.getX();
        double z = target.getZ();
        double y = target.getY() + target.getBbHeight() + 0.18D;
        float rotation = this.tickCount * 0.16F;
        for (int i = 0; i < 4; i++) {
            level.sendParticles(ParticleTypes.END_ROD, x, y + 0.38D + i * 0.28D, z,
                    1, 0.015D, 0.015D, 0.015D, 0.0D);
        }
        double arm = 0.3D;
        for (int side = -1; side <= 1; side += 2) {
            double ax = x + Math.cos(rotation) * arm * side;
            double az = z + Math.sin(rotation) * arm * side;
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, ax, y + 0.25D, az,
                    3, 0.025D, 0.025D, 0.025D, 0.06D);
        }
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y + 0.05D, z,
                8, 0.06D, 0.04D, 0.06D, 0.1D);
    }

    private void spawnLockedLightning(ServerLevel level) {
        Entity target = this.lockedTargetId == null ? null : level.getEntity(this.lockedTargetId);
        double x = target != null && target.isAlive() ? target.getX() : this.impactX;
        double z = target != null && target.isAlive() ? target.getZ() : this.impactZ;
        double y = target != null && target.isAlive() ? target.getY() : this.impactY;
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(x, y, z);
            level.addFreshEntity(bolt);
            level.playSound(null, BlockPos.containing(x, y, z), SoundEvents.LIGHTNING_BOLT_THUNDER,
                    SoundSource.HOSTILE, 4.0F, 0.62F);
            level.playSound(null, BlockPos.containing(x, y, z), SoundEvents.LIGHTNING_BOLT_IMPACT,
                    SoundSource.HOSTILE, 3.0F, 0.72F);
            level.sendParticles(ParticleTypes.FLASH, x, y + 0.5D, z, 1,
                    0.0D, 0.0D, 0.0D, 0.0D);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y + 0.8D, z,
                    52, 0.65D, 1.2D, 0.65D, 0.24D);
        }
    }

    private void spawnVisualLightning(ServerLevel level, double x, double y, double z) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(x, y, z);
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }
    }

    private void sendImpactShockwave(ServerLevel level) {
        RamessesEndCrystalBreakVfxPacket packet =
                new RamessesEndCrystalBreakVfxPacket(this.impactX, this.impactY + 0.8D, this.impactZ);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(this.impactX, this.impactY, this.impactZ) <= 192.0D * 192.0D) {
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
            }
        }
    }

    private void heaveIslandBlockLayer(ServerLevel level, double ringRadius, int count, double powerScale) {
        for (int index = 0; index < count; index++) {
            double angle = Mth.TWO_PI * index / count + this.random.nextDouble() * 0.22D;
            double radius = Math.max(2.5D, ringRadius - 1.8D + this.random.nextDouble() * 3.6D);
            int x = Mth.floor(this.impactX + Math.cos(angle) * radius);
            int z = Mth.floor(this.impactZ + Math.sin(angle) * radius);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
            BlockPos sourcePos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(sourcePos);
            if (state.isAir() || state.hasBlockEntity()) {
                continue;
            }
            FallingBlockEntity lifted = FallingBlockEntity.fall(level, sourcePos, state);
            level.setBlock(sourcePos, state, 3);
            lifted.dropItem = false;
            lifted.disableDrop();
            lifted.setInvulnerable(true);
            lifted.setPos(x + 0.5D, y + 1.05D, z + 0.5D);
            Vec3 outward = new Vec3(x + 0.5D - this.impactX, 0.0D, z + 0.5D - this.impactZ).normalize();
            lifted.setDeltaMovement(outward.x * (0.08D + 0.08D * powerScale),
                    0.35D + this.random.nextDouble() * 0.28D + 0.28D * powerScale,
                    outward.z * (0.08D + 0.08D * powerScale));
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                    x + 0.5D, y + 1.0D, z + 0.5D, 8, 0.2D, 0.25D, 0.2D, 0.1D);
        }
    }

    private boolean refreshObsidianShield() {
        if (!(this.level() instanceof ServerLevel level)) {
            return this.isObsidianShieldActive();
        }
        AABB islandBounds = new AABB(
                this.getFightOrigin().getX() - ISLAND_COMBAT_RADIUS,
                level.getMinBuildHeight(),
                this.getFightOrigin().getZ() - ISLAND_COMBAT_RADIUS,
                this.getFightOrigin().getX() + ISLAND_COMBAT_RADIUS,
                level.getMaxBuildHeight(),
                this.getFightOrigin().getZ() + ISLAND_COMBAT_RADIUS);
        boolean hasLivingCrystals = !level.getEntitiesOfClass(
                RamessesEndCrystalEntity.class, islandBounds, Entity::isAlive).isEmpty();
        boolean active = hasLivingCrystals || GoetyObsidianMonolithCompat.hasLivingIslandMonoliths(
                level, this.getFightOrigin(), ISLAND_COMBAT_RADIUS + 16.0D);
        if (this.entityData.get(OBSIDIAN_SHIELD) != active) {
            this.entityData.set(OBSIDIAN_SHIELD, active);
        }
        return active;
    }

    private void blockShieldHit() {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        level.playSound(null, this.blockPosition(), SoundEvents.SHIELD_BLOCK,
                SoundSource.HOSTILE, 2.0F, 0.52F);
        level.sendParticles(ParticleTypes.ENCHANTED_HIT,
                this.getX(), this.getY() + this.getBbHeight() * 0.5D, this.getZ(),
                36, this.getBbWidth() * 0.6D, this.getBbHeight() * 0.45D,
                this.getBbWidth() * 0.6D, 0.12D);
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
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
    public int findClosestNode() {
        this.initializeIslandNodes();
        return this.findClosestNodeAbsolute(this.getX(), this.getY(), this.getZ());
    }

    @Override
    public int findClosestNode(double x, double y, double z) {
        this.initializeIslandNodes();
        // Several vanilla phases pass coordinates relative to the central podium.
        if (Math.abs(x) <= 256.0D && Math.abs(z) <= 256.0D) {
            x += this.getFightOrigin().getX();
            z += this.getFightOrigin().getZ();
        }
        return this.findClosestNodeAbsolute(x, y, z);
    }

    private int findClosestNodeAbsolute(double x, double y, double z) {
        float bestDistance = Float.MAX_VALUE;
        int bestIndex = 12;
        Node target = new Node(Mth.floor(x), Mth.floor(y), Mth.floor(z));
        int firstNode = this.getDragonFight() == null || this.getDragonFight().getCrystalsAlive() == 0 ? 12 : 0;
        for (int i = firstNode; i < this.islandNodes.length; i++) {
            float distance = this.islandNodes[i].distanceToSqr(target);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    @Override
    public @Nullable Path findPath(int startIndex, int destinationIndex, @Nullable Node extraDestination) {
        this.initializeIslandNodes();
        for (Node node : this.islandNodes) {
            node.closed = false;
            node.f = 0.0F;
            node.g = 0.0F;
            node.h = 0.0F;
            node.cameFrom = null;
            node.heapIdx = -1;
        }

        Node start = this.islandNodes[startIndex];
        Node destination = this.islandNodes[destinationIndex];
        start.g = 0.0F;
        start.h = start.distanceTo(destination);
        start.f = start.h;
        this.islandOpenSet.clear();
        this.islandOpenSet.insert(start);
        Node closest = start;
        int firstNode = this.getDragonFight() == null || this.getDragonFight().getCrystalsAlive() == 0 ? 12 : 0;

        while (!this.islandOpenSet.isEmpty()) {
            Node current = this.islandOpenSet.pop();
            if (current.equals(destination)) {
                if (extraDestination != null) {
                    extraDestination.cameFrom = destination;
                    destination = extraDestination;
                }
                return reconstructPath(destination);
            }
            if (current.distanceTo(destination) < closest.distanceTo(destination)) {
                closest = current;
            }
            current.closed = true;
            int currentIndex = 0;
            for (int i = 0; i < this.islandNodes.length; i++) {
                if (this.islandNodes[i] == current) {
                    currentIndex = i;
                    break;
                }
            }
            for (int i = firstNode; i < this.islandNodes.length; i++) {
                if ((NODE_ADJACENCY[currentIndex] & 1 << i) == 0) {
                    continue;
                }
                Node next = this.islandNodes[i];
                if (next.closed) {
                    continue;
                }
                float cost = current.g + current.distanceTo(next);
                if (!next.inOpenSet() || cost < next.g) {
                    next.cameFrom = current;
                    next.g = cost;
                    next.h = next.distanceTo(destination);
                    if (next.inOpenSet()) {
                        this.islandOpenSet.changeCost(next, next.g + next.h);
                    } else {
                        next.f = next.g + next.h;
                        this.islandOpenSet.insert(next);
                    }
                }
            }
        }

        if (closest == start) {
            return null;
        }
        if (extraDestination != null) {
            extraDestination.cameFrom = closest;
            closest = extraDestination;
        }
        return reconstructPath(closest);
    }

    private void initializeIslandNodes() {
        if (this.islandNodes[0] != null) {
            return;
        }
        int centerX = this.getFightOrigin().getX();
        int centerZ = this.getFightOrigin().getZ();
        for (int i = 0; i < this.islandNodes.length; i++) {
            int heightOffset = 5;
            int localX;
            int localZ;
            if (i < 12) {
                localX = Mth.floor(60.0F * Mth.cos(2.0F * (-(float) Math.PI + (float) Math.PI / 12.0F * i)));
                localZ = Mth.floor(60.0F * Mth.sin(2.0F * (-(float) Math.PI + (float) Math.PI / 12.0F * i)));
            } else if (i < 20) {
                int innerIndex = i - 12;
                localX = Mth.floor(40.0F * Mth.cos(2.0F * (-(float) Math.PI + (float) Math.PI / 8.0F * innerIndex)));
                localZ = Mth.floor(40.0F * Mth.sin(2.0F * (-(float) Math.PI + (float) Math.PI / 8.0F * innerIndex)));
                heightOffset += 10;
            } else {
                int centerIndex = i - 20;
                localX = Mth.floor(20.0F * Mth.cos(2.0F * (-(float) Math.PI + (float) Math.PI / 4.0F * centerIndex)));
                localZ = Mth.floor(20.0F * Mth.sin(2.0F * (-(float) Math.PI + (float) Math.PI / 4.0F * centerIndex)));
            }
            int nodeX = centerX + localX;
            int nodeZ = centerZ + localZ;
            int nodeY = Math.max(this.level().getSeaLevel() + 10,
                    this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                            new BlockPos(nodeX, 0, nodeZ)).getY() + heightOffset);
            this.islandNodes[i] = new Node(nodeX, nodeY, nodeZ);
        }
    }

    private static Path reconstructPath(Node destination) {
        List<Node> nodes = new ArrayList<>();
        Node node = destination;
        while (node != null) {
            nodes.add(node);
            node = node.cameFrom;
        }
        Collections.reverse(nodes);
        return new Path(nodes, new BlockPos(destination.x, destination.y, destination.z), true);
    }
}
