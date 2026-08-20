package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class ToggleCompoundBowModePacket {
    public static void encode(ToggleCompoundBowModePacket packet, FriendlyByteBuf buffer) {
    }

    public static ToggleCompoundBowModePacket decode(FriendlyByteBuf buffer) {
        return new ToggleCompoundBowModePacket();
    }

    public static void handle(ToggleCompoundBowModePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || player.isUsingItem()) {
                return;
            }
            ItemStack bow = findHeldBow(player);
            if (bow.isEmpty()) {
                return;
            }
            boolean rapidFire = ModItems.EternalSilenceCompoundBow.toggleFireMode(bow);
            player.displayClientMessage(Component.translatable(
                    rapidFire
                            ? "message.ramesses_curse.compound_bow_mode.rapid"
                            : "message.ramesses_curse.compound_bow_mode.triple"), true);
        });
        context.setPacketHandled(true);
    }

    private static ItemStack findHeldBow(ServerPlayer player) {
        if (ModItems.EternalSilenceCompoundBow.isCompoundBow(player.getMainHandItem())) {
            return player.getMainHandItem();
        }
        if (ModItems.EternalSilenceCompoundBow.isCompoundBow(player.getOffhandItem())) {
            return player.getOffhandItem();
        }
        return ItemStack.EMPTY;
    }
}

