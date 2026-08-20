package com.eldren.wutao.ramesses_curse.integration.l2;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;

/**
 * Optional integration boundary. A 1.21.1 L2 adapter can be restored here
 * without making L2Library a hard dependency of the base mod.
 */
public final class L2LibraryCompat {
    private L2LibraryCompat() {
    }

    public static void register(IEventBus modEventBus) {
    }

    public static void sync(ServerPlayer player, int base, int multiplierTenths, int effective) {
    }
}
