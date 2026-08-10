package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SatietyRiddleSelectPacket {
    private final int index;

    public SatietyRiddleSelectPacket(int index) {
        this.index = index;
    }

    public static void encode(SatietyRiddleSelectPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.index);
    }

    public static SatietyRiddleSelectPacket decode(FriendlyByteBuf buffer) {
        return new SatietyRiddleSelectPacket(buffer.readVarInt());
    }

    public static void handle(SatietyRiddleSelectPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) ModEvents.selectSatietyRiddle(player, packet.index);
        });
        context.setPacketHandled(true);
    }
}
