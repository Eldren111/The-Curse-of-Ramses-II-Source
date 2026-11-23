package com.eldren.wutao.ramesses_curse;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID, value = Dist.CLIENT)
public final class DuatBlackHoleFallbackRenderer {
    private DuatBlackHoleFallbackRenderer() {
    }

    @SubscribeEvent
    public static void renderEventHorizon(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        DuatBlackHoleVfxState.BlackHole blackHole =
                DuatBlackHoleVfxState.activeBlackHole(minecraft.level.getGameTime());
        if (blackHole == null) {
            return;
        }
        float intensity = blackHole.intensity(
                minecraft.level.getGameTime(), event.getPartialTick());
        if (intensity <= 0.01F) {
            return;
        }

        Vec3 camera = event.getCamera().getPosition();
        float radius = 0.35F + intensity * 2.15F;
        if (camera.distanceToSqr(blackHole.origin()) <= radius * radius * 1.32F) {
            return;
        }
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        try {
            poseStack.translate(-camera.x, -camera.y, -camera.z);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            drawSphere(buffer, poseStack.last().pose(), blackHole.origin(),
                    radius);
            tesselator.end();
        } finally {
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            poseStack.popPose();
        }
    }

    private static void drawSphere(BufferBuilder buffer, Matrix4f matrix,
                                   Vec3 center, float radius) {
        int latitudes = 12;
        int longitudes = 24;
        for (int latitude = 0; latitude < latitudes; latitude++) {
            float phi0 = (float) (-Math.PI / 2.0D + Math.PI * latitude / latitudes);
            float phi1 = (float) (-Math.PI / 2.0D + Math.PI * (latitude + 1) / latitudes);
            for (int longitude = 0; longitude < longitudes; longitude++) {
                float theta0 = Mth.TWO_PI * longitude / longitudes;
                float theta1 = Mth.TWO_PI * (longitude + 1) / longitudes;
                vertex(buffer, matrix, center, radius, phi0, theta0);
                vertex(buffer, matrix, center, radius, phi0, theta1);
                vertex(buffer, matrix, center, radius, phi1, theta1);
                vertex(buffer, matrix, center, radius, phi1, theta0);
            }
        }
    }

    private static void vertex(BufferBuilder buffer, Matrix4f matrix, Vec3 center,
                               float radius, float phi, float theta) {
        float horizontal = Mth.cos(phi) * radius;
        buffer.vertex(matrix,
                        (float) center.x + horizontal * Mth.cos(theta),
                        (float) center.y + Mth.sin(phi) * radius,
                        (float) center.z + horizontal * Mth.sin(theta))
                .color(0, 0, 0, 255).endVertex();
    }
}
