package com.eldren.wutao.ramesses_curse;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CompoundBowAmmoScreen extends AbstractContainerScreen<CompoundBowAmmoMenu> {
    public CompoundBowAmmoScreen(CompoundBowAmmoMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 148;
        inventoryLabelY = 54;
        titleLabelX = 8;
        titleLabelY = 7;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = leftPos;
        int top = topPos;
        DuatRitualGui.panel(graphics, left, top, imageWidth, imageHeight, false);

        graphics.fill(left + 5, top + 15, left + imageWidth - 5, top + 53, 0xFF111821);
        drawBorder(graphics, left + 5, top + 15, imageWidth - 10, 38, 0xFF273746, 0xFF8AA0AD);
        drawSlot(graphics, left + 79, top + 25, 0xFF6A4B1B, 0xFF21170D);

        graphics.fill(left + 5, top + 61, left + imageWidth - 5, top + imageHeight - 5, 0xFF111821);
        drawBorder(graphics, left + 5, top + 61, imageWidth - 10, imageHeight - 66, 0xFF273746, 0xFF8AA0AD);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlot(graphics, left + 7 + column * 18, top + 65 + row * 18, 0xFF587183, 0xFF18222B);
            }
        }
        for (int column = 0; column < 9; column++) {
            drawSlot(graphics, left + 7 + column * 18, top + 123, 0xFF587183, 0xFF18222B);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        DuatRitualGui.fittedText(graphics, title, titleLabelX, titleLabelY - 2, imageWidth - 16, 12, 0xFFE2B860);
        DuatRitualGui.fittedText(graphics, playerInventoryTitle, inventoryLabelX, inventoryLabelY - 2, imageWidth - 16, 12, 0xFFBDAFC2);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private static void drawSlot(GuiGraphics graphics, int x, int y, int border, int fill) {
        graphics.fill(x, y, x + 18, y + 18, border);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, fill);
        graphics.fill(x + 2, y + 2, x + 16, y + 16, 0x66000000);
    }

    private static void drawBorder(GuiGraphics graphics, int x, int y, int width, int height, int outer, int inner) {
        graphics.fill(x, y, x + width, y + 1, outer);
        graphics.fill(x, y + height - 1, x + width, y + height, outer);
        graphics.fill(x, y, x + 1, y + height, outer);
        graphics.fill(x + width - 1, y, x + width, y + height, outer);
        graphics.fill(x + 2, y + 2, x + width - 2, y + 3, inner);
    }
}
