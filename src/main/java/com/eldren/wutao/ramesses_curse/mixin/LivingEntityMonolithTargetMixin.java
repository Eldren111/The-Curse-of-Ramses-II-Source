package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.integration.goety.GoetyObsidianMonolithCompat;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMonolithTargetMixin {
    @Inject(method = "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void ramesses$excludeOwnMonolith(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        if (target != null && GoetyObsidianMonolithCompat.isOwnedMonolith(target, (LivingEntity)(Object)this)) {
            cir.setReturnValue(false);
        }
    }
}

