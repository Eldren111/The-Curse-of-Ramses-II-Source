package com.eldren.wutao.ramesses_curse;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

final class EdictVfxRenderer {
    private static final int[] STAR_ORDER = {0, 2, 4, 1, 3, 0};

    private EdictVfxRenderer() {
    }

    static void renderQuads(BufferBuilder buffer, Matrix4f matrix,
                            double x, double y, double z, float baseRadius,
                            int durationTicks, int effectAge, boolean domain,
                            float partialTick, Vector3f cameraLeft, Vector3f cameraUp) {
        float age = Math.min(durationTicks, effectAge + partialTick);
        float life = Mth.clamp(age / Math.max(1.0F, durationTicks), 0.0F, 1.0F);
        float appear = smoothStep(Mth.clamp(age / 9.0F, 0.0F, 1.0F));
        float fade = 1.0F - smoothStep(Mth.clamp((life - 0.82F) / 0.18F, 0.0F, 1.0F));
        float intensity = appear * fade;
        if (intensity <= 0.001F) {
            return;
        }

        float pulse = 0.93F + 0.07F * Mth.sin(age * 0.18F);
        float radius = baseRadius * pulse;
        Vec3 center = new Vec3(x, y + 0.12D, z);

        drawTubeRing(buffer, matrix, center.add(0.0D, radius * 0.08D, 0.0D),
                radius, age * 0.010F, 0.18F, 0.20F,
                36, tube(radius, 0.013F, 0.055F, 0.20F), 10, 2,
                255, 150, 34, alpha(72.0F, intensity), 4);
        drawTubeRing(buffer, matrix, center.add(0.0D, radius * 0.105D, 0.0D),
                radius * 0.985F, -age * 0.014F, -0.24F, -0.42F,
                34, tube(radius, 0.006F, 0.032F, 0.11F), 12, 5,
                255, 236, 154, alpha(226.0F, intensity), 4);
        drawTubeRing(buffer, matrix, center.add(0.0D, radius * 0.17D, 0.0D),
                radius * 0.70F, age * 0.018F, 0.46F, 0.78F,
                30, tube(radius, 0.005F, 0.028F, 0.085F), 9, 1,
                65, 220, 255, alpha(188.0F, intensity), 4);

        drawRoyalPentagram(buffer, matrix, center, radius * 0.74F, age, intensity, domain);
        drawShockwaves(buffer, matrix, center, radius, age, intensity);

        float haloHeight = radius * (domain ? 0.30F : 0.24F);
        drawTubeRing(buffer, matrix, center.add(0.0D, haloHeight, 0.0D),
                radius * 0.78F, age * 0.020F, 0.56F, 0.30F,
                32, tube(radius, 0.007F, 0.040F, 0.13F), 9, 1,
                255, 186, 58, alpha(178.0F, intensity), 5);
        drawTubeRing(buffer, matrix, center.add(0.0D, haloHeight * 1.62F, 0.0D),
                radius * 0.58F, -age * 0.029F + 1.7F, -0.68F, -0.76F,
                30, tube(radius, 0.006F, 0.033F, 0.10F), 8, 4,
                72, 225, 255, alpha(196.0F, intensity), 5);
        if (domain) {
            drawTubeRing(buffer, matrix, center.add(0.0D, haloHeight * 2.25F, 0.0D),
                    radius * 0.40F, age * 0.041F + 2.4F, 0.84F, 1.16F,
                    26, tube(radius, 0.005F, 0.026F, 0.075F), 7, 2,
                    232, 248, 255, alpha(170.0F, intensity), 5);
            drawVolumetricDome(buffer, matrix, center, radius, age, intensity);
        }

        drawCrownSpikes(buffer, matrix, center, radius, age, intensity, domain);
        drawEnergyNodes(buffer, matrix, center.add(0.0D, haloHeight, 0.0D),
                radius, age, intensity, domain);
    }

