package com.eldren.wutao.ramesses_curse;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class HeartWardTriggeredPacket {
    public static void encode(HeartWardTriggeredPacket packet, net.minecraft.network.FriendlyByteBuf buffer) {
    }

    public static HeartWardTriggeredPacket decode(net.minecraft.network.FriendlyByteBuf buffer) {
        return new HeartWardTriggeredPacket();
    }

    public static void handle(HeartWardTriggeredPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> ClientPacketHandlers::showHeartWardTriggered));
        context.setPacketHandled(true);
    }
}
