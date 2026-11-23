package com.eldren.wutao.ramesses_requiem;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

/** Tracks only the unspent absorption granted by this effect, including across saves. */
public final class CrimsonWardEffect extends MobEffect {
    private static final String REMAINING = "CrimsonRequiemWardRemaining";
    public CrimsonWardEffect() { super(MobEffectCategory.BENEFICIAL, 0xBD267B); }

    @Override public void addAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        super.addAttributeModifiers(entity, attributes, amplifier);
        float amount = CrimsonRequiemConfig.WARD_HEALTH.get().floatValue();
        entity.setAbsorptionAmount(entity.getAbsorptionAmount() + amount);
        entity.getPersistentData().putFloat(REMAINING, amount);
    }

    @Override public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        float remaining = entity.getPersistentData().getFloat(REMAINING);
        entity.getPersistentData().remove(REMAINING);
        entity.setAbsorptionAmount(Math.max(0, entity.getAbsorptionAmount() - remaining));
        super.removeAttributeModifiers(entity, attributes, amplifier);
    }

    public static void absorptionChanged(LivingEntity entity, float newAmount) {
        float spent = Math.max(0, entity.getAbsorptionAmount() - Math.max(0, newAmount));
        if (spent > 0 && entity.getPersistentData().contains(REMAINING)) {
            entity.getPersistentData().putFloat(REMAINING, Math.max(0, entity.getPersistentData().getFloat(REMAINING) - spent));
        }
    }
}
