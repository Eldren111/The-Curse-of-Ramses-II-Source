package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;

import java.util.function.Supplier;

public class EdictWorldEffectPacket {
    private final double x;
    private final double y;
    private final double z;
    private final float radius;
    private final int durationTicks;
    private final boolean domain;

    public EdictWorldEffectPacket(double x, double y, double z, float radius, int durationTicks, boolean domain) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.durationTicks = durationTicks;
        this.domain = domain;
    }

    public static void encode(EdictWorldEffectPacket packet, FriendlyByteBuf buffer) {
        buffer.writeDouble(packet.x);
        buffer.writeDouble(packet.y);
        buffer.writeDouble(packet.z);
        buffer.writeFloat(packet.radius);
        buffer.writeVarInt(packet.durationTicks);
        buffer.writeBoolean(packet.domain);
    }

    public static EdictWorldEffectPacket decode(FriendlyByteBuf buffer) {
        return new EdictWorldEffectPacket(
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readFloat(),
                buffer.readVarInt(),
                buffer.readBoolean());
    }

    public static void handle(EdictWorldEffectPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ModEvents.ClientOverlayEvents.addEdictWorldEffect(
                        packet.x,
                        packet.y,
                        packet.z,
                        packet.radius,
                        packet.durationTicks,
                        packet.domain)));
        context.setPacketHandled(true);
    }
}

