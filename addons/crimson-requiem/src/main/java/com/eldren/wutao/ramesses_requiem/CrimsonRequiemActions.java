package com.eldren.wutao.ramesses_requiem;

import com.eldren.wutao.ramesses_curse.CurseNineSeal;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class CrimsonRequiemActions {
    public static final String OWNER = "CrimsonRequiemCaster";
    private static final String BURST = "CrimsonRequiemBurstUntil";
    private static final String WARD = "CrimsonRequiemWardUntil";
    private static final String THROW = "CrimsonRequiemThrowUntil";
    private static final String LAST_WAVE_TICK = "CrimsonRequiemLastWaveTick";
    private CrimsonRequiemActions() {}

    public static net.minecraft.world.InteractionHand weaponHand(Player player) {
        return player.getMainHandItem().is(RequiemItems.CRIMSON_REQUIEM.get())
                ? net.minecraft.world.InteractionHand.MAIN_HAND
                : net.minecraft.world.InteractionHand.OFF_HAND;
    }

    public static ItemStack weapon(Player player) {
        return player.getItemInHand(weaponHand(player));
    }

    public static boolean canUse(Player player) {
        return player instanceof ServerPlayer && player.isAlive() && !player.isSpectator()
                && !CurseNineSeal.isSealed(player) && weapon(player).is(RequiemItems.CRIMSON_REQUIEM.get())
                && !CrimsonRequiemFlight.locked(weapon(player));
    }

    private static CompoundTag cooldowns(Player player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(Player.PERSISTED_NBT_TAG)) data.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        return data.getCompound(Player.PERSISTED_NBT_TAG);
    }

    private static long time(Player player) { return player.getServer().overworld().getGameTime(); }

    private static boolean ready(Player player, String key, boolean notify) {
        long remaining = cooldowns(player).getLong(key) - time(player);
        if (remaining <= 0) return true;
        if (notify) player.displayClientMessage(Component.translatable("message.ramesses_curse.crimson_requiem.cooldown", (remaining + 19) / 20), true);
        return false;
    }

    public static void rightClick(Player player) {
        if (!canUse(player)) return;
        if (player.isShiftKeyDown()) {
            if (!ready(player, WARD, true)) return;
            if (CrimsonRiftExecution.start((ServerPlayer)player)) {
                cooldowns(player).putLong(WARD, time(player) + CrimsonRequiemConfig.WARD_COOLDOWN.get() * 20L);
            }
        } else {
            if (!ready(player, THROW, false)) return;
            CrimsonRequiemProjectile projectile = CrimsonRequiemProjectile.create((ServerPlayer) player, false);
            if (player.level().addFreshEntity(projectile)) {
                CrimsonSwordStorm.start((ServerPlayer)player);
                CrimsonRequiemFlight.begin(weapon(player), projectile.getUUID(), player.level());
                player.getInventory().setChanged();
                player.containerMenu.broadcastChanges();
                cooldowns(player).putLong(THROW, time(player) + 260);
            }
        }
    }

    public static void returned(Player player) { cooldowns(player).remove(THROW); }

    /** Temporary playtest hook; remove together with CrimsonRequiemDebugCommands. */
    public static void resetExecutionCooldown(ServerPlayer player) { cooldowns(player).remove(WARD); }

    public static void leftClick(Player player) {
        if (!canUse(player)) return;
        if (player.isShiftKeyDown()) { burst(player); return; }
        // Coalesce multi-target callbacks for one swing; no attack-speed cooldown.
        CompoundTag runtime = player.getPersistentData();
        if (runtime.contains(LAST_WAVE_TICK) && runtime.getLong(LAST_WAVE_TICK) == time(player)) return;
        ItemStack stack = weapon(player);
        CrimsonRequiemItem.ensureEnchantments(stack);
        CrimsonRequiemProjectile wave = CrimsonRequiemProjectile.create((ServerPlayer) player, true);
        if (player.level().addFreshEntity(wave)) {
            runtime.putLong(LAST_WAVE_TICK, time(player));
        }
    }

    /** Called after the main-hand strike, before its attack-strength timer resets. */
    public static void offhandMelee(Player player, net.minecraft.world.entity.Entity target) {
        if (!canUse(player) || weaponHand(player) != net.minecraft.world.InteractionHand.OFF_HAND
                || player.isShiftKeyDown() || !target.isAlive() || target == player
                || target instanceof Player other && !player.canHarmPlayer(other)) return;
        CrimsonRequiemItem.ensureEnchantments(weapon(player));
        CrimsonRequiemProjectile snapshot = CrimsonRequiemProjectile.create((ServerPlayer)player, true);
        snapshot.hitWave((ServerPlayer)player, target);
        snapshot.discard();
    }

    public static void burst(Player player) {
        if (!canUse(player) || !player.isShiftKeyDown() || !ready(player, BURST, true)) return;
        Vec3 look = player.getLookAngle();
        Vec3 side = new Vec3(-look.z, 0, look.x).normalize().scale(0.45);
        CrimsonRequiemSkull skull = new CrimsonRequiemSkull(RequiemEntities.CRIMSON_REQUIEM_SKULL.get(), player.level());
        skull.setDangerous(true);
        CrimsonRequiemFireball fireball = new CrimsonRequiemFireball(RequiemEntities.CRIMSON_REQUIEM_FIREBALL.get(), player.level());
        for (Projectile projectile : new Projectile[]{skull, fireball}) {
            projectile.setOwner(player);
            projectile.getPersistentData().putUUID(OWNER, player.getUUID());
            projectile.setPos(player.getEyePosition().add(look.scale(0.5)).add(projectile == skull ? side : side.scale(-1)));
            projectile.setDeltaMovement(look.scale(1.1));
            if (projectile instanceof net.minecraft.world.entity.projectile.AbstractHurtingProjectile hurting) {
                hurting.xPower = look.x * 0.1; hurting.yPower = look.y * 0.1; hurting.zPower = look.z * 0.1;
            }
            player.level().addFreshEntity(projectile);
        }
        cooldowns(player).putLong(BURST, time(player) + Math.max(1, CrimsonRequiemConfig.BURST_COOLDOWN.get() * 20L));
    }
}
