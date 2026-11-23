package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SunEaterStageRitualVfxPacket(
        double x, double y, double z, int stage, int elapsedTicks, int durationTicks) {
    public static void encode(SunEaterStageRitualVfxPacket packet, FriendlyByteBuf buffer) {
        buffer.writeDouble(packet.x);
        buffer.writeDouble(packet.y);
        buffer.writeDouble(packet.z);
        buffer.writeVarInt(packet.stage);
        buffer.writeVarInt(packet.elapsedTicks);
        buffer.writeInt(packet.durationTicks);
    }

    public static SunEaterStageRitualVfxPacket decode(FriendlyByteBuf buffer) {
        return new SunEaterStageRitualVfxPacket(
                buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
                buffer.readVarInt(), buffer.readVarInt(), buffer.readInt());
    }

    public static void handle(
            SunEaterStageRitualVfxPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientPacketHandlers.syncSunEaterStageRitual(
                        packet.x, packet.y, packet.z, packet.stage,
                        packet.elapsedTicks, packet.durationTicks)));
        context.setPacketHandled(true);
    }
}
