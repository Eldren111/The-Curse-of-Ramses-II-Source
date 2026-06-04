package com.eldren.wutao.ramesses_curse;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class HeartWardTriggeredPacket {

    public static void encode(HeartWardTriggeredPacket packet, net.minecraft.network.FriendlyByteBuf buffer) {
    }

    public static HeartWardTriggeredPacket decode(net.minecraft.network.FriendlyByteBuf buffer) {
        return new HeartWardTriggeredPacket();
    }

    public static void handle(HeartWardTriggeredPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null || minecraft.gameRenderer == null) {
                return;
            }

            Player player = minecraft.player;
            minecraft.gameRenderer.displayItemActivation(new ItemStack(ModItems.HEART_OF_DUAT.get()));
            player.playSound(SoundEvents.WITHER_DEATH, 1.0F, 0.85F);
            player.playSound(SoundEvents.BEACON_ACTIVATE, 1.2F, 0.7F);
            player.displayClientMessage(Component.literal("杜阿特之心已拒绝死亡")
                    .withStyle(net.minecraft.ChatFormatting.GOLD, net.minecraft.ChatFormatting.BOLD), true);

            RandomSource random = player.getRandom();
            for (int i = 0; i < 48; i++) {
                double offsetX = (random.nextDouble() - 0.5D) * 1.8D;
                double offsetY = random.nextDouble() * 1.6D + 0.2D;
                double offsetZ = (random.nextDouble() - 0.5D) * 1.8D;
                double velocityX = offsetX * 0.18D;
                double velocityY = 0.08D + random.nextDouble() * 0.12D;
                double velocityZ = offsetZ * 0.18D;
                player.level().addParticle(ParticleTypes.TOTEM_OF_UNDYING,
                        player.getX() + offsetX,
                        player.getY() + offsetY,
                        player.getZ() + offsetZ,
                        velocityX,
                        velocityY,
                        velocityZ);
            }

            for (int i = 0; i < 32; i++) {
                double offsetX = (random.nextDouble() - 0.5D) * 2.4D;
                double offsetY = random.nextDouble() * 1.8D + 0.1D;
                double offsetZ = (random.nextDouble() - 0.5D) * 2.4D;
                double velocityX = (random.nextDouble() - 0.5D) * 0.35D;
                double velocityY = random.nextDouble() * 0.2D;
                double velocityZ = (random.nextDouble() - 0.5D) * 0.35D;
                player.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        player.getX() + offsetX,
                        player.getY() + offsetY,
                        player.getZ() + offsetZ,
                        velocityX,
                        velocityY,
                        velocityZ);
            }

            for (int i = 0; i < 16; i++) {
                double offsetX = (random.nextDouble() - 0.5D) * 1.2D;
                double offsetY = random.nextDouble() * 1.4D + 0.3D;
                double offsetZ = (random.nextDouble() - 0.5D) * 1.2D;
                player.level().addParticle(ParticleTypes.FLASH,
                        player.getX() + offsetX,
                        player.getY() + offsetY,
                        player.getZ() + offsetZ,
                        0.0D,
                        0.0D,
                        0.0D);
            }
        });
        context.setPacketHandled(true);
    }
}
