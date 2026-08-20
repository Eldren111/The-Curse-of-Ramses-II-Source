package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;

import java.util.function.Supplier;

public record ChanceDieRollPacket(int outcome) {
    public static void encode(ChanceDieRollPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.outcome);
    }

    public static ChanceDieRollPacket decode(FriendlyByteBuf buffer) {
        return new ChanceDieRollPacket(buffer.readVarInt());
    }

    public static void handle(ChanceDieRollPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ModEvents.ClientOverlayEvents.showChanceDieRoll(packet.outcome)));
        context.setPacketHandled(true);
    }
}

