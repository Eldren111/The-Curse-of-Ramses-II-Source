package com.eldren.wutao.ramesses_curse;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, RamessesCurseMod.MODID);

    public static final RegistryObject<Enchantment> CORROSION_WARD =
            ENCHANTMENTS.register("corrosion_ward", CorrosionWardEnchantment::new);

    private static class CorrosionWardEnchantment extends Enchantment {
        private static final EquipmentSlot[] ALL_SLOTS = EquipmentSlot.values();

        private CorrosionWardEnchantment() {
            super(Rarity.RARE, EnchantmentCategory.BREAKABLE, ALL_SLOTS);
        }

        @Override
        public int getMaxLevel() {
            return 1;
        }

        @Override
        public boolean canEnchant(ItemStack stack) {
            return stack.isDamageableItem();
        }

        @Override
        public boolean isDiscoverable() {
            return false;
        }
    }
}
