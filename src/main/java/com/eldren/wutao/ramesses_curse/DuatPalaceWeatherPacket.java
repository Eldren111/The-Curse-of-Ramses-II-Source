package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;

import java.util.function.Supplier;

public final class DuatPalaceWeatherPacket {
    private final boolean active;

    public DuatPalaceWeatherPacket(boolean active) {
        this.active = active;
    }

    public static void encode(DuatPalaceWeatherPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.active);
    }

    public static DuatPalaceWeatherPacket decode(FriendlyByteBuf buffer) {
        return new DuatPalaceWeatherPacket(buffer.readBoolean());
    }

    public static void handle(DuatPalaceWeatherPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> DuatPalaceWeatherRenderer.setActive(packet.active)));
        context.setPacketHandled(true);
    }
}

