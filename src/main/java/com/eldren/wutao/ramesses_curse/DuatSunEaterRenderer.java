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
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraftforge.fml.ModList;
import org.joml.Matrix4f;

public final class DuatSunEaterRenderer extends EntityRenderer<DuatSunEaterEntity> {
    private static final ResourceLocation DRAGON_TEXTURE =
            new ResourceLocation(RamessesCurseMod.MODID, "textures/entity/duat_sun_eater.png");
    private static final ResourceLocation GLOW_TEXTURE =
            new ResourceLocation(RamessesCurseMod.MODID, "textures/entity/duat_sun_eater_glow.png");
    private static final ResourceLocation EYES_TEXTURE =
            new ResourceLocation(RamessesCurseMod.MODID, "textures/entity/duat_sun_eater_eyes.png");
    private static final ResourceLocation CROWN_TEXTURE =
            new ResourceLocation(RamessesCurseMod.MODID, "textures/entity/duat_sun_eater_crown.png");
    private static final ResourceLocation APOSTLE_SHIELD =
            new ResourceLocation("goety", "textures/entity/cultist/apostle_aura.png");
    private static final RenderType BASE = RenderType.entityCutoutNoCull(DRAGON_TEXTURE);
    private static final RenderType CROWN = RenderType.entityCutoutNoCull(CROWN_TEXTURE);
    private static final RenderType EMBER_GLOW = RenderType.eyes(GLOW_TEXTURE);
    private static final RenderType EYES = RenderType.eyes(EYES_TEXTURE);
    private static final float HALF_SQRT_THREE = (float) (Math.sqrt(3.0D) / 2.0D);
    private final EnderDragonRenderer.DragonModel model;
    private final DuatSunEaterCrownModel crownModel;
    private EnderDragon modelDriver;

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
        // The vanilla mesh is reused only as geometry. Feed it a client-side driver
        // so its animation code has the vanilla state object it expects; the
        // rendered entity itself remains DuatSunEaterEntity.
        if (this.modelDriver == null || this.modelDriver.level() != entity.level()) {
            this.modelDriver = new EnderDragon(net.minecraft.world.entity.EntityType.ENDER_DRAGON, entity.level());
        }
        entity.copyLatencyHistory(this.modelDriver.positions);
        this.modelDriver.posPointer = entity.getLatencyPointer();
        this.modelDriver.setYRot(entity.getYRot());
        this.modelDriver.oFlapTime = entity.oFlapTime;
        this.modelDriver.flapTime = entity.flapTime;
        this.model.prepareMobModel(this.modelDriver, 0.0F, 0.0F, partialTick);
        boolean hurt = entity.hurtTime > 0;
        VertexConsumer base = buffers.getBuffer(BASE);
        int combatStage = entity.getCombatStage();
        float greenTint = combatStage == 1 ? 0.88F : combatStage == 2 ? 0.62F : 0.38F;
        float blueTint = combatStage == 1 ? 0.88F : combatStage == 2 ? 0.62F : 0.42F;
        this.model.renderToBuffer(poseStack, base, packedLight, OverlayTexture.pack(0.0F, hurt),
                1.0F, greenTint, blueTint, 1.0F);
        this.crownModel.render(entity, partialTick, poseStack, buffers.getBuffer(CROWN), packedLight);

        VertexConsumer glow = buffers.getBuffer(EMBER_GLOW);
        this.model.renderToBuffer(poseStack, glow, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);

        VertexConsumer eyes = buffers.getBuffer(EYES);
        this.model.renderToBuffer(poseStack, eyes, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);

        if (entity.dragonDeathTime > 0) {
            renderRedDeathRays(entity, partialTick, poseStack, buffers.getBuffer(RenderType.lightning()));
        }

        if (entity.isObsidianShieldActive() && ModList.get().isLoaded("goety")) {
            float shieldTime = entity.tickCount + partialTick;
            float shieldX = Mth.cos(shieldTime * 0.02F) * 3.0F % 1.0F;
            float shieldY = shieldTime * 0.01F % 1.0F;
            VertexConsumer shield = buffers.getBuffer(RenderType.energySwirl(APOSTLE_SHIELD, shieldX, shieldY));
            this.model.renderToBuffer(poseStack, shield, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                    0.72F, 0.32F, 1.0F, 1.0F);
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
        consumer.vertex(matrix, firstX, length, firstZ).color(255, 12, 4, 0).endVertex();
        consumer.vertex(matrix, secondX, length, secondZ).color(130, 0, 8, 0).endVertex();
        redRayCenter(consumer, matrix, alpha);
    }

    private static void redRayCenter(VertexConsumer consumer, Matrix4f matrix, int alpha) {
        consumer.vertex(matrix, 0.0F, 0.0F, 0.0F)
                .color(255, 218, 190, alpha).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(DuatSunEaterEntity entity) {
        return DRAGON_TEXTURE;
    }
}
