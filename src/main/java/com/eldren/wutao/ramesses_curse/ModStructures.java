package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.registries.Registries;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModStructures {
    private static final BlockPos NEBUKA_SKULL_OFFSET = new BlockPos(27, 35, 24);
    private static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, RamessesCurseMod.MODID);
    private static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, RamessesCurseMod.MODID);
    private static final DeferredRegister<StructurePlacementType<?>> STRUCTURE_PLACEMENTS =
            DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, RamessesCurseMod.MODID);

    public static final ResourceKey<Structure> DUAT_PALACE = ResourceKey.create(
            Registries.STRUCTURE,
            new ResourceLocation(RamessesCurseMod.MODID, "duat_palace"));
    public static final ResourceKey<Structure> RAMESSES_END_ISLAND = ResourceKey.create(
            Registries.STRUCTURE,
            new ResourceLocation(RamessesCurseMod.MODID, "ramesses_end_island"));

    public static final RegistryObject<StructureType<DuatPalaceStructure>> DUAT_PALACE_TYPE =
            STRUCTURE_TYPES.register("duat_palace", () -> () -> DuatPalaceStructure.CODEC);
    public static final RegistryObject<StructureType<RamessesEndIslandStructure>> RAMESSES_END_ISLAND_TYPE =
            STRUCTURE_TYPES.register("ramesses_end_island", () -> () -> RamessesEndIslandStructure.CODEC);
    public static final RegistryObject<StructurePlacementType<FixedEndIslandPlacement>> FIXED_END_ISLAND_PLACEMENT =
            STRUCTURE_PLACEMENTS.register("fixed_end_island", () -> () -> FixedEndIslandPlacement.CODEC);

    public static final RegistryObject<StructurePieceType> DUAT_PALACE_PIECE =
            STRUCTURE_PIECES.register(
                    "duat_palace_piece",
                    () -> (StructurePieceType.StructureTemplateType) DuatPalacePiece::new);
    public static final RegistryObject<StructurePieceType> RAMESSES_END_ISLAND_PIECE =
            STRUCTURE_PIECES.register(
                    "ramesses_end_island_piece",
                    () -> (StructurePieceType.ContextlessType) RamessesEndIslandPiece::new);

    private ModStructures() {
    }

    public static void register(IEventBus eventBus) {
        STRUCTURE_TYPES.register(eventBus);
        STRUCTURE_PIECES.register(eventBus);
        STRUCTURE_PLACEMENTS.register(eventBus);
    }

    public static boolean isInsideDuatPalace(Level level, BlockPos pos) {
        return getDuatPalaceAt(level, pos).isValid();
    }

    public static StructureStart getDuatPalaceAt(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)
                || !ModDimensions.DUAT_DESERT.equals(serverLevel.dimension())) {
            return StructureStart.INVALID_START;
        }
        return serverLevel.structureManager().getStructureWithPieceAt(pos, DUAT_PALACE);
    }

    public static StructureStart findDuatPalaceNear(ServerLevel level, BlockPos pos, int radius) {
        if (!ModDimensions.DUAT_DESERT.equals(level.dimension())) {
            return StructureStart.INVALID_START;
        }

        int sampleY = Math.max(
                DuatPalaceStructure.PALACE_Y,
                Math.min(pos.getY(), DuatPalaceStructure.PALACE_Y + DuatPalaceStructure.PALACE_SIZE_Y - 1));
        int[] offsets = {-radius, 0, radius};
        for (int offsetX : offsets) {
            for (int offsetZ : offsets) {
                StructureStart start = level.structureManager().getStructureWithPieceAt(
                        new BlockPos(pos.getX() + offsetX, sampleY, pos.getZ() + offsetZ),
                        DUAT_PALACE);
                if (start.isValid()) {
                    return start;
                }
            }
        }
        return StructureStart.INVALID_START;
    }

    public static boolean isNebukaSummoningSkull(ServerLevel level, BlockPos pos) {
        if (pos.getY() != DuatPalaceStructure.PALACE_Y + NEBUKA_SKULL_OFFSET.getY()) {
            return false;
        }
        StructureStart start = getDuatPalaceAt(level, pos);
        if (!start.isValid()) {
            return false;
        }
        BoundingBox box = start.getBoundingBox();
        BlockPos expected = new BlockPos(
                box.minX() + NEBUKA_SKULL_OFFSET.getX(),
                box.minY() + NEBUKA_SKULL_OFFSET.getY(),
                box.minZ() + NEBUKA_SKULL_OFFSET.getZ());
        return expected.equals(pos);
    }
}
