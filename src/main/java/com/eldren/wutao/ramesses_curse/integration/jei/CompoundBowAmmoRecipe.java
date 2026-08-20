package com.eldren.wutao.ramesses_curse.integration.jei;

import java.util.List;
import net.minecraft.world.item.ItemStack;

public record CompoundBowAmmoRecipe(
        ItemStack bow,
        List<ItemStack> ammunition,
        List<ItemStack> rapidFireAmmunition) {
}
