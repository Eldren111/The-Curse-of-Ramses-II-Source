package com.eldren.wutao.ramesses_requiem.mixin;

import com.eldren.wutao.ramesses_requiem.CrimsonRequiemClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Run before Better Combat's HEAD hook, which bypasses Forge's vanilla input event. */
@Mixin(value = Minecraft.class, priority = 1100)
public abstract class CrimsonRequiemInputMixin {
    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void crimsonRequiem$offhandUse(CallbackInfo ci) {
        if (CrimsonRequiemClient.offhandUse()) ci.cancel();
    }
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void crimsonRequiem$skill(CallbackInfoReturnable<Boolean> ci) {
        if (CrimsonRequiemClient.shiftAttack()) ci.setReturnValue(false);
    }
    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void crimsonRequiem$heldSkill(boolean attack, CallbackInfo ci) {
        if (attack && CrimsonRequiemClient.shiftAttack()) ci.cancel();
    }
}
