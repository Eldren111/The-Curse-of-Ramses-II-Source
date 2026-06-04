package com.eldren.wutao.ramesses_curse;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class HeartWardActivatePacket {

    public static void encode(HeartWardActivatePacket packet, net.minecraft.network.FriendlyByteBuf buffer) {
    }

    public static HeartWardActivatePacket decode(net.minecraft.network.FriendlyByteBuf buffer) {
        return new HeartWardActivatePacket();
    }

    public static void handle(HeartWardActivatePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !ModEvents.canUseHeartSlot(player)) {
                return;
            }

            ModItems.tryActivateHeartWard(player);
        });
        context.setPacketHandled(true);
    }
}
