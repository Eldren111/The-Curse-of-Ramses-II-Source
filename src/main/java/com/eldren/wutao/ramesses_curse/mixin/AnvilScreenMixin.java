package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.ModEvents;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AnvilScreen.class)
public class AnvilScreenMixin {
    @Shadow @Final private Player player;

    @Redirect(
            method = "renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AnvilMenu;getCost()I"))
    private int ramessesCurse$showAnvilBaseCost(net.minecraft.world.inventory.AnvilMenu menu) {
        return Math.max(0, ModEvents.getDisplayedAnvilBaseCost(this.player, menu.getCost()));
    }

    @Redirect(
            method = "renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I"))
    private int ramessesCurse$drawAnvilCostWithExtra(GuiGraphics guiGraphics, Font font, Component component, int x, int y, int color) {
        int width = guiGraphics.drawString(font, component, x, y, color);
        int vanillaCost = ModEvents.getDisplayedAnvilBaseCost(this.player, parseCost(component.getString()));
        int extraCost = ModEvents.getCurseThreeAnvilExtraCost(this.player, vanillaCost);

        if (extraCost > 0 && component.getString().contains(Integer.toString(vanillaCost))) {
            guiGraphics.drawString(font, "+" + extraCost, x + font.width(component) + 2, y, 0xFFFF5555);
        }
        return width;
    }

    private static int parseCost(String text) {
        text = text.replaceAll("\\D+", "");
        if (text.isEmpty()) {
            return 0;
        }

        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
