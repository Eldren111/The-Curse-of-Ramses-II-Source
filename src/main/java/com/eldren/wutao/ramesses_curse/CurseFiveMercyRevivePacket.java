package com.eldren.wutao.ramesses_curse;

import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public class CurseFiveMercyRevivePacket {

    public static void encode(CurseFiveMercyRevivePacket packet, net.minecraft.network.FriendlyByteBuf buffer) {
    }

    public static CurseFiveMercyRevivePacket decode(net.minecraft.network.FriendlyByteBuf buffer) {
        return new CurseFiveMercyRevivePacket();
    }

    public static void handle(CurseFiveMercyRevivePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                if (ModEvents.triggerCurseFiveMercyRevive(player)) {
                    ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new CurseFiveMercyReviveAckPacket());
                }
            }
        });
        context.setPacketHandled(true);
    }
}

