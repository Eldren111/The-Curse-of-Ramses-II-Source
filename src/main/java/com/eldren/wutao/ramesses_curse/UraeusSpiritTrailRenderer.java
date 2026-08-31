package com.eldren.wutao.ramesses_curse;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

final class UraeusSpiritTrailRenderer {
    private static final int AURA = 0;
    private static final int BODY = 1;
    private static final int CORE = 2;
    private static final float[] DROPLET_AXIAL = {0.27F, 0.245F, 0.20F, 0.12F, 0.02F, -0.10F, -0.23F, -0.36F, -0.50F};
    private static final float[] DROPLET_RADIUS = {0.0F, 0.052F, 0.105F, 0.145F, 0.162F, 0.151F, 0.118F, 0.070F, 0.0F};

    private UraeusSpiritTrailRenderer() {
    }

    static void render(PoseStack.Pose pose, VertexConsumer consumer, List<Vec3> points,
                       Vec3 origin, Vec3 cameraPosition, float age, boolean storm, float scale,
                       int seed) {
        if (points.size() < 3) {
            return;
        }

        float flowPhase = seed * 0.071F;
        renderRibbon(pose, consumer, points, origin, cameraPosition, age, storm,
                0.20D * scale, 48, 0.030D * scale, flowPhase, AURA);
        renderRibbon(pose, consumer, points, origin, cameraPosition, age, storm,
                0.082D * scale, 142, 0.025D * scale, flowPhase + 0.38F, BODY);
        renderRibbon(pose, consumer, points, origin, cameraPosition, age, storm,
                0.024D * scale, 245, 0.017D * scale, flowPhase + 0.72F, CORE);

        if (scale >= 0.80F) {
            renderFilaments(pose, consumer, points, origin, cameraPosition, age, storm, scale, flowPhase);
        }
        renderMotes(pose, consumer, points, origin, cameraPosition, age, storm, scale, seed);
    }

    static void renderDroplet(PoseStack.Pose pose, VertexConsumer consumer, Vec3 localCenter,
                              Vec3 worldCenter, Vec3 velocity, Vec3 cameraPosition, float age,
                              boolean storm, float scale, boolean renderOrbiters) {
        HeadFrame frame = headFrame(velocity, cameraPosition.subtract(worldCenter));
        Matrix4f matrix = pose.pose();
        float breath = 1.0F + 0.025F * Mth.sin(age * 0.21F);
        float phase = age * 0.16F;

        if (storm) {
            addDiamond(matrix, consumer, localCenter, frame.screenRight(), frame.screenUp(),
                    0.23F * scale, 38, 168, 255, 34);
            renderDropletShell(matrix, consumer, localCenter, frame, scale * breath,
                    age, phase, 0.070F, 112, 226, 255, 138);
            renderDropletShell(matrix, consumer, localCenter, frame, scale * 0.48F,
                    age, phase + 1.2F, 0.025F, 232, 255, 255, 245);
        } else {
            addDiamond(matrix, consumer, localCenter, frame.screenRight(), frame.screenUp(),
                    0.23F * scale, 255, 128, 18, 34);
            renderDropletShell(matrix, consumer, localCenter, frame, scale * breath,
                    age, phase, 0.070F, 255, 204, 68, 138);
            renderDropletShell(matrix, consumer, localCenter, frame, scale * 0.48F,
                    age, phase + 1.2F, 0.025F, 255, 252, 214, 245);
        }

        renderFlowVeins(matrix, consumer, localCenter, frame, age, phase, storm, scale,
                renderOrbiters ? 3 : 1);
        renderLivingCore(matrix, consumer, localCenter, frame, age, storm, scale);

        if (renderOrbiters) {
            renderOrbit(matrix, consumer, localCenter, frame, age, scale);
        }
    }

