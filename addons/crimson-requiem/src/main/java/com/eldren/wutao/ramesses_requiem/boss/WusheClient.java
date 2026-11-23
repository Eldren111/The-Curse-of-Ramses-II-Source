package com.eldren.wutao.ramesses_requiem.boss;

import com.eldren.wutao.ramesses_curse.UraeusVfxState;
import com.eldren.wutao.ramesses_requiem.RequiemAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RequiemAddon.MODID, value = Dist.CLIENT)
public final class WusheClient {
    private static int bossId = -1, remaining, fadeRemaining, fadeDuration;
    private static boolean frozen;
    private static float frozenYaw, frozenPitch;
    public static void receive(WusheScenePacket packet) {
        Minecraft mc = Minecraft.getInstance();
        switch (packet.kind()) {
            case 0 -> { bossId = packet.bossId(); remaining = packet.ticks(); frozen = false; }
            case 1 -> { fadeDuration = fadeRemaining = packet.ticks(); }
            case 2 -> release();
            case 3 -> {
                bossId = packet.bossId(); remaining = packet.ticks(); frozen = true;
                if (mc.player != null) { frozenYaw = mc.player.getYRot(); frozenPitch = mc.player.getXRot(); }
            }
            case 4 -> { if (mc.level != null) UraeusVfxState.trigger(packet.point(), false, mc.level.getGameTime()); }
        }
    }
    private static void release() { bossId = -1; remaining = 0; frozen = false; }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) { release(); fadeRemaining = 0; return; }
        if (mc.isPaused()) return;
        if (fadeRemaining > 0) fadeRemaining--;
        if (remaining > 0) remaining--;
        if (remaining == 0 || !mc.player.isAlive() || !(mc.level.getEntity(bossId) instanceof WusheBoss boss) || !boss.isAlive()) release();
    }
    @SubscribeEvent public static void input(MovementInputUpdateEvent event) {
        if (!frozen || remaining <= 0) return;
        var input = event.getInput();
        input.forwardImpulse = 0; input.leftImpulse = 0; input.jumping = false;
        input.up = false; input.down = false; input.left = false; input.right = false;
        event.getEntity().setDeltaMovement(Vec3.ZERO);
    }
    @SubscribeEvent public static void camera(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (remaining <= 0 || mc.player == null || mc.level == null || !(mc.level.getEntity(bossId) instanceof WusheBoss boss)) return;
        Vec3 difference = boss.getEyePosition((float)event.getPartialTick()).subtract(mc.player.getEyePosition((float)event.getPartialTick()));
        float yaw = (float)Math.toDegrees(Math.atan2(-difference.x, difference.z));
        float pitch = (float)-Math.toDegrees(Math.atan2(difference.y, difference.horizontalDistance()));
        if (frozen) { yaw = frozenYaw; pitch = frozenPitch; }
        mc.player.setYRot(yaw); mc.player.setXRot(pitch);
        event.setYaw(yaw); event.setPitch(pitch); event.setRoll(0);
    }
    @SubscribeEvent public static void overlay(RenderGuiEvent.Post event) {
        if (fadeRemaining <= 0 || fadeDuration == 0) return;
        int elapsed = fadeDuration - fadeRemaining;
        float fade = Math.min(1, Math.min(elapsed / 8F, fadeRemaining / 12F));
        int alpha = Mth.clamp((int)(fade * 255), 0, 255);
        var window = Minecraft.getInstance().getWindow();
        event.getGuiGraphics().fill(0, 0, window.getGuiScaledWidth(), window.getGuiScaledHeight(), alpha << 24);
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { release(); fadeRemaining = 0; }
    private WusheClient() {}
}
