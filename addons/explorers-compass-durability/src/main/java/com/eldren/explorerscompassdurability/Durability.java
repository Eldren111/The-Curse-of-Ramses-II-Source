package com.eldren.explorerscompassdurability;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class Durability {
    private Durability() {}

    public static void consume(Player player, ItemStack stack) {
        if (player.level().isClientSide || player.getAbilities().instabuild || stack.isEmpty()
                || !stack.isDamageableItem() || DurabilityConfig.damagePerUse() == 0) {
            return;
        }
        int damage = Wear.afterUse(stack.getDamageValue(), DurabilityConfig.damagePerUse(), stack.getMaxDamage());
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(serverPlayer, stack, damage);
        }
        // A search always costs exactly the configured amount, including with Unbreaking.
        stack.setDamageValue(damage);
        if (damage >= stack.getMaxDamage()) {
            InteractionHand hand = player.getOffhandItem() == stack ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            player.broadcastBreakEvent(hand);
            Item item = stack.getItem();
            stack.shrink(1);
            player.awardStat(Stats.ITEM_BROKEN.get(item));
            stack.setDamageValue(0);
        }
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
    }
}
