package com.eldren.wutao.ramesses_curse;

import net.minecraft.nbt.CompoundTag;
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

import java.util.List;
import java.util.UUID;

public class UraeusStormStarEntity extends Entity {
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(UraeusStormStarEntity.class, EntityDataSerializers.FLOAT);
    private static final String OWNER_KEY = "Owner";
    private static final String AGE_KEY = "Age";
    private static final String RADIUS_KEY = "Radius";
    private static final String BURSTS_KEY = "Bursts";
    private static final int BURST_COUNT = 5;
    private static final int BURST_INTERVAL = 20 * 3;
    private static final int MAX_AGE = BURST_INTERVAL * (BURST_COUNT - 1) + 80;
    private static final double EFFECT_RADIUS = 16.0D;
    private static final double VISUAL_RADIUS = 8.0D;

    private UUID ownerUuid;
    private int burstsDone;

    public UraeusStormStarEntity(EntityType<? extends UraeusStormStarEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    public static void spawn(ServerLevel level, ServerPlayer owner, LivingEntity target) {
        UraeusStormStarEntity star = new UraeusStormStarEntity(ModEntities.URAEUS_STORM_STAR.get(), level);
        star.setPos(target.getX(), target.getY() + target.getBbHeight() + 2.8D, target.getZ());
        star.ownerUuid = owner.getUUID();
        star.entityData.set(RADIUS, (float) VISUAL_RADIUS);
        level.addFreshEntity(star);
    }

    public static boolean hasActiveFor(ServerLevel level, ServerPlayer owner, Vec3 center) {
        AABB searchArea = new AABB(
                center.x - 128.0D,
                level.getMinBuildHeight(),
                center.z - 128.0D,
                center.x + 128.0D,
                level.getMaxBuildHeight(),
                center.z + 128.0D);
        return !level.getEntitiesOfClass(
                UraeusStormStarEntity.class,
                searchArea,
                star -> !star.isRemoved() && owner.getUUID().equals(star.ownerUuid)).isEmpty();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RADIUS, 2.4F);
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount > MAX_AGE) {
            discard();
            return;
        }

        if (!level().isClientSide && burstsDone < BURST_COUNT && tickCount % BURST_INTERVAL == 1) {
            burstsDone++;
            scatterOverTargets((ServerLevel) level());
        }
    }

    private void scatterOverTargets(ServerLevel level) {
        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                getEffectArea(),
                entity -> entity.isAlive()
                        && !(entity instanceof Player)
                        && !entity.isRemoved()
                        && (ownerUuid == null || !entity.getUUID().equals(ownerUuid))
                        && entity.distanceToSqr(getX(), entity.getY(), getZ()) <= EFFECT_RADIUS * EFFECT_RADIUS);
        for (LivingEntity target : targets) {
            Vec3 origin = new Vec3(target.getX(), Math.max(getY(), target.getY() + target.getBbHeight() + 2.25D), target.getZ());
            double spread = Math.max(1.35D, target.getBbWidth() + 1.1D);
            UraeusRainShardEntity.spawnBurst(level, ownerUuid, origin, true, spread, 10);
        }
    }

    private AABB getEffectArea() {
        return new AABB(
                getX() - EFFECT_RADIUS,
                getY() - EFFECT_RADIUS,
                getZ() - EFFECT_RADIUS,
                getX() + EFFECT_RADIUS,
                getY() + EFFECT_RADIUS,
                getZ() + EFFECT_RADIUS);
    }

    public float getRadius() {
        return entityData.get(RADIUS);
    }

    public float getLifeProgress(float partialTick) {
        return Math.min(1.0F, (tickCount + partialTick) / (float) MAX_AGE);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID(OWNER_KEY)) {
            ownerUuid = tag.getUUID(OWNER_KEY);
        }
        tickCount = tag.getInt(AGE_KEY);
        burstsDone = tag.getInt(BURSTS_KEY);
        entityData.set(RADIUS, tag.getFloat(RADIUS_KEY));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUuid != null) {
            tag.putUUID(OWNER_KEY, ownerUuid);
        }
        tag.putInt(AGE_KEY, tickCount);
        tag.putInt(BURSTS_KEY, burstsDone);
        tag.putFloat(RADIUS_KEY, getRadius());
    }
}

