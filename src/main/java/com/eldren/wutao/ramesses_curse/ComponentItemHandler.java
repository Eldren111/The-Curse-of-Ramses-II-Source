package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

final class ComponentItemHandler implements IItemHandlerModifiable {
    private final ItemStack owner;
    private final int slots;
    private final Predicate<ItemStack> validator;
    private final boolean uniqueItems;

    ComponentItemHandler(ItemStack owner, int slots, Predicate<ItemStack> validator, boolean uniqueItems) {
        this.owner = owner;
        this.slots = slots;
        this.validator = validator;
        this.uniqueItems = uniqueItems;
    }

    @Override public int getSlots() { return slots; }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        validateSlot(slot);
        ItemContainerContents contents = owner.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        return slot < contents.getSlots() ? contents.getStackInSlot(slot) : ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        validateSlot(slot);
        if (stack.isEmpty() || !isItemValid(slot, stack) || !getStackInSlot(slot).isEmpty()) return stack;
        if (uniqueItems) {
            for (int index = 0; index < slots; index++) {
                if (index != slot && ItemStack.isSameItemSameComponents(getStackInSlot(index), stack)) return stack;
            }
        }
        if (!simulate) setStackInSlot(slot, stack.copyWithCount(1));
        return stack.getCount() == 1 ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - 1);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        validateSlot(slot);
        ItemStack stored = getStackInSlot(slot);
        if (amount <= 0 || stored.isEmpty()) return ItemStack.EMPTY;
        ItemStack extracted = stored.copyWithCount(Math.min(amount, stored.getCount()));
        if (!simulate) setStackInSlot(slot, stored.getCount() <= amount
                ? ItemStack.EMPTY : stored.copyWithCount(stored.getCount() - amount));
        return extracted;
    }

    @Override public int getSlotLimit(int slot) { validateSlot(slot); return 1; }
    @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { validateSlot(slot); return validator.test(stack); }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        validateSlot(slot);
        NonNullList<ItemStack> items = NonNullList.withSize(slots, ItemStack.EMPTY);
        owner.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(items);
        items.set(slot, stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
        owner.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
    }

    private void validateSlot(int slot) {
        if (slot < 0 || slot >= slots) throw new IndexOutOfBoundsException("Invalid slot " + slot);
    }
}
