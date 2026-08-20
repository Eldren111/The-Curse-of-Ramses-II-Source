package com.eldren.wutao.ramesses_curse;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = RamessesCurseMod.MODID, value = Dist.CLIENT)
public final class DuatDesertClientEffects {
    private static final int SYNC_TIMEOUT_TICKS = 40;
    private static boolean sandstormActive;
    private static double stormCenterX;
    private static double stormCenterZ;
    private static double stormVelocityX;
    private static double stormVelocityZ;
    private static double stormRadius;
    private static float stormIntensity;
    private static int stormRemainingTicks;
    private static int syncTimeout;

    private DuatDesertClientEffects() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().isPaused()) {
            return;
        }
        tickSyncedStorm();
    }

    static void applySandstormSync(DuatSandstormSyncPacket packet) {
        sandstormActive = packet.active();
        stormCenterX = packet.centerX();
        stormCenterZ = packet.centerZ();
        stormVelocityX = packet.velocityX();
        stormVelocityZ = packet.velocityZ();
        stormRadius = packet.radius();
        stormIntensity = packet.intensity();
        stormRemainingTicks = packet.remainingTicks();
        syncTimeout = packet.active() ? SYNC_TIMEOUT_TICKS : 0;
    }

    static boolean shouldRender(Minecraft minecraft) {
        return sandstormActive
                && minecraft.level != null
                && minecraft.player != null
                && minecraft.level.dimension() == ModDimensions.DUAT_DESERT
                && localIntensity(minecraft, 0.0F) > 0.001F;
    }

    static float localIntensity(Minecraft minecraft, float partialTick) {
        if (!sandstormActive || minecraft.player == null) {
            return 0.0F;
        }

        double centerX = stormCenterX + stormVelocityX * partialTick;
        double centerZ = stormCenterZ + stormVelocityZ * partialTick;
        double dx = minecraft.player.getX() - centerX;
        double dz = minecraft.player.getZ() - centerZ;
        double distance = Math.sqrt(dx * dx + dz * dz);
        float influence = 1.0F - Mth.clamp((float) ((distance - stormRadius * 0.25D) / stormRadius), 0.0F, 1.0F);
        return Mth.clamp(stormIntensity * influence, 0.0F, 1.0F);
    }

    static float stormCenterOffsetX(Minecraft minecraft, float partialTick) {
        return (float) (stormCenterX + stormVelocityX * partialTick - minecraft.gameRenderer.getMainCamera().getPosition().x);
    }

    static float stormCenterOffsetZ(Minecraft minecraft, float partialTick) {
        return (float) (stormCenterZ + stormVelocityZ * partialTick - minecraft.gameRenderer.getMainCamera().getPosition().z);
    }

    static float stormRadius() {
        return (float) stormRadius;
    }

    static float windX() {
        double length = Math.sqrt(stormVelocityX * stormVelocityX + stormVelocityZ * stormVelocityZ);
        return length > 1.0E-5D ? (float) (stormVelocityX / length) : 1.0F;
    }

    static float windZ() {
        double length = Math.sqrt(stormVelocityX * stormVelocityX + stormVelocityZ * stormVelocityZ);
        return length > 1.0E-5D ? (float) (stormVelocityZ / length) : 0.0F;
    }

    private static void tickSyncedStorm() {
        if (!sandstormActive) {
            return;
        }

        stormCenterX += stormVelocityX;
        stormCenterZ += stormVelocityZ;
        stormRemainingTicks--;
        syncTimeout--;
        if (stormRemainingTicks <= 0 || syncTimeout <= 0) {
            sandstormActive = false;
        }
    }
}

