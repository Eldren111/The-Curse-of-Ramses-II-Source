package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;

public class SatietyCrystalEntity extends Entity {
    private static final String OWNER_KEY = "Owner";
    private static final String BOUND_X_KEY = "BoundX";
    private static final String BOUND_Y_KEY = "BoundY";
    private static final String BOUND_Z_KEY = "BoundZ";

    private UUID ownerUuid;
    private Vec3 boundPosition;
    public final float hoverStart = (float) (Math.random() * Math.PI * 2.0D);

    public SatietyCrystalEntity(EntityType<? extends SatietyCrystalEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public SatietyCrystalEntity(Level level, Vec3 position, UUID ownerUuid) {
        this(ModEntities.SATIETY_CRYSTAL.get(), level);
        this.ownerUuid = ownerUuid;
        this.boundPosition = position;
        this.setPos(position.x, position.y, position.z);
        this.setYRot(this.random.nextFloat() * 360.0F);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);
        this.setDeltaMovement(Vec3.ZERO);

        if (!level().isClientSide) {
            if (boundPosition == null) {
                boundPosition = position();
            }
            if (position().distanceToSqr(boundPosition) > 1.0E-4D) {
                teleportTo(boundPosition.x, boundPosition.y, boundPosition.z);
            }
            return;
        }

        if (tickCount % 2 == 0) {
            level().addParticle(
                    ParticleTypes.END_ROD,
                    getX() + (random.nextDouble() - 0.5D) * 0.28D,
                    getY() + 0.28D + random.nextDouble() * 0.18D,
                    getZ() + (random.nextDouble() - 0.5D) * 0.28D,
                    0.0D,
                    0.012D,
                    0.0D);
        }
    }

    @Override
    public void move(MoverType type, Vec3 movement) {
        if (level().isClientSide) {
            super.move(type, movement);
        }
    }

    @Override
    public void playerTouch(Player player) {
        if (level().isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (!player.getUUID().equals(ownerUuid)) {
            return;
        }
        if (ModEvents.tryRecoverSatietyCrystalFromEntity(serverPlayer)) {
            discard();
        }
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    @Override
    public boolean isCurrentlyGlowing() {
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096.0D;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID(OWNER_KEY)) {
            ownerUuid = tag.getUUID(OWNER_KEY);
        }
        if (tag.contains(BOUND_X_KEY) && tag.contains(BOUND_Y_KEY) && tag.contains(BOUND_Z_KEY)) {
            boundPosition = new Vec3(tag.getDouble(BOUND_X_KEY), tag.getDouble(BOUND_Y_KEY), tag.getDouble(BOUND_Z_KEY));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUuid != null) {
            tag.putUUID(OWNER_KEY, ownerUuid);
        }
        Vec3 position = boundPosition == null ? position() : boundPosition;
        tag.putDouble(BOUND_X_KEY, position.x);
        tag.putDouble(BOUND_Y_KEY, position.y);
        tag.putDouble(BOUND_Z_KEY, position.z);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }
}
