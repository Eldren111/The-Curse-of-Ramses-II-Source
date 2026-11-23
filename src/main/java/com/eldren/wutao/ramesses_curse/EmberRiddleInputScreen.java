package com.eldren.wutao.ramesses_curse;

import com.sighs.apricityui.element.AbstractText;
import com.sighs.apricityui.init.Document;
import com.sighs.apricityui.init.Element;
import com.sighs.apricityui.screen.ApricityScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** AUI-backed Duat workspace. The page deliberately contains visual UI only. */
public final class EmberRiddleInputScreen extends ApricityScreen {
    private static final String TEMPLATE = "screens/ember_riddle.html";

    private final boolean satietyMode;
    private Component subtitle;
    private String inscription;
    private final List<Component> roundTitles = new ArrayList<>();
    private boolean compareOpen;
    private boolean alphabetOpen = true;
    private boolean showingCurseSevenPage;
    private boolean minimized;
    private boolean maximized;
    private String visualSignature = "";

    public EmberRiddleInputScreen() {
        this(false);
    }

    public EmberRiddleInputScreen(boolean satietyMode) {
        super(TEMPLATE);
        this.satietyMode = satietyMode;
        setPauseGame(false);
        setShowDefaultBackground(false);
        refreshClientSnapshot();
    }

    @Override
    protected void init() {
        // ApricityScreen recreates its document during resize (including F11).
        // Capture the live input before super.init() removes the old document.
        AnswerState answerState = captureAnswerState();
        super.init();
        visualSignature = "";
        Document document = getLinkedDocument();
        if (document != null) {
            Document.runWithContext(document, this::bindDocument);
        }
        refreshClientSnapshot();
        syncDocumentVisuals();
        if (document != null) {
            Document.runWithContext(document, () -> restoreAnswerState(document, answerState));
        }
    }

    private AnswerState captureAnswerState() {
        Document document = getLinkedDocument();
        Element answer = document == null ? null : document.getElementById("answer");
        if (answer == null) {
            return new AnswerState("", 0, 0, "none", true);
        }
        String value = answer.getValue() == null ? "" : answer.getValue();
        if (answer instanceof AbstractText text) {
            return new AnswerState(value, text.getSelectionStart(), text.getSelectionEnd(),
                    text.getSelectionDirection(), Element.isElementFocusing(answer));
        }
        return new AnswerState(value, value.length(), value.length(), "none",
                Element.isElementFocusing(answer));
    }

    private void restoreAnswerState(Document document, AnswerState state) {
        Element answer = document.getElementById("answer");
        if (answer == null) {
            return;
        }
        answer.setValue(state.value());
        if (answer instanceof AbstractText text) {
            text.setSelectionRange(state.selectionStart(), state.selectionEnd(), state.selectionDirection());
        }
        if (state.focused()) {
            answer.focus();
        }
    }

    private void clearAnswer(Element answer) {
        answer.setValue("");
        // AUI 1.2.5/1.2.6 setValue does not reset the cursor or selection.
        // Reset both before another character event can reach insertText.
        if (answer instanceof AbstractText text) {
            text.setSelectionRange(0, 0);
        }
        // Keep the built-in blinking caret visible and accept the next answer.
        answer.focus();
    }

    private record AnswerState(String value, int selectionStart, int selectionEnd,
                               String selectionDirection, boolean focused) {
    }

    @Override
    public void render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        refreshClientSnapshot();
        syncDocumentVisuals();
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void bindDocument() {
        Document document = getLinkedDocument();
        if (document == null) {
            return;
        }

        bindClick(document, "window-close", this::onClose);
        bindClick(document, "window-minimize", () -> {
            minimized = !minimized;
            syncDocumentVisuals(true);
        });
        bindClick(document, "window-maximize", () -> {
            maximized = !maximized;
            minimized = false;
            syncDocumentVisuals(true);
        });
        bindClick(document, "nav-compare", () -> {
            compareOpen = !compareOpen;
            alphabetOpen = false;
            syncDocumentVisuals(true);
        });
        bindClick(document, "nav-alphabet", () -> {
            alphabetOpen = !alphabetOpen;
            compareOpen = false;
            syncDocumentVisuals(true);
        });
        bindClick(document, "nav-generate", this::refreshRiddle);
        bindClick(document, "nav-cancel", this::onClose);
        bindClick(document, "nav-terminal", () -> {
            showingCurseSevenPage = true;
            refreshClientSnapshot();
            syncDocumentVisuals(true);
        });

        Element form = document.getElementById("answer-form");
        if (form != null) {
            form.addEventListener("submit", event -> {
                event.preventDefault();
                runOnClientThread(this::submit);
            });
        }

    }

