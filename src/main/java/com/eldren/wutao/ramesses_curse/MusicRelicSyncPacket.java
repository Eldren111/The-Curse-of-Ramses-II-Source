package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MusicRelicSyncPacket {

    private final boolean awakened;

    public MusicRelicSyncPacket(boolean awakened) {
        this.awakened = awakened;
    }

    public static void encode(MusicRelicSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.awakened);
    }

    public static MusicRelicSyncPacket decode(FriendlyByteBuf buffer) {
        return new MusicRelicSyncPacket(buffer.readBoolean());
    }

    public static void handle(MusicRelicSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ModItems.ResonantScarab.updateClientAwakened(packet.awakened)));
        context.setPacketHandled(true);
    }
}
