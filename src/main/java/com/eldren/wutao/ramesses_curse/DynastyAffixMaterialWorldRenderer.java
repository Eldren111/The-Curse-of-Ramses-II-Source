package com.eldren.wutao.ramesses_curse;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID, value = Dist.CLIENT)
public final class DynastyAffixMaterialWorldRenderer {
    private static final TagKey<Item> AFFIX_MATERIALS = TagKey.create(
            Registries.ITEM,
            new ResourceLocation(RamessesCurseMod.MODID, "dynasty_affix_materials"));
    private static final TagKey<Item> DEEP_PURPLE_EFFECT_ITEMS = TagKey.create(
            Registries.ITEM,
            new ResourceLocation(RamessesCurseMod.MODID, "deep_purple_effect_items"));
    private static final int RAY_COUNT = 9;
    private static final double MAX_RENDER_DISTANCE_SQR = 48.0D * 48.0D;
    private static final float HALF_SQRT_THREE = (float) (Math.sqrt(3.0D) / 2.0D);

    private DynastyAffixMaterialWorldRenderer() {
    }

    @SubscribeEvent
    public static void renderDroppedMaterials(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        Camera camera = event.getCamera();
        Vec3 cameraPosition = camera.getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(RenderType.lightning());
        float partialTick = event.getPartialTick();
        boolean rendered = false;

        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof ItemEntity itemEntity)
                    || (!itemEntity.getItem().is(AFFIX_MATERIALS)
                    && !itemEntity.getItem().is(DEEP_PURPLE_EFFECT_ITEMS))
                    || itemEntity.distanceToSqr(cameraPosition) > MAX_RENDER_DISTANCE_SQR
                    || !event.getFrustum().isVisible(itemEntity.getBoundingBox().inflate(1.5D))) {
                continue;
            }

            double x = Mth.lerp(partialTick, itemEntity.xo, itemEntity.getX());
            double y = Mth.lerp(partialTick, itemEntity.yo, itemEntity.getY());
            double z = Mth.lerp(partialTick, itemEntity.zo, itemEntity.getZ());
            float bob = Mth.sin((itemEntity.getAge() + partialTick) / 10.0F + itemEntity.bobOffs) * 0.1F + 0.35F;

            poseStack.pushPose();
            poseStack.translate(
                    x - cameraPosition.x,
                    y - cameraPosition.y + bob,
                    z - cameraPosition.z);
            renderRays(
                    poseStack,
                    consumer,
                    itemEntity,
                    partialTick,
                    itemEntity.getItem().is(DEEP_PURPLE_EFFECT_ITEMS));
            poseStack.popPose();
            rendered = true;
        }

        if (rendered) {
            buffers.endBatch(RenderType.lightning());
        }
    }

    private static void renderRays(PoseStack poseStack, VertexConsumer consumer,
                                   ItemEntity itemEntity, float partialTick, boolean deepPurple) {
        float age = itemEntity.getAge() + partialTick;
        float rotationY = (age % 300.0F) / 300.0F * Mth.TWO_PI;
        float rotationZ = (age % 420.0F) / 420.0F * Mth.TWO_PI;
        float pulse = 0.5F + 0.5F * Mth.sin(age * 0.18F);
        RandomSource random = RandomSource.create(0xA11F1L + itemEntity.getId());

        poseStack.mulPose(Axis.YP.rotation(rotationY));
        poseStack.mulPose(Axis.ZP.rotation(rotationZ));
        for (int ray = 0; ray < RAY_COUNT; ray++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0F));

            float length = 0.8F + random.nextFloat() * 0.45F + pulse * 0.12F;
            float width = 0.10F + random.nextFloat() * 0.10F;
            Matrix4f matrix = poseStack.last().pose();
            int alpha = 150 + Mth.floor(pulse * 80.0F);
            renderRay(consumer, matrix, length, width, alpha, deepPurple);
            poseStack.popPose();
        }
    }

    private static void renderRay(VertexConsumer consumer, Matrix4f matrix,
                                  float length, float width, int alpha, boolean deepPurple) {
        float x1 = -HALF_SQRT_THREE * width;
        float x2 = HALF_SQRT_THREE * width;
        float z1 = -0.5F * width;
        float z2 = width;

        face(consumer, matrix, x1, z1, x2, z1, length, alpha, deepPurple);
        face(consumer, matrix, x2, z1, 0.0F, z2, length, alpha, deepPurple);
        face(consumer, matrix, 0.0F, z2, x1, z1, length, alpha, deepPurple);
    }

    private static void face(VertexConsumer consumer, Matrix4f matrix,
                             float firstX, float firstZ, float secondX, float secondZ,
                             float length, int alpha, boolean deepPurple) {
        center(consumer, matrix, alpha, deepPurple);
        consumer.vertex(matrix, firstX, length, firstZ)
                .color(deepPurple ? 52 : 255, deepPurple ? 12 : 174, deepPurple ? 82 : 18, 0)
                .endVertex();
        consumer.vertex(matrix, secondX, length, secondZ)
                .color(deepPurple ? 133 : 255, deepPurple ? 58 : 218, deepPurple ? 196 : 72, 0)
                .endVertex();
        center(consumer, matrix, alpha, deepPurple);
    }

    private static void center(VertexConsumer consumer, Matrix4f matrix, int alpha, boolean deepPurple) {
        consumer.vertex(matrix, 0.0F, 0.0F, 0.0F)
                .color(deepPurple ? 188 : 255, deepPurple ? 112 : 248, deepPurple ? 255 : 196, alpha)
                .endVertex();
    }
}
