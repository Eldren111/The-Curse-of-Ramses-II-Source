package com.eldren.wutao.ramesses_curse;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class InscriptionBagMenu extends AbstractContainerMenu {
    public static final int BAG_SIZE = 16;
    private static final int BAG_COLUMNS = 4;
    private static final int BAG_X = 53;
    private static final int BAG_Y = 18;
    private static final int PLAYER_INVENTORY_Y = 104;

    private final ItemStack bagStack;
    private final InteractionHand hand;

    public static InscriptionBagMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        InteractionHand hand = buffer.readEnum(InteractionHand.class);
        return new InscriptionBagMenu(containerId, inventory, inventory.player.getItemInHand(hand), hand);
    }

    public InscriptionBagMenu(int containerId, Inventory inventory, ItemStack bagStack, InteractionHand hand) {
        super(ModMenus.INSCRIPTION_BAG.get(), containerId);
        this.bagStack = bagStack;
        this.hand = hand;

        IItemHandler handler = bagStack.getCapability(ForgeCapabilities.ITEM_HANDLER)
                .orElseGet(() -> new ItemStackHandler(BAG_SIZE));
        for (int slot = 0; slot < BAG_SIZE; slot++) {
            int column = slot % BAG_COLUMNS;
            int row = slot / BAG_COLUMNS;
            addSlot(new SlotItemHandler(handler, slot, BAG_X + column * 18, BAG_Y + row * 18));
        }

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
        if (index < BAG_SIZE) {
            if (!moveItemStackTo(source, BAG_SIZE, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!(source.getItem() instanceof InscriptionFragmentItem)
                    || !moveItemStackTo(source, 0, BAG_SIZE, false)) {
                return ItemStack.EMPTY;
            }
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
        return !bagStack.isEmpty()
                && bagStack.is(ModItems.INSCRIPTION_BAG.get())
                && player.getItemInHand(hand) == bagStack;
    }
}
