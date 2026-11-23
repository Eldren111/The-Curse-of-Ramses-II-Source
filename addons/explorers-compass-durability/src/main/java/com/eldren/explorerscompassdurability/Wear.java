package com.eldren.explorerscompassdurability;

/** Overflow-safe arithmetic, shared by the game hook and boundary tests. */
public final class Wear {
    private Wear() {}

    public static int afterUse(int currentDamage, int cost, int maximum) {
        return (int) Math.min((long) maximum, Math.max(0L, currentDamage) + Math.max(0L, cost));
    }
}
