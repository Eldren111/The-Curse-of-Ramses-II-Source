package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public class EmberRiddleSelectPacket {

    private final int index;

    public EmberRiddleSelectPacket(int index) {
        this.index = index;
    }

    public static void encode(EmberRiddleSelectPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.index);
    }

    public static EmberRiddleSelectPacket decode(FriendlyByteBuf buffer) {
        return new EmberRiddleSelectPacket(buffer.readVarInt());
    }

    public static void handle(EmberRiddleSelectPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ModEvents.selectEmberRiddle(player, packet.index);
            }
        });
        context.setPacketHandled(true);
    }
}

