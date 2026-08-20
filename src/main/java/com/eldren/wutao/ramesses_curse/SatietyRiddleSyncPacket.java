package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SatietyRiddleSyncPacket {
    private final List<String> round;
    private final int index;
    private final List<Boolean> solved;
    private final boolean openScreen;

    public SatietyRiddleSyncPacket(List<String> round, int index, List<Boolean> solved, boolean openScreen) {
        this.round = round;
        this.index = index;
        this.solved = solved;
        this.openScreen = openScreen;
    }

    public static void encode(SatietyRiddleSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.round.size());
        packet.round.forEach(buffer::writeUtf);
        buffer.writeVarInt(packet.index);
        buffer.writeVarInt(packet.solved.size());
        packet.solved.forEach(value -> buffer.writeBoolean(Boolean.TRUE.equals(value)));
        buffer.writeBoolean(packet.openScreen);
    }

    public static SatietyRiddleSyncPacket decode(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<String> round = new ArrayList<>(size);
        for (int i = 0; i < size; i++) round.add(buffer.readUtf());
        int index = buffer.readVarInt();
        int solvedSize = buffer.readVarInt();
        List<Boolean> solved = new ArrayList<>(solvedSize);
        for (int i = 0; i < solvedSize; i++) solved.add(buffer.readBoolean());
        return new SatietyRiddleSyncPacket(round, index, solved, buffer.readBoolean());
    }

    public static void handle(SatietyRiddleSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ModEvents.ClientOverlayEvents.updateSatietyRiddleRound(
                        packet.round, packet.index, packet.solved, packet.openScreen)));
        context.setPacketHandled(true);
    }
}

