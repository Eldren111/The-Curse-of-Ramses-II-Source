package com.eldren.wutao.ramesses_curse;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public final class CompoundBowAmmoCapabilityProvider {
    private CompoundBowAmmoCapabilityProvider() {
    }

    public static IItemHandler createHandler(ItemStack bowStack) {
        return new ComponentItemHandler(
                bowStack,
                1,
                ModItems.EternalSilenceCompoundBow::isSupportedAmmo,
                false);
    }

    public static ItemStack getSelectedAmmo(ItemStack bowStack) {
        return createHandler(bowStack).getStackInSlot(0).copy();
    }
}
