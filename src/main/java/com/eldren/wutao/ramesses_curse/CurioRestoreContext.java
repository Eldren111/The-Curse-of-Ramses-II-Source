package com.eldren.wutao.ramesses_curse;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

/** Marks only the validation of a saved slot, never a player's equip operation. */
public final class CurioRestoreContext {
    private static final ThreadLocal<LivingEntity> WEARER = new ThreadLocal<>();

    private CurioRestoreContext() {}

    public static boolean isRestoring(LivingEntity wearer) {
        return wearer != null && WEARER.get() == wearer;
    }

    public static boolean validate(LivingEntity wearer, IDynamicStackHandler handler,
                                   int slot, ItemStack stack) {
        LivingEntity previous = WEARER.get();
        WEARER.set(wearer);
        try {
            // Preserve Curios tags, slot validity, config switches and other mods' checks.
            return handler.isItemValid(slot, stack);
        } finally {
            if (previous == null) WEARER.remove();
            else WEARER.set(previous);
        }
    }
}
