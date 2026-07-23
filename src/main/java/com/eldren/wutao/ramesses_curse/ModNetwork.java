package com.eldren.wutao.ramesses_curse;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RamessesCurseMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static int packetId = 0;
    private static boolean registered = false;

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        CHANNEL.registerMessage(
                packetId++,
                GuiltSyncPacket.class,
                GuiltSyncPacket::encode,
                GuiltSyncPacket::decode,
                GuiltSyncPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                HeartAwakeningPacket.class,
                HeartAwakeningPacket::encode,
                HeartAwakeningPacket::decode,
                HeartAwakeningPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                HeartWardTriggeredPacket.class,
                HeartWardTriggeredPacket::encode,
                HeartWardTriggeredPacket::decode,
                HeartWardTriggeredPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                EternalSilenceTotemTriggeredPacket.class,
                EternalSilenceTotemTriggeredPacket::encode,
                EternalSilenceTotemTriggeredPacket::decode,
                EternalSilenceTotemTriggeredPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                EternalSilenceRebirthCooldownPacket.class,
                EternalSilenceRebirthCooldownPacket::encode,
                EternalSilenceRebirthCooldownPacket::decode,
                EternalSilenceRebirthCooldownPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                HeartWardActivatePacket.class,
                HeartWardActivatePacket::encode,
                HeartWardActivatePacket::decode,
                HeartWardActivatePacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                HeartStateSyncPacket.class,
                HeartStateSyncPacket::encode,
                HeartStateSyncPacket::decode,
                HeartStateSyncPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                MusicRelicSyncPacket.class,
                MusicRelicSyncPacket::encode,
                MusicRelicSyncPacket::decode,
                MusicRelicSyncPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                EdictOfTwoLandsActivatePacket.class,
                EdictOfTwoLandsActivatePacket::encode,
                EdictOfTwoLandsActivatePacket::decode,
                EdictOfTwoLandsActivatePacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                EdictOfTwoLandsTriggeredPacket.class,
                EdictOfTwoLandsTriggeredPacket::encode,
                EdictOfTwoLandsTriggeredPacket::decode,
                EdictOfTwoLandsTriggeredPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                EdictWorldEffectPacket.class,
                EdictWorldEffectPacket::encode,
                EdictWorldEffectPacket::decode,
                EdictWorldEffectPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                EdictOfTwoLandsDomainActivatePacket.class,
                EdictOfTwoLandsDomainActivatePacket::encode,
                EdictOfTwoLandsDomainActivatePacket::decode,
                EdictOfTwoLandsDomainActivatePacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                EdictCooldownSyncPacket.class,
                EdictCooldownSyncPacket::encode,
                EdictCooldownSyncPacket::decode,
                EdictCooldownSyncPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                UraeusStormCooldownSyncPacket.class,
                UraeusStormCooldownSyncPacket::encode,
                UraeusStormCooldownSyncPacket::decode,
                UraeusStormCooldownSyncPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                EmberRiddleSyncPacket.class,
                EmberRiddleSyncPacket::encode,
                EmberRiddleSyncPacket::decode,
                EmberRiddleSyncPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                EmberRiddleSubmitPacket.class,
                EmberRiddleSubmitPacket::encode,
                EmberRiddleSubmitPacket::decode,
                EmberRiddleSubmitPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                EmberRiddleRefreshPacket.class,
                EmberRiddleRefreshPacket::encode,
                EmberRiddleRefreshPacket::decode,
                EmberRiddleRefreshPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                EmberRiddleSelectPacket.class,
                EmberRiddleSelectPacket::encode,
                EmberRiddleSelectPacket::decode,
                EmberRiddleSelectPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                CurseSevenRevealPacket.class,
                CurseSevenRevealPacket::encode,
                CurseSevenRevealPacket::decode,
                CurseSevenRevealPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                CurseSevenKeywordPacket.class,
                CurseSevenKeywordPacket::encode,
                CurseSevenKeywordPacket::decode,
                CurseSevenKeywordPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                CurseSevenHintPacket.class,
                CurseSevenHintPacket::encode,
                CurseSevenHintPacket::decode,
                CurseSevenHintPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                CurseFiveChoicePromptPacket.class,
                CurseFiveChoicePromptPacket::encode,
                CurseFiveChoicePromptPacket::decode,
                CurseFiveChoicePromptPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                CurseFiveChoiceResponsePacket.class,
                CurseFiveChoiceResponsePacket::encode,
                CurseFiveChoiceResponsePacket::decode,
                CurseFiveChoiceResponsePacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                CurseFiveMercyRevivePacket.class,
                CurseFiveMercyRevivePacket::encode,
                CurseFiveMercyRevivePacket::decode,
                CurseFiveMercyRevivePacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                CurseFiveMercyReviveAckPacket.class,
                CurseFiveMercyReviveAckPacket::encode,
                CurseFiveMercyReviveAckPacket::decode,
                CurseFiveMercyReviveAckPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                DuatPalaceWeatherPacket.class,
                DuatPalaceWeatherPacket::encode,
                DuatPalaceWeatherPacket::decode,
                DuatPalaceWeatherPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                NebukaRitualShakePacket.class,
                NebukaRitualShakePacket::encode,
                NebukaRitualShakePacket::decode,
                NebukaRitualShakePacket::handle);
    }
}
