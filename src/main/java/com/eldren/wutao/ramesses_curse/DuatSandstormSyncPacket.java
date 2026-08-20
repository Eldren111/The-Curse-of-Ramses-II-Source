package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;

import java.util.function.Supplier;

public record DuatSandstormSyncPacket(
        boolean active,
        double centerX,
        double centerZ,
        double velocityX,
        double velocityZ,
        double radius,
        float intensity,
        int remainingTicks) {

    public static DuatSandstormSyncPacket inactive() {
        return new DuatSandstormSyncPacket(false, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0F, 0);
    }

    public static void encode(DuatSandstormSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.active);
        buffer.writeDouble(packet.centerX);
        buffer.writeDouble(packet.centerZ);
        buffer.writeDouble(packet.velocityX);
        buffer.writeDouble(packet.velocityZ);
        buffer.writeDouble(packet.radius);
        buffer.writeFloat(packet.intensity);
        buffer.writeVarInt(packet.remainingTicks);
    }

    public static DuatSandstormSyncPacket decode(FriendlyByteBuf buffer) {
        return new DuatSandstormSyncPacket(
                buffer.readBoolean(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readFloat(),
                buffer.readVarInt());
    }

    public static void handle(DuatSandstormSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> DuatDesertClientEffects.applySandstormSync(packet)));
        context.setPacketHandled(true);
    }
}

