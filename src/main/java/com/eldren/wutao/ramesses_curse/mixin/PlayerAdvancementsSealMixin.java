package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.KadeshCurioAccess;
import net.minecraft.advancements.Advancement;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancements.class)
public abstract class PlayerAdvancementsSealMixin {
    @Shadow
    private ServerPlayer player;

    @Inject(method = "award", at = @At("HEAD"), cancellable = true)
    private void ramessesCurse$requireKadesh(Advancement advancement, String criterion,
                                               CallbackInfoReturnable<Boolean> callback) {
        if (KadeshCurioAccess.suppressAdvancement(player, advancement)) {
            callback.setReturnValue(false);
        }
    }
}
