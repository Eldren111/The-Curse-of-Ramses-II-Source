package com.eldren.wutao.ramesses_curse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class UraeusRainShardEntity extends Entity {
    private static final EntityDataAccessor<Float> DAMAGE =
            SynchedEntityData.defineId(UraeusRainShardEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> THUNDER =
            SynchedEntityData.defineId(UraeusRainShardEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> SHARD_COUNT =
            SynchedEntityData.defineId(UraeusRainShardEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> SHARD_SEED =
            SynchedEntityData.defineId(UraeusRainShardEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Float> SPREAD_RADIUS =
            SynchedEntityData.defineId(UraeusRainShardEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Long> ACTIVE_MASK =
            SynchedEntityData.defineId(UraeusRainShardEntity.class, EntityDataSerializers.LONG);

    private static final String OWNER_KEY = "Owner";
    private static final String AGE_KEY = "Age";
    private static final String DAMAGE_KEY = "Damage";
    private static final String THUNDER_KEY = "Thunder";
    private static final String COUNT_KEY = "ShardCount";
    private static final String SEED_KEY = "ShardSeed";
    private static final String SPREAD_KEY = "SpreadRadius";
    private static final String ACTIVE_MASK_KEY = "ActiveMask";
    private static final int MAX_AGE = 80;
    private static final int MAX_SHARDS_PER_BATCH = 64;
    private static final int TRAIL_LENGTH = 10;
    private static final double GRAVITY = 0.045D;
    private static final double COLLISION_PADDING = 0.46D;

    private UUID ownerUuid;
    private long initializedSeed = Long.MIN_VALUE;
    private int initializedCount = -1;
    private final List<ShardState> shards = new ArrayList<>();

    public UraeusRainShardEntity(EntityType<? extends UraeusRainShardEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        noCulling = true;
    }

    public static void spawnBurst(ServerLevel level, UUID ownerUuid, Vec3 origin, boolean thunder) {
        spawnBurst(level, ownerUuid, origin, thunder, 1.25D, thunder ? 32 : 24);
    }

    public static void spawnBurst(
            ServerLevel level,
            UUID ownerUuid,
            Vec3 origin,
            boolean thunder,
            double spreadRadius,
            int count) {
        int clampedCount = Mth.clamp(count, 1, MAX_SHARDS_PER_BATCH);
        UraeusRainShardEntity batch =
                new UraeusRainShardEntity(ModEntities.URAEUS_RAIN_SHARD.get(), level);
        batch.setPos(origin.x, origin.y, origin.z);
        batch.ownerUuid = ownerUuid;
        batch.entityData.set(DAMAGE, thunder ? 7.0F : 5.0F);
        batch.entityData.set(THUNDER, thunder);
        batch.entityData.set(SHARD_COUNT, clampedCount);
        batch.entityData.set(SHARD_SEED, level.random.nextLong());
        batch.entityData.set(SPREAD_RADIUS, (float) Math.max(0.75D, spreadRadius));
        batch.entityData.set(ACTIVE_MASK, initialActiveMask(clampedCount));
        batch.ensureShardsInitialized();
        level.addFreshEntity(batch);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DAMAGE, 0.0F);
        builder.define(THUNDER, false);
        builder.define(SHARD_COUNT, 0);
        builder.define(SHARD_SEED, 0L);
        builder.define(SPREAD_RADIUS, 1.25F);
        builder.define(ACTIVE_MASK, 0L);
    }

    @Override
    public void tick() {
        super.tick();
        ensureShardsInitialized();
        if (shards.isEmpty() || tickCount > MAX_AGE) {
            discard();
            return;
        }

        long activeMask = entityData.get(ACTIVE_MASK);
        List<LivingEntity> collisionCandidates = level().isClientSide
                ? List.of()
                : collectCollisionCandidates(activeMask);
        long updatedMask = activeMask;

        for (int index = 0; index < shards.size(); index++) {
            long bit = 1L << index;
            ShardState shard = shards.get(index);
            if ((activeMask & bit) == 0L) {
                shard.active = false;
                continue;
            }

            shard.active = true;
            shard.recordTrail();
            shard.previousPosition = shard.position;
            Vec3 nextVelocity = shard.velocity.add(0.0D, -GRAVITY, 0.0D).scale(0.985D);
            Vec3 nextPosition = shard.position.add(nextVelocity);

            if (hitsSolidBlock(nextPosition)) {
                shard.active = false;
                updatedMask &= ~bit;
                continue;
            }

            if (!level().isClientSide && damageFirstEntityHit(shard.position, nextPosition, collisionCandidates)) {
                shard.active = false;
                updatedMask &= ~bit;
                continue;
            }

            shard.position = nextPosition;
            shard.velocity = nextVelocity;
        }

        if (!level().isClientSide && updatedMask != activeMask) {
            entityData.set(ACTIVE_MASK, updatedMask);
        }
        if (updatedMask == 0L) {
            discard();
        }
    }

    private void ensureShardsInitialized() {
        int count = Mth.clamp(entityData.get(SHARD_COUNT), 0, MAX_SHARDS_PER_BATCH);
        long seed = entityData.get(SHARD_SEED);
        if (count == initializedCount && seed == initializedSeed) {
            return;
        }

        initializedCount = count;
        initializedSeed = seed;
        shards.clear();
        if (count <= 0) {
            return;
        }

        RandomSource random = RandomSource.create(seed);
        double spreadRadius = Math.max(0.75D, entityData.get(SPREAD_RADIUS));
        Vec3 origin = position();
        for (int index = 0; index < count; index++) {
            Vec3 shardPosition = new Vec3(
                    origin.x + (random.nextDouble() - 0.5D) * spreadRadius,
                    origin.y + 0.45D + random.nextDouble() * 0.75D,
                    origin.z + (random.nextDouble() - 0.5D) * spreadRadius);
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double speed = 0.14D + random.nextDouble() * 0.28D
                    + Math.min(0.18D, spreadRadius * 0.018D);
            Vec3 velocity = new Vec3(
                    Math.cos(angle) * speed,
                    0.2D + random.nextDouble() * 0.24D,
                    Math.sin(angle) * speed);
            shards.add(new ShardState(shardPosition, velocity));
        }
    }

    private List<LivingEntity> collectCollisionCandidates(long activeMask) {
        AABB bounds = null;
        for (int index = 0; index < shards.size(); index++) {
            if ((activeMask & (1L << index)) == 0L) {
                continue;
            }
            ShardState shard = shards.get(index);
            Vec3 nextVelocity = shard.velocity.add(0.0D, -GRAVITY, 0.0D).scale(0.985D);
            Vec3 next = shard.position.add(nextVelocity);
            AABB shardBounds = new AABB(shard.position, next).inflate(COLLISION_PADDING);
            bounds = bounds == null ? shardBounds : bounds.minmax(shardBounds);
        }
        if (bounds == null) {
            return List.of();
        }
        return level().getEntitiesOfClass(
                LivingEntity.class,
                bounds,
                entity -> entity.isAlive()
                        && !(entity instanceof Player)
                        && !entity.isRemoved()
                        && (ownerUuid == null || !entity.getUUID().equals(ownerUuid)));
    }

    private boolean damageFirstEntityHit(
            Vec3 currentPosition,
            Vec3 nextPosition,
            List<LivingEntity> candidates) {
        if (getDamage() <= 0.0F) {
            return true;
        }
        for (LivingEntity target : candidates) {
            AABB hitBox = target.getBoundingBox().inflate(COLLISION_PADDING);
            if (!hitBox.contains(nextPosition) && hitBox.clip(currentPosition, nextPosition).isEmpty()) {
                continue;
            }
            if (level() instanceof ServerLevel serverLevel) {
                Optional<Player> owner = getOwner(serverLevel);
                target.hurt(
                        owner.map(player -> player.damageSources().magic()).orElse(damageSources().magic()),
                        getDamage());
                target.invulnerableTime = 0;
            }
            return true;
        }
        return false;
    }

    private boolean hitsSolidBlock(Vec3 position) {
        BlockPos blockPos = BlockPos.containing(position);
        return !level().getBlockState(blockPos).getCollisionShape(level(), blockPos).isEmpty();
    }

    private Optional<Player> getOwner(ServerLevel level) {
        if (ownerUuid == null) {
            return Optional.empty();
        }
        Entity entity = level.getEntity(ownerUuid);
        return entity instanceof Player player ? Optional.of(player) : Optional.empty();
    }

    public int getShardCount() {
        ensureShardsInitialized();
        return shards.size();
    }

    public boolean isShardActive(int shardIndex) {
        ensureShardsInitialized();
        return shardIndex >= 0
                && shardIndex < shards.size()
                && (entityData.get(ACTIVE_MASK) & (1L << shardIndex)) != 0L;
    }

    public Vec3 getShardPosition(int shardIndex, float partialTick) {
        ensureShardsInitialized();
        if (shardIndex < 0 || shardIndex >= shards.size()) {
            return position();
        }
        ShardState shard = shards.get(shardIndex);
        return shard.previousPosition.lerp(shard.position, partialTick);
    }

    public int getShardTrailLength(int shardIndex) {
        ensureShardsInitialized();
        if (shardIndex < 0 || shardIndex >= shards.size()) {
            return 0;
        }
        return shards.get(shardIndex).trailPositions.size() + 1;
    }

    public Vec3 getShardTrailPosition(int shardIndex, int trailIndex, float partialTick) {
        ensureShardsInitialized();
        if (shardIndex < 0 || shardIndex >= shards.size()) {
            return position();
        }
        ShardState shard = shards.get(shardIndex);
        if (trailIndex <= 0) {
            return shard.previousPosition.lerp(shard.position, partialTick);
        }
        int historyIndex = trailIndex - 1;
        if (historyIndex >= shard.trailPositions.size()) {
            return shard.trailPositions.isEmpty()
                    ? shard.position
                    : shard.trailPositions.get(shard.trailPositions.size() - 1);
        }
        return shard.trailPositions.get(historyIndex);
    }

    public float getDamage() {
        return entityData.get(DAMAGE);
    }

    public boolean isThunder() {
        return entityData.get(THUNDER);
    }

    private static long initialActiveMask(int count) {
        return count >= Long.SIZE ? -1L : (1L << count) - 1L;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID(OWNER_KEY)) {
            ownerUuid = tag.getUUID(OWNER_KEY);
        }
        tickCount = tag.getInt(AGE_KEY);
        entityData.set(DAMAGE, tag.getFloat(DAMAGE_KEY));
        entityData.set(THUNDER, tag.getBoolean(THUNDER_KEY));
        entityData.set(SHARD_COUNT, tag.getInt(COUNT_KEY));
        entityData.set(SHARD_SEED, tag.getLong(SEED_KEY));
        entityData.set(SPREAD_RADIUS, tag.getFloat(SPREAD_KEY));
        entityData.set(ACTIVE_MASK, tag.getLong(ACTIVE_MASK_KEY));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUuid != null) {
            tag.putUUID(OWNER_KEY, ownerUuid);
        }
        tag.putInt(AGE_KEY, tickCount);
        tag.putFloat(DAMAGE_KEY, getDamage());
        tag.putBoolean(THUNDER_KEY, isThunder());
        tag.putInt(COUNT_KEY, entityData.get(SHARD_COUNT));
        tag.putLong(SEED_KEY, entityData.get(SHARD_SEED));
        tag.putFloat(SPREAD_KEY, entityData.get(SPREAD_RADIUS));
        tag.putLong(ACTIVE_MASK_KEY, entityData.get(ACTIVE_MASK));
    }

    private static final class ShardState {
        private Vec3 position;
        private Vec3 previousPosition;
        private Vec3 velocity;
        private boolean active = true;
        private final List<Vec3> trailPositions = new ArrayList<>();

        private ShardState(Vec3 position, Vec3 velocity) {
            this.position = position;
            this.previousPosition = position;
            this.velocity = velocity;
        }

        private void recordTrail() {
            trailPositions.add(0, position);
            while (trailPositions.size() > TRAIL_LENGTH) {
                trailPositions.remove(trailPositions.size() - 1);
            }
        }
    }
}