    private void bindClick(Document document, String id, Runnable action) {
        Element element = document.getElementById(id);
        if (element != null) {
            element.addEventListener("click", event -> runOnClientThread(action));
        }
    }

    private void runOnClientThread(Runnable action) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.isSameThread()) {
            action.run();
        } else {
            minecraft.execute(action);
        }
    }

    private void submit() {
        Document document = getLinkedDocument();
        if (document == null) {
            return;
        }
        Element answer = document.getElementById("answer");
        if (answer == null) {
            return;
        }
        String value = answer.getValue() == null ? "" : answer.getValue().trim();
        if (value.isEmpty()) {
            return;
        }
        if (showingCurseSevenPage) {
            ModNetwork.CHANNEL.sendToServer(new CurseSevenRevealPacket(value));
        } else {
            ModNetwork.CHANNEL.sendToServer(satietyMode
                    ? new SatietyRiddleSubmitPacket(value)
                    : new EmberRiddleSubmitPacket(value));
        }
        clearAnswer(answer);
    }

    private void refreshRiddle() {
        if (satietyMode) {
            return;
        }
        if (showingCurseSevenPage) {
            showingCurseSevenPage = false;
            refreshClientSnapshot();
            syncDocumentVisuals(true);
            return;
        }
        if (ModEvents.isClientCurseSevenKeywordLockedForRiddleRefresh()) {
            return;
        }
        ModNetwork.CHANNEL.sendToServer(new EmberRiddleRefreshPacket());
        Document document = getLinkedDocument();
        if (document != null) {
            Element answer = document.getElementById("answer");
            if (answer != null) {
                clearAnswer(answer);
            }
        }
    }

    private void refreshClientSnapshot() {
        if (showingCurseSevenPage) {
            subtitle = ModEvents.getClientCurseSevenDuatHint();
            inscription = ModEvents.getClientCurseSevenDuatInscription();
        } else {
            subtitle = currentHint();
            inscription = currentInscription();
        }
        List<Component> latestTitles = currentTitles();
        if (!roundTitles.equals(latestTitles)) {
            roundTitles.clear();
            roundTitles.addAll(latestTitles);
        }
    }

    private void syncDocumentVisuals() {
        syncDocumentVisuals(false);
    }

    private void syncDocumentVisuals(boolean force) {
        Document document = getLinkedDocument();
        if (document == null) {
            return;
        }

        String signature = stateSignature();
        if (!force && signature.equals(visualSignature)) {
            return;
        }
        visualSignature = signature;

        Document.runWithContext(document, () -> {
            Element app = document.getElementById("app");
            if (app != null) {
                app.setClassName("app-shell"
                        + (compareOpen ? " is-compare-open" : "")
                        + (alphabetOpen ? " is-alphabet-open" : "")
                        + (showingCurseSevenPage ? " is-terminal" : "")
                        + (minimized ? " is-minimized" : "")
                        + (maximized ? " is-maximized" : "")
                        + (ModEvents.shouldShowCurseSevenDuatEntry() ? " has-terminal" : " no-terminal"));
            }
            setTextContent(document, "title-art", currentInscriptionTitle().getString());
            setTextContent(document, "prompt-art", subtitle == null ? "" : subtitle.getString());
            setInnerHtml(document, "glyph-art", buildCipherGlyphArt(inscription));
            setTextContent(document, "progress-art", progressLabel());
            setInnerHtml(document, "alphabet-art", buildAlphabetArt());
            setInnerHtml(document, "compare-art", buildComparisonArt());
            bindComparisonRows(document);
        });
    }

    private void bindComparisonRows(Document document) {
        int count = roundTitles.size();
        for (int index = 0; index < count; index++) {
            final int selectedIndex = index;
            Element row = document.getElementById("compare-row-" + index);
            if (row != null) {
                row.addEventListener("click", event -> runOnClientThread(() -> selectRiddle(selectedIndex)));
            }
        }
    }

    private void selectRiddle(int index) {
        if (index < 0 || index >= roundTitles.size() || currentSolved(index) || index == currentIndex()) {
            return;
        }
        ModNetwork.CHANNEL.sendToServer(satietyMode
                ? new SatietyRiddleSelectPacket(index)
                : new EmberRiddleSelectPacket(index));
    }

    private void setInnerHtml(Document document, String id, String markup) {
        Element element = document.getElementById(id);
        if (element != null) {
            element.setInnerHTML(markup);
        }
    }

    private void setTextContent(Document document, String id, String text) {
        Element element = document.getElementById(id);
        if (element != null) {
            element.setTextContent(text == null ? "" : text);
        }
    }

    private String progressLabel() {
        int size = Math.max(1, currentRoundSize());
        int index = Math.max(0, Math.min(size - 1, currentIndex()));
        return (index + 1) + " / " + size;
    }

    private String stateSignature() {
        StringBuilder signature = new StringBuilder();
        signature.append(showingCurseSevenPage).append('|')
                .append(compareOpen).append('|')
                .append(alphabetOpen).append('|')
                .append(minimized).append('|')
                .append(maximized).append('|')
                .append(currentIndex()).append('|')
                .append(currentRoundSize()).append('|')
                .append(currentInscriptionTitle().getString()).append('|')
                .append(inscription).append('|')
                .append(subtitle);
        for (Component title : roundTitles) {
            signature.append('|').append(title.getString());
        }
        return signature.toString();
    }

    private String buildArt(String seed, int limit, String className) {
        if (seed == null || seed.isEmpty()) {
            return "<i class=\"empty-stroke\"></i>";
        }
        StringBuilder markup = new StringBuilder();
        int count = Math.min(limit, seed.length());
        for (int i = 0; i < count; i++) {
            int value = seed.charAt(i);
            int width = 18 + Math.floorMod(value * 7 + i * 11, 38);
            int height = 3 + Math.floorMod(value * 3 + i * 5, 6);
            int angle = Math.floorMod(value + i * 13, 17) - 8;
            markup.append("<i class=\"").append(className)
                    .append("\" style=\"width:").append(width)
                    .append("px;height:").append(height)
                    .append("px;transform:rotate(").append(angle)
                    .append("deg);animation-delay:-").append(i * 90)
                    .append("ms\"></i>");
        }
        return markup.toString();
    }

    private String buildGlyphArt(String glyphs) {
        if (glyphs == null || glyphs.isBlank()) {
            return "<i class=\"empty-glyph\"></i>";
        }
        StringBuilder markup = new StringBuilder();
        int count = Math.min(64, glyphs.length());
        for (int i = 0; i < count; i++) {
            int value = glyphs.charAt(i);
            int width = 12 + Math.floorMod(value * 5 + i * 3, 22);
            int height = 22 + Math.floorMod(value * 11 + i * 7, 46);
            int angle = Math.floorMod(value + i * 9, 21) - 10;
            markup.append("<i class=\"glyph-cell\" style=\"width:").append(width)
                    .append("px;height:").append(height)
                    .append("px;transform:rotate(").append(angle)
                    .append("deg);animation-delay:-").append(i * 55)
                    .append("ms\"><i class=\"glyph-core\"></i></i>");
        }
        return markup.toString();
    }

    private String buildCipherGlyphArt(String glyphs) {
        if (glyphs == null || glyphs.isBlank()) {
            return "<span class=\"cipher-empty\">暂无碑文</span>";
        }
        StringBuilder markup = new StringBuilder();
        String[] lines = glyphs.split("\\r?\\n", -1);
        for (String line : lines) {
            markup.append("<span class=\"cipher-line\">");
            for (int i = 0; i < line.length(); i++) {
                int glyphIndex = cipherGlyphIndex(line.charAt(i));
                if (glyphIndex < 0) {
                    continue;
                }
                int column = glyphIndex % 16;
                int row = glyphIndex / 16;
                String xPosition = String.format(Locale.ROOT, "%.4f", column * 100.0D / 15.0D);
                markup.append("<span class=\"cipher-glyph\" style=\"background-position:")
                        .append(xPosition)
                        .append("% ")
                        .append(row * 100)
                        .append("%\"></span>");
            }
            markup.append("</span>");
        }
        return markup.toString();
    }

    private String buildAlphabetArt() {
        StringBuilder markup = new StringBuilder();
        for (int index = 0; index < 26; index++) {
            int column = index % 16;
            int row = index / 16;
            String xPosition = String.format(Locale.ROOT, "%.4f", column * 100.0D / 15.0D);
            markup.append("<span class=\"alphabet-cell\">")
                    .append("<span class=\"alphabet-glyph\" style=\"background-position:")
                    .append(xPosition)
                    .append("% ")
                    .append(row * 100)
                    .append("%\"></span>")
                    .append("<span class=\"alphabet-letter\">")
                    .append((char) ('A' + index))
                    .append("</span></span>");
        }
        return markup.toString();
    }

    private int cipherGlyphIndex(char value) {
        if (value >= '\uE000' && value <= '\uE01F') {
            return value - '\uE000';
        }
        char upper = Character.toUpperCase(value);
        return upper >= 'A' && upper <= 'Z' ? upper - 'A' : -1;
    }

    private String buildProgressArt() {
        int size = Math.max(1, Math.min(12, currentRoundSize()));
        int index = Math.max(0, Math.min(size - 1, currentIndex()));
        StringBuilder markup = new StringBuilder();
        for (int i = 0; i < size; i++) {
            String state = i == index ? " progress-active" : currentSolved(i) ? " progress-solved" : "";
            markup.append("<i class=\"progress-dot").append(state).append("\"></i>");
        }
        return markup.toString();
    }

    private String buildComparisonArt() {
        if (roundTitles.isEmpty()) {
            return "<i class=\"empty-stroke\"></i>";
        }
        StringBuilder markup = new StringBuilder();
        int count = roundTitles.size();
        for (int i = 0; i < count; i++) {
            String state = i == currentIndex() ? " compare-current" : currentSolved(i) ? " compare-solved" : "";
            markup.append("<button type=\"button\" id=\"compare-row-").append(i)
                    .append("\" class=\"compare-row").append(state)
                    .append("\">")
                    .append(escapeHtml(roundTitles.get(i).getString()))
                    .append("</button>");
        }
        return markup.toString();
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private Component currentHint() {
        return satietyMode ? ModEvents.getClientCurrentSatietyRiddleHint() : ModEvents.getClientCurrentEmberRiddleHint();
    }

    private String currentInscription() {
        return satietyMode ? ModEvents.getClientCurrentSatietyRiddleInscription() : ModEvents.getClientCurrentEmberRiddleInscription();
    }

    private List<Component> currentTitles() {
        return satietyMode ? ModEvents.ClientOverlayEvents.getClientSatietyRiddleTitles()
                : ModEvents.ClientOverlayEvents.getClientEmberRiddleTitles();
    }

    private Component currentInscriptionTitle() {
        if (showingCurseSevenPage) {
            return Component.literal("终末碑文");
        }
        int index = currentIndex();
        return index >= 0 && index < roundTitles.size() ? roundTitles.get(index) : Component.literal("碑文");
    }

    private int currentRoundSize() {
        return satietyMode ? ModEvents.ClientOverlayEvents.getClientSatietyRiddleRoundSize()
                : ModEvents.ClientOverlayEvents.getClientEmberRiddleRoundSize();
    }

    private int currentIndex() {
        return satietyMode ? ModEvents.ClientOverlayEvents.getClientSatietyRiddleIndex()
                : ModEvents.ClientOverlayEvents.getClientEmberRiddleIndex();
    }

    private boolean currentSolved(int index) {
        return satietyMode ? ModEvents.ClientOverlayEvents.isClientSatietyRiddleSolved(index)
                : ModEvents.ClientOverlayEvents.isClientEmberRiddleSolved(index);
    }

    public boolean isSatietyMode() {
        return satietyMode;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
