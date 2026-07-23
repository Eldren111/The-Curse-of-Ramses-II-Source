package com.eldren.wutao.ramesses_curse;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DatePalmSaplingBlock extends SaplingBlock {
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);
    private static final int[][] FROND_DIRECTIONS = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };

    public DatePalmSaplingBlock() {
        super(new AbstractTreeGrower() {
            @Nullable
            @Override
            protected net.minecraft.resources.ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(
                    RandomSource random, boolean hasFlowers) {
                return null;
            }
        }, BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING));
    }

    @Override
    public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level,
                               BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void advanceTree(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        if (state.getValue(STAGE) == 0) {
            level.setBlock(pos, state.cycle(STAGE), 4);
            return;
        }
        generateDatePalm(level, pos, random);
    }

    public static boolean generateDatePalm(LevelAccessor level, BlockPos saplingPos, RandomSource random) {
        int trunkHeight = 6 + random.nextInt(5);
        BlockState log = ModBlocks.DATE_PALM_LOG.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState leaves = ModBlocks.DATE_PALM_LEAVES.get().defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, true);

        for (int y = 1; y <= trunkHeight; y++) {
            BlockPos target = saplingPos.above(y);
            if (!canReplace(level, target)) {
                return false;
            }
        }
        for (int y = 0; y <= trunkHeight; y++) {
            level.setBlock(saplingPos.above(y), log, 3);
        }

        BlockPos crown = saplingPos.above(trunkHeight);
        placeNaturalCrown(level, crown, leaves, random);
        return true;
    }

    private static void placeNaturalCrown(LevelAccessor level, BlockPos crown, BlockState leaves, RandomSource random) {
        placeLeaves(level, crown.above(3), leaves);
        placeLeaves(level, crown.above(2), leaves);

        for (int y = -1; y <= 2; y++) {
            int radius = y == 0 ? 2 : 1;
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    int distance = x * x + z * z;
                    if (distance <= radius * radius && (distance <= 1 || random.nextFloat() < 0.72F)) {
                        placeLeaves(level, crown.offset(x, y, z), leaves);
                    }
                }
            }
        }

        for (int[] direction : FROND_DIRECTIONS) {
            boolean diagonal = direction[0] != 0 && direction[1] != 0;
            int length = diagonal ? 2 + random.nextInt(2) : 3 + random.nextInt(2);
            int startHeight = random.nextBoolean() ? 1 : 0;
            for (int step = 1; step <= length; step++) {
                int drop = step == length ? 2 : step == length - 1 ? 1 : 0;
                BlockPos frond = crown.offset(
                        direction[0] * step,
                        startHeight - drop,
                        direction[1] * step);
                placeLeaves(level, frond, leaves);

                if (step < length && random.nextFloat() < 0.65F) {
                    int sideX = -direction[1];
                    int sideZ = direction[0];
                    int side = random.nextBoolean() ? 1 : -1;
                    placeLeaves(level, frond.offset(sideX * side, 0, sideZ * side), leaves);
                }
                if (step <= 2 && random.nextFloat() < 0.55F) {
                    placeLeaves(level, frond.below(), leaves);
                }
            }
        }
    }

    private static void placeLeaves(LevelAccessor level, BlockPos pos, BlockState leaves) {
        if (canReplace(level, pos)) {
            level.setBlock(pos, leaves, 3);
        }
    }

    private static boolean canReplace(LevelReader level, BlockPos pos) {
        return level.getBlockState(pos).isAir() || level.getBlockState(pos).canBeReplaced();
    }
}
