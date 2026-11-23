package com.eldren.wutao.ramesses_curse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CurseFiveChoiceScreen extends Screen {

    public CurseFiveChoiceScreen() {
        super(Component.translatable("screen.ramesses_curse.curse_5_choice.title"));
    }

    @Override
    protected void init() {
        super.init();
        int panelWidth = panelWidth();
        int panelHeight = panelHeight();
        int panelY = (this.height - panelHeight) / 2;
        int buttonWidth = Math.max(72, (panelWidth - 40) / 2);
        int buttonHeight = 20;
        int gap = 8;
        int totalWidth = buttonWidth * 2 + gap;
        int startX = (this.width - totalWidth) / 2;
        int y = panelY + panelHeight - 30;

        this.addRenderableWidget(DuatRitualGui.button(
                Component.translatable("screen.ramesses_curse.curse_5_choice.reverse"),
                button -> submit(true), startX, y, buttonWidth, buttonHeight));

        this.addRenderableWidget(DuatRitualGui.button(
                Component.translatable("screen.ramesses_curse.curse_5_choice.decline"),
                button -> submit(false), startX + buttonWidth + gap, y, buttonWidth, buttonHeight));
    }

    private void submit(boolean reverse) {
        ModNetwork.CHANNEL.sendToServer(new CurseFiveChoiceResponsePacket(reverse));
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        int panelWidth = panelWidth();
        int panelHeight = panelHeight();
        int x = (this.width - panelWidth) / 2;
        int y = (this.height - panelHeight) / 2;

        DuatRitualGui.panel(guiGraphics, x, y, panelWidth, panelHeight, true);

        DuatRitualGui.fittedText(guiGraphics, this.title, x + 16, y + 12, panelWidth - 32, 16, 0xFFF0D39A);
        drawCenteredWrapped(guiGraphics, Component.translatable("screen.ramesses_curse.curse_5_choice.body_1"), this.width / 2, y + 39, panelWidth - 32, 0xFFD8D0C6);
        drawCenteredWrapped(guiGraphics, Component.translatable("screen.ramesses_curse.curse_5_choice.body_2"), this.width / 2, y + 70, panelWidth - 32, 0xFFBFAE9B);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void drawCenteredWrapped(GuiGraphics guiGraphics, Component text, int centerX, int y, int width, int color) {
        for (net.minecraft.util.FormattedCharSequence line : this.font.split(text, width)) {
            guiGraphics.drawCenteredString(this.font, line, centerX, y, color);
            y += this.font.lineHeight + 1;
        }
    }

    private int panelWidth() {
        return Math.max(220, Math.min(360, this.width - 16));
    }

    private int panelHeight() {
        return Math.max(128, Math.min(154, this.height - 12));
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
