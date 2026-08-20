package com.eldren.wutao.ramesses_curse;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.IItemDecorator;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public final class DynastyAffixMaterialDecorator implements IItemDecorator {
    public static final DynastyAffixMaterialDecorator INSTANCE =
            new DynastyAffixMaterialDecorator(255, 178, 24, 255, 245, 255);
    public static final DynastyAffixMaterialDecorator DEEP_PURPLE =
            new DynastyAffixMaterialDecorator(58, 17, 92, 156, 86, 214);

    private static final int RAY_COUNT = 9;
    private static final long ROTATION_PERIOD_MILLIS = 15000L;

    private final int outerRed;
    private final int outerGreen;
    private final int outerBlue;
    private final int centerRed;
    private final int centerGreen;
    private final int centerBlue;

    private DynastyAffixMaterialDecorator(
            int outerRed,
            int outerGreen,
            int outerBlue,
            int centerRed,
            int centerGreen,
            int centerBlue) {
        this.outerRed = outerRed;
        this.outerGreen = outerGreen;
        this.outerBlue = outerBlue;
        this.centerRed = centerRed;
        this.centerGreen = centerGreen;
        this.centerBlue = centerBlue;
    }

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        long now = Util.getMillis();
        float rotation = (now % ROTATION_PERIOD_MILLIS) / (float) ROTATION_PERIOD_MILLIS * Mth.TWO_PI;
        float pulse = 0.5F + 0.5F * Mth.sin((now % 2500L) / 2500.0F * Mth.TWO_PI);
        long seed = 0x5EEDL + stack.getItem().hashCode();
        RandomSource random = RandomSource.create(seed);
        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();
        poseStack.translate(xOffset + 8.0F, yOffset + 8.0F, 140.0F);

        VertexConsumer consumer = guiGraphics.bufferSource().getBuffer(RenderType.lightning());
        for (int ray = 0; ray < RAY_COUNT; ray++) {
            float angle = rotation
                    + Mth.TWO_PI * ray / RAY_COUNT
                    + (random.nextFloat() - 0.5F) * 0.35F;
            float length = 12.0F + random.nextFloat() * 5.0F + pulse * 1.0F;
            float width = 2.2F + random.nextFloat() * 1.8F;
            Matrix4f matrix = poseStack.last().pose();
            int centerAlpha = 120 + Mth.floor(pulse * 70.0F);
            ray(consumer, matrix, angle, length, width, centerAlpha);
        }

        poseStack.popPose();
        guiGraphics.flush();
        return false;
    }

    private void ray(VertexConsumer consumer, Matrix4f matrix, float angle,
                     float length, float width, int alpha) {
        float directionX = Mth.cos(angle);
        float directionY = Mth.sin(angle);
        float sideX = -directionY;
        float sideY = directionX;
        float innerRadius = 1.0F;
        float innerWidth = 0.35F;
        float innerX = directionX * innerRadius;
        float innerY = directionY * innerRadius;
        float outerX = directionX * length;
        float outerY = directionY * length;

        consumer.addVertex(matrix, innerX + sideX * innerWidth, innerY + sideY * innerWidth, 0.0F)
                .setColor(centerRed, centerGreen, centerBlue, alpha)
                ;
        consumer.addVertex(matrix, outerX + sideX * width, outerY + sideY * width, 0.0F)
                .setColor(outerRed, outerGreen, outerBlue, 0)
                ;
        consumer.addVertex(matrix, outerX - sideX * width, outerY - sideY * width, 0.0F)
                .setColor(outerRed, outerGreen, outerBlue, 0)
                ;
        consumer.addVertex(matrix, innerX - sideX * innerWidth, innerY - sideY * innerWidth, 0.0F)
                .setColor(centerRed, centerGreen, centerBlue, alpha)
                ;
    }
}