    private static void drawRoyalPentagram(BufferBuilder buffer, Matrix4f matrix,
                                            Vec3 center, float radius, float age,
                                            float intensity, boolean domain) {
        Vec3 outerCenter = center.add(0.0D, radius * (domain ? 0.42D : 0.34D), 0.0D);
        Vec3[] outer = starPoints3d(
                outerCenter,
                radius,
                -Mth.HALF_PI + age * 0.012F,
                0.52F + 0.05F * Mth.sin(age * 0.025F),
                age * 0.007F);
        float outerRadius = tube(radius, 0.020F, 0.075F, 0.23F);
        for (int edge = 0; edge < STAR_ORDER.length - 1; edge++) {
            Vec3 start = outer[STAR_ORDER[edge]];
            Vec3 end = outer[STAR_ORDER[edge + 1]];
            drawPrismBeam(buffer, matrix, start, end, outerRadius * 2.35F, 6,
                    255, 100, 18, alpha(34.0F, intensity));
            drawPrismBeam(buffer, matrix, start, end, outerRadius, 6,
                    255, 199, 62, alpha(186.0F, intensity));
            drawPrismBeam(buffer, matrix, start, end, outerRadius * 0.33F, 6,
                    255, 252, 224, alpha(252.0F, intensity));
        }

        Vec3 innerCenter = outerCenter.add(0.0D, radius * 0.10D, 0.0D);
        Vec3[] inner = starPoints3d(
                innerCenter,
                radius * 0.46F,
                Mth.HALF_PI - age * 0.018F,
                -0.43F,
                -age * 0.011F + 0.85F);
        float innerRadius = outerRadius * 0.70F;
        for (int edge = 0; edge < STAR_ORDER.length - 1; edge++) {
            Vec3 start = inner[STAR_ORDER[edge]];
            Vec3 end = inner[STAR_ORDER[edge + 1]];
            drawPrismBeam(buffer, matrix, start, end, innerRadius * 1.8F, 6,
                    28, 163, 255, alpha(48.0F, intensity));
            drawPrismBeam(buffer, matrix, start, end, innerRadius, 6,
                    106, 231, 255, alpha(206.0F, intensity));
            drawPrismBeam(buffer, matrix, start, end, innerRadius * 0.28F, 6,
                    232, 255, 255, alpha(245.0F, intensity));
        }

        for (int point = 0; point < outer.length; point++) {
            float flicker = 0.76F + 0.24F * Mth.sin(age * 0.31F + point * 1.8F);
            drawPrismBeam(buffer, matrix, outer[point], inner[(point + 2) % inner.length],
                    innerRadius * 0.42F, 4,
                    point % 2 == 0 ? 255 : 78,
                    point % 2 == 0 ? 218 : 224,
                    point % 2 == 0 ? 108 : 255,
                    alpha(132.0F * flicker, intensity));
            drawOctahedron(buffer, matrix, outer[point], outerRadius * (1.7F + flicker * 0.5F),
                    255, 234, 156, alpha(220.0F * flicker, intensity));
        }
        drawOctahedron(buffer, matrix, outerCenter, outerRadius * 3.4F,
                245, 252, 255, alpha(218.0F, intensity));
    }

    private static Vec3[] starPoints3d(Vec3 center, float radius, float rotation,
                                       float tilt, float yaw) {
        Vec3 axisU = new Vec3(Mth.cos(yaw), 0.0D, Mth.sin(yaw));
        Vec3 axisV = new Vec3(
                -Mth.sin(yaw) * Mth.cos(tilt),
                Mth.sin(tilt),
                Mth.cos(yaw) * Mth.cos(tilt));
        Vec3[] points = new Vec3[5];
        for (int point = 0; point < points.length; point++) {
            float angle = rotation + Mth.TWO_PI * point / points.length;
            points[point] = center
                    .add(axisU.scale(Mth.cos(angle) * radius))
                    .add(axisV.scale(Mth.sin(angle) * radius));
        }
        return points;
    }

