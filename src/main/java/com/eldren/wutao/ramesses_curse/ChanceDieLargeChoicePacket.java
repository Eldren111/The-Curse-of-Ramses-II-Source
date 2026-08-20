package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public record ChanceDieLargeChoicePacket(int index, boolean accept) {
    public static void encode(ChanceDieLargeChoicePacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.index);
        buffer.writeBoolean(packet.accept);
    }

    public static ChanceDieLargeChoicePacket decode(FriendlyByteBuf buffer) {
        return new ChanceDieLargeChoicePacket(buffer.readVarInt(), buffer.readBoolean());
    }

    public static void handle(ChanceDieLargeChoicePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ModItems.ChanceDieLarge.choose(player, packet.index, packet.accept);
            }
        });
        context.setPacketHandled(true);
    }
}

