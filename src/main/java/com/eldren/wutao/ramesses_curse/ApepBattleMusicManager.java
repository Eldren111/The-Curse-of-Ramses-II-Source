package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID)
public final class ApepBattleMusicManager {
    private static final double ISLAND_MUSIC_RADIUS = 112.0D;
    private static final double BOSS_SEARCH_RADIUS = 256.0D;
    private static final Map<UUID, Boolean> PLAYER_STATES = new HashMap<>();

    private ApepBattleMusicManager() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
                || player.tickCount % 5 != 0) {
            return;
        }

        boolean shouldPlay = shouldPlay(player);
        Boolean previous = PLAYER_STATES.put(player.getUUID(), shouldPlay);
        if (previous == null || previous != shouldPlay || shouldPlay && player.tickCount % 40 == 0) {
            send(player, shouldPlay, false);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        PLAYER_STATES.remove(event.getEntity().getUUID());
    }

    public static void bossDied(DuatSunEaterEntity boss) {
        if (!(boss.level() instanceof ServerLevel level)) {
            return;
        }
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (PLAYER_STATES.getOrDefault(player.getUUID(), false)) {
                PLAYER_STATES.put(player.getUUID(), false);
                send(player, false, true);
            }
        }
    }

    private static boolean shouldPlay(ServerPlayer player) {
        if (!Level.END.equals(player.level().dimension())) {
            return false;
        }
        BlockPos center = new BlockPos(RamessesEndIslandStructure.CENTER_X,
                RamessesEndIslandPiece.CENTER_Y, RamessesEndIslandStructure.CENTER_Z);
        double dx = player.getX() - center.getX();
        double dz = player.getZ() - center.getZ();
        if (dx * dx + dz * dz > ISLAND_MUSIC_RADIUS * ISLAND_MUSIC_RADIUS) {
            return false;
        }

        ServerLevel level = player.serverLevel();
        AABB search = new AABB(center.getX() - BOSS_SEARCH_RADIUS, level.getMinBuildHeight(),
                center.getZ() - BOSS_SEARCH_RADIUS, center.getX() + BOSS_SEARCH_RADIUS,
                level.getMaxBuildHeight(), center.getZ() + BOSS_SEARCH_RADIUS);
        return !level.getEntitiesOfClass(DuatSunEaterEntity.class, search,
                boss -> boss.isAlive() && !boss.isDeadOrDying() && !boss.isRespawnJourneyActive()
                        && boss.getFightOrigin().getX() == center.getX()
                        && boss.getFightOrigin().getZ() == center.getZ()).isEmpty();
    }

    private static void send(ServerPlayer player, boolean playing, boolean immediateStop) {
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new ApepBattleMusicPacket(playing, immediateStop));
    }
}
