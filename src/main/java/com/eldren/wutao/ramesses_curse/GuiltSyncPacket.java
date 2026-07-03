package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GuiltSyncPacket {

    private final int guilt;
    private final int mercy;
    private final boolean mercyChargeReady;
    private final boolean sealEquipped;
    private final boolean netherAwakened;
    private final boolean curseOneReversed;
    private final boolean curseTwoReversed;
    private final boolean curseFiveReversed;
    private final boolean curseFiveDeclined;
    private final boolean curseSevenKeywordUnlocked;
    private final boolean curseSevenTerminalSolved;
    private final boolean curseSevenReversed;
    private final boolean curseSevenSentenceUnlocked;
    private final boolean curseEightReversed;
    private final boolean curseNineReversed;
    private final int lockTicks;
    private final int satietyCrystalCount;
    private final int curseNineDebtTicks;
    private final int lockedSlot;
    private final int lockedSlotTicks;

    public GuiltSyncPacket(int guilt, int mercy, boolean mercyChargeReady, boolean sealEquipped, boolean netherAwakened, boolean curseOneReversed, boolean curseTwoReversed, boolean curseFiveReversed, boolean curseFiveDeclined, boolean curseSevenKeywordUnlocked, boolean curseSevenTerminalSolved, boolean curseSevenReversed, boolean curseSevenSentenceUnlocked, boolean curseEightReversed, boolean curseNineReversed, int lockTicks, int satietyCrystalCount, int curseNineDebtTicks, int lockedSlot, int lockedSlotTicks) {
        this.guilt = guilt;
        this.mercy = mercy;
        this.mercyChargeReady = mercyChargeReady;
        this.sealEquipped = sealEquipped;
        this.netherAwakened = netherAwakened;
        this.curseOneReversed = curseOneReversed;
        this.curseTwoReversed = curseTwoReversed;
        this.curseFiveReversed = curseFiveReversed;
        this.curseFiveDeclined = curseFiveDeclined;
        this.curseSevenKeywordUnlocked = curseSevenKeywordUnlocked;
        this.curseSevenTerminalSolved = curseSevenTerminalSolved;
        this.curseSevenReversed = curseSevenReversed;
        this.curseSevenSentenceUnlocked = curseSevenSentenceUnlocked;
        this.curseEightReversed = curseEightReversed;
        this.curseNineReversed = curseNineReversed;
        this.lockTicks = lockTicks;
        this.satietyCrystalCount = satietyCrystalCount;
        this.curseNineDebtTicks = curseNineDebtTicks;
        this.lockedSlot = lockedSlot;
        this.lockedSlotTicks = lockedSlotTicks;
    }

    public static void encode(GuiltSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.guilt);
        buffer.writeVarInt(packet.mercy);
        buffer.writeBoolean(packet.mercyChargeReady);
        buffer.writeBoolean(packet.sealEquipped);
        buffer.writeBoolean(packet.netherAwakened);
        buffer.writeBoolean(packet.curseOneReversed);
        buffer.writeBoolean(packet.curseTwoReversed);
        buffer.writeBoolean(packet.curseFiveReversed);
        buffer.writeBoolean(packet.curseFiveDeclined);
        buffer.writeBoolean(packet.curseSevenKeywordUnlocked);
        buffer.writeBoolean(packet.curseSevenTerminalSolved);
        buffer.writeBoolean(packet.curseSevenReversed);
        buffer.writeBoolean(packet.curseSevenSentenceUnlocked);
        buffer.writeBoolean(packet.curseEightReversed);
        buffer.writeBoolean(packet.curseNineReversed);
        buffer.writeVarInt(packet.lockTicks);
        buffer.writeVarInt(packet.satietyCrystalCount);
        buffer.writeVarInt(packet.curseNineDebtTicks);
        buffer.writeVarInt(packet.lockedSlot);
        buffer.writeVarInt(packet.lockedSlotTicks);
    }

    public static GuiltSyncPacket decode(FriendlyByteBuf buffer) {
        return new GuiltSyncPacket(
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readVarInt(),
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
                        packet.mercy,
                        packet.mercyChargeReady,
                        packet.sealEquipped,
                        packet.netherAwakened,
                        packet.curseOneReversed,
                        packet.curseTwoReversed,
                        packet.curseFiveReversed,
                        packet.curseFiveDeclined,
                        packet.curseSevenKeywordUnlocked,
                        packet.curseSevenTerminalSolved,
                        packet.curseSevenReversed,
                        packet.curseSevenSentenceUnlocked,
                        packet.curseEightReversed,
                        packet.curseNineReversed,
                        packet.lockTicks,
                        packet.satietyCrystalCount,
                        packet.curseNineDebtTicks,
                        packet.lockedSlot,
                        packet.lockedSlotTicks)));
        context.setPacketHandled(true);
    }
}
