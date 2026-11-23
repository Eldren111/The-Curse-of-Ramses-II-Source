package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ApepBattleMusicPacket(boolean playing, boolean immediateStop) {
    public static void encode(ApepBattleMusicPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.playing());
        buffer.writeBoolean(packet.immediateStop());
    }

    public static ApepBattleMusicPacket decode(FriendlyByteBuf buffer) {
        return new ApepBattleMusicPacket(buffer.readBoolean(), buffer.readBoolean());
    }

    public static void handle(ApepBattleMusicPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ApepBattleMusicClient.update(packet.playing(), packet.immediateStop())));
        context.setPacketHandled(true);
    }
}
