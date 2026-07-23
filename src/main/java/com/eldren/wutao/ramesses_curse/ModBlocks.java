package com.eldren.wutao.ramesses_curse;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, RamessesCurseMod.MODID);

    public static final RegistryObject<Block> DUAT_PORTAL = BLOCKS.register(
            "duat_portal",
            () -> new DuatPortalBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_PORTAL).noLootTable()));

    public static final RegistryObject<Block> DUAT_DUNGEON_TILE = BLOCKS.register(
            "duat_dungeon_tile",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.BEDROCK)));

    public static final RegistryObject<Block> DUAT_DUNGEON_TILE_WALL = BLOCKS.register(
            "duat_dungeon_tile_wall",
            () -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.BEDROCK)));

    public static final RegistryObject<Block> DUAT_DUNGEON_TILE_SLAB = BLOCKS.register(
            "duat_dungeon_tile_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.BEDROCK)));

    public static final RegistryObject<Block> DUAT_DUNGEON_TILE_STAIRS = BLOCKS.register(
            "duat_dungeon_tile_stairs",
            () -> new StairBlock(
                    () -> DUAT_DUNGEON_TILE.get().defaultBlockState(),
                    BlockBehaviour.Properties.copy(Blocks.BEDROCK)));

    public static final RegistryObject<Block> DUAT_DUNGEON_PILLAR = BLOCKS.register(
            "duat_dungeon_pillar",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.BEDROCK)));

    public static final RegistryObject<Block> SOLAR_RITE_BRICK = BLOCKS.register(
            "solar_rite_brick",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.BEDROCK)));

    public static final RegistryObject<Block> SILENT_SEPULCHER_WITHERED_VINE = BLOCKS.register(
            "silent_sepulcher_withered_vine",
            () -> new VineBlock(BlockBehaviour.Properties.copy(Blocks.VINE).randomTicks()));

    public static final RegistryObject<Block> DATE_PALM_LOG = BLOCKS.register(
            "date_palm_log",
            () -> new RotatedPillarBlock(
                    BlockBehaviour.Properties.copy(Blocks.OAK_LOG)
                            .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> DATE_PALM_LEAVES = BLOCKS.register(
            "date_palm_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES)));

    public static final RegistryObject<Block> DATE_PALM_SAPLING = BLOCKS.register(
            "date_palm_sapling",
            DatePalmSaplingBlock::new);

    private ModBlocks() {
    }
}
