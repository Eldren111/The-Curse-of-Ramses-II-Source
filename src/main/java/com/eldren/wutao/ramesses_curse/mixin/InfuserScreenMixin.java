package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.ModEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "fuzs.enchantinginfuser.client.gui.screens.inventory.InfuserScreen")
public abstract class InfuserScreenMixin extends AbstractContainerScreen<AbstractContainerMenu> {

    private InfuserScreenMixin() {
        super((AbstractContainerMenu) null, (Inventory) null, Component.empty());
    }

    @Inject(method = "renderEnchantButtonCost", at = @At("TAIL"), remap = false, require = 0)
    private void ramessesCurse$renderInfuserExtraCost(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        Object button = ramessesCurse$getField(this, "enchantButton");
        if (player == null || button == null) {
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

        int buttonX = ramessesCurse$getButtonCoordinate(button, "getX");
        int buttonY = ramessesCurse$getButtonCoordinate(button, "getY");
        guiGraphics.drawString(this.font, "+" + extraCost, buttonX, buttonY + 9, 0xFFFF5555, false);
        if (mouseX >= buttonX && mouseY >= buttonY
                && mouseX < buttonX + 18 && mouseY < buttonY + 18) {
            java.util.List<FormattedCharSequence> tooltip = this.font.split(ModEvents.getCurseThreeEnchantTooltip(player, vanillaCost), 180);
            this.setTooltipForNextRenderPass(tooltip);
        }
    }

    private static Object ramessesCurse$getField(Object target, String name) {
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            try {
                java.lang.reflect.Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(target);
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    private static int ramessesCurse$getButtonCoordinate(Object button, String methodName) {
        try {
            return ((Number) button.getClass().getMethod(methodName).invoke(button)).intValue();
        } catch (ReflectiveOperationException | ClassCastException ignored) {
            return 0;
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
