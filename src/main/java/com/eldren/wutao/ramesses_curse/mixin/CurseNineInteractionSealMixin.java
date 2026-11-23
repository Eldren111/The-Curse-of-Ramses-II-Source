package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.CurseNineSeal;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class CurseNineInteractionSealMixin {
    @Shadow public ServerPlayer player;

    // Run after the packet has moved to the server thread, before any inventory mutation.
    @Inject(method = {"handleContainerClick", "handleContainerButtonClick", "handlePlaceRecipe",
            "handlePickItem", "handleSetCarriedItem", "handleSetCreativeModeSlot", "handleInteract"},
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V",
                    shift = At.Shift.AFTER), cancellable = true)
    private void ramessesCurse$blockSealedInventory(CallbackInfo callback) {
        if (CurseNineSeal.isSealed(player)) {
            player.containerMenu.sendAllDataToRemote();
            player.connection.send(new ClientboundSetCarriedItemPacket(player.getInventory().selected));
            callback.cancel();
        }
    }

    @Inject(method = "handlePlayerAction", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V",
            shift = At.Shift.AFTER), cancellable = true)
    private void ramessesCurse$blockSwapAndDrop(ServerboundPlayerActionPacket packet, CallbackInfo callback) {
        if (CurseNineSeal.isSealed(player)) {
            switch (packet.getAction()) {
                case SWAP_ITEM_WITH_OFFHAND, DROP_ITEM, DROP_ALL_ITEMS -> {
                    player.inventoryMenu.sendAllDataToRemote();
                    callback.cancel();
                }
                default -> { /* Mining prediction/acknowledgement stays in vanilla; Forge cancels the action. */ }
            }
        }
    }
}
