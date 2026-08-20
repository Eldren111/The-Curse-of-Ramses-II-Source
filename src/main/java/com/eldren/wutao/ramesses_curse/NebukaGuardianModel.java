package com.eldren.wutao.ramesses_curse;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NebukaGuardianModel extends GeoModel<NebukaGuardianEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "geo/nebuka_guardian.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "textures/entity/nebuka_guardian.png");
    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "animations/nebuka_guardian.animation.json");

    @Override
    public ResourceLocation getModelResource(NebukaGuardianEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(NebukaGuardianEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(NebukaGuardianEntity animatable) {
        return ANIMATION;
    }
}
