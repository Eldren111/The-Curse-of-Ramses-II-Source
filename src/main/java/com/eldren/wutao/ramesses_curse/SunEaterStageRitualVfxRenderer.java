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
public final class SunEaterStageRitualVfxRenderer {
    private static ClientLevel lastLevel;

    private SunEaterStageRitualVfxRenderer() {
    }

    @SubscribeEvent
    public static void renderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != lastLevel) {
            SunEaterStageRitualVfxState.clear();
            lastLevel = minecraft.level;
        }
        if (minecraft.level == null) {
            return;
        }
        long gameTime = minecraft.level.getGameTime();
        SunEaterStageRitualVfxState.Ritual ritual =
                SunEaterStageRitualVfxState.activeRitual(gameTime);
        SunEaterStageRitualVfxState.Blast blast =
                SunEaterStageRitualVfxState.activeBlast(gameTime);
        if (ritual == null && blast == null) {
            return;
        }

        Vec3 camera = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        try {
            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            float partialTick = event.getPartialTick();
            if (ritual != null) {
                renderRitual(buffer, poseStack.last().pose(), ritual, gameTime, partialTick);
            }
            if (blast != null) {
                renderBlast(buffer, poseStack.last().pose(), blast, gameTime, partialTick);
            }
            Tesselator.getInstance().end();
        } finally {
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            poseStack.popPose();
        }
    }

    @SubscribeEvent
    public static void renderOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        long gameTime = minecraft.level.getGameTime();
        SunEaterStageRitualVfxState.Ritual ritual =
                SunEaterStageRitualVfxState.activeRitual(gameTime);
        SunEaterStageRitualVfxState.Blast blast =
                SunEaterStageRitualVfxState.activeBlast(gameTime);
        int color = 0;
        if (blast != null) {
            float progress = blast.progress(gameTime, minecraft.getFrameTime());
            int alpha = Mth.floor(245.0F * (1.0F - progress) * (1.0F - progress));
            color = alpha << 24 | 0xFFFFFF;
        } else if (ritual != null) {
            float progress = ritual.progress(gameTime, minecraft.getFrameTime());
            int alpha = Mth.floor(62.0F * Mth.clamp((progress - 0.45F) / 0.55F, 0.0F, 1.0F));
            color = alpha << 24;
        }
        if (color != 0) {
            event.getGuiGraphics().fill(0, 0,
                    minecraft.getWindow().getGuiScaledWidth(),
                    minecraft.getWindow().getGuiScaledHeight(), color);
        }
    }

    private static void renderRitual(
            BufferBuilder buffer, Matrix4f matrix,
            SunEaterStageRitualVfxState.Ritual ritual,
            long gameTime, float partialTick) {
        float progress = ritual.progress(gameTime, partialTick);
        float pulse = 0.82F + 0.18F * Mth.sin((gameTime + partialTick) * (0.28F + progress * 0.32F));
        float shellRadius = (8.6F - progress * 6.1F) * pulse;
        float coreRadius = 0.7F + progress * 2.25F;
        int[] primary = ritual.stage() >= 3 ? new int[]{255, 42, 24} : new int[]{255, 178, 38};
        int[] secondary = ritual.stage() >= 3 ? new int[]{105, 8, 145} : new int[]{170, 72, 255};
        drawSphere(buffer, matrix, ritual.origin(), shellRadius,
                primary[0], primary[1], primary[2], 32 + Mth.floor(progress * 68.0F));
        drawSphere(buffer, matrix, ritual.origin(), coreRadius,
                secondary[0], secondary[1], secondary[2], 115 + Mth.floor(progress * 100.0F));
        float phase = (gameTime + partialTick) * (0.045F + progress * 0.08F);
        for (int ring = 0; ring < 3; ring++) {
            drawOrbit(buffer, matrix, ritual.origin(), shellRadius * (1.08F + ring * 0.08F),
                    0.10F + progress * 0.12F, phase * (ring % 2 == 0 ? 1.0F : -1.2F), ring,
                    primary[0], primary[1], primary[2], 105 + Mth.floor(progress * 100.0F));
        }
    }

    private static void renderBlast(
            BufferBuilder buffer, Matrix4f matrix,
            SunEaterStageRitualVfxState.Blast blast,
            long gameTime, float partialTick) {
        float progress = blast.progress(gameTime, partialTick);
        float eased = 1.0F - (float) Math.pow(1.0F - progress, 3.0D);
        float radius = 1.5F + eased * 37.0F;
        int alpha = Mth.floor(205.0F * (1.0F - progress) * (1.0F - progress));
        int[] color = blast.stage() >= 3 ? new int[]{255, 35, 20} : new int[]{255, 205, 105};
        drawSphere(buffer, matrix, blast.origin(), radius,
                color[0], color[1], color[2], alpha);
        drawOrbit(buffer, matrix, blast.origin(), radius * 1.04F,
                0.65F + radius * 0.018F, gameTime * 0.07F, 1,
                255, 235, 210, alpha);
    }

    private static void drawSphere(
            BufferBuilder buffer, Matrix4f matrix, Vec3 center, float radius,
            int red, int green, int blue, int alpha) {
        int latitudeSegments = 10;
        int longitudeSegments = 20;
        for (int latitude = 0; latitude < latitudeSegments; latitude++) {
            float phi0 = (float) (-Math.PI / 2.0D + Math.PI * latitude / latitudeSegments);
            float phi1 = (float) (-Math.PI / 2.0D + Math.PI * (latitude + 1) / latitudeSegments);
            for (int longitude = 0; longitude < longitudeSegments; longitude++) {
                float theta0 = Mth.TWO_PI * longitude / longitudeSegments;
                float theta1 = Mth.TWO_PI * (longitude + 1) / longitudeSegments;
                sphereVertex(buffer, matrix, center, radius, phi0, theta0, red, green, blue, alpha);
                sphereVertex(buffer, matrix, center, radius, phi0, theta1, red, green, blue, alpha);
                sphereVertex(buffer, matrix, center, radius, phi1, theta1, red, green, blue, alpha);
                sphereVertex(buffer, matrix, center, radius, phi1, theta0, red, green, blue, alpha);
            }
        }
    }

    private static void sphereVertex(
            BufferBuilder buffer, Matrix4f matrix, Vec3 center, float radius,
            float phi, float theta, int red, int green, int blue, int alpha) {
        float horizontal = Mth.cos(phi) * radius;
        buffer.vertex(matrix,
                        (float) center.x + horizontal * Mth.cos(theta),
                        (float) center.y + Mth.sin(phi) * radius,
                        (float) center.z + horizontal * Mth.sin(theta))
                .color(red, green, blue, alpha).endVertex();
    }

    private static void drawOrbit(
            BufferBuilder buffer, Matrix4f matrix, Vec3 center, float radius, float width,
            float phase, int orientation, int red, int green, int blue, int alpha) {
        Vec3 axisA = switch (orientation) {
            case 1 -> new Vec3(0.82D, 0.46D, 0.34D).normalize();
            case 2 -> new Vec3(0.28D, -0.52D, 0.81D).normalize();
            default -> new Vec3(1.0D, 0.0D, 0.0D);
        };
        Vec3 axisB = switch (orientation) {
            case 1 -> new Vec3(-0.18D, 0.68D, -0.71D).normalize();
            case 2 -> new Vec3(0.91D, 0.39D, -0.06D).normalize();
            default -> new Vec3(0.0D, 0.0D, 1.0D);
        };
        int segments = 56;
        for (int index = 0; index < segments; index++) {
            double angle0 = Mth.TWO_PI * index / segments + phase;
            double angle1 = Mth.TWO_PI * (index + 1) / segments + phase;
            orbitSegment(buffer, matrix, center, axisA, axisB, radius, width,
                    angle0, angle1, red, green, blue, alpha);
        }
    }

    private static void orbitSegment(
            BufferBuilder buffer, Matrix4f matrix, Vec3 center, Vec3 axisA, Vec3 axisB,
            float radius, float width, double angle0, double angle1,
            int red, int green, int blue, int alpha) {
        Vec3 radial0 = axisA.scale(Math.cos(angle0)).add(axisB.scale(Math.sin(angle0)));
        Vec3 radial1 = axisA.scale(Math.cos(angle1)).add(axisB.scale(Math.sin(angle1)));
        orbitVertex(buffer, matrix, center.add(radial0.scale(radius - width)), red, green, blue, 0);
        orbitVertex(buffer, matrix, center.add(radial0.scale(radius + width)), red, green, blue, alpha);
        orbitVertex(buffer, matrix, center.add(radial1.scale(radius + width)), red, green, blue, alpha);
        orbitVertex(buffer, matrix, center.add(radial1.scale(radius - width)), red, green, blue, 0);
    }

    private static void orbitVertex(
            BufferBuilder buffer, Matrix4f matrix, Vec3 position,
            int red, int green, int blue, int alpha) {
        buffer.vertex(matrix, (float) position.x, (float) position.y, (float) position.z)
                .color(red, green, blue, alpha).endVertex();
    }
}
