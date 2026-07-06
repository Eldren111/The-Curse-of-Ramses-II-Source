package com.eldren.wutao.ramesses_curse;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UraeusRainShardEntity extends Entity {
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(UraeusRainShardEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> THUNDER = SynchedEntityData.defineId(UraeusRainShardEntity.class, EntityDataSerializers.BOOLEAN);
    private static final String OWNER_KEY = "Owner";
    private static final String AGE_KEY = "Age";
    private static final String DAMAGE_KEY = "Damage";
    private static final String THUNDER_KEY = "Thunder";
    private static final int MAX_AGE = 80;
    private static final double GRAVITY = 0.045D;
    private static final int TRAIL_LENGTH = 10;

    private UUID ownerUuid;
    private final List<Vec3> trailPositions = new ArrayList<>();

    public UraeusRainShardEntity(EntityType<? extends UraeusRainShardEntity> type, Level level) {
        super(type, level);
    }

    public static void spawnBurst(ServerLevel level, UUID ownerUuid, Vec3 origin, boolean thunder) {
        spawnBurst(level, ownerUuid, origin, thunder, 1.25D, thunder ? 32 : 24);
    }

    public static void spawnBurst(ServerLevel level, UUID ownerUuid, Vec3 origin, boolean thunder, double spreadRadius, int count) {
        spreadRadius = Math.max(0.75D, spreadRadius);
        float damage = thunder ? 7.0F : 5.0F;
        for (int i = 0; i < count; i++) {
            UraeusRainShardEntity shard = new UraeusRainShardEntity(ModEntities.URAEUS_RAIN_SHARD.get(), level);
            shard.setPos(
                    origin.x + (level.random.nextDouble() - 0.5D) * spreadRadius,
                    origin.y + 0.45D + level.random.nextDouble() * 0.75D,
                    origin.z + (level.random.nextDouble() - 0.5D) * spreadRadius);
            double angle = level.random.nextDouble() * Math.PI * 2.0D;
            double speed = 0.14D + level.random.nextDouble() * 0.28D + Math.min(0.18D, spreadRadius * 0.018D);
            shard.setDeltaMovement(
                    Math.cos(angle) * speed,
                    0.2D + level.random.nextDouble() * 0.24D,
                    Math.sin(angle) * speed);
            shard.ownerUuid = ownerUuid;
            shard.entityData.set(DAMAGE, damage);
            shard.entityData.set(THUNDER, thunder);
            level.addFreshEntity(shard);
        }
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DAMAGE, 0.0F);
        entityData.define(THUNDER, false);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {
        super.tick();
        recordTrailPosition();
        if (tickCount > MAX_AGE || onGround()) {
            discard();
            return;
        }

        Vec3 velocity = getDeltaMovement().add(0.0D, -GRAVITY, 0.0D).scale(0.985D);
        setDeltaMovement(velocity);
        move(MoverType.SELF, velocity);

        if (!level().isClientSide) {
            damageFirstEntityHit();
        }
    }

    private void damageFirstEntityHit() {
        AABB area = getBoundingBox().inflate(0.46D);
        List<LivingEntity> hits = level().getEntitiesOfClass(
                LivingEntity.class,
                area,
                entity -> entity.isAlive()
                        && !(entity instanceof Player)
                        && !entity.isRemoved()
                        && (ownerUuid == null || !entity.getUUID().equals(ownerUuid)));
        if (hits.isEmpty()) {
            return;
        }

        LivingEntity target = hits.get(0);
        if (getDamage() <= 0.0F) {
            discard();
            return;
        }
        if (level() instanceof ServerLevel serverLevel) {
            Optional<Player> owner = getOwner(serverLevel);
            target.hurt(owner.map(player -> player.damageSources().magic()).orElse(damageSources().magic()), getDamage());
            openPlayerAttackWindow(target);
        }
        discard();
    }

    private static void openPlayerAttackWindow(LivingEntity target) {
        target.invulnerableTime = 0;
    }

    private Optional<Player> getOwner(ServerLevel level) {
        if (ownerUuid == null) {
            return Optional.empty();
        }
        Entity entity = level.getEntity(ownerUuid);
        return entity instanceof Player player ? Optional.of(player) : Optional.empty();
    }

    public float getDamage() {
        return entityData.get(DAMAGE);
    }

    public boolean isThunder() {
        return entityData.get(THUNDER);
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
        while (trailPositions.size() > TRAIL_LENGTH) {
            trailPositions.remove(trailPositions.size() - 1);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID(OWNER_KEY)) {
            ownerUuid = tag.getUUID(OWNER_KEY);
        }
        tickCount = tag.getInt(AGE_KEY);
        entityData.set(DAMAGE, tag.getFloat(DAMAGE_KEY));
        entityData.set(THUNDER, tag.getBoolean(THUNDER_KEY));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUuid != null) {
            tag.putUUID(OWNER_KEY, ownerUuid);
        }
        tag.putInt(AGE_KEY, tickCount);
        tag.putFloat(DAMAGE_KEY, getDamage());
        tag.putBoolean(THUNDER_KEY, isThunder());
    }
}
