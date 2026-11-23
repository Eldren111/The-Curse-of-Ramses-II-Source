package com.eldren.wutao.ramesses_curse;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID)
public final class DuatSunEaterRespawnRitual {
    private static final int CRYSTAL_LINK_TICKS = 60;
    private static final int CENTER_CHARGE_TICKS = 10 * 20;
    private static final int RITUAL_TICKET_RADIUS = 8;
    private static final double PLAYER_RANGE = 192.0D;
    private static final BlockPos FIGHT_CENTER = new BlockPos(
            RamessesEndIslandStructure.CENTER_X,
            RamessesEndIslandPiece.CENTER_Y,
            RamessesEndIslandStructure.CENTER_Z);
    private static final Map<ServerLevel, IslandRitual> ACTIVE_RITUALS = new WeakHashMap<>();

    private DuatSunEaterRespawnRitual() {
    }

    public static void tryStartAfterPlacement(
            ServerLevel level, ServerPlayer player, BlockPos crystalBase) {
        if (isTowerCrystalBase(level, crystalBase)) {
            tryStart(level, player, true);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.level instanceof ServerLevel level)
                || !Level.END.equals(level.dimension())) {
            return;
        }
        IslandRitual active = ACTIVE_RITUALS.get(level);
        if (active != null) {
            if (active.tick(level)) {
                ACTIVE_RITUALS.remove(level);
            }
            return;
        }
        if (level.getGameTime() % 20L != 0L
                || DuatSunEaterDeathBlastManager.isBusy(level)) {
            return;
        }
        ServerPlayer wearer = findNearbyCrownWearer(level);
        if (wearer == null
                || RamessesEndIslandPiece.hasActiveDragonMarker(level, FIGHT_CENTER)
                || hasExistingSunEater(level)) {
            return;
        }
        List<RamessesEndCrystalEntity> crystals = findTowerCrystals(level);
        if (crystals.size() != RamessesEndIslandPiece.ritualCrystalPositions(level, FIGHT_CENTER).size()) {
            return;
        }
        tryStart(level, wearer, false);
    }

    private static boolean tryStart(
            ServerLevel level, ServerPlayer player, boolean notifyFailure) {
        if (!Level.END.equals(level.dimension()) || ACTIVE_RITUALS.containsKey(level)) {
            return false;
        }
        if (DuatSunEaterDeathBlastManager.isBusy(level)) {
            if (notifyFailure) {
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                                "message.ramesses_curse.sun_eater_respawn_wait_for_blast")
                        .withStyle(ChatFormatting.RED), true);
            }
            return false;
        }
        if (RamessesEndIslandPiece.hasActiveDragonMarker(level, FIGHT_CENTER)
                || hasExistingSunEater(level)) {
            if (notifyFailure) {
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                                "message.ramesses_curse.sun_eater_respawn_already_active")
                        .withStyle(ChatFormatting.RED), true);
            }
            return false;
        }
        List<RamessesEndCrystalEntity> crystals = findTowerCrystals(level);
        int required = RamessesEndIslandPiece.ritualCrystalPositions(level, FIGHT_CENTER).size();
        if (crystals.size() != required) {
            return false;
        }
        if (!DuatSunEaterCrownItem.hasEquippedEmpoweredCrown(player)) {
            if (notifyFailure) {
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                                "message.ramesses_curse.sun_eater_respawn_requires_crown")
                        .withStyle(ChatFormatting.DARK_PURPLE), true);
            }
            return false;
        }

        for (int index = 0; index < crystals.size(); index++) {
            RamessesEndCrystalEntity crystal = crystals.get(index);
            RamessesEndCrystalEntity next = crystals.get((index + 1) % crystals.size());
            crystal.beginRespawnRitual(next.blockPosition());
        }
        RamessesEndIslandPiece.markRespawnInProgress(level, FIGHT_CENTER);
        level.getChunkSource().addRegionTicket(
                TicketType.PORTAL, new ChunkPos(FIGHT_CENTER), RITUAL_TICKET_RADIUS, FIGHT_CENTER);
        ACTIVE_RITUALS.put(level, new IslandRitual(crystals));
        level.playSound(null, FIGHT_CENTER, SoundEvents.BEACON_ACTIVATE,
                SoundSource.HOSTILE, 5.0F, 0.46F);
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                        "message.ramesses_curse.sun_eater_respawn_started")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), true);
        return true;
    }

    private static boolean isTowerCrystalBase(ServerLevel level, BlockPos base) {
        for (BlockPos crystalPos : RamessesEndIslandPiece.ritualCrystalPositions(level, FIGHT_CENTER)) {
            if (crystalPos.below().equals(base)) {
                return true;
            }
        }
        return false;
    }

    private static List<RamessesEndCrystalEntity> findTowerCrystals(ServerLevel level) {
        List<RamessesEndCrystalEntity> result = new ArrayList<>();
        for (BlockPos expected : RamessesEndIslandPiece.ritualCrystalPositions(level, FIGHT_CENTER)) {
            level.getChunk(expected.getX() >> 4, expected.getZ() >> 4);
            RamessesEndCrystalEntity crystal = level.getEntitiesOfClass(
                            RamessesEndCrystalEntity.class,
                            new AABB(expected).inflate(1.25D), Entity::isAlive)
                    .stream()
                    .filter(candidate -> !result.contains(candidate))
                    .findFirst()
                    .orElse(null);
            if (crystal == null) {
                return List.of();
            }
            result.add(crystal);
        }
        return result;
    }

    private static ServerPlayer findNearbyCrownWearer(ServerLevel level) {
        return level.players().stream()
                .filter(player -> player.isAlive() && !player.isSpectator())
                .filter(player -> player.distanceToSqr(
                        FIGHT_CENTER.getX() + 0.5D,
                        FIGHT_CENTER.getY() + 1.0D,
                        FIGHT_CENTER.getZ() + 0.5D) <= PLAYER_RANGE * PLAYER_RANGE)
                .filter(DuatSunEaterCrownItem::hasEquippedEmpoweredCrown)
                .findFirst()
                .orElse(null);
    }

    private static boolean hasExistingSunEater(ServerLevel level) {
        AABB island = new AABB(
                FIGHT_CENTER.getX() - 320.0D, level.getMinBuildHeight(), FIGHT_CENTER.getZ() - 320.0D,
                FIGHT_CENTER.getX() + 320.0D, level.getMaxBuildHeight(), FIGHT_CENTER.getZ() + 320.0D);
        return !level.getEntitiesOfClass(
                DuatSunEaterEntity.class, island, Entity::isAlive).isEmpty();
    }

    private static void spawnLinkEffects(ServerLevel level, List<RamessesEndCrystalEntity> crystals) {
        for (RamessesEndCrystalEntity crystal : crystals) {
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    crystal.getX(), crystal.getY() + 0.8D, crystal.getZ(),
                    3, 0.28D, 0.35D, 0.28D, 0.08D);
        }
    }

    private static void tickCenterCharge(ServerLevel level, int tick) {
        BlockPos sky = FIGHT_CENTER.above(48);
        double progress = Math.min(1.0D, tick / (double) CENTER_CHARGE_TICKS);
        if ((tick & 1) == 0) {
            level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    sky.getX() + 0.5D, sky.getY() + 0.5D, sky.getZ() + 0.5D,
                    10 + (int) (progress * 14.0D),
                    3.0D + progress * 4.0D, 2.2D + progress * 2.0D,
                    3.0D + progress * 4.0D, 0.11D + progress * 0.1D);
            level.sendParticles(ParticleTypes.DRAGON_BREATH,
                    sky.getX() + 0.5D, sky.getY() + 0.5D, sky.getZ() + 0.5D,
                    5 + (int) (progress * 7.0D),
                    2.0D + progress * 2.0D, 1.8D,
                    2.0D + progress * 2.0D, 0.025D);
        }
        if (tick % 20 == 0) {
            level.playSound(null, sky, SoundEvents.RESPAWN_ANCHOR_CHARGE,
                    SoundSource.HOSTILE, 4.5F, (float) (0.42D + progress * 0.5D));
        }
    }

    private static boolean summonDragon(
            ServerLevel level, List<RamessesEndCrystalEntity> crystals) {
        DuatSunEaterEntity dragon = ModEntities.DUAT_SUN_EATER.get().create(level);
        if (dragon == null) {
            return false;
        }
        BlockPos sky = FIGHT_CENTER.above(48);
        for (RamessesEndCrystalEntity crystal : crystals) {
            crystal.finishRespawnRitualForCombat();
        }
        RamessesEndIslandPiece.restorePhaseCrystals(level, FIGHT_CENTER, 1);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                sky.getX() + 0.5D, sky.getY() + 0.5D, sky.getZ() + 0.5D,
                3, 2.4D, 2.0D, 2.4D, 0.0D);
        level.sendParticles(ParticleTypes.FLASH,
                sky.getX() + 0.5D, sky.getY() + 0.5D, sky.getZ() + 0.5D,
                2, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.END_ROD,
                sky.getX() + 0.5D, sky.getY() + 0.5D, sky.getZ() + 0.5D,
                150, 6.0D, 4.0D, 6.0D, 0.28D);
        level.playSound(null, sky, SoundEvents.GENERIC_EXPLODE,
                SoundSource.HOSTILE, 8.0F, 0.46F);
        level.playSound(null, sky, SoundEvents.LIGHTNING_BOLT_THUNDER,
                SoundSource.HOSTILE, 7.0F, 0.52F);

        dragon.setFightOrigin(FIGHT_CENTER);
        dragon.setPos(sky.getX() + 0.5D, sky.getY(), sky.getZ() + 0.5D);
        dragon.setYRot(level.random.nextFloat() * 360.0F);
        dragon.getPhaseManager().setPhase(
                net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase.HOLDING_PATTERN);
        if (!level.addFreshEntity(dragon)) {
            return false;
        }
        RamessesEndIslandPiece.markDragonActive(level, FIGHT_CENTER);
        RamessesEndIslandPiece.clearRespawnInProgress(level, FIGHT_CENTER);
        level.playSound(null, sky, SoundEvents.ENDER_DRAGON_GROWL,
                SoundSource.HOSTILE, 9.0F, 0.42F);
        return true;
    }

    private static void releaseTicket(ServerLevel level) {
        level.getChunkSource().removeRegionTicket(
                TicketType.PORTAL, new ChunkPos(FIGHT_CENTER), RITUAL_TICKET_RADIUS, FIGHT_CENTER);
    }

    private static final class IslandRitual {
        private final List<RamessesEndCrystalEntity> crystals;
        private final List<ChunkPos> floorRestoreChunks;
        private int restoredFloorChunks;
        private int elapsedTicks;

        private IslandRitual(List<RamessesEndCrystalEntity> crystals) {
            this.crystals = List.copyOf(crystals);
            this.floorRestoreChunks = RamessesEndIslandPiece.runtimeRebuildChunks(FIGHT_CENTER);
        }

        private boolean tick(ServerLevel level) {
            if (this.crystals.stream().anyMatch(crystal -> !crystal.isAlive())) {
                for (RamessesEndCrystalEntity crystal : this.crystals) {
                    if (crystal.isAlive()) {
                        crystal.finishRespawnRitualForCombat();
                    }
                }
                RamessesEndIslandPiece.clearRespawnInProgress(level, FIGHT_CENTER);
                releaseTicket(level);
                return true;
            }
            this.elapsedTicks++;
            if (this.elapsedTicks <= CRYSTAL_LINK_TICKS) {
                if (this.elapsedTicks % 4 == 0) {
                    spawnLinkEffects(level, this.crystals);
                }
                if (this.elapsedTicks % 20 == 0) {
                    level.playSound(null, FIGHT_CENTER, SoundEvents.BEACON_POWER_SELECT,
                            SoundSource.HOSTILE, 3.5F, 0.55F + this.elapsedTicks * 0.004F);
                }
                return false;
            }
            if (this.elapsedTicks == CRYSTAL_LINK_TICKS + 1) {
                BlockPos sky = FIGHT_CENTER.above(48);
                for (RamessesEndCrystalEntity crystal : this.crystals) {
                    crystal.setRespawnRitualBeamTarget(sky);
                }
                level.playSound(null, sky, SoundEvents.END_PORTAL_SPAWN,
                        SoundSource.HOSTILE, 6.0F, 0.5F);
            }
            int chargeTick = this.elapsedTicks - CRYSTAL_LINK_TICKS;
            this.restoreFloorThrough(level, chargeTick);
            tickCenterCharge(level, chargeTick);
            if (chargeTick < CENTER_CHARGE_TICKS) {
                return false;
            }
            boolean summoned = summonDragon(level, this.crystals);
            if (!summoned) {
                RamessesEndIslandPiece.clearRespawnInProgress(level, FIGHT_CENTER);
            }
            releaseTicket(level);
            return true;
        }

        private void restoreFloorThrough(ServerLevel level, int chargeTick) {
            int target = Math.min(this.floorRestoreChunks.size(),
                    (chargeTick * this.floorRestoreChunks.size() + CENTER_CHARGE_TICKS - 1)
                            / CENTER_CHARGE_TICKS);
            while (this.restoredFloorChunks < target) {
                RamessesEndIslandPiece.restoreRuntimeFloorChunk(level, FIGHT_CENTER,
                        this.floorRestoreChunks.get(this.restoredFloorChunks++));
            }
        }
    }

    // Allows worlds saved during the former central-podium resurrection to complete cleanly.
    static void tickCharging(ServerLevel level, BlockPos portalCenter, int tick, int duration) {
        double progress = Math.min(1.0D, tick / (double) duration);
        if ((tick & 1) == 0) {
            level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    portalCenter.getX() + 0.5D, portalCenter.getY() + 18.0D,
                    portalCenter.getZ() + 0.5D, 12,
                    3.0D + progress * 2.5D, 2.4D,
                    3.0D + progress * 2.5D, 0.12D);
        }
    }

    static void finishCharging(ServerLevel level, BlockPos portalCenter) {
        AABB nearby = new AABB(portalCenter).inflate(8.0D);
        for (RamessesEndCrystalEntity crystal : level.getEntitiesOfClass(
                RamessesEndCrystalEntity.class, nearby,
                RamessesEndCrystalEntity::isRespawnRitualCrystal)) {
            crystal.discard();
        }
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                portalCenter.getX() + 0.5D, portalCenter.getY() + 18.0D,
                portalCenter.getZ() + 0.5D, 2, 1.5D, 1.2D, 1.5D, 0.0D);
        level.playSound(null, portalCenter, SoundEvents.END_PORTAL_SPAWN,
                SoundSource.HOSTILE, 6.0F, 0.48F);
    }
}
