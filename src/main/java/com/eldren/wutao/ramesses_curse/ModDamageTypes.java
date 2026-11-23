package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public final class ModDamageTypes {
    public static final ResourceKey<DamageType> SEAL_BACKLASH = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(RamessesCurseMod.MODID, "seal_backlash"));
    public static final ResourceKey<DamageType> SEAL_ASSIST = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(RamessesCurseMod.MODID, "seal_assist"));
    public static final ResourceKey<DamageType> PERCENTAGE = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(RamessesCurseMod.MODID, "percentage_damage"));
    public static final ResourceKey<DamageType> DEATH_AUTHORITY = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(RamessesCurseMod.MODID, "death_authority"));
    public static final ResourceKey<DamageType> VULNERABILITY = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(RamessesCurseMod.MODID, "vulnerability"));
    public static final ResourceKey<DamageType> CHANCE_DIE_TRUE_DAMAGE = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(RamessesCurseMod.MODID, "chance_die_true_damage"));
    public static final ResourceKey<DamageType> APEP_DIVE = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(RamessesCurseMod.MODID, "apep_dive"));
    public static final ResourceKey<DamageType> APEP_ECLIPSE_BREATH = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(RamessesCurseMod.MODID, "apep_eclipse_breath"));
    public static final ResourceKey<DamageType> APEP_SHADOW = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(RamessesCurseMod.MODID, "apep_shadow"));
    public static final ResourceKey<DamageType> RUIN_BURST = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(RamessesCurseMod.MODID, "ruin_burst"));
    public static final ResourceKey<DamageType> EDICT_EXECUTION = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(RamessesCurseMod.MODID, "edict_execution"));
    public static final ResourceKey<DamageType> ETERNAL_CONQUEST_REFLECTION = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(RamessesCurseMod.MODID, "eternal_conquest_reflection"));
    public static final TagKey<DamageType> PERCENTAGE_DAMAGE = TagKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(RamessesCurseMod.MODID, "percentage_damage"));

    private ModDamageTypes() {
    }

    public static DamageSource sealBacklash(Level level) {
        return new DamageSource(level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(SEAL_BACKLASH));
    }

    public static DamageSource sealAssist(Level level, Player player) {
        return new DamageSource(level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(SEAL_ASSIST), player);
    }

    public static DamageSource percentageDamage(Level level, Player player) {
        return new DamageSource(level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(PERCENTAGE), player);
    }

    public static DamageSource deathAuthority(Level level, LivingEntity attacker) {
        return new DamageSource(level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(DEATH_AUTHORITY), attacker);
    }

    public static DamageSource vulnerability(Level level, LivingEntity attacker) {
        return new DamageSource(level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(VULNERABILITY), attacker);
    }

    public static DamageSource chanceDieTrueDamage(Level level, Player player) {
        return new DamageSource(level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(CHANCE_DIE_TRUE_DAMAGE), player);
    }

    public static DamageSource apepDive(Level level, LivingEntity attacker) {
        return new DamageSource(level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(APEP_DIVE), attacker);
    }

    public static DamageSource apepEclipseBreath(Level level, LivingEntity attacker) {
        return new DamageSource(level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(APEP_ECLIPSE_BREATH), attacker);
    }

    public static DamageSource apepShadow(Level level, LivingEntity attacker) {
        return new DamageSource(level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(APEP_SHADOW), attacker);
    }

    public static DamageSource ruinBurst(Level level, Player player) {
        return new DamageSource(level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(RUIN_BURST), player);
    }

    public static DamageSource edictExecution(Level level, Player player) {
        return new DamageSource(level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(EDICT_EXECUTION), player);
    }

    public static DamageSource eternalConquestReflection(Level level, Player player) {
        return new DamageSource(level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(ETERNAL_CONQUEST_REFLECTION), player);
    }
}
