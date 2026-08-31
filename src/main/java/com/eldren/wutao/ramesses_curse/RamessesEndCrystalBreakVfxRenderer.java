package com.eldren.wutao.ramesses_curse;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.List;

@EventBusSubscriber(modid = RamessesCurseMod.MODID, value = Dist.CLIENT)
public final class RamessesEndCrystalBreakVfxRenderer {
    private static ClientLevel lastLevel;

    private RamessesEndCrystalBreakVfxRenderer() {
    }

    @SubscribeEvent
    public static void renderShockwaves(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != lastLevel) {
            RamessesEndCrystalBreakVfxState.clear();
            lastLevel = minecraft.level;
        }
        if (minecraft.level == null) {
            return;
        }
        long gameTime = minecraft.level.getGameTime();
        List<RamessesEndCrystalBreakVfxState.Shockwave> shockwaves =
                RamessesEndCrystalBreakVfxState.activeShockwaves(gameTime);
        if (shockwaves.isEmpty()) {
            return;
        }

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
                BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
                for (RamessesEndCrystalBreakVfxState.Shockwave shockwave : shockwaves) {
                    renderShockwave(buffer, matrix, shockwave, gameTime, partialTick);
                }
                BufferUploader.drawWithShader(buffer.buildOrThrow());
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

    private static void renderShockwave(BufferBuilder buffer, Matrix4f matrix,
                                        RamessesEndCrystalBreakVfxState.Shockwave shockwave,
                                        long gameTime, float partialTick) {
        float progress = shockwave.progress(gameTime, partialTick);
        float eased = 1.0F - (float) Math.pow(1.0F - progress, 3.0D);
        float radius = 0.25F + eased * 7.75F;
        float fade = (1.0F - progress) * (1.0F - progress);
        int alpha = Mth.clamp(Mth.floor(185.0F * fade), 0, 185);
        drawSphere(buffer, matrix, shockwave.position(), radius, 255, 255, 255, alpha);
        drawSphere(buffer, matrix, shockwave.position(), radius * 0.94F, 210, 235, 255, alpha / 3);
    }

    private static void drawSphere(BufferBuilder buffer, Matrix4f matrix, Vec3 center, float radius,
                                   int red, int green, int blue, int alpha) {
        int latitudeSegments = 12;
        int longitudeSegments = 24;
        for (int latitude = 0; latitude < latitudeSegments; latitude++) {
            float phi0 = (float) (-Math.PI / 2.0D + Math.PI * latitude / latitudeSegments);
            float phi1 = (float) (-Math.PI / 2.0D + Math.PI * (latitude + 1) / latitudeSegments);
            for (int longitude = 0; longitude < longitudeSegments; longitude++) {
                float theta0 = (float) (Math.PI * 2.0D * longitude / longitudeSegments);
                float theta1 = (float) (Math.PI * 2.0D * (longitude + 1) / longitudeSegments);
                sphereVertex(buffer, matrix, center, radius, phi0, theta0, red, green, blue, alpha);
                sphereVertex(buffer, matrix, center, radius, phi0, theta1, red, green, blue, alpha);
                sphereVertex(buffer, matrix, center, radius, phi1, theta1, red, green, blue, alpha / 2);
                sphereVertex(buffer, matrix, center, radius, phi1, theta0, red, green, blue, alpha / 2);
            }
        }
    }

    private static void sphereVertex(BufferBuilder buffer, Matrix4f matrix, Vec3 center, float radius,
                                     float phi, float theta, int red, int green, int blue, int alpha) {
        float horizontal = Mth.cos(phi) * radius;
        float x = (float) center.x + horizontal * Mth.cos(theta);
        float y = (float) center.y + Mth.sin(phi) * radius;
        float z = (float) center.z + horizontal * Mth.sin(theta);
        buffer.addVertex(matrix, x, y, z).setColor(red, green, blue, alpha);
    }
}
