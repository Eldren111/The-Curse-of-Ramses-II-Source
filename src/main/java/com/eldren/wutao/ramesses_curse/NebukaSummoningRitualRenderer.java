package com.eldren.wutao.ramesses_curse;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;

public class NebukaSummoningRitualRenderer extends EntityRenderer<NebukaSummoningRitualEntity> {
    private static final float HALF_SQRT_3 = (float) (Math.sqrt(3.0D) / 2.0D);

    public NebukaSummoningRitualRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(NebukaSummoningRitualEntity entity, Frustum frustum,
                                double cameraX, double cameraY, double cameraZ) {
        return true;
    }

    @Override
    public void render(NebukaSummoningRitualEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float progress = entity.getProgress(partialTick);
        if (progress <= 0.0F) {
            return;
        }

        float fade = progress > 0.8F ? Math.min((progress - 0.8F) / 0.2F, 1.0F) : 0.0F;
        int rayCount = Mth.floor((progress + progress * progress) * 0.5F * 30.0F);
        int centerAlpha = (int) (255.0F * (1.0F - fade));
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        RandomSource random = RandomSource.create(432L);

        poseStack.pushPose();
        for (int i = 0; i < rayCount; i++) {
            poseStack.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0F + progress * 90.0F));

            float length = (random.nextFloat() * 5.0F + 4.0F + fade * 4.0F) * 0.78F;
            float width = (random.nextFloat() * 0.65F + 1.1F + fade * 1.8F) * 0.78F;
            Matrix4f matrix = poseStack.last().pose();

            centerVertex(consumer, matrix, centerAlpha);
            outerVertexLeft(consumer, matrix, length, width);
            outerVertexRight(consumer, matrix, length, width);
            centerVertex(consumer, matrix, centerAlpha);
            outerVertexRight(consumer, matrix, length, width);
            outerVertexTop(consumer, matrix, length, width);
            centerVertex(consumer, matrix, centerAlpha);
            outerVertexTop(consumer, matrix, length, width);
            outerVertexLeft(consumer, matrix, length, width);
        }
        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void centerVertex(VertexConsumer consumer, Matrix4f matrix, int alpha) {
        consumer.vertex(matrix, 0.0F, 0.0F, 0.0F)
                .color(255, 255, 225, alpha)
                .endVertex();
    }

    private static void outerVertexLeft(VertexConsumer consumer, Matrix4f matrix, float length, float width) {
        outerVertex(consumer, matrix, -HALF_SQRT_3 * width, length, -0.5F * width);
    }

    private static void outerVertexRight(VertexConsumer consumer, Matrix4f matrix, float length, float width) {
        outerVertex(consumer, matrix, HALF_SQRT_3 * width, length, -0.5F * width);
    }

    private static void outerVertexTop(VertexConsumer consumer, Matrix4f matrix, float length, float width) {
        outerVertex(consumer, matrix, 0.0F, length, width);
    }

    private static void outerVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z) {
        consumer.vertex(matrix, x, y, z)
                .color(255, 190, 24, 0)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(NebukaSummoningRitualEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