    private static void renderDropletShell(Matrix4f matrix, VertexConsumer consumer, Vec3 center,
                                           HeadFrame frame, float scale, float age, float phase,
                                           float distortion, int red, int green, int blue, int alpha) {
        int segments = 12;
        for (int section = 0; section < DROPLET_AXIAL.length - 1; section++) {
            for (int segment = 0; segment < segments; segment++) {
                float angle0 = Mth.TWO_PI * segment / segments;
                float angle1 = Mth.TWO_PI * (segment + 1) / segments;
                Vec3 first = dropletPoint(center, frame, DROPLET_AXIAL[section], DROPLET_RADIUS[section],
                        angle0, scale, age, phase, distortion);
                Vec3 second = dropletPoint(center, frame, DROPLET_AXIAL[section + 1], DROPLET_RADIUS[section + 1],
                        angle0, scale, age, phase, distortion);
                Vec3 third = dropletPoint(center, frame, DROPLET_AXIAL[section + 1], DROPLET_RADIUS[section + 1],
                        angle1, scale, age, phase, distortion);
                Vec3 fourth = dropletPoint(center, frame, DROPLET_AXIAL[section], DROPLET_RADIUS[section],
                        angle1, scale, age, phase, distortion);
                vertex(consumer, matrix, first, red, green, blue, section == 0 ? 0 : alpha);
                vertex(consumer, matrix, second, red, green, blue, alpha);
                vertex(consumer, matrix, third, red, green, blue, alpha);
                vertex(consumer, matrix, fourth, red, green, blue, section == 0 ? 0 : alpha);
            }
        }
    }

    private static Vec3 dropletPoint(Vec3 center, HeadFrame frame, float axial, float radius,
                                     float angle, float scale, float age, float phase, float distortion) {
        float surfaceFlow = 1.0F + distortion
                * Mth.sin(angle * 3.0F + axial * 15.0F - age * 0.19F + phase);
        float curl = distortion * 0.055F
                * Mth.sin(axial * 11.0F - age * 0.13F + phase);
        return center
                .add(frame.forward().scale(axial * scale))
                .add(frame.side().scale((Mth.cos(angle) * radius * surfaceFlow + curl) * scale))
                .add(frame.up().scale(Mth.sin(angle) * radius * surfaceFlow * scale));
    }

    private static void renderFlowVeins(Matrix4f matrix, VertexConsumer consumer, Vec3 center,
                                        HeadFrame frame, float age, float phase, boolean storm,
                                        float scale, int veinCount) {
        int red = storm ? 190 : 255;
        int green = storm ? 246 : 235;
        int blue = storm ? 255 : 146;
        for (int vein = 0; vein < veinCount; vein++) {
            Vec3 previous = null;
            for (int section = 1; section < DROPLET_AXIAL.length - 1; section++) {
                float angle = vein * Mth.TWO_PI / 3.0F
                        + section * 0.64F - age * 0.20F + phase;
                Vec3 current = dropletPoint(center, frame, DROPLET_AXIAL[section],
                        DROPLET_RADIUS[section] * 1.035F, angle, scale,
                        age, phase + vein, 0.055F);
                if (previous != null) {
                    float pulse = 0.62F + 0.38F
                            * Mth.sin(age * 0.34F - section * 0.95F + vein * 2.1F);
                    addBeam(matrix, consumer, previous, current, frame.cameraDirection(),
                            0.0055F * scale, red, green, blue,
                            Mth.floor(155.0F * pulse), Mth.floor(205.0F * pulse));
                }
                previous = current;
            }
        }
    }

    private static void renderLivingCore(Matrix4f matrix, VertexConsumer consumer, Vec3 center,
                                         HeadFrame frame, float age, boolean storm, float scale) {
        float pulse = 0.82F + 0.18F * Mth.sin(age * 0.47F);
        Vec3 core = center.add(frame.forward().scale(0.015F * scale))
                .add(frame.up().scale(Mth.sin(age * 0.31F) * 0.012F * scale));
        addDiamond(matrix, consumer, core, frame.screenRight(), frame.screenUp(),
                0.105F * scale * pulse,
                storm ? 78 : 255, storm ? 202 : 177, storm ? 255 : 42, 48);
        addDiamond(matrix, consumer, core, frame.screenRight(), frame.screenUp(),
                0.043F * scale * pulse, 244, 255, storm ? 255 : 225, 235);
    }

