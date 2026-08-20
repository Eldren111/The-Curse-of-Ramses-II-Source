package com.eldren.wutao.ramesses_curse;

import java.util.function.Supplier;
import net.minecraft.server.level.ServerPlayer;

public final class PacketDistributor {
    public static final PlayerDistributor PLAYER = new PlayerDistributor();
    public static final TrackingEntityDistributor TRACKING_ENTITY_AND_SELF = new TrackingEntityDistributor();

    private PacketDistributor() {
    }

    public static final class PlayerDistributor {
        public PlayerTarget with(Supplier<ServerPlayer> player) {
            return new PlayerTarget(player.get());
        }
    }

    public record PlayerTarget(ServerPlayer player) {
    }

    public static final class TrackingEntityDistributor {
        public TrackingEntityTarget with(Supplier<? extends net.minecraft.world.entity.Entity> entity) {
            return new TrackingEntityTarget(entity.get());
        }
    }

    public record TrackingEntityTarget(net.minecraft.world.entity.Entity entity) {
    }
}
