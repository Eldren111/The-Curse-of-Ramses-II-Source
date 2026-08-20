package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class OpenCompoundBowAmmoPacket {
    public static void encode(OpenCompoundBowAmmoPacket packet, FriendlyByteBuf buffer) {
    }

    public static OpenCompoundBowAmmoPacket decode(FriendlyByteBuf buffer) {
        return new OpenCompoundBowAmmoPacket();
    }

    public static void handle(OpenCompoundBowAmmoPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || player.isUsingItem()) {
                return;
            }
            InteractionHand hand = getHoldingHand(player);
            if (hand == null) {
                return;
            }
            ItemStack bow = player.getItemInHand(hand);
            player.openMenu(
                    new SimpleMenuProvider(
                            (containerId, inventory, ignored) -> new CompoundBowAmmoMenu(containerId, inventory, bow, hand),
                            Component.translatable("screen.ramesses_curse.compound_bow_ammo")),
                    buffer -> buffer.writeEnum(hand));
        });
        context.setPacketHandled(true);
    }

    private static InteractionHand getHoldingHand(ServerPlayer player) {
        if (ModItems.EternalSilenceCompoundBow.isCompoundBow(player.getMainHandItem())) {
            return InteractionHand.MAIN_HAND;
        }
        return ModItems.EternalSilenceCompoundBow.isCompoundBow(player.getOffhandItem())
                ? InteractionHand.OFF_HAND
                : null;
    }
}

