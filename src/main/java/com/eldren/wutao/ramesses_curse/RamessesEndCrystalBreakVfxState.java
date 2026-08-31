package com.eldren.wutao.ramesses_curse;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class RamessesEndCrystalBreakVfxState {
    public static final int DURATION_TICKS = 14;
    private static final List<Shockwave> SHOCKWAVES = new ArrayList<>();

    private RamessesEndCrystalBreakVfxState() {
    }

    public static void trigger(Vec3 position, long gameTime) {
        SHOCKWAVES.add(new Shockwave(position, gameTime));
        while (SHOCKWAVES.size() > 12) {
            SHOCKWAVES.remove(0);
        }
    }

    public static List<Shockwave> activeShockwaves(long gameTime) {
        SHOCKWAVES.removeIf(shockwave -> gameTime < shockwave.startedAt()
                || gameTime - shockwave.startedAt() > DURATION_TICKS);
        return List.copyOf(SHOCKWAVES);
    }

    public static void clear() {
        SHOCKWAVES.clear();
    }

    public record Shockwave(Vec3 position, long startedAt) {
        public float progress(long gameTime, float partialTick) {
            return Mth.clamp((gameTime - startedAt + partialTick) / DURATION_TICKS, 0.0F, 1.0F);
        }
    }
}
