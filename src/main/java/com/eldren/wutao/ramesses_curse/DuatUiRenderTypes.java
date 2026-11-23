package com.eldren.wutao.ramesses_curse;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

/** Render states for the local Duat interface geometry only. */
public final class DuatUiRenderTypes extends RenderType {
    private static ShaderInstance uiShader;
    private static ShaderInstance uiOpaqueShader;

    private static final RenderStateShard.ShaderStateShard UI_SHADER =
            new RenderStateShard.ShaderStateShard(() -> uiShader);
    private static final RenderStateShard.ShaderStateShard UI_OPAQUE_SHADER =
            new RenderStateShard.ShaderStateShard(() -> uiOpaqueShader);

    private static final RenderType UI = create(
            RamessesCurseMod.MODID + ":duat_ui",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            CompositeState.builder()
                    .setShaderState(UI_SHADER)
                    .setTextureState(NO_TEXTURE)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setOverlayState(NO_OVERLAY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setOutputState(MAIN_TARGET)
                    .createCompositeState(false));

    private static final RenderType UI_OPAQUE = create(
            RamessesCurseMod.MODID + ":duat_ui_opaque",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.TRIANGLES,
            256,
            false,
            false,
            CompositeState.builder()
                    .setShaderState(UI_OPAQUE_SHADER)
                    .setTextureState(NO_TEXTURE)
                    .setTransparencyState(NO_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setOverlayState(NO_OVERLAY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setOutputState(MAIN_TARGET)
                    .createCompositeState(false));

    private DuatUiRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode,
                              int bufferSize, boolean affectsCrumbling, boolean sortOnUpload,
                              Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static RenderType ui() {
        return UI;
    }

    public static RenderType uiOpaque() {
        return UI_OPAQUE;
    }

    public static void update(float time, float pulse) {
        if (uiShader == null) {
            return;
        }
        Uniform timeUniform = uiShader.getUniform("UiTime");
        if (timeUniform != null) {
            timeUniform.set(time);
        }
        Uniform pulseUniform = uiShader.getUniform("UiPulse");
        if (pulseUniform != null) {
            pulseUniform.set(pulse);
        }
        if (uiOpaqueShader != null) {
            Uniform opaqueTime = uiOpaqueShader.getUniform("UiTime");
            if (opaqueTime != null) {
                opaqueTime.set(time);
            }
            Uniform opaquePulse = uiOpaqueShader.getUniform("UiPulse");
            if (opaquePulse != null) {
                opaquePulse.set(pulse);
            }
        }
    }

    @Mod.EventBusSubscriber(
            modid = RamessesCurseMod.MODID,
            value = Dist.CLIENT,
            bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ShaderEvents {
        private ShaderEvents() {
        }

        @SubscribeEvent
        public static void registerShaders(RegisterShadersEvent event) throws IOException {
            event.registerShader(
                    new ShaderInstance(
                            event.getResourceProvider(),
                            new ResourceLocation(RamessesCurseMod.MODID, "duat_ui"),
                            DefaultVertexFormat.POSITION_COLOR),
                    shader -> uiShader = shader);
            event.registerShader(
                    new ShaderInstance(
                            event.getResourceProvider(),
                            new ResourceLocation(RamessesCurseMod.MODID, "duat_ui_opaque"),
                            DefaultVertexFormat.POSITION_COLOR),
                    shader -> uiOpaqueShader = shader);
        }
    }
}
