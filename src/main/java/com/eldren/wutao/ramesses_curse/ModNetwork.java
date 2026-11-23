package com.eldren.wutao.ramesses_curse;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class ModNetwork {

    private static final String PROTOCOL_VERSION = "5";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RamessesCurseMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static int packetId = 0;
    private static boolean registered = false;

    private static <T> BiConsumer<T, Supplier<NetworkEvent.Context>> guardInteraction(
            BiConsumer<T, Supplier<NetworkEvent.Context>> handler) {
        return (packet, supplier) -> {
            NetworkEvent.Context context = supplier.get();
            context.setPacketHandled(true);
            context.enqueueWork(() -> {
                if (context.getSender() != null && !CurseNineSeal.isSealed(context.getSender())) {
                    handler.accept(packet, supplier);
                }
            });
        };
    }

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
                guardInteraction(HeartAwakeningPacket::handle));
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
                guardInteraction(HeartWardActivatePacket::handle));
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
                guardInteraction(EdictOfTwoLandsActivatePacket::handle));
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
                guardInteraction(EdictOfTwoLandsDomainActivatePacket::handle));
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
                guardInteraction(EmberRiddleSubmitPacket::handle));
        CHANNEL.registerMessage(
                packetId++,
                EmberRiddleRefreshPacket.class,
                EmberRiddleRefreshPacket::encode,
                EmberRiddleRefreshPacket::decode,
                guardInteraction(EmberRiddleRefreshPacket::handle));
        CHANNEL.registerMessage(
                packetId++,
                EmberRiddleSelectPacket.class,
                EmberRiddleSelectPacket::encode,
                EmberRiddleSelectPacket::decode,
                guardInteraction(EmberRiddleSelectPacket::handle));
        CHANNEL.registerMessage(
                packetId++,
                SatietyRiddleSyncPacket.class,
                SatietyRiddleSyncPacket::encode,
                SatietyRiddleSyncPacket::decode,
                SatietyRiddleSyncPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                SatietyRiddleSubmitPacket.class,
                SatietyRiddleSubmitPacket::encode,
                SatietyRiddleSubmitPacket::decode,
                guardInteraction(SatietyRiddleSubmitPacket::handle));
        CHANNEL.registerMessage(
                packetId++,
                SatietyRiddleSelectPacket.class,
                SatietyRiddleSelectPacket::encode,
                SatietyRiddleSelectPacket::decode,
                guardInteraction(SatietyRiddleSelectPacket::handle));
        CHANNEL.registerMessage(
                packetId++,
                CurseSevenRevealPacket.class,
                CurseSevenRevealPacket::encode,
                CurseSevenRevealPacket::decode,
                guardInteraction(CurseSevenRevealPacket::handle));
        CHANNEL.registerMessage(
                packetId++,
                CurseSevenKeywordPacket.class,
                CurseSevenKeywordPacket::encode,
                CurseSevenKeywordPacket::decode,
                guardInteraction(CurseSevenKeywordPacket::handle));
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
                guardInteraction(CurseFiveChoiceResponsePacket::handle));
        CHANNEL.registerMessage(
                packetId++,
                CurseFiveMercyRevivePacket.class,
                CurseFiveMercyRevivePacket::encode,
                CurseFiveMercyRevivePacket::decode,
                guardInteraction(CurseFiveMercyRevivePacket::handle));
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
        CHANNEL.registerMessage(
                packetId++,
                DuatSandstormSyncPacket.class,
                DuatSandstormSyncPacket::encode,
                DuatSandstormSyncPacket::decode,
                DuatSandstormSyncPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                AffixKnowledgeSyncPacket.class,
                AffixKnowledgeSyncPacket::encode,
                AffixKnowledgeSyncPacket::decode,
                AffixKnowledgeSyncPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                ChanceDieRollPacket.class,
                ChanceDieRollPacket::encode,
                ChanceDieRollPacket::decode,
                ChanceDieRollPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                ChanceDieLargeRollPacket.class,
                ChanceDieLargeRollPacket::encode,
                ChanceDieLargeRollPacket::decode,
                ChanceDieLargeRollPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                ChanceDieLargeChoicePacket.class,
                ChanceDieLargeChoicePacket::encode,
                ChanceDieLargeChoicePacket::decode,
                guardInteraction(ChanceDieLargeChoicePacket::handle));
        CHANNEL.registerMessage(
                packetId++,
                ChanceDieLargeCancelPacket.class,
                ChanceDieLargeCancelPacket::encode,
                ChanceDieLargeCancelPacket::decode,
                ChanceDieLargeCancelPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                ChanceDieStateSyncPacket.class,
                ChanceDieStateSyncPacket::encode,
                ChanceDieStateSyncPacket::decode,
                ChanceDieStateSyncPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                RuinCounterSyncPacket.class,
                RuinCounterSyncPacket::encode,
                RuinCounterSyncPacket::decode,
                RuinCounterSyncPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                OpenCompoundBowAmmoPacket.class,
                OpenCompoundBowAmmoPacket::encode,
                OpenCompoundBowAmmoPacket::decode,
                guardInteraction(OpenCompoundBowAmmoPacket::handle));
        CHANNEL.registerMessage(
                packetId++,
                ToggleCompoundBowModePacket.class,
                ToggleCompoundBowModePacket::encode,
                ToggleCompoundBowModePacket::decode,
                guardInteraction(ToggleCompoundBowModePacket::handle));
        CHANNEL.registerMessage(
                packetId++,
                GoetyMonolithProtectionPacket.class,
                GoetyMonolithProtectionPacket::encode,
                GoetyMonolithProtectionPacket::decode,
                GoetyMonolithProtectionPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                CurseSevenTargetMarkPacket.class,
                CurseSevenTargetMarkPacket::encode,
                CurseSevenTargetMarkPacket::decode,
                CurseSevenTargetMarkPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                RamessesEndCrystalBreakVfxPacket.class,
                RamessesEndCrystalBreakVfxPacket::encode,
                RamessesEndCrystalBreakVfxPacket::decode,
                RamessesEndCrystalBreakVfxPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                DuatSunEaterDeathVfxPacket.class,
                DuatSunEaterDeathVfxPacket::encode,
                DuatSunEaterDeathVfxPacket::decode,
                DuatSunEaterDeathVfxPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                SunEaterStageCinematicPacket.class,
                SunEaterStageCinematicPacket::encode,
                SunEaterStageCinematicPacket::decode,
                SunEaterStageCinematicPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                DuatBlackHoleVfxPacket.class,
                DuatBlackHoleVfxPacket::encode,
                DuatBlackHoleVfxPacket::decode,
                DuatBlackHoleVfxPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                SunEaterStageRitualVfxPacket.class,
                SunEaterStageRitualVfxPacket::encode,
                SunEaterStageRitualVfxPacket::decode,
                SunEaterStageRitualVfxPacket::handle);
        CHANNEL.registerMessage(
                packetId++,
                DuatCuriosSealPacket.class,
                DuatCuriosSealPacket::encode,
                DuatCuriosSealPacket::decode,
                DuatCuriosSealPacket::handle);
        CHANNEL.registerMessage(packetId++, DynastyShieldSyncPacket.class, DynastyShieldSyncPacket::encode,
                DynastyShieldSyncPacket::decode, DynastyShieldSyncPacket::handle);
        CHANNEL.registerMessage(packetId++, ApepBattleMusicPacket.class, ApepBattleMusicPacket::encode,
                ApepBattleMusicPacket::decode, ApepBattleMusicPacket::handle);
        CHANNEL.registerMessage(packetId++, CurseNineSealPacket.class, CurseNineSealPacket::encode,
                CurseNineSealPacket::decode, CurseNineSealPacket::handle,
                java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT));
    }
}
