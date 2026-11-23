package com.eldren.wutao.ramesses_curse;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

import java.util.Optional;

/** Registers the generation anchors needed for the one large cathedral. */
public final class FixedGrandCathedralPlacement extends StructurePlacement {
    public static final Codec<FixedGrandCathedralPlacement> CODEC = RecordCodecBuilder.<FixedGrandCathedralPlacement>mapCodec(instance ->
            placementCodec(instance).apply(instance, FixedGrandCathedralPlacement::new)).codec();

    private FixedGrandCathedralPlacement(
            Vec3i locateOffset,
            FrequencyReductionMethod frequencyReductionMethod,
            float frequency,
            int salt,
            Optional<ExclusionZone> exclusionZone) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone);
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
        return GrandCathedralWorldgenStructure.sectionForAnchorChunk(chunkX, chunkZ) >= 0;
    }

    @Override
    public StructurePlacementType<?> type() {
        return ModStructures.FIXED_GRAND_CATHEDRAL_PLACEMENT.get();
    }
}
