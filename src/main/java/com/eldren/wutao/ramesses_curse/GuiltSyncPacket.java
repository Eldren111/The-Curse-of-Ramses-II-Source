package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;

import java.util.function.Supplier;

public class GuiltSyncPacket {

    private final int guilt;
    private final int mercy;
    private final boolean mercyChargeReady;
    private final boolean sealEquipped;
    private final int sealEquipSeconds;
    private final boolean netherAwakened;
    private final boolean endAwakened;
    private final boolean curseOneReversed;
    private final boolean curseTwoReversed;
    private final boolean curseFiveReversed;
    private final boolean curseFiveDeclined;
    private final boolean curseSevenKeywordUnlocked;
    private final boolean curseSevenTerminalSolved;
    private final boolean curseSixReversed;
    private final boolean curseSevenReversed;
    private final boolean curseSevenSentenceUnlocked;
    private final boolean curseEightReversed;
    private final boolean curseNineReversed;
    private final int lockTicks;
    private final int satietyCrystalCount;
    private final int curseNineDebtTicks;
    private final int curseNineRequiredFoodTypes;
    private final int curseNineRemainingFoodTypes;
    private final int lockedSlot;
    private final int lockedSlotTicks;
    private final int dynastyThreat;
    private final int dynastySilenceMainHandTicks;
    private final int dynastySilenceOffHandTicks;

    public GuiltSyncPacket(int guilt, int mercy, boolean mercyChargeReady, boolean sealEquipped, int sealEquipSeconds, boolean netherAwakened, boolean endAwakened, boolean curseOneReversed, boolean curseTwoReversed, boolean curseFiveReversed, boolean curseFiveDeclined, boolean curseSevenKeywordUnlocked, boolean curseSevenTerminalSolved, boolean curseSixReversed, boolean curseSevenReversed, boolean curseSevenSentenceUnlocked, boolean curseEightReversed, boolean curseNineReversed, int lockTicks, int satietyCrystalCount, int curseNineDebtTicks, int curseNineRequiredFoodTypes, int curseNineRemainingFoodTypes, int lockedSlot, int lockedSlotTicks, int dynastyThreat, int dynastySilenceMainHandTicks, int dynastySilenceOffHandTicks) {
        this.guilt = guilt;
        this.mercy = mercy;
        this.mercyChargeReady = mercyChargeReady;
        this.sealEquipped = sealEquipped;
        this.sealEquipSeconds = sealEquipSeconds;
        this.netherAwakened = netherAwakened;
        this.endAwakened = endAwakened;
        this.curseOneReversed = curseOneReversed;
        this.curseTwoReversed = curseTwoReversed;
        this.curseFiveReversed = curseFiveReversed;
        this.curseFiveDeclined = curseFiveDeclined;
        this.curseSevenKeywordUnlocked = curseSevenKeywordUnlocked;
        this.curseSevenTerminalSolved = curseSevenTerminalSolved;
        this.curseSixReversed = curseSixReversed;
        this.curseSevenReversed = curseSevenReversed;
        this.curseSevenSentenceUnlocked = curseSevenSentenceUnlocked;
        this.curseEightReversed = curseEightReversed;
        this.curseNineReversed = curseNineReversed;
        this.lockTicks = lockTicks;
        this.satietyCrystalCount = satietyCrystalCount;
        this.curseNineDebtTicks = curseNineDebtTicks;
        this.curseNineRequiredFoodTypes = curseNineRequiredFoodTypes;
        this.curseNineRemainingFoodTypes = curseNineRemainingFoodTypes;
        this.lockedSlot = lockedSlot;
        this.lockedSlotTicks = lockedSlotTicks;
        this.dynastyThreat = dynastyThreat;
        this.dynastySilenceMainHandTicks = dynastySilenceMainHandTicks;
        this.dynastySilenceOffHandTicks = dynastySilenceOffHandTicks;
    }

