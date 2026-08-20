package com.eldren.wutao.ramesses_curse;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.slf4j.Logger;

import java.io.IOException;

@EventBusSubscriber(modid = RamessesCurseMod.MODID, value = Dist.CLIENT)
public final class DuatVolumetricFogRenderer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation POST_CHAIN =
            ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "shaders/post/duat_volumetric_fog.json");
    private static PostChain chain;
    private static PostPass fogPass;
    private static int chainWidth = -1;
    private static int chainHeight = -1;
    private static boolean reloadRequested;
    private static boolean creationFailed;

    private DuatVolumetricFogRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (!DuatDesertClientEffects.shouldRender(minecraft)) {
            return;
        }

        try {
            ensureChain(minecraft);
            if (chain == null || fogPass == null) {
                return;
            }

            float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
            float intensity = DuatDesertClientEffects.localIntensity(minecraft, partialTick);
            Camera camera = event.getCamera();
            Quaternionf rotation = camera.rotation();
            Matrix4f inverseProjection = new Matrix4f(event.getProjectionMatrix()).invert();

            setFloat("Intensity", intensity);
            setFloat("CameraHeight", (float) camera.getPosition().y);
            setFloat2(
                    "StormOffset",
                    DuatDesertClientEffects.stormCenterOffsetX(minecraft, partialTick),
                    DuatDesertClientEffects.stormCenterOffsetZ(minecraft, partialTick));
            setFloat("StormRadius", DuatDesertClientEffects.stormRadius());
            setFloat2("Wind", DuatDesertClientEffects.windX(), DuatDesertClientEffects.windZ());
            setFloat4("CameraRotation", rotation.x, rotation.y, rotation.z, rotation.w);
            fogPass.getEffect().safeGetUniform("InvProjMat").set(inverseProjection);

            chain.process(partialTick);
            minecraft.getMainRenderTarget().bindWrite(false);
        } catch (RuntimeException exception) {
            closeChain();
            creationFailed = true;
            LOGGER.error("Failed to render Duat volumetric sandstorm", exception);
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
                        minecraft.getTextureManager(),
                        minecraft.getResourceManager(),
                        mainTarget,
                        POST_CHAIN);
                RenderTarget swapTarget = chain.getTempTarget("swap");
                if (swapTarget == null) {
                    throw new IOException("Missing swap target in Duat volumetric fog post chain");
                }

                fogPass = chain.addPass(
                        RamessesCurseMod.MODID + ":duat_volumetric_fog",
                        mainTarget,
                        swapTarget, false);
                fogPass.addAuxAsset("DepthSampler", mainTarget::getDepthTextureId, mainTarget.width, mainTarget.height);
                chain.addPass("minecraft:blit", swapTarget, mainTarget, false);
                chain.resize(mainTarget.width, mainTarget.height);
                chainWidth = mainTarget.width;
                chainHeight = mainTarget.height;
            } catch (IOException exception) {
                closeChain();
                creationFailed = true;
                LOGGER.error("Failed to load Duat volumetric fog shader", exception);
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
        Uniform uniform = fogPass.getEffect().getUniform(name);
        if (uniform != null) {
            uniform.set(value);
        }
    }

    private static void setFloat2(String name, float x, float y) {
        Uniform uniform = fogPass.getEffect().getUniform(name);
        if (uniform != null) {
            uniform.set(x, y);
        }
    }

    private static void setFloat4(String name, float x, float y, float z, float w) {
        Uniform uniform = fogPass.getEffect().getUniform(name);
        if (uniform != null) {
            uniform.set(x, y, z, w);
        }
    }

    private static void closeChain() {
        if (chain != null) {
            chain.close();
        }
        chain = null;
        fogPass = null;
        chainWidth = -1;
        chainHeight = -1;
    }

    @EventBusSubscriber(
            modid = RamessesCurseMod.MODID,
            value = Dist.CLIENT,
            bus = EventBusSubscriber.Bus.MOD)
    public static final class ReloadEvents {
        private ReloadEvents() {
        }

        @SubscribeEvent
        public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener((ResourceManagerReloadListener) resourceManager -> reloadRequested = true);
        }
    }
}

