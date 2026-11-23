package com.eldren.wutao.ramesses_requiem;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RequiemAddon.MODID)
public final class CrimsonRequiemEvents {
    private CrimsonRequiemEvents() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void lockedAttack(AttackEntityEvent event) {
        if (CrimsonRequiemFlight.locked(event.getEntity().getMainHandItem())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void lockedBlockAttack(PlayerInteractEvent.LeftClickBlock event) {
        if (CrimsonRequiemFlight.locked(event.getEntity().getMainHandItem())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void swapHands(net.minecraftforge.event.entity.living.LivingSwapItemsEvent.Hands event) {
        if (CrimsonRequiemFlight.locked(event.getItemSwappedToMainHand())
                || CrimsonRequiemFlight.locked(event.getItemSwappedToOffHand())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void pursuitAnvil(net.minecraftforge.event.AnvilUpdateEvent event) {
        // Creative anvils otherwise bypass Enchantment.canEnchant restrictions.
        var enchantments = net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantments(event.getRight());
        if ((enchantments.containsKey(ModEnchantments.SOUL_PURSUIT.get())
                || enchantments.containsKey(ModEnchantments.SWORD_STORM.get()))
                && !event.getLeft().is(RequiemItems.CRIMSON_REQUIEM.get())
                && !event.getLeft().is(net.minecraft.world.item.Items.ENCHANTED_BOOK)) event.setCanceled(true);
    }
    public static boolean isCaster(Entity projectile, Entity entity) {
        return projectile != null && projectile.getPersistentData().hasUUID(CrimsonRequiemActions.OWNER)
                && projectile.getPersistentData().getUUID(CrimsonRequiemActions.OWNER).equals(entity.getUUID());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void attack(AttackEntityEvent event) {
        if (!CrimsonRequiemActions.canUse(event.getEntity())) return;
        CrimsonRequiemActions.leftClick(event.getEntity());
        if (event.getEntity().isShiftKeyDown()) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void blockAttack(PlayerInteractEvent.LeftClickBlock event) {
        if (!CrimsonRequiemActions.canUse(event.getEntity())) return;
        if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START) return;
        CrimsonRequiemActions.leftClick(event.getEntity());
        if (event.getEntity().isShiftKeyDown()) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void blockUse(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() == CrimsonRequiemActions.weaponHand(event.getEntity()) && event.getItemStack().is(RequiemItems.CRIMSON_REQUIEM.get())) {
            event.setUseBlock(Event.Result.DENY);
            event.setUseItem(Event.Result.DENY);
            if (!event.getLevel().isClientSide) CrimsonRequiemActions.rightClick(event.getEntity());
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void entityUse(PlayerInteractEvent.EntityInteract event) {
        useEntity(event);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void entityUseSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        useEntity(event);
    }
    private static void useEntity(PlayerInteractEvent event) {
        if (event.getHand() == CrimsonRequiemActions.weaponHand(event.getEntity()) && event.getItemStack().is(RequiemItems.CRIMSON_REQUIEM.get())) {
            if (!event.getLevel().isClientSide) CrimsonRequiemActions.rightClick(event.getEntity());
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void selfDamage(LivingAttackEvent event) {
        if (isCaster(event.getSource().getDirectEntity(), event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void executionStaging(net.minecraftforge.event.entity.living.LivingDamageEvent event) {
        // Clamp after normal damage modifiers; early swords must not accidentally end the finale.
        if (event.getSource().getDirectEntity() instanceof CrimsonRiftExecution cast
                && cast.isTarget(event.getEntity()) && !event.getSource().is(CrimsonRiftExecution.EXECUTION)) {
            event.setAmount(Math.min(event.getAmount(), Math.max(0, event.getEntity().getHealth() - 1)));
        }
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void explosion(ExplosionEvent.Detonate event) {
        Entity source = event.getExplosion().getDirectSourceEntity();
        event.getAffectedEntities().removeIf(entity -> isCaster(source, entity));
    }
    @SubscribeEvent
    public static void looting(LootingLevelEvent event) {
        if (event.getDamageSource() != null && event.getDamageSource().getDirectEntity() instanceof CrimsonRiftExecution cast) {
            event.setLootingLevel(cast.weapon().getEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.MOB_LOOTING));
        }
        if (event.getDamageSource() != null && event.getDamageSource().getDirectEntity() instanceof CrimsonRequiemProjectile projectile) {
            event.setLootingLevel(projectile.weapon().getEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.MOB_LOOTING));
        }
        if (event.getDamageSource() != null && event.getDamageSource().getDirectEntity() instanceof CrimsonSwordStorm sword) {
            event.setLootingLevel(sword.weapon().getEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.MOB_LOOTING));
        }
    }
}