    public static void encode(GuiltSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.guilt);
        buffer.writeVarInt(packet.mercy);
        buffer.writeBoolean(packet.mercyChargeReady);
        buffer.writeBoolean(packet.sealEquipped);
        buffer.writeVarInt(packet.sealEquipSeconds);
        buffer.writeBoolean(packet.netherAwakened);
        buffer.writeBoolean(packet.endAwakened);
        buffer.writeBoolean(packet.curseOneReversed);
        buffer.writeBoolean(packet.curseTwoReversed);
        buffer.writeBoolean(packet.curseFiveReversed);
        buffer.writeBoolean(packet.curseFiveDeclined);
        buffer.writeBoolean(packet.curseSevenKeywordUnlocked);
        buffer.writeBoolean(packet.curseSevenTerminalSolved);
        buffer.writeBoolean(packet.curseSixReversed);
        buffer.writeBoolean(packet.curseSevenReversed);
        buffer.writeBoolean(packet.curseSevenSentenceUnlocked);
        buffer.writeBoolean(packet.curseEightReversed);
        buffer.writeBoolean(packet.curseNineReversed);
        buffer.writeVarInt(packet.lockTicks);
        buffer.writeVarInt(packet.satietyCrystalCount);
        buffer.writeVarInt(packet.curseNineDebtTicks);
        buffer.writeVarInt(packet.curseNineRequiredFoodTypes);
        buffer.writeVarInt(packet.curseNineRemainingFoodTypes);
        buffer.writeVarInt(packet.lockedSlot);
        buffer.writeVarInt(packet.lockedSlotTicks);
        buffer.writeVarInt(packet.dynastyThreat);
        buffer.writeVarInt(packet.dynastySilenceMainHandTicks);
        buffer.writeVarInt(packet.dynastySilenceOffHandTicks);
    }

    public static GuiltSyncPacket decode(FriendlyByteBuf buffer) {
        int guilt = buffer.readVarInt();
        int mercy = buffer.readVarInt();
        boolean mercyChargeReady = buffer.readBoolean();
        boolean sealEquipped = buffer.readBoolean();
        int sealEquipSeconds = buffer.readVarInt();
        boolean netherAwakened = buffer.readBoolean();
        boolean endAwakened = buffer.readBoolean();
        boolean curseOneReversed = buffer.readBoolean();
        boolean curseTwoReversed = buffer.readBoolean();
        boolean curseFiveReversed = buffer.readBoolean();
        boolean curseFiveDeclined = buffer.readBoolean();
        boolean curseSevenKeywordUnlocked = buffer.readBoolean();
        boolean curseSevenTerminalSolved = buffer.readBoolean();
        boolean curseSixReversed = buffer.readBoolean();
        boolean curseSevenReversed = buffer.readBoolean();
        boolean curseSevenSentenceUnlocked = buffer.readBoolean();
        boolean curseEightReversed = buffer.readBoolean();
        boolean curseNineReversed = buffer.readBoolean();
        int lockTicks = buffer.readVarInt();
        int satietyCrystalCount = buffer.readVarInt();
        int curseNineDebtTicks = buffer.readVarInt();
        int curseNineRequiredFoodTypes = buffer.readVarInt();
        int curseNineRemainingFoodTypes = buffer.readVarInt();
        int lockedSlot = buffer.readVarInt();
        int lockedSlotTicks = buffer.readVarInt();
        int dynastyThreat = buffer.readVarInt();
        int dynastySilenceMainHandTicks = buffer.readVarInt();
        int dynastySilenceOffHandTicks = buffer.readVarInt();
        return new GuiltSyncPacket(
                guilt, mercy, mercyChargeReady, sealEquipped, sealEquipSeconds, netherAwakened, endAwakened,
                curseOneReversed, curseTwoReversed, curseFiveReversed, curseFiveDeclined,
                curseSevenKeywordUnlocked, curseSevenTerminalSolved, curseSixReversed, curseSevenReversed,
                curseSevenSentenceUnlocked, curseEightReversed, curseNineReversed, lockTicks,
                satietyCrystalCount, curseNineDebtTicks, curseNineRequiredFoodTypes, curseNineRemainingFoodTypes,
                lockedSlot, lockedSlotTicks, dynastyThreat,
                dynastySilenceMainHandTicks, dynastySilenceOffHandTicks);
    }

    public static void handle(GuiltSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ModEvents.ClientOverlayEvents.updateClientGuilt(
                        packet.guilt,
                        packet.mercy,
                        packet.mercyChargeReady,
                        packet.sealEquipped,
                        packet.sealEquipSeconds,
                        packet.netherAwakened,
                        packet.endAwakened,
                        packet.curseOneReversed,
                        packet.curseTwoReversed,
                        packet.curseFiveReversed,
                        packet.curseFiveDeclined,
                        packet.curseSevenKeywordUnlocked,
                        packet.curseSevenTerminalSolved,
                        packet.curseSixReversed,
                        packet.curseSevenReversed,
                        packet.curseSevenSentenceUnlocked,
                        packet.curseEightReversed,
                        packet.curseNineReversed,
                        packet.lockTicks,
                        packet.satietyCrystalCount,
                        packet.curseNineDebtTicks,
                        packet.curseNineRequiredFoodTypes,
                        packet.curseNineRemainingFoodTypes,
                        packet.lockedSlot,
                        packet.lockedSlotTicks,
                        packet.dynastyThreat,
                        packet.dynastySilenceMainHandTicks,
                        packet.dynastySilenceOffHandTicks)));
        context.setPacketHandled(true);
    }
}

