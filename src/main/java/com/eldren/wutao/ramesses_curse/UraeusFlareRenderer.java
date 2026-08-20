package com.eldren.wutao.ramesses_curse;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import com.mojang.math.Axis;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class UraeusFlareRenderer extends GeoEntityRenderer<UraeusFlareEntity> {
    private static final ResourceLocation EFFECT_TEXTURE = ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "textures/effect/uraeus_flare_cube.png");
    private static final ResourceLocation TRAIL_TEXTURE = ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "textures/effect/uraeus_flare_trail.png");

    public UraeusFlareRenderer(EntityRendererProvider.Context context) {
        super(context, new UraeusFlareModel());
        this.withScale(0.09F);
    }

    @Override
    public void render(UraeusFlareEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        renderTrail(entity, partialTick, poseStack, bufferSource);
        renderParticleCloud(entity, partialTick, poseStack, bufferSource);

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, LightTexture.FULL_BRIGHT);
    }

    @Override
    protected void applyRotations(UraeusFlareEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick) {
        Vec3 velocity = entity.getDeltaMovement();
        if (velocity.lengthSqr() > 1.0E-5D) {
            float yaw = (float) (Math.atan2(velocity.x, velocity.z) * (180.0D / Math.PI));
            float pitch = (float) (Math.atan2(velocity.y, velocity.horizontalDistance()) * (180.0D / Math.PI));
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
        }
        float age = entity.tickCount + partialTick;
        poseStack.mulPose(Axis.YP.rotationDegrees(age * 12.0F + entity.getId() * 17.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(age * 5.0F));
    }

    private void renderParticleCloud(UraeusFlareEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.eyes(EFFECT_TEXTURE));
        float age = entity.tickCount + partialTick;
        float pulse = 0.88F + 0.12F * Mth.sin(age * 0.22F);
        int red = entity.isRainEmpowered() ? 125 : 255;
        int green = entity.isRainEmpowered() ? 245 : 214;
        int blue = entity.isRainEmpowered() ? 255 : 76;

        poseStack.pushPose();
        for (int i = 0; i < 7; i++) {
            float angle = age * 0.18F + i * 1.795F;
            float radius = 0.18F + 0.05F * Mth.sin(age * 0.11F + i);
            float x = Mth.cos(angle) * radius;
            float y = Mth.sin(age * 0.14F + i * 0.63F) * 0.12F;
            float z = Mth.sin(angle) * radius;
            float size = (0.075F + 0.02F * Mth.sin(age * 0.31F + i)) * pulse;
            drawBox(poseStack, consumer, x, y, z, size, size, size, red, green, blue, 160);
        }
        poseStack.popPose();
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

    private void renderTrail(UraeusFlareEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource) {
        int length = entity.getTrailLength();
        if (length < 3) {
            return;
        }

        Vec3 origin = entity.getPosition(partialTick);
        Vec3 cameraPosition = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.eyes(TRAIL_TEXTURE));

        for (int i = 0; i < length - 1; i++) {
            Vec3 current = entity.getTrailPosition(i, partialTick).subtract(origin);
            Vec3 next = entity.getTrailPosition(i + 1, partialTick).subtract(origin);
            Vec3 direction = next.subtract(current);
            if (direction.lengthSqr() < 1.0E-5D) {
                continue;
            }
            Vec3 worldCurrent = current.add(origin);
            Vec3 toCamera = cameraPosition.subtract(worldCurrent);
            Vec3 side = direction.cross(toCamera);
            if (side.lengthSqr() < 1.0E-5D) {
                side = direction.cross(new Vec3(0.0D, 1.0D, 0.0D));
            }
            if (side.lengthSqr() < 1.0E-5D) {
                side = new Vec3(1.0D, 0.0D, 0.0D);
            }
            double fade = 1.0D - i / (double) length;
            side = side.normalize().scale(0.34D * fade);
            int alpha = Mth.clamp((int) (220.0D * fade), 18, 220);
            int red = entity.isRainEmpowered() && i % 2 == 0 ? 100 : 255;
            int green = entity.isRainEmpowered() && i % 2 == 0 ? 240 : 222;
            int blue = entity.isRainEmpowered() && i % 2 == 0 ? 255 : 92;
            float u1 = i / (float) (length - 1);
            float u2 = (i + 1) / (float) (length - 1);
            addTrailQuad(poseStack.last(), consumer, current, next, side, u1, u2, red, green, blue, alpha);
        }
    }

    private static void addTrailQuad(PoseStack.Pose pose, VertexConsumer consumer, Vec3 current, Vec3 next, Vec3 side, float u1, float u2, int red, int green, int blue, int alpha) {
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        int nextAlpha = Math.max(8, alpha / 2);
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
    public ResourceLocation getTextureLocation(UraeusFlareEntity entity) {
        return EFFECT_TEXTURE;
    }
}
