package com.eldren.wutao.ramesses_curse;

import com.mojang.blaze3d.shaders.FogShape;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID, value = Dist.CLIENT)
public final class VoidDimensionFogRenderer {
    private VoidDimensionFogRenderer() {
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        if (!isInVoidDimension()) {
            return;
        }
        event.setRed(0.075F);
        event.setGreen(0.018F);
        event.setBlue(0.105F);
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (!isInVoidDimension()) {
            return;
        }
        event.setNearPlaneDistance(4.0F);
        event.setFarPlaneDistance(54.0F);
        event.setFogShape(FogShape.SPHERE);
        event.setCanceled(true);
    }

    private static boolean isInVoidDimension() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level != null
                && ModDimensions.RAMSES_VOID.equals(minecraft.level.dimension());
    }
}
