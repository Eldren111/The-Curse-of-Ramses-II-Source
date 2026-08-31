package com.eldren.wutao.ramesses_curse;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class UraeusFlareRenderer extends EntityRenderer<UraeusFlareEntity> {
    public UraeusFlareRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(UraeusFlareEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        Vec3 origin = entity.getPosition(partialTick);
        Vec3 cameraPosition = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        VertexConsumer consumer = bufferSource.getBuffer(UraeusRenderTypes.spiritGlow());
        float age = entity.tickCount + partialTick;
        boolean branch = entity.isBranchFlare();

        renderTrail(entity, partialTick, poseStack, consumer, origin, cameraPosition, age);
        UraeusSpiritTrailRenderer.renderDroplet(
                poseStack.last(),
                consumer,
                Vec3.ZERO,
                origin,
                entity.getDeltaMovement(),
                cameraPosition,
                age,
                entity.isRainEmpowered(),
                branch ? 0.78F : 1.0F,
                !branch);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderTrail(UraeusFlareEntity entity, float partialTick, PoseStack poseStack,
                                    VertexConsumer consumer, Vec3 origin, Vec3 cameraPosition, float age) {
        int length = entity.getTrailLength();
        if (length < 3) {
            return;
        }

        List<Vec3> controls = new ArrayList<>(length);
        for (int index = 0; index < length; index++) {
            controls.add(entity.getTrailPosition(index, partialTick));
        }
        List<Vec3> points = SmoothTrailGeometry.catmullRom(controls, entity.isBranchFlare() ? 1 : 2);
        UraeusSpiritTrailRenderer.render(poseStack.last(), consumer, points, origin, cameraPosition,
                age, entity.isRainEmpowered(), entity.isBranchFlare() ? 0.72F : 1.0F, entity.getId());
    }

    @Override
    public ResourceLocation getTextureLocation(UraeusFlareEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
