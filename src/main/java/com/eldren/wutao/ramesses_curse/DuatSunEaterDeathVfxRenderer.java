package com.eldren.wutao.ramesses_curse;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID, value = Dist.CLIENT)
public final class DuatSunEaterDeathVfxRenderer {
    private static ClientLevel lastLevel;

    private DuatSunEaterDeathVfxRenderer() {
    }

    @SubscribeEvent
    public static void renderBlast(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != lastLevel) {
            DuatSunEaterDeathVfxState.clear();
            lastLevel = minecraft.level;
        }
        if (minecraft.level == null) {
            return;
        }
        long gameTime = minecraft.level.getGameTime();
        DuatSunEaterDeathVfxState.Blast blast = DuatSunEaterDeathVfxState.activeBlast(gameTime);
        if (blast == null) {
            return;
        }

        float progress = blast.progress(gameTime, event.getPartialTick());
        float radius = 1.0F + progress * DuatSunEaterDeathVfxState.MAX_RADIUS;
        float fade = 1.0F - progress * 0.72F;
        Vec3 camera = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        try {
            poseStack.translate(-camera.x, -camera.y, -camera.z);
            Matrix4f matrix = poseStack.last().pose();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            try {
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder buffer = tesselator.getBuilder();
                buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                drawSphere(buffer, matrix, blast.origin(), radius,
                        255, 70, 42, Mth.floor(220.0F * fade));
                drawCorona(buffer, matrix, blast.origin(), radius,
                        (gameTime + event.getPartialTick()) * 0.08F,
                        Mth.floor(235.0F * fade));
                tesselator.end();
            } finally {
                RenderSystem.depthMask(true);
                RenderSystem.enableCull();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableBlend();
            }
        } finally {
            poseStack.popPose();
        }
    }

    @SubscribeEvent
    public static void renderWhiteFlash(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        DuatSunEaterDeathVfxState.Blast blast =
                DuatSunEaterDeathVfxState.activeBlast(minecraft.level.getGameTime());
        if (blast == null) {
            return;
        }
        float ringingVolume = DuatSunEaterDeathVfxState.consumeRingingVolume(
                minecraft.level.getGameTime());
        if (ringingVolume > 0.0F && minecraft.player != null) {
            minecraft.player.playSound(SoundEvents.BEACON_AMBIENT, ringingVolume, 1.85F);
            minecraft.player.playSound(SoundEvents.AMETHYST_BLOCK_CHIME,
                    ringingVolume * 0.72F, 2.0F);
        }
        int alpha = blast.flashAlpha(minecraft.level.getGameTime(), minecraft.getFrameTime());
        if (alpha > 0) {
            event.getGuiGraphics().fill(0, 0,
                    minecraft.getWindow().getGuiScaledWidth(),
                    minecraft.getWindow().getGuiScaledHeight(),
                    alpha << 24 | 0xFFFFFF);
        }
    }

    private static void drawSphere(BufferBuilder buffer, Matrix4f matrix, Vec3 center, float radius,
                                   int red, int green, int blue, int alpha) {
        int latitudeSegments = 18;
        int longitudeSegments = 36;
        for (int latitude = 0; latitude < latitudeSegments; latitude++) {
            float phi0 = (float) (-Math.PI / 2.0D + Math.PI * latitude / latitudeSegments);
            float phi1 = (float) (-Math.PI / 2.0D + Math.PI * (latitude + 1) / latitudeSegments);
            for (int longitude = 0; longitude < longitudeSegments; longitude++) {
                float theta0 = (float) (Mth.TWO_PI * longitude / longitudeSegments);
                float theta1 = (float) (Mth.TWO_PI * (longitude + 1) / longitudeSegments);
                sphereVertex(buffer, matrix, center, radius, phi0, theta0, red, green, blue, alpha);
                sphereVertex(buffer, matrix, center, radius, phi0, theta1, red, green, blue, alpha);
                sphereVertex(buffer, matrix, center, radius, phi1, theta1, red, green, blue, alpha);
                sphereVertex(buffer, matrix, center, radius, phi1, theta0, red, green, blue, alpha);
            }
        }
    }

    private static void sphereVertex(BufferBuilder buffer, Matrix4f matrix, Vec3 center, float radius,
                                     float phi, float theta, int red, int green, int blue, int alpha) {
        float horizontal = Mth.cos(phi) * radius;
        buffer.vertex(matrix,
                        (float) center.x + horizontal * Mth.cos(theta),
                        (float) center.y + Mth.sin(phi) * radius,
                        (float) center.z + horizontal * Mth.sin(theta))
                .color(red, green, blue, alpha).endVertex();
    }

    private static void drawCorona(BufferBuilder buffer, Matrix4f matrix, Vec3 center,
                                   float radius, float phase, int alpha) {
        int rays = 40;
        double goldenAngle = Math.PI * (3.0D - Math.sqrt(5.0D));
        for (int index = 0; index < rays; index++) {
            double vertical = -0.92D + 1.84D * index / (rays - 1.0D);
            double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - vertical * vertical));
            double angle = index * goldenAngle + phase * (index % 2 == 0 ? 1.0D : -0.7D);
            Vec3 direction = new Vec3(Math.cos(angle) * horizontal, vertical,
                    Math.sin(angle) * horizontal).normalize();
            Vec3 tangent = direction.cross(new Vec3(0.0D, 1.0D, 0.0D));
            if (tangent.lengthSqr() < 1.0E-5D) {
                tangent = direction.cross(new Vec3(1.0D, 0.0D, 0.0D));
            }
            tangent = tangent.normalize();
            Vec3 secondTangent = direction.cross(tangent).normalize();
            double pulse = 0.5D + 0.5D * Math.sin(phase * 3.2D + index * 1.73D);
            double innerRadius = Math.max(0.3D, radius * 0.94D);
            double outerRadius = radius * (1.055D + pulse * 0.055D) + 0.8D;
            double width = 0.18D + radius * 0.005D;
            Vec3 inner = center.add(direction.scale(innerRadius));
            Vec3 outer = center.add(direction.scale(outerRadius));
            int red = 255;
            int green = index % 3 == 0 ? 238 : 128;
            int blue = index % 3 == 0 ? 194 : 62;
            coronaQuad(buffer, matrix, inner, outer, tangent, width, red, green, blue, alpha);
            coronaQuad(buffer, matrix, inner, outer, secondTangent, width,
                    red, green, blue, alpha);
        }
    }

    private static void coronaQuad(BufferBuilder buffer, Matrix4f matrix, Vec3 inner, Vec3 outer,
                                   Vec3 tangent, double width,
                                   int red, int green, int blue, int alpha) {
        Vec3 innerWidth = tangent.scale(width);
        Vec3 outerWidth = tangent.scale(width * 0.12D);
        coronaVertex(buffer, matrix, inner.add(innerWidth), red, green, blue, 0);
        coronaVertex(buffer, matrix, inner.subtract(innerWidth), red, green, blue, 0);
        coronaVertex(buffer, matrix, outer.subtract(outerWidth), red, green, blue, alpha);
        coronaVertex(buffer, matrix, outer.add(outerWidth), red, green, blue, alpha);
    }

    private static void coronaVertex(BufferBuilder buffer, Matrix4f matrix, Vec3 position,
                                     int red, int green, int blue, int alpha) {
        buffer.vertex(matrix, (float) position.x, (float) position.y, (float) position.z)
                .color(red, green, blue, alpha).endVertex();
    }
}
