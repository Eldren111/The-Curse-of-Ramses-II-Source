package com.eldren.wutao.ramesses_curse;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = RamessesCurseMod.MODID, value = Dist.CLIENT)
public final class NebukaRitualClientEffects {
    private static int shakeTicks;
    private static int shakeTotalTicks;
    private static float shakeStrength;

    private NebukaRitualClientEffects() {
    }

    public static void startShake(int durationTicks, float strength) {
        if (durationTicks <= 0 || strength <= 0.0F) {
            return;
        }
        if (shakeTicks <= 0 || strength >= shakeStrength) {
            shakeTicks = durationTicks;
            shakeTotalTicks = durationTicks;
            shakeStrength = strength;
            return;
        }
        shakeTicks = Math.max(shakeTicks, durationTicks);
        shakeTotalTicks = Math.max(shakeTotalTicks, shakeTicks);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (shakeTicks <= 0) {
            return;
        }
        shakeTicks--;
        if (shakeTicks <= 0) {
            shakeTotalTicks = 0;
            shakeStrength = 0.0F;
        }
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (shakeTicks <= 0 || shakeStrength <= 0.0F) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        float remaining = shakeTicks / (float) Math.max(1, shakeTotalTicks);
        float intensity = shakeStrength * Math.max(0.18F, remaining);
        float tick = minecraft.player.tickCount + (float) event.getPartialTick();
        event.setYaw(event.getYaw() + Mth.sin(tick * 1.73F) * intensity);
        event.setPitch(event.getPitch() + Mth.cos(tick * 2.11F) * intensity * 0.62F);
        event.setRoll(event.getRoll() + Mth.sin(tick * 2.47F) * intensity * 0.42F);
    }
}

