package com.eldren.wutao.ramesses_curse;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;

public class ModMobEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, RamessesCurseMod.MODID);

    public static final RegistryObject<MobEffect> INVERSION =
            MOB_EFFECTS.register("inversion", InversionMobEffect::new);

    public static class InversionMobEffect extends MobEffect {
        private static final ResourceLocation INVERSION_ICON = new ResourceLocation(RamessesCurseMod.MODID, "textures/mob_effect/inversion.png");
        private static final int ICON_SIZE = 16;
        private static final int ICON_OFFSET_X = 1;
        private static final int ICON_OFFSET_Y = 1;

        public InversionMobEffect() {
            super(MobEffectCategory.HARMFUL, 0x6A2CA0);
        }

        @Override
        public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
            consumer.accept(new IClientMobEffectExtensions() {
                @Override
                public boolean renderInventoryIcon(MobEffectInstance instance, EffectRenderingInventoryScreen<?> screen, GuiGraphics guiGraphics, int x, int y, int blitOffset) {
                    guiGraphics.blit(INVERSION_ICON, x + ICON_OFFSET_X, y + ICON_OFFSET_Y, blitOffset, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
                    return true;
                }

                @Override
                public boolean renderGuiIcon(MobEffectInstance instance, Gui gui, GuiGraphics guiGraphics, int x, int y, float z, float alpha) {
                    guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha);
                    guiGraphics.blit(INVERSION_ICON, x + ICON_OFFSET_X, y + ICON_OFFSET_Y, (int) z, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
                    guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                    return true;
                }
            });
        }
    }
}
