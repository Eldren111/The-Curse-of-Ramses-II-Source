package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.network.NetworkHooks;

public class DragonBreathArrowEntity extends AbstractArrow {

    public DragonBreathArrowEntity(EntityType<? extends DragonBreathArrowEntity> type, Level level) {
        super(type, level);
        pickup = Pickup.DISALLOWED;
    }

    public DragonBreathArrowEntity(Level level, LivingEntity owner) {
        super(ModEntities.DRAGON_BREATH_ARROW.get(), owner, level);
        pickup = Pickup.DISALLOWED;
    }

    @Override
    public void tick() {
        double previousX = getX();
        double previousY = getY();
        double previousZ = getZ();
        super.tick();
        if (!level().isClientSide || isRemoved()) {
            return;
        }

        for (int step = 0; step < 3; step++) {
            double progress = (step + 1.0D) / 3.0D;
            level().addParticle(
                    ParticleTypes.DRAGON_BREATH,
                    previousX + (getX() - previousX) * progress,
                    previousY + (getY() - previousY) * progress,
                    previousZ + (getZ() - previousZ) * progress,
                    0.0D,
                    0.0D,
                    0.0D);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        discard();
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
