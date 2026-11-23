package com.eldren.wutao.ramesses_requiem.mixin;

import com.eldren.wutao.ramesses_requiem.boss.WusheControl;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class WusheMovementMixin {
    @Shadow public ServerPlayer player;
    @Inject(method = "handleMovePlayer", at = @At("HEAD"), cancellable = true)
    private void wushe$freezeMovement(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        // Do not access world/entity state on Netty; vanilla re-enters this handler on the server thread.
        if (player.server.isSameThread() && WusheControl.isFrozen(player)) ci.cancel();
    }
}
