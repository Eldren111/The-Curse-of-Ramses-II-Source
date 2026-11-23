package com.eldren.wutao.ramesses_requiem;

import net.minecraftforge.common.ForgeConfigSpec;

/** Server-owned settings, synchronized by Forge. Times are in seconds. */
public final class CrimsonRequiemConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.DoubleValue ATTACK_DAMAGE = BUILDER.comment(
            "Total melee attack damage before Sharpness and other modifiers.").defineInRange("attackDamage", 20D, 1D, 10000D);
    public static final ForgeConfigSpec.DoubleValue ATTACK_SPEED = BUILDER.defineInRange("attackSpeed", 1.6D, 0.1D, 20D);
    public static final ForgeConfigSpec.DoubleValue TRUE_DAMAGE = BUILDER.comment(
            "Base true damage per hit; adds 3 times the caster's current health.").defineInRange("boomerangTrueDamage", 50D, 0D, 100000D);
    public static final ForgeConfigSpec.DoubleValue MAGIC_DAMAGE = BUILDER.comment(
            "Base magic damage per hit; adds 5 times the caster's luck, with total damage clamped to zero.").defineInRange("boomerangMagicDamage", 20D, 0D, 100000D);
    public static final ForgeConfigSpec.DoubleValue THROW_RANGE = BUILDER.defineInRange("boomerangRange", 24D, 2D, 96D);
    public static final ForgeConfigSpec.BooleanValue BREAK_BLOCKS = BUILDER.comment(
            "Break blocks in the flight path, including bedrock; drop their block item when available.").define("boomerangBreakBlocks", true);
    public static final ForgeConfigSpec.IntValue BURST_COOLDOWN = BUILDER.defineInRange("burstCooldownSeconds", 10, 0, 86400);
    public static final ForgeConfigSpec.IntValue FIREBALL_POWER = BUILDER.defineInRange("fireballExplosionPower", 2, 1, 16);
    public static final ForgeConfigSpec.IntValue WARD_COOLDOWN = BUILDER.comment(
            "Cooldown of Rift Execution (Shift+right-click). Retains the old key for world compatibility.").defineInRange("wardCooldownSeconds", 180, 0, 86400);
    public static final ForgeConfigSpec.DoubleValue EXECUTION_RANGE = BUILDER.defineInRange("executionTargetRange", 32D, 2D, 64D);
    public static final ForgeConfigSpec.DoubleValue EXECUTION_BLAST_RADIUS = BUILDER.defineInRange("executionBlastRadius", 2.5D, 0.5D, 8D);
    public static final ForgeConfigSpec.DoubleValue EXECUTION_BLAST_DAMAGE = BUILDER.defineInRange("executionBlastDamage", 8D, 0D, 10000D);
    public static final ForgeConfigSpec.IntValue WARD_DURATION = BUILDER.defineInRange("wardDurationSeconds", 60, 1, 86400);
    public static final ForgeConfigSpec.DoubleValue WARD_HEALTH = BUILDER.defineInRange("wardAbsorptionHealth", 50D, 1D, 10000D);
    public static final ForgeConfigSpec.DoubleValue WAVE_RANGE = BUILDER.defineInRange("waveRange", 24D, 2D, 96D);
    public static final ForgeConfigSpec SPEC = BUILDER.build();
    private CrimsonRequiemConfig() {}
}
