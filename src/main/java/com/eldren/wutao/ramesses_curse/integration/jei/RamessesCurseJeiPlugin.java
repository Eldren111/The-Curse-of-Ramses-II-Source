package com.eldren.wutao.ramesses_curse.integration.jei;

import com.eldren.wutao.ramesses_curse.DynastyAffixData;
import com.eldren.wutao.ramesses_curse.ModItems;
import com.eldren.wutao.ramesses_curse.RamessesCurseMod;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

import java.math.BigDecimal;
import java.util.List;

@JeiPlugin
public class RamessesCurseJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_UID = new ResourceLocation(RamessesCurseMod.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new CompoundBowAmmoRecipeCategory(
                registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(ModItems.ETERNAL_SILENCE_COMPOUND_BOW.get()),
                CompoundBowAmmoRecipeCategory.TYPE);
        registration.addRecipeCatalyst(
                new ItemStack(ModItems.DRAGON_BREATH_COMPOUND_BOW.get()),
                CompoundBowAmmoRecipeCategory.TYPE);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(
                CompoundBowAmmoRecipeCategory.TYPE,
                List.of(new CompoundBowAmmoRecipe(
                        new ItemStack(ModItems.ETERNAL_SILENCE_COMPOUND_BOW.get()),
                        List.of(
                                new ItemStack(Items.ARROW),
                                new ItemStack(Items.TIPPED_ARROW),
                                new ItemStack(ModItems.ARMOR_PIERCING_ARROW.get()),
                                new ItemStack(Items.TNT),
                                new ItemStack(Items.DRAGON_BREATH),
                                new ItemStack(Items.FIRE_CHARGE),
                                new ItemStack(ModItems.TRUTH.get())),
                        List.of(
                                new ItemStack(Items.ARROW),
                                new ItemStack(Items.TIPPED_ARROW),
                                new ItemStack(ModItems.ARMOR_PIERCING_ARROW.get()),
                                new ItemStack(Items.DRAGON_BREATH),
                                new ItemStack(Items.FIRE_CHARGE),
                                new ItemStack(ModItems.TRUTH.get())))));

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
                List.of(new ItemStack(ModItems.RUIN_TALISMAN.get())),
                Component.translatable("jei.ramesses_curse.ruin_talisman.line1"),
                Component.translatable("jei.ramesses_curse.ruin_talisman.line2"),
                Component.translatable("jei.ramesses_curse.ruin_talisman.line3"));

        registration.addItemStackInfo(
                List.of(new ItemStack(ModItems.RAW_GROUP_U.get())),
                Component.translatable("jei.ramesses_curse.raw_group_u.drop"));

        registerAffixMaterialInfo(registration, ModItems.CALMING_TIBIA.get(), "rage");
        registerAffixMaterialInfo(registration, ModItems.PITCHFIRE_CRYSTAL.get(), "molten");
        registerAffixMaterialInfo(registration, ModItems.HEART_DEVOURING_REMNANT.get(), "blood_forfeit");
        registerAffixMaterialInfo(registration, ModItems.RAVENOUS_CLAW_FRAGMENT.get(), "claw_rage");
        registerAffixMaterialInfo(registration, ModItems.FAMINE_CORE.get(), "flesh_feast");
        registerAffixMaterialInfo(registration, ModItems.VENOM_ORB.get(), "miasma");
        registerAffixMaterialInfo(registration, ModItems.KA_REMNANT_BREATH.get(), "restoration");
        registerAffixMaterialInfo(registration, ModItems.LIFE_BINDING_REMNANT_CRYSTAL.get(), "blood_limit");
        registerAffixMaterialInfo(registration, ModItems.AUTHORITY_FRAGMENT.get(), "death_authority");
        registerAffixMaterialInfo(registration, ModItems.TRUTH.get(), "vulnerability");
        registerAffixMaterialInfo(registration, ModItems.REVERSE_SCALE.get(), "counter");

        registration.addItemStackInfo(
                ModItems.AFFIX_KNOWLEDGE_BOOKS.values().stream()
                        .map(book -> new ItemStack(book.get()))
                        .toList(),
                Component.translatable("jei.ramesses_curse.affix_knowledge_book.line1"));
    }

    private static void registerAffixMaterialInfo(IRecipeRegistration registration, net.minecraft.world.item.Item item,
                                                  String affixId) {
        double chance = Math.max(0.0D, DynastyAffixData.number(affixId, 1, "drop_chance"));
        String percentage = BigDecimal.valueOf(chance * 100.0D).stripTrailingZeros().toPlainString() + "%";
        registration.addItemStackInfo(
                List.of(new ItemStack(item)),
                Component.translatable(
                        "jei.ramesses_curse.affix_material.drop",
                        Component.translatable("affix.ramesses_curse." + affixId),
                        percentage));
    }
}