    private static void renderOrbit(Matrix4f matrix, VertexConsumer consumer, Vec3 center,
                                    HeadFrame frame, float age, float scale) {
        int segments = 20;
        float radius = 0.245F * scale;
        float tilt = 0.42F + 0.08F * Mth.sin(age * 0.09F);
        for (int segment = 0; segment < segments; segment++) {
            float angle0 = Mth.TWO_PI * segment / segments + age * 0.18F;
            float angle1 = Mth.TWO_PI * (segment + 1) / segments + age * 0.18F;
            Vec3 current = orbitPoint(center, frame, angle0, radius, tilt);
            Vec3 next = orbitPoint(center, frame, angle1, radius, tilt);
            Vec3 tangent = next.subtract(current);
            Vec3 facingSide = tangent.cross(frame.cameraDirection());
            if (facingSide.lengthSqr() < 1.0E-7D) {
                facingSide = frame.forward();
            } else {
                facingSide = facingSide.normalize();
            }
            Vec3 width = facingSide.scale(0.006F * scale);
            addQuad(matrix, consumer, current, next, width, width, 72, 207, 255, 34, 34);
        }

        for (int orb = 0; orb < 2; orb++) {
            float phase = age * 0.46F + orb * Mth.PI;
            Vec3 position = orbitPoint(center, frame, phase, radius, tilt);
            float pulse = 0.82F + 0.18F * Mth.sin(age * 0.83F + orb * 2.1F);
            addDiamond(matrix, consumer, position, frame.screenRight(), frame.screenUp(),
                    0.090F * scale * pulse, 36, 150, 255, 62);
            addDiamond(matrix, consumer, position, frame.screenRight(), frame.screenUp(),
                    0.048F * scale * pulse, 142, 232, 255, 225);
            addDiamond(matrix, consumer, position, frame.screenRight(), frame.screenUp(),
                    0.018F * scale, 240, 255, 255, 255);

            Vec3 arcEnd = center.add(frame.forward().scale(0.035F * scale));
            Vec3 midpoint = position.lerp(arcEnd, 0.52D)
                    .add(frame.screenUp().scale(Mth.sin(age * 1.7F + orb) * 0.025F * scale));
            addBeam(matrix, consumer, position, midpoint, frame.cameraDirection(), 0.007F * scale,
                    88, 205, 255, 95, 52);
            addBeam(matrix, consumer, midpoint, arcEnd, frame.cameraDirection(), 0.006F * scale,
                    142, 232, 255, 52, 0);
        }
    }

    private static Vec3 orbitPoint(Vec3 center, HeadFrame frame, float angle, float radius, float tilt) {
        Vec3 radial = frame.side().scale(Mth.cos(angle) * radius)
                .add(frame.up().scale(Mth.sin(angle) * radius));
        return center.add(radial).add(frame.forward().scale(Mth.sin(angle) * radius * tilt));
    }

    private static void addBeam(Matrix4f matrix, VertexConsumer consumer, Vec3 start, Vec3 end,
                                Vec3 cameraDirection, float width, int red, int green, int blue,
                                int startAlpha, int endAlpha) {
        Vec3 side = end.subtract(start).cross(cameraDirection);
        if (side.lengthSqr() < 1.0E-7D) {
            side = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            side = side.normalize();
        }
        Vec3 offset = side.scale(width);
        addQuad(matrix, consumer, start, end, offset, offset,
                red, green, blue, startAlpha, endAlpha);
    }

