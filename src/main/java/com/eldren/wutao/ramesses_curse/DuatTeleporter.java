package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import java.util.Optional;
import java.util.function.Function;

final class DuatTeleporter implements ITeleporter {
    static final DuatTeleporter INSTANCE = new DuatTeleporter();

    private static final int SEARCH_RADIUS = 16;

    private DuatTeleporter() {
    }

    @Override
    public PortalInfo getPortalInfo(Entity entity, ServerLevel destination,
                                    Function<ServerLevel, PortalInfo> defaultPortalInfo) {
        BlockPos target = BlockPos.containing(entity.getX(), entity.getY(), entity.getZ());
        BlockPos portal = findNearbyPortal(destination, target).orElseGet(() -> createPortal(destination, target));
        BlockPos arrival = DuatPortalShape.find(destination, portal)
                .map(DuatPortalShape::arrivalBlock)
                .orElse(portal);
        Vec3 position = new Vec3(arrival.getX() + 0.5D, arrival.getY() + 0.1D, arrival.getZ() + 0.5D);
        return new PortalInfo(position, Vec3.ZERO, entity.getYRot(), entity.getXRot());
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destination,
                              float yaw, Function<Boolean, Entity> repositionEntity) {
        return repositionEntity.apply(false);
    }

    private static Optional<BlockPos> findNearbyPortal(ServerLevel level, BlockPos target) {
        for (int radius = 0; radius <= SEARCH_RADIUS; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (radius > 0 && Math.abs(x) != radius && Math.abs(z) != radius) {
                        continue;
                    }
                    int worldX = target.getX() + x;
                    int worldZ = target.getZ() + z;
                    int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, worldX, worldZ);
                    int minY = Math.max(level.getMinBuildHeight(), surfaceY - 20);
                    int maxY = Math.min(level.getMaxBuildHeight() - 1, surfaceY + 20);
                    for (int y = minY; y <= maxY; y++) {
                        BlockPos pos = new BlockPos(worldX, y, worldZ);
                        if (level.getBlockState(pos).is(ModBlocks.DUAT_PORTAL.get())) {
                            return Optional.of(pos);
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    private static BlockPos createPortal(ServerLevel level, BlockPos target) {
        int x = target.getX();
        int z = target.getZ();
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        int bottomY = Math.max(level.getMinBuildHeight() + 2,
                Math.min(surfaceY, level.getMaxBuildHeight() - 6));
        BlockPos innerBottomLeft = new BlockPos(x, bottomY, z);

        for (int frameX = -1; frameX <= 2; frameX++) {
            for (int frameY = -1; frameY <= 3; frameY++) {
                BlockPos framePos = innerBottomLeft.offset(frameX, frameY, 0);
                boolean frame = frameX == -1 || frameX == 2 || frameY == -1 || frameY == 3;
                level.setBlock(framePos, frame ? Blocks.SANDSTONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);
                if (!frame) {
                    level.setBlock(framePos.north(), Blocks.AIR.defaultBlockState(), 3);
                    level.setBlock(framePos.south(), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        for (int portalY = 0; portalY < 3; portalY++) {
            for (int portalX = 0; portalX < 2; portalX++) {
                level.setBlock(
                        innerBottomLeft.offset(portalX, portalY, 0),
                        ModBlocks.DUAT_PORTAL.get().defaultBlockState()
                                .setValue(NetherPortalBlock.AXIS, Direction.Axis.X),
                        18);
            }
        }
        return innerBottomLeft;
    }
}
