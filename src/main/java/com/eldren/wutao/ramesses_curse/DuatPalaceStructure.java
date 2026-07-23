package com.eldren.wutao.ramesses_curse;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.List;
import java.util.Optional;

public final class DuatPalaceStructure extends Structure {
    public static final Codec<DuatPalaceStructure> CODEC = simpleCodec(DuatPalaceStructure::new);
    public static final int PALACE_Y = 128;
    public static final int PALACE_SIZE_X = 65;
    public static final int PALACE_SIZE_Y = 69;
    public static final int PALACE_SIZE_Z = 56;

    private static final List<PieceDefinition> PIECES = List.of(
            piece("no.1", 0, 0, 0),
            piece("no.2", 48, 0, 0),
            piece("no.3", 0, 0, 48),
            piece("no.4", 48, 0, 48),
            piece("no.5", 0, 48, 0),
            piece("no.6", 48, 48, 0),
            piece("no.7", 0, 48, 48),
            piece("no.8", 48, 48, 48));

    public DuatPalaceStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int centerX = context.chunkPos().getMiddleBlockX();
        int centerZ = context.chunkPos().getMiddleBlockZ();
        BlockPos origin = new BlockPos(
                centerX - PALACE_SIZE_X / 2,
                PALACE_Y,
                centerZ - PALACE_SIZE_Z / 2);

        return Optional.of(new GenerationStub(origin, builder -> addPieces(context, builder, origin)));
    }

    private static void addPieces(GenerationContext context, StructurePiecesBuilder builder, BlockPos origin) {
        for (PieceDefinition piece : PIECES) {
            builder.addPiece(new DuatPalacePiece(
                    context.structureTemplateManager(),
                    piece.template(),
                    origin.offset(piece.offset())));
        }
    }

    private static PieceDefinition piece(String name, int x, int y, int z) {
        return new PieceDefinition(
                new ResourceLocation(RamessesCurseMod.MODID, "duat_palace/" + name),
                new BlockPos(x, y, z));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.DUAT_PALACE_TYPE.get();
    }

    private record PieceDefinition(ResourceLocation template, BlockPos offset) {
    }
}
