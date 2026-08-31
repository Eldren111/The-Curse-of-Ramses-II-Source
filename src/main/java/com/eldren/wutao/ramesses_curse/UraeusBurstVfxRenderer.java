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
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import java.util.List;

@EventBusSubscriber(modid = RamessesCurseMod.MODID, value = Dist.CLIENT)
public final class UraeusBurstVfxRenderer {
    private UraeusBurstVfxRenderer() {
    }

    @SubscribeEvent
    public static void renderWorldBurst(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        List<UraeusVfxState.Burst> bursts = UraeusVfxState.activeBursts(minecraft.level.getGameTime());
        if (bursts.isEmpty()) {
            return;
        }

        Vec3 cameraPosition = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        try {
            poseStack.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);
            Matrix4f matrix = poseStack.last().pose();

            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            try {
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                Vector3f cameraLeft = event.getCamera().getLeftVector();
                Vector3f cameraUp = event.getCamera().getUpVector();
                float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
                long gameTime = minecraft.level.getGameTime();
                for (UraeusVfxState.Burst burst : bursts) {
                    renderBurst(buffer, matrix, burst, gameTime, partialTick, cameraLeft, cameraUp);
                }
                BufferUploader.drawWithShader(buffer.buildOrThrow());
            } finally {
                RenderSystem.depthMask(true);
                RenderSystem.enableDepthTest();
                RenderSystem.enableCull();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableBlend();
            }
        } finally {
            poseStack.popPose();
        }
    }

    private static void renderBurst(BufferBuilder buffer, Matrix4f matrix, UraeusVfxState.Burst burst,
                                    long gameTime, float partialTick, Vector3f cameraLeft, Vector3f cameraUp) {
        float progress = burst.progress(gameTime, partialTick);
        float eased = 1.0F - (float) Math.pow(1.0F - progress, 3.0D);
        float fade = (1.0F - progress) * (1.0F - progress);
        float radius = 0.18F + eased * (burst.rainEmpowered() ? 3.25F : 2.55F);
        Vec3 center = burst.position();
        int red = burst.rainEmpowered() ? 92 : 255;
        int green = burst.rainEmpowered() ? 235 : 198;
        int blue = burst.rainEmpowered() ? 255 : 72;
        int alpha = Mth.clamp(Mth.floor(178.0F * fade), 0, 178);

        drawRing(buffer, matrix, center, radius, 0.07F + 0.14F * fade,
                new Vector3f(1.0F, 0.0F, 0.0F), new Vector3f(0.0F, 0.0F, 1.0F),
                red, green, blue, alpha);
        drawRing(buffer, matrix, center.add(0.0D, 0.08D, 0.0D), radius * 0.68F, 0.04F + 0.08F * fade,
                cameraLeft, cameraUp, 255, 245, burst.rainEmpowered() ? 255 : 168, Math.max(0, alpha / 2));
    }

    private static void drawRing(BufferBuilder buffer, Matrix4f matrix, Vec3 center, float radius, float width,
                                 Vector3f axisA, Vector3f axisB, int red, int green, int blue, int alpha) {
        int segments = 32;
        float inner = Math.max(0.0F, radius - width);
        float outer = radius + width;
        for (int segment = 0; segment < segments; segment++) {
            float angle0 = (float) (Math.PI * 2.0D * segment / segments);
            float angle1 = (float) (Math.PI * 2.0D * (segment + 1) / segments);
            ringVertex(buffer, matrix, center, axisA, axisB, angle0, inner, red, green, blue, alpha);
            ringVertex(buffer, matrix, center, axisA, axisB, angle1, inner, red, green, blue, alpha);
            ringVertex(buffer, matrix, center, axisA, axisB, angle1, outer, red, green, blue, 0);
            ringVertex(buffer, matrix, center, axisA, axisB, angle0, outer, red, green, blue, 0);
        }
    }

    private static void ringVertex(BufferBuilder buffer, Matrix4f matrix, Vec3 center,
                                   Vector3f axisA, Vector3f axisB, float angle, float radius,
                                   int red, int green, int blue, int alpha) {
        float cosine = Mth.cos(angle) * radius;
        float sine = Mth.sin(angle) * radius;
        float x = (float) center.x + axisA.x() * cosine + axisB.x() * sine;
        float y = (float) center.y + axisA.y() * cosine + axisB.y() * sine;
        float z = (float) center.z + axisA.z() * cosine + axisB.z() * sine;
        buffer.addVertex(matrix, x, y, z).setColor(red, green, blue, alpha);
    }

}
