package com.eldren.wutao.ramesses_curse;

import net.minecraft.client.Minecraft;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CurseFiveMercyReviveAckPacket {

    public static void encode(CurseFiveMercyReviveAckPacket packet, net.minecraft.network.FriendlyByteBuf buffer) {
    }

    public static CurseFiveMercyReviveAckPacket decode(net.minecraft.network.FriendlyByteBuf buffer) {
        return new CurseFiveMercyReviveAckPacket();
    }

    public static void handle(CurseFiveMercyReviveAckPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                minecraft.player.respawn();
            }
        });
        context.setPacketHandled(true);
    }
}
