package com.eldren.wutao.ramesses_curse;


import java.util.function.Supplier;

public class EdictCooldownSyncPacket {

    private final int cooldownTicks;

    public EdictCooldownSyncPacket(int cooldownTicks) {
        this.cooldownTicks = cooldownTicks;
    }

    public static void encode(EdictCooldownSyncPacket packet, net.minecraft.network.FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.cooldownTicks);
    }

    public static EdictCooldownSyncPacket decode(net.minecraft.network.FriendlyByteBuf buffer) {
        return new EdictCooldownSyncPacket(buffer.readVarInt());
    }

    public static void handle(EdictCooldownSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ModEvents.ClientOverlayEvents.updateClientEdictCooldown(packet.cooldownTicks));
        context.setPacketHandled(true);
    }
}

