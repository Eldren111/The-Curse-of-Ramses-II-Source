package com.eldren.wutao.ramesses_requiem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** One orbit controller per cast, and one visual/damaging projectile per falling sword. */
public final class CrimsonSwordStorm extends Projectile {
    private static final double STRIKE_SPEED = 2.0;
    private static final EntityDataAccessor<ItemStack> WEAPON = SynchedEntityData.defineId(CrimsonSwordStorm.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> REMAINING = SynchedEntityData.defineId(CrimsonSwordStorm.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MODE = SynchedEntityData.defineId(CrimsonSwordStorm.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(CrimsonSwordStorm.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TARGET_ID = SynchedEntityData.defineId(CrimsonSwordStorm.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> STUCK = SynchedEntityData.defineId(CrimsonSwordStorm.class, EntityDataSerializers.BOOLEAN);
    private static final String ACTIVE = "CrimsonSwordStormActive";
    public static final ResourceKey<net.minecraft.world.damagesource.DamageType> DAMAGE = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(RequiemAddon.LEGACY_NAMESPACE, "crimson_sword_storm"));
    private UUID targetId;
    private Vec3 aim = Vec3.ZERO;
    private Vec3 impactOffset = Vec3.ZERO;
    private int nextVolley = 12;
    private int pendingBlades;
    private int volleySize;
    private int nextBlade;
    private double volleyPhase;
    private UUID volleyTarget;
    private int stuckUntil;
    private Vec3 clientCorrection = Vec3.ZERO;
    private int clientCorrectionTicks;
    private int clientPredictionTicks;
    private final Map<UUID, Integer> targeted = new HashMap<>();

    public CrimsonSwordStorm(EntityType<? extends CrimsonSwordStorm> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public static void start(ServerPlayer owner) {
        ItemStack stack = CrimsonRequiemActions.weapon(owner);
        if (stack.getEnchantmentLevel(ModEnchantments.SWORD_STORM.get()) <= 0) return;
        CompoundTag data = owner.getPersistentData();
        if (data.hasUUID(ACTIVE) && owner.serverLevel().getEntity(data.getUUID(ACTIVE)) instanceof CrimsonSwordStorm active
                && active.isAlive() && active.isController()) return;
        CrimsonSwordStorm storm = new CrimsonSwordStorm(RequiemEntities.CRIMSON_SWORD_STORM.get(), owner.level());
        storm.setOwner(owner);
        storm.entityData.set(OWNER_ID, owner.getId());
        storm.entityData.set(WEAPON, stack.copy());
        CrimsonRequiemFlight.clear(storm.weapon());
        storm.setPos(owner.position());
        if (owner.level().addFreshEntity(storm)) data.putUUID(ACTIVE, storm.getUUID());
    }

    public ItemStack weapon() { return entityData.get(WEAPON); }
    public boolean isController() { return entityData.get(MODE) == 0; }
    public int remaining() { return entityData.get(REMAINING); }
    public boolean isStuck() { return entityData.get(STUCK); }
    public Entity visualOwner() { return level().getEntity(entityData.get(OWNER_ID)); }
    public Entity visualTarget() { return level().getEntity(entityData.get(TARGET_ID)); }

    @Override protected void defineSynchedData() {
        entityData.define(WEAPON, ItemStack.EMPTY);
        entityData.define(REMAINING, 6);
        entityData.define(MODE, 0);
        entityData.define(OWNER_ID, -1);
        entityData.define(TARGET_ID, -1);
        entityData.define(STUCK, false);
    }

    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }

    @Override public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps, boolean teleport) {
        Vec3 destination = new Vec3(x, y, z);
        if (teleport || position().distanceToSqr(destination) > 16 * 16) {
            super.lerpTo(x, y, z, yaw, pitch, steps, teleport);
            clientCorrection = Vec3.ZERO;
            clientCorrectionTicks = 0;
        } else {
            // Correct prediction over two ticks rather than snapping on each server packet.
            clientCorrection = destination.subtract(position());
            clientCorrectionTicks = 2;
        }
        clientPredictionTicks = 0;
        setYRot(yaw);
        setXRot(pitch);
    }

    private void tickClientMotion() {
        if (isController()) {
            Entity owner = visualOwner();
            if (owner != null) setPos(owner.position());
            return;
        }
        // Keep high-frame-rate rendering moving between packets; stop extrapolating on network stalls.
        Vec3 movement = clientPredictionTicks++ < 3 ? getDeltaMovement() : Vec3.ZERO;
        if (clientCorrectionTicks > 0) {
            Vec3 correction = clientCorrection.scale(1D / clientCorrectionTicks);
            movement = movement.add(correction);
            clientCorrection = clientCorrection.subtract(correction);
            clientCorrectionTicks--;
        }
        setPos(position().add(movement));
    }

    @Override public void tick() {
        super.tick();
        if (level().isClientSide) { tickClientMotion(); return; }
        if (!(getOwner() instanceof ServerPlayer owner) || !owner.isAlive() || owner.isSpectator() || owner.level() != level()) {
            discard(); return;
        }
        entityData.set(OWNER_ID, owner.getId());
        if (isController()) tickOrbit(owner); else tickFalling(owner);
    }

    private boolean validTarget(ServerPlayer owner, LivingEntity target) {
        return target != owner && target.isAlive() && !target.isSpectator() && target.isPickable()
                && owner.distanceToSqr(target) <= 400
                && !(target instanceof net.minecraft.world.entity.decoration.ArmorStand)
                && !owner.isAlliedTo(target) && !target.isAlliedTo(owner)
                && !(target instanceof TamableAnimal pet && pet.isOwnedBy(owner))
                && !(target instanceof net.minecraft.world.entity.animal.horse.AbstractHorse horse
                    && owner.getUUID().equals(horse.getOwnerUUID()))
                && (!(target instanceof Player other) || !other.isCreative() && owner.canHarmPlayer(other));
    }

    private void tickOrbit(ServerPlayer owner) {
        setPos(owner.position());
        if (tickCount >= 260) { discard(); return; }
        if (pendingBlades > 0) {
            if (tickCount >= nextBlade) {
                Entity found = ((ServerLevel)level()).getEntity(volleyTarget);
                if (found instanceof LivingEntity living && validTarget(owner, living)) {
                    spawnFalling(owner, living, volleyPhase + Math.PI * 2 * (volleySize - pendingBlades) / volleySize);
                    pendingBlades--;
                    nextBlade = tickCount + 2;
                } else pendingBlades = 0;
            }
            return;
        }
        if (remaining() <= 0) { discard(); return; }
        if (tickCount < nextVolley) return;
        LivingEntity target = level().getEntitiesOfClass(LivingEntity.class, owner.getBoundingBox().inflate(20),
                living -> validTarget(owner, living)).stream()
                .min(java.util.Comparator.<LivingEntity>comparingInt(living -> targeted.getOrDefault(living.getUUID(), -1))
                        .thenComparingDouble(owner::distanceToSqr)).orElse(null);
        if (target == null) return;
        targeted.put(target.getUUID(), tickCount);
        entityData.set(REMAINING, remaining() - 1);
        volleySize = 1 + random.nextInt(15);
        volleyPhase = random.nextDouble() * Math.PI * 2;
        volleyTarget = target.getUUID();
        spawnFalling(owner, target, volleyPhase);
        pendingBlades = volleySize - 1;
        nextBlade = tickCount + 2;
        nextVolley = tickCount + Math.max(25, volleySize * 2 + 6);
    }

    private void spawnFalling(ServerPlayer owner, LivingEntity target, double angle) {
        CrimsonSwordStorm sword = new CrimsonSwordStorm(RequiemEntities.CRIMSON_SWORD_STORM.get(), level());
        sword.setOwner(owner);
        sword.entityData.set(OWNER_ID, owner.getId());
        sword.entityData.set(WEAPON, weapon().copy());
        sword.entityData.set(MODE, 1);
        sword.entityData.set(REMAINING, 0);
        sword.entityData.set(TARGET_ID, target.getId());
        sword.targetId = target.getUUID();
        // Aim at varied points on the body so the blades don't overlap after impact.
        double width = target.getBbWidth() * 0.32;
        sword.impactOffset = new Vec3((random.nextDouble() * 2 - 1) * width,
                target.getBbHeight() * (0.3 + random.nextDouble() * 0.5), (random.nextDouble() * 2 - 1) * width);
        sword.aim = target.position().add(sword.impactOffset);
        // Distribute each volley around the full target, with varied elevation and a readable approach.
        double radius = 6 + random.nextDouble() * 3;
        Vec3 spawn = sword.aim.add(Math.cos(angle) * radius, random.nextDouble() * 5 - 1, Math.sin(angle) * radius);
        sword.setPos(spawn);
        sword.setDeltaMovement(sword.aim.subtract(spawn).normalize().scale(STRIKE_SPEED));
        sword.updateRotation();
        if (level().hasChunkAt(BlockPos.containing(spawn))) level().addFreshEntity(sword);
    }

    private void tickFalling(ServerPlayer owner) {
        if (tickCount > 60) { discard(); return; }
        Entity found = targetId == null ? null : ((ServerLevel)level()).getEntity(targetId);
        if (!(found instanceof LivingEntity target)) { discard(); return; }
        entityData.set(TARGET_ID, target.getId());
        if (isStuck()) {
            discard(); // Remove embedded blades loaded from older saves as well.
            return;
        }
        if (!validTarget(owner, target)) { discard(); return; }
        if (weapon().getEnchantmentLevel(ModEnchantments.SOUL_PURSUIT.get()) > 0) {
            aim = target.position().add(impactOffset);
            setDeltaMovement(aim.subtract(position()).normalize().scale(STRIKE_SPEED));
        }
        Vec3 start = position();
        Vec3 end = start.add(getDeltaMovement());
        if (!level().hasChunkAt(BlockPos.containing(end))) { discard(); return; }
        var box = target.getBoundingBox().inflate(0.15);
        Vec3 contact = box.contains(start) ? start : box.clip(start, end).orElse(null);
        if (contact != null) {
            updateRotation();
            impactOffset = contact.subtract(target.position());
            setPos(contact);
            setDeltaMovement(Vec3.ZERO);
            CrimsonRequiemDamage.hitScalingBlade(owner, this, target);
            owner.setLastHurtMob(target);
            discard();
        } else {
            setPos(end);
            updateRotation();
            if (weapon().getEnchantmentLevel(ModEnchantments.SOUL_PURSUIT.get()) == 0
                    && aim.subtract(end).dot(getDeltaMovement()) < -2) discard();
        }
    }

    @Override protected void updateRotation() {
        Vec3 velocity = getDeltaMovement();
        setYRot((float)Math.toDegrees(Math.atan2(velocity.x, velocity.z)));
        setXRot((float)Math.toDegrees(Math.atan2(velocity.y, velocity.horizontalDistance())));
    }

    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Weapon", weapon().save(new CompoundTag()));
        tag.putInt("Mode", entityData.get(MODE)); tag.putInt("Remaining", remaining());
        tag.putInt("Age", tickCount); tag.putInt("NextVolley", nextVolley);
        tag.putInt("PendingBlades", pendingBlades); tag.putInt("VolleySize", volleySize);
        tag.putInt("NextBlade", nextBlade); tag.putDouble("VolleyPhase", volleyPhase);
        if (volleyTarget != null) tag.putUUID("VolleyTarget", volleyTarget);
        tag.putBoolean("Stuck", isStuck()); tag.putInt("StuckUntil", stuckUntil);
        if (targetId != null) tag.putUUID("Target", targetId);
        tag.putDouble("AimX", aim.x); tag.putDouble("AimY", aim.y); tag.putDouble("AimZ", aim.z);
        tag.putDouble("OffsetX", impactOffset.x); tag.putDouble("OffsetY", impactOffset.y); tag.putDouble("OffsetZ", impactOffset.z);
    }

    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(WEAPON, ItemStack.of(tag.getCompound("Weapon")));
        entityData.set(MODE, tag.getInt("Mode")); entityData.set(REMAINING, tag.getInt("Remaining"));
        tickCount = tag.getInt("Age"); nextVolley = tag.getInt("NextVolley");
        volleyTarget = tag.hasUUID("VolleyTarget") ? tag.getUUID("VolleyTarget") : null;
        volleySize = Math.max(1, Math.min(15, tag.getInt("VolleySize")));
        pendingBlades = volleyTarget == null ? 0 : Math.max(0, Math.min(volleySize, tag.getInt("PendingBlades")));
        nextBlade = tag.getInt("NextBlade"); volleyPhase = tag.getDouble("VolleyPhase");
        entityData.set(STUCK, tag.getBoolean("Stuck")); stuckUntil = tag.getInt("StuckUntil");
        targetId = tag.hasUUID("Target") ? tag.getUUID("Target") : null;
        aim = new Vec3(tag.getDouble("AimX"), tag.getDouble("AimY"), tag.getDouble("AimZ"));
        impactOffset = new Vec3(tag.getDouble("OffsetX"), tag.getDouble("OffsetY"), tag.getDouble("OffsetZ"));
    }
}
