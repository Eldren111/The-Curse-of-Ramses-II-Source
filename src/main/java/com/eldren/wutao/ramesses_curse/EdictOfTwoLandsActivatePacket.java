package com.eldren.wutao.ramesses_curse;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public class EdictOfTwoLandsActivatePacket {

    public static void encode(EdictOfTwoLandsActivatePacket packet, net.minecraft.network.FriendlyByteBuf buffer) {
    }

    public static EdictOfTwoLandsActivatePacket decode(net.minecraft.network.FriendlyByteBuf buffer) {
        return new EdictOfTwoLandsActivatePacket();
    }

    public static void handle(EdictOfTwoLandsActivatePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            if (!ModItems.isEdictOfTwoLandsEquipped(player)) {
                return;
            }

            if (!ModItems.tryActivateEdictOfTwoLands(player)) {
                int cooldownTicks = ModItems.getEdictOfTwoLandsCooldownTicks(player);
                if (cooldownTicks > 0) {
                    player.displayClientMessage(
                            Component.translatable("message.ramesses_curse.edict_cooldown", String.format(java.util.Locale.ROOT, "%.1f", cooldownTicks / 20.0F))
                                    .withStyle(ChatFormatting.RED),
                            true);
                }
            }
        });
        context.setPacketHandled(true);
    }
}

