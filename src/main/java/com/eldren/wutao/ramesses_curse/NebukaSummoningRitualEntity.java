package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class NebukaSummoningRitualEntity extends Entity {
    public static final int DURATION_TICKS = 63;
    private static final int FINAL_PARTICLE_START_TICK = 52;
    private static final double SHAKE_RADIUS = 28.0D;
    private static final DustParticleOptions GOLD_DUST = new DustParticleOptions(
            new Vector3f(1.0F, 0.72F, 0.08F), 1.2F);
    private BlockPos skullPos = BlockPos.ZERO;

    public NebukaSummoningRitualEntity(EntityType<? extends NebukaSummoningRitualEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    public static void spawn(ServerLevel level, BlockPos skullPos) {
        NebukaSummoningRitualEntity ritual = new NebukaSummoningRitualEntity(
                ModEntities.NEBUKA_SUMMONING_RITUAL.get(), level);
        ritual.skullPos = skullPos.immutable();
        ritual.setPos(skullPos.getX() + 0.5D, skullPos.getY() + 1.15D, skullPos.getZ() + 0.5D);
        level.addFreshEntity(ritual);
        level.playSound(
                null,
                skullPos,
                SoundEvents.PORTAL_TRIGGER,
                SoundSource.HOSTILE,
                1.0F,
                0.72F);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level();
        if (tickCount == 1) {
            sendShake(serverLevel, 80, 0.28F);
        }
        if (tickCount >= FINAL_PARTICLE_START_TICK) {
            spawnFinalParticleSphere(serverLevel);
        }
        if (tickCount >= DURATION_TICKS) {
            completeRitual(serverLevel);
        }
    }

    private void spawnFinalParticleSphere(ServerLevel level) {
        int particles = 12 + (tickCount - FINAL_PARTICLE_START_TICK) * 2;
        Vec3 center = position().add(0.0D, 0.45D, 0.0D);
        for (int i = 0; i < particles; i++) {
            Vec3 direction = new Vec3(
                    random.nextGaussian(),
                    random.nextGaussian(),
                    random.nextGaussian());
            if (direction.lengthSqr() < 1.0E-5D) {
                continue;
            }
            direction = direction.normalize();
            double speed = 0.08D + random.nextDouble() * 0.2D;
            level.sendParticles(
                    GOLD_DUST,
                    center.x,
                    center.y,
                    center.z,
                    0,
                    direction.x,
                    direction.y,
                    direction.z,
                    speed);
            if (i % 5 == 0) {
                level.sendParticles(
                        ParticleTypes.END_ROD,
                        center.x,
                        center.y,
                        center.z,
                        0,
                        direction.x,
                        direction.y,
                        direction.z,
                        speed * 0.7D);
            }
        }
    }

    private void completeRitual(ServerLevel level) {
        AABB nearby = new AABB(skullPos).inflate(32.0D, 16.0D, 32.0D);
        boolean alreadySpawned = !level.getEntitiesOfClass(
                NebukaGuardianEntity.class,
                nearby,
                boss -> boss.isAlive() && !boss.isRemoved()).isEmpty();
        if (alreadySpawned) {
            removeSkullAfterSuccessfulSummon(level);
            discard();
            return;
        }

        NebukaGuardianEntity boss = ModEntities.NEBUKA_GUARDIAN.get().create(level);
        if (boss == null) {
            discard();
            return;
        }

        BlockState skullState = level.getBlockState(skullPos);
        int skullRotation = skullState.hasProperty(SkullBlock.ROTATION)
                ? skullState.getValue(SkullBlock.ROTATION)
                : 0;
        float spawnYaw = skullRotation * 22.5F;
        boss.moveTo(
                skullPos.getX() + 0.5D,
                skullPos.getY() + 1.0D,
                skullPos.getZ() + 0.5D,
                spawnYaw,
                0.0F);
        DifficultyInstance difficulty = level.getCurrentDifficultyAt(skullPos);
        boss.finalizeSpawn(level, difficulty, MobSpawnType.EVENT, null);
        boss.setRitualHome(skullPos, spawnYaw);
        if (!level.addFreshEntity(boss)) {
            discard();
            return;
        }

        removeSkullAfterSuccessfulSummon(level);
        sendShake(level, 40, 0.48F);
        level.playSound(
                null,
                skullPos,
                SoundEvents.TOTEM_USE,
                SoundSource.HOSTILE,
                1.3F,
                0.55F);
        discard();
    }

    private void removeSkullAfterSuccessfulSummon(ServerLevel level) {
        if (level.getBlockState(skullPos).is(Blocks.SKELETON_SKULL)) {
            level.removeBlock(skullPos, false);
        }
    }

    private void sendShake(ServerLevel level, int durationTicks, float strength) {
        for (var player : level.players()) {
            double distance = player.distanceTo(this);
            if (distance > SHAKE_RADIUS) {
                continue;
            }
            float distanceFade = (float) Math.max(0.0D, 1.0D - distance / SHAKE_RADIUS);
            ModNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new NebukaRitualShakePacket(durationTicks, strength * distanceFade));
        }
    }

    public float getProgress(float partialTick) {
        return Math.min(1.0F, (tickCount + partialTick) / DURATION_TICKS);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        tickCount = tag.getInt("Age");
        skullPos = BlockPos.of(tag.getLong("SkullPos"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Age", tickCount);
        tag.putLong("SkullPos", skullPos.asLong());
    }
}

