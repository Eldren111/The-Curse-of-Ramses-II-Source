package com.eldren.wutao.ramesses_curse;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.sounds.SoundEvents;

public class ChanceDieLargeScreen extends Screen {
    private static final ResourceLocation[] OUTCOME_TEXTURES = {
            texture(1), texture(2), texture(3), texture(4), texture(5), texture(6)
    };
    private static final int COLUMNS = 6;
    private static final int DEFAULT_ANIMATION_TICKS = 180;
    private static final int CARD_SIZE = 54;
    private static final int CARD_SPACING = 62;
    private static final int FINAL_INDEX = 32;

    private final int[] outcomes;
    private final int[][] sequences = new int[COLUMNS][45];
    private final int[] lastSoundIndex = new int[COLUMNS];
    private final boolean[] stopSoundPlayed = new boolean[COLUMNS];
    private final Checkbox[] choiceBoxes = new Checkbox[COLUMNS];
    private final int animationTotalTicks;
    private int animationTicks;
    private boolean buttonsAdded;
    private boolean submitted;

    public ChanceDieLargeScreen(int[] outcomes, int unlockTicks) {
        super(Component.translatable("screen.ramesses_curse.chance_die_large.title"));
        this.outcomes = outcomes.clone();
        this.animationTotalTicks = Math.max(1, unlockTicks);
        this.animationTicks = animationTotalTicks;
        RandomSource random = RandomSource.create();
        java.util.Arrays.fill(lastSoundIndex, -1);
        for (int column = 0; column < COLUMNS; column++) {
            for (int index = 0; index < sequences[column].length; index++) {
                sequences[column][index] = 1 + random.nextInt(6);
            }
            sequences[column][FINAL_INDEX] = Mth.clamp(this.outcomes[column], 1, 6);
        }
    }

    private static ResourceLocation texture(int side) {
        return new ResourceLocation(RamessesCurseMod.MODID,
                "textures/gui/chance_die/chance_die_outcome_" + side + ".png");
    }

    @Override
    protected void init() {
        super.init();
        buttonsAdded = false;
        addRenderableWidget(DuatRitualGui.button(
                        Component.translatable("screen.ramesses_curse.chance_die_large.exit"),
                        button -> onClose(), width / 2 + 4, height - 28, 76, 20));
    }

    @Override
    public void tick() {
        super.tick();
        if (animationTicks > 0) {
            animationTicks--;
            playRollingSounds();
        }
        if (animationTicks == 0 && !buttonsAdded) {
            addChoiceButtons();
        }
    }

    private void addChoiceButtons() {
        buttonsAdded = true;
        int panelWidth = panelWidth();
        int panelLeft = panelLeft();
        int columnWidth = (panelWidth - 12) / COLUMNS;
        int checkboxY = height - 48;
        for (int column = 0; column < COLUMNS; column++) {
            int x = panelLeft + 6 + column * columnWidth;
            choiceBoxes[column] = addRenderableWidget(new Checkbox(
                    x + columnWidth / 2 - 7,
                    checkboxY,
                    14,
                    14,
                    Component.empty(),
                    false,
                    false));
        }
        addRenderableWidget(DuatRitualGui.button(
                        Component.translatable("screen.ramesses_curse.chance_die_large.complete"),
                        button -> submitChoices(), width / 2 - 80, height - 28, 76, 20));
    }

    private void submitChoices() {
        if (animationTicks > 0 || submitted) {
            return;
        }
        submitted = true;
        for (int column = 0; column < COLUMNS; column++) {
            ModNetwork.CHANNEL.sendToServer(new ChanceDieLargeChoicePacket(
                    column,
                    choiceBoxes[column] != null && choiceBoxes[column].selected()));
        }
        Minecraft.getInstance().player.playSound(SoundEvents.PLAYER_LEVELUP, 0.7F, 1.15F);
        onClose();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        if (!submitted) {
            submitted = true;
            ModNetwork.CHANNEL.sendToServer(new ChanceDieLargeCancelPacket());
        }
        super.onClose();
    }

    private boolean isSelected(int column) {
        return choiceBoxes[column] != null && choiceBoxes[column].selected();
    }

