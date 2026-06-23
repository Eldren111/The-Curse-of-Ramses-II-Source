package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GuiltSyncPacket {

    private final int guilt;
    private final boolean sealEquipped;
    private final boolean netherAwakened;
    private final boolean curseTwoReversed;
    private final boolean curseEightReversed;
    private final int lockTicks;
    private final int satietyCrystalCount;
    private final int lockedSlot;
    private final int lockedSlotTicks;

    public GuiltSyncPacket(int guilt, boolean sealEquipped, boolean netherAwakened, boolean curseTwoReversed, boolean curseEightReversed, int lockTicks, int satietyCrystalCount, int lockedSlot, int lockedSlotTicks) {
        this.guilt = guilt;
        this.sealEquipped = sealEquipped;
        this.netherAwakened = netherAwakened;
        this.curseTwoReversed = curseTwoReversed;
        this.curseEightReversed = curseEightReversed;
        this.lockTicks = lockTicks;
        this.satietyCrystalCount = satietyCrystalCount;
        this.lockedSlot = lockedSlot;
        this.lockedSlotTicks = lockedSlotTicks;
    }

    public static void encode(GuiltSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.guilt);
        buffer.writeBoolean(packet.sealEquipped);
        buffer.writeBoolean(packet.netherAwakened);
        buffer.writeBoolean(packet.curseTwoReversed);
        buffer.writeBoolean(packet.curseEightReversed);
        buffer.writeVarInt(packet.lockTicks);
        buffer.writeVarInt(packet.satietyCrystalCount);
        buffer.writeVarInt(packet.lockedSlot);
        buffer.writeVarInt(packet.lockedSlotTicks);
    }

    public static GuiltSyncPacket decode(FriendlyByteBuf buffer) {
        return new GuiltSyncPacket(
                buffer.readVarInt(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt());
    }

    public static void handle(GuiltSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ModEvents.ClientOverlayEvents.updateClientGuilt(
                        packet.guilt,
                        packet.sealEquipped,
                        packet.netherAwakened,
                        packet.curseTwoReversed,
                        packet.curseEightReversed,
                        packet.lockTicks,
                        packet.satietyCrystalCount,
                        packet.lockedSlot,
                        packet.lockedSlotTicks)));
        context.setPacketHandled(true);
    }
}
