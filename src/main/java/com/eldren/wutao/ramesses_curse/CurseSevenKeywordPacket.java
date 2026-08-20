package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public class CurseSevenKeywordPacket {

    public CurseSevenKeywordPacket() {
    }

    public static void encode(CurseSevenKeywordPacket packet, FriendlyByteBuf buffer) {
    }

    public static CurseSevenKeywordPacket decode(FriendlyByteBuf buffer) {
        return new CurseSevenKeywordPacket();
    }

    public static void handle(CurseSevenKeywordPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ModEvents.handleCurseSevenKeyword(player);
            }
        });
        context.setPacketHandled(true);
    }
}

