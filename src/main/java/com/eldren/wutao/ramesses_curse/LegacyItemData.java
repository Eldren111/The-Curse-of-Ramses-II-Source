package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Bridges the mod's 1.20 item NBT fields to the 1.21 custom-data component. */
final class LegacyItemData {
    private LegacyItemData() {
    }

    static boolean has(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && !data.isEmpty();
    }

    static CompoundTag get(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.getUnsafe();
    }

    static CompoundTag getOrCreate(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            data = CustomData.of(new CompoundTag());
            stack.set(DataComponents.CUSTOM_DATA, data);
        }
        return data.getUnsafe();
    }
}
