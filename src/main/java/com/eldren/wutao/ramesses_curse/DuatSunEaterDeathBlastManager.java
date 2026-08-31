package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID)
public final class DuatSunEaterDeathBlastManager {
    private static final int COUNTDOWN_SECONDS = 10;
    private static final int COUNTDOWN_TICKS = COUNTDOWN_SECONDS * 20;
    private static final int DURATION_TICKS = 157;
    private static final int MAX_RADIUS = 108;
    private static final int DESTRUCTION_MIN_Y = 16;
    private static final int DESTRUCTION_MAX_Y = 128;
    private static final Map<ServerLevel, Countdown> PENDING_BLASTS = new WeakHashMap<>();
    private static final Map<ServerLevel, Blast> ACTIVE_BLASTS = new WeakHashMap<>();

    private DuatSunEaterDeathBlastManager() {
    }

    public static void schedule(ServerLevel level, BlockPos center, ServerPlayer player) {
        if (PENDING_BLASTS.containsKey(level) || ACTIVE_BLASTS.containsKey(level)) {
            return;
        }
        PENDING_BLASTS.put(level, new Countdown(center.immutable(), player.getUUID()));
        sendCountdown(player, COUNTDOWN_SECONDS);
    }

    public static void start(ServerLevel level, BlockPos center) {
        PENDING_BLASTS.remove(level);
        ACTIVE_BLASTS.put(level, new Blast(center.immutable()));
        level.playSound(null, center, SoundEvents.GENERIC_EXPLODE,
                SoundSource.HOSTILE, 8.0F, 0.42F);
        level.playSound(null, center, SoundEvents.LIGHTNING_BOLT_THUNDER,
                SoundSource.HOSTILE, 6.0F, 0.48F);
        level.sendParticles(ParticleTypes.FLASH,
                center.getX() + 0.5D, center.getY() + 8.0D, center.getZ() + 0.5D,
                12, 8.0D, 6.0D, 8.0D, 0.0D);

        DuatSunEaterDeathVfxPacket packet = new DuatSunEaterDeathVfxPacket(
                center.getX() + 0.5D, center.getY() + 5.0D, center.getZ() + 0.5D);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(center.getX(), center.getY(), center.getZ()) <= 256.0D * 256.0D) {
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) {
            return;
        }

        Countdown countdown = PENDING_BLASTS.get(level);
        if (countdown != null && countdown.tick(level)) {
            PENDING_BLASTS.remove(level);
            start(level, countdown.center);
        }

