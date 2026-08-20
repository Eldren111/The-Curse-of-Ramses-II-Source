package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class ModDimensions {
    public static final ResourceKey<Level> DUAT_DESERT = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "duat_desert"));

    private ModDimensions() {
    }
}
