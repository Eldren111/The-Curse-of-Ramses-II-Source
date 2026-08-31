package com.eldren.wutao.ramesses_curse;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.util.FastColor;

final class DuatSunEaterCrownModel {
    private final ModelPart crown;

    DuatSunEaterCrownModel() {
        this.crown = createBodyLayer().bakeRoot().getChild("crown");
    }

    private static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition crown = mesh.getRoot().addOrReplaceChild("crown",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-9.0F, -11.0F, -11.0F, 18.0F, 4.0F, 13.0F)
                        .texOffs(0, 0).addBox(-11.0F, -4.0F, -9.0F, 3.0F, 5.0F, 13.0F)
                        .texOffs(0, 0).mirror().addBox(8.0F, -4.0F, -9.0F, 3.0F, 5.0F, 13.0F)
                        .texOffs(0, 0).addBox(-6.0F, 1.0F, -25.0F, 2.0F, 9.0F, 3.0F)
                        .texOffs(0, 0).mirror().addBox(4.0F, 1.0F, -25.0F, 2.0F, 9.0F, 3.0F),
                PartPose.ZERO);

        addHorn(crown, "left", 1.0F);
        addHorn(crown, "right", -1.0F);
        crown.addOrReplaceChild("center_spike",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-1.5F, -11.0F, -1.5F, 3.0F, 12.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, -9.0F, -3.0F, 0.72F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    private static void addHorn(PartDefinition crown, String name, float side) {
        crown.addOrReplaceChild(name + "_horn_base",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-2.5F, -13.0F, -2.5F, 5.0F, 14.0F, 5.0F),
                PartPose.offsetAndRotation(side * 6.0F, -8.0F, -1.0F,
                        0.44F, 0.0F, side * 0.31F));
        crown.addOrReplaceChild(name + "_horn_mid",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-2.0F, -11.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offsetAndRotation(side * 10.0F, -17.0F, 4.0F,
                        0.86F, 0.0F, side * 0.43F));
        crown.addOrReplaceChild(name + "_horn_tip",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-1.0F, -9.0F, -1.0F, 2.0F, 10.0F, 2.0F),
                PartPose.offsetAndRotation(side * 13.0F, -22.0F, 12.0F,
                        1.18F, 0.0F, side * 0.36F));
    }

    void render(DuatSunEaterEntity entity, float partialTick, PoseStack poseStack,
                VertexConsumer consumer, int packedLight) {
        poseStack.pushPose();
        float flap = Mth.lerp(partialTick, entity.oFlapTime, entity.flapTime);
        float bob = (float) (Math.sin(flap * Mth.TWO_PI - 1.0F) + 1.0D);
        bob = (bob * bob + bob * 2.0F) * 0.05F;
        poseStack.translate(0.0F, bob - 2.0F, -3.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(bob * 2.0F));

        float headX = 0.0F;
        float headY = 20.0F;
        float headZ = -12.0F;
        double[] center = entity.getLatencyPos(6, partialTick);
        float neckYaw = Mth.wrapDegrees((float) (entity.getLatencyPos(5, partialTick)[0]
                - entity.getLatencyPos(10, partialTick)[0]));
        float averageYaw = Mth.wrapDegrees((float) (entity.getLatencyPos(5, partialTick)[0] + neckYaw / 2.0F));
        float cycle = flap * Mth.TWO_PI;

        for (int index = 0; index < 5; index++) {
            double[] segment = entity.getLatencyPos(5 - index, partialTick);
            float wave = Mth.cos(index * 0.45F + cycle) * 0.15F;
            float yaw = Mth.wrapDegrees((float) (segment[0] - center[0])) * Mth.DEG_TO_RAD * 1.5F;
            float pitch = wave + entity.getHeadPartYOffset(index, center, segment)
                    * Mth.DEG_TO_RAD * 1.5F * 5.0F;
            headY += Mth.sin(pitch) * 10.0F;
            headZ -= Mth.cos(yaw) * Mth.cos(pitch) * 10.0F;
            headX -= Mth.sin(yaw) * Mth.cos(pitch) * 10.0F;
        }

        double[] head = entity.getLatencyPos(0, partialTick);
        this.crown.x = headX;
        this.crown.y = headY;
        this.crown.z = headZ;
        this.crown.yRot = Mth.wrapDegrees((float) (head[0] - center[0])) * Mth.DEG_TO_RAD;
        this.crown.xRot = Mth.wrapDegrees(entity.getHeadPartYOffset(6, center, head))
                * Mth.DEG_TO_RAD * 1.5F * 5.0F;
        this.crown.zRot = -Mth.wrapDegrees((float) (head[0] - averageYaw)) * Mth.DEG_TO_RAD;
        this.crown.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY,
                FastColor.ARGB32.colorFromFloat(1.0F, 1.0F, 1.0F, 1.0F));
        poseStack.popPose();
    }
}
