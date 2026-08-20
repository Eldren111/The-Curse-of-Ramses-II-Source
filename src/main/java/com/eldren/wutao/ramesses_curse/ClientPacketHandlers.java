package com.eldren.wutao.ramesses_curse;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientPacketHandlers {
    private ClientPacketHandlers() {
    }

    public static void acknowledgeMercyRevive() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.respawn();
        }
    }

    public static void showEdictTriggered() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(
                    Component.translatable("message.ramesses_curse.edict_cast").withStyle(ChatFormatting.GOLD), true);
        }
    }

    public static void showHeartWardTriggered() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.gameRenderer == null) {
            return;
        }

        Player player = minecraft.player;
        minecraft.gameRenderer.displayItemActivation(new ItemStack(ModItems.HEART_OF_DUAT.get()));
        player.playSound(SoundEvents.WITHER_DEATH, 1.0F, 0.85F);
        player.playSound(SoundEvents.BEACON_ACTIVATE, 1.2F, 0.7F);
        player.displayClientMessage(Component.translatable("message.ramesses_curse.heart_ward_triggered")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), true);

        RandomSource random = player.getRandom();
        addBurst(player, random, ParticleTypes.TOTEM_OF_UNDYING, 48, 1.8D, 1.6D);
        addBurst(player, random, ParticleTypes.ELECTRIC_SPARK, 32, 2.4D, 1.8D);
        addBurst(player, random, ParticleTypes.FLASH, 16, 1.2D, 1.4D);
    }

    public static void showEternalSilenceTotemTriggered(boolean rebirthTotem) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameRenderer != null) {
            minecraft.gameRenderer.displayItemActivation(
                    new ItemStack(rebirthTotem
                            ? ModItems.ETERNAL_SILENCE_REBIRTH_TOTEM.get()
                            : ModItems.ETERNAL_SILENCE_TOTEM.get()));
        }
    }

    public static void showLargeChanceDieScreen(int[] outcomes, int unlockTicks) {
        Minecraft.getInstance().setScreen(new ChanceDieLargeScreen(outcomes, unlockTicks));
    }

    public static void closeEmberRiddleScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof EmberRiddleInputScreen) {
            minecraft.setScreen(null);
        }
    }

    private static void addBurst(Player player, RandomSource random, ParticleOptions particle,
                                 int count, double horizontalSpread, double verticalSpread) {
        for (int i = 0; i < count; i++) {
            double offsetX = (random.nextDouble() - 0.5D) * horizontalSpread;
            double offsetY = random.nextDouble() * verticalSpread + 0.2D;
            double offsetZ = (random.nextDouble() - 0.5D) * horizontalSpread;
            player.level().addParticle(particle,
                    player.getX() + offsetX, player.getY() + offsetY, player.getZ() + offsetZ,
                    offsetX * 0.18D, 0.08D + random.nextDouble() * 0.12D, offsetZ * 0.18D);
        }
    }
}

