package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.ModEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EnchantmentScreen.class)
public class EnchantmentScreenMixin {

    @Redirect(
            method = "renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I"))
    private int ramessesCurse$drawEnchantCostWithExtra(GuiGraphics guiGraphics, Font font, String text, int x, int y, int color) {
        int width = guiGraphics.drawString(font, text, x, y, color);
        Player player = Minecraft.getInstance().player;
        EnchantmentScreen screen = (EnchantmentScreen) (Object) this;

        if (player == null || screen.getClass() != EnchantmentScreen.class) {
            return width;
        }

        int vanillaCost = parseCost(text);
        int extraCost = ModEvents.getCurseThreeEnchantExtraCost(player, vanillaCost);

        if (extraCost > 0) {
            guiGraphics.drawString(font, "+" + extraCost, x + font.width(text) + 2, y, 0xFFFF5555);
        }
        return width;
    }

    private static int parseCost(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
