package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record DuatSunEaterDeathVfxPacket(double x, double y, double z) {
    public static void encode(DuatSunEaterDeathVfxPacket packet, FriendlyByteBuf buffer) {
        buffer.writeDouble(packet.x);
        buffer.writeDouble(packet.y);
        buffer.writeDouble(packet.z);
    }

    public static DuatSunEaterDeathVfxPacket decode(FriendlyByteBuf buffer) {
        return new DuatSunEaterDeathVfxPacket(
                buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    public static void handle(DuatSunEaterDeathVfxPacket packet,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientPacketHandlers.triggerDuatSunEaterDeathVfx(packet.x, packet.y, packet.z)));
        context.setPacketHandled(true);
    }
}
