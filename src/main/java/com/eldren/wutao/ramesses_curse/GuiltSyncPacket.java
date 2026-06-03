package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GuiltSyncPacket {

    private final int guilt;
    private final boolean sealEquipped;
    private final int lockTicks;
    private final int satietyCrystalCount;

    public GuiltSyncPacket(int guilt, boolean sealEquipped, int lockTicks, int satietyCrystalCount) {
        this.guilt = guilt;
        this.sealEquipped = sealEquipped;
        this.lockTicks = lockTicks;
        this.satietyCrystalCount = satietyCrystalCount;
    }

    public static void encode(GuiltSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.guilt);
        buffer.writeBoolean(packet.sealEquipped);
        buffer.writeVarInt(packet.lockTicks);
        buffer.writeVarInt(packet.satietyCrystalCount);
    }

    public static GuiltSyncPacket decode(FriendlyByteBuf buffer) {
        return new GuiltSyncPacket(buffer.readVarInt(), buffer.readBoolean(), buffer.readVarInt(), buffer.readVarInt());
    }

    public static void handle(GuiltSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ModEvents.ClientOverlayEvents.updateClientGuilt(packet.guilt, packet.sealEquipped, packet.lockTicks, packet.satietyCrystalCount)));
        context.setPacketHandled(true);
    }
}
