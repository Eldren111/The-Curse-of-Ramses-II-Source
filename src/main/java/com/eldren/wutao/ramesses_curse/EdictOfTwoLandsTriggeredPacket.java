package com.eldren.wutao.ramesses_curse;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EdictOfTwoLandsTriggeredPacket {
    public static void encode(EdictOfTwoLandsTriggeredPacket packet, net.minecraft.network.FriendlyByteBuf buffer) {
    }

    public static EdictOfTwoLandsTriggeredPacket decode(net.minecraft.network.FriendlyByteBuf buffer) {
        return new EdictOfTwoLandsTriggeredPacket();
    }

    public static void handle(EdictOfTwoLandsTriggeredPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> ClientPacketHandlers::showEdictTriggered));
        context.setPacketHandled(true);
    }
}
