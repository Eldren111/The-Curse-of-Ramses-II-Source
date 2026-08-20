package com.eldren.wutao.ramesses_curse;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FissionHeartDropEntity extends ItemEntity {
    private static final String HOVER_Y_KEY = "HoverY";
    private static final double RISE_HEIGHT = 1.0D;
    private static final double RISE_SPEED = 0.055D;

    private double hoverY = Double.NaN;

    public FissionHeartDropEntity(EntityType<? extends FissionHeartDropEntity> type, Level level) {
        super(type, level);
        initializeFloatingState();
    }

    public FissionHeartDropEntity(Level level, double x, double y, double z, ItemStack stack) {
        this(ModEntities.FISSION_HEART_DROP.get(), level);
        setPos(x, y, z);
        setItem(stack);
        hoverY = y + RISE_HEIGHT;
    }

    private void initializeFloatingState() {
        setNoGravity(true);
        setGlowingTag(true);
        setUnlimitedLifetime();
    }

    @Override
    public void tick() {
        setNoGravity(true);
        if (Double.isNaN(hoverY)) {
            hoverY = getY() + RISE_HEIGHT;
        }
        double rise = Math.min(RISE_SPEED, Math.max(0.0D, hoverY - getY()));
        setDeltaMovement(0.0D, rise, 0.0D);
        super.tick();
        if (getY() >= hoverY - 0.01D) {
            setPos(getX(), hoverY, getZ());
            setDeltaMovement(Vec3.ZERO);
        } else {
            setDeltaMovement(0.0D, Math.min(RISE_SPEED, hoverY - getY()), 0.0D);
        }
        setNoGravity(true);
        setGlowingTag(true);
        setUnlimitedLifetime();
    }

    @Override
    public boolean isCurrentlyGlowing() {
        return true;
    }

    @Override
    public int getTeamColor() {
        return 0xA855F7;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (!Double.isNaN(hoverY)) {
            tag.putDouble(HOVER_Y_KEY, hoverY);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        hoverY = tag.contains(HOVER_Y_KEY) ? tag.getDouble(HOVER_Y_KEY) : getY() + RISE_HEIGHT;
        initializeFloatingState();
    }
}

