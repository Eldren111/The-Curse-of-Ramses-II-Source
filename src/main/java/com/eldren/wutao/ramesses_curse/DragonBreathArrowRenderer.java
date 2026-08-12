package com.eldren.wutao.ramesses_curse;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class DragonBreathArrowRenderer extends EntityRenderer<DragonBreathArrowEntity> {
    private static final ResourceLocation EMPTY_TEXTURE =
            new ResourceLocation(RamessesCurseMod.MODID, "textures/entity/empty.png");

    public DragonBreathArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(DragonBreathArrowEntity entity) {
        return EMPTY_TEXTURE;
    }
}
