package com.eldren.wutao.ramesses_curse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID, value = Dist.CLIENT)
public final class ApepBattleMusicClient {
    private static final float FADE_STEP = 1.0F / 40.0F;
    private static final double ISLAND_MUSIC_RADIUS = 112.0D;
    private static boolean playing;
    private static ClientLevel activeLevel;
    private static LoopingBattleSound sound;

    private ApepBattleMusicClient() {
    }

    public static void update(boolean active, boolean immediateStop) {
        playing = active;
        if (immediateStop && sound != null) {
            sound.stopNow();
            sound = null;
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.phase != TickEvent.Phase.END || minecraft.isPaused()) {
            return;
        }
        if (minecraft.level != activeLevel) {
            if (sound != null) {
                sound.stopNow();
                sound = null;
            }
            if (activeLevel != null || minecraft.level == null) {
                playing = false;
            }
            activeLevel = minecraft.level;
        }
        if (!playing || !isInsideIsland(minecraft)) {
            return;
        }
        minecraft.getMusicManager().stopPlaying();
        if (sound == null || sound.isFinished()) {
            sound = new LoopingBattleSound();
            minecraft.getSoundManager().play(sound);
        }
    }

    private static boolean isInsideIsland(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.player == null
                || !Level.END.equals(minecraft.level.dimension())) {
            return false;
        }
        double dx = minecraft.player.getX() - RamessesEndIslandStructure.CENTER_X;
        double dz = minecraft.player.getZ() - RamessesEndIslandStructure.CENTER_Z;
        return dx * dx + dz * dz <= ISLAND_MUSIC_RADIUS * ISLAND_MUSIC_RADIUS;
    }

    private static final class LoopingBattleSound extends AbstractTickableSoundInstance {
        private boolean finished;

        private LoopingBattleSound() {
            super(ModSounds.APEP_BATTLE.get(), SoundSource.MUSIC, RandomSource.create());
            this.looping = true;
            this.relative = true;
            this.attenuation = Attenuation.NONE;
            this.volume = FADE_STEP;
        }

        @Override
        public void tick() {
            if (playing && Minecraft.getInstance().level == activeLevel
                    && isInsideIsland(Minecraft.getInstance())) {
                this.volume = Math.min(1.0F, this.volume + FADE_STEP);
            } else {
                this.volume = Math.max(0.0F, this.volume - FADE_STEP);
                if (this.volume == 0.0F) {
                    stopNow();
                }
            }
        }

        private void stopNow() {
            this.finished = true;
            this.stop();
        }

        private boolean isFinished() {
            return this.finished;
        }
    }
}
