package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record DuatCuriosSealPacket(boolean sealed, int overlayAlpha, List<String> whitelist) {
    public static void encode(DuatCuriosSealPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.sealed);
        buffer.writeVarInt(packet.overlayAlpha);
        buffer.writeVarInt(packet.whitelist.size());
        packet.whitelist.forEach(buffer::writeUtf);
    }

    public static DuatCuriosSealPacket decode(FriendlyByteBuf buffer) {
        boolean sealed = buffer.readBoolean();
        int overlayAlpha = buffer.readVarInt();
        int size = Math.min(buffer.readVarInt(), 4096);
        List<String> whitelist = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            whitelist.add(buffer.readUtf());
        }
        return new DuatCuriosSealPacket(sealed, overlayAlpha, List.copyOf(whitelist));
    }

    public static void handle(DuatCuriosSealPacket packet,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                DuatCuriosSealClient.update(packet.sealed, packet.overlayAlpha, packet.whitelist)));
        context.setPacketHandled(true);
    }
}
