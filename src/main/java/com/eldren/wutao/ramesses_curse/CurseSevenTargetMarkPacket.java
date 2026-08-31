package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;

import java.util.function.Supplier;

public record CurseSevenTargetMarkPacket(int entityId, long expiryGameTime) {
    public static void encode(CurseSevenTargetMarkPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId);
        buffer.writeVarLong(packet.expiryGameTime);
    }

    public static CurseSevenTargetMarkPacket decode(FriendlyByteBuf buffer) {
        return new CurseSevenTargetMarkPacket(buffer.readVarInt(), buffer.readVarLong());
    }

    public static void handle(CurseSevenTargetMarkPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientPacketHandlers.syncCurseSevenTargetMark(packet.entityId, packet.expiryGameTime)));
        context.setPacketHandled(true);
    }
}
