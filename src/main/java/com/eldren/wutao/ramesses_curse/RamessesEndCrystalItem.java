package com.eldren.wutao.ramesses_curse;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EndCrystalItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

public final class RamessesEndCrystalItem extends EndCrystalItem {
    public RamessesEndCrystalItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos basePos = context.getClickedPos();
        if (!level.getBlockState(basePos).is(Blocks.OBSIDIAN)
                && !level.getBlockState(basePos).is(Blocks.BEDROCK)) {
            return InteractionResult.FAIL;
        }

        BlockPos crystalPos = basePos.above();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if ((player != null && !player.mayUseItemAt(crystalPos, context.getClickedFace(), stack))
                || !level.isEmptyBlock(crystalPos)) {
            return InteractionResult.FAIL;
        }

        AABB placementBounds = new AABB(
                crystalPos.getX(), crystalPos.getY(), crystalPos.getZ(),
                crystalPos.getX() + 1.0D, crystalPos.getY() + 2.0D, crystalPos.getZ() + 1.0D);
        if (!level.getEntities((Entity) null, placementBounds).isEmpty()) {
            return InteractionResult.FAIL;
        }

        if (level instanceof ServerLevel serverLevel) {
            RamessesEndCrystalEntity crystal = ModEntities.RAMESSES_END_CRYSTAL.get().create(serverLevel);
            if (crystal == null) {
                return InteractionResult.FAIL;
            }
            crystal.setPos(crystalPos.getX() + 0.5D, crystalPos.getY(), crystalPos.getZ() + 0.5D);
            crystal.setShowBottom(false);
            if (!serverLevel.addFreshEntity(crystal)) {
                return InteractionResult.FAIL;
            }
            serverLevel.gameEvent(player, GameEvent.ENTITY_PLACE, crystal.position());
            if (player instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.SUMMONED_ENTITY.trigger(serverPlayer, crystal);
                DuatSunEaterRespawnRitual.tryStartAfterPlacement(serverLevel, serverPlayer, basePos);
            }
        }

        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
