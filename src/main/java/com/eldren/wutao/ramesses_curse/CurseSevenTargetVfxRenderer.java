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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.Iterator;
import java.util.Map;

@EventBusSubscriber(modid = RamessesCurseMod.MODID, value = Dist.CLIENT)
public final class CurseSevenTargetVfxRenderer {
    private static ClientLevel lastLevel;

    private CurseSevenTargetVfxRenderer() {
    }

    @SubscribeEvent
    public static void renderMarks(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != lastLevel) {
            ClientPacketHandlers.clearCurseSevenTargetMarks();
            lastLevel = minecraft.level;
        }
        if (minecraft.level == null || ClientPacketHandlers.curseSevenTargetMarks().isEmpty()) {
            return;
        }
        Vec3 camera = event.getCamera().getPosition();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        long gameTime = minecraft.level.getGameTime();
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
                Iterator<Map.Entry<Integer, Long>> iterator = ClientPacketHandlers.curseSevenTargetMarks().entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Integer, Long> entry = iterator.next();
                    if (entry.getValue() <= gameTime) {
                        iterator.remove();
                        continue;
                    }
                    Entity entity = minecraft.level.getEntity(entry.getKey());
                    if (!(entity instanceof LivingEntity target) || !target.isAlive()) {
                        iterator.remove();
                        continue;
                    }
                    if (minecraft.cameraEntity == null
                            || target.distanceToSqr(minecraft.cameraEntity) > 64.0D * 64.0D) {
                        continue;
                    }
                    drawArrow(buffer, matrix, target, gameTime, partialTick);
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

    private static void drawArrow(BufferBuilder buffer, Matrix4f matrix, LivingEntity target,
                                  long gameTime, float partialTick) {
        Vec3 position = target.getPosition(partialTick);
        float age = gameTime + partialTick;
        float bob = Mth.sin(age * 0.12F) * 0.045F;
        float rotation = age * 0.08F;
        double x = position.x;
        double y = position.y + target.getBbHeight() + 0.12D + bob;
        double z = position.z;
        drawPrism(buffer, matrix, x, y + 0.28D, z, y + 1.48D, 0.075D, rotation, 190, 12, 16, 150);
        drawPrism(buffer, matrix, x, y + 0.30D, z, y + 1.43D, 0.032D, -rotation * 1.7F, 255, 65, 48, 230);
        drawCone(buffer, matrix, x, y + 0.26D, z, y + 0.02D, 0.25D, rotation, 255, 28, 28, 220);
        drawRing(buffer, matrix, x, y + 0.78D, z, 0.18D, 0.025D, rotation * 1.8F, 255, 45, 30, 150);
    }

    private static void drawPrism(BufferBuilder buffer, Matrix4f matrix, double x, double bottom, double z,
                                  double top, double radius, float rotation, int red, int green, int blue, int alpha) {
        int sides = 6;
        for (int side = 0; side < sides; side++) {
            float a0 = rotation + (float) (Math.PI * 2.0D * side / sides);
            float a1 = rotation + (float) (Math.PI * 2.0D * (side + 1) / sides);
            vertex(buffer, matrix, x + Mth.cos(a0) * radius, bottom, z + Mth.sin(a0) * radius, red, green, blue, alpha);
            vertex(buffer, matrix, x + Mth.cos(a1) * radius, bottom, z + Mth.sin(a1) * radius, red, green, blue, alpha);
            vertex(buffer, matrix, x + Mth.cos(a1) * radius, top, z + Mth.sin(a1) * radius, red, green, blue, alpha);
            vertex(buffer, matrix, x + Mth.cos(a0) * radius, top, z + Mth.sin(a0) * radius, red, green, blue, alpha);
        }
    }

    private static void drawCone(BufferBuilder buffer, Matrix4f matrix, double x, double baseY, double z,
                                 double tipY, double radius, float rotation, int red, int green, int blue, int alpha) {
        int sides = 6;
        for (int side = 0; side < sides; side++) {
            float a0 = rotation + (float) (Math.PI * 2.0D * side / sides);
            float a1 = rotation + (float) (Math.PI * 2.0D * (side + 1) / sides);
            vertex(buffer, matrix, x, tipY, z, 255, 85, 65, alpha);
            vertex(buffer, matrix, x + Mth.cos(a0) * radius, baseY, z + Mth.sin(a0) * radius, red, green, blue, alpha);
            vertex(buffer, matrix, x + Mth.cos(a1) * radius, baseY, z + Mth.sin(a1) * radius, red, green, blue, alpha);
            vertex(buffer, matrix, x, tipY, z, 255, 85, 65, alpha);
        }
    }

    private static void drawRing(BufferBuilder buffer, Matrix4f matrix, double x, double y, double z,
                                 double radius, double width, float rotation, int red, int green, int blue, int alpha) {
        int sides = 20;
        double inner = Math.max(0.0D, radius - width);
        double outer = radius + width;
        for (int side = 0; side < sides; side++) {
            float a0 = rotation + (float) (Math.PI * 2.0D * side / sides);
            float a1 = rotation + (float) (Math.PI * 2.0D * (side + 1) / sides);
            vertex(buffer, matrix, x + Mth.cos(a0) * inner, y, z + Mth.sin(a0) * inner, red, green, blue, alpha);
            vertex(buffer, matrix, x + Mth.cos(a1) * inner, y, z + Mth.sin(a1) * inner, red, green, blue, alpha);
            vertex(buffer, matrix, x + Mth.cos(a1) * outer, y, z + Mth.sin(a1) * outer, red, green, blue, 0);
            vertex(buffer, matrix, x + Mth.cos(a0) * outer, y, z + Mth.sin(a0) * outer, red, green, blue, 0);
        }
    }

    private static void vertex(BufferBuilder buffer, Matrix4f matrix, double x, double y, double z,
                               int red, int green, int blue, int alpha) {
        buffer.addVertex(matrix, (float) x, (float) y, (float) z).setColor(red, green, blue, alpha);
    }
}
