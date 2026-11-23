package com.eldren.wutao.ramesses_requiem.boss;

import com.eldren.wutao.ramesses_requiem.RequiemAddon;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.ArrayList;
import java.util.List;

/** Local supernatural weather; never changes the world's saved weather or another player's view. */
@Mod.EventBusSubscriber(modid = RequiemAddon.MODID, value = Dist.CLIENT)
public final class WusheWeather {
    private static final int RANGE = 64, RAIN_RADIUS = 12;
    private static ClientLevel currentLevel;
    private static boolean active;
    private static BlockPos columnOrigin;
    private static final List<Column> columns = new ArrayList<>();
    private record Column(int x, int z, int bottom, int top, int seed) {}

    public static boolean active() {
        Minecraft mc = Minecraft.getInstance();
        return active && mc.level != null && mc.level == currentLevel;
    }
    private static void clear() { active = false; currentLevel = null; columnOrigin = null; columns.clear(); }

    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) { clear(); return; }
        if (mc.isPaused()) return;
        if (currentLevel != mc.level) { clear(); currentLevel = mc.level; }
        Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
        active = !mc.level.getEntitiesOfClass(WusheBoss.class, new AABB(camera, camera).inflate(RANGE),
                boss -> boss.isAlive() && boss.distanceToSqr(camera) <= RANGE * RANGE).isEmpty();
        if (!active) { columns.clear(); columnOrigin = null; return; }
        BlockPos origin = BlockPos.containing(camera);
        if (!origin.equals(columnOrigin) || mc.level.getGameTime() % 5 == 0) rebuildColumns(mc.level, origin);
        if (mc.level.getGameTime() % 40 == 0 && !columns.isEmpty()) {
            mc.level.playLocalSound(camera.x, camera.y + 2, camera.z, SoundEvents.WEATHER_RAIN,
                    SoundSource.WEATHER, .28F, .65F, false);
        }
    }

    private static void rebuildColumns(ClientLevel level, BlockPos origin) {
        columnOrigin = origin; columns.clear();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -RAIN_RADIUS; dx <= RAIN_RADIUS; dx++) for (int dz = -RAIN_RADIUS; dz <= RAIN_RADIUS; dz++) {
            if (dx * dx + dz * dz > RAIN_RADIUS * RAIN_RADIUS) continue;
            int x = origin.getX() + dx, z = origin.getZ() + dz, y = origin.getY();
            pos.set(x, y, z);
            if (!level.hasChunkAt(pos) || !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) continue;
            int bottom = y, top = y + 1;
            // Stop at nearby floors/ceilings, including Nether caves, without drawing through blocks.
            while (bottom > Math.max(level.getMinBuildHeight(), y - 10)) {
                pos.set(x, bottom - 1, z);
                if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) break;
                bottom--;
            }
            while (top < Math.min(level.getMaxBuildHeight(), y + 10)) {
                pos.set(x, top, z);
                if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) break;
                top++;
            }
            columns.add(new Column(x, z, bottom, top, x * 3121 ^ z * 418711));
        }
    }

    @SubscribeEvent public static void fogColor(ViewportEvent.ComputeFogColor event) {
        if (!active() || event.getCamera().getFluidInCamera() != FogType.NONE) return;
        event.setRed(.065F); event.setGreen(.018F); event.setBlue(.095F);
    }
    @SubscribeEvent public static void fog(ViewportEvent.RenderFog event) {
        if (!active() || event.getCamera().getFluidInCamera() != FogType.NONE) return;
        // Keep blindness/darkness if they already impose a shorter view distance.
        event.setNearPlaneDistance(Math.min(2F, event.getNearPlaneDistance()));
        event.setFarPlaneDistance(Math.min(15F, event.getFarPlaneDistance()));
        event.setFogShape(FogShape.SPHERE); event.setCanceled(true);
    }
    @SubscribeEvent public static void rain(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER || !active() || columns.isEmpty()
                || event.getCamera().getFluidInCamera() != FogType.NONE) return;
        Vec3 camera = event.getCamera().getPosition();
        float time = (currentLevel.getGameTime() % 24000) + event.getPartialTick();
        var matrix = event.getPoseStack().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        RenderType type = RainType.RAIN;
        type.setupRenderState();
        try {
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
            for (Column column : columns) {
                double x = column.x + .5 - camera.x, z = column.z + .5 - camera.z;
                double distance = Math.sqrt(x * x + z * z);
                if (distance < .7 || distance >= RAIN_RADIUS) continue;
                float sideX = (float)(-z / distance * .5), sideZ = (float)(x / distance * .5);
                float top = (float)(column.top - camera.y), bottom = (float)(column.bottom - camera.y);
                float speed = .38F + (column.seed & 15) * .006F;
                float scroll = time * speed + (column.seed & 255) / 256F;
                float vTop = column.top * .25F + scroll, vBottom = column.bottom * .25F + scroll;
                float shade = (column.seed & 3) == 0 ? .24F : .8F;
                float alpha = .8F * (1 - (float)(distance * distance / (RAIN_RADIUS * RAIN_RADIUS)));
                int light = LightTexture.FULL_BRIGHT;
                buffer.vertex(matrix, (float)x - sideX, top, (float)z - sideZ).uv(0, vTop).color(.46F * shade, .08F * shade, .68F * shade, alpha).uv2(light).endVertex();
                buffer.vertex(matrix, (float)x + sideX, top, (float)z + sideZ).uv(1, vTop).color(.46F * shade, .08F * shade, .68F * shade, alpha).uv2(light).endVertex();
                buffer.vertex(matrix, (float)x + sideX, bottom, (float)z + sideZ).uv(1, vBottom).color(.46F * shade, .08F * shade, .68F * shade, alpha).uv2(light).endVertex();
                buffer.vertex(matrix, (float)x - sideX, bottom, (float)z - sideZ).uv(0, vBottom).color(.46F * shade, .08F * shade, .68F * shade, alpha).uv2(light).endVertex();
            }
            BufferUploader.drawWithShader(buffer.end());
        } finally { type.clearRenderState(); }
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { clear(); }

    private static final class RainType extends RenderType {
        private static final RenderType RAIN = create(RequiemAddon.MODID + ":wushe_rain", DefaultVertexFormat.PARTICLE,
                VertexFormat.Mode.QUADS, 65536, false, true, CompositeState.builder()
                        .setShaderState(new ShaderStateShard(GameRenderer::getParticleShader))
                        .setTextureState(new TextureStateShard(new ResourceLocation("minecraft", "textures/environment/rain.png"), false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDepthTestState(LEQUAL_DEPTH_TEST)
                        .setCullState(NO_CULL).setLightmapState(LIGHTMAP).setWriteMaskState(COLOR_WRITE)
                        .setOutputState(WEATHER_TARGET).createCompositeState(false));
        private RainType(String name, VertexFormat format, VertexFormat.Mode mode, int size, boolean crumbling,
                         boolean sort, Runnable setup, Runnable clear) { super(name, format, mode, size, crumbling, sort, setup, clear); }
    }
    private WusheWeather() {}
}
