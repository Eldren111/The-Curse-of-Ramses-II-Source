package com.eldren.wutao.ramesses_requiem;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import java.util.UUID;

/** The inventory stack owns the weapon; the flying entity only holds a damage/render snapshot. */
public final class CrimsonRequiemFlight {
    private static final String FLIGHT = "CrimsonRequiemFlight";
    private static final String UNTIL = "CrimsonRequiemFlightUntil";
    private CrimsonRequiemFlight() {}

    public static boolean locked(ItemStack stack) {
        return stack.is(RequiemItems.CRIMSON_REQUIEM.get()) && stack.hasTag() && stack.getTag().hasUUID(FLIGHT);
    }

    private static long time(Level level) {
        return level.getServer().overworld().getGameTime();
    }

    public static void begin(ItemStack stack, UUID projectile, Level level) {
        stack.getOrCreateTag().putUUID(FLIGHT, projectile);
        stack.getOrCreateTag().putLong(UNTIL, time(level) + 260);
    }

    public static void clear(ItemStack stack) {
        if (stack.hasTag()) {
            stack.getTag().remove(FLIGHT);
            stack.getTag().remove(UNTIL);
        }
    }

    public static void expire(ItemStack stack, Level level) {
        // Also recovers weapons whose projectile was unloaded, or whose owner logged out/died.
        if (!level.isClientSide && locked(stack) && time(level) >= stack.getTag().getLong(UNTIL)) clear(stack);
    }

    public static boolean active(Player owner, UUID projectile) {
        for (int i = 0; i < owner.getInventory().getContainerSize(); i++) {
            ItemStack stack = owner.getInventory().getItem(i);
            if (locked(stack) && projectile.equals(stack.getTag().getUUID(FLIGHT))
                    && time(owner.level()) < stack.getTag().getLong(UNTIL)) return true;
        }
        return false;
    }

    public static boolean release(Player owner, UUID projectile) {
        boolean released = false;
        for (int i = 0; i < owner.getInventory().getContainerSize(); i++) {
            ItemStack stack = owner.getInventory().getItem(i);
            if (locked(stack) && projectile.equals(stack.getTag().getUUID(FLIGHT))) {
                clear(stack);
                released = true;
            }
        }
        owner.getInventory().setChanged();
        owner.containerMenu.broadcastChanges();
        return released;
    }
}
