package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public class EmberRiddleSubmitPacket {

    private final String answer;

    public EmberRiddleSubmitPacket(String answer) {
        this.answer = answer;
    }

    public static void encode(EmberRiddleSubmitPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.answer, 256);
    }

    public static EmberRiddleSubmitPacket decode(FriendlyByteBuf buffer) {
        return new EmberRiddleSubmitPacket(buffer.readUtf(256));
    }

    public static void handle(EmberRiddleSubmitPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ModEvents.submitEmberRiddleAnswer(player, packet.answer);
            }
        });
        context.setPacketHandled(true);
    }
}

