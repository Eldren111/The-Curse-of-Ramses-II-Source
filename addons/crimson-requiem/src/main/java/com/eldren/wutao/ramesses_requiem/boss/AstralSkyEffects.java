package com.eldren.wutao.ramesses_requiem.boss;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import java.util.*;

/** Original procedural sky: no external mod calls, copied shaders, textures or framebuffer passes. */
public final class AstralSkyEffects extends DimensionSpecialEffects {
    private record Star(Vec3 center, Vec3 right, Vec3 up, int shade, float phase) {}
    private final List<Star> stars = new ArrayList<>();
    public AstralSkyEffects() {
        super(Float.NaN, false, SkyType.NONE, true, false);
        Random random = new Random(0x5755534845L);
        for (int i = 0; i < 1000; i++) {
            Vec3 direction = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize();
            Vec3 right = direction.cross(new Vec3(0, 1, 0)).normalize().scale(.04 + random.nextDouble() * .12);
            Vec3 up = direction.cross(right).normalize().scale(right.length());
            stars.add(new Star(direction.scale(90), right, up, 150 + random.nextInt(106), random.nextFloat() * 6.28F));
        }
    }
    @Override public Vec3 getBrightnessDependentFogColor(Vec3 color, float brightness) { return new Vec3(.012, .008, .035); }
    @Override public boolean isFoggyAt(int x, int y) { return false; }
    @Override public float[] getSunriseColor(float time, float partialTick) { return null; }
    @Override public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack pose, Camera camera, Matrix4f projection, boolean foggy, Runnable setupFog) {
        float time = (level.getGameTime() % 240000 + partialTick);
        pose.pushPose();
        try {
            pose.mulPose(Axis.YP.rotationDegrees(time * .002F));
            RenderSystem.depthMask(false); RenderSystem.disableDepthTest(); RenderSystem.disableCull();
            RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionColorShader); RenderSystem.setShaderColor(1, 1, 1, 1);
            FogRenderer.setupNoFog();
            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            Matrix4f matrix = pose.last().pose();
            // Layered, tilted violet nebula ribbon with soft translucent edges.
            for (int i = 0; i < 180; i++) {
                double a = i * Math.PI * 2 / 180;
                Vec3 center = new Vec3(Math.cos(a) * 85, Math.sin(a) * 32, Math.sin(a) * 78);
                Vec3 tangent = new Vec3(-Math.sin(a), .37 * Math.cos(a), .92 * Math.cos(a)).normalize();
                Vec3 across = center.normalize().cross(tangent).normalize();
                for (int layer = 4; layer >= 1; layer--)
                    quad(buffer, matrix, center, tangent.scale(1.8), across.scale(layer * 1.6), 53 + i % 32, 18, 120 + i % 40, 8 + (5 - layer) * 3);
            }
            for (Star star : stars) {
                int alpha = (int)(star.shade * (.78 + .22 * Math.sin(time * .025 + star.phase)));
                quad(buffer, matrix, star.center, star.right, star.up, 205, 205, 255, alpha);
            }
            BufferUploader.drawWithShader(buffer.end());
        } finally {
            pose.popPose(); RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.depthMask(true); RenderSystem.enableDepthTest(); RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc(); RenderSystem.disableBlend(); setupFog.run();
        }
        return true;
    }
    private static void quad(BufferBuilder b, Matrix4f matrix, Vec3 center, Vec3 right, Vec3 up, int r, int g, int blue, int a) {
        for (int[] sign : new int[][]{{-1,-1},{-1,1},{1,1},{1,-1}}) {
            Vec3 p = center.add(right.scale(sign[0])).add(up.scale(sign[1]));
            b.vertex(matrix, (float)p.x, (float)p.y, (float)p.z).color(r, g, blue, a).endVertex();
        }
    }
}
