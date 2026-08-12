package com.eldren.wutao.ramesses_curse;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WardenAmuletProtection {
    private static final double ANGER_CLEAR_RANGE_SQR = 64.0D * 64.0D;
    private static final Map<EntityType<?>, Boolean> WARDEN_NAMED_TYPES = new ConcurrentHashMap<>();

    private WardenAmuletProtection() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void preventTargeting(LivingChangeTargetEvent event) {
        LivingEntity target = event.getNewTarget();
        if (target instanceof Player player
                && isProtected(player)
                && isWardenNamed(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void preventWardenDamage(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof Player player) || !isProtected(player)) {
            return;
        }

        Entity attacker = event.getSource().getEntity();
        Entity directAttacker = event.getSource().getDirectEntity();
        if (isWardenNamed(attacker) || isWardenNamed(directAttacker)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void clearExistingTargets(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Mob mob)
                || mob.level().isClientSide
                || !isWardenNamed(mob)) {
            return;
        }

        LivingEntity target = mob.getTarget();
        if (target instanceof Player player && isProtected(player)) {
            clearTarget(mob, player);
        }

        if (mob instanceof Warden warden && warden.tickCount % 20 == 0) {
            for (ServerPlayer player : warden.level().getServer().getPlayerList().getPlayers()) {
                if (player.level() == warden.level()
                        && player.distanceToSqr(warden) <= ANGER_CLEAR_RANGE_SQR
                        && isProtected(player)) {
                    warden.clearAnger(player);
                }
            }
        }
    }

    private static void clearTarget(Mob mob, Player player) {
        mob.setTarget(null);
        mob.getNavigation().stop();
        if (mob instanceof Warden warden) {
            warden.clearAnger(player);
            warden.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            warden.getBrain().eraseMemory(MemoryModuleType.ROAR_TARGET);
            warden.getBrain().eraseMemory(MemoryModuleType.DISTURBANCE_LOCATION);
        }
    }

    private static boolean isProtected(Player player) {
        return ModItems.HushedDepthsAmulet.isEquipped(player);
    }

    private static boolean isWardenNamed(Entity entity) {
        if (entity == null) {
            return false;
        }
        if (entity instanceof Warden) {
            return true;
        }
        return WARDEN_NAMED_TYPES.computeIfAbsent(entity.getType(), WardenAmuletProtection::hasWardenRegistryName);
    }

    private static boolean hasWardenRegistryName(EntityType<?> entityType) {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
        return id != null && id.toString().toLowerCase(Locale.ROOT).contains("warden");
    }
}
