package com.eldren.wutao.ramesses_requiem;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;

public final class CrimsonRequiemRenderer extends EntityRenderer<CrimsonRequiemProjectile> {
    private final ItemRenderer items;
    public CrimsonRequiemRenderer(EntityRendererProvider.Context context) { super(context); items = context.getItemRenderer(); }
    @Override public void render(CrimsonRequiemProjectile entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffer, int light) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
        if (entity.isWave()) {
            pose.popPose();
            super.render(entity, yaw, partial, pose, buffer, light);
            return;
        }
        pose.mulPose(Axis.XP.rotationDegrees(90));
        pose.mulPose(Axis.ZP.rotationDegrees((entity.tickCount + partial) * 45));
        pose.scale(1.5F, 1.5F, 1.5F);
        items.renderStatic(entity.weapon(), ItemDisplayContext.FIXED, 15728880, OverlayTexture.NO_OVERLAY, pose, buffer, entity.level(), entity.getId());
        pose.popPose();
        super.render(entity, yaw, partial, pose, buffer, light);
    }
    @Override public ResourceLocation getTextureLocation(CrimsonRequiemProjectile entity) { return TextureAtlas.LOCATION_BLOCKS; }
}
