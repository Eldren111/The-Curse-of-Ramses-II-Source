package com.eldren.wutao.ramesses_curse;

import com.eldren.wutao.ramesses_curse.integration.goety.GoetyObsidianMonolithCompat;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;

public final class RamessesEndCrystalEntity extends EndCrystal {
    private static final String TOWER_GUARDIAN_INITIALIZED = "RamessesTowerGuardianInitialized";
    private static final EntityDataAccessor<Boolean> CRACKED =
            SynchedEntityData.defineId(RamessesEndCrystalEntity.class, EntityDataSerializers.BOOLEAN);

    public RamessesEndCrystalEntity(EntityType<? extends EndCrystal> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CRACKED, false);
    }

    public boolean isCracked() {
        return this.entityData.get(CRACKED);
    }

    public boolean isTowerGuardianInitialized() {
        return this.getPersistentData().getBoolean(TOWER_GUARDIAN_INITIALIZED);
    }

    public void markTowerGuardianInitialized() {
        this.getPersistentData().putBoolean(TOWER_GUARDIAN_INITIALIZED, true);
    }

    public void restoreForCombatStage() {
        this.entityData.set(CRACKED, false);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source) || source.getEntity() instanceof EnderDragon) {
            return false;
        }
        if (!this.level().isClientSide
                && this.level() instanceof ServerLevel serverLevel
                && GoetyObsidianMonolithCompat.protectsIslandCrystal(serverLevel, this)) {
            this.level().playSound(null, this.blockPosition(), SoundEvents.SHIELD_BLOCK,
                    SoundSource.HOSTILE, 1.4F, 0.65F);
            serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT,
                    this.getX(), this.getY() + 0.8D, this.getZ(), 18, 0.55D, 0.7D, 0.55D, 0.05D);
            return false;
        }
        if (!this.isCracked()) {
            if (!this.level().isClientSide) {
                this.entityData.set(CRACKED, true);
                this.level().playSound(null, this.blockPosition(), SoundEvents.AMETHYST_BLOCK_BREAK,
                        SoundSource.BLOCKS, 1.8F, 0.55F);
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            this.getX(), this.getY() + 0.8D, this.getZ(), 28, 0.65D, 0.8D, 0.65D, 0.08D);
                    serverLevel.sendParticles(ParticleTypes.END_ROD,
                            this.getX(), this.getY() + 0.8D, this.getZ(), 12, 0.4D, 0.55D, 0.4D, 0.03D);
                }
            }
            return true;
        }
        boolean destroyed = super.hurt(source, amount);
        if (destroyed && this.level() instanceof ServerLevel serverLevel) {
            EndCrystalFirestormManager.trigger(serverLevel, source.getEntity());
            notifyIslandDragon(serverLevel, source);
            sendBreakVfx(serverLevel);
        }
        return destroyed;
    }

    private void notifyIslandDragon(ServerLevel level, DamageSource source) {
        for (DuatSunEaterEntity dragon : level.getEntitiesOfClass(
                DuatSunEaterEntity.class, this.getBoundingBox().inflate(256.0D), DuatSunEaterEntity::isAlive)) {
            dragon.onCrystalDestroyed(this, this.blockPosition(), source);
        }
    }

    private void sendBreakVfx(ServerLevel level) {
        RamessesEndCrystalBreakVfxPacket packet =
                new RamessesEndCrystalBreakVfxPacket(this.getX(), this.getY() + 0.8D, this.getZ());
        for (net.minecraft.server.level.ServerPlayer player : level.players()) {
            if (player.distanceToSqr(this.getX(), this.getY(), this.getZ()) <= 160.0D * 160.0D) {
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Cracked", this.isCracked());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(CRACKED, tag.getBoolean("Cracked"));
    }
}
