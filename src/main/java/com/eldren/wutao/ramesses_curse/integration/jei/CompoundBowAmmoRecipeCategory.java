package com.eldren.wutao.ramesses_curse.integration.jei;

import com.eldren.wutao.ramesses_curse.ModItems;
import com.eldren.wutao.ramesses_curse.RamessesCurseMod;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class CompoundBowAmmoRecipeCategory implements IRecipeCategory<CompoundBowAmmoRecipe> {
    public static final RecipeType<CompoundBowAmmoRecipe> TYPE = RecipeType.create(
            RamessesCurseMod.MODID,
            "compound_bow_ammunition",
            CompoundBowAmmoRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public CompoundBowAmmoRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(126, 86);
        this.icon = guiHelper.createDrawableItemLike(ModItems.ETERNAL_SILENCE_COMPOUND_BOW.get());
    }

    @Override
    public RecipeType<CompoundBowAmmoRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return ModItems.ETERNAL_SILENCE_COMPOUND_BOW.get().getDescription();
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(
            IRecipeLayoutBuilder builder,
            CompoundBowAmmoRecipe recipe,
            IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST, 8, 19)
                .setStandardSlotBackground()
                .addItemStack(recipe.bow());

        for (int index = 0; index < recipe.ammunition().size(); index++) {
            int column = index % 3;
            int row = index / 3;
            builder.addSlot(RecipeIngredientRole.INPUT, 55 + column * 22, 8 + row * 22)
                    .setStandardSlotBackground()
                    .addItemStack(recipe.ammunition().get(index));
        }

        for (int index = 0; index < recipe.rapidFireAmmunition().size(); index++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 11 + index * 22, 66)
                    .setStandardSlotBackground()
                    .addItemStack(recipe.rapidFireAmmunition().get(index));
        }
    }

    @Override
    public void draw(
            CompoundBowAmmoRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics guiGraphics,
            double mouseX,
            double mouseY) {
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                Component.translatable("jei.ramesses_curse.compound_bow.rapid_fire"),
                8,
                54,
                0xE0A92F,
                false);
    }
}