    private void playRollingSounds() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        boolean playedThisTick = false;
        int elapsed = animationTotalTicks - animationTicks;
        for (int column = 0; column < COLUMNS; column++) {
            int duration = columnDuration(column);
            float progress = Mth.clamp(elapsed / (float) duration, 0.0F, 1.0F);
            float eased = 1.0F - (float) Math.pow(1.0F - progress, 3.0D);
            int soundIndex = Mth.clamp(Mth.floor(eased * FINAL_INDEX), 0, FINAL_INDEX);
            if (soundIndex != lastSoundIndex[column] && !playedThisTick) {
                lastSoundIndex[column] = soundIndex;
                minecraft.player.playSound(SoundEvents.NOTE_BLOCK_HAT.value(), 0.28F,
                        0.72F + column * 0.045F + soundIndex * 0.006F);
                playedThisTick = true;
            }
            if (progress >= 1.0F && !stopSoundPlayed[column]) {
                stopSoundPlayed[column] = true;
                minecraft.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.42F,
                        0.85F + column * 0.04F);
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int centerX = width / 2;
        int panelWidth = panelWidth();
        int panelLeft = panelLeft();
        int panelRight = panelLeft + panelWidth;
        int top = 4;
        int bottom = height - 4;
        int reelTop = top + 32;
        int reelBottom = height - 62;
        int reelCenterY = (reelTop + reelBottom) / 2;
        int columnWidth = (panelWidth - 12) / COLUMNS;
        DuatRitualGui.panel(guiGraphics, panelLeft, top, panelWidth, bottom - top, false);
        guiGraphics.fill(panelLeft, reelTop - 4, panelRight, reelBottom + 4, 0x080F1518);
        guiGraphics.drawCenteredString(font,
                Component.translatable("screen.ramesses_curse.chance_die_large.title")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                centerX, top + 8, 0xFFF1D58A);
        if (animationTicks == 0) {
            guiGraphics.drawCenteredString(font,
                    Component.translatable("screen.ramesses_curse.chance_die_large.select_hint")
                            .withStyle(ChatFormatting.GRAY),
                    centerX, height - 51, 0xFFD0D0D0);
        }

        for (int column = 0; column < COLUMNS; column++) {
            int columnLeft = panelLeft + 6 + column * columnWidth;
            int columnRight = columnLeft + columnWidth - 2;
            int columnCenterX = (columnLeft + columnRight) / 2;
            boolean rollingUp = column % 2 == 0;
            int columnDuration = columnDuration(column);
            float progress = Mth.clamp((animationTotalTicks - animationTicks) / (float) columnDuration, 0.0F, 1.0F);
            float eased = 1.0F - (float) Math.pow(1.0F - progress, 3.0D);
            float offset = eased * FINAL_INDEX * CARD_SPACING;
            int direction = rollingUp ? -1 : 1;
                    guiGraphics.fill(columnLeft, reelTop, columnRight, reelBottom,
                    isSelected(column) ? 0x141A2024 : 0x201A2024);
            guiGraphics.enableScissor(columnLeft, reelTop, columnRight, reelBottom);
            for (int index = 0; index < sequences[column].length; index++) {
                int cardY = Math.round(reelCenterY
                        + (index * CARD_SPACING - offset) * direction
                        - CARD_SIZE / 2.0F);
                if (cardY + CARD_SIZE < reelTop || cardY > reelBottom) {
                    continue;
                }
                int cardLeft = columnCenterX - CARD_SIZE / 2;
                boolean selectedResult = progress >= 1.0F && index == FINAL_INDEX;
                guiGraphics.fill(cardLeft - 2, cardY - 2, cardLeft + CARD_SIZE + 2, cardY + CARD_SIZE + 2,
                        selectedResult && !isSelected(column) ? 0xD09A7329 : 0xA02F363A);
                guiGraphics.blit(OUTCOME_TEXTURES[sequences[column][index] - 1],
                        cardLeft, cardY, CARD_SIZE, CARD_SIZE,
                        0.0F, 0.0F, 16, 16, 16, 16);
            }
            guiGraphics.disableScissor();
            guiGraphics.fill(columnLeft, reelCenterY - 1, columnLeft + 7, reelCenterY + 1,
                    isSelected(column) ? 0x705D6468 : 0xFFE0B84F);
            guiGraphics.fill(columnRight - 7, reelCenterY - 1, columnRight, reelCenterY + 1,
                    isSelected(column) ? 0x705D6468 : 0xFFE0B84F);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private int columnDuration(int column) {
        return Math.max(1, Math.round((126 + column * 9)
                * animationTotalTicks / (float) DEFAULT_ANIMATION_TICKS));
    }

    private int panelWidth() {
        return Math.max(1, Math.min(720, width - 12));
    }

    private int panelLeft() {
        return (width - panelWidth()) / 2;
    }
}
