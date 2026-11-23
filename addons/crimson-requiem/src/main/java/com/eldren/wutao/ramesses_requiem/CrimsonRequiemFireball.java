package com.eldren.wutao.ramesses_requiem;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

public final class CrimsonRequiemFireball extends LargeFireball {
    public CrimsonRequiemFireball(EntityType<? extends LargeFireball> type, Level level) { super(type, level); }
    @Override protected boolean canHitEntity(Entity entity) {
        return !CrimsonRequiemEvents.isCaster(this, entity) && super.canHitEntity(entity);
    }
    @Override protected void onHit(HitResult hit) {
        if (level().isClientSide) return;
        if (hit instanceof EntityHitResult entityHit) onHitEntity(entityHit);
        else if (hit instanceof BlockHitResult blockHit) onHitBlock(blockHit);
        // No lingering terrain fire: it loses ownership and would burn the caster later.
        level().explode(this, getX(), getY(), getZ(), CrimsonRequiemConfig.FIREBALL_POWER.get(), false, Level.ExplosionInteraction.BLOCK);
        discard();
    }
    @Override public void tick() { super.tick(); if (!level().isClientSide && tickCount > 200) discard(); }
}
