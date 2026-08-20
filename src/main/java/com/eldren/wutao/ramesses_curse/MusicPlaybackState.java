package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.BlockPos;

public record MusicPlaybackState(BlockPos pos, long startTick, int requiredTicks) {
    public String key() {
        return pos.getX() + ":" + pos.getY() + ":" + pos.getZ() + ":" + startTick;
    }
}
