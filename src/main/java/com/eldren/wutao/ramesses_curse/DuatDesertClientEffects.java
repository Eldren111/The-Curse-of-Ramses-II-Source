package com.eldren.wutao.ramesses_curse;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID, value = Dist.CLIENT)
public final class DuatDesertClientEffects {
    private static final BlockParticleOption WIND_BLOWN_SAND =
            new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState());
    private static final DustParticleOptions AIRBORNE_SAND =
            new DustParticleOptions(new Vector3f(0.78F, 0.58F, 0.28F), 0.8F);

    private DuatDesertClientEffects() {
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!isInDuatDesert(minecraft)) {
            return;
        }

        float fogTime = (float) (minecraft.level.getGameTime() + event.getPartialTick());
        float pulse = 0.025F * Mth.sin(fogTime * 0.025F);
        event.setRed(Mth.clamp(0.72F + pulse, 0.0F, 1.0F));
        event.setGreen(Mth.clamp(0.54F + pulse * 0.7F, 0.0F, 1.0F));
        event.setBlue(Mth.clamp(0.29F + pulse * 0.35F, 0.0F, 1.0F));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (!isInDuatDesert(minecraft) || minecraft.isPaused()) {
            return;
        }

        RandomSource random = minecraft.level.random;
        double windAngle = minecraft.level.getGameTime() * 0.0025D;
        double windX = Math.cos(windAngle) * 0.42D;
        double windZ = Math.sin(windAngle) * 0.42D;

        for (int i = 0; i < 8; i++) {
            double x = minecraft.player.getX() + (random.nextDouble() - 0.5D) * 12.0D - windX * 5.0D;
            double y = minecraft.player.getY() + random.nextDouble() * 5.0D - 1.0D;
            double z = minecraft.player.getZ() + (random.nextDouble() - 0.5D) * 12.0D - windZ * 5.0D;
            double gust = 2.4D + random.nextDouble() * 1.8D;
            minecraft.level.addParticle(
                    AIRBORNE_SAND,
                    x,
                    y,
                    z,
                    windX * gust,
                    (random.nextDouble() - 0.5D) * 0.035D,
                    windZ * gust);
        }

        for (int i = 0; i < 3; i++) {
            minecraft.level.addParticle(
                    WIND_BLOWN_SAND,
                    minecraft.player.getX() + (random.nextDouble() - 0.5D) * 10.0D,
                    minecraft.player.getY() + random.nextDouble() * 4.0D,
                    minecraft.player.getZ() + (random.nextDouble() - 0.5D) * 10.0D,
                    0.0D,
                    0.0D,
                    0.0D);
        }
    }

    private static boolean isInDuatDesert(Minecraft minecraft) {
        return minecraft.level != null
                && minecraft.player != null
                && minecraft.level.dimension() == ModDimensions.DUAT_DESERT;
    }
}
