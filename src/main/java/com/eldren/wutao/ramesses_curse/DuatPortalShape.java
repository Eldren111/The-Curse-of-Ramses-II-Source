package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

final class DuatPortalShape {
    private static final int MIN_WIDTH = 2;
    private static final int MAX_WIDTH = 21;
    private static final int MIN_HEIGHT = 3;
    private static final int MAX_HEIGHT = 21;

    private final LevelAccessor level;
    private final Direction.Axis axis;
    private final Direction widthDirection;
    private final BlockPos bottomLeft;
    private final int width;
    private final int height;

    private DuatPortalShape(LevelAccessor level, Direction.Axis axis, Direction widthDirection,
                            BlockPos bottomLeft, int width, int height) {
        this.level = level;
        this.axis = axis;
        this.widthDirection = widthDirection;
        this.bottomLeft = bottomLeft;
        this.width = width;
        this.height = height;
    }

    static Optional<DuatPortalShape> find(LevelAccessor level, BlockPos pos) {
        Optional<DuatPortalShape> xShape = find(level, pos, Direction.Axis.X);
        return xShape.isPresent() ? xShape : find(level, pos, Direction.Axis.Z);
    }

    static Optional<DuatPortalShape> findEmpty(LevelAccessor level, BlockPos pos) {
        return find(level, pos).filter(shape -> shape.countPortalBlocks() == 0);
    }

    private static Optional<DuatPortalShape> find(LevelAccessor level, BlockPos pos, Direction.Axis axis) {
        Direction positive = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        Direction negative = positive.getOpposite();
        BlockPos cursor = pos.immutable();

        int descent = 0;
        while (cursor.getY() > level.getMinBuildHeight()
                && descent++ < MAX_HEIGHT
                && isInterior(level.getBlockState(cursor.below()))) {
            cursor = cursor.below();
        }

        if (!isFrame(level.getBlockState(cursor.below()))) {
            return Optional.empty();
        }

        int shift = 0;
        while (shift++ < MAX_WIDTH
                && isInterior(level.getBlockState(cursor.relative(negative)))
                && isFrame(level.getBlockState(cursor.relative(negative).below()))) {
            cursor = cursor.relative(negative);
        }

        BlockPos bottomLeft = cursor;
        if (!isFrame(level.getBlockState(bottomLeft.relative(negative)))) {
            return Optional.empty();
        }

        int width = 0;
        while (width <= MAX_WIDTH
                && isInterior(level.getBlockState(bottomLeft.relative(positive, width)))
                && isFrame(level.getBlockState(bottomLeft.relative(positive, width).below()))) {
            width++;
        }
        if (width < MIN_WIDTH || width > MAX_WIDTH
                || !isFrame(level.getBlockState(bottomLeft.relative(positive, width)))) {
            return Optional.empty();
        }

        for (int y = 0; y <= MAX_HEIGHT; y++) {
            boolean topFrame = true;
            for (int x = 0; x < width; x++) {
                if (!isFrame(level.getBlockState(bottomLeft.relative(positive, x).above(y)))) {
                    topFrame = false;
                    break;
                }
            }
            if (topFrame) {
                return y >= MIN_HEIGHT
                        ? Optional.of(new DuatPortalShape(level, axis, positive, bottomLeft, width, y))
                        : Optional.empty();
            }

            if (!isFrame(level.getBlockState(bottomLeft.relative(negative).above(y)))
                    || !isFrame(level.getBlockState(bottomLeft.relative(positive, width).above(y)))) {
                return Optional.empty();
            }
            for (int x = 0; x < width; x++) {
                if (!isInterior(level.getBlockState(bottomLeft.relative(positive, x).above(y)))) {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    void createPortalBlocks() {
        BlockState portal = ModBlocks.DUAT_PORTAL.get().defaultBlockState()
                .setValue(NetherPortalBlock.AXIS, axis);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                level.setBlock(bottomLeft.relative(widthDirection, x).above(y), portal, 18);
            }
        }
    }

    boolean isComplete() {
        return countPortalBlocks() == width * height;
    }

    BlockPos arrivalBlock() {
        return bottomLeft.relative(widthDirection, width / 2);
    }

    private int countPortalBlocks() {
        int count = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (level.getBlockState(bottomLeft.relative(widthDirection, x).above(y)).is(ModBlocks.DUAT_PORTAL.get())) {
                    count++;
                }
            }
        }
        return count;
    }

    private static boolean isFrame(BlockState state) {
        return state.is(Blocks.SANDSTONE);
    }

    private static boolean isInterior(BlockState state) {
        return state.isAir() || state.is(BlockTags.FIRE) || state.is(ModBlocks.DUAT_PORTAL.get());
    }
}
