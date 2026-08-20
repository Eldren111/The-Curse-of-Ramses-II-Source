package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;

import java.util.function.Supplier;

public record ChanceDieLargeRollPacket(int[] outcomes, int unlockTicks) {
    public static void encode(ChanceDieLargeRollPacket packet, FriendlyByteBuf buffer) {
        for (int outcome : packet.outcomes) {
            buffer.writeVarInt(outcome);
        }
        buffer.writeVarInt(packet.unlockTicks);
    }

    public static ChanceDieLargeRollPacket decode(FriendlyByteBuf buffer) {
        int[] outcomes = new int[6];
        for (int index = 0; index < outcomes.length; index++) {
            outcomes[index] = buffer.readVarInt();
        }
        return new ChanceDieLargeRollPacket(outcomes, buffer.readVarInt());
    }

    public static void handle(ChanceDieLargeRollPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientPacketHandlers.showLargeChanceDieScreen(packet.outcomes, packet.unlockTicks)));
        context.setPacketHandled(true);
    }
}

