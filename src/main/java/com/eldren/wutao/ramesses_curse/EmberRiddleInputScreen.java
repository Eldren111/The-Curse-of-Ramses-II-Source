package com.eldren.wutao.ramesses_curse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class EmberRiddleInputScreen extends Screen {
    private static final int BASE_MAIN_WIDTH = 316;
    private static final int BASE_MAIN_HEIGHT = 214;
    private static final int BASE_SIDEBAR_WIDTH = 160;

    private static final String[] CIPHER_GLYPHS = new String[]{
            "\uE000", "\uE001", "\uE002", "\uE003", "\uE004", "\uE005", "\uE006", "\uE007",
            "\uE008", "\uE009", "\uE00A", "\uE00B", "\uE00C", "\uE00D", "\uE00E", "\uE00F",
            "\uE010", "\uE011", "\uE012", "\uE013", "\uE014", "\uE015", "\uE016", "\uE017",
            "\uE018", "\uE019"
    };

    private EditBox answerBox;
    private Button submitButton;
    private Button toggleReferenceButton;
    private Button refreshButton;
    private Button curseSevenButton;
    private Component subtitle;
    private final List<Component> roundTitles = new ArrayList<>();
    private String inscription;
    private boolean referenceOpen = false;
    private boolean showingCurseSevenPage = false;
    private int referenceScroll = 0;
    private final List<ReferenceEntry> referenceEntries = new ArrayList<>();
    private int referenceContentHeight = 0;
    private final boolean satietyMode;

    public EmberRiddleInputScreen() {
        this(false);
    }

    public EmberRiddleInputScreen(boolean satietyMode) {
        super(Component.translatable(satietyMode
                ? "message.ramesses_curse.satiety_riddle_book_title"
                : "message.ramesses_curse.ember_riddle_book_title"));
        this.satietyMode = satietyMode;
        this.subtitle = currentHint();
        this.inscription = currentInscription();
        this.roundTitles.addAll(currentTitles());
    }

    @Override
    protected void init() {
        super.init();
        rebuildPanel();
    }

    private void rebuildPanel() {
        refreshClientSnapshot();
        this.clearWidgets();
        rebuildReferenceEntries();

        Layout layout = getLayout();
        int left = layout.left();
        int top = layout.top();
        int mainWidth = layout.mainWidth();
        int mainHeight = layout.mainHeight();
        int scale = layout.scalePercent();

        this.answerBox = new EditBox(this.font, left + scaled(22, scale), top + scaled(146, scale), mainWidth - scaled(44, scale), scaled(18, scale),
                Component.translatable("screen.ramesses_curse.ember_riddle_input.answer"));
        this.answerBox.setBordered(false);
        this.answerBox.setTextColor(0xFFF5E8C9);
        this.answerBox.setTextColorUneditable(0xFFC8B892);
        this.answerBox.setMaxLength(128);
        this.answerBox.setCanLoseFocus(false);
        this.answerBox.setResponder(value -> updateSubmitButton());
        this.addRenderableWidget(this.answerBox);

        this.submitButton = this.addRenderableWidget(Button.builder(
                        Component.translatable("screen.ramesses_curse.ember_riddle_input.submit"),
                        button -> submit())
                .bounds(left - scaled(92, scale), top + scaled(68, scale), scaled(84, scale), scaled(20, scale))
                .build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> onClose())
                .bounds(left - scaled(92, scale), top + scaled(92, scale), scaled(84, scale), scaled(20, scale))
                .build());

        this.toggleReferenceButton = this.addRenderableWidget(Button.builder(
                        Component.translatable(this.referenceOpen
                                ? "screen.ramesses_curse.ember_riddle_input.reference_close"
                                : "screen.ramesses_curse.ember_riddle_input.reference_open"),
                        button -> {
                            this.referenceOpen = !this.referenceOpen;
                            this.referenceScroll = 0;
                            rebuildPanel();
                        })
                .bounds(left - scaled(92, scale), top + scaled(20, scale), scaled(84, scale), scaled(20, scale))
                .build());

        this.refreshButton = this.addRenderableWidget(Button.builder(
                        Component.translatable("screen.ramesses_curse.ember_riddle_input.refresh"),
                        button -> refreshRiddle())
                .bounds(left - scaled(92, scale), top + scaled(44, scale), scaled(84, scale), scaled(20, scale))
                .build());
        this.refreshButton.visible = !this.satietyMode;

        if (!this.satietyMode && ModEvents.shouldShowCurseSevenDuatEntry()) {
            this.curseSevenButton = this.addRenderableWidget(Button.builder(
                            Component.literal("终末碑录"),
                            button -> {
                                this.showingCurseSevenPage = true;
                                refreshClientSnapshot();
                                updateSubmitButton();
                            })
                    .bounds(left - scaled(92, scale), top + scaled(116, scale), scaled(84, scale), scaled(20, scale))
                    .build());
        } else {
            this.curseSevenButton = null;
            this.showingCurseSevenPage = false;
        }

        this.setInitialFocus(this.answerBox);
        updateSubmitButton();
    }

    private Layout getLayout() {
        int scale = getUiScalePercent();
        int mainWidth = scaled(BASE_MAIN_WIDTH, scale);
        int mainHeight = scaled(BASE_MAIN_HEIGHT, scale);
        int sidebarWidth = this.referenceOpen ? scaled(BASE_SIDEBAR_WIDTH, scale) : 0;
        int gap = this.referenceOpen ? scaled(6, scale) : 0;
        int panelWidth = mainWidth + sidebarWidth + gap;
        panelWidth = Math.min(panelWidth, this.width - 24);
        int left = (this.width - panelWidth) / 2;
        int top = Math.max(2, (this.height - mainHeight) / 2 - scaled(8, scale));
        return new Layout(left, top, mainWidth, mainHeight, sidebarWidth, gap, panelWidth, scale);
    }

    private int getUiScalePercent() {
        int widthDriven = (int) Math.round((this.width * 100.0D) / 620.0D);
        int heightDriven = (int) Math.round((this.height * 100.0D) / 350.0D);
        return Mth.clamp(Math.min(widthDriven, heightDriven), 68, 160);
    }

    private int scaled(int value, int scalePercent) {
        return Math.max(1, value * scalePercent / 100);
    }

    private void updateSubmitButton() {
        if (this.submitButton != null) {
            this.submitButton.active = !this.answerBox.getValue().trim().isEmpty();
        }
        if (this.refreshButton != null) {
            this.refreshButton.active = !this.satietyMode && !ModEvents.isClientCurseSevenKeywordLockedForRiddleRefresh();
        }
        if (this.curseSevenButton != null) {
            this.curseSevenButton.active = ModEvents.shouldShowCurseSevenDuatEntry();
        }
        if (this.answerBox != null) {
            this.answerBox.setEditable(true);
        }
    }

    private void submit() {
        if (this.showingCurseSevenPage) {
            String value = this.answerBox.getValue().trim();
            if (!value.isEmpty()) {
                ModNetwork.CHANNEL.sendToServer(new CurseSevenRevealPacket(value));
                this.answerBox.setValue("");
            }
            return;
        }
        String value = this.answerBox.getValue().trim();
        if (value.isEmpty()) {
            return;
        }
        ModNetwork.CHANNEL.sendToServer(this.satietyMode
                ? new SatietyRiddleSubmitPacket(value)
                : new EmberRiddleSubmitPacket(value));
        this.answerBox.setValue("");
        updateSubmitButton();
    }

    private void refreshRiddle() {
        if (this.satietyMode) {
            return;
        }
        if (this.showingCurseSevenPage) {
            this.showingCurseSevenPage = false;
            refreshClientSnapshot();
            updateSubmitButton();
            return;
        }
        if (ModEvents.isClientCurseSevenKeywordLockedForRiddleRefresh()) {
            return;
        }
        ModNetwork.CHANNEL.sendToServer(new EmberRiddleRefreshPacket());
        this.answerBox.setValue("");
        updateSubmitButton();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        refreshClientSnapshot();
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        Layout layout = getLayout();
        renderMainPanel(graphics, layout);
        if (this.referenceOpen) {
            renderReferencePanel(graphics, layout);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderMainPanel(GuiGraphics graphics, Layout layout) {
        int left = layout.left();
        int top = layout.top();
        int width = layout.mainWidth();
        int height = layout.mainHeight();
        int scale = layout.scalePercent();

        graphics.fill(left, top, left + width, top + height, 0xFF474038);
        graphics.fill(left + 2, top + 2, left + width - 2, top + height - 2, 0xFF5F554A);
        graphics.fill(left + 5, top + 5, left + width - 5, top + height - 5, 0xFF7A6B56);
        graphics.fill(left + 7, top + 7, left + width - 7, top + height - 7, 0xFF54493D);
        graphics.fill(left, top, left + width, top + 3, 0xFFE2D2B0);
        graphics.fill(left, top + height - 3, left + width, top + height, 0xFF8F8570);
        graphics.fill(left, top, left + 3, top + height, 0xFF8F8570);
        graphics.fill(left + width - 3, top, left + width, top + height, 0xFF8F8570);
        graphics.fill(left + 10, top + 10, left + width - 10, top + height - 10, 0x1828211B);
        graphics.fill(left + 14, top + 14, left + width - 14, top + height - 14, 0x0E120F0C);
        renderStoneOrnaments(graphics, left, top, width, height, false);
        renderStoneSidePillars(graphics, left, top, width, height, scale);

        renderTitleCartouche(graphics, left, top, width, scale);
        graphics.drawCenteredString(this.font, this.title, left + width / 2 + scaled(2, scale), top + scaled(11, scale), 0xFFF5E6C1);
        int roundSize = this.showingCurseSevenPage ? 1 : Math.max(1, currentRoundSize());
        int currentIndex = this.showingCurseSevenPage ? 1 : Math.max(0, currentIndex() + 1);
        graphics.drawCenteredString(
                this.font,
                Component.translatable("screen.ramesses_curse.ember_riddle_input.progress", currentIndex, roundSize),
                left + width / 2 + scaled(2, scale),
                top + scaled(24, scale),
                0xFFD7C28D);

        renderHeaderBand(graphics, left, top, width, scale);

        renderHintPanel(graphics, left, top, width, scale);
        List<FormattedCharSequence> lines = this.font.split(this.subtitle, width - scaled(40, scale));
        int y = top + scaled(44, scale);
        for (int i = 0; i < Math.min(lines.size(), 4); i++) {
            int lineWidth = this.font.width(lines.get(i));
            graphics.drawString(this.font, lines.get(i), left + (width - lineWidth) / 2, y, 0xFFF7F1E4, false);
            y += scaled(10, scale);
        }

        renderInscription(graphics, left, top, width, scale);
        renderDivider(graphics, left + scaled(18, scale), left + width - scaled(18, scale), top + scaled(128, scale));

        graphics.drawString(this.font, Component.translatable("screen.ramesses_curse.ember_riddle_input.answer"),
                left + scaled(20, scale), top + scaled(134, scale), 0xFFE8D39C, false);
        graphics.drawCenteredString(this.font,
                Component.translatable("screen.ramesses_curse.ember_riddle_input.panel"),
                left + width / 2,
                top + scaled(120, scale),
                0xFFE4D6BA);
        renderAnswerFrame(graphics, left, top, width, scale);
    }

    private void renderInscription(GuiGraphics graphics, int left, int top, int width, int scale) {
        graphics.fill(left + scaled(22, scale), top + scaled(60, scale), left + width - scaled(22, scale), top + scaled(108, scale), 0x40221812);
        graphics.fill(left + scaled(24, scale), top + scaled(62, scale), left + width - scaled(24, scale), top + scaled(106, scale), 0x8E9F8A66);
        graphics.fill(left + scaled(28, scale), top + scaled(66, scale), left + width - scaled(28, scale), top + scaled(102, scale), 0x2A241B13);
        graphics.fill(left + scaled(34, scale), top + scaled(72, scale), left + width - scaled(34, scale), top + scaled(96, scale), 0x14100B08);
        renderInsetMarks(graphics, left + scaled(28, scale), top + scaled(66, scale), width - scaled(56, scale), scaled(32, scale));
        renderDivider(graphics, left + scaled(34, scale), left + width - scaled(34, scale), top + scaled(58, scale));
        graphics.drawCenteredString(
                this.font,
                Component.translatable("screen.ramesses_curse.ember_riddle_input.inscription"),
                left + width / 2 + scaled(5, scale),
                top + scaled(64, scale),
                0xFFE7D39C);

        if (this.inscription == null || this.inscription.isBlank()) {
            graphics.drawCenteredString(
                    this.font,
                    Component.translatable("message.ramesses_curse.ember_riddle_missing"),
                    left + width / 2 + scaled(5, scale),
                    top + scaled(84, scale),
                    0xFFD7CCB3);
            return;
        }

        String[] glyphLines = this.inscription.split("\\n");
        int glyphY = top + scaled(78, scale) - ((glyphLines.length - 1) * scaled(6, scale));
        for (String glyphLine : glyphLines) {
            int lineWidth = this.font.width(glyphLine);
            graphics.drawString(this.font, glyphLine, left + (width - lineWidth) / 2 + scaled(5, scale), glyphY + 1, 0x66443424, false);
            graphics.drawString(this.font, glyphLine, left + (width - lineWidth) / 2 + scaled(5, scale), glyphY, 0xFFF2E1B2, false);
            glyphY += scaled(12, scale);
        }
    }

    private void renderReferencePanel(GuiGraphics graphics, Layout layout) {
        int left = layout.left() + layout.mainWidth() + layout.gap();
        int top = layout.top();
        int width = layout.sidebarWidth();
        int height = layout.mainHeight();
        int scale = layout.scalePercent();

        graphics.fill(left, top, left + width, top + height, 0xFF403A33);
        graphics.fill(left + 2, top + 2, left + width - 2, top + height - 2, 0xFF595044);
        graphics.fill(left + 5, top + 5, left + width - 5, top + height - 5, 0xFF4A4137);
        graphics.fill(left, top, left + width, top + 3, 0xFFD8C7A0);
        graphics.fill(left, top + height - 3, left + width, top + height, 0xFF8A7C62);
        graphics.fill(left + 8, top + 8, left + width - 8, top + height - 8, 0x1A2A241E);
        renderStoneOrnaments(graphics, left, top, width, height, true);

        graphics.drawCenteredString(this.font, Component.translatable("screen.ramesses_curse.ember_riddle_input.reference_panel"), left + width / 2, top + scaled(10, scale), 0xFFF1E4C8);
        renderDivider(graphics, left + scaled(10, scale), left + width - scaled(14, scale), top + scaled(38, scale));

        int contentLeft = left + scaled(10, scale);
        int contentTop = top + scaled(28, scale);
        int contentBottom = top + height - scaled(14, scale);
        int y = contentTop - this.referenceScroll;

        if (y + 9 >= contentTop && y <= contentBottom) {
            graphics.drawString(this.font, Component.translatable("screen.ramesses_curse.ember_riddle_input.alphabet"), contentLeft, y, 0xFFE7D39C, false);
        }
        y += scaled(14, scale);

        for (String line : buildAlphabetLines()) {
            if (y + 9 >= contentTop && y <= contentBottom) {
                graphics.drawString(this.font, Component.literal(line), contentLeft, y, 0xFFF6F0E2, false);
            }
            y += scaled(12, scale);
        }

        y += scaled(15, scale);
        if (y + 9 >= contentTop && y <= contentBottom) {
            graphics.drawString(this.font, Component.translatable(this.satietyMode
                    ? "screen.ramesses_curse.satiety_riddle_input.round_titles"
                    : "screen.ramesses_curse.ember_riddle_input.round_titles"), contentLeft, y, 0xFFE7D39C, false);
        }
        y += scaled(12, scale);

        for (ReferenceEntry entry : this.referenceEntries) {
            int entryTop = y + entry.offsetY();
            int entryBottom = entryTop + entry.height();
            if (entryBottom < contentTop || entryTop > contentBottom) {
                continue;
            }

            boolean solved = entry.selectable() && currentSolved(entry.index());
            boolean selected = entry.selectable() && entry.index() == currentIndex();
            int entryLeft = contentLeft - 2;
            int entryRight = left + width - 24;
            int clippedTop = Math.max(contentTop, entryTop - 1);
            int clippedBottom = Math.min(contentBottom, entryBottom);
            if (!entry.selectable()) {
                graphics.fill(entryLeft, clippedTop, entryRight, clippedBottom, 0x2A20170F);
            } else if (solved) {
                graphics.fill(entryLeft, clippedTop, entryRight, clippedBottom, 0x33221812);
            } else if (selected) {
                graphics.fill(entryLeft, clippedTop, entryRight, clippedBottom, 0x446F5B33);
            }

            int lineY = entryTop;
            for (int i = 0; i < entry.lines().size(); i++) {
                FormattedCharSequence line = entry.lines().get(i);
                int color = !entry.selectable() && i == 0
                        ? 0xFFE7D39C
                        : solved ? 0xFF8D8474 : selected ? 0xFFF7E4B8 : 0xFFF6F0E2;
                if (lineY + 8 >= contentTop && lineY <= contentBottom) {
                    graphics.drawString(this.font, line, contentLeft, lineY, color, false);
                }
                lineY += scaled(9, scale);
            }
        }

        int contentHeight = Math.max(1, (y - (contentTop - this.referenceScroll)) + this.referenceContentHeight);
        int viewportHeight = contentBottom - contentTop;
        int maxScroll = Math.max(0, contentHeight - viewportHeight);
        if (maxScroll > 0) {
            int barX = left + width - scaled(9, scale);
            int barTop = contentTop;
            int barBottom = contentBottom;
            graphics.fill(barX, barTop, barX + scaled(4, scale), barBottom, 0x66302012);
            int thumbHeight = Math.max(scaled(18, scale), (int) ((float) viewportHeight * viewportHeight / contentHeight));
            int thumbY = barTop + (int) ((float) this.referenceScroll * (viewportHeight - thumbHeight) / maxScroll);
            graphics.fill(barX, thumbY, barX + scaled(4, scale), thumbY + thumbHeight, 0xD8E7D39C);
            graphics.fill(barX - 1, thumbY, barX, thumbY + thumbHeight, 0xAA6E5C3B);
        }
    }

    private void renderHeaderBand(GuiGraphics graphics, int left, int top, int width, int scale) {
        graphics.fill(left + scaled(30, scale), top + scaled(29, scale), left + width - scaled(30, scale), top + scaled(34, scale), 0x44362E24);
        graphics.fill(left + scaled(38, scale), top + scaled(30, scale), left + width - scaled(38, scale), top + scaled(33, scale), 0x889E8A61);
        renderDivider(graphics, left + scaled(28, scale), left + width - scaled(28, scale), top + scaled(36, scale));
    }

    private void renderAnswerFrame(GuiGraphics graphics, int left, int top, int width, int scale) {
        int boxLeft = left + scaled(16, scale);
        int boxTop = top + scaled(144, scale);
        int boxRight = left + width - scaled(16, scale);
        int boxBottom = top + scaled(178, scale);
        graphics.fill(boxLeft, boxTop, boxRight, boxBottom, 0x5532271B);
        graphics.fill(boxLeft + 2, boxTop + 2, boxRight - 2, boxBottom - 2, 0xCC1A1410);
        graphics.fill(boxLeft + 4, boxTop + 4, boxRight - 4, boxBottom - 4, 0x553F3428);
        graphics.fill(boxLeft + 6, boxTop + 1, boxRight - 6, boxTop + 2, 0x66D1B678);
        graphics.fill(boxLeft + 6, boxBottom - 2, boxRight - 6, boxBottom - 1, 0x66291E14);
    }

    private void renderDivider(GuiGraphics graphics, int left, int right, int y) {
        int center = (left + right) / 2;
        graphics.fill(left, y, center - 8, y + 1, 0x887A6848);
        graphics.fill(center + 8, y, right, y + 1, 0x887A6848);
        graphics.fill(center - 6, y - 1, center + 6, y + 2, 0xAA9E8A61);
        graphics.fill(center - 1, y - 3, center + 1, y + 4, 0x667A6848);
    }

    private void renderStoneOrnaments(GuiGraphics graphics, int left, int top, int width, int height, boolean compact) {
        int accent = compact ? 0xAAAE9C73 : 0xAAA78F64;
        int shadow = compact ? 0x66423424 : 0x6650402A;
        renderCornerMotif(graphics, left + 8, top + 8, accent, shadow, false, false);
        renderCornerMotif(graphics, left + width - 8, top + 8, accent, shadow, true, false);
        renderCornerMotif(graphics, left + 8, top + height - 8, accent, shadow, false, true);
        renderCornerMotif(graphics, left + width - 8, top + height - 8, accent, shadow, true, true);

        int bandColor = compact ? 0x33261F18 : 0x3A2F261C;
        graphics.fill(left + 6, top + 48, left + width - 6, top + 50, bandColor);
        graphics.fill(left + 6, top + height - 28, left + width - 6, top + height - 26, bandColor);
    }

    private void renderTitleCartouche(GuiGraphics graphics, int left, int top, int width, int scale) {
        int cartoucheLeft = left + scaled(56, scale);
        int cartoucheRight = left + width - scaled(56, scale);
        int cartoucheTop = top + scaled(6, scale);
        int cartoucheBottom = top + scaled(20, scale);
        graphics.fill(cartoucheLeft, cartoucheTop, cartoucheRight, cartoucheBottom, 0x4431261B);
        graphics.fill(cartoucheLeft + 2, cartoucheTop + 2, cartoucheRight - 2, cartoucheBottom - 2, 0x886F5B3C);
        graphics.fill(cartoucheLeft + 8, cartoucheTop + 5, cartoucheRight - 8, cartoucheBottom - 5, 0x33322519);
        graphics.fill(cartoucheLeft - scaled(8, scale), top + scaled(12, scale), cartoucheLeft, top + scaled(14, scale), 0xAA9E8A61);
        graphics.fill(cartoucheRight, top + scaled(12, scale), cartoucheRight + scaled(8, scale), top + scaled(14, scale), 0xAA9E8A61);
    }

    private void renderHintPanel(GuiGraphics graphics, int left, int top, int width, int scale) {
        int panelLeft = left + scaled(18, scale);
        int panelTop = top + scaled(34, scale);
        int panelRight = left + width - scaled(18, scale);
        int panelBottom = top + scaled(58, scale);
        graphics.fill(panelLeft, panelTop, panelRight, panelBottom, 0x2E221913);
        graphics.fill(panelLeft + 2, panelTop + 2, panelRight - 2, panelBottom - 2, 0x412B2118);
        graphics.fill(panelLeft + 6, panelTop + 5, panelLeft + 8, panelBottom - 5, 0x889E8A61);
        graphics.fill(panelRight - 8, panelTop + 5, panelRight - 6, panelBottom - 5, 0x889E8A61);
    }

    private void renderStoneSidePillars(GuiGraphics graphics, int left, int top, int width, int height, int scale) {
        int pillarWidth = scaled(10, scale);
        int innerTop = top + scaled(22, scale);
        int innerBottom = top + height - scaled(22, scale);
        renderPillar(graphics, left + scaled(10, scale), innerTop, pillarWidth, innerBottom - innerTop);
        renderPillar(graphics, left + width - scaled(20, scale), innerTop, pillarWidth, innerBottom - innerTop);
    }

    private void renderPillar(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0x2C1D150F);
        graphics.fill(x + 1, y + 2, x + width - 1, y + height - 2, 0x55453427);
        graphics.fill(x + width / 2, y + 4, x + width / 2 + 1, y + height - 4, 0x88B49A63);
        for (int offset = 10; offset < height - 10; offset += 14) {
            graphics.fill(x + 2, y + offset, x + width - 2, y + offset + 2, 0x66604C31);
        }
    }

    private void renderInsetMarks(GuiGraphics graphics, int left, int top, int width, int height) {
        int step = Math.max(12, width / 8);
        for (int x = left + 6; x < left + width - 6; x += step) {
            graphics.fill(x, top + 3, x + 1, top + 7, 0x558F7A54);
            graphics.fill(x, top + height - 7, x + 1, top + height - 3, 0x55483A27);
        }
    }

    private void renderCornerMotif(GuiGraphics graphics, int anchorX, int anchorY, int accent, int shadow, boolean right, boolean bottom) {
        int dx = right ? -1 : 1;
        int dy = bottom ? -1 : 1;

        graphics.fill(anchorX, anchorY, anchorX + dx * 8, anchorY + dy * 2, accent);
        graphics.fill(anchorX, anchorY, anchorX + dx * 2, anchorY + dy * 8, accent);
        graphics.fill(anchorX + dx * 2, anchorY + dy * 2, anchorX + dx * 6, anchorY + dy * 4, shadow);
        graphics.fill(anchorX + dx * 2, anchorY + dy * 2, anchorX + dx * 4, anchorY + dy * 6, shadow);
    }

    private List<String> buildAlphabetLines() {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < CIPHER_GLYPHS.length; i += 3) {
            String first = formatAlphabetEntry(i);
            String second = i + 1 < CIPHER_GLYPHS.length ? formatAlphabetEntry(i + 1) : "";
            String third = i + 2 < CIPHER_GLYPHS.length ? formatAlphabetEntry(i + 2) : "";
            lines.add(String.format("%-10s %-10s %-10s", first, second, third).stripTrailing());
        }
        return lines;
    }

    private String formatAlphabetEntry(int index) {
        return CIPHER_GLYPHS[index] + " " + (char) ('A' + index);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 || keyCode == 335) {
            submit();
            return true;
        }
        if (this.referenceOpen && (keyCode == 264 || keyCode == 265)) {
            int step = scaled(20, getLayout().scalePercent());
            scrollReference(keyCode == 264 ? step : -step);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (this.referenceOpen) {
            Layout layout = getLayout();
            int left = layout.left() + layout.mainWidth() + layout.gap();
            int top = layout.top();
            int width = layout.sidebarWidth();
            int height = layout.mainHeight();
            if (mouseX >= left && mouseX <= left + width && mouseY >= top && mouseY <= top + height) {
                scrollReference((int) (-deltaY * scaled(18, layout.scalePercent())));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    private void scrollReference(int delta) {
        int maxScroll = getReferenceMaxScroll();
        this.referenceScroll = Math.max(0, Math.min(maxScroll, this.referenceScroll + delta));
    }

    private int getReferenceMaxScroll() {
        Layout layout = getLayout();
        int scale = layout.scalePercent();
        int totalHeight = scaled(14, scale) + scaled(12, scale) * buildAlphabetLines().size() + scaled(6, scale) + scaled(12, scale) + this.referenceContentHeight;
        int viewportHeight = layout.mainHeight() - scaled(42, scale);
        return Math.max(0, totalHeight - viewportHeight);
    }

    private void rebuildReferenceEntries() {
        this.referenceEntries.clear();
        Layout layout = getLayout();
        int scale = layout.scalePercent();
        int wrapWidth = Math.max(72, layout.sidebarWidth() - scaled(34, scale));
        int offsetY = 0;
        for (int i = 0; i < Math.min(this.roundTitles.size(), this.satietyMode ? 3 : 12); i++) {
            List<FormattedCharSequence> wrapped = this.font.split(this.roundTitles.get(i), wrapWidth);
            int height = wrapped.size() * scaled(9, scale) + scaled(2, scale);
            this.referenceEntries.add(new ReferenceEntry(i, offsetY, height, wrapped, true));
            offsetY += height;
        }
        if (!this.satietyMode && ModEvents.shouldShowCurseSevenSentenceInDuatUi()) {
            offsetY += scaled(8, scale);
            List<FormattedCharSequence> clueLines = new ArrayList<>();
            clueLines.addAll(this.font.split(Component.literal("余烬残句"), wrapWidth));
            clueLines.addAll(this.font.split(Component.literal(ModEvents.getCurseSevenSentenceMaskedForManual()), wrapWidth));
            clueLines.addAll(this.font.split(Component.literal(ModEvents.getCurseSevenSentenceCipherForManual()), wrapWidth));
            int height = clueLines.size() * scaled(9, scale) + scaled(4, scale);
            this.referenceEntries.add(new ReferenceEntry(-1, offsetY, height, clueLines, false));
            offsetY += height;
        }
        this.referenceContentHeight = offsetY;
        this.referenceScroll = Mth.clamp(this.referenceScroll, 0, getReferenceMaxScroll());
    }

    private void refreshClientSnapshot() {
        if (this.showingCurseSevenPage) {
            this.subtitle = ModEvents.getClientCurseSevenDuatHint();
            this.inscription = ModEvents.getClientCurseSevenDuatInscription();
        } else {
            this.subtitle = currentHint();
            this.inscription = currentInscription();
        }
        List<Component> latestTitles = currentTitles();
        if (!this.roundTitles.equals(latestTitles)) {
            this.roundTitles.clear();
            this.roundTitles.addAll(latestTitles);
            if (this.font != null) {
                rebuildReferenceEntries();
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.referenceOpen && tryClickReferenceTitle(mouseX, mouseY)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean tryClickReferenceTitle(double mouseX, double mouseY) {
        Layout layout = getLayout();
        int left = layout.left() + layout.mainWidth() + layout.gap();
        int top = layout.top();
        int width = layout.sidebarWidth();
        int scale = layout.scalePercent();
        int contentLeft = left + scaled(10, scale);
        int contentTop = top + scaled(28, scale);
        int yBase = contentTop - this.referenceScroll;
        int titleStartY = yBase
                + scaled(14, scale)
                + scaled(12, scale) * buildAlphabetLines().size()
                + scaled(15, scale)
                + scaled(12, scale);
        int contentRight = left + width - scaled(24, scale);

        for (ReferenceEntry entry : this.referenceEntries) {
            if (!entry.selectable()) {
                continue;
            }
            int entryTop = titleStartY + entry.offsetY();
            int entryBottom = entryTop + entry.height();
            if (mouseX >= contentLeft && mouseX <= contentRight && mouseY >= entryTop - 1 && mouseY <= entryBottom) {
                if (currentSolved(entry.index())) {
                    return true;
                }
                if (entry.index() != currentIndex()) {
                    ModNetwork.CHANNEL.sendToServer(this.satietyMode
                            ? new SatietyRiddleSelectPacket(entry.index())
                            : new EmberRiddleSelectPacket(entry.index()));
                }
                return true;
            }
        }
        return false;
    }

    private Component currentHint() {
        return this.satietyMode
                ? ModEvents.getClientCurrentSatietyRiddleHint()
                : ModEvents.getClientCurrentEmberRiddleHint();
    }

    private String currentInscription() {
        return this.satietyMode
                ? ModEvents.getClientCurrentSatietyRiddleInscription()
                : ModEvents.getClientCurrentEmberRiddleInscription();
    }

    private List<Component> currentTitles() {
        return this.satietyMode
                ? ModEvents.ClientOverlayEvents.getClientSatietyRiddleTitles()
                : ModEvents.ClientOverlayEvents.getClientEmberRiddleTitles();
    }

    private int currentRoundSize() {
        return this.satietyMode
                ? ModEvents.ClientOverlayEvents.getClientSatietyRiddleRoundSize()
                : ModEvents.ClientOverlayEvents.getClientEmberRiddleRoundSize();
    }

    private int currentIndex() {
        return this.satietyMode
                ? ModEvents.ClientOverlayEvents.getClientSatietyRiddleIndex()
                : ModEvents.ClientOverlayEvents.getClientEmberRiddleIndex();
    }

    private boolean currentSolved(int index) {
        return this.satietyMode
                ? ModEvents.ClientOverlayEvents.isClientSatietyRiddleSolved(index)
                : ModEvents.ClientOverlayEvents.isClientEmberRiddleSolved(index);
    }

    public boolean isSatietyMode() {
        return this.satietyMode;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record Layout(int left, int top, int mainWidth, int mainHeight, int sidebarWidth, int gap, int totalWidth, int scalePercent) {
    }

    private record ReferenceEntry(int index, int offsetY, int height, List<FormattedCharSequence> lines, boolean selectable) {
    }
}
