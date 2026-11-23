package com.eldren.wutao.ramesses_requiem;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/** Wither-like damage that retains this weapon's provenance after the skull despawns. */
public final class CrimsonDecayEffect extends MobEffect {
    public static final String CASTER = "CrimsonDecayCaster";
    public static final ResourceKey<DamageType> DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE,
            new ResourceLocation(RequiemAddon.LEGACY_NAMESPACE, "crimson_decay"));
    public CrimsonDecayEffect() { super(MobEffectCategory.HARMFUL, 0x662244); }
    @Override public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % Math.max(1, 40 >> Math.min(amplifier, 5)) == 0;
    }
    @Override public void applyEffectTick(LivingEntity target, int amplifier) {
        if (!(target.level() instanceof ServerLevel level)) return;
        var data = target.getPersistentData();
        var owner = data.hasUUID(CASTER) ? level.getEntity(data.getUUID(CASTER)) : null;
        if (owner == target) return;
        target.hurt(new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(DAMAGE), owner), 1F);
    }
}
