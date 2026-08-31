package com.eldren.wutao.ramesses_curse.integration.l2;

import com.eldren.wutao.ramesses_curse.Config;
import com.eldren.wutao.ramesses_curse.DynastyAffixData;
import com.eldren.wutao.ramesses_curse.ModEvents;
import com.eldren.wutao.ramesses_curse.ModItems;
import dev.xkmc.l2tabs.tabs.contents.BaseTextScreen;
import dev.xkmc.l2tabs.tabs.core.BaseTab;
import dev.xkmc.l2tabs.tabs.core.TabManager;
import dev.xkmc.l2tabs.tabs.core.TabRegistry;
import dev.xkmc.l2tabs.tabs.core.TabToken;
import dev.xkmc.l2tabs.init.data.L2TabsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class L2LibraryClientCompat {
    private static final int TAB_PRIORITY = 500;
    private static TabToken<DynastyStatusTab> dynastyStatusToken;

    private L2LibraryClientCompat() {
    }

    public static void registerTab() {
        if (dynastyStatusToken != null) {
            return;
        }
        dynastyStatusToken = TabRegistry.registerTab(
                TAB_PRIORITY,
                DynastyStatusTab::new,
                ModItems.DYNASTY_PRESSURE_POTION,
                Component.translatable("screen.ramesses_curse.dynasty_status.title"));
    }

    public static final class DynastyStatusTab extends BaseTab<DynastyStatusTab> {
        public DynastyStatusTab(TabToken<DynastyStatusTab> token, TabManager manager,
                                ItemStack stack, Component title) {
            super(token, manager, stack, title);
        }

        @Override
        public void onTabClicked() {
            Minecraft.getInstance().setScreen(new DynastyStatusScreen(this.getMessage(), 0));
        }
    }

    private static final class DynastyStatusScreen extends BaseTextScreen {
        private static final ResourceLocation EMPTY_TEXTURE =
                new ResourceLocation("l2tabs", "textures/gui/empty.png");
        private static final int BUTTON_WIDTH = 10;
        private static final int BUTTON_HEIGHT = 11;
        private final int page;

        private DynastyStatusScreen(Component title, int page) {
            super(title, EMPTY_TEXTURE);
            this.page = Math.max(0, page);
        }

        @Override
        public void init() {
            super.init();
            new TabManager(this).init(this::addRenderableWidget, dynastyStatusToken);

            int pageCount = pageCount(buildLines().size());
            int currentPage = Mth.clamp(this.page, 0, pageCount - 1);
            int buttonX = (this.width + this.imageWidth) / 2 - 16;
            int buttonY = (this.height - this.imageHeight) / 2 + 4;
            if (currentPage > 0) {
                this.addRenderableWidget(Button.builder(Component.literal("<"),
                                button -> openPage(currentPage - 1))
                        .pos(buttonX - BUTTON_WIDTH - 1, buttonY)
                        .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                        .build());
            }
            if (currentPage < pageCount - 1) {
                this.addRenderableWidget(Button.builder(Component.literal(">"),
                                button -> openPage(currentPage + 1))
                        .pos(buttonX, buttonY)
                        .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                        .build());
            }
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.render(graphics, mouseX, mouseY, partialTick);
            List<Component> lines = buildLines();
            int linesPerPage = linesPerPage();
            int currentPage = Mth.clamp(this.page, 0, pageCount(lines.size()) - 1);
            int start = currentPage * linesPerPage;
            int end = Math.min(start + linesPerPage, lines.size());
            int textX = this.leftPos + 8;
            int textY = this.topPos + 6;
            for (int index = start; index < end; index++) {
                graphics.drawString(this.font, lines.get(index), textX, textY, 0, false);
                textY += 10;
            }
        }

        private List<Component> buildLines() {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null) {
                return List.of();
            }
            L2LibraryCompat.ThreatSnapshot threat = L2LibraryCompat.getThreatSnapshot(minecraft.player);
            int effectiveThreat = threat.effective() > 0
                    ? threat.effective()
                    : ModEvents.ClientOverlayEvents.getClientDynastyThreat();
            int shieldLevel = ModEvents.getDynastyShieldLevelForThreat(effectiveThreat);
            int affixTier = ModEvents.getDynastyAffixTierForThreat(effectiveThreat);
            List<String> affixes = DynastyAffixData.availableAtTier(affixTier);
            List<Component> lines = new ArrayList<>();
            lines.add(Component.translatable("screen.ramesses_curse.dynasty_status.threat", effectiveThreat));
            lines.add(Component.translatable("screen.ramesses_curse.dynasty_status.shield_level", shieldLevel));
            lines.add(Component.translatable("screen.ramesses_curse.dynasty_status.affix_tier", affixTier));
            lines.add(Component.translatable("screen.ramesses_curse.dynasty_status.guilt",
                    ModEvents.ClientOverlayEvents.getClientGuilt(), Config.CURSE_5_MAX_GUILT.get()));
            if (ModEvents.ClientOverlayEvents.isClientCurseFiveReversed()) {
                Component mercy = Component.translatable("screen.ramesses_curse.dynasty_status.mercy",
                        ModEvents.ClientOverlayEvents.getClientMercy(), Config.CURSE_5_MERCY_MAX.get());
                if (ModEvents.ClientOverlayEvents.isClientMercyChargeReady()) {
                    mercy = mercy.copy().append(Component.translatable(
                            "screen.ramesses_curse.dynasty_status.mercy_ready"));
                }
                lines.add(mercy);
            }
            lines.add(Component.translatable("screen.ramesses_curse.dynasty_status.affixes"));
            if (affixes.isEmpty()) {
                lines.add(Component.translatable("screen.ramesses_curse.dynasty_status.no_affixes"));
            } else {
                for (String id : affixes) {
                    lines.add(Component.translatable("screen.ramesses_curse.dynasty_status.affix_entry",
                            Component.translatable("affix.ramesses_curse." + id),
                            DynastyAffixData.level(id, affixTier)));
                }
            }
            return lines;
        }

        private void openPage(int page) {
            Minecraft.getInstance().setScreen(new DynastyStatusScreen(this.title, page));
        }

        private static int linesPerPage() {
            return Math.max(1, L2TabsConfig.CLIENT.attributeLinePerPage.get());
        }

        private static int pageCount(int lineCount) {
            return Math.max(1, Mth.ceil(lineCount / (float) linesPerPage()));
        }
    }
}
