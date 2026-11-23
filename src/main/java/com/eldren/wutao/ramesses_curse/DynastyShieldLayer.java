package com.eldren.wutao.ramesses_curse;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.event.EntityRenderersEvent;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public final class DynastyShieldLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/entity/wither/wither_armor.png");
    private DynastyShieldLayer(RenderLayerParent<T, M> parent) { super(parent); }
    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, T entity,
            float swing, float amount, float partial, float age, float yaw, float pitch) {
        var state = DynastyShieldVisuals.state(entity);
        var player = Minecraft.getInstance().player;
        if (state == null || entity.isDeadOrDying() || entity.isInvisible()
                || player == null || player.distanceToSqr(entity) > 4096) return;
        float flash = Math.max(0, 1 - (entity.tickCount + partial - state.hitTick()) / 8F);
        float time = entity.tickCount + partial;
        var vertices = buffers.getBuffer(RenderType.energySwirl(TEXTURE, time * 0.003F % 1, time * 0.002F % 1));
        pose.pushPose();
        pose.scale(1.015F, 1.015F, 1.015F);
        getParentModel().renderToBuffer(pose, vertices, 15728880, OverlayTexture.NO_OVERLAY,
                0.32F + flash * 0.4F, 0.16F + flash * 0.4F, 0.48F, 1F);
        pose.popPose();
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void register(EntityRenderersEvent.AddLayers event) {
        Set<Object> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (EntityType<?> type : net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getValues()) {
            var renderer = event.getEntityRenderer((EntityType)type);
            if (renderer instanceof LivingEntityRenderer living && seen.add(renderer)) {
                living.addLayer(new DynastyShieldLayer(living));
            }
        }
    }
}

