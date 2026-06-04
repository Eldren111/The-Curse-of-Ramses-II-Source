package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class HeartStateSyncPacket {

    private final int battleStacks;
    private final int cooldownTicks;
    private final boolean wardReady;

    public HeartStateSyncPacket(int battleStacks, int cooldownTicks, boolean wardReady) {
        this.battleStacks = battleStacks;
        this.cooldownTicks = cooldownTicks;
        this.wardReady = wardReady;
    }

    public static void encode(HeartStateSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.battleStacks);
        buffer.writeVarInt(packet.cooldownTicks);
        buffer.writeBoolean(packet.wardReady);
    }

    public static HeartStateSyncPacket decode(FriendlyByteBuf buffer) {
        return new HeartStateSyncPacket(buffer.readVarInt(), buffer.readVarInt(), buffer.readBoolean());
    }

    public static void handle(HeartStateSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ModEvents.ClientOverlayEvents.updateHeartState(packet.battleStacks, packet.cooldownTicks, packet.wardReady)));
        context.setPacketHandled(true);
    }
}
