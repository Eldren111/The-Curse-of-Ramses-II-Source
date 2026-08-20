package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public final class DuatPalacePiece extends TemplateStructurePiece {
    private static final ResourceKey<net.minecraft.world.level.storage.loot.LootTable> PALACE_LOOT_TABLE =
            ResourceKey.create(Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "chests/duat_palace"));

    public DuatPalacePiece(StructureTemplateManager manager, ResourceLocation template, BlockPos position) {
        super(
                ModStructures.DUAT_PALACE_PIECE.get(),
                0,
                manager,
                template,
                template.toString(),
                createSettings(),
                position);
    }

    public DuatPalacePiece(StructureTemplateManager manager, CompoundTag tag) {
        super(ModStructures.DUAT_PALACE_PIECE.get(), tag, manager, ignored -> createSettings());
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
        super.postProcess(level, structureManager, chunkGenerator, random, generationBox, chunkPos, pivot);
        assignLoot(level, random, generationBox, Blocks.CHEST);
        assignLoot(level, random, generationBox, Blocks.TRAPPED_CHEST);
    }

    private void assignLoot(WorldGenLevel level, RandomSource random, BoundingBox generationBox, Block chestBlock) {
        for (StructureTemplate.StructureBlockInfo info
                : this.template.filterBlocks(this.templatePosition, this.placeSettings, chestBlock)) {
            if (!generationBox.isInside(info.pos())) {
                continue;
            }

            BlockEntity blockEntity = level.getBlockEntity(info.pos());
            if (!(blockEntity instanceof ChestBlockEntity chest)) {
                continue;
            }

            chest.clearContent();
            if (chest.getBlockState().getBlock() instanceof ChestBlock
                    && chest.getBlockState().getValue(ChestBlock.TYPE) == ChestType.RIGHT) {
                chest.setChanged();
                continue;
            }
            chest.setLootTable(PALACE_LOOT_TABLE, random.nextLong());
            chest.setChanged();
        }
    }
}
