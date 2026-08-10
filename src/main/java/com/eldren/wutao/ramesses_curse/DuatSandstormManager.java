package com.eldren.wutao.ramesses_curse;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID)
public final class DuatSandstormManager {
    private static final int DEFAULT_DURATION_TICKS = 20 * 60 * 3;
    private static final int TRANSITION_TICKS = 20 * 15;
    private static final int NATURAL_CHECK_INTERVAL = 20 * 20;
    private static final double DEFAULT_RADIUS = 48.0D;
    private static final Map<UUID, PlayerStorm> ACTIVE_STORMS = new HashMap<>();
    private static boolean automaticStormsEnabled = true;

    private DuatSandstormManager() {
    }

    public static int start(CommandSourceStack source) {
        ServerPlayer player = getDuatPlayer(source);
        if (player == null) {
            return 0;
        }

        automaticStormsEnabled = true;
        ACTIVE_STORMS.computeIfAbsent(player.getUUID(), ignored -> createStorm(player));
        source.sendSuccess(() -> Component.literal("杜阿特自然沙尘暴已开启，风沙正在聚集"), true);
        return 1;
    }

    public static int spawn(CommandSourceStack source) {
        ServerPlayer player = getDuatPlayer(source);
        if (player == null) {
            return 0;
        }

        PlayerStorm storm = createStorm(player);
        ACTIVE_STORMS.put(player.getUUID(), storm);
        sync(player, storm);
        source.sendSuccess(() -> Component.literal("玩家周围的风沙开始聚集"), true);
        return 1;
    }

    public static int stop(CommandSourceStack source) {
        automaticStormsEnabled = false;
        ACTIVE_STORMS.values().forEach(PlayerStorm::beginEnding);
        source.sendSuccess(() -> Component.literal("杜阿特沙尘暴正在逐渐消散"), true);
        return 1;
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) {
            return;
        }

        if (!isDuat(level)) {
            for (ServerPlayer player : level.players()) {
                if (ACTIVE_STORMS.remove(player.getUUID()) != null) {
                    sendClear(player);
                }
            }
            return;
        }

        for (ServerPlayer player : level.players()) {
            UUID playerId = player.getUUID();
            PlayerStorm storm = ACTIVE_STORMS.get(playerId);
            if (storm == null) {
                tryNaturalSpawn(level, player);
                continue;
            }

            storm.tick(player);
            if (storm.isFinished()) {
                ACTIVE_STORMS.remove(playerId);
                sendClear(player);
            } else if (level.getGameTime() % 5L == 0L) {
                sync(player, storm);
            }
        }
    }

    private static void tryNaturalSpawn(ServerLevel level, ServerPlayer player) {
        long staggeredTime = level.getGameTime() + Math.abs(player.getUUID().hashCode());
        if (!automaticStormsEnabled
                || staggeredTime % NATURAL_CHECK_INTERVAL != 0L
                || level.random.nextFloat() > 0.35F) {
            return;
        }

        PlayerStorm storm = createStorm(player);
        ACTIVE_STORMS.put(player.getUUID(), storm);
        sync(player, storm);
    }

    private static PlayerStorm createStorm(ServerPlayer player) {
        double angle = player.getRandom().nextDouble() * Mth.TWO_PI;
        double distance = 10.0D + player.getRandom().nextDouble() * 5.0D;
        return new PlayerStorm(
                player.getX() + Math.cos(angle) * distance,
                player.getZ() + Math.sin(angle) * distance,
                angle,
                distance,
                DEFAULT_RADIUS,
                DEFAULT_DURATION_TICKS);
    }

    private static void sync(ServerPlayer player, PlayerStorm storm) {
        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new DuatSandstormSyncPacket(
                        true,
                        storm.centerX,
                        storm.centerZ,
                        storm.velocityX,
                        storm.velocityZ,
                        storm.radius,
                        storm.intensity(),
                        storm.remainingTicks));
    }

    private static void sendClear(ServerPlayer player) {
        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                DuatSandstormSyncPacket.inactive());
    }

    private static ServerPlayer getDuatPlayer(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("该指令必须由玩家执行"));
            return null;
        }
        if (!isDuat(player.serverLevel())) {
            source.sendFailure(Component.literal("该指令只能在杜阿特维度中使用"));
            return null;
        }
        return player;
    }

    private static boolean isDuat(ServerLevel level) {
        return level.dimension() == ModDimensions.DUAT_DESERT;
    }

    private static final class PlayerStorm {
        private double centerX;
        private double centerZ;
        private double velocityX;
        private double velocityZ;
        private double orbitAngle;
        private final double orbitDistance;
        private final double radius;
        private int remainingTicks;
        private int age;
        private boolean ending;

        private PlayerStorm(
                double centerX,
                double centerZ,
                double orbitAngle,
                double orbitDistance,
                double radius,
                int durationTicks) {
            this.centerX = centerX;
            this.centerZ = centerZ;
            this.orbitAngle = orbitAngle;
            this.orbitDistance = orbitDistance;
            this.radius = radius;
            this.remainingTicks = durationTicks;
        }

        private void tick(ServerPlayer player) {
            age++;
            remainingTicks--;
            orbitAngle += 0.0025D + Math.sin(age * 0.006D) * 0.0007D;

            double breathingDistance = orbitDistance + Math.sin(age * 0.012D) * 2.5D;
            double targetX = player.getX() + Math.cos(orbitAngle) * breathingDistance;
            double targetZ = player.getZ() + Math.sin(orbitAngle) * breathingDistance;
            double nextCenterX = Mth.lerp(0.08D, centerX, targetX);
            double nextCenterZ = Mth.lerp(0.08D, centerZ, targetZ);
            velocityX = nextCenterX - centerX;
            velocityZ = nextCenterZ - centerZ;
            centerX = nextCenterX;
            centerZ = nextCenterZ;
        }

        private void beginEnding() {
            ending = true;
            remainingTicks = Math.min(remainingTicks, TRANSITION_TICKS);
        }

        private boolean isFinished() {
            return remainingTicks <= 0;
        }

        private float intensity() {
            float fadeIn = Mth.clamp(age / (float) TRANSITION_TICKS, 0.0F, 1.0F);
            float fadeOut = Mth.clamp(remainingTicks / (float) TRANSITION_TICKS, 0.0F, 1.0F);
            float transition = ending ? fadeOut : Math.min(fadeIn, fadeOut);
            float gust = 0.82F + 0.18F * Mth.sin(age * 0.045F);
            return Mth.clamp(transition * gust, 0.0F, 1.0F);
        }
    }
}
