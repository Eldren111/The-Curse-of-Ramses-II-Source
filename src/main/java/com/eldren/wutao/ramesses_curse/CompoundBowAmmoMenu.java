package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class CompoundBowAmmoMenu extends AbstractContainerMenu {
    private static final int AMMO_SLOT_X = 80;
    private static final int AMMO_SLOT_Y = 26;
    private static final int PLAYER_INVENTORY_Y = 66;

    private final ItemStack bowStack;
    private final InteractionHand hand;

    public static CompoundBowAmmoMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        InteractionHand hand = buffer.readEnum(InteractionHand.class);
        return new CompoundBowAmmoMenu(containerId, inventory, inventory.player.getItemInHand(hand), hand);
    }

    public CompoundBowAmmoMenu(int containerId, Inventory inventory, ItemStack bowStack, InteractionHand hand) {
        super(ModMenus.COMPOUND_BOW_AMMO.get(), containerId);
        this.bowStack = bowStack;
        this.hand = hand;

        IItemHandler handler = CompoundBowAmmoCapabilityProvider.createHandler(bowStack);
        addSlot(new SlotItemHandler(handler, 0, AMMO_SLOT_X, AMMO_SLOT_Y));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, PLAYER_INVENTORY_Y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            int inventorySlot = column;
            addSlot(new Slot(inventory, inventorySlot, 8 + column * 18, PLAYER_INVENTORY_Y + 58) {
                @Override
                public boolean mayPickup(Player player) {
                    return hand != InteractionHand.MAIN_HAND || inventory.selected != inventorySlot;
                }
            });
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot sourceSlot = slots.get(index);
        if (!sourceSlot.hasItem() || !sourceSlot.mayPickup(player)) {
            return ItemStack.EMPTY;
        }

        ItemStack source = sourceSlot.getItem();
        ItemStack original = source.copy();
        if (index == 0) {
            if (!moveItemStackTo(source, 1, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(source, 0, 1, false)) {
            return ItemStack.EMPTY;
        }

        if (source.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(player, source);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return ModItems.EternalSilenceCompoundBow.isCompoundBow(bowStack)
                && player.getItemInHand(hand) == bowStack;
    }
}

