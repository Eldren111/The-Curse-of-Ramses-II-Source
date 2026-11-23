package com.eldren.wutao.ramesses_curse;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID)
public final class GrandCathedralEntryEvents {
    private static final int WARNING_RADIUS = 16;
    private static final int WARNING_INTERVAL_TICKS = 5;
    private static final BlockParticleOption BARRIER_PARTICLE = new BlockParticleOption(
            ParticleTypes.BLOCK_MARKER,
            Blocks.BARRIER.defaultBlockState());

    private GrandCathedralEntryEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
                || !(player.level() instanceof ServerLevel level)
                || !isCathedralDimension(level)) {
            return;
        }

        BoundingBox bounds = cathedralBounds(level);
        boolean hasSeal = ModEvents.isSealEquippedForPalace(player);
        if (!hasSeal && bounds.isInside(player.blockPosition())) {
            ejectPlayer(level, player, bounds);
        }

        if (!hasSeal
                && player.tickCount % WARNING_INTERVAL_TICKS == 0
                && isNearBoundary(player.blockPosition(), bounds)) {
            spawnBarrierWarning(level, player, bounds);
        }
    }

    private static boolean isCathedralDimension(ServerLevel level) {
        return Level.NETHER.equals(level.dimension()) || ModDimensions.RAMSES_VOID.equals(level.dimension());
    }

    private static BoundingBox cathedralBounds(ServerLevel level) {
        int minY = Level.NETHER.equals(level.dimension()) ? GrandCathedralStructure.NETHER_FOUNDATION_Y : 0;
        int maxY = Math.min(
                minY + GrandCathedralStructure.SIZE_Y - 1,
                level.getMaxBuildHeight() - 1);
        return new BoundingBox(
                GrandCathedralWorldgenStructure.CENTER_X,
                minY,
                GrandCathedralWorldgenStructure.CENTER_Z,
                GrandCathedralWorldgenStructure.CENTER_X + GrandCathedralStructure.SIZE_X - 1,
                maxY,
                GrandCathedralWorldgenStructure.CENTER_Z + GrandCathedralStructure.SIZE_Z - 1);
    }

    private static boolean isNearBoundary(BlockPos pos, BoundingBox bounds) {
        return pos.getX() >= bounds.minX() - WARNING_RADIUS
                && pos.getX() <= bounds.maxX() + WARNING_RADIUS
                && pos.getY() >= bounds.minY() - 8
                && pos.getY() <= bounds.maxY() + 8
                && pos.getZ() >= bounds.minZ() - WARNING_RADIUS
                && pos.getZ() <= bounds.maxZ() + WARNING_RADIUS;
    }

    private static void ejectPlayer(ServerLevel level, ServerPlayer player, BoundingBox bounds) {
        Vec3 rejectedPosition = player.position();
        BlockPos previousPos = BlockPos.containing(player.xOld, player.yOld, player.zOld);
        Vec3 destination = bounds.isInside(previousPos)
                ? nearestHorizontalExit(player.position(), bounds)
                : new Vec3(player.xOld, player.yOld, player.zOld);

        player.teleportTo(destination.x, destination.y, destination.z);
        Vec3 outward = destination.subtract(Vec3.atCenterOf(bounds.getCenter())).multiply(1.0D, 0.0D, 1.0D);
        if (outward.lengthSqr() < 1.0E-4D) {
            outward = new Vec3(0.0D, 0.0D, 1.0D);
        }
        outward = outward.normalize().scale(0.85D);
        player.setDeltaMovement(outward.x, 0.18D, outward.z);
        player.hurtMarked = true;
        player.displayClientMessage(
                Component.translatable("message.ramesses_curse.grand_cathedral.requires_seal")
                        .withStyle(ChatFormatting.RED),
                true);
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
