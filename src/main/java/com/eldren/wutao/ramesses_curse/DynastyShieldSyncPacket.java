package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record DynastyShieldSyncPacket(int entityId, float current, float max) {
    public static void encode(DynastyShieldSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.entityId);
        buf.writeFloat(packet.current);
        buf.writeFloat(packet.max);
    }
    public static DynastyShieldSyncPacket decode(FriendlyByteBuf buf) {
        return new DynastyShieldSyncPacket(buf.readVarInt(), buf.readFloat(), buf.readFloat());
    }
    public static void handle(DynastyShieldSyncPacket packet, Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> DynastyShieldVisuals.sync(packet)));
        context.setPacketHandled(true);
    }
}

