package com.eldren.wutao.ramesses_curse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.Map;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID, value = Dist.CLIENT)
public final class DynastyShieldVisuals {
    private static final Map<LivingEntity, State> STATES = new WeakHashMap<>();
    public record State(float current, float max, int hitTick) {}
    public static State state(LivingEntity entity) { return STATES.get(entity); }
    public static void sync(DynastyShieldSyncPacket packet) {
        var level = Minecraft.getInstance().level;
        if (level == null || !(level.getEntity(packet.entityId()) instanceof LivingEntity entity)) return;
        var old = STATES.get(entity);
        int hitTick = old != null && packet.current() < old.current() ? entity.tickCount
                : old == null ? -100 : old.hitTick();
        if (packet.current() <= 0 || packet.max() <= 0) STATES.remove(entity);
        else STATES.put(entity, new State(packet.current(), packet.max(), hitTick));
    }
    @SubscribeEvent
    public static void renderValue(RenderLivingEvent.Post<?, ?> event) {
        var entity = event.getEntity();
        var state = state(entity);
        var mc = Minecraft.getInstance();
        if (state == null || entity.isDeadOrDying() || entity.isInvisible() || mc.options.hideGui
                || mc.player == null || mc.player.distanceToSqr(entity) > 1024) return;
        if (mc.crosshairPickEntity != entity && entity.tickCount - state.hitTick() > 40) return;
        // Ignore the mob's geometry, but do not expose its shield through solid walls.
        var camera = mc.gameRenderer.getMainCamera().getPosition();
        var center = entity.getBoundingBox().getCenter();
        if (entity.level().clip(new net.minecraft.world.level.ClipContext(camera, center,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE, mc.player)).getType()
                != net.minecraft.world.phys.HitResult.Type.MISS) return;
        var pose = event.getPoseStack();
        pose.pushPose();
        // Entity-local origin: place at the middle of the body, never above its head.
        pose.translate(0, entity.getBbHeight() * 0.5, 0);
        pose.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        // Keep the label at body center; the text pass ignores model depth.
        pose.scale(-0.025F, -0.025F, 0.025F);
        Component text = Component.translatable("hud.ramesses_curse.dynasty_shield",
                (int) Math.ceil(state.current()), (int) Math.ceil(state.max()));
        mc.font.drawInBatch(text, -mc.font.width(text) / 2F, -4, 0xFFFFDC91, true,
                pose.last().pose(), event.getMultiBufferSource(), Font.DisplayMode.SEE_THROUGH, 0, 15728880);
        pose.popPose();
    }
}
