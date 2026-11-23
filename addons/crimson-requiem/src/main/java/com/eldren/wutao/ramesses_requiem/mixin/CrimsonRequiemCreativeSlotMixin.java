package com.eldren.wutao.ramesses_requiem.mixin;

import com.eldren.wutao.ramesses_requiem.CrimsonRequiemFlight;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class CrimsonRequiemCreativeSlotMixin {
    @Shadow public ServerPlayer player;

    @Inject(method = "handleSetCreativeModeSlot", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V",
            shift = At.Shift.AFTER), cancellable = true)
    private void crimsonRequiem$creativeLock(ServerboundSetCreativeModeSlotPacket packet, CallbackInfo ci) {
        int slot = packet.getSlotNum();
        if (CrimsonRequiemFlight.locked(packet.getItem()) || slot >= 0 && slot < player.inventoryMenu.slots.size()
                && CrimsonRequiemFlight.locked(player.inventoryMenu.getSlot(slot).getItem())) {
            player.inventoryMenu.sendAllDataToRemote();
            ci.cancel();
        }
    }
}