    private static void drawShockwaves(BufferBuilder buffer, Matrix4f matrix,
                                       Vec3 center, float radius, float age, float intensity) {
        for (int wave = 0; wave < 2; wave++) {
            float progress = Mth.frac(age * 0.035F + wave * 0.5F);
            float waveFade = (1.0F - progress) * (1.0F - progress) * intensity;
            float waveRadius = radius * (0.14F + progress * 0.94F);
            drawTubeRing(buffer, matrix,
                    center.add(0.0D, 0.10D + wave * 0.07D, 0.0D),
                    waveRadius, age * 0.018F, 0.10F + wave * 0.12F, wave * 0.46F,
                    28, tube(radius, 0.006F, 0.025F, 0.10F) * (1.0F - progress * 0.55F),
                    10, wave * 4,
                    wave == 0 ? 255 : 64,
                    wave == 0 ? 212 : 224,
                    wave == 0 ? 105 : 255,
                    alpha(164.0F, waveFade), 4);
        }
    }

    private static void drawTubeRing(BufferBuilder buffer, Matrix4f matrix,
                                     Vec3 center, float radius, float rotation,
                                     float tilt, float yaw, int segments, float tubeRadius,
                                     int gapStride, int gapOffset,
                                     int red, int green, int blue, int alpha, int sides) {
        for (int segment = 0; segment < segments; segment++) {
            if (Math.floorMod(segment + gapOffset, gapStride) <= 1) {
                continue;
            }
            float angle0 = rotation + Mth.TWO_PI * segment / segments;
            float angle1 = rotation + Mth.TWO_PI * (segment + 1) / segments;
            Vec3 start = ringPoint(center, radius, angle0, tilt, yaw);
            Vec3 end = ringPoint(center, radius, angle1, tilt, yaw);
            drawPrismBeam(buffer, matrix, start, end, tubeRadius, sides,
                    red, green, blue, alpha);
        }
    }

    private static Vec3 ringPoint(Vec3 center, float radius, float angle,
                                  float tilt, float yaw) {
        float c = Mth.cos(angle);
        float s = Mth.sin(angle);
        float ux = Mth.cos(yaw);
        float uz = Mth.sin(yaw);
        float vx = -Mth.sin(yaw) * Mth.cos(tilt);
        float vy = Mth.sin(tilt);
        float vz = Mth.cos(yaw) * Mth.cos(tilt);
        float warp = 1.0F + 0.018F * Mth.sin(angle * 5.0F + yaw * 3.0F);
        return center.add(
                (ux * c + vx * s) * radius * warp,
                vy * s * radius,
                (uz * c + vz * s) * radius * warp);
    }

    private static void drawVolumetricDome(BufferBuilder buffer, Matrix4f matrix,
                                           Vec3 center, float radius, float age, float intensity) {
        float beamRadius = tube(radius, 0.0035F, 0.022F, 0.060F);
        int meridians = 9;
        int sections = 12;
        for (int meridian = 0; meridian < meridians; meridian++) {
            float azimuth = Mth.TWO_PI * meridian / meridians + age * 0.004F;
            for (int section = 0; section < sections; section++) {
                if ((section + meridian * 2) % 8 == 0) {
                    continue;
                }
                float t0 = Mth.HALF_PI * section / sections;
                float t1 = Mth.HALF_PI * (section + 1) / sections;
                Vec3 start = domePoint(center, radius, azimuth, t0, age);
                Vec3 end = domePoint(center, radius, azimuth, t1, age);
                drawPrismBeam(buffer, matrix, start, end, beamRadius, 4,
                        meridian % 2 == 0 ? 255 : 74,
                        meridian % 2 == 0 ? 202 : 228,
                        meridian % 2 == 0 ? 92 : 255,
                        alpha(100.0F, intensity));
            }
        }
    }

    private static Vec3 domePoint(Vec3 center, float radius, float azimuth,
                                  float elevation, float age) {
        float horizontal = Mth.cos(elevation) * radius
                * (1.0F + 0.012F * Mth.sin(age * 0.05F + elevation * 8.0F));
        return center.add(
                Mth.cos(azimuth) * horizontal,
                Mth.sin(elevation) * radius,
                Mth.sin(azimuth) * horizontal);
    }

