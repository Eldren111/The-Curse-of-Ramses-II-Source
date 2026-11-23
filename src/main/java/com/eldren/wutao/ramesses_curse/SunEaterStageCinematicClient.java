package com.eldren.wutao.ramesses_curse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID, value = Dist.CLIENT)
public final class SunEaterStageCinematicClient {
    private static final int RETARGET_TICKS = 10;
    private static Vec3 focus;
    private static Vec3 previousFocus;
    private static ClientLevel activeLevel;
    private static int remainingTicks;
    private static int totalTicks;
    private static int retargetTicks;

    private SunEaterStageCinematicClient() {
    }

    public static void start(double x, double y, double z, int durationTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        if (durationTicks <= 0) {
            clear();
            return;
        }
        if (minecraft.level == null) {
            return;
        }
        if (activeLevel == minecraft.level && remainingTicks > 0 && totalTicks > 0) {
            previousFocus = currentFocus(0.0F);
            focus = new Vec3(x, y, z);
            remainingTicks = durationTicks;
            retargetTicks = RETARGET_TICKS;
            return;
        }
        focus = new Vec3(x, y, z);
        previousFocus = focus;
        activeLevel = minecraft.level;
        remainingTicks = durationTicks;
        totalTicks = durationTicks;
        retargetTicks = 0;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || remainingTicks <= 0) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.level != activeLevel) {
            clear();
            return;
        }
        remainingTicks--;
        if (retargetTicks > 0) {
            retargetTicks--;
        }
        if (remainingTicks <= 0) {
            clear();
        }
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        float strength = focusStrength(event.getPartialTick());
        if (strength <= 0.0F || focus == null) {
            return;
        }
        Vec3 camera = event.getCamera().getPosition();
        Vec3 direction = currentFocus((float) event.getPartialTick()).subtract(camera);
        double horizontal = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        float targetYaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        float targetPitch = (float) -Math.toDegrees(Math.atan2(direction.y, horizontal));
        event.setYaw(Mth.rotLerp(strength, event.getYaw(), targetYaw));
        event.setPitch(Mth.lerp(strength, event.getPitch(), targetPitch));
        event.setRoll(Mth.lerp(strength, event.getRoll(), 0.0F));
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        float strength = focusStrength(event.getPartialTick());
        if (strength > 0.0F) {
            event.setFOV(event.getFOV() * Mth.lerp(strength, 1.0D, 0.58D));
        }
    }

    private static float focusStrength(double partialTick) {
        if (remainingTicks <= 0 || totalTicks <= 0 || focus == null) {
            return 0.0F;
        }
        float elapsed = totalTicks - remainingTicks + (float) partialTick;
        float fadeIn = Mth.clamp(elapsed / 12.0F, 0.0F, 1.0F);
        float fadeOut = Mth.clamp((remainingTicks - (float) partialTick) / 12.0F, 0.0F, 1.0F);
        float strength = Math.min(fadeIn, fadeOut);
        return strength * strength * (3.0F - 2.0F * strength);
    }

    private static Vec3 currentFocus(float partialTick) {
        if (focus == null) {
            return Vec3.ZERO;
        }
        if (previousFocus == null || retargetTicks <= 0) {
            return focus;
        }
        float progress = Mth.clamp(
                (RETARGET_TICKS - retargetTicks + partialTick) / RETARGET_TICKS,
                0.0F, 1.0F);
        progress = progress * progress * (3.0F - 2.0F * progress);
        return previousFocus.lerp(focus, progress);
    }

    private static void clear() {
        focus = null;
        previousFocus = null;
        activeLevel = null;
        remainingTicks = 0;
        totalTicks = 0;
        retargetTicks = 0;
    }
}
