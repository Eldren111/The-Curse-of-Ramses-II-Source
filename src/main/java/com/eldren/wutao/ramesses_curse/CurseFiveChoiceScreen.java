package com.eldren.wutao.ramesses_curse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CurseFiveChoiceScreen extends Screen {

    public CurseFiveChoiceScreen() {
        super(Component.translatable("screen.ramesses_curse.curse_5_choice.title"));
    }

    @Override
    protected void init() {
        super.init();
        int buttonWidth = 110;
        int buttonHeight = 20;
        int gap = 12;
        int totalWidth = buttonWidth * 2 + gap;
        int startX = (this.width - totalWidth) / 2;
        int y = this.height / 2 + 26;

        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.ramesses_curse.curse_5_choice.reverse"),
                button -> submit(true))
                .bounds(startX, y, buttonWidth, buttonHeight)
                .build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.ramesses_curse.curse_5_choice.decline"),
                button -> submit(false))
                .bounds(startX + buttonWidth + gap, y, buttonWidth, buttonHeight)
                .build());
    }

    private void submit(boolean reverse) {
        ModNetwork.CHANNEL.sendToServer(new CurseFiveChoiceResponsePacket(reverse));
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        int panelWidth = 278;
        int panelHeight = 118;
        int x = (this.width - panelWidth) / 2;
        int y = (this.height - panelHeight) / 2;

        guiGraphics.fill(x - 1, y - 1, x + panelWidth + 1, y + panelHeight + 1, 0xCC2A110A);
        guiGraphics.fill(x, y, x + panelWidth, y + panelHeight, 0xF0180D09);

        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, y + 14, 0xFFE7C77A);
        drawCenteredWrapped(guiGraphics, Component.translatable("screen.ramesses_curse.curse_5_choice.body_1"), this.width / 2, y + 36, 240, 0xFFD8D0C6);
        drawCenteredWrapped(guiGraphics, Component.translatable("screen.ramesses_curse.curse_5_choice.body_2"), this.width / 2, y + 56, 240, 0xFFBFAE9B);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void drawCenteredWrapped(GuiGraphics guiGraphics, Component text, int centerX, int y, int width, int color) {
        for (net.minecraft.util.FormattedCharSequence line : this.font.split(text, width)) {
            guiGraphics.drawCenteredString(this.font, line, centerX, y, color);
            y += this.font.lineHeight + 1;
        }
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
