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

public class UraeusRainShardRenderer extends EntityRenderer<UraeusRainShardEntity> {
    public UraeusRainShardRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(UraeusRainShardEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        Vec3 origin = entity.getPosition(partialTick);
        Vec3 cameraPosition = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        VertexConsumer consumer = bufferSource.getBuffer(UraeusRenderTypes.spiritGlow());
        float age = entity.tickCount + partialTick;

        for (int shardIndex = 0; shardIndex < entity.getShardCount(); shardIndex++) {
            if (!entity.isShardActive(shardIndex)) {
                continue;
            }

            renderTrail(entity, shardIndex, partialTick, origin, cameraPosition, poseStack, consumer);
            Vec3 worldPosition = entity.getShardPosition(shardIndex, partialTick);
            Vec3 localPosition = worldPosition.subtract(origin);
            Vec3 previousPosition = entity.getShardTrailLength(shardIndex) > 1
                    ? entity.getShardTrailPosition(shardIndex, 1, partialTick)
                    : worldPosition.add(0.0D, 0.1D, 0.0D);
            UraeusSpiritTrailRenderer.renderDroplet(
                    poseStack.last(),
                    consumer,
                    localPosition,
                    worldPosition,
                    worldPosition.subtract(previousPosition),
                    cameraPosition,
                    age + shardIndex * 0.37F,
                    entity.isThunder(),
                    entity.isThunder() ? 0.44F : 0.34F,
                    false);
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderTrail(UraeusRainShardEntity entity, int shardIndex, float partialTick,
                                    Vec3 origin, Vec3 cameraPosition, PoseStack poseStack,
                                    VertexConsumer consumer) {
        int length = entity.getShardTrailLength(shardIndex);
        if (length < 3) {
            return;
        }

        List<Vec3> controls = new ArrayList<>(length);
        for (int index = 0; index < length; index++) {
            controls.add(entity.getShardTrailPosition(shardIndex, index, partialTick));
        }
        List<Vec3> points = SmoothTrailGeometry.catmullRom(controls, 2);
        float age = entity.tickCount + partialTick + shardIndex * 0.37F;
        UraeusSpiritTrailRenderer.render(poseStack.last(), consumer, points, origin, cameraPosition,
                age, entity.isThunder(), entity.isThunder() ? 0.62F : 0.48F,
                entity.getId() * 31 + shardIndex);
    }

    @Override
    public ResourceLocation getTextureLocation(UraeusRainShardEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
