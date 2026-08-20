package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;

import java.util.function.Supplier;

public record AffixKnowledgeSyncPacket(long learnedMask) {
    public static void encode(AffixKnowledgeSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarLong(packet.learnedMask);
    }

    public static AffixKnowledgeSyncPacket decode(FriendlyByteBuf buffer) {
        return new AffixKnowledgeSyncPacket(buffer.readVarLong());
    }

    public static void handle(AffixKnowledgeSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientAffixKnowledge.update(packet.learnedMask)));
        context.setPacketHandled(true);
    }
}

