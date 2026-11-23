package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

public final class VoidGatewayPortalBlock extends NetherPortalBlock {
    private static final String PORTAL_TIME = "RamessesVoidGatewayTime";
    private static final String LAST_PORTAL_TICK = "RamessesVoidGatewayLastTick";
    private static final VoxelShape HORIZONTAL_SHAPE = box(0.0D, 10.0D, 0.0D, 16.0D, 13.0D, 16.0D);
    private static final DustParticleOptions PORTAL_DUST =
            new DustParticleOptions(new Vector3f(0.92F, 0.05F, 0.32F), 1.15F);

    public VoidGatewayPortalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return HORIZONTAL_SHAPE;
    }

    @Override
    public BlockState updateShape(BlockState state, net.minecraft.core.Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!entity.canChangeDimensions() || entity.isPassenger()) {
            return;
        }
        if (level.isClientSide) {
            entity.handleInsidePortal(pos);
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        CompoundTag data = entity.getPersistentData();
        long gameTime = level.getGameTime();
        if (entity.isOnPortalCooldown()) {
            entity.setPortalCooldown();
            data.putInt(PORTAL_TIME, 0);
            data.putLong(LAST_PORTAL_TICK, gameTime);
            return;
        }
        if (data.getLong(LAST_PORTAL_TICK) == gameTime) {
            return;
        }
        if (data.getLong(LAST_PORTAL_TICK) + 1L != gameTime) {
            data.putInt(PORTAL_TIME, 0);
        }
        data.putLong(LAST_PORTAL_TICK, gameTime);

        int portalTime = data.getInt(PORTAL_TIME) + 1;
        data.putInt(PORTAL_TIME, portalTime);
        if (portalTime < Math.max(1, entity.getPortalWaitTime())) {
            return;
        }

        ServerLevel destination = serverLevel.getServer().getLevel(
                ModDimensions.RAMSES_VOID.equals(level.dimension()) ? Level.END : ModDimensions.RAMSES_VOID);
        if (destination == null) {
            return;
        }

        if (Level.END.equals(level.dimension())) {
            if (!(entity instanceof ServerPlayer player)) {
                return;
            }
            int remainingEntries = VoidGatewaySavedData.consumeEntry(serverLevel);
            if (remainingEntries < 0) {
                return;
            }
            player.displayClientMessage(Component.translatable(
                    "message.ramesses_curse.void_gateway.remaining", remainingEntries), true);
        }

        data.putInt(PORTAL_TIME, 0);
        entity.setPortalCooldown();
        entity.changeDimension(destination, VoidGatewayTeleporter.INSTANCE);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(90) == 0) {
            level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.55F,
                    0.72F + random.nextFloat() * 0.16F, false);
        }
        for (int i = 0; i < 3; i++) {
            level.addParticle(PORTAL_DUST,
                    pos.getX() + random.nextDouble(), pos.getY() + 0.72D, pos.getZ() + random.nextDouble(),
                    (random.nextDouble() - 0.5D) * 0.18D,
                    0.04D + random.nextDouble() * 0.12D,
                    (random.nextDouble() - 0.5D) * 0.18D);
        }
    }
}
