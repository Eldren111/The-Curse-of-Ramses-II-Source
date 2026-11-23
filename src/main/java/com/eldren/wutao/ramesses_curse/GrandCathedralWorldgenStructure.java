package com.eldren.wutao.ramesses_curse;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/** Worldgen registration used by /locate; the template remains the source of the building blocks. */
public final class GrandCathedralWorldgenStructure extends Structure {
    public static final Codec<GrandCathedralWorldgenStructure> CODEC = simpleCodec(GrandCathedralWorldgenStructure::new);
    public static final int CENTER_X = 1_024;
    public static final int CENTER_Z = 1_024;
    private static final int ORIGIN_CHUNK_X = Math.floorDiv(CENTER_X, 16);
    private static final int ORIGIN_CHUNK_Z = Math.floorDiv(CENTER_Z, 16);
    // Structure references only search eight chunks from a start, so the 20-row template needs two starts.
    private static final int SECTION_ROWS = 10;

    public GrandCathedralWorldgenStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int section = sectionForAnchorChunk(context.chunkPos().x, context.chunkPos().z);
        if (section < 0) {
            return Optional.empty();
        }

        int foundationY = context.heightAccessor().getMinBuildHeight() == 0
                ? GrandCathedralStructure.NETHER_FOUNDATION_Y
                : 0;
        BlockPos origin = new BlockPos(CENTER_X, foundationY, CENTER_Z);
        int firstSliceZ = section * SECTION_ROWS;
        int lastSliceZ = Math.min(firstSliceZ + SECTION_ROWS, GrandCathedralStructure.SLICES_Z);
        return Optional.of(new GenerationStub(origin, builder -> {
            for (int sliceZ = firstSliceZ; sliceZ < lastSliceZ; sliceZ++) {
                for (int sliceX = 0; sliceX < GrandCathedralStructure.SLICES_X; sliceX++) {
                    BlockPos pieceOrigin = origin.offset(
                            sliceX * GrandCathedralStructure.SLICE_SIZE,
                            0,
                            sliceZ * GrandCathedralStructure.SLICE_SIZE);
                    builder.addPiece(new GrandCathedralPiece(
                            context.structureTemplateManager(),
                            GrandCathedralStructure.templateId(sliceX, sliceZ),
                            pieceOrigin));
                }
            }
        }));
    }

    static int sectionForAnchorChunk(int chunkX, int chunkZ) {
        if (chunkX != ORIGIN_CHUNK_X + GrandCathedralStructure.SLICES_X / 2) {
            return -1;
        }
        for (int section = 0; section * SECTION_ROWS < GrandCathedralStructure.SLICES_Z; section++) {
            int firstRow = section * SECTION_ROWS;
            int rows = Math.min(SECTION_ROWS, GrandCathedralStructure.SLICES_Z - firstRow);
            int anchorZ = ORIGIN_CHUNK_Z + firstRow + (rows - 1) / 2;
            if (chunkZ == anchorZ) {
                return section;
            }
        }
        return -1;
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.GRAND_CATHEDRAL_TYPE.get();
    }
}
