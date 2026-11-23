package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.HashMap;
import java.util.Map;

/** Template-backed piece for the projection-exported cathedral. */
public final class GrandCathedralPiece extends TemplateStructurePiece {
    private static final ResourceLocation NETHER_COMMON_LOOT_TABLE = new ResourceLocation(
            RamessesCurseMod.MODID, "chests/grand_cathedral_nether_common");
    private static final ResourceLocation NETHER_RICH_LOOT_TABLE = new ResourceLocation(
            RamessesCurseMod.MODID, "chests/grand_cathedral_nether");
    private static final ResourceLocation NETHER_TREASURE_LOOT_TABLE = new ResourceLocation(
            RamessesCurseMod.MODID, "chests/grand_cathedral_nether_treasure");
    private static final ResourceLocation VOID_LOOT_TABLE = new ResourceLocation(
            RamessesCurseMod.MODID, "chests/grand_cathedral_void");

    public GrandCathedralPiece(StructureTemplateManager manager, ResourceLocation template, BlockPos position) {
        super(
                ModStructures.GRAND_CATHEDRAL_PIECE.get(),
                0,
                manager,
                template,
                template.toString(),
                createSettings(),
                position);
    }

    public GrandCathedralPiece(StructureTemplateManager manager, CompoundTag tag) {
        super(ModStructures.GRAND_CATHEDRAL_PIECE.get(), tag, manager, ignored -> createSettings());
    }

    private static StructurePlaceSettings createSettings() {
        return new StructurePlaceSettings()
                .setMirror(Mirror.NONE)
                .setRotation(Rotation.NONE)
                .setIgnoreEntities(false);
    }

    @Override
    protected void handleDataMarker(
            String marker,
            BlockPos pos,
            ServerLevelAccessor level,
            RandomSource random,
            BoundingBox generationBox) {
    }

    @Override
    public void postProcess(
            WorldGenLevel level,
            StructureManager structureManager,
            ChunkGenerator chunkGenerator,
            RandomSource random,
            BoundingBox generationBox,
            ChunkPos chunkPos,
            BlockPos pivot) {
        boolean nether = Level.NETHER.equals(level.getLevel().dimension());
        Map<BlockPos, BlockState> netherSubfloor = nether
                ? captureNetherSubfloor(level, generationBox)
                : Map.of();
        if (nether) {
            clearNetherTerrain(level, generationBox);
        }
        super.postProcess(level, structureManager, chunkGenerator, random, generationBox, chunkPos, pivot);
        if (nether) {
            restoreNetherSubfloor(level, netherSubfloor);
        }
        assignLootToEmptyChests(level, random, generationBox, Blocks.CHEST, nether);
        assignLootToEmptyChests(level, random, generationBox, Blocks.TRAPPED_CHEST, nether);
    }

    private void clearNetherTerrain(WorldGenLevel level, BoundingBox generationBox) {
        BoundingBox pieceBox = this.getBoundingBox();
        int minX = Math.max(pieceBox.minX(), generationBox.minX());
        int maxX = Math.min(pieceBox.maxX(), generationBox.maxX());
        int minY = Math.max(pieceBox.minY() + 2, generationBox.minY());
        int maxY = Math.min(
                Math.min(pieceBox.maxY(), generationBox.maxY()),
                level.getMaxBuildHeight() - 1);
        int minZ = Math.max(pieceBox.minZ(), generationBox.minZ());
        int maxZ = Math.min(pieceBox.maxZ(), generationBox.maxZ());
        if (minX > maxX || minY > maxY || minZ > maxZ) {
            return;
        }

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int terrainTop = Math.min(
                        maxY,
                        level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z));
                for (int y = minY; y <= terrainTop; y++) {
                    cursor.set(x, y, z);
                    if (!level.getBlockState(cursor).isAir()) {
                        level.setBlock(cursor, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
    }

    private Map<BlockPos, BlockState> captureNetherSubfloor(
            WorldGenLevel level, BoundingBox generationBox) {
        BoundingBox pieceBox = this.getBoundingBox();
        int minX = Math.max(pieceBox.minX(), generationBox.minX());
        int maxX = Math.min(pieceBox.maxX(), generationBox.maxX());
        int minZ = Math.max(pieceBox.minZ(), generationBox.minZ());
        int maxZ = Math.min(pieceBox.maxZ(), generationBox.maxZ());
        int minY = Math.max(pieceBox.minY(), generationBox.minY());
        int maxY = Math.min(pieceBox.minY() + 1, generationBox.maxY());
        Map<BlockPos, BlockState> captured = new HashMap<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    cursor.set(x, y, z);
                    BlockState state = level.getBlockState(cursor);
                    if (!state.isAir()) {
                        captured.put(cursor.immutable(), state);
                    }
                }
            }
        }
        return captured;
    }

    private static void restoreNetherSubfloor(
            WorldGenLevel level, Map<BlockPos, BlockState> captured) {
        captured.forEach((pos, state) -> {
            if (level.getBlockState(pos).isAir()) {
                level.setBlock(pos, state, Block.UPDATE_CLIENTS);
            }
        });
    }

    private void assignLootToEmptyChests(
            WorldGenLevel level,
            RandomSource random,
            BoundingBox generationBox,
            Block chestBlock,
            boolean nether) {
        for (StructureTemplate.StructureBlockInfo info
                : this.template.filterBlocks(this.templatePosition, this.placeSettings, chestBlock)) {
            if (!generationBox.isInside(info.pos()) || templateHasContents(info.nbt())) {
                continue;
            }
            BlockEntity blockEntity = level.getBlockEntity(info.pos());
            if (blockEntity instanceof ChestBlockEntity chest && chest.isEmpty()) {
                ResourceLocation lootTable = nether ? selectNetherLootTable(random) : VOID_LOOT_TABLE;
                chest.setLootTable(lootTable, random.nextLong());
                chest.setChanged();
            }
        }
    }

    private static ResourceLocation selectNetherLootTable(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.65F) {
            return NETHER_COMMON_LOOT_TABLE;
        }
        if (roll < 0.92F) {
            return NETHER_RICH_LOOT_TABLE;
        }
        return NETHER_TREASURE_LOOT_TABLE;
    }

    private static boolean templateHasContents(CompoundTag nbt) {
        if (nbt == null || nbt.isEmpty()) {
            return false;
        }
        if (nbt.contains("LootTable", Tag.TAG_STRING)) {
            return true;
        }
        return nbt.contains("Items", Tag.TAG_LIST)
                && !nbt.getList("Items", Tag.TAG_COMPOUND).isEmpty();
    }
}
