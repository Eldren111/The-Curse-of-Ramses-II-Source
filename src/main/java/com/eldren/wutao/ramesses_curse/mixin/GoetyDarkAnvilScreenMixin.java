package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.ModEvents;
import com.eldren.wutao.ramesses_curse.integration.goety.GoetyDarkAnvilMenuAccess;
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
@Mixin(targets = "com.Polarice3.Goety.client.gui.screen.inventory.DarkAnvilScreen")
public abstract class GoetyDarkAnvilScreenMixin extends AbstractContainerScreen<AbstractContainerMenu> {
    private GoetyDarkAnvilScreenMixin() {
        super((AbstractContainerMenu) null, (Inventory) null, Component.empty());
    }

    @Inject(method = {"renderLabels", "m_280003_"}, at = @At("TAIL"), remap = false, require = 0)
    private void ramessesCurse$renderDarkAnvilCost(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player == null || !(this.menu instanceof GoetyDarkAnvilMenuAccess darkAnvilMenu)) {
            return;
        }

        int actualCost = Math.max(0, darkAnvilMenu.ramessesCurse$getDarkAnvilCost());
        int baseCost = ModEvents.getDisplayedAnvilBaseCost(player, actualCost);
        int extraCost = Math.max(0, actualCost - baseCost);
        if (actualCost <= 0 || extraCost <= 0) {
            return;
        }

        Component text = Component.translatable(
                "screen.ramesses_curse.goety_dark_anvil_cost",
                baseCost,
                extraCost,
                actualCost);
        int x = (this.imageWidth - this.font.width(text)) / 2;
        guiGraphics.drawString(this.font, text, x, -12, 0xFFFFD166, false);
    }
}