    private static HeadFrame headFrame(Vec3 velocity, Vec3 cameraDirection) {
        Vec3 forward = velocity.lengthSqr() < 1.0E-7D
                ? new Vec3(0.0D, 0.0D, 1.0D)
                : velocity.normalize();
        Vec3 side = forward.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (side.lengthSqr() < 1.0E-7D) {
            side = forward.cross(new Vec3(1.0D, 0.0D, 0.0D));
        }
        side = side.normalize();
        Vec3 up = side.cross(forward).normalize();
        Vec3 camera = cameraDirection.lengthSqr() < 1.0E-7D
                ? new Vec3(0.0D, 0.0D, 1.0D)
                : cameraDirection.normalize();
        Vec3 screenRight = camera.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (screenRight.lengthSqr() < 1.0E-7D) {
            screenRight = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            screenRight = screenRight.normalize();
        }
        Vec3 screenUp = screenRight.cross(camera).normalize();
        return new HeadFrame(forward, side, up, camera, screenRight, screenUp);
    }

    private static void renderRibbon(PoseStack.Pose pose, VertexConsumer consumer, List<Vec3> points,
                                     Vec3 origin, Vec3 cameraPosition, float age, boolean storm,
                                     double baseWidth, int maxAlpha, double offsetAmplitude,
                                     float phaseOffset, int layer) {
        int last = points.size() - 1;
        for (int index = 0; index < last; index++) {
            float progress = index / (float) last;
            float nextProgress = (index + 1) / (float) last;
            Frame currentFrame = frame(points, index, cameraPosition);
            Frame nextFrame = frame(points, index + 1, cameraPosition);

            Vec3 current = displacedPoint(points.get(index), currentFrame, progress, age,
                    offsetAmplitude, phaseOffset).subtract(origin);
            Vec3 next = displacedPoint(points.get(index + 1), nextFrame, nextProgress, age,
                    offsetAmplitude, phaseOffset).subtract(origin);
            double currentFade = Math.pow(1.0D - progress, layer == AURA ? 0.62D : 0.82D);
            double nextFade = Math.pow(1.0D - nextProgress, layer == AURA ? 0.62D : 0.82D);
            float currentKnot = 0.82F + 0.18F
                    * Mth.sin(age * 0.19F - progress * 17.0F + phaseOffset + layer * 0.83F);
            float nextKnot = 0.82F + 0.18F
                    * Mth.sin(age * 0.19F - nextProgress * 17.0F + phaseOffset + layer * 0.83F);
            Vec3 currentSide = currentFrame.side().scale(baseWidth * currentFade * currentKnot);
            Vec3 nextSide = nextFrame.side().scale(baseWidth * nextFade * nextKnot);

            float pulse = 0.72F + 0.28F * Mth.sin(age * 0.27F - progress * 12.0F + phaseOffset);
            int[] color = color(layer, storm, pulse);
            float alphaPower = layer == AURA ? 1.8F : 1.15F;
            int alpha = Mth.clamp(Mth.floor(maxAlpha * pulse * Math.pow(1.0F - progress, alphaPower)), 0, maxAlpha);
            int nextAlpha = Mth.clamp(Mth.floor(maxAlpha * pulse * Math.pow(1.0F - nextProgress, alphaPower)), 0, maxAlpha);
            addQuad(pose.pose(), consumer, current, next, currentSide, nextSide,
                    color[0], color[1], color[2], alpha, nextAlpha);
        }
    }

