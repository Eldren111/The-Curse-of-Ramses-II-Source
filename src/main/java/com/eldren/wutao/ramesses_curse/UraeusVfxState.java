package com.eldren.wutao.ramesses_curse;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class UraeusVfxState {
    public static final int DURATION_TICKS = 12;
    private static final List<Burst> BURSTS = new ArrayList<>();

    private UraeusVfxState() {
    }

    public static void trigger(Vec3 position, boolean rainEmpowered, long gameTime) {
        for (int index = BURSTS.size() - 1; index >= 0; index--) {
            Burst existing = BURSTS.get(index);
            if (gameTime - existing.startedAt() > 2L) {
                break;
            }
            if (existing.rainEmpowered() == rainEmpowered
                    && existing.position().distanceToSqr(position) < 9.0D) {
                return;
            }
        }
        BURSTS.add(new Burst(position, rainEmpowered, gameTime));
        while (BURSTS.size() > 8) {
            BURSTS.remove(0);
        }
    }

    public static List<Burst> activeBursts(long gameTime) {
        BURSTS.removeIf(burst -> gameTime < burst.startedAt()
                || gameTime - burst.startedAt() > DURATION_TICKS);
        return List.copyOf(BURSTS);
    }

    public record Burst(Vec3 position, boolean rainEmpowered, long startedAt) {
        public float progress(long gameTime, float partialTick) {
            return Mth.clamp((gameTime - startedAt + partialTick) / DURATION_TICKS, 0.0F, 1.0F);
        }
    }
}
