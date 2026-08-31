package com.eldren.wutao.ramesses_curse;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UraeusFlareEntity extends Entity implements GeoEntity {
    private static final byte BURST_VFX_EVENT = 68;
    private static final EntityDataAccessor<Integer> TARGET_ID = SynchedEntityData.defineId(UraeusFlareEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(UraeusFlareEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> RAIN_EMPOWERED = SynchedEntityData.defineId(UraeusFlareEntity.class, EntityDataSerializers.BOOLEAN);
    private static final String OWNER_KEY = "Owner";
    private static final String AGE_KEY = "Age";
    private static final String DAMAGE_KEY = "Damage";
    private static final String RAIN_KEY = "RainEmpowered";
    private static final String SEARCH_X_KEY = "SearchX";
    private static final String SEARCH_Y_KEY = "SearchY";
    private static final String SEARCH_Z_KEY = "SearchZ";
    private static final String HIT_TARGETS_KEY = "HitTargets";
    private static final String OPENING_ROUND_KEY = "OpeningRoundApplied";
    private static final String BRANCH_DEPTH_KEY = "BranchDepth";
    private static final String BRANCH_COOLDOWN_KEY = "BranchCooldown";
    private static final int MAX_AGE = 20 * 60;
    private static final int BRANCH_MAX_AGE = 20 * 8;
    private static final double SPEED = 0.38D;
    private static final double TURN_STRENGTH = 0.18D;
    private static final double CLOSE_TURN_STRENGTH = 0.85D;
    private static final double ORBIT_RADIUS = 0.14D;
    private static final double ORBIT_VERTICAL_RADIUS = 0.08D;
    private static final double TANGENT_STRENGTH = 0.025D;
    private static final double SEARCH_RADIUS = 16.0D;
    private static final float BRANCH_CHANCE = 0.5F;
    private static final float BRANCH_DAMAGE = 10.0F;
    private static final float BRANCH_CRIT_DAMAGE = 15.0F;
    private static final float BRANCH_CRIT_CHANCE = 0.25F;
    private static final float RAIN_BURST_CHANCE = 0.5F;
    private static final float STORM_STAR_CHANCE = 0.5F;
    private static final int BRANCH_CHILDREN = 2;
    private static final int MAX_BRANCH_DEPTH = 1;
    public static final float CHAIN_DAMAGE = 20.0F;

    private UUID ownerUuid;
    private Vec3 searchCenter = Vec3.ZERO;
    private boolean openingRoundApplied;
    private int branchDepth;
    private int branchCooldown;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final List<UUID> hitTargets = new ArrayList<>();
    private final List<Vec3> trailPositions = new ArrayList<>();

    public UraeusFlareEntity(EntityType<? extends UraeusFlareEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public static void spawn(ServerLevel level, Player owner, LivingEntity target, float damage, boolean rainEmpowered) {
        if (damage < 0.0F || !target.isAlive()) {
            return;
        }
        UraeusFlareEntity flare = new UraeusFlareEntity(ModEntities.URAEUS_FLARE.get(), level);
        Vec3 look = owner.getLookAngle();
        flare.setPos(owner.getX() + look.x * 0.85D, owner.getEyeY() - 0.22D + look.y * 0.25D, owner.getZ() + look.z * 0.85D);
        flare.setDeltaMovement(look.scale(0.16D).add(0.0D, 0.04D, 0.0D));
        flare.ownerUuid = owner.getUUID();
        flare.searchCenter = target.position();
        flare.entityData.set(TARGET_ID, target.getId());
        flare.entityData.set(DAMAGE, damage);
        flare.entityData.set(RAIN_EMPOWERED, rainEmpowered);
        level.addFreshEntity(flare);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TARGET_ID, -1);
        builder.define(DAMAGE, 0.0F);
        builder.define(RAIN_EMPOWERED, false);
    }

    @Override
    public void tick() {
        super.tick();
        recordTrailPosition();

        if (branchCooldown > 0) {
            branchCooldown--;
        }

        int maxAge = branchDepth > 0 ? BRANCH_MAX_AGE : MAX_AGE;
        if (this.tickCount > maxAge) {
            discard();
            return;
        }

        if (!level().isClientSide && !openingRoundApplied) {
            openingRoundApplied = true;
            applyOpeningRound();
            if (!selectNextTarget()) {
                discard();
                return;
            }
        }

        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) {
            if (!level().isClientSide) {
                if (!selectNextTarget()) {
                    discard();
                }
            }
            return;
        }

        Vec3 targetPos = target.position().add(0.0D, target.getBbHeight() * 0.58D, 0.0D);
        Vec3 toTarget = targetPos.subtract(position());
        double distance = toTarget.length();
        double hitDistance = Math.max(1.35D, Math.max(target.getBbWidth(), target.getBbHeight() * 0.35D) + 0.75D);
        if (!level().isClientSide && distance <= hitDistance) {
            hitTargetOnce(target);
            if (!selectNextTarget()) {
                discard();
            }
            return;
        }

        Vec3 desired = getCurvedApproachVelocity(targetPos, toTarget, distance);
        Vec3 nextVelocity = steerSmoothly(getDeltaMovement(), desired, distance);
        setDeltaMovement(nextVelocity);
        Vec3 nextPosition = position().add(nextVelocity);
        if (!level().isClientSide && willHitTarget(target, nextPosition, hitDistance)) {
            setPos(nextPosition.x, nextPosition.y, nextPosition.z);
            hitTargetOnce(target);
            if (!selectNextTarget()) {
                discard();
            }
            return;
        }
        setPos(nextPosition.x, nextPosition.y, nextPosition.z);
        updateRotationFromVelocity(nextVelocity);
    }

    private Vec3 getCurvedApproachVelocity(Vec3 targetPos, Vec3 toTarget, double distance) {
        if (distance < 1.0E-5D) {
            return Vec3.ZERO;
        }
        if (distance < 5.5D || ORBIT_RADIUS <= 0.0D) {
            return toTarget.normalize().scale(SPEED);
        }

        Vec3 forward = toTarget.normalize();
        Vec3 side = forward.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (side.lengthSqr() < 1.0E-5D) {
            side = forward.cross(new Vec3(1.0D, 0.0D, 0.0D));
        }
        side = side.normalize();
        Vec3 up = side.cross(forward).normalize();

        double phase = tickCount * 0.22D + getId() * 0.37D;
        double approach = Math.min(1.0D, distance / 5.0D);
        double settle = Math.min(1.0D, Math.max(0.0D, (distance - 5.0D) / 4.0D));
        double radius = ORBIT_RADIUS * approach * settle;
        double verticalRadius = ORBIT_VERTICAL_RADIUS * approach * settle;
        Vec3 curvedTarget = targetPos
                .add(side.scale(Math.sin(phase) * radius * getOrbitDirection()))
                .add(up.scale(Math.cos(phase * 0.73D) * verticalRadius));

        Vec3 desired = curvedTarget.subtract(position());
        if (desired.lengthSqr() < 1.0E-5D) {
            desired = toTarget;
        }

        Vec3 tangent = side.scale(Math.cos(phase) * getOrbitDirection())
                .add(up.scale(-Math.sin(phase * 0.73D) * 0.73D));
        if (tangent.lengthSqr() > 1.0E-5D) {
            double tangentBlend = TANGENT_STRENGTH * Math.min(1.0D, Math.max(0.0D, (distance - 1.35D) / 3.5D));
            desired = desired.normalize().scale(1.0D - tangentBlend)
                    .add(tangent.normalize().scale(tangentBlend));
        }
        return desired.normalize().scale(SPEED);
    }

    private Vec3 steerSmoothly(Vec3 currentVelocity, Vec3 desiredVelocity, double distance) {
        if (desiredVelocity.lengthSqr() < 1.0E-5D) {
            return currentVelocity;
        }
        if (currentVelocity.lengthSqr() < 1.0E-5D) {
            return desiredVelocity.normalize().scale(SPEED);
        }

        Vec3 currentDirection = currentVelocity.normalize();
        Vec3 desiredDirection = desiredVelocity.normalize();
        double turnStrength = distance < 6.0D ? CLOSE_TURN_STRENGTH : TURN_STRENGTH;
        Vec3 blended = currentDirection.scale(1.0D - turnStrength).add(desiredDirection.scale(turnStrength));
        if (blended.lengthSqr() < 1.0E-5D) {
            blended = desiredDirection;
        }
        return blended.normalize().scale(SPEED);
    }

    private boolean willHitTarget(LivingEntity target, Vec3 nextPosition, double hitDistance) {
        Vec3 start = position();
        Vec3 targetPos = target.position().add(0.0D, target.getBbHeight() * 0.58D, 0.0D);
        if (targetPos.distanceToSqr(nextPosition) <= hitDistance * hitDistance) {
            return true;
        }
        return target.getBoundingBox().inflate(0.95D).clip(start, nextPosition).isPresent();
    }

    private void hitTargetOnce(LivingEntity target) {
        hitTargetOnce(target, true);
    }

    private void hitTargetOnce(LivingEntity target, boolean allowBranch) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        hitTargets.add(target.getUUID());
        float damage = getHitDamage();
        if (damage <= 0.0F) {
            return;
        }
        if (allowBranch) {
            serverLevel.broadcastEntityEvent(this, BURST_VFX_EVENT);
        }
        Player owner = getOwner(serverLevel).orElse(null);
        if (owner == null) {
            target.hurt(damageSources().magic(), damage);
            openPlayerAttackWindow(target);
            return;
        }
        target.getPersistentData().putBoolean("RamessesUraeusBurst", true);
        try {
            target.hurt(owner.damageSources().magic(), damage);
            openPlayerAttackWindow(target);
        } finally {
            target.getPersistentData().remove("RamessesUraeusBurst");
        }
        if (allowBranch) {
            if (isRainEmpowered()) {
                tryRainBurst(serverLevel, target);
                return;
            }
            tryBranch(serverLevel, target);
        }
    }

    private void tryRainBurst(ServerLevel serverLevel, LivingEntity target) {
        if (serverLevel.isThundering() && random.nextFloat() < STORM_STAR_CHANCE) {
            Player owner = getOwner(serverLevel).orElse(null);
            if (owner instanceof ServerPlayer serverPlayer) {
                int cooldownTicks = ModItems.getUraeusStormPassiveCooldownTicks(serverPlayer);
                if (cooldownTicks <= 0 && !UraeusStormStarEntity.hasActiveFor(serverLevel, serverPlayer, target.position())) {
                    ModItems.setUraeusStormPassiveCooldown(serverPlayer);
                    UraeusStormStarEntity.spawn(serverLevel, serverPlayer, target);
                    return;
                }
            }
        }

        if (random.nextFloat() >= RAIN_BURST_CHANCE) {
            return;
        }
        Vec3 origin = target.position().add(0.0D, target.getBbHeight() * 0.65D, 0.0D);
        UraeusRainShardEntity.spawnBurst(serverLevel, ownerUuid, origin, serverLevel.isThundering());
    }

    private void tryBranch(ServerLevel serverLevel, LivingEntity hitTarget) {
        if (branchDepth >= MAX_BRANCH_DEPTH || branchCooldown > 0 || random.nextFloat() >= BRANCH_CHANCE) {
            return;
        }

        List<LivingEntity> candidates = new ArrayList<>(getChainCandidates());
        candidates.removeIf(entity -> entity.getUUID().equals(hitTarget.getUUID()));
        if (candidates.isEmpty()) {
            return;
        }
        branchCooldown = 20;

        for (int i = 0; i < BRANCH_CHILDREN && !candidates.isEmpty(); i++) {
            LivingEntity branchTarget = candidates.remove(random.nextInt(candidates.size()));
            spawnBranch(serverLevel, branchTarget, i);
        }
    }

    private void spawnBranch(ServerLevel serverLevel, LivingEntity target, int branchIndex) {
        UraeusFlareEntity branch = new UraeusFlareEntity(ModEntities.URAEUS_FLARE.get(), serverLevel);
        branch.setPos(getX(), getY(), getZ());
        Vec3 toTarget = target.position().add(0.0D, target.getBbHeight() * 0.58D, 0.0D).subtract(position());
        Vec3 direction = toTarget.lengthSqr() < 1.0E-5D ? getDeltaMovement() : toTarget.normalize();
        Vec3 side = direction.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (side.lengthSqr() < 1.0E-5D) {
            side = direction.cross(new Vec3(1.0D, 0.0D, 0.0D));
        }
        double splitSign = branchIndex % 2 == 0 ? 1.0D : -1.0D;
        Vec3 splitVelocity = direction.scale(0.18D).add(side.normalize().scale(0.14D * splitSign)).add(0.0D, 0.05D, 0.0D);
        branch.setDeltaMovement(splitVelocity);
        branch.ownerUuid = ownerUuid;
        branch.searchCenter = searchCenter;
        branch.openingRoundApplied = true;
        branch.branchDepth = branchDepth + 1;
        branch.hitTargets.addAll(hitTargets);
        branch.entityData.set(TARGET_ID, target.getId());
        branch.entityData.set(DAMAGE, BRANCH_DAMAGE);
        branch.entityData.set(RAIN_EMPOWERED, isRainEmpowered());
        serverLevel.addFreshEntity(branch);
    }

    private float getHitDamage() {
        if (branchDepth <= 0) {
            return getDamage();
        }
        return random.nextFloat() < BRANCH_CRIT_CHANCE ? BRANCH_CRIT_DAMAGE : BRANCH_DAMAGE;
    }

    private static void openPlayerAttackWindow(LivingEntity target) {
        target.invulnerableTime = 0;
    }

    private double getOrbitDirection() {
        return branchDepth % 2 == 0 ? 1.0D : -1.0D;
    }

    private boolean selectNextTarget() {
        if (level().isClientSide) {
            return false;
        }
        List<LivingEntity> candidates = getChainCandidates();
        if (candidates.isEmpty()) {
            return false;
        }

        LivingEntity next = candidates.stream()
                .filter(entity -> !hitTargets.contains(entity.getUUID()))
                .min((a, b) -> Double.compare(a.distanceToSqr(position()), b.distanceToSqr(position())))
                .orElse(null);
        if (next == null) {
            if (candidates.size() <= 1) {
                return false;
            }
            hitTargets.clear();
            next = candidates.stream()
                    .min((a, b) -> Double.compare(a.distanceToSqr(position()), b.distanceToSqr(position())))
                    .orElse(null);
        }
        if (next == null) {
            return false;
        }
        entityData.set(TARGET_ID, next.getId());
        return true;
    }

    private void applyOpeningRound() {
        List<LivingEntity> candidates = getChainCandidates();
        for (LivingEntity candidate : candidates) {
            if (candidate.isAlive()) {
                hitTargetOnce(candidate, false);
            }
        }
    }

    private List<LivingEntity> getChainCandidates() {
        AABB area = new AABB(
                searchCenter.x - SEARCH_RADIUS,
                searchCenter.y - SEARCH_RADIUS,
                searchCenter.z - SEARCH_RADIUS,
                searchCenter.x + SEARCH_RADIUS,
                searchCenter.y + SEARCH_RADIUS,
                searchCenter.z + SEARCH_RADIUS);
        return level().getEntitiesOfClass(
                LivingEntity.class,
                area,
                entity -> isAliveChainTarget(entity)
                        && entity.distanceToSqr(searchCenter) <= SEARCH_RADIUS * SEARCH_RADIUS);
    }

    private boolean isAliveChainTarget(LivingEntity entity) {
        if (!entity.isAlive() || entity instanceof Player || entity.isRemoved()) {
            return false;
        }
        return !entity.getUUID().equals(ownerUuid);
    }

    private Optional<Player> getOwner(ServerLevel level) {
        if (ownerUuid == null) {
            return Optional.empty();
        }
        Entity entity = level.getEntity(ownerUuid);
        return entity instanceof Player player ? Optional.of(player) : Optional.empty();
    }

    private LivingEntity getTarget() {
        Entity entity = level().getEntity(this.entityData.get(TARGET_ID));
        return entity instanceof LivingEntity living ? living : null;
    }

    public float getDamage() {
        return this.entityData.get(DAMAGE);
    }

    public boolean isRainEmpowered() {
        return this.entityData.get(RAIN_EMPOWERED);
    }

    public boolean isBranchFlare() {
        return branchDepth > 0 || (getDamage() > 0.0F && getDamage() < CHAIN_DAMAGE);
    }

    @Override
    public void handleEntityEvent(byte eventId) {
        if (eventId == BURST_VFX_EVENT) {
            UraeusVfxState.trigger(position(), isRainEmpowered(), level().getGameTime());
            return;
        }
        super.handleEntityEvent(eventId);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    public Vec3 getTrailPosition(int index, float partialTick) {
        if (index <= 0) {
            return getPosition(partialTick);
        }
        int listIndex = index - 1;
        if (listIndex >= trailPositions.size()) {
            return trailPositions.isEmpty() ? getPosition(partialTick) : trailPositions.get(trailPositions.size() - 1);
        }
        return trailPositions.get(listIndex);
    }

    public int getTrailLength() {
        return trailPositions.size() + 1;
    }

    private void recordTrailPosition() {
        trailPositions.add(0, position());
        while (trailPositions.size() > 12) {
            trailPositions.remove(trailPositions.size() - 1);
        }
    }

    private void updateRotationFromVelocity(Vec3 velocity) {
        if (velocity.lengthSqr() < 1.0E-5D) {
            return;
        }
        setYRot((float) (Math.atan2(velocity.x, velocity.z) * (180.0D / Math.PI)));
        setXRot((float) (Math.atan2(velocity.y, velocity.horizontalDistance()) * (180.0D / Math.PI)));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID(OWNER_KEY)) {
            ownerUuid = tag.getUUID(OWNER_KEY);
        }
        tickCount = tag.getInt(AGE_KEY);
        entityData.set(DAMAGE, tag.getFloat(DAMAGE_KEY));
        entityData.set(RAIN_EMPOWERED, tag.getBoolean(RAIN_KEY));
        searchCenter = new Vec3(tag.getDouble(SEARCH_X_KEY), tag.getDouble(SEARCH_Y_KEY), tag.getDouble(SEARCH_Z_KEY));
        openingRoundApplied = tag.getBoolean(OPENING_ROUND_KEY);
        branchDepth = tag.getInt(BRANCH_DEPTH_KEY);
        branchCooldown = tag.getInt(BRANCH_COOLDOWN_KEY);
        hitTargets.clear();
        ListTag hits = tag.getList(HIT_TARGETS_KEY, Tag.TAG_INT_ARRAY);
        for (int i = 0; i < hits.size(); i++) {
            hitTargets.add(NbtUtils.loadUUID(hits.get(i)));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUuid != null) {
            tag.putUUID(OWNER_KEY, ownerUuid);
        }
        tag.putInt(AGE_KEY, tickCount);
        tag.putFloat(DAMAGE_KEY, getDamage());
        tag.putBoolean(RAIN_KEY, isRainEmpowered());
        tag.putDouble(SEARCH_X_KEY, searchCenter.x);
        tag.putDouble(SEARCH_Y_KEY, searchCenter.y);
        tag.putDouble(SEARCH_Z_KEY, searchCenter.z);
        tag.putBoolean(OPENING_ROUND_KEY, openingRoundApplied);
        tag.putInt(BRANCH_DEPTH_KEY, branchDepth);
        tag.putInt(BRANCH_COOLDOWN_KEY, branchCooldown);
        ListTag hits = new ListTag();
        for (UUID hitTarget : hitTargets) {
            hits.add(NbtUtils.createUUID(hitTarget));
        }
        tag.put(HIT_TARGETS_KEY, hits);
    }
}

