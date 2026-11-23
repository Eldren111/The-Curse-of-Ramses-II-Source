package com.eldren.wutao.ramesses_curse;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class DuatBlackHoleVfxState {
    private static BlackHole activeBlackHole;

    private DuatBlackHoleVfxState() {
    }

    public static void trigger(Vec3 origin, long gameTime, int durationTicks) {
        if (durationTicks <= 0) {
            clear();
            return;
        }
        activeBlackHole = new BlackHole(origin, gameTime, Math.max(1, durationTicks));
    }

    public static BlackHole activeBlackHole(long gameTime) {
        if (activeBlackHole != null
                && (gameTime < activeBlackHole.startedAt()
                || gameTime - activeBlackHole.startedAt() > activeBlackHole.durationTicks())) {
            activeBlackHole = null;
        }
        return activeBlackHole;
    }

    public static void clear() {
        activeBlackHole = null;
    }

    public record BlackHole(Vec3 origin, long startedAt, int durationTicks) {
        public float elapsed(long gameTime, float partialTick) {
            return Mth.clamp(gameTime - startedAt + partialTick, 0.0F, durationTicks);
        }

        public float progress(long gameTime, float partialTick) {
            return elapsed(gameTime, partialTick) / durationTicks;
        }

        public float intensity(long gameTime, float partialTick) {
            float elapsed = elapsed(gameTime, partialTick);
            float growth = Mth.clamp(elapsed / 30.0F, 0.0F, 1.0F);
            float collapse = Mth.clamp((durationTicks - elapsed) / 20.0F, 0.0F, 1.0F);
            return growth * growth * (3.0F - 2.0F * growth) * collapse;
        }

        public float collapseProgress(long gameTime, float partialTick) {
            float collapseStart = Math.max(0.0F, durationTicks - 20.0F);
            return Mth.clamp((elapsed(gameTime, partialTick) - collapseStart) / 20.0F,
                    0.0F, 1.0F);
        }
    }
}
