package com.eldren.wutao.ramesses_curse;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class UraeusStormStarRenderer extends EntityRenderer<UraeusStormStarEntity> {
    private static final int[] STAR_ORDER = {0, 2, 4, 1, 3, 0};

    public UraeusStormStarRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public boolean shouldRender(UraeusStormStarEntity entity, Frustum frustum,
                                double cameraX, double cameraY, double cameraZ) {
        return true;
    }

    @Override
    public void render(UraeusStormStarEntity entity, float yaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float age = entity.tickCount + partialTicks;
        float progress = entity.getLifeProgress(partialTicks);
        float appear = Mth.clamp(age / 12.0F, 0.0F, 1.0F);
        float fade = 1.0F - Mth.clamp((progress - 0.72F) / 0.28F, 0.0F, 1.0F);
        float radius = (3.35F + 0.18F * Mth.sin(age * 0.035F)) * appear;
        float pulse = 0.88F + 0.12F * Mth.sin(age * 0.075F);
        float rotation = age * 0.18F + entity.getId() * 11.0F;
        float chaosPhase = entity.getId() * 0.137F;
        Vec3 center = new Vec3(
                Mth.sin(age * 0.017F + chaosPhase * 3.0F) * radius * 0.035F,
                0.0D,
                Mth.cos(age * 0.013F + chaosPhase * 4.0F) * radius * 0.028F
        );

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = buffer.getBuffer(UraeusRenderTypes.spiritGlow());

        Vec3[] points = new Vec3[5];
        for (int index = 0; index < points.length; index++) {
            float angle = (float) (-Math.PI / 2.0D + index * Math.PI * 2.0D / 5.0D);
            angle += Mth.sin(age * 0.021F + index * 2.37F + chaosPhase) * 0.10F;
            float radialJitter = 0.90F + 0.10F * Mth.sin(age * 0.017F + index * 1.91F + chaosPhase * 2.0F);
            points[index] = new Vec3(
                    center.x + Mth.cos(angle) * radius * radialJitter * 1.04F,
                    Mth.sin(age * 0.09F + index * 1.63F + chaosPhase) * 0.045F,
                    center.z + Mth.sin(angle) * radius * radialJitter * 0.91F
            );
        }

        renderBrokenRing(matrix, consumer, center.add(0.0D, 0.010D, 0.0D), radius * 1.09F,
                46, 0.052F, age, chaosPhase, 11, 1, 40, 112, 180, (int) (146.0F * fade * pulse));
        renderBrokenRing(matrix, consumer, center.add(radius * 0.035F, 0.016D, -radius * 0.025F), radius * 0.89F,
                40, 0.037F, age, chaosPhase + 1.7F, 9, 3, 78, 194, 255, (int) (102.0F * fade));
        renderBrokenRing(matrix, consumer, center.add(-radius * 0.025F, 0.022D, radius * 0.04F), radius * 0.48F,
                32, 0.028F, age, chaosPhase + 3.1F, 8, 5, 138, 82, 255, (int) (88.0F * fade));

        renderStarLayer(matrix, consumer, points, age, chaosPhase, 0.006F, 0.102F,
                24, 58, 126, (int) (105.0F * fade));
        renderStarLayer(matrix, consumer, points, age, chaosPhase, 0.014F, 0.060F,
                69, 158, 230, (int) (185.0F * fade * pulse));
        renderStarLayer(matrix, consumer, points, age, chaosPhase, 0.021F, 0.026F,
                194, 238, 255, (int) (230.0F * fade));

        renderVertexSeals(matrix, consumer, points, age, chaosPhase, fade);
        renderBurst(entity, matrix, consumer, age, chaosPhase, partialTicks);

        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void renderStarLayer(Matrix4f matrix, VertexConsumer consumer, Vec3[] points,
                                        float age, float phase, float y, float width,
                                        int red, int green, int blue, int alpha) {
        for (int index = 0; index < STAR_ORDER.length - 1; index++) {
            renderCurvedConnection(matrix, consumer, points[STAR_ORDER[index]], points[STAR_ORDER[index + 1]],
                    age, phase, index, y, width, red, green, blue, alpha);
        }
    }

    private static void renderCurvedConnection(Matrix4f matrix, VertexConsumer consumer, Vec3 start, Vec3 end,
                                               float age, float phase, int path, float y, float width,
                                               int red, int green, int blue, int alpha) {
        Vec3 delta = end.subtract(start);
        Vec3 side = new Vec3(-delta.z, 0.0D, delta.x).normalize();
        Vec3 previous = curvedPoint(start, end, side, 0.0F, age, phase, path).add(0.0D, y, 0.0D);
        int sections = 7;
        for (int section = 1; section <= sections; section++) {
            float t = section / (float) sections;
            Vec3 current = curvedPoint(start, end, side, t, age, phase, path).add(0.0D, y, 0.0D);
            addGroundLine(matrix, consumer, previous, current, width, red, green, blue, alpha);
            previous = current;
        }
    }

    private static Vec3 curvedPoint(Vec3 start, Vec3 end, Vec3 side, float t,
                                    float age, float phase, int path) {
        float archStrength = 0.13F + 0.045F * Mth.sin(age * 0.031F + path * 1.71F + phase);
        float arch = Mth.sin((float) Math.PI * t) * archStrength;
        float ripple = Mth.sin(t * (float) Math.PI * 3.0F + age * 0.047F + path * 1.39F + phase) * 0.042F;
        float height = Mth.sin(t * (float) Math.PI * 4.0F + age * 0.038F + path) * 0.018F;
        return start.lerp(end, t).add(side.scale(arch + ripple)).add(0.0D, height, 0.0D);
    }

    private static void renderBrokenRing(Matrix4f matrix, VertexConsumer consumer, Vec3 center, float radius,
                                         int segments, float width, float age, float phase,
                                         int breakStride, int breakOffset,
                                         int red, int green, int blue, int alpha) {
        for (int segment = 0; segment < segments; segment++) {
            int breakIndex = Math.floorMod(segment + breakOffset, breakStride);
            if (breakIndex <= 1) {
                continue;
            }
            float angleA = (float) (Math.PI * 2.0D * segment / segments);
            float angleB = (float) (Math.PI * 2.0D * (segment + 1) / segments);
            Vec3 start = warpedRingPoint(center, radius, angleA, age, phase);
            Vec3 end = warpedRingPoint(center, radius, angleB, age, phase);
            addGroundLine(matrix, consumer, start, end, width, red, green, blue, alpha);
        }
    }

    private static Vec3 warpedRingPoint(Vec3 center, float radius, float angle, float age, float phase) {
        float warp = 1.0F
                + 0.055F * Mth.sin(angle * 3.0F + age * 0.014F + phase)
                + 0.026F * Mth.sin(angle * 7.0F - age * 0.021F + phase * 1.7F);
        float y = Mth.sin(angle * 5.0F + age * 0.040F + phase) * 0.024F;
        return new Vec3(
                center.x + Mth.cos(angle) * radius * warp * 1.025F,
                center.y + y,
                center.z + Mth.sin(angle) * radius * warp * 0.91F
        );
    }

    private static void renderVertexSeals(Matrix4f matrix, VertexConsumer consumer, Vec3[] points,
                                          float age, float phase, float fade) {
        for (int index = 0; index < points.length; index++) {
            float shimmer = 0.78F + 0.22F * Mth.sin(age * 0.13F + index * 1.7F + phase);
            float size = 0.125F + 0.026F * Mth.sin(age * 0.041F + index * 2.2F);
            addGroundDiamond(matrix, consumer, points[index].add(0.0D, 0.03D, 0.0D), size,
                    102, 211, 255, (int) (210.0F * fade * shimmer));
        }
    }

    private static void renderBurst(UraeusStormStarEntity entity, Matrix4f matrix, VertexConsumer consumer,
                                    float age, float phase, float partialTick) {
        float burst = entity.getBurstProgress(partialTick);
        if (burst >= 1.0F) {
            return;
        }
        float eased = 1.0F - (1.0F - burst) * (1.0F - burst);
        float fade = 1.0F - burst;
        Vec3 center = new Vec3(
                Mth.sin(age * 0.023F + phase) * 0.13F,
                0.04D,
                Mth.cos(age * 0.019F + phase * 1.3F) * 0.11F
        );
        renderBrokenRing(matrix, consumer, center, 1.0F + eased * 5.2F,
                40, 0.105F * fade + 0.025F, age * 1.7F, phase + 4.0F,
                10, 2, 76, 190, 255, (int) (220.0F * fade));
        renderBrokenRing(matrix, consumer, center.add(0.16D, 0.01D, -0.09D), 0.45F + eased * 3.65F,
                30, 0.052F, age * 1.4F, phase + 5.9F,
                8, 4, 170, 92, 255, (int) (145.0F * fade));

        int rays = 11;
        for (int index = 0; index < rays; index++) {
            float angle = (float) (Math.PI * 2.0D * index / rays)
                    + Mth.sin(index * 2.71F + phase) * 0.13F;
            float length = (0.65F + eased * 4.2F) * (0.82F + 0.23F * Mth.sin(index * 1.91F + phase));
            float width = 0.12F * fade + 0.02F;
            Vec3 start = center.add(Mth.cos(angle) * eased * 0.35F, 0.0D, Mth.sin(angle) * eased * 0.35F);
            Vec3 end = center.add(Mth.cos(angle) * length, 0.0D, Mth.sin(angle) * length);
            addGroundLine(matrix, consumer, start, end, width, 115, 218, 255, (int) (180.0F * fade));
        }
    }

    private static void addGroundLine(Matrix4f matrix, VertexConsumer consumer, Vec3 start, Vec3 end,
                                      float width, int red, int green, int blue, int alpha) {
        Vec3 direction = end.subtract(start);
        Vec3 side = new Vec3(-direction.z, 0.0D, direction.x);
        if (side.lengthSqr() < 1.0E-6D) {
            return;
        }
        side = side.normalize().scale(width * 0.5F);
        Vec3 a = start.add(side);
        Vec3 b = start.subtract(side);
        Vec3 c = end.subtract(side);
        Vec3 d = end.add(side);
        vertex(consumer, matrix, a, red, green, blue, alpha);
        vertex(consumer, matrix, b, red, green, blue, alpha);
        vertex(consumer, matrix, c, red, green, blue, alpha);
        vertex(consumer, matrix, d, red, green, blue, alpha);
    }

    private static void addGroundDiamond(Matrix4f matrix, VertexConsumer consumer, Vec3 center, float size,
                                         int red, int green, int blue, int alpha) {
        vertex(consumer, matrix, center.add(0.0D, 0.0D, -size), red, green, blue, alpha);
        vertex(consumer, matrix, center.add(size, 0.0D, 0.0D), red, green, blue, alpha);
        vertex(consumer, matrix, center.add(0.0D, 0.0D, size), red, green, blue, alpha);
        vertex(consumer, matrix, center.add(-size, 0.0D, 0.0D), red, green, blue, alpha);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Vec3 point,
                               int red, int green, int blue, int alpha) {
        consumer.addVertex(matrix, (float) point.x, (float) point.y, (float) point.z)
                .setColor(red, green, blue, Mth.clamp(alpha, 0, 255));
    }

    @Override
    public ResourceLocation getTextureLocation(UraeusStormStarEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
