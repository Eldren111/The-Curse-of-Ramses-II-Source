package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class EmberRiddleSyncPacket {

    private final List<String> round;
    private final int index;
    private final boolean unlocked;
    private final List<Boolean> solved;

    public EmberRiddleSyncPacket(List<String> round, int index, boolean unlocked, List<Boolean> solved) {
        this.round = round;
        this.index = index;
        this.unlocked = unlocked;
        this.solved = solved;
    }

    public static void encode(EmberRiddleSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.round.size());
        for (String id : packet.round) {
            buffer.writeUtf(id);
        }
        buffer.writeVarInt(packet.index);
        buffer.writeBoolean(packet.unlocked);
        buffer.writeVarInt(packet.solved.size());
        for (Boolean value : packet.solved) {
            buffer.writeBoolean(Boolean.TRUE.equals(value));
        }
    }

    public static EmberRiddleSyncPacket decode(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<String> round = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            round.add(buffer.readUtf());
        }
        int index = buffer.readVarInt();
        boolean unlocked = buffer.readBoolean();
        int solvedSize = buffer.readVarInt();
        List<Boolean> solved = new ArrayList<>(solvedSize);
        for (int i = 0; i < solvedSize; i++) {
            solved.add(buffer.readBoolean());
        }
        return new EmberRiddleSyncPacket(round, index, unlocked, solved);
    }

    public static void handle(EmberRiddleSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ModEvents.ClientOverlayEvents.updateEmberRiddleRound(packet.round, packet.index, packet.unlocked, packet.solved)));
        context.setPacketHandled(true);
    }
}

