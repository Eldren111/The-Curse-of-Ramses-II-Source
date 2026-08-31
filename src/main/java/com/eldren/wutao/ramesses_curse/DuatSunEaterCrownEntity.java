package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public final class DuatSunEaterCrownEntity extends ItemEntity {
    private static final String ANCHOR_X_KEY = "AnchorX";
    private static final String ANCHOR_Y_KEY = "AnchorY";
    private static final String ANCHOR_Z_KEY = "AnchorZ";
    private static final String BLAST_X_KEY = "BlastX";
    private static final String BLAST_Y_KEY = "BlastY";
    private static final String BLAST_Z_KEY = "BlastZ";
    private double anchorX;
    private double anchorY;
    private double anchorZ;
    private BlockPos blastCenter = BlockPos.ZERO;
    private boolean initialized;

    public DuatSunEaterCrownEntity(EntityType<? extends DuatSunEaterCrownEntity> type, Level level) {
        super(type, level);
        initializeFloatingState();
    }

    public DuatSunEaterCrownEntity(Level level, double x, double y, double z, BlockPos blastCenter) {
        this(ModEntities.DUAT_SUN_EATER_CROWN.get(), level);
        this.anchorX = x;
        this.anchorY = y;
        this.anchorZ = z;
        this.blastCenter = blastCenter.immutable();
        this.initialized = true;
        this.setPos(x, y, z);
        this.setItem(DuatSunEaterCrownItem.createDragonDropStack());
        this.setPickUpDelay(40);
    }

    private void initializeFloatingState() {
        this.setNoGravity(true);
        this.setGlowingTag(true);
        this.setUnlimitedLifetime();
    }

    @Override
    public void tick() {
        if (!this.initialized) {
            this.anchorX = this.getX();
            this.anchorY = this.getY();
            this.anchorZ = this.getZ();
            this.blastCenter = this.blockPosition();
            this.initialized = true;
        }

        this.setNoGravity(true);
        this.setDeltaMovement(Vec3.ZERO);
        super.tick();
        if (this.isRemoved()) {
            return;
        }

        double bob = Math.sin(this.tickCount * 0.085D) * 0.16D;
        this.setPos(this.anchorX, this.anchorY + bob, this.anchorZ);
        this.setDeltaMovement(Vec3.ZERO);
        initializeFloatingState();
        if (!this.level().isClientSide && this.tickCount % 2 == 0) {
            spawnConvergingParticles((ServerLevel) this.level());
        }
    }

    private void spawnConvergingParticles(ServerLevel level) {
        for (int index = 0; index < 8; index++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double radius = 2.2D + this.random.nextDouble() * 1.8D;
            double particleX = this.getX() + Math.cos(angle) * radius;
            double particleY = this.getY() - 1.25D + this.random.nextDouble() * 2.5D;
            double particleZ = this.getZ() + Math.sin(angle) * radius;
            Vec3 velocity = new Vec3(
                    this.getX() - particleX,
                    this.getY() + 0.15D - particleY,
                    this.getZ() - particleZ).normalize().scale(0.24D + this.random.nextDouble() * 0.08D);
            level.sendParticles(ParticleTypes.PORTAL,
                    particleX, particleY, particleZ,
                    0, velocity.x, velocity.y, velocity.z, 1.0D);
        }
    }

    @Override
    public void playerTouch(Player player) {
        if (this.level().isClientSide) {
            super.playerTouch(player);
            return;
        }

        this.setItem(DuatSunEaterCrownItem.ensureDragonDropStack(this.getItem()));
        int countBeforePickup = this.getItem().getCount();
        super.playerTouch(player);
        if (this.level() instanceof ServerLevel serverLevel
                && player instanceof ServerPlayer serverPlayer
                && (this.isRemoved() || this.getItem().getCount() < countBeforePickup)) {
            DuatSunEaterDeathBlastManager.schedule(serverLevel, this.blastCenter, serverPlayer);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isCurrentlyGlowing() {
        return true;
    }

    @Override
    public int getTeamColor() {
        return 0xFF2028;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble(ANCHOR_X_KEY, this.anchorX);
        tag.putDouble(ANCHOR_Y_KEY, this.anchorY);
        tag.putDouble(ANCHOR_Z_KEY, this.anchorZ);
        tag.putInt(BLAST_X_KEY, this.blastCenter.getX());
        tag.putInt(BLAST_Y_KEY, this.blastCenter.getY());
        tag.putInt(BLAST_Z_KEY, this.blastCenter.getZ());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setItem(DuatSunEaterCrownItem.ensureDragonDropStack(this.getItem()));
        this.anchorX = tag.contains(ANCHOR_X_KEY) ? tag.getDouble(ANCHOR_X_KEY) : this.getX();
        this.anchorY = tag.contains(ANCHOR_Y_KEY) ? tag.getDouble(ANCHOR_Y_KEY) : this.getY();
        this.anchorZ = tag.contains(ANCHOR_Z_KEY) ? tag.getDouble(ANCHOR_Z_KEY) : this.getZ();
        this.blastCenter = tag.contains(BLAST_X_KEY)
                ? new BlockPos(tag.getInt(BLAST_X_KEY), tag.getInt(BLAST_Y_KEY), tag.getInt(BLAST_Z_KEY))
                : this.blockPosition();
        this.initialized = true;
        initializeFloatingState();
    }
}