    private static void renderFilaments(PoseStack.Pose pose, VertexConsumer consumer, List<Vec3> points,
                                        Vec3 origin, Vec3 cameraPosition, float age, boolean storm,
                                        float scale, float phaseOffset) {
        int last = points.size() - 1;
        if (last < 4) {
            return;
        }
        for (int filament = 0; filament < 2; filament++) {
            Vec3 previous = null;
            Frame previousFrame = null;
            for (int index = 0; index <= last; index++) {
                float progress = index / (float) last;
                Frame pointFrame = frame(points, index, cameraPosition);
                float envelope = Mth.sin(Mth.PI * progress);
                float phase = age * (0.31F + filament * 0.035F)
                        - progress * (12.0F + filament * 2.0F)
                        + phaseOffset + filament * Mth.PI;
                double reach = (0.055D + 0.105D * progress) * envelope * scale;
                Vec3 current = points.get(index)
                        .add(pointFrame.side().scale(Mth.sin(phase) * reach))
                        .add(pointFrame.screenUp().scale(Mth.cos(phase * 0.73F) * reach * 0.68D))
                        .subtract(origin);
                if (previous != null && previousFrame != null) {
                    float fade = (1.0F - progress) * envelope;
                    float previousProgress = (index - 1) / (float) last;
                    float previousFade = (1.0F - previousProgress) * Mth.sin(Mth.PI * previousProgress);
                    Vec3 previousSide = previousFrame.side().scale(0.012D * scale * previousFade);
                    Vec3 currentSide = pointFrame.side().scale(0.012D * scale * fade);
                    int alpha = Mth.clamp(Mth.floor(155.0F * previousFade), 0, 155);
                    int nextAlpha = Mth.clamp(Mth.floor(155.0F * fade), 0, 155);
                    addQuad(pose.pose(), consumer, previous, current, previousSide, currentSide,
                            storm ? 118 : 255, storm ? 226 : 207, storm ? 255 : 78,
                            alpha, nextAlpha);
                }
                previous = current;
                previousFrame = pointFrame;
            }
        }
    }

    private static Vec3 displacedPoint(Vec3 point, Frame frame, float progress, float age,
                                       double amplitude, float phaseOffset) {
        float envelope = Mth.sin(Mth.PI * Mth.clamp(progress, 0.0F, 1.0F));
        float phase = age * 0.42F - progress * 18.0F + phaseOffset;
        double sideOffset = Mth.sin(phase) * amplitude * envelope;
        double verticalOffset = Mth.cos(phase) * amplitude * envelope * 0.62D;
        return point.add(frame.side().scale(sideOffset)).add(frame.screenUp().scale(verticalOffset));
    }

    private static void renderMotes(PoseStack.Pose pose, VertexConsumer consumer, List<Vec3> points,
                                    Vec3 origin, Vec3 cameraPosition, float age, boolean storm,
                                    float scale, int seed) {
        int last = points.size() - 1;
        int count = Math.max(1, Mth.floor(3.0F * scale));
        for (int mote = 0; mote < count; mote++) {
            float travel = Mth.frac(age * 0.026F + seed * 0.071F + mote / (float) count);
            int index = Mth.clamp(Mth.floor(travel * last), 0, last);
            float progress = index / (float) last;
            Frame pointFrame = frame(points, index, cameraPosition);
            float orbit = age * 0.51F + mote * 2.399F;
            double orbitRadius = (0.065D + 0.055D * Mth.sin(orbit * 0.7F)) * scale;
            Vec3 center = points.get(index)
                    .add(pointFrame.side().scale(Mth.sin(orbit) * orbitRadius))
                    .add(pointFrame.screenUp().scale(Mth.cos(orbit) * orbitRadius))
                    .subtract(origin);
            float fade = Mth.sin(Mth.PI * travel) * (1.0F - progress);
            float size = (0.016F + 0.012F * Mth.sin(orbit * 1.3F)) * scale;
            int alpha = Mth.clamp(Mth.floor(205.0F * fade), 0, 205);
            int red = storm && (mote & 1) == 1 ? 128 : 255;
            int green = storm && (mote & 1) == 1 ? 242 : 224;
            int blue = storm && (mote & 1) == 1 ? 255 : 118;
            addDiamond(pose.pose(), consumer, center, pointFrame.side(), pointFrame.screenUp(),
                    size, red, green, blue, alpha);
        }
    }

