package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;

import java.util.function.Supplier;

public final class NebukaRitualShakePacket {
    private final int durationTicks;
    private final float strength;

    public NebukaRitualShakePacket(int durationTicks, float strength) {
        this.durationTicks = durationTicks;
        this.strength = strength;
    }

    public static void encode(NebukaRitualShakePacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.durationTicks);
        buffer.writeFloat(packet.strength);
    }

    public static NebukaRitualShakePacket decode(FriendlyByteBuf buffer) {
        return new NebukaRitualShakePacket(buffer.readVarInt(), buffer.readFloat());
    }

    public static void handle(NebukaRitualShakePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> NebukaRitualClientEffects.startShake(packet.durationTicks, packet.strength)));
        context.setPacketHandled(true);
    }
}

