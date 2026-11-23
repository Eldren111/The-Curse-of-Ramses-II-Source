package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class ModDimensions {
    public static final ResourceKey<Level> DUAT_DESERT = ResourceKey.create(
            Registries.DIMENSION,
            new ResourceLocation(RamessesCurseMod.MODID, "duat_desert"));

    /** Empty dimension reserved for the future large cathedral structure. */
    public static final ResourceKey<Level> RAMSES_VOID = ResourceKey.create(
            Registries.DIMENSION,
            new ResourceLocation(RamessesCurseMod.MODID, "ramesses_void"));

    private ModDimensions() {
    }
}
