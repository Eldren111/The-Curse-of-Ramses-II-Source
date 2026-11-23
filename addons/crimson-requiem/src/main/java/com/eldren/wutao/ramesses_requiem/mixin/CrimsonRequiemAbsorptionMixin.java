package com.eldren.wutao.ramesses_requiem.mixin;

import com.eldren.wutao.ramesses_requiem.CrimsonRequiemDamage;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class CrimsonRequiemAbsorptionMixin {
    @Redirect(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getAbsorptionAmount()F"))
    private float crimsonRequiem$ignoreAbsorption(LivingEntity target, DamageSource source, float amount) {
        return (CrimsonRequiemDamage.bypassesAffixes(source) || com.eldren.wutao.ramesses_requiem.boss.WusheDamage.trueDamage(source)) ? 0F : target.getAbsorptionAmount();
    }
    @Redirect(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setAbsorptionAmount(F)V"))
    private void crimsonRequiem$preserveAbsorption(LivingEntity target, float absorption, DamageSource source, float amount) {
        if (!(CrimsonRequiemDamage.bypassesAffixes(source) || com.eldren.wutao.ramesses_requiem.boss.WusheDamage.trueDamage(source))) target.setAbsorptionAmount(absorption);
    }
}
