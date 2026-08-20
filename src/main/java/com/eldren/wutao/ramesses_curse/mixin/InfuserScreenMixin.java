package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.ModEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "fuzs.enchantinginfuser.client.gui.screens.inventory.InfuserScreen")
public abstract class InfuserScreenMixin extends AbstractContainerScreen<AbstractContainerMenu> {

    @Shadow(remap = false)
    protected abstract void renderReadableText(GuiGraphics guiGraphics, int posX, int posY, String text, int color);

    @Shadow(remap = false)
    private Button enchantButton;

    private InfuserScreenMixin() {
        super((AbstractContainerMenu) null, (Inventory) null, Component.empty());
    }

    @Inject(method = "renderEnchantButtonCost", at = @At("TAIL"), remap = false)
    private void ramessesCurse$renderInfuserExtraCost(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player == null || this.enchantButton == null) {
            return;
        }

        Object menu = this.menu;
        int vanillaCost = ramessesCurse$getInfuserCost(menu);
        if (vanillaCost <= 0) {
            return;
        }

        int extraCost = ModEvents.getCurseThreeEnchantExtraCost(player, vanillaCost);
        if (extraCost <= 0) {
            return;
        }

        this.renderReadableText(guiGraphics, this.enchantButton.getX(), this.enchantButton.getY() + 9, "+" + extraCost, 0xFFFF5555);
        if (mouseX >= this.enchantButton.getX()
                && mouseY >= this.enchantButton.getY()
                && mouseX < this.enchantButton.getX() + 18
                && mouseY < this.enchantButton.getY() + 18) {
            java.util.List<FormattedCharSequence> tooltip = this.font.split(ModEvents.getCurseThreeEnchantTooltip(player, vanillaCost), 180);
            this.setTooltipForNextRenderPass(tooltip);
        }
    }

    private static int ramessesCurse$getInfuserCost(Object menu) {
        try {
            return (int) menu.getClass().getMethod("getEnchantCost").invoke(menu);
        } catch (ReflectiveOperationException ignored) {
            return 0;
        }
    }
}