    private static void drawCrownSpikes(BufferBuilder buffer, Matrix4f matrix,
                                        Vec3 center, float radius, float age,
                                        float intensity, boolean domain) {
        int count = domain ? 16 : 11;
        for (int spike = 0; spike < count; spike++) {
            float seed = pseudoRandom(spike * 73 + 19);
            float angle = Mth.TWO_PI * spike / count + age * (0.006F + seed * 0.006F);
            float distance = radius * (0.28F + seed * 0.62F);
            float progress = Mth.frac(age * (0.018F + seed * 0.016F) + seed);
            Vec3 start = center.add(
                    Mth.cos(angle) * distance,
                    progress * radius * (domain ? 0.42F : 0.24F),
                    Mth.sin(angle) * distance);
            Vec3 end = start.add(
                    Mth.cos(angle + 0.45F) * radius * 0.025F,
                    (0.42F + seed * 0.95F) * (domain ? 1.35F : 1.0F),
                    Mth.sin(angle + 0.45F) * radius * 0.025F);
            float startRadius = 0.035F + seed * 0.045F;
            drawTaperedPrism(buffer, matrix, start, end,
                    startRadius, startRadius * 0.12F, 4,
                    seed > 0.58F ? 66 : 255,
                    seed > 0.58F ? 220 : 207,
                    seed > 0.58F ? 255 : 84,
                    alpha(142.0F * (1.0F - progress), intensity));
        }
    }

    private static void drawEnergyNodes(BufferBuilder buffer, Matrix4f matrix,
                                        Vec3 center, float radius, float age,
                                        float intensity, boolean domain) {
        int count = domain ? 9 : 6;
        for (int node = 0; node < count; node++) {
            float seed = pseudoRandom(node * 41 + 7);
            float angle = Mth.TWO_PI * node / count + age * (0.024F + seed * 0.010F);
            float orbitRadius = radius * (0.48F + seed * 0.34F);
            Vec3 position = center.add(
                    Mth.cos(angle) * orbitRadius,
                    Mth.sin(angle * 2.0F + age * 0.035F) * radius * 0.10F,
                    Mth.sin(angle) * orbitRadius);
            float pulse = 0.72F + 0.28F * Mth.sin(age * 0.31F + node * 1.9F);
            float size = (0.075F + seed * 0.075F) * pulse;
            int red = seed > 0.52F ? 68 : 255;
            int green = seed > 0.52F ? 224 : 220;
            int blue = seed > 0.52F ? 255 : 108;
            drawOctahedron(buffer, matrix, position, size * 2.1F,
                    red, green, blue, alpha(42.0F * pulse, intensity));
            drawOctahedron(buffer, matrix, position, size,
                    red, green, blue, alpha(232.0F * pulse, intensity));
        }
    }

    private static void drawPrismBeam(BufferBuilder buffer, Matrix4f matrix,
                                      Vec3 start, Vec3 end, float radius, int sides,
                                      int red, int green, int blue, int alpha) {
        drawTaperedPrism(buffer, matrix, start, end, radius, radius,
                sides, red, green, blue, alpha);
    }

    private static void drawTaperedPrism(BufferBuilder buffer, Matrix4f matrix,
                                         Vec3 start, Vec3 end,
                                         float startRadius, float endRadius, int sides,
                                         int red, int green, int blue, int alpha) {
        Vec3 direction = end.subtract(start);
        if (direction.lengthSqr() < 1.0E-8D) {
            return;
        }
        direction = direction.normalize();
        Vec3 reference = Math.abs(direction.y) < 0.90D
                ? new Vec3(0.0D, 1.0D, 0.0D)
                : new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 axisU = direction.cross(reference).normalize();
        Vec3 axisV = direction.cross(axisU).normalize();

        for (int side = 0; side < sides; side++) {
            float angle0 = Mth.TWO_PI * side / sides;
            float angle1 = Mth.TWO_PI * (side + 1) / sides;
            Vec3 radial0 = axisU.scale(Mth.cos(angle0)).add(axisV.scale(Mth.sin(angle0)));
            Vec3 radial1 = axisU.scale(Mth.cos(angle1)).add(axisV.scale(Mth.sin(angle1)));
            float shade = 0.58F + 0.42F * (0.5F + 0.5F * Mth.sin(angle0 + 0.85F));
            int faceRed = Mth.clamp(Mth.floor(red * shade), 0, 255);
            int faceGreen = Mth.clamp(Mth.floor(green * shade), 0, 255);
            int faceBlue = Mth.clamp(Mth.floor(blue * shade), 0, 255);
            addQuad(buffer, matrix,
                    start.add(radial0.scale(startRadius)),
                    end.add(radial0.scale(endRadius)),
                    end.add(radial1.scale(endRadius)),
                    start.add(radial1.scale(startRadius)),
                    faceRed, faceGreen, faceBlue, alpha);
        }
    }

