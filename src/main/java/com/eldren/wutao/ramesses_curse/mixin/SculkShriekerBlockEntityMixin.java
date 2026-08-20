package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.SculkShriekerBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SculkShriekerBlockEntity.class)
public abstract class SculkShriekerBlockEntityMixin {
    @Inject(method = "tryShriek", at = @At("HEAD"), cancellable = true)
    private void ramessesCurse$preventShriekForAmuletWearer(
            ServerLevel level,
            @Nullable ServerPlayer player,
            CallbackInfo callbackInfo) {
        if (player != null && ModItems.HushedDepthsAmulet.isEquipped(player)) {
            callbackInfo.cancel();
        }
    }
}
