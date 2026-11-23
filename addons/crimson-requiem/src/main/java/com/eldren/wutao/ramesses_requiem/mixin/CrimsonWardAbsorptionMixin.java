package com.eldren.wutao.ramesses_requiem.mixin;

import com.eldren.wutao.ramesses_requiem.CrimsonWardEffect;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class CrimsonWardAbsorptionMixin {
    @Inject(method = "setAbsorptionAmount", at = @At("HEAD"))
    private void crimsonRequiem$trackAbsorption(float amount, CallbackInfo ci) {
        CrimsonWardEffect.absorptionChanged((Player)(Object)this, amount);
    }
}
