package com.eldren.wutao.ramesses_curse;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CompoundBowAmmoCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    private static final String AMMO_TAG = "RamessesCompoundBowAmmo";

    private final ItemStack bowStack;
    private final AmmoHandler handler;
    private final LazyOptional<IItemHandler> optional;

    public CompoundBowAmmoCapabilityProvider(ItemStack bowStack, @Nullable CompoundTag legacyData) {
        this.bowStack = bowStack;
        this.handler = new AmmoHandler(bowStack);
        this.optional = LazyOptional.of(() -> handler);
        CompoundTag stored = bowStack.getTagElement(AMMO_TAG);
        if (stored != null) {
            handler.deserializeNBT(stored);
        } else if (legacyData != null && !legacyData.isEmpty()) {
            handler.deserializeNBT(legacyData);
        }
    }

    public static ItemStack getSelectedAmmo(ItemStack bowStack) {
        return bowStack.getCapability(ForgeCapabilities.ITEM_HANDLER)
                .map(handler -> handler.getStackInSlot(0).copy())
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(
            @NotNull Capability<T> capability,
            @Nullable net.minecraft.core.Direction side) {
        return capability == ForgeCapabilities.ITEM_HANDLER ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return handler.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        handler.deserializeNBT(nbt);
    }

    private static final class AmmoHandler extends ItemStackHandler {
        private final ItemStack owner;
        private boolean loading;

        private AmmoHandler(ItemStack owner) {
            super(1);
            this.owner = owner;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return ModItems.EternalSilenceCompoundBow.isSupportedAmmo(stack);
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            loading = true;
            try {
                super.deserializeNBT(nbt);
                ItemStack selected = getStackInSlot(0);
                if (!selected.isEmpty() && !isItemValid(0, selected)) {
                    setStackInSlot(0, ItemStack.EMPTY);
                } else if (selected.getCount() > 1) {
                    selected.setCount(1);
                }
            } finally {
                loading = false;
            }
            saveToOwner();
        }

        @Override
        protected void onContentsChanged(int slot) {
            if (!loading) {
                saveToOwner();
            }
        }

        private void saveToOwner() {
            owner.getOrCreateTag().put(AMMO_TAG, serializeNBT());
        }
    }
}
