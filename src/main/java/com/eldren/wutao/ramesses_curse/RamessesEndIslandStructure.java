package com.eldren.wutao.ramesses_curse;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

public final class RamessesEndIslandStructure extends Structure {
    public static final Codec<RamessesEndIslandStructure> CODEC = simpleCodec(RamessesEndIslandStructure::new);
    public static final int CENTER_X = 447_552;
    public static final int CENTER_Z = 16_384;
    static final int TARGET_CHUNK_X = Math.floorDiv(CENTER_X, 16);
    static final int TARGET_CHUNK_Z = Math.floorDiv(CENTER_Z, 16);

    public RamessesEndIslandStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if (context.chunkPos().x != TARGET_CHUNK_X || context.chunkPos().z != TARGET_CHUNK_Z) {
            return Optional.empty();
        }
        int centerX = context.chunkPos().getMinBlockX();
        int centerZ = context.chunkPos().getMinBlockZ();
        BlockPos center = new BlockPos(centerX, RamessesEndIslandPiece.CENTER_Y, centerZ);
        return Optional.of(new GenerationStub(center, builder ->
                builder.addPiece(new RamessesEndIslandPiece(centerX, centerZ))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.RAMESSES_END_ISLAND_TYPE.get();
    }
}
