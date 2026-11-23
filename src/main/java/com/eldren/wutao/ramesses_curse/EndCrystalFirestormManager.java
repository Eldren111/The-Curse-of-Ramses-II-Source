package com.eldren.wutao.ramesses_curse;

import com.eldren.wutao.ramesses_curse.integration.goety.GoetyObsidianMonolithCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;

import java.util.Map;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID)
public final class EndCrystalFirestormManager {
    private static final int DURATION_TICKS = 200;
    private static final int DAMAGE_INTERVAL_TICKS = 10;
    private static final double RADIUS = 2.0D;
    private static final Map<ServerLevel, Firestorm> ACTIVE = new WeakHashMap<>();

    private EndCrystalFirestormManager() {
    }

    public static void trigger(ServerLevel level) {
        trigger(level, null);
    }

    public static void trigger(ServerLevel level, Entity activator) {
        Firestorm storm = ACTIVE.get(level);
        if (storm == null) {
            Vec3 lockedCenter = activator instanceof Player player
                    ? player.position()
                    : Vec3.atCenterOf(center(level));
            storm = new Firestorm(lockedCenter);
            ACTIVE.put(level, storm);
            level.playSound(null, BlockPos.containing(storm.center), SoundEvents.BLAZE_SHOOT,
                    SoundSource.HOSTILE, 2.0F, 0.55F);
        }
        storm.remainingTicks = DURATION_TICKS;
        emitParticles(level, storm);
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) {
            return;
        }
        Firestorm storm = ACTIVE.get(level);
        if (storm == null) {
            return;
        }
        if (storm.remainingTicks <= 0) {
            ACTIVE.remove(level);
            return;
        }
        emitParticles(level, storm);
        if (level.getGameTime() % DAMAGE_INTERVAL_TICKS == 0L) {
            damageEntities(level);
        }
        storm.remainingTicks--;
    }

    private static BlockPos center(ServerLevel level) {
        return new BlockPos(RamessesEndIslandStructure.CENTER_X,
                RamessesEndIslandPiece.CENTER_Y, RamessesEndIslandStructure.CENTER_Z);
    }

    private static void emitParticles(ServerLevel level, Firestorm storm) {
        Vec3 center = storm.center;
        ParticleOptions groundFire = goetyParticle("big_fire_ground", ParticleTypes.FLAME);
        ParticleOptions risingFire = goetyParticle("fiery_pillar", ParticleTypes.FLAME);
        int points = 192;
        long time = level.getGameTime();
        // A single compact ring remains fixed at the attacker's activation position.
        for (int ring = 0; ring < 1; ring++) {
            double ringRadius = RADIUS;
            for (int i = 0; i < points; i++) {
                double angle = Math.PI * 2.0D * i / points + (time % 160L) * 0.006D;
                double x = center.x + Math.cos(angle) * ringRadius;
                double z = center.z + Math.sin(angle) * ringRadius;
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        (int) Math.floor(x), (int) Math.floor(z));
                if (isWater(level, BlockPos.containing(x, y - 1.0D, z))) {
                    continue;
                }
                level.sendParticles(groundFire, x, y + 0.16D, z,
                        0, 0.0D, 0.12D, 0.0D, 0.5D);
                level.sendParticles(ParticleTypes.FLAME, x, y + 0.2D, z,
                        3, 0.22D, 0.25D, 0.22D, 0.1D);
                if ((i + ring) % 4 == 0) {
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y + 0.28D, z,
                            1, 0.1D, 0.22D, 0.1D, 0.05D);
                }
            }
        }

        // Dense horizontal emitters keep the whole launch field on one ground plane.
        int gridSize = 15;
        for (int gx = 0; gx < gridSize; gx++) {
            for (int gz = 0; gz < gridSize; gz++) {
                double localX = ((gx + 0.5D) / gridSize * 2.0D - 1.0D) * (RADIUS - 0.15D);
                double localZ = ((gz + 0.5D) / gridSize * 2.0D - 1.0D) * (RADIUS - 0.15D);
                if (localX * localX + localZ * localZ > (RADIUS - 0.2D) * (RADIUS - 0.2D)) {
                    continue;
                }
                double x = center.x + localX;
                double z = center.z + localZ;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    (int) Math.floor(x), (int) Math.floor(z));
            if (isWater(level, BlockPos.containing(x, y - 1.0D, z))) {
                continue;
            }
            // Count zero gives each particle an exact upward velocity. All copies
            // share the same horizontal launch plane instead of forming a sphere.
            for (int particle = 0; particle < 2; particle++) {
                double offset = particle == 0 ? -0.08D : 0.08D;
                level.sendParticles(risingFire, x + offset, y + 0.28D, z - offset,
                        0, 0.0D, 3.5D, 0.0D, 1.0D);
                level.sendParticles(ParticleTypes.FLAME, x - offset, y + 0.18D, z + offset,
                        0, 0.0D, 3.5D, 0.0D, 1.0D);
            }
            if ((gx * gridSize + gz) % 3 == 0) {
                level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 0.9D, z,
                        1, 0.14D, 0.32D, 0.14D, 0.03D);
            }
            }
        }
    }

    private static ParticleOptions goetyParticle(String path, ParticleOptions fallback) {
        if (!ModList.get().isLoaded("goety")) {
            return fallback;
        }
        ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.get(
                new ResourceLocation("goety", path));
        return type instanceof SimpleParticleType simple ? simple : fallback;
    }

    private static void damageEntities(ServerLevel level) {
        Firestorm storm = ACTIVE.get(level);
        if (storm == null) {
            return;
        }
        AABB area = new AABB(storm.center, storm.center).inflate(RADIUS, 12.0D, RADIUS);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area,
                EndCrystalFirestormManager::canDamage)) {
            if (entity.isInWaterOrBubble() || isWater(level, entity.blockPosition())
                    || isWater(level, entity.blockPosition().below())) {
                continue;
            }
            entity.hurt(level.damageSources().inFire(), 5.0F);
            entity.setRemainingFireTicks(100);
        }
    }

    private static boolean canDamage(LivingEntity entity) {
        if (!entity.isAlive() || entity instanceof Player player && player.isSpectator()) {
            return false;
        }
        return !GoetyObsidianMonolithCompat.isIslandMonolith(entity);
    }

    private static boolean isWater(ServerLevel level, BlockPos pos) {
        return level.getFluidState(pos).is(FluidTags.WATER);
    }

    private static final class Firestorm {
        private final Vec3 center;
        private int remainingTicks;

        private Firestorm(Vec3 center) {
            this.center = center;
        }
    }
}
