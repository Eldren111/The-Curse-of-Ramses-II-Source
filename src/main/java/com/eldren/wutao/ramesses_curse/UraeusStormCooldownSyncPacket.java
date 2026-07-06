package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UraeusStormCooldownSyncPacket {
    private final int cooldownTicks;

    public UraeusStormCooldownSyncPacket(int cooldownTicks) {
        this.cooldownTicks = cooldownTicks;
    }

    public static void encode(UraeusStormCooldownSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.cooldownTicks);
    }

    public static UraeusStormCooldownSyncPacket decode(FriendlyByteBuf buffer) {
        return new UraeusStormCooldownSyncPacket(buffer.readVarInt());
    }

    public static void handle(UraeusStormCooldownSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ModEvents.ClientOverlayEvents.updateClientUraeusStormCooldown(packet.cooldownTicks));
        context.setPacketHandled(true);
    }
}
