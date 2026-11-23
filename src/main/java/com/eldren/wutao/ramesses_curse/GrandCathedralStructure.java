package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/**
 * Placement hook for the large cathedral exported from a projection mod.
 * Horizontal slices belong under data/ramesses_curse/structures/grand_cathedral/.
 */
public final class GrandCathedralStructure {
    public static final int SLICE_SIZE = 16;
    public static final int SLICES_X = 13;
    public static final int SLICES_Z = 20;
    public static final int SIZE_X = 196;
    public static final int SIZE_Y = 293;
    public static final int SIZE_Z = 317;
    public static final int NETHER_FOUNDATION_Y = 126;
    public static final BlockPos DEFAULT_NETHER_ORIGIN = new BlockPos(
            GrandCathedralWorldgenStructure.CENTER_X, NETHER_FOUNDATION_Y, GrandCathedralWorldgenStructure.CENTER_Z);
    public static final BlockPos DEFAULT_VOID_ORIGIN = new BlockPos(
            GrandCathedralWorldgenStructure.CENTER_X, 0, GrandCathedralWorldgenStructure.CENTER_Z);

    private GrandCathedralStructure() {
    }

    public static boolean isAllowedDimension(ServerLevel level) {
        return Level.NETHER.equals(level.dimension()) || ModDimensions.RAMSES_VOID.equals(level.dimension());
    }

    /** Keeps the configured Nether foundation while respecting custom height limits. */
    public static BlockPos resolveOrigin(ServerLevel level, BlockPos requestedOrigin) {
        if (Level.NETHER.equals(level.dimension())) {
            return new BlockPos(
                    requestedOrigin.getX(),
                    Math.min(NETHER_FOUNDATION_Y, level.getMaxBuildHeight() - 1),
                    requestedOrigin.getZ());
        }
        return requestedOrigin;
    }

    /** Places the imported template at the requested origin when the dimension is allowed. */
    public static boolean place(ServerLevel level, BlockPos requestedOrigin) {
        if (!isAllowedDimension(level)) {
            return false;
        }

        BlockPos origin = resolveOrigin(level, requestedOrigin);
        StructurePlaceSettings settings = new StructurePlaceSettings().setIgnoreEntities(false);
        boolean placed = false;
        for (int sliceZ = 0; sliceZ < SLICES_Z; sliceZ++) {
            for (int sliceX = 0; sliceX < SLICES_X; sliceX++) {
                BlockPos pieceOrigin = origin.offset(sliceX * SLICE_SIZE, 0, sliceZ * SLICE_SIZE);
                StructureTemplate template = level.getStructureManager().getOrCreate(templateId(sliceX, sliceZ));
                placed |= template.placeInWorld(
                        level, pieceOrigin, pieceOrigin, settings, level.random, Block.UPDATE_CLIENTS);
            }
        }
        return placed;
    }

    public static ResourceLocation templateId(int sliceX, int sliceZ) {
        return new ResourceLocation(
                RamessesCurseMod.MODID,
                "grand_cathedral/piece_" + sliceX + "_" + sliceZ);
    }
}
