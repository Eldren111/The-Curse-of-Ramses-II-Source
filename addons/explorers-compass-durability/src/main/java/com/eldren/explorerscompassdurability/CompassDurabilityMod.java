package com.eldren.explorerscompassdurability;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(CompassDurabilityMod.MOD_ID)
public final class CompassDurabilityMod {
    public static final String MOD_ID = "explorerscompassdurability";

    public CompassDurabilityMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, DurabilityConfig.SPEC);
    }
}
