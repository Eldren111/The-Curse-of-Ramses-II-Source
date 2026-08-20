package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public class CurseSevenRevealPacket {

    private final String answer;

    public CurseSevenRevealPacket(String answer) {
        this.answer = answer == null ? "" : answer;
    }

    public static void encode(CurseSevenRevealPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.answer);
    }

    public static CurseSevenRevealPacket decode(FriendlyByteBuf buffer) {
        return new CurseSevenRevealPacket(buffer.readUtf());
    }

    public static void handle(CurseSevenRevealPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ModEvents.handleCurseSevenReveal(player, packet.answer);
            }
        });
        context.setPacketHandled(true);
    }
}

