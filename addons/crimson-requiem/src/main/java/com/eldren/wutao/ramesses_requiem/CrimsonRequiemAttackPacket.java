package com.eldren.wutao.ramesses_requiem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/** Only intent is sent. Ownership, stance, cooldown, damage and aim are server-authoritative. */
public record CrimsonRequiemAttackPacket(boolean burst, boolean use) {
    public CrimsonRequiemAttackPacket(boolean burst) { this(burst, false); }
    public static void encode(CrimsonRequiemAttackPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.burst); buffer.writeBoolean(packet.use);
    }
    public static CrimsonRequiemAttackPacket decode(FriendlyByteBuf buffer) {
        return new CrimsonRequiemAttackPacket(buffer.readBoolean(), buffer.readBoolean());
    }
    public static void handle(CrimsonRequiemAttackPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            var player = context.getSender();
            if (player == null || !CrimsonRequiemActions.canUse(player)) return;
            if (packet.use) CrimsonRequiemActions.rightClick(player);
            else if (packet.burst) CrimsonRequiemActions.burst(player);
            else if (!player.isShiftKeyDown()) CrimsonRequiemActions.leftClick(player);
        });
        context.setPacketHandled(true);
    }
}
