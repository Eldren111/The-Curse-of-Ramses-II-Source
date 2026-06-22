package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EdictOfTwoLandsDomainActivatePacket {

    public static void encode(EdictOfTwoLandsDomainActivatePacket packet, net.minecraft.network.FriendlyByteBuf buffer) {
    }

    public static EdictOfTwoLandsDomainActivatePacket decode(net.minecraft.network.FriendlyByteBuf buffer) {
        return new EdictOfTwoLandsDomainActivatePacket();
    }

    public static void handle(EdictOfTwoLandsDomainActivatePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            if (!ModItems.isEdictOfTwoLandsEquipped(player)) {
                return;
            }

            ModItems.EdictActivationResult result = ModItems.tryActivateEdictOfTwoLandsDomain(player);
            switch (result) {
                case COOLDOWN -> {
                    int cooldownTicks = ModItems.getEdictOfTwoLandsDomainCooldownTicks(player);
                    if (cooldownTicks > 0) {
                        player.displayClientMessage(
                                Component.translatable(
                                                "message.ramesses_curse.edict_domain_cooldown",
                                                String.format(java.util.Locale.ROOT, "%.1f", cooldownTicks / 20.0F))
                                        .withStyle(net.minecraft.ChatFormatting.RED),
                                true);
                    }
                }
                case NO_GUILT ->
                        player.displayClientMessage(Component.translatable("message.ramesses_curse.edict_no_guilt")
                                .withStyle(net.minecraft.ChatFormatting.RED), true);
                case NO_TARGETS ->
                        player.displayClientMessage(Component.translatable("message.ramesses_curse.edict_domain_no_targets")
                                .withStyle(net.minecraft.ChatFormatting.RED), true);
                case OTHER_MODE_ACTIVE ->
                        player.displayClientMessage(Component.translatable("message.ramesses_curse.edict_other_mode_active")
                                .withStyle(net.minecraft.ChatFormatting.RED), true);
                default -> {
                }
            }
        });
        context.setPacketHandled(true);
    }
}
