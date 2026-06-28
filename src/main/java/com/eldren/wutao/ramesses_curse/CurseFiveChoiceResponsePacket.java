package com.eldren.wutao.ramesses_curse;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CurseFiveChoiceResponsePacket {

    private final boolean reverse;

    public CurseFiveChoiceResponsePacket(boolean reverse) {
        this.reverse = reverse;
    }

    public static void encode(CurseFiveChoiceResponsePacket packet, net.minecraft.network.FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.reverse);
    }

    public static CurseFiveChoiceResponsePacket decode(net.minecraft.network.FriendlyByteBuf buffer) {
        return new CurseFiveChoiceResponsePacket(buffer.readBoolean());
    }

    public static void handle(CurseFiveChoiceResponsePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ModEvents.resolveCurseFiveChoice(player, packet.reverse);
            }
        });
        context.setPacketHandled(true);
    }
}
