package com.eldren.wutao.ramesses_curse;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;

public final class DuatSunEaterRenderer extends EntityRenderer<DuatSunEaterEntity> {
    private static final ResourceLocation DRAGON_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "textures/entity/duat_sun_eater.png");
    private static final ResourceLocation GLOW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "textures/entity/duat_sun_eater_glow.png");
    private static final ResourceLocation EYES_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "textures/entity/duat_sun_eater_eyes.png");
    private static final ResourceLocation CROWN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "textures/entity/duat_sun_eater_crown.png");
    private static final ResourceLocation APOSTLE_SHIELD =
            ResourceLocation.fromNamespaceAndPath("goety", "textures/entity/cultist/apostle_aura.png");
    private static final RenderType BASE = RenderType.entityCutoutNoCull(DRAGON_TEXTURE);
    private static final RenderType CROWN = RenderType.entityCutoutNoCull(CROWN_TEXTURE);
    private static final RenderType EMBER_GLOW = RenderType.eyes(GLOW_TEXTURE);
    private static final RenderType EYES = RenderType.eyes(EYES_TEXTURE);
    private static final int BASE_COLOR = FastColor.ARGB32.colorFromFloat(1.0F, 1.0F, 1.0F, 1.0F);
    private static final int GLOW_COLOR = FastColor.ARGB32.colorFromFloat(1.0F, 1.0F, 1.0F, 1.0F);
    private static final int EYE_COLOR = FastColor.ARGB32.colorFromFloat(1.0F, 1.0F, 1.0F, 1.0F);
    private static final int SHIELD_COLOR = FastColor.ARGB32.colorFromFloat(1.0F, 0.72F, 0.32F, 1.0F);
    private static final float HALF_SQRT_THREE = (float) (Math.sqrt(3.0D) / 2.0D);
    private final EnderDragonRenderer.DragonModel model;
    private final DuatSunEaterCrownModel crownModel;

    public DuatSunEaterRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        this.model = new EnderDragonRenderer.DragonModel(context.bakeLayer(ModelLayers.ENDER_DRAGON));
        this.crownModel = new DuatSunEaterCrownModel();
    }

    @Override
    public void render(DuatSunEaterEntity entity, float yaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        if (entity.isInvisible() && entity.dragonDeathTime <= 0) {
            return;
        }
        poseStack.pushPose();
        float bodyYaw = (float) entity.getLatencyPos(7, partialTick)[0];
        float bodyPitch = (float) (entity.getLatencyPos(5, partialTick)[1]
                - entity.getLatencyPos(10, partialTick)[1]);
        poseStack.mulPose(Axis.YP.rotationDegrees(-bodyYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(bodyPitch * 10.0F));
        poseStack.translate(0.0F, 0.0F, 1.0F);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        boolean hurt = entity.hurtTime > 0;
        this.model.prepareMobModel(entity, 0.0F, 0.0F, partialTick);

        VertexConsumer base = buffers.getBuffer(BASE);
        int combatStage = entity.getCombatStage();
        float greenTint = combatStage == 1 ? 0.88F : combatStage == 2 ? 0.62F : 0.38F;
        float blueTint = combatStage == 1 ? 0.88F : combatStage == 2 ? 0.62F : 0.42F;
        int stageColor = FastColor.ARGB32.colorFromFloat(1.0F, 1.0F, greenTint, blueTint);
        this.model.renderToBuffer(poseStack, base, packedLight,
                OverlayTexture.pack(0.0F, hurt), stageColor);
        this.crownModel.render(entity, partialTick, poseStack, buffers.getBuffer(CROWN), packedLight);

        VertexConsumer glow = buffers.getBuffer(EMBER_GLOW);
        this.model.renderToBuffer(poseStack, glow, LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, GLOW_COLOR);

        VertexConsumer eyes = buffers.getBuffer(EYES);
        this.model.renderToBuffer(poseStack, eyes, LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, EYE_COLOR);

        if (entity.dragonDeathTime > 0) {
            renderRedDeathRays(entity, partialTick, poseStack, buffers.getBuffer(RenderType.lightning()));
        }

        if (entity.isObsidianShieldActive()) {
            float shieldTime = entity.tickCount + partialTick;
            float shieldX = Mth.cos(shieldTime * 0.02F) * 3.0F % 1.0F;
            float shieldY = shieldTime * 0.01F % 1.0F;
            VertexConsumer shield = buffers.getBuffer(RenderType.energySwirl(APOSTLE_SHIELD, shieldX, shieldY));
            this.model.renderToBuffer(poseStack, shield, LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY, SHIELD_COLOR);
        }
        poseStack.popPose();

        if (entity.nearestCrystal != null) {
            poseStack.pushPose();
            float dx = (float) (entity.nearestCrystal.getX() - Mth.lerp((double) partialTick, entity.xo, entity.getX()));
            float dy = (float) (entity.nearestCrystal.getY() - Mth.lerp((double) partialTick, entity.yo, entity.getY()));
            float dz = (float) (entity.nearestCrystal.getZ() - Mth.lerp((double) partialTick, entity.zo, entity.getZ()));
            EnderDragonRenderer.renderCrystalBeams(dx,
                    dy + EndCrystalRenderer.getY(entity.nearestCrystal, partialTick), dz,
                    partialTick, entity.tickCount, poseStack, buffers, packedLight);
            poseStack.popPose();
        }
        super.render(entity, yaw, partialTick, poseStack, buffers, packedLight);
    }

    private static void renderRedDeathRays(DuatSunEaterEntity entity, float partialTick,
                                           PoseStack poseStack, VertexConsumer consumer) {
        float progress = ((float) entity.dragonDeathTime + partialTick) / 200.0F;
        float finalBurst = Math.min(progress > 0.8F ? (progress - 0.8F) / 0.2F : 0.0F, 1.0F);
        RandomSource random = RandomSource.create(432L);
        poseStack.pushPose();
        poseStack.translate(0.0F, -1.0F, -2.0F);
        int rayCount = Mth.floor((progress + progress * progress) * 36.0F);
        for (int index = 0; index < rayCount; index++) {
            poseStack.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0F + progress * 110.0F));
            float length = random.nextFloat() * 24.0F + 7.0F + finalBurst * 16.0F;
            float width = random.nextFloat() * 2.5F + 1.2F + finalBurst * 3.0F;
            int alpha = Mth.floor(255.0F * (1.0F - finalBurst));
            renderRedRay(consumer, poseStack.last().pose(), length, width, alpha);
        }
        poseStack.popPose();
    }

    private static void renderRedRay(VertexConsumer consumer, Matrix4f matrix,
                                     float length, float width, int alpha) {
        float x1 = -HALF_SQRT_THREE * width;
        float x2 = HALF_SQRT_THREE * width;
        float z1 = -0.5F * width;
        float z2 = width;
        redRayFace(consumer, matrix, x1, z1, x2, z1, length, alpha);
        redRayFace(consumer, matrix, x2, z1, 0.0F, z2, length, alpha);
        redRayFace(consumer, matrix, 0.0F, z2, x1, z1, length, alpha);
    }

    private static void redRayFace(VertexConsumer consumer, Matrix4f matrix,
                                   float firstX, float firstZ, float secondX, float secondZ,
                                   float length, int alpha) {
        redRayCenter(consumer, matrix, alpha);
        consumer.addVertex(matrix, firstX, length, firstZ).setColor(255, 12, 4, 0);
        consumer.addVertex(matrix, secondX, length, secondZ).setColor(130, 0, 8, 0);
        redRayCenter(consumer, matrix, alpha);
    }

    private static void redRayCenter(VertexConsumer consumer, Matrix4f matrix, int alpha) {
        consumer.addVertex(matrix, 0.0F, 0.0F, 0.0F)
                .setColor(255, 218, 190, alpha);
    }

    @Override
    public ResourceLocation getTextureLocation(DuatSunEaterEntity entity) {
        return DRAGON_TEXTURE;
    }
}
