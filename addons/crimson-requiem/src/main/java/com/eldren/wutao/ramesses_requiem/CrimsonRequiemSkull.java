package com.eldren.wutao.ramesses_requiem;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.level.Level;

public final class CrimsonRequiemSkull extends WitherSkull {
    public CrimsonRequiemSkull(EntityType<? extends WitherSkull> type, Level level) { super(type, level); }
    @Override protected void onHitEntity(net.minecraft.world.phys.EntityHitResult hit) {
        if (level().isClientSide || CrimsonRequiemEvents.isCaster(this, hit.getEntity())) return;
        Entity target = hit.getEntity();
        Entity owner = getOwner();
        if (!target.hurt(damageSources().witherSkull(this, owner), 8F)) return;
        if (owner instanceof net.minecraft.world.entity.LivingEntity livingOwner && !target.isAlive()) livingOwner.heal(5F);
        int seconds = level().getDifficulty() == net.minecraft.world.Difficulty.HARD ? 40
                : level().getDifficulty() == net.minecraft.world.Difficulty.NORMAL ? 10 : 0;
        if (seconds > 0 && target instanceof net.minecraft.world.entity.LivingEntity living && living.isAlive()) {
            if (owner != null) living.getPersistentData().putUUID(CrimsonDecayEffect.CASTER, owner.getUUID());
            living.addEffect(new net.minecraft.world.effect.MobEffectInstance(RequiemEffects.CRIMSON_DECAY.get(), seconds * 20, 1), owner);
        }
    }
    @Override protected boolean canHitEntity(Entity entity) {
        return !CrimsonRequiemEvents.isCaster(this, entity) && super.canHitEntity(entity);
    }
    @Override public void tick() { super.tick(); if (!level().isClientSide && tickCount > 200) discard(); }
}
