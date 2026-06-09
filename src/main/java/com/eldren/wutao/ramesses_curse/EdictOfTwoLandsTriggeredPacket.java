package com.eldren.wutao.ramesses_curse;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EdictOfTwoLandsTriggeredPacket {

    public static void encode(EdictOfTwoLandsTriggeredPacket packet, net.minecraft.network.FriendlyByteBuf buffer) {
    }

    public static EdictOfTwoLandsTriggeredPacket decode(net.minecraft.network.FriendlyByteBuf buffer) {
        return new EdictOfTwoLandsTriggeredPacket();
    }

    public static void handle(EdictOfTwoLandsTriggeredPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null || minecraft.gameRenderer == null) {
                return;
            }

            minecraft.gameRenderer.displayItemActivation(new ItemStack(ModItems.EDICT_OF_TWO_LANDS.get()));
            minecraft.player.displayClientMessage(
                    Component.translatable("message.ramesses_curse.edict_cast").withStyle(net.minecraft.ChatFormatting.GOLD),
                    true);
        });
        context.setPacketHandled(true);
    }
}
