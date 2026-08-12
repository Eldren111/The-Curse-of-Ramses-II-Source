package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Warden.class)
public abstract class WardenMixin {
    @Inject(method = "canTargetEntity", at = @At("HEAD"), cancellable = true)
    private void ramessesCurse$rejectProtectedPlayer(
            @Nullable Entity target,
            CallbackInfoReturnable<Boolean> callbackInfo) {
        if (target instanceof Player player && ModItems.HushedDepthsAmulet.isEquipped(player)) {
            callbackInfo.setReturnValue(false);
        }
    }
}
