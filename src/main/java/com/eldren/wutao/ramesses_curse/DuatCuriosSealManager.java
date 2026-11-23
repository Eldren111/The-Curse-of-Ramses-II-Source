package com.eldren.wutao.ramesses_curse;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotAttribute;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;
import top.theillusivec4.curios.api.event.CurioEquipEvent;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID)
public final class DuatCuriosSealManager {
    private static final int SEALED_OVERLAY_ALPHA = 170;
    private static final double MAX_ISLAND_SEAL_RADIUS = 112.0D;
    public static final String ACTIVE_KEY = "RamessesApepCuriosSealActive";
    private static final String BOSS_KEY = "RamessesApepCuriosSealBoss";
    private static final String LEGACY_DEATH_RELEASED_BOSS_KEY = "RamessesApepCuriosSealDeathReleasedBoss";
    private static final String BOSS_ATTACK_KEY = "RamessesApepAttackBoss";
    private static final String STORED_CURIOS_KEY = "RamessesApepSealedCurios";
    private static final String STORED_IDENTIFIER_KEY = "Identifier";
    private static final String STORED_SLOT_KEY = "Slot";
    private static final String STORED_STACK_KEY = "Stack";

    private DuatCuriosSealManager() {
    }

    /** Returns whether this living entity is currently inside Apep's Curios seal. */
    public static boolean isSealed(LivingEntity wearer) {
        return Config.APEP_CURIOS_SEAL_ENABLED.get()
                && wearer != null
                && wearer.getPersistentData().getBoolean(ACTIVE_KEY);
    }

    /** Use this for item-specific mechanics that are not driven by Curios' tick callback. */
    public static boolean suppressesMechanics(LivingEntity wearer, ItemStack stack) {
        return !KadeshCurioAccess.isReadingOriginalAttributes(wearer)
                && (KadeshCurioAccess.isLocked(wearer, stack)
                || isSealed(wearer) && !isWhitelisted(stack));
    }

    /** True when at least one equipped Curio is actually subject to the seal. */
    public static boolean hasSealedCurio(LivingEntity wearer) {
        if (!isSealed(wearer)) {
            return false;
        }
        return CuriosApi.getCuriosInventory(wearer)
                .map(inventory -> inventory.getCurios().values().stream()
                        .flatMap(handler -> java.util.stream.IntStream.range(0, handler.getStacks().getSlots())
                                .mapToObj(index -> handler.getStacks().getStackInSlot(index)))
                        .anyMatch(stack -> !stack.isEmpty() && !isWhitelisted(stack)))
                .orElse(false);
    }

    public static boolean suppressesEquippedItem(LivingEntity wearer, Item... items) {
        if (!isSealed(wearer) && !(wearer instanceof Player player
                && !KadeshCurioAccess.isWearingSeal(player))) {
            return false;
        }
        return CuriosApi.getCuriosInventory(wearer)
                .map(inventory -> inventory.getCurios().values().stream()
                        .flatMap(handler -> java.util.stream.IntStream.range(0, handler.getStacks().getSlots())
                                .mapToObj(index -> handler.getStacks().getStackInSlot(index)))
                        .anyMatch(stack -> !stack.isEmpty() && suppressesMechanics(wearer, stack)
                                && java.util.Arrays.stream(items).anyMatch(stack::is)))
                .orElse(false);
    }

