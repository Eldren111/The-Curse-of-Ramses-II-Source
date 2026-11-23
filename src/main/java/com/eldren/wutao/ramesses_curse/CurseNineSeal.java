package com.eldren.wutao.ramesses_curse;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/** Curse IX's timed inventory, hand and interaction seal. Equipped Curios remain active. */
@Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID)
public final class CurseNineSeal {
    public static final String CLIENT_TICKS = "RamessesCurseNineSealClientTicks";
    private static final String APPLIED = "RamessesCurseNineSealApplied";

    private CurseNineSeal() {}

    public static boolean isSealed(LivingEntity entity) {
        return entity instanceof Player player && ModEvents.getCurseNineFoodSealTicks(player) > 0;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
            refresh(player, false);
        }
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            refresh(player, true);
        }
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) refresh(player, true);
    }

    private static void refresh(ServerPlayer player, boolean force) {
        int ticks = ModEvents.getCurseNineFoodSealTicks(player);
        boolean active = ticks > 0;
        boolean previous = player.getPersistentData().getBoolean(APPLIED);
        if (previous != active || force && active) {
            player.getPersistentData().putBoolean(APPLIED, active);
            if (active) {
                player.stopUsingItem();
                if (player.containerMenu != player.inventoryMenu) player.closeContainer();
            }
        }
        if (force || previous != active || active && player.tickCount % 20 == 0) {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new CurseNineSealPacket(ticks));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void itemUse(PlayerInteractEvent.RightClickItem event) {
        if (isSealed(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void blockUse(PlayerInteractEvent.RightClickBlock event) {
        if (isSealed(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void blockHit(PlayerInteractEvent.LeftClickBlock event) {
        if (isSealed(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void entityUse(PlayerInteractEvent.EntityInteract event) {
        if (isSealed(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void entityUseSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (isSealed(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void attack(AttackEntityEvent event) {
        if (isSealed(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void breakBlock(BlockEvent.BreakEvent event) {
        if (isSealed(event.getPlayer())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void pickup(EntityItemPickupEvent event) {
        if (isSealed(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void useStart(LivingEntityUseItemEvent.Start event) {
        if (isSealed(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void useTick(LivingEntityUseItemEvent.Tick event) {
        if (isSealed(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void openContainer(PlayerContainerEvent.Open event) {
        if (event.getEntity() instanceof ServerPlayer player && isSealed(player)) player.closeContainer();
    }
}
