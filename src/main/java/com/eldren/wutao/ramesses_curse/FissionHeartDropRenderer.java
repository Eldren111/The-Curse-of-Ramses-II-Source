package com.eldren.wutao.ramesses_curse;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;

public class FissionHeartDropRenderer extends EntityRenderer<FissionHeartDropEntity> {
    private final ItemRenderer itemRenderer;

    public FissionHeartDropRenderer(EntityRendererProvider.Context context) {
        super(context);
        itemRenderer = context.getItemRenderer();
        shadowRadius = 0.18F;
        shadowStrength = 0.35F;
    }

    @Override
    public void render(
            FissionHeartDropEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight) {
        float age = entity.tickCount + partialTick;
        float bob = Mth.sin(age * 0.12F) * 0.08F + 0.18F;
        poseStack.pushPose();
        poseStack.translate(0.0D, bob, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(age * 3.0F));
        poseStack.scale(1.2F, 1.2F, 1.2F);
        itemRenderer.renderStatic(
                entity.getItem(),
                ItemDisplayContext.GROUND,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                entity.level(),
                entity.getId());
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FissionHeartDropEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
