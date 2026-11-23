package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.CurioRestoreContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.capability.CurioInventoryCapability;

@Mixin(value = CurioInventoryCapability.CurioInventoryWrapper.class, remap = false)
public abstract class CuriosRestoreValidationMixin {
    // readTag builds a separate slot map and publishes it only after all slots validate.
    // Its validation must not depend on the wearer's still-empty (or stale) live map.
    @Redirect(method = {"lambda$readTag$9", "lambda$readTag$12"}, at = @At(value = "INVOKE",
            target = "Ltop/theillusivec4/curios/api/type/inventory/IDynamicStackHandler;isItemValid(ILnet/minecraft/world/item/ItemStack;)Z"),
            require = 2, expect = 2, remap = false)
    private boolean ramessesCurse$validateSavedSlot(IDynamicStackHandler handler, int slot,
                                                   ItemStack stack) {
        return CurioRestoreContext.validate(((ICuriosItemHandler) (Object) this).getWearer(),
                handler, slot, stack);
    }
}
