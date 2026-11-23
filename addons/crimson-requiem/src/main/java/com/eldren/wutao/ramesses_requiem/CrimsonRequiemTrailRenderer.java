package com.eldren.wutao.ramesses_requiem;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import java.util.*;

/** Samples flight paths in world space and refracts the final scene before the HUD is drawn. */
@Mod.EventBusSubscriber(modid = RequiemAddon.MODID, value = Dist.CLIENT)
public final class CrimsonRequiemTrailRenderer {
    private static final int SEGMENTS = 24;
    private record Sample(Vec3 point, long tick) {}
    private static final Map<UUID, ArrayDeque<Sample>> trails = new LinkedHashMap<>();
    private static net.minecraft.client.multiplayer.ClientLevel world;
    private static Matrix4f projection;
    private static boolean frameReady;
    private static PostChain chain;
    private static PostPass pass;
    private static RenderTarget target;
    private static int width, height;
    private static boolean reload, failed;
    private static boolean verified;

    @SubscribeEvent
    public static void sample(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var mc = Minecraft.getInstance();
        if (!verified && !failed && mc.getOverlay() == null) {
            try { verifyOverlay(mc); verified = true; }
            catch (java.io.IOException | RuntimeException exception) {
                failed = true;
                LogUtils.getLogger().error("Crimson trail GPU check failed; leaving the world image untouched", exception);
            }
        }
        if (world != mc.level) { trails.clear(); world = mc.level; frameReady = false; }
        if (world == null || mc.isPaused()) return;
        long now = world.getGameTime();
        for (var entity : world.entitiesForRendering()) {
            if (!(entity instanceof CrimsonRequiemProjectile sword) || sword.isWave() || sword.isRemoved()) continue;
            if (!trails.containsKey(sword.getUUID()) && trails.size() >= 8) continue;
            var path = trails.computeIfAbsent(sword.getUUID(), id -> new ArrayDeque<>());
            if (!path.isEmpty() && path.getLast().point().distanceToSqr(sword.position()) > 64) path.clear();
            path.addLast(new Sample(sword.position(), now));
        }
        trails.values().removeIf(path -> {
            while (!path.isEmpty() && now - path.getFirst().tick() >= 12) path.removeFirst();
            return path.isEmpty();
        });
    }

