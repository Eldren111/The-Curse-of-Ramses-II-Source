package com.eldren.wutao.ramesses_requiem.mixin;

import com.eldren.wutao.ramesses_requiem.ModEnchantments;
import com.eldren.wutao.ramesses_requiem.RequiemAddon;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ForgeHooks.class, remap = false)
public abstract class RequiemBookCreatorMixin {
    @Inject(method = "getDefaultCreatorModId", at = @At("HEAD"), cancellable = true)
    private static void requiem$bookCreator(ItemStack stack, CallbackInfoReturnable<String> callback) {
        if (!stack.is(Items.ENCHANTED_BOOK)) return;
        var enchantments = EnchantedBookItem.getEnchantments(stack);
        // Match Forge's single-enchantment attribution without changing saved enchantment IDs.
        if (enchantments.size() != 1) return;
        var id = ResourceLocation.tryParse(enchantments.getCompound(0).getString("id"));
        if (ModEnchantments.SOUL_PURSUIT.getId().equals(id) || ModEnchantments.SWORD_STORM.getId().equals(id)) {
            callback.setReturnValue(RequiemAddon.MODID);
        }
    }
}
