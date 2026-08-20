package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = RamessesCurseMod.MODID)
public final class DuatPalaceEntryEvents {
    private static final int AMBIENT_RADIUS = 24;
    private static final int AMBIENT_INTERVAL_TICKS = 5;
    private static final BlockParticleOption BARRIER_PARTICLE = new BlockParticleOption(
            ParticleTypes.BLOCK_MARKER,
            Blocks.BARRIER.defaultBlockState());

    private DuatPalaceEntryEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(player.level() instanceof ServerLevel level)) {
            return;
        }

        if (!ModDimensions.DUAT_DESERT.equals(level.dimension())) {
            syncGoldenRain(player, false);
            return;
        }

        StructureStart inside = ModStructures.getDuatPalaceAt(level, player.blockPosition());
        boolean hasSeal = ModEvents.isSealEquippedForPalace(player);
        if (inside.isValid() && !hasSeal) {
            ejectPlayer(level, player, inside.getBoundingBox());
        }

        if (player.tickCount % AMBIENT_INTERVAL_TICKS != 0) {
            return;
        }

        StructureStart nearby = inside.isValid()
                ? inside
                : ModStructures.findDuatPalaceNear(level, player.blockPosition(), AMBIENT_RADIUS);
        syncGoldenRain(player, nearby.isValid());
        if (!nearby.isValid()) {
            return;
        }

        if (!hasSeal) {
            spawnBarrierWarning(level, player, nearby.getBoundingBox());
        }
    }

    private static void ejectPlayer(ServerLevel level, ServerPlayer player, BoundingBox palaceBounds) {
        Vec3 rejectedPosition = player.position();
        BlockPos previousPos = BlockPos.containing(player.xOld, player.yOld, player.zOld);
        Vec3 destination;

        if (!ModStructures.isInsideDuatPalace(level, previousPos)) {
            destination = new Vec3(player.xOld, player.yOld, player.zOld);
        } else {
            destination = nearestHorizontalExit(player.position(), palaceBounds);
        }

        player.teleportTo(destination.x, destination.y, destination.z);
        Vec3 center = Vec3.atCenterOf(palaceBounds.getCenter());
        Vec3 outward = destination.subtract(center).multiply(1.0D, 0.0D, 1.0D);
        if (outward.lengthSqr() < 1.0E-4D) {
            outward = new Vec3(0.0D, 0.0D, 1.0D);
        }
        outward = outward.normalize().scale(0.85D);
        player.setDeltaMovement(outward.x, 0.18D, outward.z);
        player.hurtMarked = true;

        level.sendParticles(
                player,
                BARRIER_PARTICLE,
                true,
                rejectedPosition.x,
                rejectedPosition.y + 1.0D,
                rejectedPosition.z,
                36,
                1.4D,
                1.8D,
                1.4D,
                0.0D);
    }

    private static Vec3 nearestHorizontalExit(Vec3 position, BoundingBox bounds) {
        double west = Math.abs(position.x - bounds.minX());
        double east = Math.abs(bounds.maxX() - position.x);
        double north = Math.abs(position.z - bounds.minZ());
        double south = Math.abs(bounds.maxZ() - position.z);
        double nearest = Math.min(Math.min(west, east), Math.min(north, south));

        double x = position.x;
        double z = position.z;
        if (nearest == west) {
            x = bounds.minX() - 1.25D;
        } else if (nearest == east) {
            x = bounds.maxX() + 2.25D;
        } else if (nearest == north) {
            z = bounds.minZ() - 1.25D;
        } else {
            z = bounds.maxZ() + 2.25D;
        }
        return new Vec3(x, position.y, z);
    }

    private static void syncGoldenRain(ServerPlayer player, boolean active) {
        if (player.tickCount % 10 == 0) {
            ModNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new DuatPalaceWeatherPacket(active));
        }
    }

    private static void spawnBarrierWarning(ServerLevel level, ServerPlayer player, BoundingBox bounds) {
        double x = Mth.clamp(player.getX(), bounds.minX(), bounds.maxX() + 1.0D);
        double z = Mth.clamp(player.getZ(), bounds.minZ(), bounds.maxZ() + 1.0D);
        level.sendParticles(
                player,
                BARRIER_PARTICLE,
                true,
                x,
                player.getY() + 1.0D,
                z,
                14,
                1.8D,
                2.2D,
                1.8D,
                0.0D);
    }

}

