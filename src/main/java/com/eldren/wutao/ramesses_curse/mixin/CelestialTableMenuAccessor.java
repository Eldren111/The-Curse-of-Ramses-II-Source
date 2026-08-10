package com.eldren.wutao.ramesses_curse.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(targets = "com.xiaoyue.celestial_enchantments.content.table.CelestialTableMenu")
public interface CelestialTableMenuAccessor {
    @Accessor(value = "costs", remap = false)
    int[] ramessesCurse$getCelestialCosts();
}
