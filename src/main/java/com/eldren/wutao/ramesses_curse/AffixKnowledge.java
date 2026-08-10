package com.eldren.wutao.ramesses_curse;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

public final class AffixKnowledge {
    private static final String DATA_KEY = "RamessesDynastyAffixKnowledge";

    private AffixKnowledge() {
    }

    public static boolean isKnownAffix(String affixId) {
        return DynastyAffixData.KNOWN_AFFIXES.contains(affixId);
    }

    public static boolean hasLearned(Player player, String affixId) {
        return player != null && isKnownAffix(affixId) && getKnowledgeTag(player).getBoolean(affixId);
    }

    public static boolean learn(ServerPlayer player, String affixId) {
        if (!isKnownAffix(affixId) || hasLearned(player, affixId)) {
            return false;
        }
        getKnowledgeTag(player).putBoolean(affixId, true);
        ModEvents.onAffixKnowledgeLearned(player, affixId);
        sync(player);
        return true;
    }

    public static long createMask(Player player) {
        long mask = 0L;
        for (int index = 0; index < DynastyAffixData.KNOWN_AFFIXES.size(); index++) {
            if (hasLearned(player, DynastyAffixData.KNOWN_AFFIXES.get(index))) {
                mask |= 1L << index;
            }
        }
        return mask;
    }

    public static void sync(ServerPlayer player) {
        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new AffixKnowledgeSyncPacket(createMask(player)));
    }

    private static CompoundTag getKnowledgeTag(Player player) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (!player.getPersistentData().contains(Player.PERSISTED_NBT_TAG)) {
            player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        }
        CompoundTag knowledge = persisted.getCompound(DATA_KEY);
        if (!persisted.contains(DATA_KEY)) {
            persisted.put(DATA_KEY, knowledge);
        }
        return knowledge;
    }
}
