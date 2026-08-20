package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.SculkShriekerBlockEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.entity.monster.warden.Warden$VibrationUser")
public abstract class WardenVibrationUserMixin {
    @Inject(method = "canReceiveVibration", at = @At("HEAD"), cancellable = true)
    private void ramessesCurse$ignoreProtectedPlayerVibrations(
            ServerLevel level,
            BlockPos eventPos,
            GameEvent gameEvent,
            GameEvent.Context context,
            CallbackInfoReturnable<Boolean> callbackInfo) {
        ServerPlayer player = SculkShriekerBlockEntity.tryGetPlayer(context.sourceEntity());
        if (player != null && ModItems.HushedDepthsAmulet.isEquipped(player)) {
            callbackInfo.setReturnValue(false);
        }
    }
}
