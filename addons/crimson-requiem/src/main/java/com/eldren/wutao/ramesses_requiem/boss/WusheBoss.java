package com.eldren.wutao.ramesses_requiem.boss;

import com.eldren.wutao.ramesses_requiem.RequiemItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Independent boss combat controller. Sword item callbacks are never used by this entity. */
public final class WusheBoss extends Monster {
    private static final EntityDataAccessor<Float> MAX_HP = SynchedEntityData.defineId(WusheBoss.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> TIER = SynchedEntityData.defineId(WusheBoss.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(WusheBoss.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SKILL = SynchedEntityData.defineId(WusheBoss.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> AGE = SynchedEntityData.defineId(WusheBoss.class, EntityDataSerializers.INT);
    private final ServerBossEvent bar = new ServerBossEvent(Component.translatable("entity.ramesses_requiem.wushe"), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
    private final long[] readyAt = new long[6];
    private long protectedUntil;
    private boolean healthReady, acceptingDamage, loadingHealth;
    private float lossBudget;
    private int recovery, meleeCooldown, skillCursor;
    private java.util.UUID skillTarget;
    private Vec3 strikePoint = Vec3.ZERO;

    public WusheBoss(EntityType<? extends WusheBoss> type, Level level) {
        super(type, level);
        xpReward = 200;
        setPersistenceRequired();
        setCanPickUpLoot(false);
        setItemSlot(EquipmentSlot.MAINHAND, RequiemItems.CRIMSON_REQUIEM.get().getDefaultInstance());
        setDropChance(EquipmentSlot.MAINHAND, 0);
        healthReady = true;
        if (!level.isClientSide) configureTier(tierFor(level));
    }
    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 666).add(Attributes.ATTACK_DAMAGE, 12)
                .add(Attributes.MOVEMENT_SPEED, .31).add(Attributes.FOLLOW_RANGE, 48).add(Attributes.KNOCKBACK_RESISTANCE, 1);
    }
    @Override protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(MAX_HP, 666F); entityData.define(TIER, 0); entityData.define(PHASE, 1);
        entityData.define(SKILL, 0); entityData.define(AGE, 0);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, .8) {
            @Override public boolean canUse() { return skill() == 0 && getTarget() == null && super.canUse(); }
            @Override public boolean canContinueToUse() { return skill() == 0 && getTarget() == null && super.canContinueToUse(); }
        });
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(0, new HurtByTargetGoal(this));
    }
    public static int tierFor(Level level) { return level.dimension().equals(Level.END) ? 2 : level.dimension().equals(Level.NETHER) ? 1 : 0; }
    public int tier() { return entityData.get(TIER); }
    public int phase() { return entityData.get(PHASE); }
    public int skill() { return entityData.get(SKILL); }
    public int skillAge() { return entityData.get(AGE); }
    public boolean protectionActive() { return level().getGameTime() < protectedUntil; }
    public void configureTier(int tier) {
        float ratio = healthReady ? getHealth() / getMaxHealth() : 1;
        entityData.set(TIER, Mth.clamp(tier, 0, 2));
        entityData.set(MAX_HP, (float)WusheConfig.health(tier()));
        loadingHealth = true;
        setHealth(getMaxHealth() * ratio);
        loadingHealth = false;
    }
    // Vanilla MAX_HEALTH clamps to 1024. Override only this boss's queried/synchronized maximum.
    @Override public double getAttributeValue(Attribute attribute) {
        return attribute == Attributes.MAX_HEALTH && healthReady ? entityData.get(MAX_HP) : super.getAttributeValue(attribute);
    }
    @Override public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide || !isAlive() || source.is(DamageTypeTags.IS_FALL) || protectionActive() || amount <= 0 || Float.isNaN(amount)) return false;
        boolean capped = amount > getMaxHealth() * WusheConfig.threshold(tier());
        float allowed = capped ? WusheConfig.CAPPED_DAMAGE.get().floatValue() : amount * (1F - (float)WusheConfig.reduction(phase()));
        float before = getHealth();
        acceptingDamage = true;
        lossBudget = allowed;
        try { return super.hurt(source, allowed); }
        finally {
            acceptingDamage = false;
            if (capped && getHealth() < before) protectedUntil = level().getGameTime() + WusheConfig.IMMUNITY_TICKS.get();
        }
    }
    @Override public void setHealth(float health) {
        if (healthReady && !loadingHealth && health < getHealth()) {
            if (!acceptingDamage) return; // Includes the final sword's direct-health execution path.
            float loss = Math.min(getHealth() - health, Math.max(0, lossBudget));
            lossBudget -= loss;
            health = getHealth() - loss;
        }
        super.setHealth(health);
    }
    @Override public void die(DamageSource source) { if (getHealth() <= 0) { WusheControl.releaseBoss(this); super.die(source); } }
    @Override public void kill() { loadingHealth = true; super.setHealth(0); loadingHealth = false; die(damageSources().genericKill()); }
    @Override public boolean causeFallDamage(float distance, float multiplier, DamageSource source) { return false; }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override public void startSeenByPlayer(ServerPlayer player) { super.startSeenByPlayer(player); bar.addPlayer(player); }
    @Override public void stopSeenByPlayer(ServerPlayer player) { super.stopSeenByPlayer(player); bar.removePlayer(player); }

    @Override public void tick() {
        super.tick();
        if (level().isClientSide || !isAlive()) return;
        if (!AstralArena.isArena(level()) && tier() != tierFor(level())) configureTier(tierFor(level()));
        entityData.set(PHASE, Math.max(phase(), getHealth() <= getMaxHealth() * .35F ? 3 : getHealth() <= getMaxHealth() * .7F ? 2 : 1));
        bar.setProgress(Mth.clamp(getHealth() / getMaxHealth(), 0, 1));
        bar.setName(getDisplayName().copy().append(" · " + phase()));
        if (AstralArena.tickBoss(this)) return;
        if (recovery > 0) recovery--;
        if (meleeCooldown > 0) meleeCooldown--;
        if (skill() == 0 && !isNoAi()) acquirePlayerTarget();
        LivingEntity target = skill() == 0 ? getTarget() : ((ServerLevel)level()).getEntity(skillTarget) instanceof LivingEntity living ? living : null;
        if (!eligible(target) || distanceToSqr(target) > 96 * 96) {
            if (skill() != 0) finishSkill();
            if (getTarget() != null) { setTarget(null); getNavigation().stop(); }
            return;
        }
        getLookControl().setLookAt(target, 40, 40);
        if (skill() != 0) {
            entityData.set(AGE, skillAge() + 1);
            WusheSkills.tick(this, target);
            return;
        }
        if (recovery > 0) return;
        int available = tier() == 0 ? 3 : 5; // Slots 6–9 reserved until the user designs them.
        for (int offset = 0; offset < available; offset++) {
            int candidate = (skillCursor + offset) % available + 1;
            if (level().getGameTime() >= readyAt[candidate] && canStart(candidate, target)) {
                skillCursor = candidate % available;
                startSkill(candidate, target);
                return;
            }
        }
        getNavigation().moveTo(target, 1.1);
        if (distanceToSqr(target) < 9 && hasLineOfSight(target) && meleeCooldown == 0) {
            swing(InteractionHand.MAIN_HAND);
            target.hurt(damageSources().mobAttack(this), WusheConfig.MELEE_DAMAGE.get().floatValue());
            meleeCooldown = 24;
        }
    }
    public static boolean eligible(LivingEntity target) {
        return target != null && target.isAlive() && !(target instanceof WusheBoss)
                && (!(target instanceof Player p) || !p.isSpectator());
    }
    // Vanilla combat TargetingConditions exclude creative players before custom predicates run.
    void acquirePlayerTarget() {
        if (eligible(getTarget()) && distanceToSqr(getTarget()) <= 96 * 96) return;
        Player nearest = null;
        double distance = getAttributeValue(Attributes.FOLLOW_RANGE);
        double best = distance * distance;
        for (Player player : level().players()) {
            double squared = distanceToSqr(player);
            if (eligible(player) && squared <= best) { nearest = player; best = squared; }
        }
        if (nearest == null && getTarget() != null) getNavigation().stop();
        setTarget(nearest);
    }
    public boolean canStart(int id, LivingEntity target) {
        return eligible(target) && hasLineOfSight(target) && (id <= 3 || tier() > 0 && target instanceof ServerPlayer)
                && (id != 2 || distanceToSqr(target) < 16) && (id != 5 || !AstralArena.isArena(level()))
                && (!(target instanceof ServerPlayer p) || !WusheControl.heldByOther(p, getUUID()));
    }
    public boolean startSkill(int id, LivingEntity target) {
        if (id < 1 || id > 5 || skill() != 0 || !canStart(id, target)) return false;
        getNavigation().stop();
        setTarget(target);
        skillTarget = target.getUUID();
        strikePoint = target.position();
        entityData.set(SKILL, id); entityData.set(AGE, 0);
        readyAt[id] = level().getGameTime() + WusheConfig.cooldown(id);
        if (id == 4 || id == 5) WusheControl.begin((ServerPlayer)target, this, id == 4 ? 100 : 70);
        sound(SoundEvents.PIGLIN_BRUTE_ANGRY, .8F, .65F);
        return true;
    }
    public void finishSkill() {
        if (skill() == 3) readyAt[3] = level().getGameTime() + WusheConfig.cooldown(3);
        WusheControl.releaseBoss(this);
        entityData.set(SKILL, 0); entityData.set(AGE, 0); skillTarget = null;
        setNoGravity(false); recovery = 18;
    }
    public Vec3 strikePoint() { return strikePoint; }
    public void strikePoint(Vec3 point) { strikePoint = point; }
    public void sound(SoundEvent sound, float volume, float pitch) { level().playSound(null, blockPosition(), sound, SoundSource.HOSTILE, volume, pitch); }
    public void lookAtTarget(LivingEntity target) {
        Vec3 delta = target.getEyePosition().subtract(getEyePosition());
        setYRot((float)Math.toDegrees(Math.atan2(-delta.x, delta.z))); yBodyRot = getYRot(); setYHeadRot(getYRot());
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("WusheTier", tier()); tag.putInt("WushePhase", phase()); tag.putFloat("WusheMaxHealth", getMaxHealth());
        tag.putLongArray("WusheCooldowns", readyAt); tag.putLong("WusheProtectedUntil", protectedUntil);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        loadingHealth = true;
        if (tag.contains("WusheMaxHealth")) entityData.set(MAX_HP, Math.max(1, tag.getFloat("WusheMaxHealth")));
        entityData.set(TIER, Mth.clamp(tag.getInt("WusheTier"), 0, 2));
        entityData.set(PHASE, Mth.clamp(tag.getInt("WushePhase"), 1, 3));
        super.readAdditionalSaveData(tag);
        loadingHealth = false;
        long[] cooldowns = tag.getLongArray("WusheCooldowns");
        System.arraycopy(cooldowns, 0, readyAt, 0, Math.min(cooldowns.length, readyAt.length));
        protectedUntil = tag.getLong("WusheProtectedUntil");
        // Interrupted cinematics never resume with a stale player lock.
        setNoGravity(false); entityData.set(SKILL, 0); entityData.set(AGE, 0); skillTarget = null;
    }
}
