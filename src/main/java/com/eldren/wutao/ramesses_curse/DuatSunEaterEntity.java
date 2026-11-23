package com.eldren.wutao.ramesses_curse;

import com.eldren.wutao.ramesses_curse.api.DynastyCombatHooks;
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
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonChargePlayerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.BinaryHeap;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class DuatSunEaterEntity extends Mob implements Enemy {
    private static final EntityDataAccessor<Boolean> OBSIDIAN_SHIELD =
            SynchedEntityData.defineId(DuatSunEaterEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> COMBAT_STAGE =
            SynchedEntityData.defineId(DuatSunEaterEntity.class, EntityDataSerializers.INT);
    private static final String COMBAT_STAGE_KEY = "RamessesSunEaterCombatStage";
    private static final String STAGE_TRANSITION_TICKS_KEY = "RamessesSunEaterStageTransitionTicks";
    private static final String RESPAWN_JOURNEY_TICKS_KEY = "RamessesSunEaterRespawnJourneyTicks";
    private static final String RESPAWN_PORTAL_ORIGIN_KEY = "RamessesSunEaterRespawnPortalOrigin";
    private static final int FINAL_COMBAT_STAGE = 3;
    private static final int STAGE_ENTRY_TICKS = 80;
    private static final int RESPAWN_RITUAL_CHARGE_TICKS = 160;
    private static final int RESPAWN_DEPARTURE_END_TICK = RESPAWN_RITUAL_CHARGE_TICKS + 100;
    private static final int RESPAWN_CHUNK_TICKET_RADIUS = 8;
    private static final int STAGE_RITUAL_TICKS = 10 * 20;
    private static final int STAGE_CRYSTAL_LINK_TICKS = 70;
    private static final int STAGE_RITUAL_CHUNK_TICKET_RADIUS = 8;
    private static final double STAGE_RITUAL_VIEW_RANGE = 224.0D;
    private static final float VANILLA_FLIGHT_ACCELERATION = 0.095F;
    private static final double KNOCKBACK_RESISTANCE = 1.0D;
    private static final double ISLAND_COMBAT_RADIUS = 112.0D;
    private static final int ATTACK_IDLE = 0;
    private static final int ATTACK_ASCEND = 1;
    private static final int ATTACK_DIVE = 2;
    private static final int ATTACK_BURROW = 3;
    private static final int ATTACK_EMERGE = 4;
    private static final int ATTACK_BLACK_HOLE = 5;
    private static final int ATTACK_ECLIPSE_BREATH = 6;
    private static final int ATTACK_APEP_SHADOW = 7;
    private static final int COMBAT_CHUNK_TICKET_RADIUS = 2;
    private static final int SHADOW_STRIKE_INTERVAL = 20;
    private static final int SHADOW_WARNING_TICKS = 12;
    private static final double DIVE_SPEED = 3.4D;
    private static final double DIVE_RESPONSE = 0.92D;
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
    private int growlTime = 100;
    private double impactX;
    private double impactY;
    private double impactZ;
    @Nullable
    private UUID lockedTargetId;
    private long damageImmuneUntil;
    private boolean applyingApprovedDamage;
    private float approvedDamageHealthFloor;
    private int hitInvisibilityTicks;
    private int combatStage = 1;
    private int stageTransitionTicks = -1;
    private int respawnJourneyTicks = -1;
    private BlockPos respawnPortalOrigin = BlockPos.ZERO;
    private boolean stageTransitionsEnabled;
    @Nullable
    private ChunkPos combatTicketPos;
    @Nullable
    private BlockPos combatTicketKey;

    private final double[][] positions = new double[64][3];
    private int posPointer;
    private boolean latencyHistoryInitialized;
    private double lastLatencyX;
    private double lastLatencyY;
    private double lastLatencyZ;
    public float oFlapTime;
    public float flapTime;
    public int dragonDeathTime;
    @Nullable
    EndCrystal nearestCrystal;
    private BlockPos fightOrigin = BlockPos.ZERO;
    private EnderDragonPhase currentPhase = EnderDragonPhase.HOLDING_PATTERN;
    private final SimplePhaseManager phaseManager = new SimplePhaseManager();
    @Nullable
    private VanillaFlightPilot vanillaFlightPilot;
    private float vanillaFlightYawAcceleration;
    private Vec3 vanillaFlightForward = Vec3.ZERO;
    private boolean applyVanillaFlightDamping;

    public DuatSunEaterEntity(EntityType<? extends DuatSunEaterEntity> type, Level level) {
        super(type, level);
        this.fightOrigin = new BlockPos(
                RamessesEndIslandStructure.CENTER_X,
                RamessesEndIslandPiece.CENTER_Y,
                RamessesEndIslandStructure.CENTER_Z);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(stageMaxHealth(this.combatStage));
        this.getAttribute(Attributes.ARMOR).setBaseValue(Config.APEP_ARMOR.get());
        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(KNOCKBACK_RESISTANCE);
        this.setNoGravity(true);
        this.noPhysics = true;
        this.setHealth(this.getMaxHealth());
        this.stageTransitionsEnabled = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1024.0D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_RESISTANCE)
                .add(Attributes.FLYING_SPEED, 0.6D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.FOLLOW_RANGE, 256.0D);
    }

    @Override
    protected void registerGoals() {
    }

    public void setFightOrigin(BlockPos origin) { this.fightOrigin = origin.immutable(); }
    public BlockPos getFightOrigin() { return this.fightOrigin; }
    public EndCrystal getNearestCrystal() { return this.nearestCrystal; }

    public boolean isRespawnJourneyActive() {
        return this.respawnJourneyTicks >= 0;
    }

    public void startRespawnJourney(BlockPos portalOrigin) {
        if (this.level().isClientSide) {
            return;
        }
        if (this.isStageTransitionActive() && this.level() instanceof ServerLevel serverLevel) {
            this.abortStageRitual(serverLevel);
        }
        this.clearCurrentAttackEffects((ServerLevel) this.level());
        this.respawnPortalOrigin = portalOrigin.immutable();
        this.respawnJourneyTicks = 0;
        this.stageTransitionTicks = -1;
        this.attackState = ATTACK_IDLE;
        this.attackTicks = 0;
        this.attackCooldown = 160;
        this.lockedTargetId = null;
        this.burrowTargetsHit.clear();
        this.clearHitInvisibility();
        this.setPersistenceRequired();
        this.bossEvent.setVisible(false);
        this.setPos(portalOrigin.getX() + 0.5D, portalOrigin.getY() + 18.0D,
                portalOrigin.getZ() + 0.5D);
        this.setDeltaMovement(Vec3.ZERO);
        this.resetLatencyHistory();
        this.getPhaseManager().setPhase(EnderDragonPhase.HOVERING);
    }
    public double[] getLatencyPos(int index, float partialTick) {
        if (this.isDeadOrDying()) {
            partialTick = 0.0F;
        }
        partialTick = 1.0F - partialTick;
        int pointer = Math.floorMod(this.posPointer - Mth.clamp(index, 0, positions.length - 1), positions.length);
        int previous = Math.floorMod(pointer - 1, positions.length);
        double yaw = this.positions[pointer][0];
        return new double[] {
                yaw + Mth.wrapDegrees(this.positions[previous][0] - yaw) * partialTick,
                Mth.lerp(partialTick, this.positions[pointer][1], this.positions[previous][1]),
                Mth.lerp(partialTick, this.positions[pointer][2], this.positions[previous][2])
        };
    }

    public float getHeadPartYOffset(int index, double[] first, double[] second) {
        return index == 6 ? 0.0F : (float) (second[1] - first[1]);
    }

    public int getLatencyPointer() {
        return this.posPointer;
    }

    public void copyLatencyHistory(double[][] target) {
        int samples = Math.min(this.positions.length, target.length);
        for (int index = 0; index < samples; index++) {
            System.arraycopy(this.positions[index], 0, target[index], 0,
                    Math.min(this.positions[index].length, target[index].length));
        }
    }
    public SimplePhaseManager getPhaseManager() { return this.phaseManager; }

    public final class SimplePhaseManager {
        public SimplePhaseInstance getCurrentPhase() { return new SimplePhaseInstance(currentPhase); }
        public void setPhase(EnderDragonPhase phase) { currentPhase = phase; }
        public SimpleChargePhase getPhase(EnderDragonPhase phase) { return new SimpleChargePhase(); }
    }
    public final class SimplePhaseInstance {
        private final EnderDragonPhase phase;
        private SimplePhaseInstance(EnderDragonPhase phase) { this.phase = phase; }
        public EnderDragonPhase getPhase() { return phase; }
        public boolean isSitting() { return phase == EnderDragonPhase.LANDING || phase == EnderDragonPhase.LANDING_APPROACH; }
        public float onHurt(DamageSource source, float amount) { return amount; }
    }
    public final class SimpleChargePhase {
        public void setTarget(Vec3 target) {
            Vec3 delta = target.subtract(position());
            if (delta.lengthSqr() > 1.0E-6D) setDeltaMovement(delta.normalize().scale(0.55D));
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(OBSIDIAN_SHIELD, false);
        this.entityData.define(COMBAT_STAGE, 1);
    }

    public boolean isObsidianShieldActive() {
        return this.entityData.get(OBSIDIAN_SHIELD);
    }

    public int getCombatStage() {
        return this.level().isClientSide ? this.entityData.get(COMBAT_STAGE) : this.combatStage;
    }

    private boolean hurtFromBody(DamageSource source, float amount) {
        if (!this.level().isClientSide && !this.canAcceptWeaponHit(source)) {
            return false;
        }
        if (!this.level().isClientSide && this.isRespawnJourneyActive()) {
            return false;
        }
        if (!this.level().isClientSide && this.isStageTransitionActive()) {
            return false;
        }
        if (!this.level().isClientSide && !DynastyCombatHooks.bypassesAffixes(source) && this.refreshObsidianShield()) {
            this.blockShieldHit();
            return false;
        }

        SimplePhaseInstance phase = this.getPhaseManager().getCurrentPhase();
        if (phase.getPhase() == EnderDragonPhase.DYING) {
            return false;
        }

        float adjustedAmount = phase.onHurt(source, amount);
        if (Float.isNaN(adjustedAmount) || adjustedAmount < 0.01F) {
            return false;
        }

        // Cap actual hit damage instead of rejecting weapons with high base damage.
        boolean damaged = this.reallyHurt(source, adjustedAmount);
        if (damaged && this.isDeadOrDying() && !phase.isSitting()) {
            this.getPhaseManager().setPhase(EnderDragonPhase.DYING);
        }
        return damaged;
    }

    private boolean reallyHurt(DamageSource source, float amount) {
        if (!this.level().isClientSide
                && (this.applyingApprovedDamage || this.level().getGameTime() < this.damageImmuneUntil)) {
            return false;
        }

        float shieldedAmount = Math.min(amount * stageDamageTakenMultiplier(this.combatStage),
                this.getStageDamageLimit());
        int stageBeforeHit = this.combatStage;
        boolean damaged;
        this.applyingApprovedDamage = true;
        this.approvedDamageHealthFloor = Math.max(0.0F, this.getHealth() - shieldedAmount);
        try {
            damaged = super.hurt(source, shieldedAmount);
        } finally {
            this.applyingApprovedDamage = false;
            this.approvedDamageHealthFloor = 0.0F;
        }
        if (damaged && !this.level().isClientSide) {
            if (this.combatStage != stageBeforeHit) {
                return true;
            }
            int immunityTicks = Config.APEP_DAMAGE_IMMUNITY_TICKS.get();
            this.damageImmuneUntil = this.level().getGameTime() + immunityTicks;
            this.invulnerableTime = immunityTicks;
            if (this.isAlive() && this.random.nextFloat() < Config.APEP_HIT_INVISIBILITY_CHANCE.get()) {
                this.hitInvisibilityTicks = Config.APEP_HIT_INVISIBILITY_TICKS.get();
                this.setInvisible(true);
            }
        }
        return damaged;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return this.hurtFromBody(source, amount);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (!this.level().isClientSide
                && !this.applyingApprovedDamage
                && this.level().getGameTime() < this.damageImmuneUntil) {
            return true;
        }
        return super.isInvulnerableTo(source);
    }

    @Override
    public void setHealth(float health) {
        if (!this.level().isClientSide
                && this.stageTransitionsEnabled
                && health < this.getHealth()
                && this.getPhaseManager().getCurrentPhase().getPhase() != EnderDragonPhase.DYING) {
            if (!this.applyingApprovedDamage) {
                return;
            }
            health = Math.max(health, this.approvedDamageHealthFloor);
        }
        if (this.isRespawnJourneyActive() && health < this.getHealth()) {
            return;
        }
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
        this.clearCurrentAttackEffects(level);
        this.combatStage++;
        this.entityData.set(COMBAT_STAGE, this.combatStage);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(stageMaxHealth(this.combatStage));
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
        tag.putInt(RESPAWN_JOURNEY_TICKS_KEY, this.respawnJourneyTicks);
        tag.putLong(RESPAWN_PORTAL_ORIGIN_KEY, this.respawnPortalOrigin.asLong());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        this.stageTransitionsEnabled = false;
        super.readAdditionalSaveData(tag);
        this.combatStage = Mth.clamp(tag.contains(COMBAT_STAGE_KEY)
                ? tag.getInt(COMBAT_STAGE_KEY) : 1, 1, FINAL_COMBAT_STAGE);
        this.stageTransitionTicks = tag.contains(STAGE_TRANSITION_TICKS_KEY)
                ? tag.getInt(STAGE_TRANSITION_TICKS_KEY) : -1;
        this.respawnJourneyTicks = tag.contains(RESPAWN_JOURNEY_TICKS_KEY)
                ? tag.getInt(RESPAWN_JOURNEY_TICKS_KEY) : -1;
        this.respawnPortalOrigin = tag.contains(RESPAWN_PORTAL_ORIGIN_KEY)
                ? BlockPos.of(tag.getLong(RESPAWN_PORTAL_ORIGIN_KEY)) : BlockPos.ZERO;
        this.entityData.set(COMBAT_STAGE, this.combatStage);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(stageMaxHealth(this.combatStage));
        this.setHealth(Math.min(this.getHealth(), this.getMaxHealth()));
        if (this.isStageTransitionActive()) {
            this.setInvisible(true);
        } else {
            this.setInvisible(false);
        }
        if (this.isRespawnJourneyActive()) {
            this.stageTransitionTicks = -1;
            this.setInvisible(false);
            this.setPersistenceRequired();
            this.bossEvent.setVisible(false);
        }
        this.stageTransitionsEnabled = true;
    }

    private boolean canAcceptWeaponHit(DamageSource source) {
        if (this.applyingApprovedDamage || this.level().getGameTime() < this.damageImmuneUntil) {
            return false;
        }
        if (!(source.getEntity() instanceof Player player) || source.getDirectEntity() != player) {
            return false;
        }
        return canHurtFrom(player);
    }

    public boolean canHurtFrom(Player player) {
        float whiteboardDamage = getRawWhiteboardWeaponDamage(player);
        return whiteboardDamage > 1.0F;
    }

    private static float getRawWhiteboardWeaponDamage(Player player) {
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            return 0.0F;
        }

        ItemStack pristine = new ItemStack(held.getItem());
        double base = 1.0D;
        double addition = 0.0D;
        double multiplyBase = 0.0D;
        double multiplyTotal = 1.0D;
        for (AttributeModifier modifier
                : pristine.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE)) {
            switch (modifier.getOperation()) {
                case ADDITION -> addition += modifier.getAmount();
                case MULTIPLY_BASE -> multiplyBase += modifier.getAmount();
                case MULTIPLY_TOTAL -> multiplyTotal *= 1.0D + modifier.getAmount();
            }
        }
        double whiteboardDamage = (base + addition + base * multiplyBase) * multiplyTotal;
        if (!Double.isFinite(whiteboardDamage)) {
            return Float.POSITIVE_INFINITY;
        }
        return Math.max(0.0F, (float) whiteboardDamage);
    }

    private float getStageDamageLimit() {
        return switch (Mth.clamp(this.getCombatStage(), 1, FINAL_COMBAT_STAGE)) {
            case 1 -> Config.APEP_STAGE_ONE_DAMAGE_CAP.get().floatValue();
            case 2 -> Config.APEP_STAGE_TWO_DAMAGE_CAP.get().floatValue();
            default -> Config.APEP_STAGE_THREE_DAMAGE_CAP.get().floatValue();
        };
    }

    public boolean startTestDeathSequence() {
        if (this.level().isClientSide
                || !this.isAlive()
                || this.getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.DYING) {
            return false;
        }
        ServerLevel serverLevel = (ServerLevel) this.level();
        this.clearCurrentAttackEffects(serverLevel);
        if (this.isStageTransitionActive()) {
            this.abortStageRitual(serverLevel);
        }
        if (this.isRespawnJourneyActive()) {
            serverLevel.getChunkSource().removeRegionTicket(TicketType.PORTAL,
                    new ChunkPos(this.getFightOrigin()), RESPAWN_CHUNK_TICKET_RADIUS,
                    this.getFightOrigin());
            if (this.respawnJourneyTicks < RESPAWN_RITUAL_CHARGE_TICKS) {
                DuatSunEaterRespawnRitual.finishCharging(serverLevel, this.respawnPortalOrigin);
            }
            RamessesEndIslandPiece.clearRespawnInProgress(serverLevel, this.getFightOrigin());
        }
        this.respawnJourneyTicks = -1;
        this.stageTransitionTicks = -1;
        this.stageTransitionsEnabled = false;
        this.clearHitInvisibility();
        this.damageImmuneUntil = 0L;
        this.invulnerableTime = 0;
        this.attackState = ATTACK_IDLE;
        this.setDeltaMovement(Vec3.ZERO);
        this.getPhaseManager().setPhase(EnderDragonPhase.DYING);
        super.setHealth(0.0F);
        this.die(serverLevel.damageSources().fellOutOfWorld());
        return true;
    }

    public boolean setCombatStageForTesting(int stage) {
        if (!(this.level() instanceof ServerLevel serverLevel)
                || !this.isAlive()
                || this.getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.DYING) {
            return false;
        }
        if (this.isStageTransitionActive()) {
            this.abortStageRitual(serverLevel);
        }
        this.clearCurrentAttackEffects(serverLevel);
        this.combatStage = Mth.clamp(stage, 1, FINAL_COMBAT_STAGE);
        this.entityData.set(COMBAT_STAGE, this.combatStage);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(stageMaxHealth(this.combatStage));
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

    public boolean startBlackHoleAttackForTesting() {
        if (!(this.level() instanceof ServerLevel serverLevel)
                || !this.isAlive()
                || this.isRespawnJourneyActive()
                || this.isStageTransitionActive()
                || this.getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.DYING) {
            return false;
        }
        Entity target = this.selectIslandTarget(serverLevel);
        if (target == null) {
            return false;
        }
        this.lockedTargetId = target.getUUID();
        this.beginBlackHoleAttack(serverLevel, target);
        return true;
    }

    public boolean startEclipseBreathForTesting() {
        if (!(this.level() instanceof ServerLevel serverLevel) || !this.canStartTestAttack()) {
            return false;
        }
        Entity target = this.selectIslandTarget(serverLevel);
        if (target == null) {
            return false;
        }
        this.beginEclipseBreath(serverLevel, target);
        return true;
    }

    public boolean startApepShadowForTesting() {
        if (!(this.level() instanceof ServerLevel serverLevel) || !this.canStartTestAttack()) {
            return false;
        }
        Entity target = this.selectIslandTarget(serverLevel);
        if (target == null) {
            return false;
        }
        this.beginApepShadow(serverLevel, target);
        return true;
    }

    private boolean canStartTestAttack() {
        return this.isAlive()
                && !this.isRespawnJourneyActive()
                && !this.isStageTransitionActive()
                && this.getPhaseManager().getCurrentPhase().getPhase() != EnderDragonPhase.DYING;
    }

    private void startCombatStageTransition(ServerLevel level) {
        this.stageTransitionTicks = 0;
        this.clearHitInvisibility();
        this.setInvisible(true);
        this.damageImmuneUntil = level.getGameTime()
                + RamessesEndIslandPiece.CRYSTAL_CAGE_BUILD_TICKS + STAGE_RITUAL_TICKS + 40L;
        this.invulnerableTime = RamessesEndIslandPiece.CRYSTAL_CAGE_BUILD_TICKS + STAGE_RITUAL_TICKS;
        Vec3 holding = this.stageTransitionHoldingPosition();
        this.setPos(holding.x, holding.y, holding.z);
        this.setDeltaMovement(Vec3.ZERO);
        this.resetLatencyHistory();
        this.getPhaseManager().setPhase(EnderDragonPhase.HOVERING);
        this.retainStageRitualChunks(level);
        RamessesEndIslandPiece.beginAnimatedPhaseCrystalRestore(
                level, this.getFightOrigin(), this.combatStage);
        this.refreshObsidianShield();
    }

    private boolean isStageTransitionActive() {
        return this.stageTransitionTicks >= 0;
    }

    private Vec3 stageTransitionHoldingPosition() {
        BlockPos center = this.getFightOrigin();
        return new Vec3(center.getX() + 0.5D, center.getY() + 72.0D, center.getZ() + 0.5D);
    }

    private void retainStageRitualChunks(ServerLevel level) {
        BlockPos ticketKey = this.getFightOrigin();
        level.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(ticketKey),
                STAGE_RITUAL_CHUNK_TICKET_RADIUS, ticketKey);
    }

    private void prepareStageTransitionTick(ServerLevel level) {
        this.retainStageRitualChunks(level);
        if (this.stageTransitionTicks
                < RamessesEndIslandPiece.CRYSTAL_CAGE_BUILD_TICKS + STAGE_RITUAL_TICKS) {
            Vec3 holding = this.stageTransitionHoldingPosition();
            this.setInvisible(true);
            if (this.position().distanceToSqr(holding) > 1.0D) {
                this.setPos(holding.x, holding.y, holding.z);
                this.resetLatencyHistory();
            }
            this.setDeltaMovement(Vec3.ZERO);
            this.getPhaseManager().setPhase(EnderDragonPhase.HOVERING);
            return;
        }
    }

    private void tickStageTransition(ServerLevel level) {
        if (this.stageTransitionTicks < RamessesEndIslandPiece.CRYSTAL_CAGE_BUILD_TICKS) {
            RamessesEndIslandPiece.tickAnimatedPhaseCrystalRestore(
                    level, this.getFightOrigin(), this.stageTransitionTicks);
            this.setDeltaMovement(Vec3.ZERO);
            this.stageTransitionTicks++;
            if (this.stageTransitionTicks == RamessesEndIslandPiece.CRYSTAL_CAGE_BUILD_TICKS) {
                RamessesEndIslandPiece.finishAnimatedPhaseCrystalRestore(
                        level, this.getFightOrigin(), this.combatStage);
                RamessesEndIslandPiece.beginStageCrystalRitual(level, this.getFightOrigin());
                this.sendStageRitualVfx(level, 0, false);
                BlockPos focus = RamessesEndIslandPiece.stageRitualFocus(this.getFightOrigin());
                level.playSound(null, focus, SoundEvents.BEACON_ACTIVATE,
                        SoundSource.HOSTILE, 5.5F, this.combatStage == 2 ? 0.55F : 0.42F);
            }
            return;
        }

        int ritualTick = this.stageTransitionTicks - RamessesEndIslandPiece.CRYSTAL_CAGE_BUILD_TICKS;
        this.tickStageCrystalRitual(level, ritualTick);
        this.stageTransitionTicks++;
        if (ritualTick + 1 < STAGE_RITUAL_TICKS) {
            return;
        }
        this.stageTransitionTicks = -1;
        this.releaseStageRitualChunks(level);
        RamessesEndIslandPiece.finishStageCrystalRitual(level, this.getFightOrigin());
        BlockPos focus = RamessesEndIslandPiece.stageRitualFocus(this.getFightOrigin());
        this.setPos(focus.getX() + 0.5D, focus.getY(), focus.getZ() + 0.5D);
        this.setDeltaMovement(Vec3.ZERO);
        this.resetLatencyHistory();
        this.setInvisible(false);
        this.damageImmuneUntil = level.getGameTime() + 20L;
        this.invulnerableTime = 20;
        this.attackCooldown = 60;
        this.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
        this.sendStageRitualVfx(level, STAGE_RITUAL_TICKS, true);
        level.playSound(null, focus, SoundEvents.GENERIC_EXPLODE,
                SoundSource.HOSTILE, 8.0F, this.combatStage == 2 ? 0.54F : 0.43F);
        level.playSound(null, focus, SoundEvents.LIGHTNING_BOLT_THUNDER,
                SoundSource.HOSTILE, 7.5F, 0.48F);
        level.playSound(null, this.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL,
                SoundSource.HOSTILE, 9.0F, this.combatStage == 2 ? 0.66F : 0.52F);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                this.getX(), this.getY() + 2.0D, this.getZ(),
                2, 1.5D, 1.5D, 1.5D, 0.0D);
        level.sendParticles(ParticleTypes.END_ROD,
                this.getX(), this.getY() + this.getBbHeight() * 0.5D, this.getZ(),
                90, 5.0D, 3.0D, 5.0D, 0.22D);
    }

    private void tickStageCrystalRitual(ServerLevel level, int ritualTick) {
        BlockPos focus = RamessesEndIslandPiece.stageRitualFocus(this.getFightOrigin());
        if (ritualTick == STAGE_CRYSTAL_LINK_TICKS) {
            RamessesEndIslandPiece.focusStageCrystalsOnCenter(level, this.getFightOrigin());
            level.playSound(null, focus, SoundEvents.END_PORTAL_SPAWN,
                    SoundSource.HOSTILE, 6.0F, this.combatStage == 2 ? 0.62F : 0.48F);
        }
        double progress = ritualTick / (double) STAGE_RITUAL_TICKS;
        if ((ritualTick & 3) == 0) {
            level.sendParticles(this.combatStage >= 3 ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.END_ROD,
                    focus.getX() + 0.5D, focus.getY() + 0.5D, focus.getZ() + 0.5D,
                    7 + Mth.floor(progress * 7.0D),
                    4.5D - progress * 2.4D, 3.0D - progress * 1.4D,
                    4.5D - progress * 2.4D, 0.04D + progress * 0.08D);
        }
        if (ritualTick % 20 == 0) {
            level.playSound(null, focus, SoundEvents.RESPAWN_ANCHOR_CHARGE,
                    SoundSource.HOSTILE, 4.5F, (float) (0.38D + progress * 0.72D));
        }
    }

    private void releaseStageRitualChunks(ServerLevel level) {
        BlockPos ticketKey = this.getFightOrigin();
        level.getChunkSource().removeRegionTicket(TicketType.PORTAL, new ChunkPos(ticketKey),
                STAGE_RITUAL_CHUNK_TICKET_RADIUS, ticketKey);
    }

    private void abortStageRitual(ServerLevel level) {
        RamessesEndIslandPiece.finishStageCrystalRitual(level, this.getFightOrigin());
        this.releaseStageRitualChunks(level);
        this.sendStageRitualVfx(level, 0, false);
    }

    private void sendStageRitualVfx(ServerLevel level, int elapsedTicks, boolean detonate) {
        BlockPos focus = RamessesEndIslandPiece.stageRitualFocus(this.getFightOrigin());
        SunEaterStageRitualVfxPacket packet = new SunEaterStageRitualVfxPacket(
                focus.getX() + 0.5D, focus.getY() + 0.5D, focus.getZ() + 0.5D,
                this.combatStage, elapsedTicks, detonate ? -1 : STAGE_RITUAL_TICKS);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(focus.getX() + 0.5D, focus.getY() + 0.5D, focus.getZ() + 0.5D)
                    <= STAGE_RITUAL_VIEW_RANGE * STAGE_RITUAL_VIEW_RANGE) {
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
                if (detonate) {
                    ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                            new NebukaRitualShakePacket(36, this.combatStage >= 3 ? 2.0F : 1.55F));
                }
            }
        }
    }

    private Vec3 respawnTravelDirection() {
        Vec3 direction = new Vec3(
                this.getFightOrigin().getX() - this.respawnPortalOrigin.getX(),
                0.0D,
                this.getFightOrigin().getZ() - this.respawnPortalOrigin.getZ());
        return direction.horizontalDistanceSqr() < 1.0E-6D
                ? new Vec3(1.0D, 0.0D, 0.0D)
                : direction.normalize();
    }

    private Vec3 respawnRemoteEntryPosition() {
        Vec3 direction = this.respawnTravelDirection();
        BlockPos center = this.getFightOrigin();
        return new Vec3(
                center.getX() + 0.5D - direction.x * 118.0D,
                center.getY() + 62.0D,
                center.getZ() + 0.5D - direction.z * 118.0D);
    }

    private void prepareRespawnJourneyTick(ServerLevel level) {
        if (this.respawnJourneyTicks < RESPAWN_RITUAL_CHARGE_TICKS) {
            this.setDeltaMovement(Vec3.ZERO);
            this.getPhaseManager().setPhase(EnderDragonPhase.HOVERING);
            DuatSunEaterRespawnRitual.tickCharging(level, this.respawnPortalOrigin,
                    this.respawnJourneyTicks, RESPAWN_RITUAL_CHARGE_TICKS);
            return;
        }
        if (this.respawnJourneyTicks == RESPAWN_RITUAL_CHARGE_TICKS) {
            DuatSunEaterRespawnRitual.finishCharging(level, this.respawnPortalOrigin);
        }
        if (this.respawnJourneyTicks < RESPAWN_DEPARTURE_END_TICK) {
            Vec3 direction = this.respawnTravelDirection();
            Vec3 departureTarget = new Vec3(
                    this.respawnPortalOrigin.getX() + 0.5D + direction.x * 92.0D,
                    this.respawnPortalOrigin.getY() + 52.0D,
                    this.respawnPortalOrigin.getZ() + 0.5D + direction.z * 92.0D);
            this.faceTravelDirection(departureTarget.subtract(this.position()));
            this.chargeToward(departureTarget);
            return;
        }

        ChunkPos centerChunk = new ChunkPos(this.getFightOrigin());
        level.getChunkSource().addRegionTicket(TicketType.PORTAL, centerChunk,
                RESPAWN_CHUNK_TICKET_RADIUS, this.getFightOrigin());
        if (this.respawnJourneyTicks == RESPAWN_DEPARTURE_END_TICK) {
            double oldX = this.getX();
            double oldY = this.getY();
            double oldZ = this.getZ();
            Vec3 entry = this.respawnRemoteEntryPosition();
            level.getChunk(Mth.floor(entry.x) >> 4, Mth.floor(entry.z) >> 4);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, oldX, oldY + 2.0D, oldZ,
                    90, 3.0D, 2.0D, 3.0D, 0.28D);
            level.playSound(null, BlockPos.containing(oldX, oldY, oldZ),
                    SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, 5.0F, 0.7F);
            this.setPos(entry.x, entry.y, entry.z);
            this.setDeltaMovement(Vec3.ZERO);
            this.resetLatencyHistory();
            level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    entry.x, entry.y + 2.0D, entry.z,
                    120, 4.0D, 2.5D, 4.0D, 0.3D);
            level.playSound(null, this.blockPosition(), SoundEvents.END_PORTAL_SPAWN,
                    SoundSource.HOSTILE, 6.0F, 0.5F);
            RamessesEndIslandPiece.prepareRuntimeRespawn(level, this.getFightOrigin());
        }

        this.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
        this.enforcePermanentFlight();
    }

    private void tickRespawnJourney(ServerLevel level) {
        int terrainTicks = RamessesEndIslandPiece.runtimeRebuildChunkCount(this.getFightOrigin());
        int terrainEnd = RESPAWN_DEPARTURE_END_TICK + terrainTicks;
        int cageEnd = terrainEnd + RamessesEndIslandPiece.CRYSTAL_CAGE_BUILD_TICKS;
        int tick = this.respawnJourneyTicks;

        if (tick >= RESPAWN_DEPARTURE_END_TICK && tick < terrainEnd) {
            RamessesEndIslandPiece.rebuildRuntimeChunk(
                    level, this.getFightOrigin(), tick - RESPAWN_DEPARTURE_END_TICK);
        }
        if (tick == terrainEnd) {
            RamessesEndIslandPiece.beginAnimatedPhaseCrystalRestore(
                    level, this.getFightOrigin(), 1);
        }
        if (tick >= terrainEnd && tick < cageEnd) {
            RamessesEndIslandPiece.tickAnimatedPhaseCrystalRestore(
                    level, this.getFightOrigin(), tick - terrainEnd);
        }
        if (tick == cageEnd) {
            RamessesEndIslandPiece.finishAnimatedPhaseCrystalRestore(
                    level, this.getFightOrigin(), 1);
            RamessesEndIslandPiece.restorePhaseCrystals(level, this.getFightOrigin(), 1);
            RamessesEndIslandPiece.markDragonActive(level, this.getFightOrigin());
        }
        if (tick >= cageEnd + STAGE_ENTRY_TICKS) {
            this.finishRespawnJourney(level);
            return;
        }
        this.respawnJourneyTicks++;
    }

    private void finishRespawnJourney(ServerLevel level) {
        level.getChunkSource().removeRegionTicket(TicketType.PORTAL,
                new ChunkPos(this.getFightOrigin()), RESPAWN_CHUNK_TICKET_RADIUS,
                this.getFightOrigin());
        this.respawnJourneyTicks = -1;
        this.setInvisible(false);
        this.damageImmuneUntil = level.getGameTime() + 40L;
        this.invulnerableTime = 20;
        this.attackState = ATTACK_IDLE;
        this.attackTicks = 0;
        this.attackCooldown = 120;
        this.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
        this.bossEvent.setVisible(true);
        RamessesEndIslandPiece.finishStageCrystalRitual(level, this.getFightOrigin());
        RamessesEndIslandPiece.clearRespawnInProgress(level, this.getFightOrigin());
        RamessesEndIslandPiece.markDragonActive(level, this.getFightOrigin());
        this.refreshObsidianShield();
        level.playSound(null, this.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL,
                SoundSource.HOSTILE, 9.0F, 0.62F);
        level.sendParticles(ParticleTypes.FLAME,
                this.getX(), this.getY() + this.getBbHeight() * 0.5D, this.getZ(),
                100, 5.0D, 3.0D, 5.0D, 0.16D);
        level.sendParticles(ParticleTypes.END_ROD,
                this.getX(), this.getY() + this.getBbHeight() * 0.5D, this.getZ(),
                75, 4.0D, 2.5D, 4.0D, 0.12D);
    }

    private void faceTravelDirection(Vec3 movement) {
        if (movement.horizontalDistanceSqr() < 1.0E-6D) {
            return;
        }
        float yaw = (float) Math.toDegrees(Math.atan2(movement.z, movement.x)) + 90.0F;
        float smoothedYaw = Mth.rotLerp(0.24F, this.getYRot(), yaw);
        this.setYRot(smoothedYaw);
        this.yBodyRot = smoothedYaw;
        this.yHeadRot = smoothedYaw;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return effect.getEffect() != MobEffects.MOVEMENT_SLOWDOWN && super.canBeAffected(effect);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void knockback(double strength, double x, double z) {
        // Attribute resistance handles standard knockback; this also blocks forced calls from other mods.
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENDER_DRAGON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENDER_DRAGON_HURT;
    }

    @Override
    protected float getSoundVolume() {
        return 5.0F;
    }

    @Override
    public boolean isFlapping() {
        if (this.isDeadOrDying()) {
            return false;
        }
        float current = Mth.cos(this.flapTime * ((float) Math.PI * 2.0F));
        float previous = Mth.cos(this.oFlapTime * ((float) Math.PI * 2.0F));
        return previous <= -0.3F && current >= -0.3F;
    }

    @Override
    public void onFlap() {
        if (this.level().isClientSide && !this.isSilent() && !this.isDeadOrDying()) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENDER_DRAGON_FLAP, this.getSoundSource(),
                    5.0F, 0.8F + this.random.nextFloat() * 0.3F, false);
        }
    }

    @Override
    public void aiStep() {
        this.processFlappingMovement();
        if (this.level().isClientSide
                && !this.isDeadOrDying()
                && !this.isSilent()
                && !this.getPhaseManager().getCurrentPhase().isSitting()
                && --this.growlTime < 0) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENDER_DRAGON_GROWL, this.getSoundSource(),
                    2.5F, 0.8F + this.random.nextFloat() * 0.3F, false);
            this.growlTime = 200 + this.random.nextInt(200);
        }
        boolean respawnJourney = !this.level().isClientSide && this.isRespawnJourneyActive();
        boolean stageTransition = !respawnJourney
                && !this.level().isClientSide && this.isStageTransitionActive();
        if (!this.level().isClientSide && this.getPhaseManager().getCurrentPhase().getPhase() != EnderDragonPhase.DYING) {
            this.retainCombatChunk((ServerLevel) this.level());
            if (respawnJourney) {
                this.prepareRespawnJourneyTick((ServerLevel) this.level());
            } else if (stageTransition) {
                this.prepareStageTransitionTick((ServerLevel) this.level());
            } else {
                this.enforcePermanentFlight();
                this.tickBurrowingLightningAttack((ServerLevel) this.level());
            }
        }
        super.aiStep();
        this.oFlapTime = this.flapTime;
        if (!this.isDeadOrDying() && this.dragonDeathTime <= 0) {
            this.advanceFlapAnimation();
        } else {
            float xOffset = (this.random.nextFloat() - 0.5F) * 8.0F;
            float yOffset = (this.random.nextFloat() - 0.5F) * 4.0F;
            float zOffset = (this.random.nextFloat() - 0.5F) * 8.0F;
            this.level().addParticle(ParticleTypes.EXPLOSION,
                    this.getX() + xOffset, this.getY() + 2.0D + yOffset, this.getZ() + zOffset,
                    0.0D, 0.0D, 0.0D);
        }
        this.updateLatencyHistory();
        if (!this.level().isClientSide) {
            if (respawnJourney) {
                this.tickRespawnJourney((ServerLevel) this.level());
            } else if (stageTransition) {
                this.tickStageTransition((ServerLevel) this.level());
            } else {
                this.tickHitInvisibility();
            }
            this.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            if (!this.isRespawnJourneyActive() && this.tickCount % 10 == 0) {
                this.refreshObsidianShield();
            }
            this.bossEvent.setProgress(Mth.clamp(this.getHealth() / this.getMaxHealth(), 0.0F, 1.0F));
            this.bossEvent.setName(this.getDisplayName());
        }
    }

    private void advanceFlapAnimation() {
        Vec3 movement = this.getDeltaMovement();
        double horizontalSpeed = movement.horizontalDistance();
        float step;
        if (movement.lengthSqr() < 1.0E-4D) {
            step = 0.035F;
        } else {
            step = 0.2F / ((float) horizontalSpeed * 10.0F + 1.0F);
            step *= (float) Math.pow(2.0D, Mth.clamp(movement.y, -0.5D, 0.5D));
            step = Mth.clamp(step, 0.025F, 0.065F);
        }
        this.flapTime += step;
    }

    private void updateLatencyHistory() {
        double dx = this.getX() - this.lastLatencyX;
        double dy = this.getY() - this.lastLatencyY;
        double dz = this.getZ() - this.lastLatencyZ;
        if (!this.latencyHistoryInitialized || dx * dx + dy * dy + dz * dz > 256.0D) {
            this.resetLatencyHistory();
            return;
        }
        this.posPointer = (this.posPointer + 1) % this.positions.length;
        this.positions[this.posPointer][0] = this.getYRot();
        this.positions[this.posPointer][1] = this.getY();
        this.positions[this.posPointer][2] = 0.0D;
        this.lastLatencyX = this.getX();
        this.lastLatencyY = this.getY();
        this.lastLatencyZ = this.getZ();
    }

    private void resetLatencyHistory() {
        this.posPointer = 0;
        for (double[] sample : this.positions) {
            sample[0] = this.getYRot();
            sample[1] = this.getY();
            sample[2] = 0.0D;
        }
        this.lastLatencyX = this.getX();
        this.lastLatencyY = this.getY();
        this.lastLatencyZ = this.getZ();
        this.latencyHistoryInitialized = true;
    }

    @Override
    protected void tickDeath() {
        if (!this.level().isClientSide) {
            this.clearHitInvisibility();
        }
        ServerLevel serverLevel = this.level() instanceof ServerLevel level ? level : null;
        BlockPos blastOrigin = serverLevel != null ? this.getFightOrigin() : null;
        // Keep the long dragon-style death animation; Mob.tickDeath would remove us after 20 ticks.
        this.dragonDeathTime++;
        this.deathTime = this.dragonDeathTime;
        if (this.dragonDeathTime == 1) {
            if (serverLevel != null) {
                ApepBattleMusicManager.bossDied(this);
                this.releaseCombatChunk(serverLevel);
                this.clearCurrentAttackEffects(serverLevel);
            }
            DuatCuriosSealManager.bossDefeated(this);
            this.setDeltaMovement(Vec3.ZERO);
            if (serverLevel != null && !this.isSilent()) {
                serverLevel.globalLevelEvent(1028, this.blockPosition(), 0);
            }
        }
        if (this.dragonDeathTime >= 180 && this.dragonDeathTime <= 200) {
            float xOffset = (this.random.nextFloat() - 0.5F) * 8.0F;
            float yOffset = (this.random.nextFloat() - 0.5F) * 4.0F;
            float zOffset = (this.random.nextFloat() - 0.5F) * 8.0F;
            this.level().addParticle(ParticleTypes.EXPLOSION_EMITTER,
                    this.getX() + xOffset, this.getY() + 2.0D + yOffset, this.getZ() + zOffset,
                    0.0D, 0.0D, 0.0D);
        }
        this.move(MoverType.SELF, new Vec3(0.0D, 0.1D, 0.0D));
        boolean deathCompletesThisTick = this.dragonDeathTime >= 200;
        if (deathCompletesThisTick && serverLevel != null && blastOrigin != null) {
            double crownY = Mth.clamp(this.getY(), blastOrigin.getY() + 6.0D, blastOrigin.getY() + 18.0D);
            serverLevel.addFreshEntity(new DuatSunEaterCrownEntity(
                    serverLevel, this.getX(), crownY, this.getZ(), blastOrigin));
            this.remove(RemovalReason.KILLED);
        }
    }

    private void tickHitInvisibility() {
        if (this.attackState == ATTACK_APEP_SHADOW) {
            this.setInvisible(true);
            return;
        }
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
        SimplePhaseInstance phase = this.getPhaseManager().getCurrentPhase();
        if (phase.getPhase() == EnderDragonPhase.DYING || this.attackState != ATTACK_IDLE) {
            return;
        }
        if (phase.isSitting()
                || phase.getPhase() == EnderDragonPhase.LANDING
                || phase.getPhase() == EnderDragonPhase.LANDING_APPROACH) {
            this.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
        }
        this.runVanillaHoldingPattern();
    }

    private void runVanillaHoldingPattern() {
        VanillaFlightPilot pilot = this.getOrCreateVanillaFlightPilot();
        pilot.setPos(this.getX(), this.getY(), this.getZ());
        pilot.setYRot(this.getYRot());
        pilot.yBodyRot = this.yBodyRot;
        pilot.setDeltaMovement(this.getDeltaMovement());
        pilot.yRotA = this.vanillaFlightYawAcceleration;
        pilot.tickCount = this.tickCount;
        pilot.horizontalCollision = this.horizontalCollision;
        pilot.verticalCollision = this.verticalCollision;

        pilot.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
        DragonPhaseInstance vanillaPhase = pilot.getPhaseManager().getCurrentPhase();
        vanillaPhase.doServerTick();
        if (pilot.getPhaseManager().getCurrentPhase().getPhase() != EnderDragonPhase.HOLDING_PATTERN) {
            pilot.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
            vanillaPhase = pilot.getPhaseManager().getCurrentPhase();
            vanillaPhase.doServerTick();
        }

        Vec3 target = vanillaPhase.getFlyTargetLocation();
        if (target == null) {
            return;
        }
        double dx = target.x - this.getX();
        double dy = target.y - this.getY();
        double dz = target.z - this.getZ();
        double distanceSquared = dx * dx + dy * dy + dz * dz;
        float flySpeed = vanillaPhase.getFlySpeed();
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        if (horizontalDistance > 0.0D) {
            dy = Mth.clamp(dy / horizontalDistance, -flySpeed, flySpeed);
        }

        this.setDeltaMovement(this.getDeltaMovement().add(0.0D, dy * 0.01D, 0.0D));
        this.setYRot(Mth.wrapDegrees(this.getYRot()));
        Vec3 targetDirection = target.subtract(this.position()).normalize();
        Vec3 forward = new Vec3(
                Mth.sin(this.getYRot() * ((float) Math.PI / 180.0F)),
                this.getDeltaMovement().y,
                -Mth.cos(this.getYRot() * ((float) Math.PI / 180.0F))).normalize();
        float alignment = Math.max(((float) forward.dot(targetDirection) + 0.5F) / 1.5F, 0.0F);
        if (Math.abs(dx) > 1.0E-5F || Math.abs(dz) > 1.0E-5F) {
            float yawError = Mth.clamp(Mth.wrapDegrees(
                    180.0F - (float) Mth.atan2(dx, dz) * (180.0F / (float) Math.PI)
                            - this.getYRot()), -50.0F, 50.0F);
            this.vanillaFlightYawAcceleration *= 0.8F;
            this.vanillaFlightYawAcceleration += yawError * vanillaPhase.getTurnSpeed();
            this.setYRot(this.getYRot() + this.vanillaFlightYawAcceleration * 0.1F);
        }

        float distanceWeight = (float) (2.0D / (distanceSquared + 1.0D));
        this.moveRelative(VANILLA_FLIGHT_ACCELERATION
                        * (alignment * distanceWeight + (1.0F - distanceWeight)),
                new Vec3(0.0D, 0.0D, -1.0D));
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();
        this.vanillaFlightForward = forward;
        this.applyVanillaFlightDamping = true;
        this.hasImpulse = true;
    }

    private VanillaFlightPilot getOrCreateVanillaFlightPilot() {
        if (this.vanillaFlightPilot == null || this.vanillaFlightPilot.level() != this.level()) {
            this.vanillaFlightPilot = new VanillaFlightPilot(this.level());
        }
        return this.vanillaFlightPilot;
    }

    private void retainCombatChunk(ServerLevel level) {
        ChunkPos current = new ChunkPos(this.blockPosition());
        if (current.equals(this.combatTicketPos)) {
            if (this.tickCount % 100 == 0 && this.combatTicketKey != null) {
                level.getChunkSource().addRegionTicket(
                        TicketType.PORTAL, current, COMBAT_CHUNK_TICKET_RADIUS,
                        this.combatTicketKey);
            }
            return;
        }
        this.releaseCombatChunk(level);
        BlockPos key = this.blockPosition().immutable();
        level.getChunkSource().addRegionTicket(
                TicketType.PORTAL, current, COMBAT_CHUNK_TICKET_RADIUS, key);
        this.combatTicketPos = current;
        this.combatTicketKey = key;
    }

    private void releaseCombatChunk(ServerLevel level) {
        if (this.combatTicketPos == null || this.combatTicketKey == null) {
            return;
        }
        level.getChunkSource().removeRegionTicket(
                TicketType.PORTAL, this.combatTicketPos, COMBAT_CHUNK_TICKET_RADIUS,
                this.combatTicketKey);
        this.combatTicketPos = null;
        this.combatTicketKey = null;
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
                float blackHoleChance = blackHoleChance(this.combatStage);
                float eclipseChance = eclipseBreathChance(this.combatStage);
                float shadowChance = apepShadowChance(this.combatStage);
                float attackRoll = this.random.nextFloat();
                if (attackRoll < blackHoleChance) {
                    this.beginBlackHoleAttack(level, target);
                    return;
                }
                if (attackRoll < blackHoleChance + eclipseChance) {
                    this.beginEclipseBreath(level, target);
                    return;
                }
                if (attackRoll < blackHoleChance + eclipseChance + shadowChance) {
                    this.beginApepShadow(level, target);
                    return;
                }
                // Record the dive point once. Airborne targets receive an aerial impact point;
                // the attack does not continue steering after this moment.
                this.impactY = this.findDiveImpactY(level, target);
                this.lockedTargetId = null;
                this.attackState = ATTACK_ASCEND;
                this.attackTicks = 0;
                this.burrowTargetsHit.clear();
                level.playSound(null, this.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL,
                        SoundSource.HOSTILE, 3.0F, 0.62F);
            }
            return;
        }

        this.attackTicks++;
        if (this.attackState == ATTACK_BLACK_HOLE) {
            this.tickBlackHoleAttack(level);
            return;
        }
        if (this.attackState == ATTACK_ECLIPSE_BREATH) {
            this.tickEclipseBreath(level);
            return;
        }
        if (this.attackState == ATTACK_APEP_SHADOW) {
            this.tickApepShadow(level);
            return;
        }
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
                        SoundEvents.TRIDENT_THUNDER, SoundSource.HOSTILE, 2.8F, 0.55F);
            }
            return;
        }
        if (this.attackState == ATTACK_DIVE) {
            this.getPhaseManager().setPhase(EnderDragonPhase.CHARGING_PLAYER);
            this.steerDiveToward(new Vec3(this.impactX, this.impactY - 8.0D, this.impactZ));
            this.spawnDiveTrail(level);
            double horizontalDistance = Mth.square(this.getX() - this.impactX)
                    + Mth.square(this.getZ() - this.impactZ);
            if (horizontalDistance <= 100.0D && this.getY() <= this.impactY + 5.0D) {
                this.performBurrowImpact(level);
                this.attackState = ATTACK_BURROW;
                this.attackTicks = 0;
            } else if (this.attackTicks >= 60) {
                this.cancelLockedAttack();
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
            this.attackCooldown = Math.max(70, 135 - this.combatStage * 15) + this.random.nextInt(41);
            this.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
        }
    }

    private void beginBlackHoleAttack(ServerLevel level, Entity target) {
        this.impactX = target.getX();
        this.impactZ = target.getZ();
        this.impactY = this.findImpactSurface(level, target.blockPosition()) + 9.0D;
        this.attackState = ATTACK_BLACK_HOLE;
        this.attackTicks = 0;
        this.sendBlackHoleVfx(level);
        BlockPos origin = BlockPos.containing(this.impactX, this.impactY, this.impactZ);
        level.playSound(null, origin, SoundEvents.RESPAWN_ANCHOR_CHARGE,
                SoundSource.HOSTILE, 4.0F, 0.42F);
        level.playSound(null, origin, SoundEvents.WITHER_SPAWN,
                SoundSource.HOSTILE, 2.6F, 0.52F);
    }

    private void tickBlackHoleAttack(ServerLevel level) {
        Vec3 center = new Vec3(this.impactX, this.impactY, this.impactZ);
        double orbitAngle = this.tickCount * 0.045D;
        Vec3 castingPosition = center.add(
                Math.cos(orbitAngle) * 28.0D,
                15.0D + Math.sin(orbitAngle * 0.7D) * 4.0D,
                Math.sin(orbitAngle) * 28.0D);
        this.steerToward(castingPosition, 0.78D, 0.14D);

        int durationTicks = Config.APEP_BLACK_HOLE_DURATION_TICKS.get();
        int warningTicks = Math.min(Config.APEP_BLACK_HOLE_WARNING_TICKS.get(), durationTicks - 1);
        int collapseTick = Math.max(warningTicks, durationTicks - 20);
        if (this.attackTicks >= warningTicks && this.attackTicks < collapseTick) {
            this.pullIntoBlackHole(level, center);
        }
        if (this.attackTicks == collapseTick) {
            this.collapseBlackHole(level, center);
        }
        if (this.attackTicks >= durationTicks) {
            this.releaseBlackHolePull(level);
            this.sendBlackHoleVfxStop(level);
            this.attackState = ATTACK_IDLE;
            this.attackTicks = 0;
            this.attackCooldown = Math.max(55, 110 - this.combatStage * 15) + this.random.nextInt(31);
            this.lockedTargetId = null;
            this.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
        }
    }

    private void pullIntoBlackHole(ServerLevel level, Vec3 center) {
        double radius = Config.APEP_BLACK_HOLE_RADIUS.get();
        AABB bounds = AABB.ofSize(center, radius * 2.0D, radius * 2.0D, radius * 2.0D);
        List<Entity> targets = level.getEntities(this, bounds, this::canBeAffectedByBlackHole);
        int warningTicks = Math.min(Config.APEP_BLACK_HOLE_WARNING_TICKS.get(),
                Config.APEP_BLACK_HOLE_DURATION_TICKS.get() - 1);
        boolean damageTick = (this.attackTicks - warningTicks) % 20 == 0;
        for (Entity target : targets) {
            Vec3 targetCenter = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
            Vec3 inward = center.subtract(targetCenter);
            double distance = inward.length();
            if (distance < 0.25D || distance > radius) {
                continue;
            }
            double proximity = 1.0D - distance / radius;
            double force = Config.APEP_BLACK_HOLE_BASE_PULL.get()
                    + proximity * proximity * Config.APEP_BLACK_HOLE_PROXIMITY_PULL.get();
            Vec3 pull = inward.scale(force / distance);
            target.setDeltaMovement(target.getDeltaMovement()
                    .scale(Config.APEP_BLACK_HOLE_VELOCITY_RETENTION.get()).add(pull));
            target.hurtMarked = true;
            if (damageTick) {
                target.hurt(this.damageSources().mobAttack(this), 4.0F + this.combatStage * 2.0F);
            }
        }
    }

    private void collapseBlackHole(ServerLevel level, Vec3 center) {
        BlockPos origin = BlockPos.containing(center);
        level.playSound(null, origin, SoundEvents.WARDEN_SONIC_BOOM,
                SoundSource.HOSTILE, 5.0F, 0.5F);
        level.playSound(null, origin, SoundEvents.GENERIC_EXPLODE,
                SoundSource.HOSTILE, 4.0F, 0.62F);
        double radius = Config.APEP_BLACK_HOLE_RADIUS.get();
        AABB bounds = AABB.ofSize(center, radius * 2.0D, radius * 2.0D, radius * 2.0D);
        for (Entity target : level.getEntities(this, bounds, this::canBeAffectedByBlackHole)) {
            Vec3 inward = center.subtract(target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D));
            double distance = inward.length();
            if (distance > radius) {
                continue;
            }
            if (distance > 0.1D) {
                target.setDeltaMovement(inward.normalize().scale(1.15D));
                target.hurtMarked = true;
            }
            target.hurt(this.damageSources().mobAttack(this), 14.0F + this.combatStage * 4.0F);
        }
    }

    private void sendBlackHoleVfx(ServerLevel level) {
        DuatBlackHoleVfxPacket packet = new DuatBlackHoleVfxPacket(
                this.impactX, this.impactY, this.impactZ, Config.APEP_BLACK_HOLE_DURATION_TICKS.get());
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(this.impactX, this.impactY, this.impactZ) <= 192.0D * 192.0D) {
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
            }
        }
    }

    private void sendBlackHoleVfxStop(ServerLevel level) {
        DuatBlackHoleVfxPacket packet = new DuatBlackHoleVfxPacket(
                this.impactX, this.impactY, this.impactZ, 0);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(this.impactX, this.impactY, this.impactZ) <= 224.0D * 224.0D) {
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
            }
        }
    }

    private void clearCurrentAttackEffects(ServerLevel level) {
        if (this.attackState == ATTACK_BLACK_HOLE) {
            this.releaseBlackHolePull(level);
            this.sendBlackHoleVfxStop(level);
        }
        if (this.attackState == ATTACK_APEP_SHADOW) {
            this.clearHitInvisibility();
        }
    }

    private void releaseBlackHolePull(ServerLevel level) {
        Vec3 center = new Vec3(this.impactX, this.impactY, this.impactZ);
        double radius = Config.APEP_BLACK_HOLE_RADIUS.get();
        AABB bounds = AABB.ofSize(center, radius * 2.0D, radius * 2.0D, radius * 2.0D);
        for (Entity target : level.getEntities(this, bounds, this::canBeAffectedByBlackHole)) {
            Vec3 inward = center.subtract(
                    target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D));
            if (inward.lengthSqr() < 1.0E-6D) {
                continue;
            }
            Vec3 direction = inward.normalize();
            Vec3 velocity = target.getDeltaMovement();
            double inwardSpeed = velocity.dot(direction);
            if (inwardSpeed > 0.0D) {
                target.setDeltaMovement(velocity.subtract(direction.scale(inwardSpeed)));
                target.hurtMarked = true;
            }
        }
    }

    private void beginEclipseBreath(ServerLevel level, Entity target) {
        this.attackState = ATTACK_ECLIPSE_BREATH;
        this.attackTicks = 0;
        this.lockedTargetId = target.getUUID();
        this.impactX = target.getX();
        this.impactY = this.findImpactSurface(level, target.blockPosition()) + 1.0D;
        this.impactZ = target.getZ();
        this.setDeltaMovement(this.getDeltaMovement().scale(0.35D));
        this.getPhaseManager().setPhase(EnderDragonPhase.HOVERING);
        level.playSound(null, this.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL,
                SoundSource.HOSTILE, 4.5F, 0.42F);
        level.playSound(null, BlockPos.containing(this.impactX, this.impactY, this.impactZ),
                SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.HOSTILE, 3.5F, 0.38F);
    }

    private void tickEclipseBreath(ServerLevel level) {
        int warningTicks = Math.min(Config.APEP_ECLIPSE_BREATH_WARNING_TICKS.get(),
                Config.APEP_ECLIPSE_BREATH_DURATION_TICKS.get() - 1);
        int durationTicks = Config.APEP_ECLIPSE_BREATH_DURATION_TICKS.get();
        Vec3 start = this.eclipseBreathOrigin();
        Vec3 end = new Vec3(this.impactX, this.impactY, this.impactZ);
        this.setDeltaMovement(this.getDeltaMovement().scale(0.55D));
        this.facePoint(end);

        if (this.attackTicks < warningTicks) {
            if ((this.attackTicks & 1) == 0) {
                this.spawnEclipseBreathLine(level, start, end, false);
            }
            level.sendParticles(ParticleTypes.DRAGON_BREATH,
                    start.x, start.y, start.z, 8, 1.2D, 0.8D, 1.2D, 0.035D);
        } else {
            if ((this.attackTicks & 1) == 0) {
                this.spawnEclipseBreathLine(level, start, end, true);
            }
            if ((this.attackTicks - warningTicks) % 10 == 0) {
                this.damageAlongLine(level, start, end,
                        Config.APEP_ECLIPSE_BREATH_HIT_RADIUS.get(),
                        Config.APEP_ECLIPSE_BREATH_DAMAGE.get().floatValue()
                                + (this.combatStage - 1) * 3.0F,
                        true);
                level.playSound(null, BlockPos.containing(end), SoundEvents.ENDER_DRAGON_SHOOT,
                        SoundSource.HOSTILE, 3.2F, 0.52F + this.combatStage * 0.06F);
            }
        }

        if (this.attackTicks >= durationTicks) {
            this.attackState = ATTACK_IDLE;
            this.attackTicks = 0;
            this.attackCooldown = Math.max(55, 105 - this.combatStage * 12) + this.random.nextInt(31);
            this.lockedTargetId = null;
            this.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
        }
    }

    private Vec3 eclipseBreathOrigin() {
        Vec3 forward = this.getLookAngle();
        return this.position().add(0.0D, this.getBbHeight() * 0.48D, 0.0D)
                .add(forward.scale(5.2D));
    }

    private void spawnEclipseBreathLine(ServerLevel level, Vec3 start, Vec3 end, boolean active) {
        Vec3 line = end.subtract(start);
        int samples = active ? 18 : 11;
        for (int index = 1; index <= samples; index++) {
            double progress = index / (double) samples;
            Vec3 point = start.add(line.scale(progress));
            level.sendParticles(active ? ParticleTypes.DRAGON_BREATH : ParticleTypes.SMOKE,
                    point.x, point.y, point.z, active ? 2 : 1,
                    active ? 0.28D : 0.08D, active ? 0.28D : 0.08D,
                    active ? 0.28D : 0.08D, active ? 0.045D : 0.005D);
            if (active && index % 3 == 0) {
                level.sendParticles(ParticleTypes.FLAME, point.x, point.y, point.z,
                        1, 0.1D, 0.1D, 0.1D, 0.01D);
            }
        }
    }

    private void beginApepShadow(ServerLevel level, Entity target) {
        this.attackState = ATTACK_APEP_SHADOW;
        this.attackTicks = 0;
        this.lockedTargetId = target.getUUID();
        this.impactX = target.getX();
        this.impactY = this.findImpactSurface(level, target.blockPosition()) + 1.0D;
        this.impactZ = target.getZ();
        this.burrowTargetsHit.clear();
        this.setDeltaMovement(Vec3.ZERO);
        this.hitInvisibilityTicks = this.shadowAttackDuration();
        this.setInvisible(true);
        this.getPhaseManager().setPhase(EnderDragonPhase.HOVERING);
        level.playSound(null, this.blockPosition(), SoundEvents.WARDEN_HEARTBEAT,
                SoundSource.HOSTILE, 5.0F, 0.48F);
        level.sendParticles(ParticleTypes.SQUID_INK,
                this.getX(), this.getY() + 2.0D, this.getZ(),
                55, 4.0D, 2.5D, 4.0D, 0.12D);
    }

    private void tickApepShadow(ServerLevel level) {
        this.setDeltaMovement(Vec3.ZERO);
        int strikeCount = this.shadowStrikeCount();
        int duration = this.shadowAttackDuration();
        int strikeIndex = Math.max(0, (this.attackTicks - 1) / SHADOW_STRIKE_INTERVAL);
        int strikeTick = Math.max(0, (this.attackTicks - 1) % SHADOW_STRIKE_INTERVAL);
        if (strikeIndex < strikeCount) {
            double angle = strikeIndex * 2.399963229728653D + this.getId() * 0.173D;
            Vec3 center = new Vec3(this.impactX, this.impactY, this.impactZ);
            Vec3 lateral = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
            Vec3 start = center.add(lateral.scale(22.0D)).add(0.0D, 7.0D, 0.0D);
            Vec3 end = center.subtract(lateral.scale(22.0D)).add(0.0D, 1.0D, 0.0D);
            if (strikeTick < SHADOW_WARNING_TICKS && strikeTick % 3 == 0) {
                this.spawnShadowPath(level, start, end, false);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                        start.x, start.y, start.z, 14, 1.4D, 1.1D, 1.4D, 0.08D);
            } else if (strikeTick == SHADOW_WARNING_TICKS) {
                this.spawnShadowPath(level, start, end, true);
                this.damageAlongLine(level, start, end,
                        Config.APEP_SHADOW_HIT_RADIUS.get(),
                        Config.APEP_SHADOW_DAMAGE.get().floatValue()
                                + (this.combatStage - 1) * 4.0F,
                        false);
                level.playSound(null, BlockPos.containing(center), SoundEvents.WITHER_SHOOT,
                        SoundSource.HOSTILE, 3.5F, 0.46F + strikeIndex * 0.04F);
            }
        }

        if (this.attackTicks >= duration) {
            this.clearHitInvisibility();
            this.attackState = ATTACK_IDLE;
            this.attackTicks = 0;
            this.attackCooldown = Math.max(60, 120 - this.combatStage * 15) + this.random.nextInt(31);
            this.lockedTargetId = null;
            this.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    this.getX(), this.getY() + 2.0D, this.getZ(),
                    70, 4.0D, 2.5D, 4.0D, 0.18D);
            level.playSound(null, this.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL,
                    SoundSource.HOSTILE, 5.5F, 0.55F);
        }
    }

    private int shadowStrikeCount() {
        return Config.APEP_SHADOW_STRIKE_COUNT.get() + this.combatStage - 1;
    }

    private int shadowAttackDuration() {
        return this.shadowStrikeCount() * SHADOW_STRIKE_INTERVAL + 18;
    }

    private void spawnShadowPath(ServerLevel level, Vec3 start, Vec3 end, boolean striking) {
        Vec3 line = end.subtract(start);
        int samples = striking ? 24 : 12;
        for (int index = 0; index <= samples; index++) {
            Vec3 point = start.add(line.scale(index / (double) samples));
            level.sendParticles(striking ? ParticleTypes.SQUID_INK : ParticleTypes.SMOKE,
                    point.x, point.y, point.z, striking ? 2 : 1,
                    striking ? 0.38D : 0.06D, striking ? 0.32D : 0.06D,
                    striking ? 0.38D : 0.06D, striking ? 0.06D : 0.0D);
            if (striking && index % 2 == 0) {
                level.sendParticles(ParticleTypes.DRAGON_BREATH,
                        point.x, point.y, point.z, 1, 0.18D, 0.18D, 0.18D, 0.02D);
            }
        }
    }

    private void damageAlongLine(ServerLevel level, Vec3 start, Vec3 end,
                                 double radius, float damage, boolean eclipseBreath) {
        AABB bounds = new AABB(start, end).inflate(radius);
        for (Entity target : level.getEntities(this, bounds, this::canAttackIslandEntity)) {
            Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
            double hitRadius = radius + target.getBbWidth() * 0.45D;
            if (distanceToSegmentSqr(center, start, end) > hitRadius * hitRadius) {
                continue;
            }
            target.hurt(eclipseBreath
                    ? ModDamageTypes.apepEclipseBreath(level, this)
                    : ModDamageTypes.apepShadow(level, this), damage);
            if (eclipseBreath) {
                target.setSecondsOnFire(4);
                if (target instanceof LivingEntity living) {
                    living.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0));
                }
            } else if (target instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 50, 0));
            }
        }
    }

    private static double distanceToSegmentSqr(Vec3 point, Vec3 start, Vec3 end) {
        Vec3 line = end.subtract(start);
        double lengthSqr = line.lengthSqr();
        if (lengthSqr < 1.0E-8D) {
            return point.distanceToSqr(start);
        }
        double progress = Mth.clamp(point.subtract(start).dot(line) / lengthSqr, 0.0D, 1.0D);
        return point.distanceToSqr(start.add(line.scale(progress)));
    }

    private void facePoint(Vec3 point) {
        Vec3 offset = point.subtract(this.position());
        if (offset.horizontalDistanceSqr() < 1.0E-8D) {
            return;
        }
        float yaw = (float) Math.toDegrees(Math.atan2(offset.z, offset.x)) + 90.0F;
        this.setYRot(Mth.rotLerp(0.22F, this.getYRot(), yaw));
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();
    }

    private void chargeToward(Vec3 target) {
        this.getPhaseManager().setPhase(EnderDragonPhase.CHARGING_PLAYER);
        this.steerToward(target, 0.62D, 0.38D);
    }

    private static double stageMaxHealth(int stage) {
        return switch (Mth.clamp(stage, 1, FINAL_COMBAT_STAGE)) {
            case 1 -> Config.APEP_STAGE_ONE_HEALTH.get();
            case 2 -> Config.APEP_STAGE_TWO_HEALTH.get();
            default -> Config.APEP_STAGE_THREE_HEALTH.get();
        };
    }

    private static float stageDamageTakenMultiplier(int stage) {
        double reduction = switch (Mth.clamp(stage, 1, FINAL_COMBAT_STAGE)) {
            case 1 -> Config.APEP_STAGE_ONE_DAMAGE_REDUCTION.get();
            case 2 -> Config.APEP_STAGE_TWO_DAMAGE_REDUCTION.get();
            default -> Config.APEP_STAGE_THREE_DAMAGE_REDUCTION.get();
        };
        return (float) (1.0D - reduction);
    }

    private static float blackHoleChance(int stage) {
        return switch (Mth.clamp(stage, 1, FINAL_COMBAT_STAGE)) {
            case 1 -> Config.APEP_BLACK_HOLE_STAGE_ONE_CHANCE.get().floatValue();
            case 2 -> Config.APEP_BLACK_HOLE_STAGE_TWO_CHANCE.get().floatValue();
            default -> Config.APEP_BLACK_HOLE_STAGE_THREE_CHANCE.get().floatValue();
        };
    }

    private static float eclipseBreathChance(int stage) {
        return switch (Mth.clamp(stage, 1, FINAL_COMBAT_STAGE)) {
            case 1 -> Config.APEP_ECLIPSE_BREATH_STAGE_ONE_CHANCE.get().floatValue();
            case 2 -> Config.APEP_ECLIPSE_BREATH_STAGE_TWO_CHANCE.get().floatValue();
            default -> Config.APEP_ECLIPSE_BREATH_STAGE_THREE_CHANCE.get().floatValue();
        };
    }

    private static float apepShadowChance(int stage) {
        return switch (Mth.clamp(stage, 1, FINAL_COMBAT_STAGE)) {
            case 1 -> Config.APEP_SHADOW_STAGE_ONE_CHANCE.get().floatValue();
            case 2 -> Config.APEP_SHADOW_STAGE_TWO_CHANCE.get().floatValue();
            default -> Config.APEP_SHADOW_STAGE_THREE_CHANCE.get().floatValue();
        };
    }

    private void steerToward(Vec3 target, double speed, double response) {
        Vec3 offset = target.subtract(this.position());
        if (offset.lengthSqr() < 1.0E-6D) {
            return;
        }
        Vec3 desired = offset.normalize().scale(speed);
        Vec3 current = this.getDeltaMovement();
        Vec3 movement = current.scale(1.0D - response).add(desired.scale(response));
        this.setDeltaMovement(movement);
        this.hasImpulse = true;
        this.faceTravelDirection(movement);
    }

    private void steerDiveToward(Vec3 target) {
        Vec3 offset = target.subtract(this.position());
        double distance = offset.length();
        if (distance < 1.0E-4D) {
            return;
        }
        double speed = Mth.clamp(distance * 0.18D, 1.45D, DIVE_SPEED);
        Vec3 desired = offset.scale(speed / distance);
        Vec3 movement = this.getDeltaMovement().scale(1.0D - DIVE_RESPONSE)
                .add(desired.scale(DIVE_RESPONSE));
        if (offset.y < -6.0D && movement.y > -1.25D) {
            movement = new Vec3(movement.x, -1.25D, movement.z);
        }
        if (movement.lengthSqr() > DIVE_SPEED * DIVE_SPEED) {
            movement = movement.normalize().scale(DIVE_SPEED);
        }
        this.setDeltaMovement(movement);
        this.hasImpulse = true;
        this.faceTravelDirection(movement);
    }

    @Override
    public void travel(Vec3 travelVector) {
        Vec3 movement = this.getDeltaMovement();
        if (this.isControlledByLocalInstance()) {
            this.move(MoverType.SELF, movement);
            if (this.applyVanillaFlightDamping && movement.lengthSqr() > 1.0E-8D) {
                Vec3 normalizedMovement = movement.normalize();
                double horizontalDamping = 0.8D
                        + 0.15D * (normalizedMovement.dot(this.vanillaFlightForward) + 1.0D) / 2.0D;
                this.setDeltaMovement(movement.multiply(horizontalDamping, 0.91D, horizontalDamping));
            } else {
                this.setDeltaMovement(movement.scale(0.96D));
            }
        }
        this.applyVanillaFlightDamping = false;
    }

    private final class VanillaFlightPilot extends EnderDragon {
        private VanillaFlightPilot(Level level) {
            super(EntityType.ENDER_DRAGON, level);
            this.setSilent(true);
        }

        @Override
        public BlockPos getFightOrigin() {
            return DuatSunEaterEntity.this.getFightOrigin();
        }

        @Override
        public int findClosestNode() {
            return DuatSunEaterEntity.this.findClosestNode();
        }

        @Override
        public int findClosestNode(double x, double y, double z) {
            return DuatSunEaterEntity.this.findClosestNode(x, y, z);
        }

        @Override
        public @Nullable Path findPath(
                int startIndex, int destinationIndex, @Nullable Node extraDestination) {
            return DuatSunEaterEntity.this.findPath(startIndex, destinationIndex, extraDestination);
        }

        @Override
        public @Nullable EndDragonFight getDragonFight() {
            return this.level() instanceof ServerLevel serverLevel
                    ? serverLevel.getDragonFight() : null;
        }
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (this.level() instanceof ServerLevel serverLevel) {
            this.releaseCombatChunk(serverLevel);
            this.clearCurrentAttackEffects(serverLevel);
        }
        super.remove(reason);
    }

    @Nullable
    private Entity selectIslandTarget(ServerLevel level) {
        BlockPos center = this.getFightOrigin();
        AABB bounds = new AABB(
                center.getX() - ISLAND_COMBAT_RADIUS, level.getMinBuildHeight(), center.getZ() - ISLAND_COMBAT_RADIUS,
                center.getX() + ISLAND_COMBAT_RADIUS, level.getMaxBuildHeight(), center.getZ() + ISLAND_COMBAT_RADIUS);
        List<Player> players = level.getEntitiesOfClass(Player.class, bounds,
                this::canAttackIslandEntity);
        if (!players.isEmpty()) {
            return players.get(this.random.nextInt(players.size()));
        }
        List<Entity> targets = level.getEntities(this, bounds, this::canAttackIslandEntity);
        if (targets.isEmpty()) {
            return null;
        }
        return targets.get(this.random.nextInt(targets.size()));
    }

    private void cancelLockedAttack() {
        this.attackState = ATTACK_IDLE;
        this.attackTicks = 0;
        this.attackCooldown = 40;
        this.lockedTargetId = null;
        this.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
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
                && !(entity instanceof LightningBolt)
                && !(entity instanceof FallingBlockEntity)
                && !GoetyObsidianMonolithCompat.isIslandMonolith(entity);
    }

    private boolean canBeAffectedByBlackHole(Entity entity) {
        return !(entity instanceof DuatSunEaterEntity) && this.canAttackIslandEntity(entity);
    }

    private double findImpactSurface(ServerLevel level, BlockPos targetPos) {
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                targetPos.getX(), targetPos.getZ());
        return Math.max(RamessesEndIslandPiece.CENTER_Y, surfaceY);
    }

    private double findDiveImpactY(ServerLevel level, Entity target) {
        double surfaceY = findImpactSurface(level, target.blockPosition());
        double targetY = target.getY();
        if (targetY > surfaceY + 3.0D) {
            return targetY + target.getBbHeight() * 0.5D;
        }
        return surfaceY;
    }

    private void performBurrowImpact(ServerLevel level) {
        double hitRadius = 4.5D;
        AABB hitBox = new AABB(
                this.impactX - hitRadius, this.impactY - 5.0D, this.impactZ - hitRadius,
                this.impactX + hitRadius, this.impactY + 9.0D, this.impactZ + hitRadius);
        for (Entity target : level.getEntities(this, hitBox, this::canAttackIslandEntity)) {
            double dx = target.getX() - this.impactX;
            double dz = target.getZ() - this.impactZ;
            if (dx * dx + dz * dz > hitRadius * hitRadius) {
                continue;
            }
            target.hurt(ModDamageTypes.apepDive(this.level(), this),
                    Config.APEP_DIVE_TRUE_DAMAGE.get().floatValue());
            double launchVelocity = Config.APEP_DIVE_LAUNCH_VELOCITY.get();
            Vec3 movement = target.getDeltaMovement();
            target.setDeltaMovement(movement.x, Math.max(movement.y, launchVelocity), movement.z);
            target.hurtMarked = true;
            this.burrowTargetsHit.add(target.getUUID());
        }
        BlockPos impact = BlockPos.containing(this.impactX, this.impactY, this.impactZ);
        level.playSound(null, impact, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 3.5F, 0.55F);
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
        AABB hitBox = new AABB(
                this.impactX - radius, this.impactY - 5.0D, this.impactZ - radius,
                this.impactX + radius, this.impactY + 9.0D, this.impactZ + radius);
        for (Entity target : level.getEntities(this, hitBox, this::canAttackIslandEntity)) {
            if (!this.burrowTargetsHit.add(target.getUUID())) {
                continue;
            }
            double dx = target.getX() - this.impactX;
            double dz = target.getZ() - this.impactZ;
            if (dx * dx + dz * dz > radius * radius) {
                continue;
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
        double x = this.impactX;
        double z = this.impactZ;
        double y = this.impactY + 0.22D;
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
        double x = this.impactX;
        double z = this.impactZ;
        double y = this.impactY;
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            DuatCuriosSealManager.markBossAttack(bolt, this);
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
        if (this.stageTransitionTicks >= RamessesEndIslandPiece.CRYSTAL_CAGE_BUILD_TICKS
                && this.stageTransitionTicks < RamessesEndIslandPiece.CRYSTAL_CAGE_BUILD_TICKS
                + STAGE_RITUAL_TICKS) {
            int elapsed = this.stageTransitionTicks - RamessesEndIslandPiece.CRYSTAL_CAGE_BUILD_TICKS;
            BlockPos focus = RamessesEndIslandPiece.stageRitualFocus(this.getFightOrigin());
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new SunEaterStageRitualVfxPacket(
                            focus.getX() + 0.5D, focus.getY() + 0.5D, focus.getZ() + 0.5D,
                            this.combatStage, elapsed, STAGE_RITUAL_TICKS));
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    public int findClosestNode() {
        this.initializeIslandNodes();
        return this.findClosestNodeAbsolute(this.getX(), this.getY(), this.getZ());
    }

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
        int firstNode = 0;
        for (int i = firstNode; i < this.islandNodes.length; i++) {
            float distance = this.islandNodes[i].distanceToSqr(target);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

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
        int firstNode = 0;

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
