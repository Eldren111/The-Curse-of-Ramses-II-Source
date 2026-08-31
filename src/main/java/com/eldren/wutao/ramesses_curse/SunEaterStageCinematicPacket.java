package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SunEaterStageCinematicPacket(double x, double y, double z, int durationTicks) {
    public static void encode(SunEaterStageCinematicPacket packet, FriendlyByteBuf buffer) {
        buffer.writeDouble(packet.x);
        buffer.writeDouble(packet.y);
        buffer.writeDouble(packet.z);
        buffer.writeVarInt(packet.durationTicks);
    }

    public static SunEaterStageCinematicPacket decode(FriendlyByteBuf buffer) {
        return new SunEaterStageCinematicPacket(
                buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readVarInt());
    }

    public static void handle(SunEaterStageCinematicPacket packet,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientPacketHandlers.startSunEaterStageCinematic(
                        packet.x, packet.y, packet.z, packet.durationTicks)));
        context.setPacketHandled(true);
    }
}
