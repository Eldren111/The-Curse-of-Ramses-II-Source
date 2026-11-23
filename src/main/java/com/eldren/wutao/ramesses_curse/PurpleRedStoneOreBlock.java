package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedStoneOreBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3f;

/** Redstone-like ore whose manual activation emits the mod's purple spark. */
public final class PurpleRedStoneOreBlock extends RedStoneOreBlock {
    private static final DustParticleOptions PURPLE_DUST =
            new DustParticleOptions(new Vector3f(0.58F, 0.08F, 0.95F), 1.0F);

    public PurpleRedStoneOreBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!state.getValue(LIT)) {
            level.setBlock(pos, state.setValue(LIT, true), 3);
            level.scheduleTick(pos, this, 30);
        }
        RandomSource random = level.random;
        if (level instanceof ServerLevel server) {
            server.sendParticles(PURPLE_DUST, pos.getX() + 0.5D, pos.getY() + 0.5D,
                    pos.getZ() + 0.5D, 16, 0.32D, 0.32D, 0.32D, 0.04D);
        } else {
            for (int i = 0; i < 16; i++) {
                level.addParticle(PURPLE_DUST,
                        pos.getX() + 0.2D + random.nextDouble() * 0.6D,
                        pos.getY() + 0.2D + random.nextDouble() * 0.6D,
                        pos.getZ() + 0.2D + random.nextDouble() * 0.6D,
                        (random.nextDouble() - 0.5D) * 0.08D,
                        0.03D + random.nextDouble() * 0.08D,
                        (random.nextDouble() - 0.5D) * 0.08D);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
