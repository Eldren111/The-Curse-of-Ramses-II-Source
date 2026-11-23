package com.eldren.wutao.ramesses_curse;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID, value = Dist.CLIENT)
public final class KadeshCurioTooltip {
    private KadeshCurioTooltip() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTooltip(ItemTooltipEvent event) {
        if (!KadeshCurioAccess.requiresSeal(event.getItemStack())) {
            return;
        }
        var lines = event.getToolTip();
        if (event.getEntity() != null && !KadeshCurioAccess.isWearingSeal(event.getEntity())) {
            for (int index = 1; index < lines.size(); index++) {
                String text = lines.get(index).getString();
                if (!text.isBlank()) {
                    // ASCII obfuscates reliably with Chinese fonts and removes dynamic-text markers.
                    int length = Math.min(48, Math.max(8, text.codePointCount(0, text.length())));
                    lines.set(index, Component.literal("x".repeat(length))
                            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.OBFUSCATED));
                }
            }
        }
        lines.add(Component.translatable("tooltip.ramesses_curse.requires_kadesh")
                .withStyle(ChatFormatting.GOLD));
    }
}
