package com.eldren.wutao.ramesses_requiem.boss;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class WusheRenderer extends HumanoidMobRenderer<WusheBoss, PiglinModel<WusheBoss>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/entity/piglin/piglin.png");
    public WusheRenderer(EntityRendererProvider.Context context) { super(context, new Model(context.bakeLayer(ModelLayers.PIGLIN)), .6F); }
    @Override public ResourceLocation getTextureLocation(WusheBoss entity) { return TEXTURE; }
    @Override protected void scale(WusheBoss entity, PoseStack pose, float partialTick) { pose.scale(1.15F, 1.15F, 1.15F); }
    private static final class Model extends PiglinModel<WusheBoss> {
        Model(ModelPart root) { super(root); }
        @Override public void setupAnim(WusheBoss boss, float walk, float speed, float age, float yaw, float pitch) {
            super.setupAnim(boss, walk, speed, age, yaw, pitch);
            float t = boss.skillAge() + (age - boss.tickCount);
            switch (boss.skill()) {
                case 1 -> {
                    rightArm.xRot = t < 24 ? -2.6F : -.4F;
                    if (t >= 16 && t < 30) { leftArm.xRot = -2.4F; leftLeg.xRot = -.45F; rightLeg.xRot = .45F; }
                }
                case 2 -> { if (t < 13) leftLeg.xRot = -1.15F * Mth.sin(t / 13 * Mth.PI); else { rightArm.xRot = -1.3F; rightArm.yRot = Mth.sin((t - 13) / 16 * Mth.PI) * 1.5F; } }
                case 3 -> { rightArm.xRot = -1.5F; rightArm.zRot = Mth.sin(t * .32F) * 1.25F; }
                case 4 -> { leftArm.xRot = -1.25F; rightArm.xRot = t < 60 ? -.5F : -1.8F; }
                case 5 -> { leftArm.xRot = -1.55F; leftArm.yRot = .2F; }
            }
            leftSleeve.copyFrom(leftArm); rightSleeve.copyFrom(rightArm); leftPants.copyFrom(leftLeg); rightPants.copyFrom(rightLeg);
        }
    }
}
