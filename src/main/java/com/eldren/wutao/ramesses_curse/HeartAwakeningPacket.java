package com.eldren.wutao.ramesses_curse;

import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public class HeartAwakeningPacket {

    public static void encode(HeartAwakeningPacket packet, net.minecraft.network.FriendlyByteBuf buffer) {
    }

    public static HeartAwakeningPacket decode(net.minecraft.network.FriendlyByteBuf buffer) {
        return new HeartAwakeningPacket();
    }

    public static void handle(HeartAwakeningPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            if (!ModEvents.canUseHeartSlot(player)) {
                return;
            }

            if (!ModItems.HeartOfDuat.hasActivationXp(player)) {
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.ramesses_curse.heart_no_xp")
                        .withStyle(net.minecraft.ChatFormatting.RED), true);
                return;
            }

            if (ModItems.canActivateHeartAwakening(player)) {
                ModItems.activateHeartAwakening(player);
            }
        });
        context.setPacketHandled(true);
    }
}

