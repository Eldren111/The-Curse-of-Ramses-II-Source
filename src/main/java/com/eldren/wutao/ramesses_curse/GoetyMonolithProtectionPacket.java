package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;

import java.util.function.Supplier;

public record GoetyMonolithProtectionPacket(int entityId, boolean active) {
    public static void encode(GoetyMonolithProtectionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId);
        buffer.writeBoolean(packet.active);
    }

    public static GoetyMonolithProtectionPacket decode(FriendlyByteBuf buffer) {
        return new GoetyMonolithProtectionPacket(buffer.readVarInt(), buffer.readBoolean());
    }

    public static void handle(
            GoetyMonolithProtectionPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientPacketHandlers.syncGoetyMonolithProtection(packet.entityId, packet.active)));
        context.setPacketHandled(true);
    }
}
