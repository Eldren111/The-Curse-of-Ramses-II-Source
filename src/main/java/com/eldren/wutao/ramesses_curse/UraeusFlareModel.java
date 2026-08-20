package com.eldren.wutao.ramesses_curse;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class UraeusFlareModel extends GeoModel<UraeusFlareEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "geo/uraeus_flare_cube.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "textures/effect/uraeus_flare_cube.png");
    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "animations/uraeus_flare_cube.animation.json");

    @Override
    public ResourceLocation getModelResource(UraeusFlareEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(UraeusFlareEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(UraeusFlareEntity animatable) {
        return ANIMATION;
    }

    @Override
    public RenderType getRenderType(UraeusFlareEntity animatable, ResourceLocation texture) {
        return RenderType.eyes(texture);
    }
}
