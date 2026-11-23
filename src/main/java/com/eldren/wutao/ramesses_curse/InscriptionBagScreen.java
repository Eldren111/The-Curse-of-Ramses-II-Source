package com.eldren.wutao.ramesses_curse;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class InscriptionBagScreen extends AbstractContainerScreen<InscriptionBagMenu> {
    private static final int PANEL_COLOR = 0xFF0B1016;
    private static final int INNER_COLOR = 0xFF111821;
    private static final int GOLD_DARK = 0xFF273746;
    private static final int GOLD = 0xFF8AA0AD;
    private static final int INSCRIPTION_SLOT_BORDER = 0xFFB9C9CC;
    private static final int INSCRIPTION_SLOT_RIM = 0xFF60777C;
    private static final int INSCRIPTION_SLOT_FILL = 0xFF31464B;
    private static final int INSCRIPTION_SLOT_HIGHLIGHT = 0xFFE0EAEB;

    public InscriptionBagScreen(InscriptionBagMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 186;
        inventoryLabelY = 92;
        titleLabelX = 8;
        titleLabelY = 7;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = leftPos;
        int top = topPos;
        DuatRitualGui.panel(graphics, left, top, imageWidth, imageHeight, true);

        graphics.fill(left + 5, top + 15, left + imageWidth - 5, top + 91, INNER_COLOR);
        drawBorder(graphics, left + 5, top + 15, imageWidth - 10, 76, GOLD_DARK, GOLD);

        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 4; column++) {
                int x = left + 52 + column * 18;
                int y = top + 17 + row * 18;
                drawInscriptionSlot(graphics, x, y, row, column);
            }
        }

        graphics.fill(left + 5, top + 99, left + imageWidth - 5, top + imageHeight - 5, INNER_COLOR);
        drawBorder(graphics, left + 5, top + 99, imageWidth - 10, imageHeight - 104, GOLD_DARK, GOLD);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawVanillaSlot(graphics, left + 7 + column * 18, top + 103 + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            drawVanillaSlot(graphics, left + 7 + column * 18, top + 161);
        }
    }

    private static void drawInscriptionSlot(GuiGraphics graphics, int x, int y, int row, int column) {
        graphics.fill(x, y, x + 18, y + 18, INSCRIPTION_SLOT_BORDER);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, INSCRIPTION_SLOT_RIM);
        graphics.fill(x + 2, y + 2, x + 16, y + 16, INSCRIPTION_SLOT_FILL);
        int accent = (row + column) % 2 == 0
                ? INSCRIPTION_SLOT_HIGHLIGHT
                : INSCRIPTION_SLOT_BORDER;
        graphics.fill(x, y, x + 4, y + 1, accent);
        graphics.fill(x, y, x + 1, y + 4, accent);
        graphics.fill(x + 14, y + 17, x + 18, y + 18, accent);
        graphics.fill(x + 17, y + 14, x + 18, y + 18, accent);
    }

    private static void drawVanillaSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, 0xFF587183);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF18222B);
    }

    private static void drawBorder(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            int outer,
            int inner) {
        graphics.fill(x, y, x + width, y + 1, outer);
        graphics.fill(x, y + height - 1, x + width, y + height, outer);
        graphics.fill(x, y, x + 1, y + height, outer);
        graphics.fill(x + width - 1, y, x + width, y + height, outer);
        graphics.fill(x + 2, y + 2, x + width - 2, y + 3, inner);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        DuatRitualGui.fittedText(graphics, title, titleLabelX, titleLabelY - 2, imageWidth - 16, 12, 0xFFE6BE6A);
        DuatRitualGui.fittedText(graphics, playerInventoryTitle, inventoryLabelX, inventoryLabelY - 2, imageWidth - 16, 12, 0xFFBDAFC2);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
