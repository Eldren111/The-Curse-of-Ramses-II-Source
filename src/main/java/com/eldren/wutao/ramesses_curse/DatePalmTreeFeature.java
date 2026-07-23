package com.eldren.wutao.ramesses_curse;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class DatePalmTreeFeature extends Feature<NoneFeatureConfiguration> {
    public DatePalmTreeFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        BlockState ground = context.level().getBlockState(context.origin().below());
        if (!ground.is(BlockTags.DIRT)
                && !ground.is(Blocks.SAND)
                && !ground.is(Blocks.RED_SAND)) {
            return false;
        }
        return DatePalmSaplingBlock.generateDatePalm(
                context.level(), context.origin(), context.random());
    }
}
