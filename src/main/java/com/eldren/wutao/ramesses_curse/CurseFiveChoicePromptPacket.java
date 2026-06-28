package com.eldren.wutao.ramesses_curse;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CurseFiveChoicePromptPacket {

    public static void encode(CurseFiveChoicePromptPacket packet, net.minecraft.network.FriendlyByteBuf buffer) {
    }

    public static CurseFiveChoicePromptPacket decode(net.minecraft.network.FriendlyByteBuf buffer) {
        return new CurseFiveChoicePromptPacket();
    }

    public static void handle(CurseFiveChoicePromptPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ModEvents.ClientOverlayEvents.openCurseFiveChoiceScreen()));
        context.setPacketHandled(true);
    }
}