    public static boolean suppressesTick(LivingEntity wearer, ItemStack stack) {
        return KadeshCurioAccess.isLocked(wearer, stack)
                || Config.APEP_CURIOS_SEAL_TICK_EFFECTS.get() && suppressesMechanics(wearer, stack);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void removeSealedAttributeModifiers(CurioAttributeModifierEvent event) {
        LivingEntity wearer = event.getSlotContext().entity();
        if (!KadeshCurioAccess.isReadingOriginalAttributes(wearer)
                && (KadeshCurioAccess.isLocked(wearer, event.getItemStack())
                || Config.APEP_CURIOS_SEAL_ATTRIBUTE_MODIFIERS.get()
                && suppressesMechanics(wearer, event.getItemStack()))) {
            event.clearModifiers();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void preventCurioEquipWhileSealed(CurioEquipEvent event) {
        // Loading existing equipment is not an equip attempt. The live slot map is incomplete.
        if (CurioRestoreContext.isRestoring(event.getEntity())) return;
        if (suppressesMechanics(event.getEntity(), event.getStack())) {
            event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void tickPlayer(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide
                || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        updatePlayer(player, false);
        if (isSealed(player)) {
            ModItems.suppressSealedCurioEffects(player);
        }
    }

    @SubscribeEvent
    public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            updatePlayer(player, true);
        }
    }

    @SubscribeEvent
    public static void playerRespawned(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getPersistentData().putBoolean(ACTIVE_KEY, false);
            updatePlayer(player, true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void restoreCuriosForDeathDrops(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isSealed(player)) {
            restoreSealedCurios(player);
        }
    }

    private static void updatePlayer(ServerPlayer player, boolean forceSync) {
        clearLegacyDeathReleasedBossId(player);
        boolean active = player.getPersistentData().getBoolean(ACTIVE_KEY);
        if (!Config.APEP_CURIOS_SEAL_ENABLED.get()) {
            if (active) {
                setSealed(player, null, false);
            } else {
                clearBossLink(player);
                restoreSealedCurios(player);
                if (forceSync) {
                    sync(player, false);
                }
            }
            return;
        }

        if (active) {
            UUID bossId = readBossId(player);
            DuatSunEaterEntity boss = findBoss(player, bossId);
            if (boss == null || !boss.isAlive() || !isInsideSealArea(player, boss)) {
                setSealed(player, null, false);
            } else {
                restoreSealedCurios(player);
                if (forceSync) {
                    notifyCurioLifecycle(player, false);
                    enforceSealedCurios(player);
                    markCurioStacksCurrent(player);
                    sync(player, true);
                }
            }
            return;
        }

        UUID linkedBossId = readBossId(player);
        DuatSunEaterEntity linkedBoss = findBoss(player, linkedBossId);
        if (linkedBoss != null && linkedBoss.isAlive() && isInsideSealArea(player, linkedBoss)) {
            setSealed(player, linkedBossId, true);
            return;
        }
        if (linkedBossId != null) {
            clearBossLink(player);
        }
        DuatSunEaterEntity nearbyBoss = player.isSpectator() ? null : findNearbyBoss(player);
        if (nearbyBoss != null) {
            setSealed(player, nearbyBoss.getUUID(), true);
        } else {
            restoreSealedCurios(player);
            if (forceSync) {
                sync(player, false);
            }
        }
    }

    private static DuatSunEaterEntity findNearbyBoss(ServerPlayer player) {
        if (!Level.END.equals(player.level().dimension())) {
            return null;
        }
        double radius = getSealRadius();
        double searchRadius = radius + 384.0D;
        ServerLevel level = (ServerLevel) player.level();
        AABB searchBox = new AABB(
                player.getX() - searchRadius, level.getMinBuildHeight(), player.getZ() - searchRadius,
                player.getX() + searchRadius, level.getMaxBuildHeight(), player.getZ() + searchRadius);
        for (DuatSunEaterEntity boss : level.getEntitiesOfClass(
                DuatSunEaterEntity.class, searchBox,
                entity -> entity.isAlive() && !entity.isRespawnJourneyActive())) {
            double dx = player.getX() - boss.getFightOrigin().getX();
            double dz = player.getZ() - boss.getFightOrigin().getZ();
            if (dx * dx + dz * dz <= radius * radius) {
                return boss;
            }
        }
        return null;
    }

    private static DuatSunEaterEntity findBoss(ServerPlayer player, UUID bossId) {
        if (bossId == null) {
            return null;
        }
        for (ServerLevel level : player.server.getAllLevels()) {
            Entity entity = level.getEntity(bossId);
            if (entity instanceof DuatSunEaterEntity boss) {
                return boss;
            }
        }
        return null;
    }

    public static void bossDefeated(DuatSunEaterEntity boss) {
        if (!(boss.level() instanceof ServerLevel level)) {
            return;
        }
        UUID bossId = boss.getUUID();
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (bossId.equals(readBossId(player))) {
                setSealed(player, null, false);
            }
        }
    }

    public static void markBossAttack(Entity attackEntity, DuatSunEaterEntity boss) {
        attackEntity.getPersistentData().putUUID(BOSS_ATTACK_KEY, boss.getUUID());
    }

    private static boolean isInsideSealArea(ServerPlayer player, DuatSunEaterEntity boss) {
        if (player.level() != boss.level()) {
            return false;
        }
        double radius = getSealRadius();
        double dx = player.getX() - boss.getFightOrigin().getX();
        double dz = player.getZ() - boss.getFightOrigin().getZ();
        return dx * dx + dz * dz <= radius * radius;
    }

    private static boolean isLinkedBossAttack(ServerPlayer player, Entity source, Entity directSource) {
        UUID bossId = readBossId(player);
        return bossId != null && (hasBossId(source, bossId) || hasBossId(directSource, bossId));
    }

    private static boolean hasBossId(Entity entity, UUID bossId) {
        return entity != null && (bossId.equals(entity.getUUID())
                || entity.getPersistentData().hasUUID(BOSS_ATTACK_KEY)
                && bossId.equals(entity.getPersistentData().getUUID(BOSS_ATTACK_KEY)));
    }

    private static void setSealed(ServerPlayer player, UUID bossId, boolean active) {
        if (active == player.getPersistentData().getBoolean(ACTIVE_KEY)) {
            return;
        }
        if (active) {
            notifyCurioLifecycle(player, false);
        }
        player.getPersistentData().putBoolean(ACTIVE_KEY, active);
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (active && bossId != null) {
            persisted.putUUID(BOSS_KEY, bossId);
        } else {
            persisted.remove(BOSS_KEY);
        }
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        if (active) {
            restoreSealedCurios(player);
            enforceSealedCurios(player);
            markCurioStacksCurrent(player);
        } else {
            restoreSealedCurios(player);
            notifyCurioLifecycle(player, true);
            if (Config.APEP_CURIOS_SEAL_ATTRIBUTE_MODIFIERS.get()) {
                applyCurioAttributes(player, true);
            }
            markCurioStacksCurrent(player);
        }
        sync(player, active);
    }

    /** Removes modifiers that were already active when the seal began. */
    private static void enforceSealedCurios(ServerPlayer player) {
        if (!Config.APEP_CURIOS_SEAL_ENABLED.get()
                || !player.getPersistentData().getBoolean(ACTIVE_KEY)) {
            return;
        }
        if (Config.APEP_CURIOS_SEAL_ATTRIBUTE_MODIFIERS.get()) {
            // Temporarily expose the original modifier set; the attribute event intentionally
            // returns an empty set while sealed, which cannot remove modifiers already applied.
            player.getPersistentData().putBoolean(ACTIVE_KEY, false);
            try {
                applyCurioAttributes(player, false);
            } finally {
                player.getPersistentData().putBoolean(ACTIVE_KEY, true);
            }
        }
        CuriosApi.getCuriosInventory(player).ifPresent(inventory ->
                inventory.getCurios().values().forEach(handler -> handler.clearCachedModifiers()));
        ModItems.suppressSealedCurioEffects(player);
    }

    private static void restoreSealedCurios(ServerPlayer player) {
        discardKadeshLockedStoredCurios(player);
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (!persisted.contains(STORED_CURIOS_KEY, Tag.TAG_LIST)) {
            return;
        }
        ListTag stored = persisted.getList(STORED_CURIOS_KEY, Tag.TAG_COMPOUND);
        boolean restoredInventory = CuriosApi.getCuriosInventory(player).map(inventory -> {
            for (int entryIndex = 0; entryIndex < stored.size(); entryIndex++) {
                CompoundTag entry = stored.getCompound(entryIndex);
                ItemStack stack = ItemStack.of(entry.getCompound(STORED_STACK_KEY));
                if (stack.isEmpty()) {
                    continue;
                }
                String identifier = entry.getString(STORED_IDENTIFIER_KEY);
                int slot = entry.getInt(STORED_SLOT_KEY);
                boolean restored = inventory.getStacksHandler(identifier)
                        .filter(handler -> slot >= 0 && slot < handler.getStacks().getSlots())
                        .filter(handler -> handler.getStacks().getStackInSlot(slot).isEmpty())
                        .map(handler -> {
                            handler.getStacks().setStackInSlot(slot, stack);
                            handler.clearCachedModifiers();
                            return true;
                        })
                        .orElse(false);
                if (!restored) {
                    returnStackToPlayer(player, stack);
                }
            }
            return true;
        }).orElse(false);
        if (!restoredInventory) {
            return;
        }
        persisted.remove(STORED_CURIOS_KEY);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        player.inventoryMenu.broadcastChanges();
    }

    static void discardKadeshLockedStoredCurios(ServerPlayer player) {
        if (KadeshCurioAccess.isWearingSeal(player)) {
            return;
        }
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        ListTag stored = persisted.getList(STORED_CURIOS_KEY, Tag.TAG_COMPOUND);
        for (int index = stored.size() - 1; index >= 0; index--) {
            if (KadeshCurioAccess.requiresSeal(ItemStack.of(stored.getCompound(index)
                    .getCompound(STORED_STACK_KEY)))) {
                stored.remove(index);
            }
        }
    }

    private static void returnStackToPlayer(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack) && !stack.isEmpty()) {
            player.drop(stack, false);
        }
    }

    private static void markCurioStacksCurrent(ServerPlayer player) {
        CuriosApi.getCuriosInventory(player).ifPresent(inventory ->
                inventory.getCurios().values().forEach(handler -> {
                    for (int index = 0; index < handler.getStacks().getSlots(); index++) {
                        ItemStack stack = handler.getStacks().getStackInSlot(index);
                        handler.getStacks().setPreviousStackInSlot(index, stack.copy());
                    }
                }));
    }

    /**
     * Gives third-party Curios a real deactivate/reactivate lifecycle while leaving
     * their stacks in the visible slots. This clears mechanics installed by onEquip
     * that do not use Curios' attribute or tick APIs.
     */
    private static void notifyCurioLifecycle(ServerPlayer player, boolean equipping) {
        boolean restoreSealFlag = !equipping && player.getPersistentData().getBoolean(ACTIVE_KEY);
        if (restoreSealFlag) {
            player.getPersistentData().putBoolean(ACTIVE_KEY, false);
        }
        try {
            CuriosApi.getCuriosInventory(player).ifPresent(inventory ->
                    inventory.getCurios().forEach((identifier, handler) -> {
                        for (int index = 0; index < handler.getStacks().getSlots(); index++) {
                            ItemStack stack = handler.getStacks().getStackInSlot(index);
                            if (stack.isEmpty() || isWhitelisted(stack)
                                    || equipping && KadeshCurioAccess.isLocked(player, stack)) {
                                continue;
                            }
                            boolean visible = index < handler.getRenders().size()
                                    && handler.getRenders().get(index);
                            SlotContext context = new SlotContext(identifier, player, index, false, visible);
                            CuriosApi.getCurio(stack).ifPresent(curio -> {
                                if (equipping) {
                                    curio.onEquip(context, ItemStack.EMPTY);
                                } else {
                                    curio.onUnequip(context, ItemStack.EMPTY);
                                }
                            });
                        }
                    }));
        } finally {
            if (restoreSealFlag) {
                player.getPersistentData().putBoolean(ACTIVE_KEY, true);
            }
        }
    }

    private static UUID readBossId(ServerPlayer player) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        return persisted.hasUUID(BOSS_KEY) ? persisted.getUUID(BOSS_KEY) : null;
    }

