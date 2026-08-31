package com.eldren.wutao.ramesses_curse;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

import java.util.Optional;

public final class FixedEndIslandPlacement extends StructurePlacement {
    public static final MapCodec<FixedEndIslandPlacement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            placementCodec(instance).apply(instance, FixedEndIslandPlacement::new));

    private FixedEndIslandPlacement(
            Vec3i locateOffset,
            FrequencyReductionMethod frequencyReductionMethod,
            float frequency,
            int salt,
            Optional<ExclusionZone> exclusionZone) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone);
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
        return chunkX == RamessesEndIslandStructure.TARGET_CHUNK_X
                && chunkZ == RamessesEndIslandStructure.TARGET_CHUNK_Z;
    }

    @Override
    public StructurePlacementType<?> type() {
        return ModStructures.FIXED_END_ISLAND_PLACEMENT.get();
    }
}
