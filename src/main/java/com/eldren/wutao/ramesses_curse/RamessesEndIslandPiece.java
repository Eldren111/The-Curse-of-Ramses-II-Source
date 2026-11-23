package com.eldren.wutao.ramesses_curse;

import com.eldren.wutao.ramesses_curse.integration.goety.GoetyObsidianMonolithCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class RamessesEndIslandPiece extends StructurePiece {
    public static final int CENTER_Y = 68;
    private static final int ISLAND_RADIUS = 92;
    private static final int GENERATION_RADIUS = 104;
    private static final int TOWER_COUNT = 10;
    private static final int CAGE_BLOCK_COUNT = 98;
    private static final int CAGE_BLOCKS_PER_TICK = 8;
    private static final int TOWER_CAMERA_TURN_TICKS = 10;
    private static final int CAGE_CLEAR_BLOCKS_PER_TICK =
            (CAGE_BLOCK_COUNT + TOWER_CAMERA_TURN_TICKS - 1) / TOWER_CAMERA_TURN_TICKS;
    private static final int TOWER_CAGE_BUILD_TICKS =
            (CAGE_BLOCK_COUNT + CAGE_BLOCKS_PER_TICK - 1) / CAGE_BLOCKS_PER_TICK;
    private static final int TOWER_CRYSTAL_REVEAL_TICKS = 1;
    private static final int TOWER_SEQUENCE_TICKS =
            TOWER_CAMERA_TURN_TICKS + TOWER_CAGE_BUILD_TICKS + TOWER_CRYSTAL_REVEAL_TICKS;
    public static final int CRYSTAL_CAGE_BUILD_TICKS = TOWER_COUNT * TOWER_SEQUENCE_TICKS;
    private static final int DRAGON_MARKER_Y = 18;
    private static final int RESPAWN_MARKER_Y = 19;
    private static final int ISLAND_GENERATED_MARKER_Y = 15;
    private static final Map<ServerLevel, Integer> FALLBACK_GENERATION_PROGRESS = new WeakHashMap<>();
    private static final double DRAGON_SEARCH_RADIUS = 256.0D;
    private final int centerX;
    private final int centerZ;

    public RamessesEndIslandPiece(int centerX, int centerZ) {
        super(ModStructures.RAMESSES_END_ISLAND_PIECE.get(), 0, new BoundingBox(
                centerX - GENERATION_RADIUS, 16, centerZ - GENERATION_RADIUS,
                centerX + GENERATION_RADIUS, 128, centerZ + GENERATION_RADIUS));
        this.centerX = centerX;
        this.centerZ = centerZ;
    }

    public RamessesEndIslandPiece(CompoundTag tag) {
        super(ModStructures.RAMESSES_END_ISLAND_PIECE.get(), tag);
        this.centerX = tag.getInt("CenterX");
        this.centerZ = tag.getInt("CenterZ");
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("CenterX", this.centerX);
        tag.putInt("CenterZ", this.centerZ);
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
        long seed = level.getSeed();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minX = Math.max(generationBox.minX(), this.boundingBox.minX());
        int maxX = Math.min(generationBox.maxX(), this.boundingBox.maxX());
        int minZ = Math.max(generationBox.minZ(), this.boundingBox.minZ());
        int maxZ = Math.min(generationBox.maxZ(), this.boundingBox.maxZ());

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int surfaceY = islandSurfaceY(seed, x, z);
                if (surfaceY != Integer.MIN_VALUE) {
                    int bottomY = islandBottomY(seed, x, z, surfaceY);
                    for (int y = bottomY; y <= surfaceY; y++) {
                        level.setBlock(pos.set(x, y, z), islandBlock(seed, x, y, z), 2);
                    }
                }
                for (int towerIndex = 0; towerIndex < TOWER_COUNT; towerIndex++) {
                    placeTowerColumn(level, generationBox, pos, seed, x, z, towerIndex);
                }
            }
        }
        for (int towerIndex = 0; towerIndex < TOWER_COUNT; towerIndex++) {
            carveTowerChamber(level, generationBox, seed, towerIndex);
            placeCrystalCage(level, generationBox, seed, towerIndex);
        }
    }

    public static void initializeRuntimeEntities(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        if (!Level.END.equals(level.dimension())) {
            return;
        }
        double dx = player.getX() - RamessesEndIslandStructure.CENTER_X;
        double dz = player.getZ() - RamessesEndIslandStructure.CENTER_Z;
        double activationRadius = GENERATION_RADIUS + 64.0D;
        if (dx * dx + dz * dz > activationRadius * activationRadius) {
            return;
        }
        BlockPos center = new BlockPos(
                RamessesEndIslandStructure.CENTER_X,
                CENTER_Y,
                RamessesEndIslandStructure.CENTER_Z);
        if (!ensureRuntimeIslandGenerated(player, center)) {
            return;
        }
        if (hasRespawnInProgress(level, center)) {
            return;
        }
        RamessesEndIslandPiece island = new RamessesEndIslandPiece(
                RamessesEndIslandStructure.CENTER_X,
                RamessesEndIslandStructure.CENTER_Z);
        island.initializeLoadedTowerEntities(level);
        island.initializeIslandDragon(level);
    }

    public static boolean isRuntimeIslandGenerationActive(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        if (!Level.END.equals(level.dimension())) {
            return false;
        }
        double dx = player.getX() - RamessesEndIslandStructure.CENTER_X;
        double dz = player.getZ() - RamessesEndIslandStructure.CENTER_Z;
        double activationRadius = GENERATION_RADIUS + 64.0D;
        if (dx * dx + dz * dz > activationRadius * activationRadius) {
            return false;
        }
        BlockPos center = new BlockPos(
                RamessesEndIslandStructure.CENTER_X,
                CENTER_Y,
                RamessesEndIslandStructure.CENTER_Z);
        return !isIslandGenerated(level, center);
    }

    private static boolean ensureRuntimeIslandGenerated(ServerPlayer player, BlockPos center) {
        ServerLevel level = player.serverLevel();
        if (isIslandGenerated(level, center)) {
            FALLBACK_GENERATION_PROGRESS.remove(level);
            return true;
        }

        int totalChunks = runtimeRebuildChunks(center).size();
        int nextChunk = FALLBACK_GENERATION_PROGRESS.getOrDefault(level, 0);
        if (nextChunk < totalChunks) {
            generateFallbackChunk(level, center, nextChunk);
            FALLBACK_GENERATION_PROGRESS.put(level, nextChunk + 1);
            return false;
        }

        level.setBlockAndUpdate(
                new BlockPos(center.getX(), ISLAND_GENERATED_MARKER_Y, center.getZ()),
                Blocks.STRUCTURE_VOID.defaultBlockState());
        FALLBACK_GENERATION_PROGRESS.remove(level);
        RamessesEndIslandPiece island = new RamessesEndIslandPiece(center.getX(), center.getZ());
        island.initializeLoadedTowerEntities(level);
        island.initializeIslandDragon(level);
        return true;
    }

    private static boolean isIslandGenerated(ServerLevel level, BlockPos center) {
        BlockPos generatedMarker = new BlockPos(center.getX(), ISLAND_GENERATED_MARKER_Y, center.getZ());
        if (level.getBlockState(generatedMarker).is(Blocks.STRUCTURE_VOID)) {
            return true;
        }
        level.getChunk(center.getX() >> 4, center.getZ() >> 4);
        return level.structureManager()
                .getStructureWithPieceAt(center, ModStructures.RAMESSES_END_ISLAND)
                .isValid();
    }

    public static void restorePhaseCrystals(ServerLevel level, BlockPos center, int combatStage) {
        RamessesEndIslandPiece island = new RamessesEndIslandPiece(center.getX(), center.getZ());
        island.restoreLoadedTowerCrystals(level, combatStage);
    }

    public static void beginAnimatedPhaseCrystalRestore(ServerLevel level, BlockPos center, int combatStage) {
        RamessesEndIslandPiece island = new RamessesEndIslandPiece(center.getX(), center.getZ());
        island.prepareAnimatedCrystalRestore(level);
        island.stopCrystalCinematic(level);
    }

    public static void tickAnimatedPhaseCrystalRestore(ServerLevel level, BlockPos center, int buildTick) {
        if (buildTick < 0 || buildTick >= CRYSTAL_CAGE_BUILD_TICKS) {
            return;
        }
        new RamessesEndIslandPiece(center.getX(), center.getZ())
                .placeAnimatedCageStep(level, buildTick);
    }

    public static void finishAnimatedPhaseCrystalRestore(ServerLevel level, BlockPos center, int combatStage) {
        RamessesEndIslandPiece island = new RamessesEndIslandPiece(center.getX(), center.getZ());
        island.finishAnimatedCrystalRestore(level, combatStage);
        island.stopCrystalCinematic(level);
    }

    public static int runtimeRebuildChunkCount(BlockPos center) {
        return runtimeRebuildChunks(center).size();
    }

    public static void rebuildRuntimeChunk(ServerLevel level, BlockPos center, int rebuildIndex) {
        List<ChunkPos> chunks = runtimeRebuildChunks(center);
        if (rebuildIndex < 0 || rebuildIndex >= chunks.size()) {
            return;
        }
        ChunkPos chunkPos = chunks.get(rebuildIndex);
        postProcessRuntimeChunk(level, center, chunkPos);
        markDragonActive(level, center);
        markRespawnInProgress(level, center);

        int surfaceY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                chunkPos.getMiddleBlockX(), chunkPos.getMiddleBlockZ());
        level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                chunkPos.getMiddleBlockX() + 0.5D, surfaceY + 0.5D, chunkPos.getMiddleBlockZ() + 0.5D,
                18, 7.0D, 2.0D, 7.0D, 0.08D);
        if ((rebuildIndex & 7) == 0) {
            playCinematicSound(level,
                    new BlockPos(chunkPos.getMiddleBlockX(), surfaceY, chunkPos.getMiddleBlockZ()),
                    net.minecraft.sounds.SoundEvents.RESPAWN_ANCHOR_CHARGE,
                    1.5F, 0.62F + rebuildIndex * 0.002F);
        }
    }

    private static void generateFallbackChunk(ServerLevel level, BlockPos center, int chunkIndex) {
        List<ChunkPos> chunks = runtimeRebuildChunks(center);
        if (chunkIndex < 0 || chunkIndex >= chunks.size()) {
            return;
        }
        postProcessRuntimeChunk(level, center, chunks.get(chunkIndex));
    }

    private static void postProcessRuntimeChunk(ServerLevel level, BlockPos center, ChunkPos chunkPos) {
        level.getChunk(chunkPos.x, chunkPos.z);
        BoundingBox generationBox = new BoundingBox(
                chunkPos.getMinBlockX(), 16, chunkPos.getMinBlockZ(),
                chunkPos.getMaxBlockX(), 128, chunkPos.getMaxBlockZ());
        RamessesEndIslandPiece island = new RamessesEndIslandPiece(center.getX(), center.getZ());
        island.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(),
                RandomSource.create(level.getSeed() ^ chunkPos.toLong()), generationBox,
                chunkPos, BlockPos.ZERO);
    }

    static void restoreRuntimeFloorChunk(ServerLevel level, BlockPos center, ChunkPos chunkPos) {
        level.getChunk(chunkPos.x, chunkPos.z);
        RamessesEndIslandPiece island = new RamessesEndIslandPiece(center.getX(), center.getZ());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minX = Math.max(chunkPos.getMinBlockX(), island.boundingBox.minX());
        int maxX = Math.min(chunkPos.getMaxBlockX(), island.boundingBox.maxX());
        int minZ = Math.max(chunkPos.getMinBlockZ(), island.boundingBox.minZ());
        int maxZ = Math.min(chunkPos.getMaxBlockZ(), island.boundingBox.maxZ());
        long seed = level.getSeed();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int surfaceY = island.islandSurfaceY(seed, x, z);
                if (surfaceY == Integer.MIN_VALUE) {
                    continue;
                }
                int bottomY = island.islandBottomY(seed, x, z, surfaceY);
                for (int y = bottomY; y <= surfaceY; y++) {
                    pos.set(x, y, z);
                    if (level.getBlockState(pos).isAir()) {
                        level.setBlock(pos, islandBlock(seed, x, y, z), 2);
                    }
                }
            }
        }

        int surfaceY = level.getHeight(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                chunkPos.getMiddleBlockX(), chunkPos.getMiddleBlockZ());
        level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                chunkPos.getMiddleBlockX() + 0.5D, surfaceY + 0.5D,
                chunkPos.getMiddleBlockZ() + 0.5D,
                8, 5.0D, 1.0D, 5.0D, 0.04D);
    }

    public static void prepareRuntimeRespawn(ServerLevel level, BlockPos center) {
        AABB bounds = new AABB(
                center.getX() - GENERATION_RADIUS - 8, 8, center.getZ() - GENERATION_RADIUS - 8,
                center.getX() + GENERATION_RADIUS + 8, 136, center.getZ() + GENERATION_RADIUS + 8);
        for (RamessesEndCrystalEntity crystal : level.getEntitiesOfClass(
                RamessesEndCrystalEntity.class, bounds, Entity::isAlive)) {
            crystal.discard();
        }
        markDragonActive(level, center);
        markRespawnInProgress(level, center);
    }

    public static boolean hasActiveDragonMarker(ServerLevel level, BlockPos center) {
        BlockPos markerPos = new BlockPos(center.getX(), DRAGON_MARKER_Y, center.getZ());
        level.getChunk(markerPos.getX() >> 4, markerPos.getZ() >> 4);
        return level.getBlockState(markerPos).is(Blocks.STRUCTURE_VOID);
    }

    public static void markDragonActive(ServerLevel level, BlockPos center) {
        BlockPos markerPos = new BlockPos(center.getX(), DRAGON_MARKER_Y, center.getZ());
        level.setBlockAndUpdate(markerPos, Blocks.STRUCTURE_VOID.defaultBlockState());
    }

    public static void clearDragonActive(ServerLevel level, BlockPos center) {
        BlockPos markerPos = new BlockPos(center.getX(), DRAGON_MARKER_Y, center.getZ());
        level.setBlockAndUpdate(markerPos, Blocks.AIR.defaultBlockState());
    }

    public static List<BlockPos> ritualCrystalPositions(ServerLevel level, BlockPos center) {
        RamessesEndIslandPiece island = new RamessesEndIslandPiece(center.getX(), center.getZ());
        List<BlockPos> result = new ArrayList<>(TOWER_COUNT);
        for (int towerIndex = 0; towerIndex < TOWER_COUNT; towerIndex++) {
            TowerPosition tower = island.towerPosition(level.getSeed(), towerIndex);
            result.add(new BlockPos(tower.x(), tower.topY() + 1, tower.z()));
        }
        return result;
    }

    public static boolean isTowerProtectedColumn(
            ServerLevel level, BlockPos center, int x, int z) {
        RamessesEndIslandPiece island = new RamessesEndIslandPiece(center.getX(), center.getZ());
        for (int towerIndex = 0; towerIndex < TOWER_COUNT; towerIndex++) {
            TowerPosition tower = island.towerPosition(level.getSeed(), towerIndex);
            int columnRadius = 3 + (towerIndex % 3 == 0 ? 1 : 0);
            int protectionRadius = columnRadius + 4;
            int dx = x - tower.x();
            int dz = z - tower.z();
            if (dx * dx + dz * dz <= protectionRadius * protectionRadius) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasRespawnInProgress(ServerLevel level, BlockPos center) {
        BlockPos markerPos = new BlockPos(center.getX(), RESPAWN_MARKER_Y, center.getZ());
        return level.getBlockState(markerPos).is(Blocks.STRUCTURE_VOID);
    }

    public static void markRespawnInProgress(ServerLevel level, BlockPos center) {
        BlockPos markerPos = new BlockPos(center.getX(), RESPAWN_MARKER_Y, center.getZ());
        level.setBlockAndUpdate(markerPos, Blocks.STRUCTURE_VOID.defaultBlockState());
    }

    public static void clearRespawnInProgress(ServerLevel level, BlockPos center) {
        BlockPos markerPos = new BlockPos(center.getX(), RESPAWN_MARKER_Y, center.getZ());
        RamessesEndIslandPiece island = new RamessesEndIslandPiece(center.getX(), center.getZ());
        int surfaceY = island.islandSurfaceY(level.getSeed(), markerPos.getX(), markerPos.getZ());
        BlockState restored = Blocks.AIR.defaultBlockState();
        if (surfaceY != Integer.MIN_VALUE) {
            int bottomY = island.islandBottomY(
                    level.getSeed(), markerPos.getX(), markerPos.getZ(), surfaceY);
            if (markerPos.getY() >= bottomY && markerPos.getY() <= surfaceY) {
                restored = islandBlock(level.getSeed(), markerPos.getX(), markerPos.getY(), markerPos.getZ());
            }
        }
        level.setBlockAndUpdate(markerPos, restored);
    }

    static List<ChunkPos> runtimeRebuildChunks(BlockPos center) {
        int minChunkX = Math.floorDiv(center.getX() - GENERATION_RADIUS, 16);
        int maxChunkX = Math.floorDiv(center.getX() + GENERATION_RADIUS, 16);
        int minChunkZ = Math.floorDiv(center.getZ() - GENERATION_RADIUS, 16);
        int maxChunkZ = Math.floorDiv(center.getZ() + GENERATION_RADIUS, 16);
        int centerChunkX = Math.floorDiv(center.getX(), 16);
        int centerChunkZ = Math.floorDiv(center.getZ(), 16);
        List<ChunkPos> chunks = new ArrayList<>();
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                chunks.add(new ChunkPos(chunkX, chunkZ));
            }
        }
        chunks.sort(Comparator.comparingInt(chunk ->
                Mth.square(chunk.x - centerChunkX) + Mth.square(chunk.z - centerChunkZ)));
        return chunks;
    }

    private void prepareAnimatedCrystalRestore(ServerLevel level) {
        AABB bounds = new AABB(
                this.centerX - GENERATION_RADIUS, 16, this.centerZ - GENERATION_RADIUS,
                this.centerX + GENERATION_RADIUS, 128, this.centerZ + GENERATION_RADIUS);
        for (RamessesEndCrystalEntity crystal : level.getEntitiesOfClass(
                RamessesEndCrystalEntity.class, bounds, Entity::isAlive)) {
            crystal.discard();
        }
        retainTowerChunk(level, level.getSeed(), 0);
    }

    private void sendCrystalCinematicFocus(
            ServerLevel level, int towerIndex, int remainingCinematicTicks) {
        long seed = level.getSeed();
        TowerPosition focus = towerPosition(seed, Mth.clamp(towerIndex, 0, TOWER_COUNT - 1));
        for (ServerPlayer player : level.players()) {
            double centerDx = player.getX() - this.centerX;
            double centerDz = player.getZ() - this.centerZ;
            if (centerDx * centerDx + centerDz * centerDz > 192.0D * 192.0D) {
                continue;
            }
            SunEaterStageCinematicPacket packet = new SunEaterStageCinematicPacket(
                    focus.x() + 0.5D, focus.topY() + 1.9D, focus.z() + 0.5D,
                    remainingCinematicTicks);
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
        }
    }

    private void stopCrystalCinematic(ServerLevel level) {
        SunEaterStageCinematicPacket packet = new SunEaterStageCinematicPacket(0.0D, 0.0D, 0.0D, 0);
        for (ServerPlayer player : level.players()) {
            double centerDx = player.getX() - this.centerX;
            double centerDz = player.getZ() - this.centerZ;
            if (centerDx * centerDx + centerDz * centerDz <= 224.0D * 224.0D) {
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
            }
        }
    }

    private void placeAnimatedCageStep(ServerLevel level, int buildTick) {
        long seed = level.getSeed();
        int towerIndex = buildTick / TOWER_SEQUENCE_TICKS;
        int towerTick = buildTick % TOWER_SEQUENCE_TICKS;
        if (towerIndex >= TOWER_COUNT) {
            return;
        }
        TowerPosition tower = towerPosition(seed, towerIndex);
        BlockPos crystalPos = new BlockPos(tower.x(), tower.topY() + 1, tower.z());
        ChunkPos towerChunk = new ChunkPos(crystalPos);
        if (towerTick == 0) {
            level.getChunkSource().addRegionTicket(TicketType.PORTAL, towerChunk, 2, crystalPos);
            retainTowerChunk(level, seed, towerIndex + 1);
            level.getChunk(towerChunk.x, towerChunk.z);
            RamessesEndCrystalEntity oldCrystal = findTowerCrystal(level, crystalPos);
            if (oldCrystal != null) {
                oldCrystal.discard();
            }
        }
        if (towerTick < TOWER_CAMERA_TURN_TICKS) {
            int firstBlock = towerTick * CAGE_CLEAR_BLOCKS_PER_TICK;
            int lastBlock = Math.min(firstBlock + CAGE_CLEAR_BLOCKS_PER_TICK, CAGE_BLOCK_COUNT);
            clearCrystalCageRange(level, tower, firstBlock, lastBlock);
            return;
        }
        int cageTick = towerTick - TOWER_CAMERA_TURN_TICKS;
        if (cageTick < TOWER_CAGE_BUILD_TICKS) {
            int firstBlock = cageTick * CAGE_BLOCKS_PER_TICK;
            int lastBlock = Math.min(firstBlock + CAGE_BLOCKS_PER_TICK, CAGE_BLOCK_COUNT);
            for (int blockIndex = firstBlock; blockIndex < lastBlock; blockIndex++) {
                CageBlock cageBlock = cageBlockAt(tower, blockIndex);
                placeAnimatedCageBlock(level, cageBlock, buildTick, blockIndex);
            }
            return;
        }
        revealTowerCrystal(level, tower);
        level.getChunkSource().removeRegionTicket(TicketType.PORTAL, towerChunk, 2, crystalPos);
    }

    private static void placeAnimatedCageBlock(
            ServerLevel level, CageBlock cageBlock, int buildTick, int blockIndex) {
        if (cageBlock == null || level.getBlockState(cageBlock.pos()).equals(cageBlock.state())) {
            return;
        }
        level.setBlock(cageBlock.pos(), cageBlock.state(),
                Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, cageBlock.state()),
                cageBlock.pos().getX() + 0.5D,
                cageBlock.pos().getY() + 0.55D,
                cageBlock.pos().getZ() + 0.5D,
                1, 0.12D, 0.16D, 0.12D, 0.025D);
        if (blockIndex % CAGE_BLOCKS_PER_TICK == 0) {
            SoundType sound = cageBlock.state().getSoundType();
            playCinematicSound(level, cageBlock.pos(), sound.getPlaceSound(),
                    (sound.getVolume() + 1.0F) * 0.5F,
                    sound.getPitch() * (0.82F + buildTick * 0.0008F));
        }
    }

    private static void playCinematicSound(
            ServerLevel level, BlockPos sourcePos, net.minecraft.sounds.SoundEvent sound,
            float volume, float pitch) {
        level.playSound(null, sourcePos, sound,
                net.minecraft.sounds.SoundSource.BLOCKS, volume, pitch);
        for (ServerPlayer player : level.players()) {
            double distance = player.distanceToSqr(
                    sourcePos.getX() + 0.5D, sourcePos.getY() + 0.5D, sourcePos.getZ() + 0.5D);
            if (distance > 24.0D * 24.0D && distance <= 224.0D * 224.0D) {
                player.playNotifySound(sound, net.minecraft.sounds.SoundSource.BLOCKS,
                        Math.min(0.82F, volume), pitch);
            }
        }
    }

    private RamessesEndCrystalEntity revealTowerCrystal(ServerLevel level, TowerPosition tower) {
        BlockPos crystalPos = new BlockPos(tower.x(), tower.topY() + 1, tower.z());
        RamessesEndCrystalEntity crystal = findTowerCrystal(level, crystalPos);
        if (crystal == null) {
            crystal = placeTowerCrystal(level, crystalPos);
        }
        if (crystal != null) {
            crystal.restoreForCombatStage();
            crystal.setInvulnerable(true);
            crystal.markTowerGuardianInitialized();
            level.sendParticles(ParticleTypes.END_ROD,
                    crystal.getX(), crystal.getY() + 0.8D, crystal.getZ(),
                    30, 0.65D, 0.85D, 0.65D, 0.075D);
            level.sendParticles(ParticleTypes.FLASH,
                    crystal.getX(), crystal.getY() + 0.8D, crystal.getZ(),
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
            playCinematicSound(level, crystalPos,
                    net.minecraft.sounds.SoundEvents.RESPAWN_ANCHOR_CHARGE,
                    1.35F, 1.22F);
        }
        return crystal;
    }

    private void finishAnimatedCrystalRestore(ServerLevel level, int combatStage) {
        long seed = level.getSeed();
        for (int towerIndex = 0; towerIndex < TOWER_COUNT; towerIndex++) {
            TowerPosition tower = towerPosition(seed, towerIndex);
            releaseTowerChunk(level, seed, towerIndex);
            rebuildCrystalCage(level, tower);
            BlockPos crystalPos = new BlockPos(tower.x(), tower.topY() + 1, tower.z());
            RamessesEndCrystalEntity crystal = findTowerCrystal(level, crystalPos);
            if (crystal == null) {
                crystal = revealTowerCrystal(level, tower);
            }
            if (crystal == null) {
                continue;
            }
            crystal.restoreForCombatStage();
            crystal.markTowerGuardianInitialized();
            if (combatStage >= 3) {
                BlockPos chamberPos = new BlockPos(tower.x(), tower.topY() - 5, tower.z());
                GoetyObsidianMonolithCompat.trySpawnIslandMonolith(level, chamberPos, crystal);
                GoetyObsidianMonolithCompat.removeIslandGuardianMobs(level, chamberPos);
            }
        }
    }

    public static void beginStageCrystalRitual(ServerLevel level, BlockPos center) {
        RamessesEndIslandPiece island = new RamessesEndIslandPiece(center.getX(), center.getZ());
        List<RamessesEndCrystalEntity> crystals = island.findAllTowerCrystals(level);
        if (crystals.isEmpty()) {
            return;
        }
        for (int index = 0; index < crystals.size(); index++) {
            RamessesEndCrystalEntity crystal = crystals.get(index);
            RamessesEndCrystalEntity next = crystals.get((index + 1) % crystals.size());
            crystal.beginRespawnRitual(next.blockPosition());
        }
    }

    public static void focusStageCrystalsOnCenter(ServerLevel level, BlockPos center) {
        RamessesEndIslandPiece island = new RamessesEndIslandPiece(center.getX(), center.getZ());
        BlockPos focus = stageRitualFocus(center);
        for (RamessesEndCrystalEntity crystal : island.findAllTowerCrystals(level)) {
            crystal.setRespawnRitualBeamTarget(focus);
        }
    }

    public static void finishStageCrystalRitual(ServerLevel level, BlockPos center) {
        RamessesEndIslandPiece island = new RamessesEndIslandPiece(center.getX(), center.getZ());
        for (RamessesEndCrystalEntity crystal : island.findAllTowerCrystals(level)) {
            crystal.finishRespawnRitualForCombat();
        }
    }

    public static BlockPos stageRitualFocus(BlockPos center) {
        return center.above(48);
    }

    private List<RamessesEndCrystalEntity> findAllTowerCrystals(ServerLevel level) {
        List<RamessesEndCrystalEntity> crystals = new ArrayList<>(TOWER_COUNT);
        long seed = level.getSeed();
        for (int towerIndex = 0; towerIndex < TOWER_COUNT; towerIndex++) {
            TowerPosition tower = towerPosition(seed, towerIndex);
            BlockPos crystalPos = new BlockPos(tower.x(), tower.topY() + 1, tower.z());
            RamessesEndCrystalEntity crystal = findTowerCrystal(level, crystalPos);
            if (crystal != null) {
                crystals.add(crystal);
            }
        }
        return crystals;
    }

    private static void clearCrystalCageRange(
            ServerLevel level, TowerPosition tower, int firstBlock, int lastBlock) {
        for (int step = firstBlock; step < lastBlock; step++) {
            CageBlock cageBlock = cageBlockAt(tower, step);
            if (cageBlock != null && !level.getBlockState(cageBlock.pos()).isAir()) {
                level.setBlock(cageBlock.pos(), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }

    private void retainTowerChunk(ServerLevel level, long seed, int towerIndex) {
        if (towerIndex < 0 || towerIndex >= TOWER_COUNT) {
            return;
        }
        TowerPosition tower = towerPosition(seed, towerIndex);
        BlockPos ticketKey = new BlockPos(tower.x(), tower.topY() + 1, tower.z());
        level.getChunkSource().addRegionTicket(
                TicketType.PORTAL, new ChunkPos(ticketKey), 2, ticketKey);
    }

    private void releaseTowerChunk(ServerLevel level, long seed, int towerIndex) {
        TowerPosition tower = towerPosition(seed, towerIndex);
        BlockPos ticketKey = new BlockPos(tower.x(), tower.topY() + 1, tower.z());
        level.getChunkSource().removeRegionTicket(
                TicketType.PORTAL, new ChunkPos(ticketKey), 2, ticketKey);
    }

    private static CageBlock cageBlockAt(TowerPosition tower, int index) {
        int floorY = tower.topY();
        if (index < 25) {
            int dx = index / 5 - 2;
            int dz = index % 5 - 2;
            return new CageBlock(new BlockPos(tower.x() + dx, floorY, tower.z() + dz),
                    Blocks.OBSIDIAN.defaultBlockState());
        }
        if (index < 73) {
            int sideIndex = index - 25;
            int y = floorY + 1 + sideIndex / 16;
            int perimeterIndex = sideIndex % 16;
            int[] offset = perimeterOffset(perimeterIndex);
            return new CageBlock(new BlockPos(tower.x() + offset[0], y, tower.z() + offset[1]),
                    ironBarsForCage(offset[0], offset[1]));
        }
        int roofIndex = index - 73;
        int dx = roofIndex / 5 - 2;
        int dz = roofIndex % 5 - 2;
        return new CageBlock(new BlockPos(tower.x() + dx, floorY + 4, tower.z() + dz),
                Blocks.OBSIDIAN.defaultBlockState());
    }

    private static int[] perimeterOffset(int index) {
        if (index < 5) {
            return new int[]{index - 2, -2};
        }
        if (index < 9) {
            return new int[]{2, index - 6};
        }
        if (index < 13) {
            return new int[]{10 - index, 2};
        }
        return new int[]{-2, 14 - index};
    }

    private void restoreLoadedTowerCrystals(ServerLevel level, int combatStage) {
        long seed = level.getSeed();
        for (int towerIndex = 0; towerIndex < TOWER_COUNT; towerIndex++) {
            TowerPosition tower = towerPosition(seed, towerIndex);
            BlockPos crystalPos = new BlockPos(tower.x(), tower.topY() + 1, tower.z());
            level.getChunk(crystalPos.getX() >> 4, crystalPos.getZ() >> 4);
            rebuildCrystalCage(level, tower);
            RamessesEndCrystalEntity crystal = findTowerCrystal(level, crystalPos);
            if (crystal == null) {
                crystal = placeTowerCrystal(level, crystalPos);
            }
            if (crystal == null) {
                continue;
            }
            crystal.restoreForCombatStage();
            crystal.markTowerGuardianInitialized();
            if (combatStage == 1 || combatStage >= 3) {
                BlockPos chamberPos = new BlockPos(tower.x(), tower.topY() - 5, tower.z());
                GoetyObsidianMonolithCompat.trySpawnIslandMonolith(level, chamberPos, crystal);
                GoetyObsidianMonolithCompat.removeIslandGuardianMobs(level, chamberPos);
            }
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                    crystal.getX(), crystal.getY() + 0.8D, crystal.getZ(),
                    30, 0.7D, 0.9D, 0.7D, 0.08D);
            level.playSound(null, crystalPos, net.minecraft.sounds.SoundEvents.RESPAWN_ANCHOR_CHARGE,
                    net.minecraft.sounds.SoundSource.HOSTILE, 1.2F, 1.25F);
        }
    }

    private static void rebuildCrystalCage(ServerLevel level, TowerPosition tower) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int floorY = tower.topY();
        int roofY = tower.topY() + 4;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                setCageBlockIfMissing(level, pos.set(tower.x() + dx, floorY, tower.z() + dz),
                        Blocks.OBSIDIAN.defaultBlockState());
                setCageBlockIfMissing(level, pos.set(tower.x() + dx, roofY, tower.z() + dz),
                        Blocks.OBSIDIAN.defaultBlockState());
                if (Math.abs(dx) != 2 && Math.abs(dz) != 2) {
                    continue;
                }
                BlockState bars = ironBarsForCage(dx, dz);
                for (int y = floorY + 1; y < roofY; y++) {
                    setCageBlockIfMissing(level, pos.set(tower.x() + dx, y, tower.z() + dz), bars);
                }
            }
        }
    }

    private static void setCageBlockIfMissing(ServerLevel level, BlockPos pos, BlockState state) {
        if (!level.getBlockState(pos).equals(state)) {
            level.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
    }

    private void initializeIslandDragon(ServerLevel level) {
        BlockPos markerPos = new BlockPos(this.centerX, DRAGON_MARKER_Y, this.centerZ);
        if (!level.hasChunkAt(markerPos)) {
            return;
        }
        AABB searchBox = new AABB(
                this.centerX - DRAGON_SEARCH_RADIUS, level.getMinBuildHeight(), this.centerZ - DRAGON_SEARCH_RADIUS,
                this.centerX + DRAGON_SEARCH_RADIUS, level.getMaxBuildHeight(), this.centerZ + DRAGON_SEARCH_RADIUS);
        List<DuatSunEaterEntity> existing = level.getEntitiesOfClass(
                DuatSunEaterEntity.class, searchBox, entity -> entity.isAlive() || entity.dragonDeathTime > 0);
        if (!existing.isEmpty()) {
            if (!level.getBlockState(markerPos).is(Blocks.STRUCTURE_VOID)) {
                level.setBlockAndUpdate(markerPos, Blocks.STRUCTURE_VOID.defaultBlockState());
            }
            return;
        }
        if (level.getBlockState(markerPos).is(Blocks.STRUCTURE_VOID)) {
            return;
        }
        BlockPos islandCenter = new BlockPos(this.centerX, CENTER_Y, this.centerZ);
        if (level.getBlockState(islandCenter).isAir()) {
            return;
        }
        DuatSunEaterEntity dragon = ModEntities.DUAT_SUN_EATER.get().create(level);
        if (dragon == null) {
            return;
        }
        dragon.setFightOrigin(islandCenter);
        dragon.setPos(this.centerX + 0.5D, CENTER_Y + 48.0D, this.centerZ + 0.5D);
        dragon.setYRot(level.random.nextFloat() * 360.0F);
        dragon.getPhaseManager().setPhase(
                net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase.HOLDING_PATTERN);
        if (level.addFreshEntity(dragon)) {
            level.setBlockAndUpdate(markerPos, Blocks.STRUCTURE_VOID.defaultBlockState());
        }
    }

    private void initializeLoadedTowerEntities(ServerLevel level) {
        long seed = level.getSeed();
        for (int towerIndex = 0; towerIndex < TOWER_COUNT; towerIndex++) {
            TowerPosition tower = towerPosition(seed, towerIndex);
            if (level.getChunkSource().getChunkNow(tower.x() >> 4, tower.z() >> 4) == null) {
                continue;
            }
            BlockPos markerPos = new BlockPos(tower.x(), tower.topY() - 6, tower.z());
            BlockPos durableMarkerPos = new BlockPos(tower.x(), tower.topY() - 7, tower.z());
            BlockPos chamberPos = new BlockPos(tower.x(), tower.topY() - 5, tower.z());
            BlockPos crystalPos = new BlockPos(tower.x(), tower.topY() + 1, tower.z());
            GoetyObsidianMonolithCompat.removeIslandGuardianMobs(level, chamberPos);
            RamessesEndCrystalEntity crystal = findTowerCrystal(level, crystalPos);
            boolean legacyInitialized = level.getBlockState(markerPos).is(Blocks.GILDED_BLACKSTONE);
            boolean initialized = legacyInitialized
                    || level.getBlockState(durableMarkerPos).is(Blocks.STRUCTURE_VOID);
            if (initialized) {
                if (crystal != null) {
                    crystal.markTowerGuardianInitialized();
                    GoetyObsidianMonolithCompat.bindExistingIslandMonolith(level, chamberPos, crystal);
                }
                if (!level.getBlockState(durableMarkerPos).is(Blocks.STRUCTURE_VOID)) {
                    level.setBlockAndUpdate(durableMarkerPos, Blocks.STRUCTURE_VOID.defaultBlockState());
                }
                continue;
            }
            // A tower crystal is only created during the one-time runtime initialization.
            // Treat its presence as the durable marker so breaking the floor block cannot respawn the guardian.
            if (crystal != null) {
                level.setBlockAndUpdate(durableMarkerPos, Blocks.STRUCTURE_VOID.defaultBlockState());
                crystal.markTowerGuardianInitialized();
                GoetyObsidianMonolithCompat.bindExistingIslandMonolith(level, chamberPos, crystal);
                continue;
            }
            if (level.getBlockState(crystalPos.below()).isAir()
                    || !level.getBlockState(chamberPos).isAir()) {
                continue;
            }
            if (crystal == null) {
                crystal = placeTowerCrystal(level, crystalPos);
            }
            if (crystal != null) {
                crystal.markTowerGuardianInitialized();
                GoetyObsidianMonolithCompat.trySpawnIslandMonolith(level, chamberPos, crystal);
                level.setBlockAndUpdate(durableMarkerPos, Blocks.STRUCTURE_VOID.defaultBlockState());
            }
            if (!legacyInitialized) {
                level.setBlockAndUpdate(markerPos, Blocks.GILDED_BLACKSTONE.defaultBlockState());
            }
        }
    }

    private int islandSurfaceY(long seed, int x, int z) {
        double dx = x - this.centerX;
        double dz = z - this.centerZ;
        double angle = Math.atan2(dz, dx);
        double seedPhase = ((seed >>> 24) & 1023L) * (Math.PI * 2.0D / 1024.0D);
        double boundary = ISLAND_RADIUS
                + Math.sin(angle * 5.0D + seedPhase) * 6.5D
                + Math.sin(angle * 9.0D - seedPhase * 0.7D) * 3.5D
                + Math.sin(angle * 17.0D + 1.4D) * 1.8D;
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance > boundary) {
            return Integer.MIN_VALUE;
        }
        int fracture = hashRoll(seed ^ 0xD1B54A32D192ED03L, x, 0, z);
        double normalized = distance / boundary;
        if (normalized > 0.80D && fracture < 7) {
            return Integer.MIN_VALUE;
        }
        double roughness = Math.sin((dx + seedPhase * 11.0D) * 0.085D) * 0.75D
                + Math.sin((dz - seedPhase * 7.0D) * 0.10D) * 0.65D
                + Math.sin((dx + dz) * 0.05D + seedPhase) * 0.45D;
        double crown = Math.max(0.0D, 1.0D - normalized * normalized) * 3.0D;
        double edgeDrop = normalized > 0.86D ? (normalized - 0.86D) * 25.0D : 0.0D;
        return CENTER_Y + (int) Math.round(crown + roughness - edgeDrop);
    }

    private int islandBottomY(long seed, int x, int z, int surfaceY) {
        double distance = Math.sqrt(square(x - this.centerX) + square(z - this.centerZ));
        double normalized = Math.min(1.0D, distance / ISLAND_RADIUS);
        double undersideRoughness = Math.sin(x * 0.093D + z * 0.057D + seed * 0.0000001D) * 3.0D;
        int thickness = (int) Math.round(6.0D + (1.0D - normalized) * 35.0D + undersideRoughness);
        return surfaceY - Math.max(4, thickness);
    }

    private void placeTowerColumn(
            WorldGenLevel level,
            BoundingBox generationBox,
            BlockPos.MutableBlockPos pos,
            long seed,
            int x,
            int z,
            int towerIndex) {
        double seedPhase = ((seed >>> 12) & 511L) * (Math.PI * 2.0D / 512.0D);
        double angle = Math.PI * 2.0D * towerIndex / TOWER_COUNT
                + Math.sin(towerIndex * 2.17D + seedPhase) * 0.09D;
        double orbit = 56.0D + Math.sin(towerIndex * 1.73D + seedPhase) * 4.0D;
        int towerX = this.centerX + (int) Math.round(Math.cos(angle) * orbit);
        int towerZ = this.centerZ + (int) Math.round(Math.sin(angle) * orbit);
        int radius = 3 + (towerIndex % 3 == 0 ? 1 : 0);
        int dx = x - towerX;
        int dz = z - towerZ;
        int distanceSquared = dx * dx + dz * dz;
        int baseSurface = islandSurfaceY(seed, towerX, towerZ);
        if (baseSurface == Integer.MIN_VALUE) {
            return;
        }
        int topY = baseSurface + 22 + Math.floorMod(towerIndex * 11 + (int) seed, 19);
        if (distanceSquared > (radius + 3) * (radius + 3)) {
            return;
        }
        int columnSurface = islandSurfaceY(seed, x, z);
        int startY = columnSurface == Integer.MIN_VALUE ? baseSurface - 3 : columnSurface - 1;
        for (int y = startY; y <= topY; y++) {
            int segment = Math.floorDiv(y - startY, 5);
            int shiftX = hashRoll(seed ^ (towerIndex * 0x632BE59BD9B4E019L), segment, 2, towerIndex) % 3 - 1;
            int shiftZ = hashRoll(seed ^ (towerIndex * 0x9E3779B97F4A7C15L), segment, 7, towerIndex) % 3 - 1;
            int localX = x - (towerX + shiftX);
            int localZ = z - (towerZ + shiftZ);
            int localDistance = localX * localX + localZ * localZ;
            boolean crown = y >= topY - 2;
            int layerRadius = crown ? radius + 1 : radius;
            if (localDistance > layerRadius * layerRadius) {
                continue;
            }
            int coreRadius = Math.max(1, layerRadius - 2);
            int chip = hashRoll(seed ^ 0xA24BAED4963EE407L ^ towerIndex, x, y, z);
            int chipThreshold = crown ? 31 : 18;
            if (localDistance > coreRadius * coreRadius && chip < chipThreshold) {
                continue;
            }
            if (generationBox.isInside(pos.set(x, y, z))) {
                level.setBlock(pos, towerBlock(seed, x, y, z), 2);
            }
        }
    }

    private RamessesEndCrystalEntity findTowerCrystal(ServerLevel level, BlockPos crystalPos) {
        AABB searchBox = new AABB(crystalPos).inflate(1.5D);
        List<RamessesEndCrystalEntity> crystals =
                level.getEntitiesOfClass(RamessesEndCrystalEntity.class, searchBox);
        return crystals.isEmpty() ? null : crystals.get(0);
    }

    private RamessesEndCrystalEntity placeTowerCrystal(ServerLevel level, BlockPos crystalPos) {
        RamessesEndCrystalEntity existing = findTowerCrystal(level, crystalPos);
        if (existing != null) {
            return existing;
        }
        RamessesEndCrystalEntity crystal = ModEntities.RAMESSES_END_CRYSTAL.get().create(level);
        if (crystal != null) {
            crystal.setPos(crystalPos.getX() + 0.5D, crystalPos.getY(), crystalPos.getZ() + 0.5D);
            crystal.setShowBottom(false);
            return level.addFreshEntity(crystal) ? crystal : null;
        }
        return null;
    }

    private void carveTowerChamber(WorldGenLevel level, BoundingBox generationBox, long seed, int towerIndex) {
        TowerPosition tower = towerPosition(seed, towerIndex);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = tower.x() - 1; x <= tower.x() + 1; x++) {
            for (int z = tower.z() - 1; z <= tower.z() + 1; z++) {
                for (int y = tower.topY() - 5; y < tower.topY(); y++) {
                    if (generationBox.isInside(pos.set(x, y, z))) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private void placeCrystalCage(WorldGenLevel level, BoundingBox generationBox, long seed, int towerIndex) {
        TowerPosition tower = towerPosition(seed, towerIndex);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int floorY = tower.topY();
        int roofY = tower.topY() + 4;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (generationBox.isInside(pos.set(tower.x() + dx, floorY, tower.z() + dz))) {
                    level.setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), 2);
                }
                if (generationBox.isInside(pos.set(tower.x() + dx, roofY, tower.z() + dz))) {
                    level.setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), 2);
                }
                if (Math.abs(dx) != 2 && Math.abs(dz) != 2) {
                    continue;
                }
                BlockState bars = ironBarsForCage(dx, dz);
                for (int y = floorY + 1; y < roofY; y++) {
                    if (generationBox.isInside(pos.set(tower.x() + dx, y, tower.z() + dz))) {
                        level.setBlock(pos, bars, 2);
                    }
                }
            }
        }
    }

    private static BlockState ironBarsForCage(int dx, int dz) {
        boolean alongZ = Math.abs(dx) == 2;
        boolean alongX = Math.abs(dz) == 2;
        return Blocks.IRON_BARS.defaultBlockState()
                .setValue(IronBarsBlock.NORTH, alongZ)
                .setValue(IronBarsBlock.SOUTH, alongZ)
                .setValue(IronBarsBlock.WEST, alongX)
                .setValue(IronBarsBlock.EAST, alongX);
    }

    private TowerPosition towerPosition(long seed, int towerIndex) {
        double seedPhase = ((seed >>> 12) & 511L) * (Math.PI * 2.0D / 512.0D);
        double angle = Math.PI * 2.0D * towerIndex / TOWER_COUNT
                + Math.sin(towerIndex * 2.17D + seedPhase) * 0.09D;
        double orbit = 56.0D + Math.sin(towerIndex * 1.73D + seedPhase) * 4.0D;
        int towerX = this.centerX + (int) Math.round(Math.cos(angle) * orbit);
        int towerZ = this.centerZ + (int) Math.round(Math.sin(angle) * orbit);
        int baseSurface = islandSurfaceY(seed, towerX, towerZ);
        int topY = baseSurface + 22 + Math.floorMod(towerIndex * 11 + (int) seed, 19);
        return new TowerPosition(towerX, towerZ, topY);
    }

    private record TowerPosition(int x, int z, int topY) {
    }

    private record CageBlock(BlockPos pos, BlockState state) {
    }

    private static BlockState islandBlock(long seed, int x, int y, int z) {
        int roll = materialRoll(seed ^ 0x5DEECE66DL, x, y, z);
        if (roll < 38) return Blocks.END_STONE.defaultBlockState();
        if (roll < 64) return Blocks.OBSIDIAN.defaultBlockState();
        if (roll < 81) return Blocks.CRYING_OBSIDIAN.defaultBlockState();
        return ModBlocks.DUAT_DUNGEON_TILE.get().defaultBlockState();
    }

    private static BlockState towerBlock(long seed, int x, int y, int z) {
        int roll = materialRoll(seed ^ 0xC6BC279692B5CC83L, x, y, z);
        if (roll < 34) return ModBlocks.DUAT_DUNGEON_TILE.get().defaultBlockState();
        if (roll < 63) return Blocks.OBSIDIAN.defaultBlockState();
        if (roll < 84) return Blocks.CRYING_OBSIDIAN.defaultBlockState();
        return Blocks.END_STONE.defaultBlockState();
    }

    private static int materialRoll(long seed, int x, int y, int z) {
        int roll = fragmentRoll(seed, x, y, z, 3, 5);
        if (roll >= 0) return roll;
        roll = fragmentRoll(seed ^ 0xD6E8FEB86659FD93L, x, y, z, 2, 7);
        if (roll >= 0) return roll;
        return hashRoll(seed, x, y, z);
    }

    private static int fragmentRoll(long seed, int x, int y, int z, int length, int chance) {
        int cellX = Math.floorDiv(x, length);
        if (hashRoll(seed ^ 0xA24BAED4963EE407L, cellX, y, z) < chance) {
            return hashRoll(seed ^ 0x9FB21C651E98DF25L, cellX, y, z);
        }
        int cellY = Math.floorDiv(y, length);
        if (hashRoll(seed ^ 0xC13FA9A902A6328FL, x, cellY, z) < chance) {
            return hashRoll(seed ^ 0x91E10DA5C79E7B1DL, x, cellY, z);
        }
        int cellZ = Math.floorDiv(z, length);
        if (hashRoll(seed ^ 0xDB4F0B9175AE2165L, x, y, cellZ) < chance) {
            return hashRoll(seed ^ 0xBBE0563303A4615FL, x, y, cellZ);
        }
        return -1;
    }

    private static int hashRoll(long seed, int x, int y, int z) {
        long value = seed;
        value ^= (long) x * 0x9E3779B97F4A7C15L;
        value ^= (long) y * 0xC2B2AE3D27D4EB4FL;
        value ^= (long) z * 0x165667B19E3779F9L;
        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        value *= 0x94D049BB133111EBL;
        value ^= value >>> 31;
        return (int) Math.floorMod(value, 100L);
    }

    private static double square(double value) {
        return value * value;
    }
}
