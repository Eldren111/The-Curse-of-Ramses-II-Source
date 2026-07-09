package com.eldren.wutao.ramesses_curse.integration.jei;

import com.eldren.wutao.ramesses_curse.ModItems;
import com.eldren.wutao.ramesses_curse.ModEnchantments;
import com.eldren.wutao.ramesses_curse.RamessesCurseMod;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.util.List;

@JeiPlugin
public class RamessesCurseJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_UID = new ResourceLocation(RamessesCurseMod.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addItemStackInfo(
                List.of(new ItemStack(ModItems.SEAL_OF_KADESH.get())),
                Component.translatable("jei.ramesses_curse.seal_of_kadesh.line1"),
                Component.translatable("jei.ramesses_curse.seal_of_kadesh.line2"),
                Component.translatable("jei.ramesses_curse.seal_of_kadesh.line3"));

        registration.addItemStackInfo(
                List.of(new ItemStack(ModItems.PURIFICATION_SCROLL.get())),
                Component.translatable("jei.ramesses_curse.purification_scroll.line1"),
                Component.translatable("jei.ramesses_curse.purification_scroll.line2"));

        registration.addItemStackInfo(
                List.of(new ItemStack(ModItems.DYNASTY_PRESSURE_POTION.get())),
                Component.translatable("jei.ramesses_curse.dynasty_pressure_potion.line1"),
                Component.translatable("jei.ramesses_curse.dynasty_pressure_potion.line2"),
                Component.translatable("jei.ramesses_curse.dynasty_pressure_potion.line3"));

        registration.addItemStackInfo(
                List.of(new ItemStack(ModItems.SATIETY_CRYSTAL.get())),
                Component.translatable("jei.ramesses_curse.satiety_crystal.line1"),
                Component.translatable("jei.ramesses_curse.satiety_crystal.line2"));

        registration.addItemStackInfo(
                List.of(new ItemStack(ModItems.EMBER_OF_DUAT.get())),
                Component.translatable("jei.ramesses_curse.ember_of_duat.line1"),
                Component.translatable("jei.ramesses_curse.ember_of_duat.line2"),
                Component.translatable("jei.ramesses_curse.ember_of_duat.line3"));

        registration.addItemStackInfo(
                List.of(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.CORROSION_WARD.get(), 1))),
                Component.translatable("jei.ramesses_curse.corrosion_ward.line1"),
                Component.translatable("jei.ramesses_curse.corrosion_ward.line2"));

        registration.addItemStackInfo(
                List.of(new ItemStack(ModItems.HEART_OF_DUAT.get())),
                Component.translatable("jei.ramesses_curse.heart_of_duat.line1"),
                Component.translatable("jei.ramesses_curse.heart_of_duat.line2"));

        registration.addItemStackInfo(
                List.of(new ItemStack(ModItems.URAEUS_NECKLACE.get())),
                Component.translatable("jei.ramesses_curse.uraeus_necklace.line1"),
                Component.translatable("jei.ramesses_curse.uraeus_necklace.line2"));
    }
}
