package com.eldren.wutao.ramesses_curse;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class SunEaterStageRitualVfxState {
    public static final int BLAST_TICKS = 30;
    private static Ritual ritual;
    private static Blast blast;

    private SunEaterStageRitualVfxState() {
    }

    public static void sync(
            Vec3 origin, int stage, int elapsedTicks, int durationTicks, long gameTime) {
        if (durationTicks < 0) {
            ritual = null;
            blast = new Blast(origin, Mth.clamp(stage, 2, 3), gameTime);
        } else if (durationTicks == 0) {
            ritual = null;
        } else {
            ritual = new Ritual(origin, Mth.clamp(stage, 2, 3),
                    gameTime - Mth.clamp(elapsedTicks, 0, durationTicks), durationTicks);
            blast = null;
        }
    }

    public static Ritual activeRitual(long gameTime) {
        if (ritual != null && ritual.progress(gameTime, 0.0F) >= 1.0F) {
            ritual = null;
        }
        return ritual;
    }

    public static Blast activeBlast(long gameTime) {
        if (blast != null && gameTime - blast.startedAt() > BLAST_TICKS) {
            blast = null;
        }
        return blast;
    }

    public static void clear() {
        ritual = null;
        blast = null;
    }

    public record Ritual(Vec3 origin, int stage, long startedAt, int durationTicks) {
        public float progress(long gameTime, float partialTick) {
            return Mth.clamp((gameTime - startedAt + partialTick) / durationTicks, 0.0F, 1.0F);
        }
    }

    public record Blast(Vec3 origin, int stage, long startedAt) {
        public float progress(long gameTime, float partialTick) {
            return Mth.clamp((gameTime - startedAt + partialTick) / BLAST_TICKS, 0.0F, 1.0F);
        }
    }
}
