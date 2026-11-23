package com.eldren.wutao.ramesses_requiem.mixin;

import com.eldren.wutao.ramesses_requiem.CrimsonRequiemActions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class CrimsonRequiemOffhandAttackMixin {
    @Inject(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;resetAttackStrengthTicker()V"))
    private void crimsonRequiem$companionStrike(Entity target, CallbackInfo ci) {
        CrimsonRequiemActions.offhandMelee((Player)(Object)this, target);
    }
}
