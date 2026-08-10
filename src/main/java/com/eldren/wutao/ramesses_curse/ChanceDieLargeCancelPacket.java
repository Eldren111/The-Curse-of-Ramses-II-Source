package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ChanceDieLargeCancelPacket() {
    public static void encode(ChanceDieLargeCancelPacket packet, FriendlyByteBuf buffer) {
    }

    public static ChanceDieLargeCancelPacket decode(FriendlyByteBuf buffer) {
        return new ChanceDieLargeCancelPacket();
    }

    public static void handle(ChanceDieLargeCancelPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ModItems.ChanceDieLarge.cancel(player);
            }
        });
        context.setPacketHandled(true);
    }
}
