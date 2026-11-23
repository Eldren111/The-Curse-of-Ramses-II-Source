package com.eldren.wutao.ramesses_requiem.boss;

import com.eldren.wutao.ramesses_requiem.RequiemAddon;
import net.minecraft.server.level.*;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

@Mod.EventBusSubscriber(modid = RequiemAddon.MODID)
public final class WusheControl {
    private record Lock(UUID boss, net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension, long until, Vec3 anchor) {}
    private static final Map<UUID, Lock> LOCKS = new HashMap<>();
    public static boolean heldByOther(ServerPlayer player, UUID boss) { Lock lock = LOCKS.get(player.getUUID()); return lock != null && !lock.boss.equals(boss); }
    public static void begin(ServerPlayer player, WusheBoss boss, int ticks) {
        if (heldByOther(player, boss.getUUID())) return;
        LOCKS.put(player.getUUID(), new Lock(boss.getUUID(), player.level().dimension(), player.level().getGameTime() + ticks, null));
        WusheScenePacket.send(player, new WusheScenePacket(0, boss.getId(), ticks, Vec3.ZERO));
    }
    public static void freeze(ServerPlayer player, WusheBoss boss) {
        Lock lock = LOCKS.get(player.getUUID());
        if (lock == null || !lock.boss.equals(boss.getUUID())) return;
        player.stopRiding();
        LOCKS.put(player.getUUID(), new Lock(lock.boss, lock.dimension, lock.until, player.position()));
        WusheScenePacket.send(player, new WusheScenePacket(3, boss.getId(), (int)Math.max(1, lock.until - player.level().getGameTime()), player.position()));
    }
    public static boolean isFrozen(ServerPlayer player) {
        Lock lock = LOCKS.get(player.getUUID()); return lock != null && lock.anchor != null && valid(player, lock);
    }
    private static boolean valid(ServerPlayer player, Lock lock) {
        return player.isAlive() && !player.isSpectator() && player.level().dimension().equals(lock.dimension)
                && player.level().getGameTime() < lock.until && player.serverLevel().getEntity(lock.boss) instanceof WusheBoss boss
                && boss.isAlive() && (boss.skill() == 4 || boss.skill() == 5) && boss.distanceToSqr(player) < 96 * 96;
    }
    public static void release(ServerPlayer player, UUID boss) {
        Lock lock = LOCKS.get(player.getUUID());
        if (lock == null || boss != null && !lock.boss.equals(boss)) return;
        LOCKS.remove(player.getUUID());
        WusheScenePacket.send(player, new WusheScenePacket(2, -1, 0, Vec3.ZERO));
    }
    public static void releaseBoss(WusheBoss boss) {
        if (boss.getServer() == null) return;
        for (ServerPlayer p : boss.getServer().getPlayerList().getPlayers()) release(p, boss.getUUID());
        LOCKS.entrySet().removeIf(e -> e.getValue().boss.equals(boss.getUUID()));
    }
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        Lock lock = LOCKS.get(player.getUUID());
        if (lock != null) {
            if (!valid(player, lock)) release(player, null);
            else if (lock.anchor != null) {
                player.setDeltaMovement(Vec3.ZERO); player.fallDistance = 0;
                if (player.position().distanceToSqr(lock.anchor) > .0001) player.connection.teleport(lock.anchor.x, lock.anchor.y, lock.anchor.z, player.getYRot(), player.getXRot());
            }
        }
        AstralArena.tickPlayer(player);
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) { release(player, null); AstralArena.returnPlayer(player); }
    }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) { release(player, null); AstralArena.tickPlayer(player); }
    }
    @SubscribeEvent public static void death(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) release(player, null);
    }
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) AstralArena.returnPlayer(player);
    }
    @SubscribeEvent public static void clonePlayer(PlayerEvent.Clone event) {
        var tag = event.getOriginal().getPersistentData();
        if (tag.contains(AstralArena.RETURN)) event.getEntity().getPersistentData().put(AstralArena.RETURN, tag.getCompound(AstralArena.RETURN).copy());
    }
    @SubscribeEvent public static void stop(net.minecraftforge.event.server.ServerStoppedEvent event) { LOCKS.clear(); }
    private WusheControl() {}
}
