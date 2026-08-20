package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InscriptionFragmentItem extends Item {
    private final int fragmentIndex;

    public InscriptionFragmentItem(Properties properties, int fragmentIndex) {
        super(properties);
        this.fragmentIndex = fragmentIndex;
    }

    public int getFragmentIndex() {
        return fragmentIndex;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag) {
        tooltip.add(Component.translatable(
                        "item.ramesses_curse.inscription_fragment_" + fragmentIndex + ".effect")
                .withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("item.ramesses_curse.inscription_fragment.activation")
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Direction direction = context.getClickedFace();
        BlockPos placementPos = context.getClickedPos().relative(direction);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player != null && !player.mayUseItemAt(placementPos, direction, stack)) {
            return InteractionResult.FAIL;
        }

        Level level = context.getLevel();
        InscriptionFragmentEntity fragment = new InscriptionFragmentEntity(level, placementPos, direction);
        fragment.setInvisible(true);
        fragment.setItem(stack.copyWithCount(1), false);
        if (!fragment.survives()) {
            return InteractionResult.CONSUME;
        }

        if (!level.isClientSide) {
            fragment.playPlacementSound();
            level.gameEvent(player, GameEvent.ENTITY_PLACE, fragment.position());
            level.addFreshEntity(fragment);
        }
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
