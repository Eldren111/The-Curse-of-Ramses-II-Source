package com.eldren.wutao.ramesses_curse;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID, value = Dist.CLIENT)
public final class DuatBlackHoleLensRenderer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation POST_CHAIN = new ResourceLocation(
            RamessesCurseMod.MODID, "shaders/post/duat_black_hole_lens.json");
    private static PostChain chain;
    private static PostPass lensPass;
    private static int chainWidth = -1;
    private static int chainHeight = -1;
    private static boolean reloadRequested;
    private static boolean creationFailed;
    private static ShaderInstance shaderPackComposite;

    private DuatBlackHoleLensRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        long gameTime = minecraft.level.getGameTime();
        DuatBlackHoleVfxState.BlackHole blackHole =
                DuatBlackHoleVfxState.activeBlackHole(gameTime);
        if (blackHole == null) {
            return;
        }
        float partialTick = event.getPartialTick();
        float intensity = blackHole.intensity(gameTime, partialTick);
        float collapseProgress = blackHole.collapseProgress(gameTime, partialTick);
        if (intensity <= 0.01F && collapseProgress <= 0.001F) {
            return;
        }
        Camera camera = event.getCamera();
        Vec3 offset = blackHole.origin().subtract(camera.getPosition());
        if (offset.lengthSqr() > 256.0D * 256.0D) {
            return;
        }

        Vector3f cameraForward = camera.getLookVector();
        Vector3f cameraLeft = camera.getLeftVector();
        Vector3f cameraUp = camera.getUpVector();
        float viewX = -dot(offset, cameraLeft);
        float viewY = dot(offset, cameraUp);
        float viewZ = -dot(offset, cameraForward);
        Matrix4f projection = new Matrix4f(event.getProjectionMatrix());
        float screenX = 0.5F;
        float screenY = 0.5F;
        float radiusX = 0.18F;
        float radiusY = 0.32F;
        float visible = 0.0F;
        if (viewZ < -0.25F) {
            Vector4f clip = new Vector4f(viewX, viewY, viewZ, 1.0F).mul(projection);
            if (clip.w > 0.0001F) {
                screenX = clip.x / clip.w * 0.5F + 0.5F;
                screenY = clip.y / clip.w * 0.5F + 0.5F;
                float visualWorldRadius = 4.5F + intensity * 6.5F;
                radiusX = Mth.clamp(
                        Math.abs(projection.m00()) * visualWorldRadius / -viewZ * 0.5F,
                        0.075F, 0.31F);
                float aspect = (float) minecraft.getMainRenderTarget().width
                        / Math.max(1, minecraft.getMainRenderTarget().height);
                radiusY = Math.min(0.76F, radiusX * aspect);
                if (screenX + radiusX * 2.4F >= 0.0F
                        && screenX - radiusX * 2.4F <= 1.0F
                        && screenY + radiusY * 2.4F >= 0.0F
                        && screenY - radiusY * 2.4F <= 1.0F) {
                    visible = 1.0F;
                }
            }
        }
        float distance = (float) Math.sqrt(offset.lengthSqr());
        float engulf = Mth.clamp((7.5F - distance) / 5.25F, 0.0F, 1.0F);
        engulf = engulf * engulf * (3.0F - 2.0F * engulf);
        float effectTime = blackHole.elapsed(gameTime, partialTick) / 20.0F;

        try {
            ensureChain(minecraft);
            if (chain == null || lensPass == null) {
                renderShaderPackComposite(screenX, screenY, radiusX, radiusY,
                        effectTime, intensity, collapseProgress, engulf, visible);
                return;
            }
            setFloat("EffectTime", effectTime);
            setFloat("Intensity", intensity);
            setFloat("CollapseProgress", collapseProgress);
            setFloat("Engulf", engulf);
            setFloat("Visible", visible);
            setFloat2("LensScreenCenter", screenX, screenY);
            setFloat2("LensScreenRadius", radiusX, radiusY);

            chain.process(partialTick);
            minecraft.getMainRenderTarget().bindWrite(false);
        } catch (RuntimeException exception) {
            closeChain();
            creationFailed = true;
            LOGGER.error("Failed to render Duat black-hole gravitational lens", exception);
            renderShaderPackComposite(screenX, screenY, radiusX, radiusY,
                    effectTime, intensity, collapseProgress, engulf, visible);
        }
    }

    private static void ensureChain(Minecraft minecraft) {
        if (reloadRequested) {
            closeChain();
            reloadRequested = false;
            creationFailed = false;
        }
        if (creationFailed) {
            return;
        }
        RenderTarget mainTarget = minecraft.getMainRenderTarget();
        if (chain == null) {
            try {
                chain = new PostChain(
                        minecraft.getTextureManager(), minecraft.getResourceManager(),
                        mainTarget, POST_CHAIN);
                RenderTarget swapTarget = chain.getTempTarget("swap");
                if (swapTarget == null) {
                    throw new IOException("Missing swap target in black-hole lens post chain");
                }
                lensPass = chain.addPass(
                        RamessesCurseMod.MODID + ":duat_black_hole_lens",
                        mainTarget, swapTarget);
                chain.addPass("minecraft:blit", swapTarget, mainTarget);
                chain.resize(mainTarget.width, mainTarget.height);
                chainWidth = mainTarget.width;
                chainHeight = mainTarget.height;
            } catch (IOException exception) {
                closeChain();
                creationFailed = true;
                LOGGER.error("Failed to load Duat black-hole lens shader", exception);
                return;
            }
        }
        if (chainWidth != mainTarget.width || chainHeight != mainTarget.height) {
            chain.resize(mainTarget.width, mainTarget.height);
            chainWidth = mainTarget.width;
            chainHeight = mainTarget.height;
        }
    }

    private static void setFloat(String name, float value) {
        Uniform uniform = lensPass.getEffect().getUniform(name);
        if (uniform != null) {
            uniform.set(value);
        }
    }

    private static void setFloat2(String name, float x, float y) {
        Uniform uniform = lensPass.getEffect().getUniform(name);
        if (uniform != null) {
            uniform.set(x, y);
        }
    }

    private static float dot(Vec3 vector, Vector3f basis) {
        return (float) (vector.x * basis.x() + vector.y * basis.y() + vector.z * basis.z());
    }

    private static void renderShaderPackComposite(
            float screenX, float screenY, float radiusX, float radiusY,
            float effectTime, float intensity, float collapseProgress,
            float engulf, float visible) {
        if (shaderPackComposite == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getMainRenderTarget().bindWrite(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        try {
            RenderSystem.setShader(() -> shaderPackComposite);
            setCompositeFloat("EffectTime", effectTime);
            setCompositeFloat("Intensity", intensity);
            setCompositeFloat("CollapseProgress", collapseProgress);
            setCompositeFloat("Engulf", engulf);
            setCompositeFloat("Visible", visible);
            setCompositeFloat2("LensScreenCenter", screenX, screenY);
            setCompositeFloat2("LensScreenRadius", radiusX, radiusY);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
            buffer.vertex(-1.0D, -1.0D, 0.0D).endVertex();
            buffer.vertex(1.0D, -1.0D, 0.0D).endVertex();
            buffer.vertex(1.0D, 1.0D, 0.0D).endVertex();
            buffer.vertex(-1.0D, 1.0D, 0.0D).endVertex();
            tesselator.end();
        } finally {
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        }
    }

    private static void setCompositeFloat(String name, float value) {
        Uniform uniform = shaderPackComposite.getUniform(name);
        if (uniform != null) {
            uniform.set(value);
        }
    }

    private static void setCompositeFloat2(String name, float x, float y) {
        Uniform uniform = shaderPackComposite.getUniform(name);
        if (uniform != null) {
            uniform.set(x, y);
        }
    }

    public static boolean externalShaderPackActive() {
        Boolean irisActive = queryShaderApi("net.irisshaders.iris.api.v0.IrisApi");
        if (irisActive == null) {
            irisActive = queryShaderApi("net.coderbot.iris.api.v0.IrisApi");
        }
        if (irisActive != null) {
            return irisActive;
        }
        Boolean optifineActive = queryStaticBoolean(
                "net.optifine.Config", "isShaders");
        if (optifineActive != null) {
            return optifineActive;
        }
        return ModList.get().isLoaded("oculus")
                || ModList.get().isLoaded("iris")
                || ModList.get().isLoaded("optifine");
    }

    private static Boolean queryShaderApi(String className) {
        try {
            Class<?> apiClass = Class.forName(className);
            Object api = apiClass.getMethod("getInstance").invoke(null);
            return (Boolean) apiClass.getMethod("isShaderPackInUse").invoke(api);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    private static Boolean queryStaticBoolean(String className, String methodName) {
        try {
            return (Boolean) Class.forName(className).getMethod(methodName).invoke(null);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    private static void closeChain() {
        if (chain != null) {
            chain.close();
        }
        chain = null;
        lensPass = null;
        chainWidth = -1;
        chainHeight = -1;
    }

    @Mod.EventBusSubscriber(
            modid = RamessesCurseMod.MODID,
            value = Dist.CLIENT,
            bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ReloadEvents {
        private ReloadEvents() {
        }

        @SubscribeEvent
        public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener(
                    (ResourceManagerReloadListener) resourceManager -> reloadRequested = true);
        }

        @SubscribeEvent
        public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
            event.registerShader(new ShaderInstance(
                            event.getResourceProvider(),
                            new ResourceLocation(RamessesCurseMod.MODID,
                                    "duat_black_hole_composite"),
                            DefaultVertexFormat.POSITION),
                    shader -> shaderPackComposite = shader);
        }
    }
}
