package com.eldren.wutao.ramesses_requiem.boss;

import com.eldren.wutao.ramesses_requiem.RequiemAddon;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RequiemAddon.MODID)
public final class AstralArenaEvents {
    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST)
    public static void deathDrops(net.minecraftforge.event.entity.living.LivingDropsEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) AstralArena.returnDeathDrops(player, event.getDrops());
    }
    @SubscribeEvent public static void breakBlock(BlockEvent.BreakEvent event) {
        if (AstralArena.isArena(event.getPlayer().level()) && !event.getPlayer().isCreative()) event.setCanceled(true);
    }
    @SubscribeEvent public static void placeBlock(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() != null && AstralArena.isArena(event.getEntity().level()) && event.getEntity() instanceof ServerPlayer player && !player.isCreative()) event.setCanceled(true);
    }
    @SubscribeEvent public static void explosion(ExplosionEvent.Detonate event) {
        if (AstralArena.isArena(event.getLevel())) event.getAffectedBlocks().clear();
    }
}
