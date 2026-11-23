package com.eldren.wutao.ramesses_curse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID, value = Dist.CLIENT)
public final class CurseNineSealClient {
    private CurseNineSealClient() {}

    public static void update(int ticks) {
        var player = Minecraft.getInstance().player;
        if (player != null) player.getPersistentData().putInt(CurseNineSeal.CLIENT_TICKS, ticks);
    }

    private static boolean sealed() {
        return CurseNineSeal.isSealed(Minecraft.getInstance().player);
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var player = Minecraft.getInstance().player;
        if (player != null) update(Math.max(0, ModEvents.getCurseNineFoodSealTicks(player) - 1));
    }

    @SubscribeEvent
    public static void interact(InputEvent.InteractionKeyMappingTriggered event) {
        if (sealed()) {
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void scroll(InputEvent.MouseScrollingEvent event) {
        if (sealed()) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void click(ScreenEvent.MouseButtonPressed.Pre event) {
        if (sealed() && event.getScreen() instanceof AbstractContainerScreen<?>) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void release(ScreenEvent.MouseButtonReleased.Pre event) {
        if (sealed() && event.getScreen() instanceof AbstractContainerScreen<?>) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void key(ScreenEvent.KeyPressed.Pre event) {
        if (sealed() && event.getScreen() instanceof AbstractContainerScreen<?>
                && event.getKeyCode() != GLFW.GLFW_KEY_ESCAPE
                && !Minecraft.getInstance().options.keyInventory.matches(event.getKeyCode(), event.getScanCode())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void render(ScreenEvent.Render.Post event) {
        if (!sealed() || !(event.getScreen() instanceof AbstractContainerScreen<?> screen)) return;
        for (Slot slot : screen.getMenu().slots) {
            if (slot instanceof top.theillusivec4.curios.common.inventory.CurioSlot) continue;
            int x = screen.getGuiLeft() + slot.x;
            int y = screen.getGuiTop() + slot.y;
            event.getGuiGraphics().fill(x, y, x + 16, y + 16, 0x99331A45);
        }
        var minecraft = Minecraft.getInstance();
        int seconds = (ModEvents.getCurseNineFoodSealTicks(minecraft.player) + 19) / 20;
        event.getGuiGraphics().drawCenteredString(minecraft.font,
                Component.translatable("hud.ramesses_curse.satiety_full_seal", seconds),
                screen.width / 2, Math.max(2, screen.getGuiTop() - 12), 0xFFFFCC66);
    }
}