    @SubscribeEvent
    public static void worldFrame(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            projection = new Matrix4f(event.getProjectionMatrix());
            frameReady = true;
        }
    }

    @SubscribeEvent
    public static void composite(RenderGuiEvent.Pre event) {
        if (!frameReady) return;
        frameReady = false;
        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.level != world || trails.isEmpty() || projection == null) return;
        // At GUI Pre, vanilla and Oculus have both resolved the world into the main color target.
        // Never read a shader pack's G-buffer or sample the framebuffer currently being written.
        boolean depth = org.lwjgl.opengl.GL11.glIsEnabled(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);
        boolean cull = org.lwjgl.opengl.GL11.glIsEnabled(org.lwjgl.opengl.GL11.GL_CULL_FACE);
        boolean blend = org.lwjgl.opengl.GL11.glIsEnabled(org.lwjgl.opengl.GL11.GL_BLEND);
        boolean scissor = org.lwjgl.opengl.GL11.glIsEnabled(org.lwjgl.opengl.GL11.GL_SCISSOR_TEST);
        int[] scissorBox = new int[4];
        org.lwjgl.opengl.GL11.glGetIntegerv(org.lwjgl.opengl.GL11.GL_SCISSOR_BOX, scissorBox);
        boolean stencil = org.lwjgl.opengl.GL11.glIsEnabled(org.lwjgl.opengl.GL11.GL_STENCIL_TEST);
        boolean depthMask = org.lwjgl.opengl.GL11.glGetBoolean(org.lwjgl.opengl.GL11.GL_DEPTH_WRITEMASK);
        int depthFunc = org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL11.GL_DEPTH_FUNC);
        Matrix4f savedProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        var savedSorting = RenderSystem.getVertexSorting();
        try {
            RenderSystem.disableScissor();
            org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_STENCIL_TEST);
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            ensureChain(mc);
            if (pass == null) return;
            for (int i = 0; i < SEGMENTS; i++) pass.getEffect().safeGetUniform("Start" + i).set(0F, 0F, 0F, 0F);
            int index = 0;
            var camera = mc.gameRenderer.getMainCamera();
            for (var path : trails.values()) {
                var points = new ArrayList<>(path);
                for (int i = points.size() - 1; i > 0 && index < SEGMENTS; i--) {
                    Sample a = points.get(i), b = points.get(i - 1);
                    if (a.point().distanceToSqr(b.point()) < 0.0001) continue;
                    Vec3 midpoint = a.point().add(b.point()).scale(0.5);
                    if (midpoint.distanceToSqr(camera.getPosition()) < 2.25) continue;
                    var obstruction = world.clip(new ClipContext(camera.getPosition(), midpoint,
                            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, camera.getEntity()));
                    if (obstruction.getType() != HitResult.Type.MISS) continue;
                    Vector3f start = project(a.point()), end = project(b.point());
                    if (start == null || end == null) continue;
                    float radius = Math.min(0.008F, Math.max(0.0006F, Math.abs(projection.m11()) * 0.09F / start.z));
                    float age = world.getGameTime() + event.getPartialTick() - a.tick();
                    float fade = Math.max(0, 1 - age / 12F);
                    pass.getEffect().safeGetUniform("Start" + index).set(start.x, start.y, radius, fade);
                    pass.getEffect().safeGetUniform("End" + index).set(end.x, end.y);
                    index++;
                }
            }
            if (index == 0) return;
            pass.getEffect().safeGetUniform("EffectTime").set((world.getGameTime() % 24000 + event.getPartialTick()) / 20F);
            pass.getEffect().safeGetUniform("Aspect").set((float)target.width / Math.max(1, target.height));
            RenderSystem.disableDepthTest(); // Target allocation/resize can enable depth testing.
            RenderSystem.depthMask(false);
            chain.process(event.getPartialTick());
            // Alpha-overlay only. A failed/empty effect can never replace the whole world with black.
            var swap = chain.getTempTarget("swap");
            target.bindWrite(true);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            swap.blitToScreen(target.width, target.height, false);
        } catch (java.io.IOException | RuntimeException exception) {
            close(); failed = true;
            LogUtils.getLogger().error("Could not render Crimson Requiem gravitational trail", exception);
        } finally {
            mc.getMainRenderTarget().bindWrite(true);
            if (blend) RenderSystem.enableBlend(); else RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            if (depth) RenderSystem.enableDepthTest(); else RenderSystem.disableDepthTest();
            if (cull) RenderSystem.enableCull(); else RenderSystem.disableCull();
            if (scissor) RenderSystem.enableScissor(scissorBox[0], scissorBox[1], scissorBox[2], scissorBox[3]);
            if (stencil) org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_STENCIL_TEST);
            RenderSystem.depthMask(depthMask);
            RenderSystem.depthFunc(depthFunc);
            RenderSystem.setProjectionMatrix(savedProjection, savedSorting);
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
    }

    private static Vector3f project(Vec3 point) {
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 offset = point.subtract(camera.getPosition());
        float x = -dot(offset, camera.getLeftVector()), y = dot(offset, camera.getUpVector());
        float depth = dot(offset, camera.getLookVector());
        if (depth < 0.3F) return null;
        var clip = new Vector4f(x, y, -depth, 1).mul(projection);
        if (clip.w <= 0) return null;
        return new Vector3f(clip.x / clip.w * 0.5F + 0.5F, clip.y / clip.w * 0.5F + 0.5F, depth);
    }

    private static float dot(Vec3 a, Vector3f b) { return (float)(a.x * b.x + a.y * b.y + a.z * b.z); }

    private static void ensureChain(Minecraft mc) throws java.io.IOException {
        if (reload) { close(); reload = false; failed = false; verified = false; }
        if (failed) return;
        if (target != mc.getMainRenderTarget()) close();
        if (chain == null) {
            if (!verified) { verifyOverlay(mc); verified = true; }
            target = mc.getMainRenderTarget();
            chain = new PostChain(mc.getTextureManager(), mc.getResourceManager(), target,
                    new ResourceLocation(RequiemAddon.LEGACY_NAMESPACE, "shaders/post/crimson_trail.json"));
            pass = chain.addPass(RequiemAddon.LEGACY_NAMESPACE + ":crimson_trail", target, chain.getTempTarget("swap"));
            chain.getTempTarget("swap").setClearColor(0, 0, 0, 0);
            LogUtils.getLogger().info("Crimson Requiem gravitational trail shader loaded");
            width = height = -1;
        }
        if (width != target.width || height != target.height) {
            width = target.width; height = target.height; chain.resize(width, height);
        }
    }

    private static void close() {
        if (chain != null) chain.close();
        chain = null; pass = null; target = null;
    }

    /** Real GPU regression check: untouched pixels stay transparent and the scene survives composition. */
    private static void verifyOverlay(Minecraft mc) throws java.io.IOException {
        var scene = new com.mojang.blaze3d.pipeline.TextureTarget(64, 64, false, Minecraft.ON_OSX);
        PostChain test = null;
        Matrix4f savedProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        var sorting = RenderSystem.getVertexSorting();
        boolean oldDepth = org.lwjgl.opengl.GL11.glIsEnabled(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);
        boolean oldCull = org.lwjgl.opengl.GL11.glIsEnabled(org.lwjgl.opengl.GL11.GL_CULL_FACE);
        boolean oldBlend = org.lwjgl.opengl.GL11.glIsEnabled(org.lwjgl.opengl.GL11.GL_BLEND);
        boolean oldMask = org.lwjgl.opengl.GL11.glGetBoolean(org.lwjgl.opengl.GL11.GL_DEPTH_WRITEMASK);
        try {
            test = new PostChain(mc.getTextureManager(), mc.getResourceManager(), scene,
                    new ResourceLocation(RequiemAddon.LEGACY_NAMESPACE, "shaders/post/crimson_trail.json"));
            var output = test.getTempTarget("swap");
            output.setClearColor(0, 0, 0, 0);
            var effect = test.addPass(RequiemAddon.LEGACY_NAMESPACE + ":crimson_trail", scene, output).getEffect();
            test.resize(64, 64);
            scene.setClearColor(0.4F, 0.5F, 0.6F, 1);
            scene.clear(Minecraft.ON_OSX);
            effect.safeGetUniform("Start0").set(0.25F, 0.5F, 0.04F, 1F);
            effect.safeGetUniform("End0").set(0.75F, 0.5F);
            effect.safeGetUniform("Aspect").set(1F);
            RenderSystem.disableDepthTest();
            RenderSystem.disableCull();
            test.process(0);
            output.bindWrite(false);
            var pixel = org.lwjgl.BufferUtils.createByteBuffer(4);
            org.lwjgl.opengl.GL11.glReadPixels(2, 2, 1, 1, org.lwjgl.opengl.GL11.GL_RGBA, org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE, pixel);
            if ((pixel.get(3) & 255) != 0) throw new IllegalStateException("Trail overlay covers pixels outside the path");
            pixel.clear();
            org.lwjgl.opengl.GL11.glReadPixels(32, 32, 1, 1, org.lwjgl.opengl.GL11.GL_RGBA, org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE, pixel);
            if ((pixel.get(3) & 255) == 0) throw new IllegalStateException("Trail shader produced no visible pixels");
            scene.bindWrite(true);
            RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc();
            output.blitToScreen(64, 64, false);
            scene.bindWrite(false);
            pixel.clear();
            org.lwjgl.opengl.GL11.glReadPixels(2, 2, 1, 1, org.lwjgl.opengl.GL11.GL_RGBA, org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE, pixel);
            if (Math.abs((pixel.get(0) & 255) - 102) > 2 || Math.abs((pixel.get(2) & 255) - 153) > 2)
                throw new IllegalStateException("Trail composition overwrote the background");
            LogUtils.getLogger().info("Crimson trail GPU regression passed: transparent exterior, visible trail, background preserved");
        } finally {
            if (test != null) test.close();
            scene.destroyBuffers();
            RenderSystem.setProjectionMatrix(savedProjection, sorting);
            if (oldDepth) RenderSystem.enableDepthTest(); else RenderSystem.disableDepthTest();
            if (oldCull) RenderSystem.enableCull(); else RenderSystem.disableCull();
            if (oldBlend) RenderSystem.enableBlend(); else RenderSystem.disableBlend();
            RenderSystem.depthMask(oldMask);
            mc.getMainRenderTarget().bindWrite(true);
        }
    }

    @Mod.EventBusSubscriber(modid = RequiemAddon.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ReloadEvents {
        @SubscribeEvent public static void register(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener((ResourceManagerReloadListener)manager -> reload = true);
        }
    }
}
