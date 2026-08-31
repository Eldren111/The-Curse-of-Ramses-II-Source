package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record RamessesEndCrystalBreakVfxPacket(double x, double y, double z) {
    public static void encode(RamessesEndCrystalBreakVfxPacket packet, FriendlyByteBuf buffer) {
        buffer.writeDouble(packet.x);
        buffer.writeDouble(packet.y);
        buffer.writeDouble(packet.z);
    }

    public static RamessesEndCrystalBreakVfxPacket decode(FriendlyByteBuf buffer) {
        return new RamessesEndCrystalBreakVfxPacket(
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble());
    }

    public static void handle(RamessesEndCrystalBreakVfxPacket packet,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientPacketHandlers.triggerEndCrystalBreakVfx(packet.x, packet.y, packet.z)));
        context.setPacketHandled(true);
    }
}
