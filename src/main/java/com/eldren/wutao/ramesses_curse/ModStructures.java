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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModStructures {
    private static final BlockPos NEBUKA_SKULL_OFFSET = new BlockPos(27, 35, 24);
    private static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, RamessesCurseMod.MODID);
    private static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, RamessesCurseMod.MODID);

    public static final ResourceKey<Structure> DUAT_PALACE = ResourceKey.create(
            Registries.STRUCTURE,
            ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "duat_palace"));

    public static final DeferredHolder<StructureType<?>, StructureType<DuatPalaceStructure>> DUAT_PALACE_TYPE =
            STRUCTURE_TYPES.register("duat_palace", () -> () -> DuatPalaceStructure.CODEC);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> DUAT_PALACE_PIECE =
            STRUCTURE_PIECES.register(
                    "duat_palace_piece",
                    () -> (StructurePieceType.StructureTemplateType) DuatPalacePiece::new);

    private ModStructures() {
    }

    public static void register(IEventBus eventBus) {
        STRUCTURE_TYPES.register(eventBus);
        STRUCTURE_PIECES.register(eventBus);
    }

    public static boolean isInsideDuatPalace(Level level, BlockPos pos) {
        return getDuatPalaceAt(level, pos).isValid();
    }

    public static StructureStart getDuatPalaceAt(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)
                || !ModDimensions.DUAT_DESERT.equals(serverLevel.dimension())) {
            return StructureStart.INVALID_START;
        }
        Structure structure = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE).get(DUAT_PALACE);
        return structure == null ? StructureStart.INVALID_START
                : serverLevel.structureManager().getStructureWithPieceAt(pos, structure);
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
                Structure structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(DUAT_PALACE);
                if (structure == null) return StructureStart.INVALID_START;
                StructureStart start = level.structureManager().getStructureWithPieceAt(
                        new BlockPos(pos.getX() + offsetX, sampleY, pos.getZ() + offsetZ), structure);
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

