package com.eldren.wutao.ramesses_curse;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;

public class EternalSilenceRebirthCooldownPacket {
    private final int remainingTicks;

    public EternalSilenceRebirthCooldownPacket(int remainingTicks) {
        this.remainingTicks = remainingTicks;
    }

    public static void encode(EternalSilenceRebirthCooldownPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.remainingTicks);
    }

    public static EternalSilenceRebirthCooldownPacket decode(FriendlyByteBuf buffer) {
        return new EternalSilenceRebirthCooldownPacket(buffer.readVarInt());
    }

    public static void handle(
            EternalSilenceRebirthCooldownPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ModItems.EternalSilenceRebirthTotem.setClientCooldownTicks(packet.remainingTicks)));
        context.setPacketHandled(true);
    }
}

