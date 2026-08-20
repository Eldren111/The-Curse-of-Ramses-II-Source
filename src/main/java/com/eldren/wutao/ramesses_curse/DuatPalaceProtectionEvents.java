package com.eldren.wutao.ramesses_curse;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ArmorStandItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.EndCrystalItem;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingDestroyBlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = RamessesCurseMod.MODID)
public final class DuatPalaceProtectionEvents {
    private DuatPalaceProtectionEvents() {
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!isProtected(event.getPlayer().level(), event.getPos())) {
            return;
        }
        event.setCanceled(true);
        showProtectionMessage(event.getPlayer());
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof Level level) || !isProtected(level, event.getPos())) {
            return;
        }
        event.setCanceled(true);
        showProtectionMessage(event.getEntity());
    }

    @SubscribeEvent
    public static void onBlockToolModification(BlockEvent.BlockToolModificationEvent event) {
        if (!(event.getLevel() instanceof Level level) || !isProtected(level, event.getPos())) {
            return;
        }
        event.setCanceled(true);
        showProtectionMessage(event.getPlayer());
    }

    @SubscribeEvent
    public static void onLivingDestroyBlock(LivingDestroyBlockEvent event) {
        if (isProtected(event.getEntity().level(), event.getPos())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onFluidPlace(BlockEvent.FluidPlaceBlockEvent event) {
        if (event.getLevel() instanceof Level level && isProtected(level, event.getPos())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (!ModDimensions.DUAT_DESERT.equals(event.getLevel().dimension())) {
            return;
        }
        event.getAffectedBlocks().removeIf(pos -> isProtected(event.getLevel(), pos));
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!isPlacementItem(event.getItemStack())) {
            return;
        }

        BlockPos clicked = event.getPos();
        BlockPos adjacent = clicked.relative(event.getFace());
        if (isProtected(event.getLevel(), clicked) || isProtected(event.getLevel(), adjacent)) {
            event.setCanceled(true);
            showProtectionMessage(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!isPlacementItem(event.getItemStack())
                || !isProtected(event.getLevel(), event.getEntity().blockPosition())) {
            return;
        }
        event.setCanceled(true);
        showProtectionMessage(event.getEntity());
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getItemStack().getItem() instanceof SpawnEggItem)
                || !isProtected(event.getLevel(), event.getTarget().blockPosition())) {
            return;
        }
        event.setCanceled(true);
        showProtectionMessage(event.getEntity());
    }

    private static boolean isPlacementItem(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof BlockItem
                || item instanceof SpawnEggItem
                || item instanceof ArmorStandItem
                || item instanceof BoatItem
                || item instanceof MinecartItem
                || item instanceof HangingEntityItem
                || item instanceof EndCrystalItem
                || item instanceof BucketItem;
    }

    private static boolean isProtected(Level level, BlockPos pos) {
        return ModStructures.isInsideDuatPalace(level, pos);
    }

    private static void showProtectionMessage(Entity entity) {
        if (entity instanceof ServerPlayer player) {
            player.displayClientMessage(
                    Component.translatable("message.ramesses_curse.duat_palace.protected")
                            .withStyle(ChatFormatting.RED),
                    true);
        }
    }
}

