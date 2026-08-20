package com.eldren.wutao.ramesses_curse;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AffixKnowledgeBookItem extends Item {
    private final String affixId;

    public AffixKnowledgeBookItem(String affixId) {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
        this.affixId = affixId;
    }

    public String getAffixId() {
        return affixId;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.ramesses_curse.affix_knowledge.book")
                .withStyle(ChatFormatting.WHITE);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        boolean learned = AffixKnowledge.learn(serverPlayer, affixId);
        if (learned) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.ramesses_curse.affix_knowledge.learned",
                                    Component.translatable("affix.ramesses_curse." + affixId))
                            .withStyle(ChatFormatting.GOLD), false);
        } else {
            serverPlayer.giveExperienceLevels(5);
            serverPlayer.displayClientMessage(
                    Component.translatable("message.ramesses_curse.affix_knowledge.duplicate",
                                    Component.translatable("affix.ramesses_curse." + affixId), 5)
                            .withStyle(ChatFormatting.GREEN), false);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.8F, 1.0F);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ramesses_curse.affix_knowledge.affix",
                        Component.translatable("affix.ramesses_curse." + affixId))
                .withStyle(ChatFormatting.GOLD));
        if (ClientAffixKnowledge.hasLearned(affixId)) {
            tooltip.add(Component.translatable("item.ramesses_curse.affix_knowledge.learned")
                    .withStyle(ChatFormatting.GREEN));
        }
        if (!Screen.hasShiftDown()) {
            tooltip.add(Component.translatable(
                            "item.ramesses_curse.affix_knowledge.hold_shift",
                            Component.literal("Shift").withStyle(ChatFormatting.GOLD))
                    .withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        tooltip.add(Component.translatable("item.ramesses_curse.affix_knowledge.description." + affixId)
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.ramesses_curse.affix_knowledge.values." + affixId)
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.ramesses_curse.affix_knowledge.immunity")
                .withStyle(ChatFormatting.DARK_GREEN));
    }
}

