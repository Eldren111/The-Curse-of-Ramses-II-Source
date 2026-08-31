package com.eldren.wutao.ramesses_curse;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;

public final class RamessesEndCrystalRenderer extends EntityRenderer<RamessesEndCrystalEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/end_crystal/end_crystal.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(TEXTURE);
    private static final float SIN_45 = (float) Math.sin(Math.PI / 4.0D);
    private final ModelPart cube;
    private final ModelPart glass;
    private final ModelPart base;

    public RamessesEndCrystalRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        ModelPart root = context.bakeLayer(ModelLayers.END_CRYSTAL);
        this.glass = root.getChild("glass");
        this.cube = root.getChild("cube");
        this.base = root.getChild("base");
    }

    @Override
    public void render(RamessesEndCrystalEntity entity, float yaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        float bob = EndCrystalRenderer.getY(entity, partialTick);
        float rotation = (entity.time + partialTick) * 3.0F;
        VertexConsumer consumer = buffers.getBuffer(RENDER_TYPE);
        int glassColor = entity.isCracked() ? 0xFFFF6105 : 0xFFFFB814;
        int innerGlassColor = entity.isCracked() ? 0xFFE85003 : 0xFFFFA30A;
        int cubeColor = entity.isCracked() ? 0xFFFF3B00 : 0xFFFFB000;
        int light = LightTexture.FULL_BRIGHT;

        poseStack.pushPose();
        poseStack.scale(2.0F, 2.0F, 2.0F);
        poseStack.translate(0.0F, -0.5F, 0.0F);
        if (entity.showsBottom()) {
            this.base.render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, 0xFFB86108);
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.translate(0.0F, 1.5F + bob / 2.0F, 0.0F);
        poseStack.mulPose(new Quaternionf().setAngleAxis((float) Math.PI / 3.0F, SIN_45, 0.0F, SIN_45));
        this.glass.render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, glassColor);
        poseStack.scale(0.875F, 0.875F, 0.875F);
        poseStack.mulPose(new Quaternionf().setAngleAxis((float) Math.PI / 3.0F, SIN_45, 0.0F, SIN_45));
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        this.glass.render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, innerGlassColor);
        poseStack.scale(0.875F, 0.875F, 0.875F);
        poseStack.mulPose(new Quaternionf().setAngleAxis((float) Math.PI / 3.0F, SIN_45, 0.0F, SIN_45));
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        this.cube.render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, cubeColor);
        poseStack.popPose();
        poseStack.popPose();

        BlockPos beamTarget = entity.getBeamTarget();
        if (beamTarget != null) {
            float dx = (float) (beamTarget.getX() + 0.5D - entity.getX());
            float dy = (float) (beamTarget.getY() + 0.5D - entity.getY());
            float dz = (float) (beamTarget.getZ() + 0.5D - entity.getZ());
            poseStack.translate(dx, dy, dz);
            EnderDragonRenderer.renderCrystalBeams(-dx, -dy + bob, -dz,
                    partialTick, entity.time, poseStack, buffers, light);
        }
        super.render(entity, yaw, partialTick, poseStack, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(RamessesEndCrystalEntity entity) {
        return TEXTURE;
    }
}