    private static Frame frame(List<Vec3> points, int index, Vec3 cameraPosition) {
        int last = points.size() - 1;
        Vec3 point = points.get(index);
        Vec3 tangent = points.get(Math.min(last, index + 1)).subtract(points.get(Math.max(0, index - 1)));
        if (tangent.lengthSqr() < 1.0E-7D) {
            tangent = new Vec3(0.0D, 0.0D, 1.0D);
        }
        tangent = tangent.normalize();
        Vec3 toCamera = cameraPosition.subtract(point);
        if (toCamera.lengthSqr() < 1.0E-7D) {
            toCamera = new Vec3(0.0D, 1.0D, 0.0D);
        }
        toCamera = toCamera.normalize();
        Vec3 side = tangent.cross(toCamera);
        if (side.lengthSqr() < 1.0E-7D) {
            side = tangent.cross(new Vec3(0.0D, 1.0D, 0.0D));
        }
        if (side.lengthSqr() < 1.0E-7D) {
            side = new Vec3(1.0D, 0.0D, 0.0D);
        }
        side = side.normalize();
        Vec3 screenUp = toCamera.cross(side);
        if (screenUp.lengthSqr() < 1.0E-7D) {
            screenUp = new Vec3(0.0D, 1.0D, 0.0D);
        } else {
            screenUp = screenUp.normalize();
        }
        return new Frame(side, screenUp);
    }

    private static int[] color(int layer, boolean storm, float pulse) {
        if (storm) {
            return switch (layer) {
                case AURA -> new int[]{50, Mth.floor(Mth.lerp(pulse, 172.0F, 218.0F)), 255};
                case BODY -> new int[]{126, Mth.floor(Mth.lerp(pulse, 226.0F, 252.0F)), 255};
                default -> new int[]{232, 255, 255};
            };
        }
        return switch (layer) {
            case AURA -> new int[]{255, Mth.floor(Mth.lerp(pulse, 126.0F, 171.0F)), 22};
            case BODY -> new int[]{255, Mth.floor(Mth.lerp(pulse, 194.0F, 228.0F)), 72};
            default -> new int[]{255, 252, 210};
        };
    }

    private static void addQuad(Matrix4f matrix, VertexConsumer consumer, Vec3 current, Vec3 next,
                                Vec3 currentSide, Vec3 nextSide, int red, int green, int blue,
                                int alpha, int nextAlpha) {
        vertex(consumer, matrix, current.subtract(currentSide), red, green, blue, alpha);
        vertex(consumer, matrix, next.subtract(nextSide), red, green, blue, nextAlpha);
        vertex(consumer, matrix, next.add(nextSide), red, green, blue, nextAlpha);
        vertex(consumer, matrix, current.add(currentSide), red, green, blue, alpha);
    }

    private static void addDiamond(Matrix4f matrix, VertexConsumer consumer, Vec3 center,
                                   Vec3 side, Vec3 up, float size, int red, int green, int blue, int alpha) {
        vertex(consumer, matrix, center.add(up.scale(size)), red, green, blue, 0);
        vertex(consumer, matrix, center.add(side.scale(size)), red, green, blue, alpha);
        vertex(consumer, matrix, center.subtract(up.scale(size)), red, green, blue, 0);
        vertex(consumer, matrix, center.subtract(side.scale(size)), red, green, blue, alpha);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Vec3 position,
                               int red, int green, int blue, int alpha) {
        consumer.vertex(matrix, (float) position.x, (float) position.y, (float) position.z)
                .color(red, green, blue, alpha)
                .endVertex();
    }

    private record Frame(Vec3 side, Vec3 screenUp) {
    }

    private record HeadFrame(Vec3 forward, Vec3 side, Vec3 up, Vec3 cameraDirection,
                             Vec3 screenRight, Vec3 screenUp) {
    }
}
