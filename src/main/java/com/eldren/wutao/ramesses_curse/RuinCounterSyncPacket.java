package com.eldren.wutao.ramesses_curse;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record RuinCounterSyncPacket(int counter) {

    public static void encode(RuinCounterSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(Math.max(0, packet.counter));
    }

    public static RuinCounterSyncPacket decode(FriendlyByteBuf buffer) {
        return new RuinCounterSyncPacket(buffer.readVarInt());
    }

    public static void handle(
            RuinCounterSyncPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ModItems.RuinTalisman.setClientCounter(packet.counter)));
        context.setPacketHandled(true);
    }
}
