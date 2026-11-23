package com.eldren.wutao.ramesses_requiem.boss;

import net.minecraftforge.common.ForgeConfigSpec;

public final class WusheConfig {
    private static final ForgeConfigSpec.Builder B = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.DoubleValue OVERWORLD_HEALTH = B.defineInRange("overworldHealth", 666D, 1D, 1000000D);
    public static final ForgeConfigSpec.DoubleValue NETHER_HEALTH = B.defineInRange("netherHealth", 1666D, 1D, 1000000D);
    public static final ForgeConfigSpec.DoubleValue END_HEALTH = B.defineInRange("endHealth", 6666D, 1D, 1000000D);
    public static final ForgeConfigSpec.DoubleValue OVERWORLD_THRESHOLD = B.defineInRange("overworldHitThreshold", .55D, 0D, 1D);
    public static final ForgeConfigSpec.DoubleValue NETHER_THRESHOLD = B.defineInRange("netherHitThreshold", .35D, 0D, 1D);
    public static final ForgeConfigSpec.DoubleValue END_THRESHOLD = B.defineInRange("endHitThreshold", .15D, 0D, 1D);
    public static final ForgeConfigSpec.DoubleValue CAPPED_DAMAGE = B.defineInRange("overThresholdDamage", 100D, 0D, 100000D);
    public static final ForgeConfigSpec.IntValue IMMUNITY_TICKS = B.defineInRange("cappedHitImmunityTicks", 10, 1, 200);
    public static final ForgeConfigSpec.DoubleValue PHASE_ONE_REDUCTION = B.defineInRange("phaseOneReduction", .1D, 0D, .95D);
    public static final ForgeConfigSpec.DoubleValue PHASE_TWO_REDUCTION = B.defineInRange("phaseTwoReduction", .2D, 0D, .95D);
    public static final ForgeConfigSpec.DoubleValue PHASE_THREE_REDUCTION = B.defineInRange("phaseThreeReduction", .3D, 0D, .95D);
    public static final ForgeConfigSpec.DoubleValue MELEE_DAMAGE = B.defineInRange("meleeDamage", 12D, 0D, 10000D);
    public static final ForgeConfigSpec.DoubleValue WAVE_DAMAGE = B.defineInRange("waveDamage", 12D, 0D, 10000D);
    public static final ForgeConfigSpec.DoubleValue BACKSTAB_DAMAGE = B.defineInRange("backstabTrueDamage", 50D, 0D, 10000D);
    public static final ForgeConfigSpec.DoubleValue SHARD_DAMAGE = B.defineInRange("backstabShardMagicDamage", 5D, 0D, 10000D);
    public static final ForgeConfigSpec.IntValue AMBUSH_COOLDOWN = B.defineInRange("ambushCooldownSeconds", 30, 1, 3600);
    public static final ForgeConfigSpec.IntValue SWEEP_COOLDOWN = B.defineInRange("sweepCooldownSeconds", 12, 1, 3600);
    public static final ForgeConfigSpec.IntValue DANCE_DURATION = B.defineInRange("bladeDanceDurationSeconds", 30, 1, 180);
    public static final ForgeConfigSpec.IntValue DANCE_COOLDOWN = B.comment("Cooldown begins after the 30-second sequence finishes.").defineInRange("bladeDanceCooldownSeconds", 30, 1, 3600);
    public static final ForgeConfigSpec.IntValue BACKSTAB_COOLDOWN = B.defineInRange("backstabCooldownSeconds", 60, 1, 3600);
    public static final ForgeConfigSpec.IntValue ARENA_DURATION = B.defineInRange("astralDuelDurationSeconds", 180, 1, 3600);
    public static final ForgeConfigSpec.IntValue ARENA_COOLDOWN = B.defineInRange("astralDuelCooldownSeconds", 240, 1, 7200);
    public static final ForgeConfigSpec SPEC = B.build();
    public static double health(int tier) { return (tier == 2 ? END_HEALTH : tier == 1 ? NETHER_HEALTH : OVERWORLD_HEALTH).get(); }
    public static double threshold(int tier) { return (tier == 2 ? END_THRESHOLD : tier == 1 ? NETHER_THRESHOLD : OVERWORLD_THRESHOLD).get(); }
    public static double reduction(int phase) { return (phase == 3 ? PHASE_THREE_REDUCTION : phase == 2 ? PHASE_TWO_REDUCTION : PHASE_ONE_REDUCTION).get(); }
    public static int cooldown(int skill) { return 20 * switch (skill) {
        case 1 -> AMBUSH_COOLDOWN.get(); case 2 -> SWEEP_COOLDOWN.get(); case 3 -> DANCE_COOLDOWN.get();
        case 4 -> BACKSTAB_COOLDOWN.get(); case 5 -> ARENA_COOLDOWN.get(); default -> 30;
    }; }
    private WusheConfig() {}
}
