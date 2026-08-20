package com.eldren.wutao.ramesses_curse;

import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public class EmberRiddleRefreshPacket {

    public static void encode(EmberRiddleRefreshPacket packet, net.minecraft.network.FriendlyByteBuf buffer) {
    }

    public static EmberRiddleRefreshPacket decode(net.minecraft.network.FriendlyByteBuf buffer) {
        return new EmberRiddleRefreshPacket();
    }

    public static void handle(EmberRiddleRefreshPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ModEvents.startNewEmberRiddleRound(player, true);
            }
        });
        context.setPacketHandled(true);
    }
}

