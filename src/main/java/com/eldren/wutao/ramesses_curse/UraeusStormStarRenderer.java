package com.eldren.wutao.ramesses_curse;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class UraeusStormStarRenderer extends EntityRenderer<UraeusStormStarEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "textures/effect/uraeus_flare_cube.png");
    private static final int[] STAR_ORDER = {0, 2, 4, 1, 3, 0};

    public UraeusStormStarRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(UraeusStormStarEntity entity, Frustum frustum, double cameraX, double cameraY, double cameraZ) {
        return true;
    }

    @Override
    public void render(UraeusStormStarEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.eyes(TEXTURE));
        float age = entity.tickCount + partialTick;
        float radius = entity.getRadius();
        float fade = 1.0F - entity.getLifeProgress(partialTick) * 0.35F;
        float pulse = 0.85F + 0.15F * Mth.sin(age * 0.45F);

        float[][] points = new float[5][2];
        for (int i = 0; i < 5; i++) {
            float angle = (float) (-Math.PI / 2.0D + i * Math.PI * 2.0D / 5.0D + age * 0.015F);
            points[i][0] = Mth.cos(angle) * radius;
            points[i][1] = Mth.sin(angle) * radius;
        }

        poseStack.pushPose();
        drawBox(poseStack, consumer, 0.0F, 0.0F, 0.0F, 0.38F * pulse, 0.38F * pulse, 0.38F * pulse, 185, 242, 255, (int) (255 * fade));
        drawBox(poseStack, consumer, 0.0F, -1.4F, 0.0F, 0.18F * pulse, 2.8F, 0.18F * pulse, 145, 244, 255, (int) (210 * fade));
        for (int path = 0; path < STAR_ORDER.length - 1; path++) {
            float[] from = points[STAR_ORDER[path]];
            float[] to = points[STAR_ORDER[path + 1]];
            for (int step = 0; step <= 11; step++) {
                float t = step / 11.0F;
                float x = Mth.lerp(t, from[0], to[0]);
                float z = Mth.lerp(t, from[1], to[1]);
                float y = Mth.sin(age * 0.22F + path * 0.9F + step * 0.35F) * 0.08F;
                float size = (0.14F + 0.035F * Mth.sin(age * 0.5F + step)) * pulse;
                int alpha = (int) ((step % 3 == 0 ? 255 : 225) * fade);
                drawBox(poseStack, consumer, x, y, z, size, size, size, 165, 238, 255, alpha);
            }
        }
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void drawBox(PoseStack poseStack, VertexConsumer consumer, float centerX, float centerY, float centerZ, float width, float height, float depth, int red, int green, int blue, int alpha) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        float minX = centerX - width * 0.5F;
        float maxX = centerX + width * 0.5F;
        float minY = centerY - height * 0.5F;
        float maxY = centerY + height * 0.5F;
        float minZ = centerZ - depth * 0.5F;
        float maxZ = centerZ + depth * 0.5F;

        face(consumer, matrix, normal, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, red, green, blue, alpha);
        face(consumer, matrix, normal, maxX, minY, minZ, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, red, green, blue, alpha);
        face(consumer, matrix, normal, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, red, green, blue, alpha);
        face(consumer, matrix, normal, maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, red, green, blue, alpha);
        face(consumer, matrix, normal, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, minX, maxY, minZ, red, green, blue, alpha);
        face(consumer, matrix, normal, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, red, green, blue, alpha);
    }

    private static void face(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             int red, int green, int blue, int alpha) {
        vertex(consumer, matrix, normal, x1, y1, z1, 0.0F, 1.0F, red, green, blue, alpha);
        vertex(consumer, matrix, normal, x2, y2, z2, 1.0F, 1.0F, red, green, blue, alpha);
        vertex(consumer, matrix, normal, x3, y3, z3, 1.0F, 0.0F, red, green, blue, alpha);
        vertex(consumer, matrix, normal, x4, y4, z4, 0.0F, 0.0F, red, green, blue, alpha);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, float x, float y, float z, float u, float v, int red, int green, int blue, int alpha) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0.0F, 1.0F, 0.0F)
                ;
    }

    @Override
    public ResourceLocation getTextureLocation(UraeusStormStarEntity entity) {
        return TEXTURE;
    }
}
