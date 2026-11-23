package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record DuatBlackHoleVfxPacket(double x, double y, double z, int durationTicks) {
    public static void encode(DuatBlackHoleVfxPacket packet, FriendlyByteBuf buffer) {
        buffer.writeDouble(packet.x);
        buffer.writeDouble(packet.y);
        buffer.writeDouble(packet.z);
        buffer.writeVarInt(packet.durationTicks);
    }

    public static DuatBlackHoleVfxPacket decode(FriendlyByteBuf buffer) {
        return new DuatBlackHoleVfxPacket(
                buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readVarInt());
    }

    public static void handle(DuatBlackHoleVfxPacket packet,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientPacketHandlers.triggerDuatBlackHoleVfx(
                        packet.x, packet.y, packet.z, packet.durationTicks)));
        context.setPacketHandled(true);
    }
}
