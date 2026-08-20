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
import net.minecraft.world.item.ItemStack;

public class SatietyCrystalRenderer extends EntityRenderer<SatietyCrystalEntity> {
    private final ItemRenderer itemRenderer;

    public SatietyCrystalRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.45F;
    }

    @Override
    public void render(SatietyCrystalEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        ItemStack stack = new ItemStack(ModItems.SATIETY_CRYSTAL.get());
        float age = entity.tickCount + partialTick;
        float bob = Mth.sin(age / 10.0F + entity.hoverStart) * 0.1F + 0.16F;

        poseStack.pushPose();
        poseStack.translate(0.0D, bob, 0.0D);
        poseStack.mulPose(Axis.YP.rotation(age / 20.0F + entity.hoverStart));
        poseStack.scale(1.25F, 1.25F, 1.25F);
        itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, entity.level(), entity.getId());
        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(SatietyCrystalEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