        Blast blast = ACTIVE_BLASTS.get(level);
        if (blast == null) {
            return;
        }
        blast.tick(level);
        if (blast.isFinished()) {
            ACTIVE_BLASTS.remove(level);
        }
    }

    private static void sendCountdown(ServerPlayer player, int seconds) {
        player.displayClientMessage(Component.translatable(
                        "message.ramesses_curse.sun_eater_blast_countdown", seconds)
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
    }

    private static final class Countdown {
        private final BlockPos center;
        private final UUID playerId;
        private int remainingTicks = COUNTDOWN_TICKS;

        private Countdown(BlockPos center, UUID playerId) {
            this.center = center;
            this.playerId = playerId;
        }

        private boolean tick(ServerLevel level) {
            this.remainingTicks--;
            if (this.remainingTicks <= 0) {
                return true;
            }
            if (this.remainingTicks % 20 == 0) {
                ServerPlayer player = level.getServer().getPlayerList().getPlayer(this.playerId);
                if (player != null) {
                    sendCountdown(player, this.remainingTicks / 20);
                }
            }
            return false;
        }
    }

    private static final class Blast {
        private final BlockPos center;
        private final Set<Long> loadedChunks = new HashSet<>();
        private int elapsedTicks;
        private int lastRadius = -1;

        private Blast(BlockPos center) {
            this.center = center;
        }

        private void tick(ServerLevel level) {
            this.elapsedTicks++;
            int radius = Mth.clamp(Mth.ceil(this.elapsedTicks * MAX_RADIUS / (float) DURATION_TICKS),
                    1, MAX_RADIUS);
            if (radius > this.lastRadius) {
                this.destroyAnnulus(level, this.lastRadius, radius);
                this.lastRadius = radius;
            }
            if (this.elapsedTicks % 8 == 0) {
                this.emitExpandingEffects(level, radius);
            }
            this.eraseEntities(level, radius + 1.5D);
        }

        private void emitExpandingEffects(ServerLevel level, int radius) {
            double verticalCenter = this.center.getY() + 5.0D;
            double phase = this.elapsedTicks * 0.31D;
            int samples = 8;
            for (int index = 0; index < samples; index++) {
                double angle = Mth.TWO_PI * index / samples + phase;
                double horizontal = radius * (0.72D + 0.24D * Math.sin(index * 2.31D + phase * 0.4D));
                double x = this.center.getX() + 0.5D + Math.cos(angle) * horizontal;
                double y = verticalCenter + Math.sin(index * 1.91D + phase) * radius * 0.34D;
                double z = this.center.getZ() + 0.5D + Math.sin(angle) * horizontal;
                level.sendParticles(ParticleTypes.EXPLOSION, x, y, z,
                        1, 0.18D, 0.18D, 0.18D, 0.0D);
                if ((index & 1) == 0) {
                    level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z,
                            1, 0.12D, 0.12D, 0.12D, 0.015D);
                }
            }
            if (this.elapsedTicks % 16 == 0) {
                double soundAngle = phase * 0.73D;
                BlockPos soundPos = BlockPos.containing(
                        this.center.getX() + 0.5D + Math.cos(soundAngle) * radius,
                        verticalCenter,
                        this.center.getZ() + 0.5D + Math.sin(soundAngle) * radius);
                float progress = this.elapsedTicks / (float) DURATION_TICKS;
                level.playSound(null, soundPos, SoundEvents.GENERIC_EXPLODE,
                        SoundSource.HOSTILE, 4.0F, 0.58F + progress * 0.34F);
            }
        }

        private boolean isFinished() {
            return this.elapsedTicks >= DURATION_TICKS;
        }

        private void destroyAnnulus(ServerLevel level, int innerRadius, int outerRadius) {
            int innerSquared = innerRadius < 0 ? -1 : innerRadius * innerRadius;
            int outerSquared = outerRadius * outerRadius;
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            for (int dx = -outerRadius; dx <= outerRadius; dx++) {
                for (int dz = -outerRadius; dz <= outerRadius; dz++) {
                    int distanceSquared = dx * dx + dz * dz;
                    if (distanceSquared > outerSquared || distanceSquared <= innerSquared) {
                        continue;
                    }
                    int x = this.center.getX() + dx;
                    int z = this.center.getZ() + dz;
                    int chunkX = x >> 4;
                    int chunkZ = z >> 4;
                    if (this.loadedChunks.add(ChunkPos.asLong(chunkX, chunkZ))) {
                        level.getChunk(chunkX, chunkZ);
                    }
                    for (int y = DESTRUCTION_MIN_Y; y <= DESTRUCTION_MAX_Y; y++) {
                        cursor.set(x, y, z);
                        if (!level.getBlockState(cursor).isAir()) {
                            level.setBlock(cursor, Blocks.AIR.defaultBlockState(),
                                    Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                        }
                    }
                }
            }
        }

        private void eraseEntities(ServerLevel level, double radius) {
            AABB bounds = new AABB(
                    this.center.getX() - radius, level.getMinBuildHeight(), this.center.getZ() - radius,
                    this.center.getX() + radius, level.getMaxBuildHeight(), this.center.getZ() + radius);
            double radiusSquared = radius * radius;
            for (Entity target : level.getEntities((Entity) null, bounds, entity -> true)) {
                double dx = target.getX() - this.center.getX();
                double dz = target.getZ() - this.center.getZ();
                if (dx * dx + dz * dz > radiusSquared) {
                    continue;
                }
                if (target instanceof LivingEntity living && living.isAlive()) {
                    living.setHealth(0.0F);
                    living.die(level.damageSources().fellOutOfWorld());
                } else if (!target.isRemoved()) {
                    target.kill();
                }
            }
        }
    }
}
