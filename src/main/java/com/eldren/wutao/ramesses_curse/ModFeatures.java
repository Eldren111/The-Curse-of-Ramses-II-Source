package com.eldren.wutao.ramesses_curse;

import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(BuiltInRegistries.FEATURE, RamessesCurseMod.MODID);

    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> DATE_PALM_TREE =
            FEATURES.register("date_palm_tree", DatePalmTreeFeature::new);

    private ModFeatures() {
    }
}

