package com.eldren.wutao.ramesses_curse;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class NebukaGuardianRenderer extends GeoEntityRenderer<NebukaGuardianEntity> {
    private static final ResourceLocation EMISSIVE = ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "textures/entity/nebuka_guardian_emissive.png");

    public NebukaGuardianRenderer(EntityRendererProvider.Context context) {
        super(context, new NebukaGuardianModel());
        this.shadowRadius = 1.15F;
        addRenderLayer(new EmissiveLayer(this));
    }

    @Override
    protected float getDeathMaxRotation(NebukaGuardianEntity animatable) {
        return 0.0F;
    }

    private static final class EmissiveLayer extends GeoRenderLayer<NebukaGuardianEntity> {
        private EmissiveLayer(NebukaGuardianRenderer renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, NebukaGuardianEntity animatable, BakedGeoModel bakedModel,
                           RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                           float partialTick, int packedLight, int packedOverlay) {
            RenderType emissiveType = RenderType.eyes(EMISSIVE);
            getRenderer().reRender(
                    bakedModel,
                    poseStack,
                    bufferSource,
                    animatable,
                    emissiveType,
                    bufferSource.getBuffer(emissiveType),
                    partialTick,
                    LightTexture.FULL_BRIGHT,
                    packedOverlay,
                    0xFFFFFFFF);
        }
    }
}
