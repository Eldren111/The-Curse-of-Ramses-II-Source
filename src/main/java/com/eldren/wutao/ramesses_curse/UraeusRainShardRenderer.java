package com.eldren.wutao.ramesses_curse;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class UraeusRainShardRenderer extends EntityRenderer<UraeusRainShardEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "textures/effect/uraeus_flare_cube.png");
    private static final ResourceLocation TRAIL_TEXTURE = ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "textures/effect/uraeus_flare_trail.png");

    public UraeusRainShardRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(UraeusRainShardEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        Vec3 origin = entity.getPosition(partialTick);
        Vec3 cameraPosition = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        VertexConsumer trailConsumer = bufferSource.getBuffer(RenderType.eyes(TRAIL_TEXTURE));
        VertexConsumer shardConsumer = bufferSource.getBuffer(RenderType.eyes(TEXTURE));
        float age = entity.tickCount + partialTick;
        int red = entity.isThunder() ? 170 : 95;
        int green = entity.isThunder() ? 230 : 220;
        int blue = entity.isThunder() ? 255 : 245;

        for (int shardIndex = 0; shardIndex < entity.getShardCount(); shardIndex++) {
            if (!entity.isShardActive(shardIndex)) {
                continue;
            }

            renderTrail(entity, shardIndex, partialTick, origin, cameraPosition, poseStack, trailConsumer);
            Vec3 localPosition = entity.getShardPosition(shardIndex, partialTick).subtract(origin);
            float pulse = 0.82F + 0.18F * Mth.sin(age * 0.35F + shardIndex * 0.31F);
            float size = (entity.isThunder() ? 0.16F : 0.13F) * pulse;

            poseStack.pushPose();
            poseStack.translate(localPosition.x, localPosition.y, localPosition.z);
            drawBox(
                    poseStack,
                    shardConsumer,
                    size,
                    red,
                    green,
                    blue,
                    entity.isThunder() ? 235 : 210);
            poseStack.popPose();
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private void renderTrail(
            UraeusRainShardEntity entity,
            int shardIndex,
            float partialTick,
            Vec3 origin,
            Vec3 cameraPosition,
            PoseStack poseStack,
            VertexConsumer consumer) {
        int length = entity.getShardTrailLength(shardIndex);
        if (length < 3) {
            return;
        }

        for (int i = 0; i < length - 1; i++) {
            Vec3 current = entity.getShardTrailPosition(shardIndex, i, partialTick).subtract(origin);
            Vec3 next = entity.getShardTrailPosition(shardIndex, i + 1, partialTick).subtract(origin);
            Vec3 direction = next.subtract(current);
            if (direction.lengthSqr() < 1.0E-5D) {
                continue;
            }

            Vec3 toCamera = cameraPosition.subtract(current.add(origin));
            Vec3 side = direction.cross(toCamera);
            if (side.lengthSqr() < 1.0E-5D) {
                side = direction.cross(new Vec3(0.0D, 1.0D, 0.0D));
            }
            if (side.lengthSqr() < 1.0E-5D) {
                side = new Vec3(1.0D, 0.0D, 0.0D);
            }

            double fade = 1.0D - i / (double) length;
            double width = entity.isThunder() ? 0.16D : 0.105D;
            int maxAlpha = entity.isThunder() ? 205 : 125;
            side = side.normalize().scale(width * fade);
            int alpha = Mth.clamp((int) (maxAlpha * fade), 8, maxAlpha);
            float u1 = i / (float) (length - 1);
            float u2 = (i + 1) / (float) (length - 1);
            int red = entity.isThunder() ? 165 : 95;
            int green = entity.isThunder() ? 238 : 220;
            int blue = entity.isThunder() ? 255 : 245;
            addTrailQuad(poseStack.last(), consumer, current, next, side, u1, u2, red, green, blue, alpha);
        }
    }

    private static void drawBox(PoseStack poseStack, VertexConsumer consumer, float size, int red, int green, int blue, int alpha) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        float min = -size * 0.5F;
        float max = size * 0.5F;
        face(consumer, matrix, normal, min, min, max, max, min, max, max, max, max, min, max, max, red, green, blue, alpha);
        face(consumer, matrix, normal, max, min, min, min, min, min, min, max, min, max, max, min, red, green, blue, alpha);
        face(consumer, matrix, normal, min, min, min, min, min, max, min, max, max, min, max, min, red, green, blue, alpha);
        face(consumer, matrix, normal, max, min, max, max, min, min, max, max, min, max, max, max, red, green, blue, alpha);
        face(consumer, matrix, normal, min, max, max, max, max, max, max, max, min, min, max, min, red, green, blue, alpha);
        face(consumer, matrix, normal, min, min, min, max, min, min, max, min, max, min, min, max, red, green, blue, alpha);
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

    private static void addTrailQuad(PoseStack.Pose pose, VertexConsumer consumer, Vec3 current, Vec3 next, Vec3 side, float u1, float u2, int red, int green, int blue, int alpha) {
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        int nextAlpha = Math.max(6, alpha / 2);
        trailVertex(consumer, matrix, normal, current.subtract(side), u1, 0.0F, red, green, blue, alpha);
        trailVertex(consumer, matrix, normal, next.subtract(side), u2, 0.0F, red, green, blue, nextAlpha);
        trailVertex(consumer, matrix, normal, next.add(side), u2, 1.0F, red, green, blue, nextAlpha);
        trailVertex(consumer, matrix, normal, current.add(side), u1, 1.0F, red, green, blue, alpha);
        trailVertex(consumer, matrix, normal, current.add(side), u1, 1.0F, red, green, blue, alpha);
        trailVertex(consumer, matrix, normal, next.add(side), u2, 1.0F, red, green, blue, nextAlpha);
        trailVertex(consumer, matrix, normal, next.subtract(side), u2, 0.0F, red, green, blue, nextAlpha);
        trailVertex(consumer, matrix, normal, current.subtract(side), u1, 0.0F, red, green, blue, alpha);
    }

    private static void trailVertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, Vec3 pos, float u, float v, int red, int green, int blue, int alpha) {
        consumer.addVertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0.0F, 1.0F, 0.0F)
                ;
    }

    @Override
    public ResourceLocation getTextureLocation(UraeusRainShardEntity entity) {
        return TEXTURE;
    }
}
