package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public class SatietyRiddleSubmitPacket {
    private final String answer;

    public SatietyRiddleSubmitPacket(String answer) {
        this.answer = answer;
    }

    public static void encode(SatietyRiddleSubmitPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.answer, 256);
    }

    public static SatietyRiddleSubmitPacket decode(FriendlyByteBuf buffer) {
        return new SatietyRiddleSubmitPacket(buffer.readUtf(256));
    }

    public static void handle(SatietyRiddleSubmitPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) ModEvents.submitSatietyRiddleAnswer(player, packet.answer);
        });
        context.setPacketHandled(true);
    }
}

