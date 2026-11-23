package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record CurseNineSealPacket(int ticks) {
    public static void encode(CurseNineSealPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.ticks);
    }

    public static CurseNineSealPacket decode(FriendlyByteBuf buffer) {
        return new CurseNineSealPacket(Math.max(0, buffer.readVarInt()));
    }

    public static void handle(CurseNineSealPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> CurseNineSealClient.update(packet.ticks)));
        context.setPacketHandled(true);
    }
}
