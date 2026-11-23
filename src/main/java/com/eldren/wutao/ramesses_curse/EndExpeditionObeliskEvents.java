package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID)
public final class EndExpeditionObeliskEvents {
    private static final BlockPos CENTRAL_BASE = new BlockPos(16, 64, 0);
    private static final BlockPos CENTRAL_CORE = CENTRAL_BASE.above(4);
    private static final BlockPos REMOTE_BASE = new BlockPos(
            RamessesEndIslandStructure.CENTER_X + 150,
            RamessesEndIslandPiece.CENTER_Y + 11,
            RamessesEndIslandStructure.CENTER_Z);
    private static final BlockPos REMOTE_CORE = REMOTE_BASE.above(4);
    private static final int LAYER_INTERVAL_TICKS = 7;
    private static final double MAIN_ISLAND_PLAYER_RADIUS = 112.0D;
    private static final Map<ServerLevel, List<ObeliskBuild>> ACTIVE_BUILDS = new WeakHashMap<>();

    private EndExpeditionObeliskEvents() {
    }

    public static void teleportIslandPlayersToNearestObelisk(
            ServerLevel level, BlockPos islandCenter) {
        if (!Level.END.equals(level.dimension())) return;
        BlockPos destinationBase = nearestObeliskBase(islandCenter);
        boolean remoteDestination = destinationBase.equals(REMOTE_BASE);
        if (remoteDestination) {
            ensureRemoteObelisk(level);
        } else {
            ensureCentralObelisk(level);
        }
        level.getChunk(destinationBase.getX() >> 4, destinationBase.getZ() >> 4);
        BlockPos arrival = destinationBase.offset(remoteDestination ? 5 : 2, 1, 0);
        double radiusSquared = MAIN_ISLAND_PLAYER_RADIUS * MAIN_ISLAND_PLAYER_RADIUS;
        for (ServerPlayer player : List.copyOf(level.players())) {
            double dx = player.getX() - islandCenter.getX() - 0.5D;
            double dz = player.getZ() - islandCenter.getZ() - 0.5D;
            if (dx * dx + dz * dz > radiusSquared) continue;
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY(0.5D), player.getZ(),
                    90, 0.8D, 1.2D, 0.8D, 0.12D);
            level.playSound(null, player.blockPosition(), SoundEvents.END_PORTAL_SPAWN,
                    SoundSource.PLAYERS, 1.0F, 0.9F);
            player.teleportTo(level, arrival.getX() + 0.5D, arrival.getY(), arrival.getZ() + 0.5D,
                    -90.0F, 0.0F);
            player.setDeltaMovement(0.0D, 0.0D, 0.0D);
            player.fallDistance = 0.0F;
            player.displayClientMessage(Component.translatable(
                    "message.ramesses_curse.sun_eater_crown_escape"), true);
        }
    }

    private static BlockPos nearestObeliskBase(BlockPos islandCenter) {
        return horizontalDistanceSquared(islandCenter, REMOTE_BASE)
                <= horizontalDistanceSquared(islandCenter, CENTRAL_BASE)
                ? REMOTE_BASE
                : CENTRAL_BASE;
    }

    private static long horizontalDistanceSquared(BlockPos first, BlockPos second) {
        long dx = (long) first.getX() - second.getX();
        long dz = (long) first.getZ() - second.getZ();
        return dx * dx + dz * dz;
    }

    @SubscribeEvent
    public static void onVanillaDragonDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon dragon)
                || !(dragon.level() instanceof ServerLevel level)
                || !level.dimension().equals(Level.END)
                || dragon.distanceToSqr(0.0D, dragon.getY(), 0.0D) > 512.0D * 512.0D) {
            return;
        }
        ensureCentralObelisk(level);
        for (ServerPlayer player : level.players()) {
            player.displayClientMessage(Component.translatable(
                    "message.ramesses_curse.end_obelisk_created"), false);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
                || player.tickCount % 100 != 0
                || !(player.level() instanceof ServerLevel level)
                || !level.dimension().equals(Level.END)
                || player.distanceToSqr(0.0D, player.getY(), 0.0D) > 256.0D * 256.0D
                || level.getDragonFight() == null
                || !level.getDragonFight().hasPreviouslyKilledDragon()) {
            return;
        }
        ensureCentralObelisk(level);
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) {
            return;
        }
        List<ObeliskBuild> builds = ACTIVE_BUILDS.get(level);
        if (builds == null) {
            return;
        }
        builds.removeIf(build -> build.tick(level));
        if (builds.isEmpty()) {
            ACTIVE_BUILDS.remove(level);
        }
    }

    @SubscribeEvent
    public static void onUseObelisk(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND
                || !event.getLevel().dimension().equals(Level.END)
                || !event.getItemStack().is(ModItems.SUN_EATER_BEACON.get())
                || (!event.getPos().equals(CENTRAL_CORE) && !event.getPos().equals(REMOTE_CORE))) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        boolean departing = event.getPos().equals(CENTRAL_CORE);
        if (departing && !ModEvents.isSealEquippedForPalace(player)) {
            player.displayClientMessage(Component.translatable(
                    "message.ramesses_curse.end_obelisk_requires_seal"), true);
            level.playSound(null, event.getPos(), SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(),
                    SoundSource.BLOCKS, 0.8F, 0.6F);
            return;
        }

        BlockPos arrival;
        if (departing) {
            level.getChunk(RamessesEndIslandStructure.CENTER_X >> 4,
                    RamessesEndIslandStructure.CENTER_Z >> 4);
            ensureRemoteObelisk(level);
            arrival = REMOTE_BASE.offset(5, 1, 0);
        } else {
            ensureCentralObelisk(level);
            arrival = CENTRAL_BASE.offset(2, 1, 0);
        }

        level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                player.getX(), player.getY(0.5D), player.getZ(),
                90, 0.8D, 1.2D, 0.8D, 0.12D);
        level.playSound(null, player.blockPosition(), SoundEvents.END_PORTAL_SPAWN,
                SoundSource.PLAYERS, 1.0F, departing ? 0.65F : 0.9F);
        player.teleportTo(level, arrival.getX() + 0.5D, arrival.getY(), arrival.getZ() + 0.5D,
                departing ? 90.0F : -90.0F, 0.0F);
        player.setDeltaMovement(0.0D, 0.0D, 0.0D);
        player.fallDistance = 0.0F;
        player.displayClientMessage(Component.translatable(departing
                ? "message.ramesses_curse.end_obelisk_depart"
                : "message.ramesses_curse.end_obelisk_return"), true);
    }

    private static void ensureCentralObelisk(ServerLevel level) {
        if (!level.getBlockState(CENTRAL_CORE).is(Blocks.LODESTONE)
                && !isBuilding(level, CENTRAL_BASE)) {
            startObeliskBuild(level, CENTRAL_BASE, false);
        }
    }

    private static void ensureRemoteObelisk(ServerLevel level) {
        if (!level.getBlockState(REMOTE_CORE).is(Blocks.LODESTONE)
                && !isBuilding(level, REMOTE_BASE)) {
            startObeliskBuild(level, REMOTE_BASE, true);
        }
    }

    private static boolean isBuilding(ServerLevel level, BlockPos base) {
        return ACTIVE_BUILDS.getOrDefault(level, List.of()).stream()
                .anyMatch(build -> build.base.equals(base));
    }

    private static void startObeliskBuild(ServerLevel level, BlockPos base, boolean safePlatform) {
        if (safePlatform) {
            prepareSafePlatform(level, base);
        }
        ACTIVE_BUILDS.computeIfAbsent(level, ignored -> new ArrayList<>())
                .add(new ObeliskBuild(base.immutable(), safePlatform));
    }

    private static void prepareSafePlatform(ServerLevel level, BlockPos base) {
        int platformRadius = 7;
        for (int dx = -platformRadius; dx <= platformRadius; dx++) {
            for (int dz = -platformRadius; dz <= platformRadius; dz++) {
                if (dx * dx + dz * dz > platformRadius * platformRadius + 3) {
                    continue;
                }
                level.setBlock(base.offset(dx, -1, dz),
                        ((dx + dz) & 3) == 0
                                ? Blocks.CRYING_OBSIDIAN.defaultBlockState()
                                : Blocks.OBSIDIAN.defaultBlockState(), 3);
                for (int y = 0; y <= 5; y++) {
                    level.setBlock(base.offset(dx, y, dz), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }

    private static List<BlockPos> placeLayer(ServerLevel level, BlockPos base,
                                             boolean safePlatform, int layer) {
        List<BlockPos> placed = new ArrayList<>();
        if (layer == 0) {
            if (!safePlatform) {
                int platformRadius = 3;
                for (int dx = -platformRadius; dx <= platformRadius; dx++) {
                    for (int dz = -platformRadius; dz <= platformRadius; dz++) {
                        if (dx * dx + dz * dz <= platformRadius * platformRadius + 3) {
                            placeBlock(level, base.offset(dx, -1, dz),
                                    ((dx + dz) & 3) == 0
                                            ? Blocks.CRYING_OBSIDIAN.defaultBlockState()
                                            : Blocks.OBSIDIAN.defaultBlockState(), placed);
                        }
                    }
                }
            }
        } else if (layer == 1) {
            placeBlock(level, base, Blocks.CRYING_OBSIDIAN.defaultBlockState(), placed);
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) == 2) {
                        placeBlock(level, base.offset(dx, 0, dz),
                                Blocks.POLISHED_BLACKSTONE.defaultBlockState(), placed);
                    }
                }
            }
        } else if (layer == 2) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) == 1) {
                        placeBlock(level, base.offset(dx, 0, dz), Blocks.OBSIDIAN.defaultBlockState(), placed);
                    }
                }
            }
            placeBlock(level, base.above(), Blocks.OBSIDIAN.defaultBlockState(), placed);
        } else if (layer == 3) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) == 2) {
                        placeBlock(level, base.offset(dx, 1, dz), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), placed);
                    }
                }
            }
        } else if (layer == 4) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) == 1) {
                        placeBlock(level, base.offset(dx, 1, dz), Blocks.GILDED_BLACKSTONE.defaultBlockState(), placed);
                    }
                }
            }
            placeBlock(level, base.above(2), Blocks.GILDED_BLACKSTONE.defaultBlockState(), placed);
        } else if (layer == 5) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) == 2) {
                        placeBlock(level, base.offset(dx, 2, dz), Blocks.OBSIDIAN.defaultBlockState(), placed);
                    }
                }
            }
        } else if (layer == 6) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) == 1) {
                        placeBlock(level, base.offset(dx, 2, dz), Blocks.CRYING_OBSIDIAN.defaultBlockState(), placed);
                    }
                }
            }
        } else if (layer == 7) {
            placeBlock(level, base.above(3), Blocks.CRYING_OBSIDIAN.defaultBlockState(), placed);
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) == 2) {
                        placeBlock(level, base.offset(dx, 3, dz), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), placed);
                    }
                }
            }
        } else if (layer == 8) {
            placeBlock(level, base.above(4), Blocks.LODESTONE.defaultBlockState(), placed);
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) == 1) {
                        placeBlock(level, base.offset(dx, 4, dz), Blocks.GILDED_BLACKSTONE.defaultBlockState(), placed);
                    }
                }
            }
        } else if (layer == 9) {
            placeBlock(level, base.above(5), Blocks.END_ROD.defaultBlockState(), placed);
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) == 2) {
                        placeBlock(level, base.offset(dx, 5, dz), Blocks.CRYING_OBSIDIAN.defaultBlockState(), placed);
                    }
                }
            }
        }
        emitLayerEffects(level, base, placed, layer == 9);
        return placed;
    }

    private static void placeBlock(ServerLevel level, BlockPos pos, BlockState state,
                                   List<BlockPos> placed) {
        if (!level.getBlockState(pos).equals(state)) {
            level.setBlock(pos, state, 3);
            placed.add(pos.immutable());
        }
    }

    private static void emitLayerEffects(ServerLevel level, BlockPos base,
                                         List<BlockPos> placed, boolean completed) {
        Set<BlockState> soundedStates = new HashSet<>();
        int particlePositions = 0;
        for (BlockPos pos : placed) {
            BlockState state = level.getBlockState(pos);
            if (soundedStates.add(state)) {
                SoundType sound = state.getSoundType();
                level.playSound(null, pos, sound.getPlaceSound(), SoundSource.BLOCKS,
                        (sound.getVolume() + 1.0F) * 0.55F, sound.getPitch() * 0.8F);
            }
            if (particlePositions++ < 28) {
                level.sendParticles(ParticleTypes.FLAME,
                        pos.getX() + 0.5D, pos.getY() + 0.75D, pos.getZ() + 0.5D,
                        4, 0.32D, 0.35D, 0.32D, 0.025D);
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        pos.getX() + 0.5D, pos.getY() + 0.65D, pos.getZ() + 0.5D,
                        2, 0.25D, 0.25D, 0.25D, 0.018D);
                level.sendParticles(ParticleTypes.LARGE_SMOKE,
                        pos.getX() + 0.5D, pos.getY() + 0.55D, pos.getZ() + 0.5D,
                        1, 0.18D, 0.12D, 0.18D, 0.01D);
            }
        }
        if (completed) {
            level.sendParticles(ParticleTypes.FLAME,
                    base.getX() + 0.5D, base.getY() + 3.0D, base.getZ() + 0.5D,
                    64, 1.8D, 3.2D, 1.8D, 0.06D);
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    base.getX() + 0.5D, base.getY() + 3.0D, base.getZ() + 0.5D,
                    36, 1.25D, 2.5D, 1.25D, 0.04D);
        }
    }

    private static final class ObeliskBuild {
        private final BlockPos base;
        private final boolean safePlatform;
        private int layer;
        private int delay;

        private ObeliskBuild(BlockPos base, boolean safePlatform) {
            this.base = base;
            this.safePlatform = safePlatform;
            this.layer = safePlatform ? 1 : 0;
        }

        private boolean tick(ServerLevel level) {
            if (this.delay-- > 0) {
                return false;
            }
            placeLayer(level, this.base, this.safePlatform, this.layer++);
            this.delay = LAYER_INTERVAL_TICKS;
            return this.layer > 9;
        }
    }
}
