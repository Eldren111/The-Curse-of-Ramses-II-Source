package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.Config;
import com.eldren.wutao.ramesses_curse.ModEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.xiaoyue.celestial_enchantments.content.table.CelestialTableScreen")
public abstract class CelestialTableScreenMixin extends AbstractContainerScreen<AbstractContainerMenu> {
    private CelestialTableScreenMixin() {
        super((AbstractContainerMenu) null, (Inventory) null, Component.empty());
    }

    @Inject(method = {"renderBg", "m_7286_"}, at = @At("TAIL"), remap = false, require = 0)
    private void ramessesCurse$renderCelestialEnchantCost(GuiGraphics guiGraphics, float partialTick,
                                                          int mouseX, int mouseY, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        int[] costs = ramessesCurse$getCelestialCosts(this.menu);
        int hovered = ramessesCurse$getHoveredOption(mouseX, mouseY);
        int sampleCost = hovered >= 0 && hovered < costs.length
                ? Math.max(0, costs[hovered] * (hovered + 1))
                : ramessesCurse$firstPositiveRequirement(costs);
        if (sampleCost <= 0
                || ModEvents.getAdjustedEnchantLevelCost(player, sampleCost) <= sampleCost) {
            return;
        }

        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        Component multiplier = Component.translatable(
                "screen.ramesses_curse.celestial_enchant_multiplier",
                ramessesCurse$formatMultiplier(Config.CURSE_3_ENCHANT_COST_MULTIPLIER.get()));
        guiGraphics.drawString(this.font, multiplier, left, top - 20, 0xFFD6A63A, false);

        if (hovered < 0 || hovered >= costs.length || costs[hovered] <= 0) {
            return;
        }

        int baseRequirement = costs[hovered] * (hovered + 1);
        int adjustedRequirement = ModEvents.getAdjustedEnchantLevelCost(player, baseRequirement);
        int baseSpend = (hovered + 1) * 10;
        int adjustedSpend = ModEvents.getAdjustedEnchantSpend(player, baseSpend);
        Component details = Component.translatable(
                "screen.ramesses_curse.celestial_enchant_cost",
                baseRequirement,
                adjustedRequirement,
                baseSpend,
                adjustedSpend);
        guiGraphics.drawString(this.font, details, left, top - 10, 0xFFFFD166, false);
    }

    private static int[] ramessesCurse$getCelestialCosts(Object menu) {
        for (Class<?> type = menu.getClass(); type != null; type = type.getSuperclass()) {
            try {
                java.lang.reflect.Field field = type.getDeclaredField("costs");
                field.setAccessible(true);
                Object value = field.get(menu);
                if (value instanceof int[] costs) {
                    return costs;
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return new int[0];
    }

    private int ramessesCurse$getHoveredOption(int mouseX, int mouseY) {
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        for (int option = 0; option < 3; option++) {
            int x = left + 60;
            int y = top + 14 + 19 * option;
            if (mouseX >= x && mouseX < x + 108 && mouseY >= y && mouseY < y + 19) {
                return option;
            }
        }
        return -1;
    }

    private static int ramessesCurse$firstPositiveRequirement(int[] costs) {
        for (int option = 0; option < costs.length; option++) {
            if (costs[option] > 0) {
                return costs[option] * (option + 1);
            }
        }
        return 0;
    }

    private static String ramessesCurse$formatMultiplier(double multiplier) {
        return multiplier == Math.rint(multiplier)
                ? Integer.toString((int) multiplier)
                : Double.toString(multiplier);
    }
}