    private static double getSealRadius() {
        return Math.min(Config.APEP_CURIOS_SEAL_RADIUS.get(), MAX_ISLAND_SEAL_RADIUS);
    }

    private static void clearLegacyDeathReleasedBossId(ServerPlayer player) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (persisted.contains(LEGACY_DEATH_RELEASED_BOSS_KEY)) {
            persisted.remove(LEGACY_DEATH_RELEASED_BOSS_KEY);
            player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        }
    }

    private static void clearBossLink(ServerPlayer player) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persisted.remove(BOSS_KEY);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }

    private static void applyCurioAttributes(ServerPlayer player, boolean add) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        CuriosApi.getCuriosInventory(player).ifPresent(inventory ->
                inventory.getCurios().forEach((identifier, handler) -> {
                    for (int index = 0; index < handler.getStacks().getSlots(); index++) {
                        ItemStack stack = handler.getStacks().getStackInSlot(index);
                        if (stack.isEmpty() || isWhitelisted(stack)) {
                            continue;
                        }
                        boolean visible = index < handler.getRenders().size() && handler.getRenders().get(index);
                        SlotContext context = new SlotContext(identifier, player, index, false, visible);
                        CuriosApi.getAttributeModifiers(context, CuriosApi.getSlotUuid(context), stack)
                                .forEach((attribute, modifier) -> {
                                    if (!(attribute instanceof SlotAttribute)) {
                                        modifiers.put(attribute, modifier);
                                    }
                                });
                    }
                }));
        if (add) {
            player.getAttributes().addTransientAttributeModifiers(modifiers);
        } else {
            player.getAttributes().removeAttributeModifiers(modifiers);
        }
    }

    private static boolean isWhitelisted(ItemStack stack) {
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return Config.APEP_CURIOS_SEAL_WHITELIST.get().stream().anyMatch(id::equals);
    }

    private static void sync(ServerPlayer player, boolean active) {
        List<String> whitelist = Config.APEP_CURIOS_SEAL_WHITELIST.get().stream()
                .map(String::valueOf).toList();
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new DuatCuriosSealPacket(active,
                        SEALED_OVERLAY_ALPHA, whitelist));
    }
}
