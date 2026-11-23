package com.eldren.wutao.ramesses_curse;

import com.eldren.wutao.ramesses_curse.dynamictext.api.DynamicTextApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.common.inventory.CurioSlot;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID, value = Dist.CLIENT)
public final class DuatCuriosSealClient {
    private static boolean sealed;
    private static int overlayAlpha = 170;
    private static Set<String> whitelist = Set.of();

    private DuatCuriosSealClient() {
    }

    public static void update(boolean active, int alpha, List<String> allowedItems) {
        sealed = active;
        overlayAlpha = Math.max(0, Math.min(255, alpha));
        whitelist = Set.copyOf(new HashSet<>(allowedItems));
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.getPersistentData()
                    .putBoolean(DuatCuriosSealManager.ACTIVE_KEY, active);
        }
    }

    @SubscribeEvent
    public static void renderSealedSlots(ScreenEvent.Render.Post event) {
        if (!sealed || overlayAlpha <= 0
                || !(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        int left = screen.getGuiLeft();
        int top = screen.getGuiTop();
        float pulse = 0.5F + 0.5F * (float) Math.sin(System.currentTimeMillis() * 0.004D);
        int alpha = Math.min(255, overlayAlpha + Math.round(pulse * 28.0F));
        for (Slot slot : screen.getMenu().slots) {
            if (!(slot instanceof CurioSlot curioSlot) || curioSlot.isCosmetic()) {
                continue;
            }
            ItemStack stack = slot.getItem();
            if (stack.isEmpty() || isWhitelisted(stack)) {
                continue;
            }
            int x = left + slot.x;
            int y = top + slot.y;
            int color = (alpha << 24) | 0xF4F7FF;
            event.getGuiGraphics().fill(x, y, x + 16, y + 1, color);
            event.getGuiGraphics().fill(x, y + 15, x + 16, y + 16, color);
            event.getGuiGraphics().fill(x, y + 1, x + 1, y + 15, color);
            event.getGuiGraphics().fill(x + 15, y + 1, x + 16, y + 15, color);
        }
    }

    @SubscribeEvent
    public static void appendSealedCurioTooltip(ItemTooltipEvent event) {
        if (!sealed || isWhitelisted(event.getItemStack())
                || !(Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        Slot hoveredSlot = screen.getSlotUnderMouse();
        if (!(hoveredSlot instanceof CurioSlot curioSlot) || curioSlot.isCosmetic()
                || !ItemStack.isSameItemSameTags(hoveredSlot.getItem(), event.getItemStack())) {
            return;
        }
        event.getToolTip().add(DynamicTextApi.parse(
                Component.literal("&u").append(Component.translatable("tooltip.ramesses_curse.curios_sealed"))));
    }

    private static boolean isWhitelisted(ItemStack stack) {
        return whitelist.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
    }
}
