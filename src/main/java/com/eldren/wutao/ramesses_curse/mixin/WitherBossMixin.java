package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.integration.goety.GoetyObsidianMonolithCompat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitherBoss.class)
public abstract class WitherBossMixin {
    @Inject(method = "setAlternativeTarget", at = @At("HEAD"), cancellable = true)
    private void ramessesCurse$rejectOwnedMonolithTarget(int head, int targetId, CallbackInfo callback) {
        if (targetId <= 0) {
            return;
        }
        WitherBoss wither = (WitherBoss) (Object) this;
        Entity target = wither.level().getEntity(targetId);
        if (target instanceof LivingEntity livingTarget
                && GoetyObsidianMonolithCompat.isOwnedMonolith(livingTarget, wither)) {
            callback.cancel();
        }
    }
}
