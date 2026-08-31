package com.eldren.wutao.ramesses_curse;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class DuatSunEaterDeathVfxState {
    public static final int DURATION_TICKS = 157;
    public static final float MAX_RADIUS = 108.0F;
    private static Blast activeBlast;
    private static long nextRingingTick;

    private DuatSunEaterDeathVfxState() {
    }

    public static void trigger(Vec3 origin, long gameTime) {
        activeBlast = new Blast(origin, gameTime);
        nextRingingTick = gameTime;
    }

    public static Blast activeBlast(long gameTime) {
        if (activeBlast != null
                && (gameTime < activeBlast.startedAt()
                || gameTime - activeBlast.startedAt() > DURATION_TICKS)) {
            activeBlast = null;
        }
        return activeBlast;
    }

    public static void clear() {
        activeBlast = null;
    }

    public static float consumeRingingVolume(long gameTime) {
        if (activeBlast == null || gameTime < nextRingingTick) {
            return 0.0F;
        }
        long elapsed = gameTime - activeBlast.startedAt();
        if (elapsed < 0L || elapsed > 100L) {
            return 0.0F;
        }
        nextRingingTick = gameTime + 12L;
        return 0.95F * (1.0F - elapsed / 120.0F);
    }

    public record Blast(Vec3 origin, long startedAt) {
        public float progress(long gameTime, float partialTick) {
            return Mth.clamp((gameTime - startedAt + partialTick) / DURATION_TICKS, 0.0F, 1.0F);
        }

        public int flashAlpha(long gameTime, float partialTick) {
            float elapsed = gameTime - startedAt + partialTick;
            if (elapsed <= 12.0F) {
                return 255;
            }
            return Mth.clamp(Mth.floor(255.0F * (1.0F - (elapsed - 12.0F) / 48.0F)), 0, 255);
        }
    }
}
