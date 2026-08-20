package com.eldren.wutao.ramesses_curse;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;

public class EternalSilenceTotemTriggeredPacket {
    private final boolean rebirthTotem;

    public EternalSilenceTotemTriggeredPacket(boolean rebirthTotem) {
        this.rebirthTotem = rebirthTotem;
    }

    public static void encode(EternalSilenceTotemTriggeredPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.rebirthTotem);
    }

    public static EternalSilenceTotemTriggeredPacket decode(FriendlyByteBuf buffer) {
        return new EternalSilenceTotemTriggeredPacket(buffer.readBoolean());
    }

    public static void handle(
            EternalSilenceTotemTriggeredPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientPacketHandlers.showEternalSilenceTotemTriggered(packet.rebirthTotem)));
        context.setPacketHandled(true);
    }
}

