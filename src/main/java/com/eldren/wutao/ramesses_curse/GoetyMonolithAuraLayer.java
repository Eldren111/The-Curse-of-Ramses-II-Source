package com.eldren.wutao.ramesses_curse;

import com.eldren.wutao.ramesses_curse.integration.goety.GoetyObsidianMonolithCompat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

@OnlyIn(Dist.CLIENT)
public final class GoetyMonolithAuraLayer<T extends LivingEntity, M extends EntityModel<T>>
        extends RenderLayer<T, M> {
    private static final ResourceLocation APOSTLE_AURA =
            new ResourceLocation("goety", "textures/entity/cultist/apostle_aura.png");

    private GoetyMonolithAuraLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {
        if (entity.isDeadOrDying()
                || !entity.getPersistentData().getBoolean(GoetyObsidianMonolithCompat.CLIENT_PROTECTION_ACTIVE)) {
            return;
        }

        float animationTime = entity.tickCount + partialTick;
        float xOffset = Mth.cos(animationTime * 0.02F) * 3.0F % 1.0F;
        float yOffset = animationTime * 0.01F % 1.0F;
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.energySwirl(APOSTLE_AURA, xOffset, yOffset));
        getParentModel().renderToBuffer(
                poseStack,
                consumer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                0.5F,
                0.5F,
                0.5F,
                1.0F);
    }

    public static void register(EntityRenderersEvent.AddLayers event) {
        if (!ModList.get().isLoaded("goety")) {
            return;
        }
        for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES.getValues()) {
            EntityRenderer<?> renderer = getRendererUnchecked(event, type);
            if (renderer instanceof LivingEntityRenderer<?, ?> livingRenderer) {
                addLayerUnchecked(livingRenderer);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static EntityRenderer<?> getRendererUnchecked(
            EntityRenderersEvent.AddLayers event,
            EntityType<?> type) {
        return event.getEntityRenderer((EntityType) type);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void addLayerUnchecked(LivingEntityRenderer renderer) {
        renderer.addLayer(new GoetyMonolithAuraLayer(renderer));
    }
}
