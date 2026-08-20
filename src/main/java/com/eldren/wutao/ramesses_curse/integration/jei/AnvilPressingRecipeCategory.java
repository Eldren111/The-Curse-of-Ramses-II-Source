package com.eldren.wutao.ramesses_curse.integration.jei;

import com.eldren.wutao.ramesses_curse.RamessesCurseMod;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class AnvilPressingRecipeCategory implements IRecipeCategory<AnvilPressingRecipe> {
    public static final RecipeType<AnvilPressingRecipe> TYPE = RecipeType.create(
            RamessesCurseMod.MODID,
            "anvil_pressing",
            AnvilPressingRecipe.class);

    private static final int INPUT_X = 20;
    private static final int SLOT_Y = 47;
    private static final int OUTPUT_X = 94;
    private static final long CYCLE_NANOS = 1_300_000_000L;
    private static final long FALL_NANOS = 800_000_000L;
    private static final float ANVIL_SCALE = 1.6F;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableStatic arrow;
    private final ItemStack anvil = new ItemStack(Items.ANVIL);

    public AnvilPressingRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(126, 70);
        this.icon = guiHelper.createDrawableItemLike(Items.ANVIL);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<AnvilPressingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.ramesses_curse.anvil_pressing.title");
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
    public void setRecipe(IRecipeLayoutBuilder builder, AnvilPressingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X, SLOT_Y)
                .setStandardSlotBackground()
                .addItemStack(recipe.input());
        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, SLOT_Y)
                .setOutputSlotBackground()
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(
            AnvilPressingRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics guiGraphics,
            double mouseX,
            double mouseY) {
        long phase = System.nanoTime() % CYCLE_NANOS;
        double progress = Math.min(1.0D, phase / (double) FALL_NANOS);
        double easedProgress = progress * progress;
        double anvilY = 5.0D + easedProgress * 19.0D;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(INPUT_X + 9.0F, anvilY + 8.0F, 100.0F);
        guiGraphics.pose().scale(ANVIL_SCALE, ANVIL_SCALE, 1.0F);
        guiGraphics.renderItem(anvil, -8, -8);
        guiGraphics.pose().popPose();
        arrow.draw(guiGraphics, 55, SLOT_Y);
        Component successRate = Component.translatable("jei.ramesses_curse.anvil_pressing.success_rate");
        int successRateX = 55 + (arrow.getWidth() - Minecraft.getInstance().font.width(successRate)) / 2;
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                successRate,
                successRateX,
                SLOT_Y - 11,
                0xFF404040,
                false);
    }
}
