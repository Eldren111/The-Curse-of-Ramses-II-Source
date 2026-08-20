package com.eldren.wutao.ramesses_curse;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import org.joml.Vector3f;

public final class DuatPortalBlock extends NetherPortalBlock {
    private static final MapCodec<NetherPortalBlock> DUAT_CODEC = simpleCodec(DuatPortalBlock::new);
    private static final String PORTAL_TIME = "RamessesDuatPortalTime";
    private static final String LAST_PORTAL_TICK = "RamessesDuatPortalLastTick";
    private static final DustParticleOptions PORTAL_DUST =
            new DustParticleOptions(new Vector3f(0.78F, 0.52F, 0.08F), 1.1F);

    public DuatPortalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<NetherPortalBlock> codec() {
        return DUAT_CODEC;
    }

    @Override
    public BlockState updateShape(BlockState state, net.minecraft.core.Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return DuatPortalShape.find(level, pos).filter(DuatPortalShape::isComplete).isPresent()
                ? state
                : Blocks.AIR.defaultBlockState();
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
    }

    @Override
    public DimensionTransition getPortalDestination(ServerLevel level, Entity entity, BlockPos pos) {
        ServerLevel destination = level.getServer().getLevel(
                ModDimensions.DUAT_DESERT.equals(level.dimension()) ? Level.OVERWORLD : ModDimensions.DUAT_DESERT);
        return destination == null ? null : DuatTeleporter.createTransition(entity, destination);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(100) == 0) {
            level.playLocalSound(
                    pos.getX() + 0.5D,
                    pos.getY() + 0.5D,
                    pos.getZ() + 0.5D,
                    SoundEvents.PORTAL_AMBIENT,
                    SoundSource.BLOCKS,
                    0.5F,
                    0.65F + random.nextFloat() * 0.2F,
                    false);
        }

        for (int i = 0; i < 4; i++) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            double speedX = (random.nextDouble() - 0.5D) * 0.8D;
            double speedY = (random.nextDouble() - 0.5D) * 0.3D;
            double speedZ = (random.nextDouble() - 0.5D) * 0.8D;
            level.addParticle(PORTAL_DUST, x, y, z, speedX, speedY, speedZ);
        }
    }
}
