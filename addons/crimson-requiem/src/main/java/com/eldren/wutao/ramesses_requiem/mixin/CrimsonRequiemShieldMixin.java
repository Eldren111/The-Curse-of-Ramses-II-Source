package com.eldren.wutao.ramesses_requiem.mixin;

import com.eldren.wutao.ramesses_requiem.CrimsonRequiemDamage;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class CrimsonRequiemShieldMixin {
    @Inject(method = "isDamageSourceBlocked", at = @At("HEAD"), cancellable = true)
    private void crimsonRequiem$bypassBlocking(DamageSource source, CallbackInfoReturnable<Boolean> ci) {
        if (CrimsonRequiemDamage.bypassesAffixes(source)) ci.setReturnValue(false);
    }
}