    private static void drawOctahedron(BufferBuilder buffer, Matrix4f matrix,
                                       Vec3 center, float size,
                                       int red, int green, int blue, int alpha) {
        Vec3 top = center.add(0.0D, size, 0.0D);
        Vec3 bottom = center.add(0.0D, -size, 0.0D);
        Vec3[] equator = {
                center.add(size, 0.0D, 0.0D),
                center.add(0.0D, 0.0D, size),
                center.add(-size, 0.0D, 0.0D),
                center.add(0.0D, 0.0D, -size)
        };
        for (int face = 0; face < equator.length; face++) {
            Vec3 current = equator[face];
            Vec3 next = equator[(face + 1) % equator.length];
            float shade = 0.62F + face * 0.09F;
            int faceRed = Mth.clamp(Mth.floor(red * shade), 0, 255);
            int faceGreen = Mth.clamp(Mth.floor(green * shade), 0, 255);
            int faceBlue = Mth.clamp(Mth.floor(blue * shade), 0, 255);
            addTriangleAsQuad(buffer, matrix, top, current, next,
                    faceRed, faceGreen, faceBlue, alpha);
            addTriangleAsQuad(buffer, matrix, bottom, next, current,
                    faceRed, faceGreen, faceBlue, alpha);
        }
    }

    private static float tube(float radius, float ratio, float minimum, float maximum) {
        return Mth.clamp(radius * ratio, minimum, maximum);
    }

    private static float smoothStep(float value) {
        return value * value * (3.0F - 2.0F * value);
    }

    private static int alpha(float value, float intensity) {
        return Mth.clamp(Mth.floor(value * intensity), 0, 255);
    }

    private static float pseudoRandom(int seed) {
        int mixed = seed * 1103515245 + 12345;
        mixed ^= mixed >>> 16;
        return (mixed & 0x7FFFFFFF) / (float) 0x7FFFFFFF;
    }

    private static void addTriangleAsQuad(BufferBuilder buffer, Matrix4f matrix,
                                          Vec3 first, Vec3 second, Vec3 third,
                                          int red, int green, int blue, int alpha) {
        vertex(buffer, matrix, first, red, green, blue, alpha);
        vertex(buffer, matrix, second, red, green, blue, alpha);
        vertex(buffer, matrix, third, red, green, blue, alpha);
        vertex(buffer, matrix, third, red, green, blue, alpha);
    }

    private static void addQuad(BufferBuilder buffer, Matrix4f matrix,
                                Vec3 first, Vec3 second, Vec3 third, Vec3 fourth,
                                int red, int green, int blue, int alpha) {
        vertex(buffer, matrix, first, red, green, blue, alpha);
        vertex(buffer, matrix, second, red, green, blue, alpha);
        vertex(buffer, matrix, third, red, green, blue, alpha);
        vertex(buffer, matrix, fourth, red, green, blue, alpha);
    }

    private static void vertex(BufferBuilder buffer, Matrix4f matrix, Vec3 point,
                               int red, int green, int blue, int alpha) {
        buffer.addVertex(matrix, (float) point.x, (float) point.y, (float) point.z)
                .setColor(red, green, blue, alpha);
    }
}
