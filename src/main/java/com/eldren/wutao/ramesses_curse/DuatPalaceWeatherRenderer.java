package com.eldren.wutao.ramesses_curse;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = RamessesCurseMod.MODID, value = Dist.CLIENT)
public final class DuatPalaceWeatherRenderer {
    private static final ResourceLocation RAIN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/environment/rain.png");
    private static final int ACTIVE_TIMEOUT_TICKS = 30;
    private static int activeTicks;

    private DuatPalaceWeatherRenderer() {
    }

    static void setActive(boolean active) {
        activeTicks = active ? ACTIVE_TIMEOUT_TICKS : 0;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (activeTicks > 0) {
            activeTicks--;
        }
    }

    @SubscribeEvent
    public static void onRenderWeather(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER || activeTicks <= 0) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null
                || minecraft.player == null
                || minecraft.level.dimension() != ModDimensions.DUAT_DESERT) {
            return;
        }

        renderGoldenRain(minecraft, event.getCamera(), event.getPartialTick().getGameTimeDeltaPartialTick(true));
    }

    private static void renderGoldenRain(Minecraft minecraft, Camera camera, float partialTick) {
        double cameraX = camera.getPosition().x;
        double cameraY = camera.getPosition().y;
        double cameraZ = camera.getPosition().z;
        int centerX = Mth.floor(cameraX);
        int centerY = Mth.floor(cameraY);
        int centerZ = Mth.floor(cameraZ);
        int radius = Minecraft.useFancyGraphics() ? 10 : 6;
        minecraft.gameRenderer.lightTexture().turnOnLightLayer();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getParticleShader);
        RenderSystem.setShaderTexture(0, RAIN_TEXTURE);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        BlockPos.MutableBlockPos lightPos = new BlockPos.MutableBlockPos();

        for (int z = centerZ - radius; z <= centerZ + radius; z++) {
            for (int x = centerX - radius; x <= centerX + radius; x++) {
                double relativeX = x + 0.5D - cameraX;
                double relativeZ = z + 0.5D - cameraZ;
                double horizontalDistance = Math.sqrt(relativeX * relativeX + relativeZ * relativeZ);
                if (horizontalDistance < 0.5D || horizontalDistance > radius) {
                    continue;
                }

                int surfaceY = minecraft.level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                int bottomY = Math.max(centerY - radius, surfaceY);
                int topY = Math.max(centerY + radius, surfaceY);
                if (bottomY >= topY) {
                    continue;
                }

                double sideX = -relativeZ / horizontalDistance * 0.5D;
                double sideZ = relativeX / horizontalDistance * 0.5D;
                RandomSource random = RandomSource.create(
                        (long) (x * x * 3121 + x * 45238971 ^ z * z * 418711 + z * 13761));
                int phase = (int) minecraft.level.getGameTime() + x * x * 3121 + x * 45238971
                        + z * z * 418711 + z * 13761 & 31;
                float vOffset = -((float) phase + partialTick) / 32.0F * (3.0F + random.nextFloat());
                float distanceRatio = (float) horizontalDistance / radius;
                float alpha = ((1.0F - distanceRatio * distanceRatio) * 0.55F + 0.25F) * 0.9F;
                lightPos.set(x, Math.max(surfaceY, centerY), z);
                int light = net.minecraft.client.renderer.LevelRenderer.getLightColor(minecraft.level, lightPos);

                double leftX = x - cameraX - sideX + 0.5D;
                double leftZ = z - cameraZ - sideZ + 0.5D;
                double rightX = x - cameraX + sideX + 0.5D;
                double rightZ = z - cameraZ + sideZ + 0.5D;
                double top = topY - cameraY;
                double bottom = bottomY - cameraY;

                buffer.addVertex((float) leftX, (float) top, (float) leftZ)
                        .setUv(0.0F, bottomY * 0.25F + vOffset)
                        .setColor(1.0F, 0.76F, 0.12F, alpha)
                        .setLight(light)
                        ;
                buffer.addVertex((float) rightX, (float) top, (float) rightZ)
                        .setUv(1.0F, bottomY * 0.25F + vOffset)
                        .setColor(1.0F, 0.76F, 0.12F, alpha)
                        .setLight(light)
                        ;
                buffer.addVertex((float) rightX, (float) bottom, (float) rightZ)
                        .setUv(1.0F, topY * 0.25F + vOffset)
                        .setColor(1.0F, 0.76F, 0.12F, alpha)
                        .setLight(light)
                        ;
                buffer.addVertex((float) leftX, (float) bottom, (float) leftZ)
                        .setUv(0.0F, topY * 0.25F + vOffset)
                        .setColor(1.0F, 0.76F, 0.12F, alpha)
                        .setLight(light)
                        ;
            }
        }

        com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        minecraft.gameRenderer.lightTexture().turnOffLightLayer();
    }
}

