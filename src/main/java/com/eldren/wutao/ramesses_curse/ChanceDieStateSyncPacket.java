package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;

import java.util.function.Supplier;

public record ChanceDieStateSyncPacket(
        boolean attackSpeed,
        boolean fallImmunity,
        boolean dodge,
        int undyingCharges,
        int healthDamageSeconds,
        int trueDamageSeconds) {

    public static void encode(ChanceDieStateSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.attackSpeed);
        buffer.writeBoolean(packet.fallImmunity);
        buffer.writeBoolean(packet.dodge);
        buffer.writeVarInt(packet.undyingCharges);
        buffer.writeVarInt(packet.healthDamageSeconds);
        buffer.writeVarInt(packet.trueDamageSeconds);
    }

    public static ChanceDieStateSyncPacket decode(FriendlyByteBuf buffer) {
        return new ChanceDieStateSyncPacket(
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt());
    }

    public static void handle(ChanceDieStateSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ModEvents.ClientOverlayEvents.updateClientChanceDieState(
                        packet.attackSpeed,
                        packet.fallImmunity,
                        packet.dodge,
                        packet.undyingCharges,
                        packet.healthDamageSeconds,
                        packet.trueDamageSeconds)));
        context.setPacketHandled(true);
    }
}

