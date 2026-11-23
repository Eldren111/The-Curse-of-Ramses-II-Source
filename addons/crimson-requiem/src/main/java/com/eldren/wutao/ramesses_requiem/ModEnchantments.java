package com.eldren.wutao.ramesses_requiem;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, RequiemAddon.LEGACY_NAMESPACE);
    private static final EnchantmentCategory REQUIEM = EnchantmentCategory.create(
            "RAMESSES_CRIMSON_REQUIEM", item -> item instanceof CrimsonRequiemItem);
    public static final RegistryObject<Enchantment> SOUL_PURSUIT = ENCHANTMENTS.register("soul_pursuit",
            () -> new Enchantment(Enchantment.Rarity.VERY_RARE, REQUIEM, new EquipmentSlot[]{EquipmentSlot.MAINHAND}) {
                @Override public boolean canEnchant(ItemStack stack) { return stack.is(RequiemItems.CRIMSON_REQUIEM.get()); }
                @Override public boolean canApplyAtEnchantingTable(ItemStack stack) { return false; }
                @Override public boolean isTreasureOnly() { return true; }
                @Override public boolean isTradeable() { return false; }
                @Override public boolean isDiscoverable() { return false; }
            });
    public static final RegistryObject<Enchantment> SWORD_STORM = ENCHANTMENTS.register("sword_storm",
            () -> new Enchantment(Enchantment.Rarity.VERY_RARE, REQUIEM, new EquipmentSlot[]{EquipmentSlot.MAINHAND}) {
                @Override public boolean canEnchant(ItemStack stack) { return stack.is(RequiemItems.CRIMSON_REQUIEM.get()); }
                @Override public boolean canApplyAtEnchantingTable(ItemStack stack) { return false; }
                @Override public boolean isTreasureOnly() { return true; }
                @Override public boolean isTradeable() { return false; }
                @Override public boolean isDiscoverable() { return false; }
            });

    public static ItemStack pursuitBook() {
        return EnchantedBookItem.createForEnchantment(new EnchantmentInstance(SOUL_PURSUIT.get(), 1));
    }

    public static ItemStack stormBook() {
        return EnchantedBookItem.createForEnchantment(new EnchantmentInstance(SWORD_STORM.get(), 1));
    }

    private ModEnchantments() {}
}
