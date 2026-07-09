package com.eldren.wutao.ramesses_curse.integration.l2;

import com.eldren.wutao.ramesses_curse.RamessesCurseMod;
import dev.xkmc.l2library.capability.player.PlayerCapabilityHolder;
import dev.xkmc.l2library.capability.player.PlayerCapabilityNetworkHandler;
import dev.xkmc.l2library.capability.player.PlayerCapabilitySerializer;
import dev.xkmc.l2library.capability.player.PlayerCapabilityTemplate;
import dev.xkmc.l2serial.serialization.SerialClass;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.eventbus.api.IEventBus;

public final class L2LibraryCompat {
    private static final Capability<DynastyThreatCapability> DYNASTY_THREAT_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {
            });

    private static final PlayerCapabilityHolder<DynastyThreatCapability> DYNASTY_THREAT =
            new PlayerCapabilityHolder<>(
                    new ResourceLocation(RamessesCurseMod.MODID, "dynasty_threat"),
                    DYNASTY_THREAT_CAPABILITY,
                    DynastyThreatCapability.class,
                    DynastyThreatCapability::new,
                    PlayerCapabilityNetworkHandler::new) {
                @Override
                public PlayerCapabilitySerializer<DynastyThreatCapability> generateSerializer(Player player) {
                    return new DynastyThreatSerializer(player, this);
                }
            };

    private L2LibraryCompat() {
    }

    public static void register(IEventBus modEventBus) {
    }

    public static void sync(ServerPlayer player, int base, int multiplierTenths, int effective) {
        if (!DYNASTY_THREAT.isProper(player)) {
            return;
        }
        DynastyThreatCapability cap = DYNASTY_THREAT.get(player);
        cap.baseThreat = base;
        cap.multiplierTenths = multiplierTenths;
        cap.effectiveThreat = effective;
        DYNASTY_THREAT.network.toClientSyncAll(player);
        DYNASTY_THREAT.network.toTracking(player);
    }

    @SerialClass
    public static class DynastyThreatCapability extends PlayerCapabilityTemplate<DynastyThreatCapability> {
        @SerialClass.SerialField(toClient = true, toTracking = true)
        public int baseThreat;

        @SerialClass.SerialField(toClient = true, toTracking = true)
        public int multiplierTenths = 10;

        @SerialClass.SerialField(toClient = true, toTracking = true)
        public int effectiveThreat;
    }

    private static class DynastyThreatSerializer extends PlayerCapabilitySerializer<DynastyThreatCapability> {
        public DynastyThreatSerializer(Player player, PlayerCapabilityHolder<DynastyThreatCapability> holder) {
            super(player, holder);
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            DynastyThreatCapability cap = lo.resolve().orElse(handler);
            tag.putInt("baseThreat", cap.baseThreat);
            tag.putInt("multiplierTenths", cap.multiplierTenths);
            tag.putInt("effectiveThreat", cap.effectiveThreat);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            DynastyThreatCapability cap = lo.resolve().orElse(handler);
            cap.baseThreat = tag.getInt("baseThreat");
            cap.multiplierTenths = tag.contains("multiplierTenths") ? tag.getInt("multiplierTenths") : 10;
            cap.effectiveThreat = tag.getInt("effectiveThreat");
        }
    }
}
