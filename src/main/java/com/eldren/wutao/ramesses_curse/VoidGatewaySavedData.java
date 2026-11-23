package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;

public final class VoidGatewaySavedData extends SavedData {
    public static final int MAX_ENTRIES = 3;
    private static final String DATA_NAME = RamessesCurseMod.MODID + "_void_gateway";
    private static final String REMAINING_ENTRIES = "RemainingEntries";
    private int remainingEntries = MAX_ENTRIES;

    public VoidGatewaySavedData() {
    }

    private static VoidGatewaySavedData load(CompoundTag tag) {
        VoidGatewaySavedData data = new VoidGatewaySavedData();
        data.remainingEntries = tag.contains(REMAINING_ENTRIES)
                ? Math.max(0, tag.getInt(REMAINING_ENTRIES))
                : MAX_ENTRIES;
        return data;
    }

    public static void activate(ServerLevel endLevel) {
        VoidGatewaySavedData data = get(endLevel);
        data.remainingEntries = MAX_ENTRIES;
        data.setDirty();
    }

    /** Returns the remaining entries, or -1 when this gateway is already exhausted. */
    public static int consumeEntry(ServerLevel endLevel) {
        VoidGatewaySavedData data = get(endLevel);
        if (data.remainingEntries <= 0) {
            restoreVanillaPortal(endLevel);
            return -1;
        }
        data.remainingEntries--;
        data.setDirty();
        int remaining = data.remainingEntries;
        if (remaining == 0) {
            restoreVanillaPortal(endLevel);
        }
        return remaining;
    }

    private static VoidGatewaySavedData get(ServerLevel level) {
        ServerLevel endLevel = Level.END.equals(level.dimension())
                ? level
                : level.getServer().getLevel(Level.END);
        if (endLevel == null) {
            throw new IllegalStateException("The End dimension is unavailable");
        }
        return endLevel.getDataStorage().computeIfAbsent(
                VoidGatewaySavedData::load, VoidGatewaySavedData::new, DATA_NAME);
    }

    private static void restoreVanillaPortal(ServerLevel level) {
        boolean restored = false;
        for (int y = 40; y <= 100; y++) {
            for (int x = -10; x <= 10; x++) {
                for (int z = -10; z <= 10; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (level.getBlockState(pos).is(ModBlocks.VOID_GATEWAY_PORTAL.get())) {
                        level.setBlock(pos, Blocks.END_PORTAL.defaultBlockState(), 18);
                        restored = true;
                    }
                }
            }
        }
        if (!restored) {
            return;
        }
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, 0.5D, 65.0D, 0.5D,
                120, 3.8D, 0.8D, 3.8D, 0.16D);
        level.playSound(null, BlockPos.ZERO, SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(),
                SoundSource.BLOCKS, 1.5F, 0.55F);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(0.0D, player.getY(), 0.0D) <= 64.0D * 64.0D) {
                player.displayClientMessage(Component.translatable(
                        "message.ramesses_curse.void_gateway.exhausted"), false);
            }
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt(REMAINING_ENTRIES, this.remainingEntries);
        return tag;
    }
}
