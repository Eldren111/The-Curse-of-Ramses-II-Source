package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;

final class VoidGatewayTeleporter implements ITeleporter {
    static final VoidGatewayTeleporter INSTANCE = new VoidGatewayTeleporter();

    private static final int VOID_CENTER_X = GrandCathedralWorldgenStructure.CENTER_X
            + GrandCathedralStructure.SIZE_X / 2;
    private static final int VOID_CENTER_Z = GrandCathedralWorldgenStructure.CENTER_Z;
    private static final int VOID_FLOOR_Y = 0;

    private VoidGatewayTeleporter() {
    }

    @Override
    public PortalInfo getPortalInfo(Entity entity, ServerLevel destination,
                                    Function<ServerLevel, PortalInfo> defaultPortalInfo) {
        Vec3 arrival = ModDimensions.RAMSES_VOID.equals(destination.dimension())
                ? prepareVoidArrival(destination)
                : findEndArrival(destination);
        return new PortalInfo(arrival, Vec3.ZERO, entity.getYRot(), entity.getXRot());
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destination,
                              float yaw, Function<Boolean, Entity> repositionEntity) {
        return repositionEntity.apply(false);
    }

    private static Vec3 prepareVoidArrival(ServerLevel level) {
        BlockPos center = new BlockPos(VOID_CENTER_X, VOID_FLOOR_Y, VOID_CENTER_Z);
        level.getChunk(center);
        for (int x = -6; x <= 6; x++) {
            for (int z = -6; z <= 6; z++) {
                BlockPos floor = center.offset(x, 0, z);
                boolean border = Math.abs(x) == 6 || Math.abs(z) == 6;
                level.setBlock(floor, (border || (x + z) % 5 == 0)
                        ? Blocks.CRYING_OBSIDIAN.defaultBlockState()
                        : Blocks.OBSIDIAN.defaultBlockState(), 18);
                if (Math.abs(x) <= 3 && Math.abs(z) <= 3) {
                    level.setBlock(floor.above(), Blocks.AIR.defaultBlockState(), 18);
                    level.setBlock(floor.above(2), Blocks.AIR.defaultBlockState(), 18);
                }
            }
        }
        for (int z = 7; z <= 10; z++) {
            for (int x = -3; x <= 3; x++) {
                level.setBlock(center.offset(x, 0, z),
                        (Math.abs(x) == 3 ? Blocks.CRYING_OBSIDIAN : Blocks.OBSIDIAN)
                                .defaultBlockState(), 18);
            }
        }
        placePortal(level, center.above());
        placePortalGuard(level, center.above());
        return new Vec3(VOID_CENTER_X + 0.5D, VOID_FLOOR_Y + 1.1D, VOID_CENTER_Z - 4.5D);
    }

    private static Vec3 findEndArrival(ServerLevel level) {
        BlockPos portal = findCentralPortal(level);
        int arrivalX = portal.getX() + 6;
        int arrivalZ = portal.getZ();
        int arrivalY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, arrivalX, arrivalZ);
        return new Vec3(arrivalX + 0.5D, arrivalY + 0.1D, arrivalZ + 0.5D);
    }

    private static BlockPos findCentralPortal(ServerLevel level) {
        for (int y = 40; y <= 100; y++) {
            for (int x = -10; x <= 10; x++) {
                for (int z = -10; z <= 10; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (level.getBlockState(pos).is(ModBlocks.VOID_GATEWAY_PORTAL.get())) {
                        return pos;
                    }
                }
            }
        }
        return new BlockPos(0, 64, 0);
    }

    private static void placePortal(ServerLevel level, BlockPos center) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                level.setBlock(center.offset(x, 0, z),
                        ModBlocks.VOID_GATEWAY_PORTAL.get().defaultBlockState()
                                .setValue(NetherPortalBlock.AXIS, Direction.Axis.X), 3);
            }
        }
    }

    private static void placePortalGuard(ServerLevel level, BlockPos portalCenter) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) != 2 && Math.abs(z) != 2) {
                    continue;
                }
                boolean entrance = x == 0 && z == 2;
                BlockPos base = portalCenter.offset(x, 0, z);
                if (entrance) {
                    level.setBlock(base, Blocks.AIR.defaultBlockState(), 18);
                    level.setBlock(base.above(), Blocks.AIR.defaultBlockState(), 18);
                } else {
                    level.setBlock(base, Blocks.CRYING_OBSIDIAN.defaultBlockState(), 18);
                    level.setBlock(base.above(), Blocks.IRON_BARS.defaultBlockState(), 18);
                }
            }
        }
    }
}
