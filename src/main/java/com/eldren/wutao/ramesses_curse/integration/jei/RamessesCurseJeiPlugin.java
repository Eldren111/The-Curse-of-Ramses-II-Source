package com.eldren.wutao.ramesses_curse.integration.jei;

import com.eldren.wutao.ramesses_curse.ModItems;
import com.eldren.wutao.ramesses_curse.RamessesCurseMod;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

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
        ItemStack awkwardPotion = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD);
        IJeiBrewingRecipe dynastyPressureBrewing = registration.getVanillaRecipeFactory().createBrewingRecipe(
                List.of(new ItemStack(Items.ECHO_SHARD)),
                awkwardPotion,
                new ItemStack(ModItems.DYNASTY_PRESSURE_POTION.get()),
                new ResourceLocation(RamessesCurseMod.MODID, "dynasty_pressure_potion_brewing"));
        registration.addRecipes(RecipeTypes.BREWING, List.of(dynastyPressureBrewing));

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
                List.of(new ItemStack(ModItems.HEART_OF_DUAT.get())),
                Component.translatable("jei.ramesses_curse.heart_of_duat.line1"),
                Component.translatable("jei.ramesses_curse.heart_of_duat.line2"));

        registration.addItemStackInfo(
                List.of(new ItemStack(ModItems.URAEUS_NECKLACE.get())),
                Component.translatable("jei.ramesses_curse.uraeus_necklace.line1"),
                Component.translatable("jei.ramesses_curse.uraeus_necklace.line2"));

        registration.addItemStackInfo(
                List.of(new ItemStack(ModItems.FISSION_WRAITH_HEART.get())),
                Component.translatable("jei.ramesses_curse.fission_wraith_heart.line1"));

        registration.addItemStackInfo(
                ModItems.AFFIX_KNOWLEDGE_BOOKS.values().stream()
                        .map(book -> new ItemStack(book.get()))
                        .toList(),
                Component.translatable("jei.ramesses_curse.affix_knowledge_book.line1"));
    }
}
