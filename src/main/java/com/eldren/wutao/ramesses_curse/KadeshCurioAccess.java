package com.eldren.wutao.ramesses_curse;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.Advancement;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotAttribute;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

@Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID)
public final class KadeshCurioAccess {
    private static final ThreadLocal<Player> ATTRIBUTE_CLEANUP = new ThreadLocal<>();

    private KadeshCurioAccess() {
    }

    public static boolean requiresSeal(ItemStack stack) {
        return !stack.isEmpty() && !stack.is(ModItems.SEAL_OF_KADESH.get())
                && RamessesCurseMod.MODID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace());
    }

    public static boolean isWearingSeal(Player player) {
        // Read the functional slot directly: Curios search results are filtered by this rule.
        return ModEvents.isSealEquippedForPalace(player);
    }

    public static boolean isLocked(LivingEntity wearer, ItemStack stack) {
        return wearer instanceof Player player && requiresSeal(stack) && !isWearingSeal(player);
    }

    public static boolean isReadingOriginalAttributes(LivingEntity wearer) {
        return wearer != null && ATTRIBUTE_CLEANUP.get() == wearer;
    }

    public static boolean suppressAdvancement(ServerPlayer player, Advancement advancement) {
        if (player == null || advancement == null
                || !RamessesCurseMod.MODID.equals(advancement.getId().getNamespace())
                || "wear_seal_of_kadesh".equals(advancement.getId().getPath())) {
            return false;
        }
        return !isWearingSeal(player);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void preventPickup(EntityItemPickupEvent event) {
        if (isLocked(event.getEntity(), event.getItem().getItem())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void preventUse(PlayerInteractEvent.RightClickItem event) {
        if (isLocked(event.getEntity(), event.getItemStack())) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void preventBlockUse(PlayerInteractEvent.RightClickBlock event) {
        if (isLocked(event.getEntity(), event.getItemStack())) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void beforeCuriosTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            enforce(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void tickPlayer(TickEvent.PlayerTickEvent event) {
        if (event.player instanceof ServerPlayer player) {
            enforce(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            enforce(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            enforce(player);
        }
    }

    public static void enforce(ServerPlayer player) {
        if (isWearingSeal(player)) {
            return;
        }
        ModItems.suppressSealedCurioEffects(player);
        boolean[] changed = {false};
        CuriosApi.getCuriosInventory(player).ifPresent(inventory ->
                inventory.getCurios().forEach((identifier, handler) -> {
                    for (int index = 0; index < handler.getStacks().getSlots(); index++) {
                        ItemStack stack = handler.getStacks().getStackInSlot(index);
                        ItemStack previous = handler.getStacks().getPreviousStackInSlot(index);
                        SlotContext context = new SlotContext(identifier, player, index, false,
                                index < handler.getRenders().size() && handler.getRenders().get(index));
                        if (requiresSeal(previous)) {
                            removeAttributes(player, inventory, context, previous);
                        }
                        if (requiresSeal(stack)) {
                            removeAttributes(player, inventory, context, stack);
                            // Curios handles the previous stack's unequip callback and client sync.
                            // A newly injected stack has no previous lifecycle, so clean it here.
                            if (!ItemStack.matches(previous, stack)) {
                                CuriosApi.getCurio(stack).ifPresent(curio ->
                                        curio.onUnequip(context, ItemStack.EMPTY));
                            }
                            handler.getStacks().setStackInSlot(index, ItemStack.EMPTY);
                            handler.clearCachedModifiers();
                            changed[0] = true;
                        }
                    }
                    for (int index = 0; index < handler.getCosmeticStacks().getSlots(); index++) {
                        if (requiresSeal(handler.getCosmeticStacks().getStackInSlot(index))) {
                            handler.getCosmeticStacks().setStackInSlot(index, ItemStack.EMPTY);
                            changed[0] = true;
                        }
                    }
                }));
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (requiresSeal(player.getInventory().getItem(slot))) {
                player.getInventory().setItem(slot, ItemStack.EMPTY);
                changed[0] = true;
            }
        }
        if (requiresSeal(player.containerMenu.getCarried())) {
            player.containerMenu.setCarried(ItemStack.EMPTY);
            changed[0] = true;
        }
        if (requiresSeal(player.inventoryMenu.getCarried())) {
            player.inventoryMenu.setCarried(ItemStack.EMPTY);
            changed[0] = true;
        }
        DuatCuriosSealManager.discardKadeshLockedStoredCurios(player);
        if (changed[0]) {
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
            player.inventoryMenu.broadcastChanges();
        }
    }

    private static void removeAttributes(ServerPlayer player, ICuriosItemHandler inventory,
                                         SlotContext context, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
        Multimap<String, AttributeModifier> slots = HashMultimap.create();
        // Locked attribute queries return nothing; obtain the original set only for removal.
        ATTRIBUTE_CLEANUP.set(player);
        try {
            CuriosApi.getAttributeModifiers(context, CuriosApi.getSlotUuid(context), stack)
                    .forEach((attribute, modifier) -> {
                        if (attribute instanceof SlotAttribute slotAttribute) {
                            slots.put(slotAttribute.getIdentifier(), modifier);
                        } else {
                            attributes.put(attribute, modifier);
                        }
                    });
        } finally {
            ATTRIBUTE_CLEANUP.remove();
        }
        player.getAttributes().removeAttributeModifiers(attributes);
        inventory.removeSlotModifiers(slots);
    }
}
