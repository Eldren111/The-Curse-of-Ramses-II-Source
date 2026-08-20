package com.eldren.wutao.ramesses_curse;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.RedStoneOreBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(BuiltInRegistries.BLOCK, RamessesCurseMod.MODID);

    public static final DeferredHolder<Block, Block> DUAT_PORTAL = BLOCKS.register(
            "duat_portal",
            () -> new DuatPortalBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_PORTAL).noLootTable()));

    public static final DeferredHolder<Block, Block> DUAT_DUNGEON_TILE = BLOCKS.register(
            "duat_dungeon_tile",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK)));

    public static final DeferredHolder<Block, Block> DUAT_DUNGEON_TILE_WALL = BLOCKS.register(
            "duat_dungeon_tile_wall",
            () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK)));

    public static final DeferredHolder<Block, Block> DUAT_DUNGEON_TILE_SLAB = BLOCKS.register(
            "duat_dungeon_tile_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK)));

    public static final DeferredHolder<Block, Block> DUAT_DUNGEON_TILE_STAIRS = BLOCKS.register(
            "duat_dungeon_tile_stairs",
            () -> new StairBlock(
                    DUAT_DUNGEON_TILE.get().defaultBlockState(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK)));

    public static final DeferredHolder<Block, Block> DUAT_DUNGEON_PILLAR = BLOCKS.register(
            "duat_dungeon_pillar",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK)));

    public static final DeferredHolder<Block, Block> SOLAR_RITE_BRICK = BLOCKS.register(
            "solar_rite_brick",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK)));

    public static final DeferredHolder<Block, Block> SILENT_SEPULCHER_WITHERED_VINE = BLOCKS.register(
            "silent_sepulcher_withered_vine",
            () -> new VineBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.VINE).randomTicks()));

    public static final DeferredHolder<Block, Block> DATE_PALM_LOG = BLOCKS.register(
            "date_palm_log",
            () -> new RotatedPillarBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)
                            .requiresCorrectToolForDrops()));

    public static final DeferredHolder<Block, Block> DATE_PALM_LEAVES = BLOCKS.register(
            "date_palm_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)));

    public static final DeferredHolder<Block, Block> DATE_PALM_SAPLING = BLOCKS.register(
            "date_palm_sapling",
            DatePalmSaplingBlock::new);

    public static final DeferredHolder<Block, Block> FRACTURED_PHASE_ORE = BLOCKS.register(
            "fractured_phase_ore",
            () -> new RedStoneOreBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_ORE)
                    .requiresCorrectToolForDrops()));

    public static final DeferredHolder<Block, Block> NETHER_FRACTURED_PHASE_ORE = BLOCKS.register(
            "nether_fractured_phase_ore",
            () -> new RedStoneOreBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_ORE)
                    .requiresCorrectToolForDrops()));

    public static final DeferredHolder<Block, Block> END_FRACTURED_PHASE_ORE = BLOCKS.register(
            "end_fractured_phase_ore",
            () -> new RedStoneOreBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_ORE)
                    .requiresCorrectToolForDrops()));

    private ModBlocks() {
    }
}

