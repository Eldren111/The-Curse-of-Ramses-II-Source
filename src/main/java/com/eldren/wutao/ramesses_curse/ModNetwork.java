package com.eldren.wutao.ramesses_curse;

import io.netty.buffer.Unpooled;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final LegacyChannel CHANNEL = new LegacyChannel();
    private static int packetId;
    private static boolean registered;

    private ModNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        register();
        event.registrar(PROTOCOL_VERSION).playBidirectional(
                LegacyPayload.TYPE,
                LegacyPayload.STREAM_CODEC,
                ModNetwork::handlePayload);
    }

    private static void handlePayload(LegacyPayload payload, IPayloadContext context) {
        CHANNEL.handle(payload, context);
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        CHANNEL.registerMessage(packetId++, GuiltSyncPacket.class, GuiltSyncPacket::encode, GuiltSyncPacket::decode, GuiltSyncPacket::handle);
        CHANNEL.registerMessage(packetId++, HeartAwakeningPacket.class, HeartAwakeningPacket::encode, HeartAwakeningPacket::decode, HeartAwakeningPacket::handle);
        CHANNEL.registerMessage(packetId++, HeartWardTriggeredPacket.class, HeartWardTriggeredPacket::encode, HeartWardTriggeredPacket::decode, HeartWardTriggeredPacket::handle);
        CHANNEL.registerMessage(packetId++, EternalSilenceTotemTriggeredPacket.class, EternalSilenceTotemTriggeredPacket::encode, EternalSilenceTotemTriggeredPacket::decode, EternalSilenceTotemTriggeredPacket::handle);
        CHANNEL.registerMessage(packetId++, EternalSilenceRebirthCooldownPacket.class, EternalSilenceRebirthCooldownPacket::encode, EternalSilenceRebirthCooldownPacket::decode, EternalSilenceRebirthCooldownPacket::handle);
        CHANNEL.registerMessage(packetId++, HeartWardActivatePacket.class, HeartWardActivatePacket::encode, HeartWardActivatePacket::decode, HeartWardActivatePacket::handle);
        CHANNEL.registerMessage(packetId++, HeartStateSyncPacket.class, HeartStateSyncPacket::encode, HeartStateSyncPacket::decode, HeartStateSyncPacket::handle);
        CHANNEL.registerMessage(packetId++, MusicRelicSyncPacket.class, MusicRelicSyncPacket::encode, MusicRelicSyncPacket::decode, MusicRelicSyncPacket::handle);
        CHANNEL.registerMessage(packetId++, EdictOfTwoLandsActivatePacket.class, EdictOfTwoLandsActivatePacket::encode, EdictOfTwoLandsActivatePacket::decode, EdictOfTwoLandsActivatePacket::handle);
        CHANNEL.registerMessage(packetId++, EdictOfTwoLandsTriggeredPacket.class, EdictOfTwoLandsTriggeredPacket::encode, EdictOfTwoLandsTriggeredPacket::decode, EdictOfTwoLandsTriggeredPacket::handle);
        CHANNEL.registerMessage(packetId++, EdictWorldEffectPacket.class, EdictWorldEffectPacket::encode, EdictWorldEffectPacket::decode, EdictWorldEffectPacket::handle);
        CHANNEL.registerMessage(packetId++, EdictOfTwoLandsDomainActivatePacket.class, EdictOfTwoLandsDomainActivatePacket::encode, EdictOfTwoLandsDomainActivatePacket::decode, EdictOfTwoLandsDomainActivatePacket::handle);
        CHANNEL.registerMessage(packetId++, EdictCooldownSyncPacket.class, EdictCooldownSyncPacket::encode, EdictCooldownSyncPacket::decode, EdictCooldownSyncPacket::handle);
        CHANNEL.registerMessage(packetId++, UraeusStormCooldownSyncPacket.class, UraeusStormCooldownSyncPacket::encode, UraeusStormCooldownSyncPacket::decode, UraeusStormCooldownSyncPacket::handle);
        CHANNEL.registerMessage(packetId++, EmberRiddleSyncPacket.class, EmberRiddleSyncPacket::encode, EmberRiddleSyncPacket::decode, EmberRiddleSyncPacket::handle);
        CHANNEL.registerMessage(packetId++, EmberRiddleSubmitPacket.class, EmberRiddleSubmitPacket::encode, EmberRiddleSubmitPacket::decode, EmberRiddleSubmitPacket::handle);
        CHANNEL.registerMessage(packetId++, EmberRiddleRefreshPacket.class, EmberRiddleRefreshPacket::encode, EmberRiddleRefreshPacket::decode, EmberRiddleRefreshPacket::handle);
        CHANNEL.registerMessage(packetId++, EmberRiddleSelectPacket.class, EmberRiddleSelectPacket::encode, EmberRiddleSelectPacket::decode, EmberRiddleSelectPacket::handle);
        CHANNEL.registerMessage(packetId++, SatietyRiddleSyncPacket.class, SatietyRiddleSyncPacket::encode, SatietyRiddleSyncPacket::decode, SatietyRiddleSyncPacket::handle);
        CHANNEL.registerMessage(packetId++, SatietyRiddleSubmitPacket.class, SatietyRiddleSubmitPacket::encode, SatietyRiddleSubmitPacket::decode, SatietyRiddleSubmitPacket::handle);
        CHANNEL.registerMessage(packetId++, SatietyRiddleSelectPacket.class, SatietyRiddleSelectPacket::encode, SatietyRiddleSelectPacket::decode, SatietyRiddleSelectPacket::handle);
        CHANNEL.registerMessage(packetId++, CurseSevenRevealPacket.class, CurseSevenRevealPacket::encode, CurseSevenRevealPacket::decode, CurseSevenRevealPacket::handle);
        CHANNEL.registerMessage(packetId++, CurseSevenKeywordPacket.class, CurseSevenKeywordPacket::encode, CurseSevenKeywordPacket::decode, CurseSevenKeywordPacket::handle);
        CHANNEL.registerMessage(packetId++, CurseSevenHintPacket.class, CurseSevenHintPacket::encode, CurseSevenHintPacket::decode, CurseSevenHintPacket::handle);
        CHANNEL.registerMessage(packetId++, CurseFiveChoicePromptPacket.class, CurseFiveChoicePromptPacket::encode, CurseFiveChoicePromptPacket::decode, CurseFiveChoicePromptPacket::handle);
        CHANNEL.registerMessage(packetId++, CurseFiveChoiceResponsePacket.class, CurseFiveChoiceResponsePacket::encode, CurseFiveChoiceResponsePacket::decode, CurseFiveChoiceResponsePacket::handle);
        CHANNEL.registerMessage(packetId++, CurseFiveMercyRevivePacket.class, CurseFiveMercyRevivePacket::encode, CurseFiveMercyRevivePacket::decode, CurseFiveMercyRevivePacket::handle);
        CHANNEL.registerMessage(packetId++, CurseFiveMercyReviveAckPacket.class, CurseFiveMercyReviveAckPacket::encode, CurseFiveMercyReviveAckPacket::decode, CurseFiveMercyReviveAckPacket::handle);
        CHANNEL.registerMessage(packetId++, DuatPalaceWeatherPacket.class, DuatPalaceWeatherPacket::encode, DuatPalaceWeatherPacket::decode, DuatPalaceWeatherPacket::handle);
        CHANNEL.registerMessage(packetId++, NebukaRitualShakePacket.class, NebukaRitualShakePacket::encode, NebukaRitualShakePacket::decode, NebukaRitualShakePacket::handle);
        CHANNEL.registerMessage(packetId++, DuatSandstormSyncPacket.class, DuatSandstormSyncPacket::encode, DuatSandstormSyncPacket::decode, DuatSandstormSyncPacket::handle);
        CHANNEL.registerMessage(packetId++, AffixKnowledgeSyncPacket.class, AffixKnowledgeSyncPacket::encode, AffixKnowledgeSyncPacket::decode, AffixKnowledgeSyncPacket::handle);
        CHANNEL.registerMessage(packetId++, ChanceDieRollPacket.class, ChanceDieRollPacket::encode, ChanceDieRollPacket::decode, ChanceDieRollPacket::handle);
        CHANNEL.registerMessage(packetId++, ChanceDieLargeRollPacket.class, ChanceDieLargeRollPacket::encode, ChanceDieLargeRollPacket::decode, ChanceDieLargeRollPacket::handle);
        CHANNEL.registerMessage(packetId++, ChanceDieLargeChoicePacket.class, ChanceDieLargeChoicePacket::encode, ChanceDieLargeChoicePacket::decode, ChanceDieLargeChoicePacket::handle);
        CHANNEL.registerMessage(packetId++, ChanceDieLargeCancelPacket.class, ChanceDieLargeCancelPacket::encode, ChanceDieLargeCancelPacket::decode, ChanceDieLargeCancelPacket::handle);
        CHANNEL.registerMessage(packetId++, ChanceDieStateSyncPacket.class, ChanceDieStateSyncPacket::encode, ChanceDieStateSyncPacket::decode, ChanceDieStateSyncPacket::handle);
        CHANNEL.registerMessage(packetId++, RuinCounterSyncPacket.class, RuinCounterSyncPacket::encode, RuinCounterSyncPacket::decode, RuinCounterSyncPacket::handle);
        CHANNEL.registerMessage(packetId++, OpenCompoundBowAmmoPacket.class, OpenCompoundBowAmmoPacket::encode, OpenCompoundBowAmmoPacket::decode, OpenCompoundBowAmmoPacket::handle);
        CHANNEL.registerMessage(packetId++, ToggleCompoundBowModePacket.class, ToggleCompoundBowModePacket::encode, ToggleCompoundBowModePacket::decode, ToggleCompoundBowModePacket::handle);
    }

    public static final class LegacyChannel {
        private final Map<Integer, Registration<?>> byId = new HashMap<>();
        private final Map<Class<?>, Registration<?>> byType = new HashMap<>();

        public <T> void registerMessage(int id, Class<T> type, BiConsumer<T, FriendlyByteBuf> encoder,
                Function<FriendlyByteBuf, T> decoder,
                BiConsumer<T, Supplier<NetworkEvent.Context>> handler) {
            Registration<T> registration = new Registration<>(id, type, encoder, decoder, handler);
            byId.put(id, registration);
            byType.put(type, registration);
        }

        public void send(PacketDistributor.PlayerTarget target, Object message) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(target.player(), encode(message));
        }

        public void send(PacketDistributor.TrackingEntityTarget target, Object message) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                    target.entity(), encode(message));
        }

        public void sendToServer(Object message) {
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(encode(message));
        }

        private LegacyPayload encode(Object message) {
            Registration<?> registration = byType.get(message.getClass());
            if (registration == null) {
                throw new IllegalArgumentException("Unregistered packet type: " + message.getClass().getName());
            }
            return registration.encodeUnchecked(message);
        }

        private void handle(LegacyPayload payload, IPayloadContext context) {
            Registration<?> registration = byId.get(payload.packetId());
            if (registration == null) {
                throw new IllegalArgumentException("Unknown packet id: " + payload.packetId());
            }
            registration.handle(payload.data(), context);
        }
    }

    private record Registration<T>(int id, Class<T> type, BiConsumer<T, FriendlyByteBuf> encoder,
            Function<FriendlyByteBuf, T> decoder,
            BiConsumer<T, Supplier<NetworkEvent.Context>> handler) {
        private LegacyPayload encodeUnchecked(Object message) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                encoder.accept(type.cast(message), buffer);
                byte[] data = new byte[buffer.readableBytes()];
                buffer.getBytes(buffer.readerIndex(), data);
                return new LegacyPayload(id, data);
            } finally {
                buffer.release();
            }
        }

        private void handle(byte[] data, IPayloadContext payloadContext) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
            try {
                T message = decoder.apply(buffer);
                NetworkEvent.Context context = new NetworkEvent.Context(payloadContext);
                handler.accept(message, () -> context);
            } finally {
                buffer.release();
            }
        }
    }

    public record LegacyPayload(int packetId, byte[] data) implements CustomPacketPayload {
        public static final Type<LegacyPayload> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "main"));
        public static final StreamCodec<RegistryFriendlyByteBuf, LegacyPayload> STREAM_CODEC = StreamCodec.of(
                (buffer, payload) -> {
                    buffer.writeVarInt(payload.packetId);
                    buffer.writeByteArray(payload.data);
                },
                buffer -> new LegacyPayload(buffer.readVarInt(), buffer.readByteArray()));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
