package com.eldren.wutao.ramesses_requiem.boss;

import com.eldren.wutao.ramesses_requiem.RequiemNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.*;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.*;
import java.util.function.Supplier;

/** S2C only: 0 camera lock, 1 black transition, 2 release, 3 freeze, 4 necklace burst. */
public record WusheScenePacket(int kind, int bossId, int ticks, Vec3 point) {
    public static void encode(WusheScenePacket p, FriendlyByteBuf b) { b.writeVarInt(p.kind); b.writeInt(p.bossId); b.writeVarInt(p.ticks); b.writeDouble(p.point.x); b.writeDouble(p.point.y); b.writeDouble(p.point.z); }
    public static WusheScenePacket decode(FriendlyByteBuf b) { return new WusheScenePacket(b.readVarInt(), b.readInt(), b.readVarInt(), new Vec3(b.readDouble(), b.readDouble(), b.readDouble())); }
    public static void handle(WusheScenePacket p, Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> WusheClient.receive(p)));
        supplier.get().setPacketHandled(true);
    }
    public static void send(ServerPlayer player, WusheScenePacket p) { RequiemNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), p); }
    public static void burst(ServerLevel level, Vec3 point) {
        RequiemNetwork.CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(point.x, point.y, point.z, 64, level.dimension())), new WusheScenePacket(4, -1, 12, point));
    }
}
