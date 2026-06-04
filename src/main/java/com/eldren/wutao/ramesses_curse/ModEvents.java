package com.eldren.wutao.ramesses_curse;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.enchanting.EnchantmentLevelSetEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

@Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {
    private static final ResourceLocation NETHER_BRIDGE_CHEST = new ResourceLocation("minecraft", "chests/nether_bridge");
    private static final ResourceLocation BASTION_BRIDGE_CHEST = new ResourceLocation("minecraft", "chests/bastion_bridge");
    private static final ResourceLocation BASTION_HOGLIN_STABLE_CHEST = new ResourceLocation("minecraft", "chests/bastion_hoglin_stable");
    private static final ResourceLocation BASTION_OTHER_CHEST = new ResourceLocation("minecraft", "chests/bastion_other");
    private static final ResourceLocation BASTION_TREASURE_CHEST = new ResourceLocation("minecraft", "chests/bastion_treasure");
    private static final ResourceLocation RUINED_PORTAL_CHEST = new ResourceLocation("minecraft", "chests/ruined_portal");

    private static final String CURSE_DATA = "RamessesCurseData";
    private static final String EQUIP_TICKS = "EquipTicks";
    private static final String MAIN_LOCK_UNTIL = "MainHandLockUntil";
    private static final String NEXT_RANDOM_LOCK = "NextRandomLock";
    private static final String LOCKED_SLOT = "LockedSlot";
    private static final String LOCKED_SLOT_UNTIL = "LockedSlotUntil";
    private static final String NETHER_AWAKENED = "NetherAwakened";
    private static final String STARTER_GRANTED = "StarterGranted";
    private static final String GUILT_PROGRESS = "GuiltProgress";
    private static final String GUILT_SENTENCE_PENDING = "GuiltSentencePending";
    private static final String GUILT_LOCK_UNTIL = "GuiltLockUntil";
    private static final String SATIETY_CRYSTAL_OWNER = "SatietyCrystalOwner";
    private static final String SATIETY_CRYSTAL_COUNT = "SatietyCrystalCount";
    private static final String SATIETY_CRYSTAL_DIMENSION = "SatietyCrystalDimension";
    private static final String SATIETY_CRYSTAL_X = "SatietyCrystalX";
    private static final String SATIETY_CRYSTAL_Y = "SatietyCrystalY";
    private static final String SATIETY_CRYSTAL_Z = "SatietyCrystalZ";
    private static final String LAST_SYNC_GUILT = "LastSyncGuilt";
    private static final String LAST_SYNC_SEAL = "LastSyncSeal";
    private static final String LAST_SYNC_LOCK_SECONDS = "LastSyncLockSeconds";
    private static final String LAST_SYNC_SATIETY = "LastSyncSatiety";
    private static final int CURSE_9_HUNGER_LOSS = 2;
    private static final UUID GUILT_ARMOR_MODIFIER = UUID.fromString("2d7c5100-0a8b-4e93-bf56-fca78f6d7fd3");
    private static final UUID GUILT_TOUGHNESS_MODIFIER = UUID.fromString("8b962d09-4d48-4a89-930f-d23456848976");
    private static final UUID GUILT_KNOCKBACK_MODIFIER = UUID.fromString("4c133cc4-96d4-4d7f-a69e-d7f2fb74e148");

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }

        Player player = event.player;
        CompoundTag data = getCurseData(player);
        boolean sealEquipped = isSealEquipped(player);
        int remainingGuiltLock = getRemainingGuiltLockTicks(player);

        updateHeartSlotAccess(player, sealEquipped);
        syncClientState(player, false);

        if (remainingGuiltLock > 0 && player instanceof ServerPlayer serverPlayer) {
            applyInventoryCooldowns(serverPlayer, remainingGuiltLock);
            suppressDefenses(serverPlayer);
        } else if (player instanceof ServerPlayer serverPlayer) {
            clearDefenseSuppression(serverPlayer);
        }

        if (player.level() instanceof ServerLevel serverLevel) {
            updateCurseNineHunger(serverLevel, player);
        }

        if (isHoldingEmberOfDuat(player)) {
            player.addEffect(new MobEffectInstance(ModMobEffects.INVERSION.get(), 10, 0, false, false, true));
        } else if (player.hasEffect(ModMobEffects.INVERSION.get())) {
            player.removeEffect(ModMobEffects.INVERSION.get());
        }

        if (!sealEquipped) {
            return;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            awardSealAdvancement(serverPlayer);
        }

        long gameTime = player.level().getGameTime();
        data.putInt(EQUIP_TICKS, data.getInt(EQUIP_TICKS) + 1);

        if (!data.getBoolean(NETHER_AWAKENED) && player.level().dimension() == Level.NETHER) {
            data.putBoolean(NETHER_AWAKENED, true);
        }

        if (data.getInt(EQUIP_TICKS) >= Config.MAIN_HAND_LOCK_DELAY.get() && data.getLong(MAIN_LOCK_UNTIL) == 0L) {
            data.putLong(MAIN_LOCK_UNTIL, gameTime + Config.MAIN_HAND_LOCK_DURATION.get());
            data.putLong(NEXT_RANDOM_LOCK, gameTime + Config.INVENTORY_LOCK_INTERVAL.get());
        }

        if (data.getLong(MAIN_LOCK_UNTIL) > 0L && gameTime >= data.getLong(MAIN_LOCK_UNTIL) && data.getLong(NEXT_RANDOM_LOCK) > 0L && gameTime >= data.getLong(NEXT_RANDOM_LOCK)) {
            lockRandomInventorySlot(player, data, gameTime);
            data.putLong(NEXT_RANDOM_LOCK, gameTime + Config.INVENTORY_LOCK_INTERVAL.get());
        }

        if (player.level().dimension() == Level.END) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.WEAKNESS,
                    220,
                    Config.END_WEAKNESS_AMPLIFIER.get(),
                    false,
                    false,
                    true));
        }

        if (data.getBoolean(GUILT_SENTENCE_PENDING) && player instanceof ServerPlayer serverPlayer) {
            applyGuiltLock(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ensureStarterSeal(player);
            syncClientState(player, true);
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                literal("ramessescurse")
                        .then(literal("guilt")
                                .requires(source -> source.hasPermission(2))
                                .then(literal("set")
                                        .then(argument("targets", EntityArgument.players())
                                                .then(argument("value", IntegerArgumentType.integer(0))
                                                        .executes(context -> adjustGuilt(
                                                                context.getSource(),
                                                                EntityArgument.getPlayers(context, "targets"),
                                                                IntegerArgumentType.getInteger(context, "value"),
                                                                GuiltOperation.SET)))))
                                .then(literal("add")
                                        .then(argument("targets", EntityArgument.players())
                                                .then(argument("value", IntegerArgumentType.integer(0))
                                                        .executes(context -> adjustGuilt(
                                                                context.getSource(),
                                                                EntityArgument.getPlayers(context, "targets"),
                                                                IntegerArgumentType.getInteger(context, "value"),
                                                                GuiltOperation.ADD)))))
                                .then(literal("remove")
                                        .then(argument("targets", EntityArgument.players())
                                                .then(argument("value", IntegerArgumentType.integer(0))
                                                        .executes(context -> adjustGuilt(
                                                                context.getSource(),
                                                                EntityArgument.getPlayers(context, "targets"),
                                                                IntegerArgumentType.getInteger(context, "value"),
                                                                GuiltOperation.REMOVE))))))
                        .then(literal("locate")
                                .executes(context -> locateSatietyCrystal(context.getSource(), context.getSource().getPlayerOrException()))
                                .then(argument("target", EntityArgument.player())
                                        .requires(source -> source.hasPermission(2))
                                        .executes(context -> locateSatietyCrystal(context.getSource(), EntityArgument.getPlayer(context, "target")))))
                        .then(literal("locate_tp")
                                .executes(context -> teleportToSatietyCrystal(context.getSource(), context.getSource().getPlayerOrException()))
                                .then(argument("target", EntityArgument.player())
                                        .requires(source -> source.hasPermission(2))
                                        .executes(context -> teleportToSatietyCrystal(context.getSource(), EntityArgument.getPlayer(context, "target")))))
                        .then(literal("cooling")
                                .then(literal("duat")
                                        .requires(source -> source.hasPermission(2))
                                        .executes(context -> clearDuatCooling(context.getSource(), context.getSource().getPlayerOrException()))
                                        .then(argument("target", EntityArgument.player())
                                                .executes(context -> clearDuatCooling(context.getSource(), EntityArgument.getPlayer(context, "target")))))));
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            CompoundTag original = event.getOriginal().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
            event.getEntity().getPersistentData().put(Player.PERSISTED_NBT_TAG, original.copy());
            Player originalPlayer = event.getOriginal();
            Player newPlayer = event.getEntity();
            if (isSealEquipped(originalPlayer)) {
                int debt = getSatietyCrystalCount(newPlayer) * CURSE_9_HUNGER_LOSS + CURSE_9_HUNGER_LOSS;
                newPlayer.getFoodData().setFoodLevel(Math.max(0, 20 - debt));
            }
            if (event.getEntity() instanceof ServerPlayer player) {
                syncClientState(player, true);
            }
        }
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (!isNetherContainerLoot(event.getName())) {
            return;
        }

        LootPool pool = LootPool.lootPool()
                .name("ramesses_curse_ember_of_duat")
                .setRolls(ConstantValue.exactly(1.0F))
                .when(LootItemRandomChanceCondition.randomChance(0.05F))
                .add(LootItem.lootTableItem(ModItems.EMBER_OF_DUAT.get())
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))))
                .build();

        event.getTable().addPool(pool);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (getSatietyCrystalCount(player) > 0 && event.getItemStack().isEdible()) {
            event.setCanceled(true);
            return;
        }
        if (isGuiltLocked(player)) {
            event.setCanceled(true);
            return;
        }
        if (isMainHandLocked(player) && event.getHand() == InteractionHand.MAIN_HAND) {
            event.setCanceled(true);
        }
        if (isLockedInventoryStack(player, event.getItemStack())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (getSatietyCrystalCount(player) > 0 && event.getItemStack().isEdible()) {
            event.setCanceled(true);
            return;
        }
        if (isGuiltLocked(player)) {
            event.setCanceled(true);
            return;
        }
        if (isMainHandLocked(player) && event.getHand() == InteractionHand.MAIN_HAND) {
            event.setCanceled(true);
        }
        if (isLockedInventoryStack(player, event.getItemStack())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(event.getTarget() instanceof AbstractVillager villager)) {
            return;
        }
        if (!isSealEquipped(player)) {
            return;
        }

        for (MerchantOffer offer : villager.getOffers()) {
            int surcharge = Math.max(1, Mth.ceil((float) (offer.getBaseCostA().getCount() * Config.CURSE_4_TRADE_SURCHARGE.get())));
            offer.setSpecialPriceDiff(surcharge);
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (isGuiltLocked(player)) {
            event.setCanceled(true);
            return;
        }
        if (isMainHandLocked(player) && event.getHand() == InteractionHand.MAIN_HAND) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onContainerClick(PlayerContainerEvent.Open event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.containerMenu.broadcastChanges();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        event.getDrops().removeIf(drop -> isSealEntity(drop));
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isGuiltLocked(player)) {
            event.setCanceled(true);
            return;
        }
        if (event.getEntity().level().isClientSide || !(event.getTarget() instanceof LivingEntity target)) {
            return;
        }

        Player player = event.getEntity();

        if (canUseHeartSlot(player) && isHeartEquipped(player) && target instanceof Enemy) {
            ModItems.onHeartOfDuatAttack(player, target, 0.0F);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && canUseHeartSlot(player) && isHeartEquipped(player)) {
            if (ModItems.isHeartOfDuatInvulnerable(player)) {
                event.setCanceled(true);
                event.setAmount(0.0F);
                return;
            }
        }

        LivingEntity target = event.getEntity();
        if (event.getSource().getEntity() instanceof Player player
                && canUseHeartSlot(player)
                && isHeartEquipped(player)
                && target instanceof Enemy) {
            event.setAmount(event.getAmount() + ModItems.HeartOfDuat.getDamageBonus(player));
        }

        if (!(event.getSource().getEntity() instanceof Player player) || player.level().isClientSide) {
            return;
        }

        CompoundTag data = getCurseData(player);
        if (!isSealEquipped(player) || !data.getBoolean(NETHER_AWAKENED)) {
            return;
        }

        if (player.getRandom().nextDouble() < Config.HELL_REBOUND_CHANCE.get()) {
            float rebound = (float) Math.max(1.0D, event.getAmount() * Config.HELL_REBOUND_DAMAGE_MULTIPLIER.get());
            player.hurt(player.damageSources().magic(), rebound);
        }
    }

    @SubscribeEvent
    public static void onLivingExperienceDrop(LivingExperienceDropEvent event) {
        if (!(event.getAttackingPlayer() instanceof ServerPlayer player) || !isSealEquipped(player)) {
            return;
        }
        event.setDroppedExperience(Math.max(0, Mth.floor(event.getDroppedExperience() * Config.CURSE_3_XP_MULTIPLIER.get())));
    }

    @SubscribeEvent
    public static void onEnchantmentLevelSet(EnchantmentLevelSetEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player != null && isSealEquipped(player)) {
            event.setEnchantLevel(Math.min(30, Mth.ceil(event.getEnchantLevel() * Config.CURSE_3_ENCHANT_COST_MULTIPLIER.get())));
        }
    }

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        Player player = event.getPlayer();
        if (!isSealEquipped(player)) {
            return;
        }
        event.setCost(Math.max(0, Mth.ceil(event.getCost() * Config.CURSE_3_ANVIL_COST_MULTIPLIER.get())));
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer fallenPlayer && isSealEquipped(fallenPlayer)) {
            spawnSatietyCrystal(fallenPlayer);
            addSatietyCrystalCount(fallenPlayer, 1);
            syncClientState(fallenPlayer, true);
        }

        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (canUseHeartSlot(player) && isHeartEquipped(player) && event.getEntity() instanceof Enemy) {
            ModItems.HeartOfDuat.onKillEnemy(player, event.getEntity());
        }

        if (!isSealEquipped(player) || !isInnocentVictim(event.getEntity())) {
            return;
        }

        CompoundTag data = getCurseData(player);
        int next = Math.min(Config.CURSE_5_MAX_GUILT.get(), data.getInt(GUILT_PROGRESS) + 1);
        data.putInt(GUILT_PROGRESS, next);
        player.displayClientMessage(Component.translatable("message.ramesses_curse.guilt_progress", next, Config.CURSE_5_MAX_GUILT.get()), true);
        syncClientState(player, true);

        if (next >= Config.CURSE_5_MAX_GUILT.get()) {
            data.putBoolean(GUILT_SENTENCE_PENDING, true);
        }
    }

    public static boolean canUseSealBoundItem(Player player) {
        return isSealEquipped(player);
    }

    public static boolean canUseHeartSlot(Player player) {
        return isSealEquipped(player);
    }

    private static boolean isHeartEquipped(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .resolve()
                .flatMap(inv -> inv.findFirstCurio(ModItems.HEART_OF_DUAT.get()))
                .map(SlotResult::stack)
                .filter(stack -> !stack.isEmpty())
                .isPresent();
    }

    private static int clearDuatCooling(CommandSourceStack source, ServerPlayer target) {
        ModItems.clearHeartWardCooldown(target);
        source.sendSuccess(() -> Component.translatable("command.ramesses_curse.cooling.duat", target.getDisplayName()), true);
        return 1;
    }

    @SubscribeEvent
    public static void onEntityItemPickup(EntityItemPickupEvent event) {
        ItemEntity itemEntity = event.getItem();
        if (!itemEntity.getItem().is(ModItems.SATIETY_CRYSTAL.get())) {
            return;
        }

        Player player = event.getEntity();
        if (!isSatietyCrystalOwner(itemEntity, player)) {
            event.setCanceled(true);
            return;
        }
        player.getFoodData().eat(CURSE_9_HUNGER_LOSS, 0.0F);
        player.removeEffect(MobEffects.HUNGER);
        player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8F, 1.2F);
        addSatietyCrystalCount(player, -1);
        itemEntity.discard();
        syncGuiltState(
                player,
                getCurseData(player).getInt(GUILT_PROGRESS),
                isSealEquipped(player),
                getRemainingGuiltLockTicks(player),
                getSatietyCrystalCount(player));
        event.setCanceled(true);
    }

    public static int consumePurificationScroll(ServerPlayer player) {
        CompoundTag data = getCurseData(player);
        int current = data.getInt(GUILT_PROGRESS);
        if (current <= 0) {
            return 0;
        }

        int removed = Math.min(current, Config.CURSE_5_PURIFICATION_AMOUNT.get());
        data.putInt(GUILT_PROGRESS, current - removed);
        data.putBoolean(GUILT_SENTENCE_PENDING, false);
        syncClientState(player, true);
        return removed;
    }

    public static int getGuiltProgress(Player player) {
        return getCurseData(player).getInt(GUILT_PROGRESS);
    }

    public static int getSealEquipTicks(Player player) {
        return getCurseData(player).getInt(EQUIP_TICKS);
    }

    public static void setGuiltProgress(ServerPlayer player, int guilt) {
        CompoundTag data = getCurseData(player);
        int clamped = Mth.clamp(guilt, 0, Config.CURSE_5_MAX_GUILT.get());
        data.putInt(GUILT_PROGRESS, clamped);
        data.putBoolean(GUILT_SENTENCE_PENDING, clamped >= Config.CURSE_5_MAX_GUILT.get());
        syncClientState(player, true);
    }

    public static void spawnCurseTwoMob(ServerPlayer player) {
        EntityType<?> entityType = Config.getRandomCurseTwoEntityType(player.getRandom().nextInt());
        if (entityType == null) {
            return;
        }

        BlockPos spawnPos = findSpawnPos(player);
        if (spawnPos == null) {
            return;
        }

        if (entityType.spawn(player.serverLevel(), (CompoundTag) null, entity -> {
        }, spawnPos, MobSpawnType.EVENT, true, false) instanceof Mob mob) {
            mob.setTarget(player);
            mob.setGlowingTag(true);
        }
    }

    public static ItemStack findRandomDamageableBurden(ServerPlayer player) {
        List<ItemStack> candidates = new ArrayList<>();

        for (ItemStack stack : player.getInventory().items) {
            if (isDamageableBurden(stack)) {
                candidates.add(stack);
            }
        }
        for (ItemStack stack : player.getInventory().armor) {
            if (isDamageableBurden(stack)) {
                candidates.add(stack);
            }
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (isDamageableBurden(stack)) {
                candidates.add(stack);
            }
        }

        if (candidates.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return candidates.get(player.getRandom().nextInt(candidates.size()));
    }

    public static boolean isMainHandLocked(Player player) {
        CompoundTag data = getCurseData(player);
        return data.getLong(MAIN_LOCK_UNTIL) > player.level().getGameTime();
    }

    public static boolean isLockedInventoryStack(Player player, ItemStack stack) {
        if (isGuiltLocked(player)) {
            return !stack.isEmpty();
        }
        CompoundTag data = getCurseData(player);
        if (data.getLong(LOCKED_SLOT_UNTIL) <= player.level().getGameTime()) {
            return false;
        }
        int slot = data.getInt(LOCKED_SLOT);
        if (slot < 0 || slot >= player.getInventory().items.size()) {
            return false;
        }
        return ItemStack.isSameItemSameTags(player.getInventory().items.get(slot), stack);
    }

    private static boolean isSealEntity(ItemEntity itemEntity) {
        return itemEntity.getItem().is(ModItems.SEAL_OF_KADESH.get());
    }

    private static boolean isGuiltLocked(Player player) {
        CompoundTag data = getCurseData(player);
        return data.getLong(GUILT_LOCK_UNTIL) > player.level().getGameTime();
    }

    private static int getRemainingGuiltLockTicks(Player player) {
        CompoundTag data = getCurseData(player);
        return (int) Math.max(0L, data.getLong(GUILT_LOCK_UNTIL) - player.level().getGameTime());
    }

    private static void ensureStarterSeal(ServerPlayer player) {
        if (!Config.START_WITH_SEAL.get()) {
            return;
        }

        CompoundTag data = getCurseData(player);
        if (data.getBoolean(STARTER_GRANTED)) {
            return;
        }

        ItemStack seal = new ItemStack(ModItems.SEAL_OF_KADESH.get());
        boolean equipped = false;

        if (Config.START_WITH_SEAL_EQUIPPED.get()) {
            equipped = CuriosApi.getCuriosInventory(player)
                    .resolve()
                    .map(inv -> {
                        if (inv.getStacksHandler(RamessesCurseMod.CURSE_SLOT_ID).isPresent()) {
                            inv.setEquippedCurio(RamessesCurseMod.CURSE_SLOT_ID, 0, seal.copy());
                            return true;
                        }
                        return false;
                    })
                    .orElse(false);
        }

        if (!equipped) {
            player.getInventory().add(seal.copy());
        }

        data.putBoolean(STARTER_GRANTED, true);
    }

    private static boolean isSealEquipped(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .resolve()
                .map(inv -> inv.findFirstCurio(ModItems.SEAL_OF_KADESH.get()).isPresent())
                .orElse(false);
    }

    private static void updateHeartSlotAccess(Player player, boolean sealEquipped) {
        CuriosApi.getCuriosInventory(player).ifPresent(inv -> {
            inv.getStacksHandler(RamessesCurseMod.HEART_SLOT_ID).ifPresent(handler -> {
                int slots = handler.getSlots();

                if (sealEquipped) {
                    if (slots <= 0) {
                        inv.growSlotType(RamessesCurseMod.HEART_SLOT_ID, 1);
                    }
                    return;
                }

                ejectHeartSlotContents(player, handler.getStacks());
                if (slots > 0) {
                    inv.shrinkSlotType(RamessesCurseMod.HEART_SLOT_ID, slots);
                }
            });
        });
    }

    private static void ejectHeartSlotContents(Player player, top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler stacks) {
        for (int i = 0; i < stacks.getSlots(); i++) {
            ItemStack stack = stacks.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }

            ItemStack extracted = stack.copy();
            stacks.setStackInSlot(i, ItemStack.EMPTY);

            if (!player.getInventory().add(extracted)) {
                player.drop(extracted, false);
            }
        }
    }

    private static void awardSealAdvancement(ServerPlayer player) {
        Advancement advancement = player.server.getAdvancements().getAdvancement(new ResourceLocation(RamessesCurseMod.MODID, "wear_seal_of_kadesh"));
        if (advancement == null) {
            return;
        }

        PlayerAdvancements advancements = player.getAdvancements();
        AdvancementProgress progress = advancements.getOrStartProgress(advancement);
        if (progress.isDone()) {
            return;
        }

        for (String criterion : progress.getRemainingCriteria()) {
            advancements.award(advancement, criterion);
        }
    }

    private static void spawnSatietyCrystal(ServerPlayer player) {
        ItemEntity crystal = new ItemEntity(
                player.level(),
                player.getX(),
                player.getY() + 2.2D,
                player.getZ(),
                new ItemStack(ModItems.SATIETY_CRYSTAL.get()));
        crystal.setNoGravity(true);
        crystal.setDeltaMovement(0.0D, 0.0D, 0.0D);
        crystal.setUnlimitedLifetime();
        crystal.setInvulnerable(true);
        crystal.setGlowingTag(true);
        crystal.getPersistentData().putUUID(SATIETY_CRYSTAL_OWNER, player.getUUID());
        player.level().addFreshEntity(crystal);
        rememberSatietyCrystal(player, crystal);
    }

    private static void updateCurseNineHunger(ServerLevel level, Player player) {
        int crystalCount = getSatietyCrystalCount(player);
        if (crystalCount <= 0) {
            player.removeEffect(MobEffects.HUNGER);
            return;
        }

        int maxFoodLevel = Math.max(0, 20 - (crystalCount * CURSE_9_HUNGER_LOSS));
        if (player.getFoodData().getFoodLevel() > maxFoodLevel) {
            player.getFoodData().setFoodLevel(maxFoodLevel);
        }
        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 220, 1, false, false, true));
    }

    private static boolean isNetherContainerLoot(ResourceLocation lootTableId) {
        return NETHER_BRIDGE_CHEST.equals(lootTableId)
                || BASTION_BRIDGE_CHEST.equals(lootTableId)
                || BASTION_HOGLIN_STABLE_CHEST.equals(lootTableId)
                || BASTION_OTHER_CHEST.equals(lootTableId)
                || BASTION_TREASURE_CHEST.equals(lootTableId)
                || RUINED_PORTAL_CHEST.equals(lootTableId);
    }

    private static boolean isHoldingEmberOfDuat(Player player) {
        return player.getMainHandItem().is(ModItems.EMBER_OF_DUAT.get())
                || player.getOffhandItem().is(ModItems.EMBER_OF_DUAT.get());
    }

    private static boolean isSatietyCrystalOwner(ItemEntity itemEntity, Player player) {
        return itemEntity.getPersistentData().hasUUID(SATIETY_CRYSTAL_OWNER)
                && player.getUUID().equals(itemEntity.getPersistentData().getUUID(SATIETY_CRYSTAL_OWNER));
    }

    private static ItemEntity findOwnedSatietyCrystal(Iterable<ServerLevel> levels, java.util.UUID ownerId) {
        for (ServerLevel level : levels) {
            List<ItemEntity> matches = level.getEntitiesOfClass(
                    ItemEntity.class,
                    new AABB(-30000000, level.getMinBuildHeight() - 64, -30000000, 30000000, level.getMaxBuildHeight() + 64, 30000000),
                    entity -> entity.getItem().is(ModItems.SATIETY_CRYSTAL.get())
                            && entity.getPersistentData().hasUUID(SATIETY_CRYSTAL_OWNER)
                            && ownerId.equals(entity.getPersistentData().getUUID(SATIETY_CRYSTAL_OWNER)));
            if (!matches.isEmpty()) {
                return matches.get(0);
            }
        }
        return null;
    }

    private static int getSatietyCrystalCount(Player player) {
        return getCurseData(player).getInt(SATIETY_CRYSTAL_COUNT);
    }

    private static void addSatietyCrystalCount(Player player, int delta) {
        CompoundTag data = getCurseData(player);
        int next = Math.max(0, data.getInt(SATIETY_CRYSTAL_COUNT) + delta);
        data.putInt(SATIETY_CRYSTAL_COUNT, next);
        if (next == 0) {
            clearRememberedSatietyCrystal(player);
        }
    }

    private static void rememberSatietyCrystal(Player player, ItemEntity crystal) {
        CompoundTag data = getCurseData(player);
        data.putString(SATIETY_CRYSTAL_DIMENSION, crystal.level().dimension().location().toString());
        data.putInt(SATIETY_CRYSTAL_X, crystal.blockPosition().getX());
        data.putInt(SATIETY_CRYSTAL_Y, crystal.blockPosition().getY());
        data.putInt(SATIETY_CRYSTAL_Z, crystal.blockPosition().getZ());
    }

    private static void clearRememberedSatietyCrystal(Player player) {
        CompoundTag data = getCurseData(player);
        data.remove(SATIETY_CRYSTAL_DIMENSION);
        data.remove(SATIETY_CRYSTAL_X);
        data.remove(SATIETY_CRYSTAL_Y);
        data.remove(SATIETY_CRYSTAL_Z);
    }

    private static BlockPos getRememberedSatietyCrystalPos(Player player) {
        CompoundTag data = getCurseData(player);
        if (!data.contains(SATIETY_CRYSTAL_DIMENSION) || !data.contains(SATIETY_CRYSTAL_X) || !data.contains(SATIETY_CRYSTAL_Y) || !data.contains(SATIETY_CRYSTAL_Z)) {
            return null;
        }
        return new BlockPos(data.getInt(SATIETY_CRYSTAL_X), data.getInt(SATIETY_CRYSTAL_Y), data.getInt(SATIETY_CRYSTAL_Z));
    }

    private static ResourceKey<Level> getRememberedSatietyCrystalDimension(Player player) {
        CompoundTag data = getCurseData(player);
        if (!data.contains(SATIETY_CRYSTAL_DIMENSION)) {
            return null;
        }
        return ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, new ResourceLocation(data.getString(SATIETY_CRYSTAL_DIMENSION)));
    }

    private static int locateSatietyCrystal(CommandSourceStack source, ServerPlayer target) {
        ItemEntity crystal = findOwnedSatietyCrystal(source.getServer().getAllLevels(), target.getUUID());
        if (crystal != null) {
            rememberSatietyCrystal(target, crystal);
        }
        BlockPos crystalPos = crystal != null ? crystal.blockPosition() : getRememberedSatietyCrystalPos(target);

        if (crystalPos == null) {
            source.sendFailure(Component.translatable("command.ramesses_curse.locate.none", target.getDisplayName()));
            return 0;
        }

        MutableComponent clickable = Component.translatable(
                        "command.ramesses_curse.locate.found",
                        target.getDisplayName(),
                        crystalPos.getX(),
                        crystalPos.getY(),
                        crystalPos.getZ())
                .withStyle(style -> style
                        .withColor(0xF6D365)
                        .withClickEvent(new ClickEvent(
                                ClickEvent.Action.RUN_COMMAND,
                                target == source.getEntity() ? "/ramessescurse locate_tp" : "/ramessescurse locate_tp " + target.getGameProfile().getName()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("command.ramesses_curse.locate.hover"))));

        source.sendSuccess(() -> clickable, false);
        return 1;
    }

    private static int teleportToSatietyCrystal(CommandSourceStack source, ServerPlayer target) throws CommandSyntaxException {
        ItemEntity crystal = findOwnedSatietyCrystal(source.getServer().getAllLevels(), target.getUUID());
        if (crystal != null) {
            rememberSatietyCrystal(target, crystal);
        }
        ResourceKey<Level> dimension = crystal != null ? crystal.level().dimension() : getRememberedSatietyCrystalDimension(target);
        BlockPos crystalPos = crystal != null ? crystal.blockPosition() : getRememberedSatietyCrystalPos(target);
        if (dimension == null || crystalPos == null) {
            source.sendFailure(Component.translatable("command.ramesses_curse.locate.none", target.getDisplayName()));
            return 0;
        }

        ServerLevel destination = source.getServer().getLevel(dimension);
        if (destination == null) {
            source.sendFailure(Component.translatable("command.ramesses_curse.locate.none", target.getDisplayName()));
            return 0;
        }

        ServerPlayer playerToMove = source.getPlayerOrException();
        playerToMove.teleportTo(destination, crystalPos.getX() + 0.5D, crystalPos.getY(), crystalPos.getZ() + 0.5D, playerToMove.getYRot(), playerToMove.getXRot());
        return 1;
    }

    private static void lockRandomInventorySlot(Player player, CompoundTag data, long gameTime) {
        List<Integer> filledSlots = new ArrayList<>();
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (!stack.isEmpty()) {
                filledSlots.add(i);
            }
        }
        if (filledSlots.isEmpty()) {
            return;
        }
        int slot = filledSlots.get(player.getRandom().nextInt(filledSlots.size()));
        data.putInt(LOCKED_SLOT, slot);
        data.putLong(LOCKED_SLOT_UNTIL, gameTime + Config.INVENTORY_LOCK_DURATION.get());
    }

    private static BlockPos findSpawnPos(ServerPlayer player) {
        int radius = Config.CURSE_2_SPAWN_RADIUS.get();
        for (int attempt = 0; attempt < 8; attempt++) {
            int x = player.blockPosition().getX() + player.getRandom().nextInt(radius * 2 + 1) - radius;
            int z = player.blockPosition().getZ() + player.getRandom().nextInt(radius * 2 + 1) - radius;
            BlockPos top = player.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, player.blockPosition().getY(), z));
            if (player.level().isEmptyBlock(top) && player.level().isEmptyBlock(top.above())) {
                return top;
            }
        }
        return null;
    }

    private static boolean isDamageableBurden(ItemStack stack) {
        return !stack.isEmpty() && stack.isDamageableItem() && !stack.is(ModItems.SEAL_OF_KADESH.get());
    }

    private static boolean isInnocentVictim(LivingEntity entity) {
        if (entity instanceof Player) {
            return false;
        }
        if (entity instanceof Enemy) {
            return false;
        }
        return entity instanceof Animal || entity instanceof NeutralMob;
    }

    private static void applyGuiltLock(ServerPlayer player) {
        CompoundTag data = getCurseData(player);
        data.putBoolean(GUILT_SENTENCE_PENDING, false);
        data.putInt(GUILT_PROGRESS, 0);
        data.putLong(GUILT_LOCK_UNTIL, player.level().getGameTime() + Config.CURSE_5_LOCK_DURATION.get());
        syncClientState(player, true);
        applyInventoryCooldowns(player, getRemainingGuiltLockTicks(player));

        BlockPos desertPos = findNearestDesert(player.serverLevel(), player.blockPosition(), 2048);
        ServerLevel targetLevel = player.serverLevel();
        BlockPos targetPos = desertPos != null ? desertPos : player.blockPosition();

        if (desertPos != null) {
            targetLevel.setWeatherParameters(0, 12000, true, true);
        }

        double x = targetPos.getX() + 0.5D;
        double y = targetPos.getY() + 24.0D;
        double z = targetPos.getZ() + 0.5D;
        player.teleportTo(targetLevel, x, y, z, player.getYRot(), player.getXRot());

        for (int i = 0; i < Config.CURSE_5_LIGHTNING_STRIKES.get(); i++) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(targetLevel);
            if (bolt != null) {
                bolt.moveTo(player.getX(), player.getY(), player.getZ());
                targetLevel.addFreshEntity(bolt);
            }
        }

        player.hurt(player.damageSources().magic(), Float.MAX_VALUE);
        player.displayClientMessage(Component.translatable("message.ramesses_curse.guilt_sentence").withStyle(net.minecraft.ChatFormatting.GOLD), false);
    }

    private static BlockPos findNearestDesert(ServerLevel level, BlockPos origin, int radius) {
        for (int step = 64; step <= radius; step += 64) {
            for (int dx = -step; dx <= step; dx += 32) {
                for (int dz = -step; dz <= step; dz += 32) {
                    BlockPos sample = origin.offset(dx, 0, dz);
                    BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, sample);
                    if (level.getBiome(top).is(Biomes.DESERT)) {
                        return top;
                    }
                }
            }
        }
        return null;
    }

    private static CompoundTag getCurseData(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(Player.PERSISTED_NBT_TAG)) {
            persistent.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        CompoundTag persisted = persistent.getCompound(Player.PERSISTED_NBT_TAG);
        if (!persisted.contains(CURSE_DATA)) {
            CompoundTag curse = new CompoundTag();
            curse.putInt(LOCKED_SLOT, -1);
            persisted.put(CURSE_DATA, curse);
        }
        return persisted.getCompound(CURSE_DATA);
    }

    private static int adjustGuilt(CommandSourceStack source, Collection<ServerPlayer> targets, int value, GuiltOperation operation) {
        int affected = 0;

        for (ServerPlayer target : targets) {
            int current = getGuiltProgress(target);
            int next = switch (operation) {
                case SET -> value;
                case ADD -> current + value;
                case REMOVE -> current - value;
            };

            setGuiltProgress(target, next);
            affected++;
        }

        if (affected == 1) {
            ServerPlayer target = targets.iterator().next();
            source.sendSuccess(() -> Component.translatable(
                    "command.ramesses_curse.guilt.single",
                    target.getDisplayName(),
                    getGuiltProgress(target),
                    Config.CURSE_5_MAX_GUILT.get()), true);
        } else {
            int affectedPlayers = affected;
            source.sendSuccess(() -> Component.translatable(
                    "command.ramesses_curse.guilt.multiple",
                    affectedPlayers,
                    Config.CURSE_5_MAX_GUILT.get()), true);
        }

        return affected;
    }

    private enum GuiltOperation {
        SET,
        ADD,
        REMOVE
    }

    private static void syncGuiltState(Player player, int guilt, boolean sealEquipped, int lockTicks, int satietyCrystalCount) {
        if (player instanceof ServerPlayer serverPlayer) {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new GuiltSyncPacket(guilt, sealEquipped, lockTicks, satietyCrystalCount));
        }
    }

    private static void syncClientState(Player player, boolean force) {
        CompoundTag data = getCurseData(player);
        int guilt = data.getInt(GUILT_PROGRESS);
        boolean sealEquipped = isSealEquipped(player);
        int lockSeconds = Mth.ceil(getRemainingGuiltLockTicks(player) / 20.0F);
        int satietyCrystalCount = getSatietyCrystalCount(player);

        if (!force
                && data.getInt(LAST_SYNC_GUILT) == guilt
                && data.getBoolean(LAST_SYNC_SEAL) == sealEquipped
                && data.getInt(LAST_SYNC_LOCK_SECONDS) == lockSeconds
                && data.getInt(LAST_SYNC_SATIETY) == satietyCrystalCount) {
            return;
        }

        data.putInt(LAST_SYNC_GUILT, guilt);
        data.putBoolean(LAST_SYNC_SEAL, sealEquipped);
        data.putInt(LAST_SYNC_LOCK_SECONDS, lockSeconds);
        data.putInt(LAST_SYNC_SATIETY, satietyCrystalCount);
        syncGuiltState(player, guilt, sealEquipped, lockSeconds * 20, satietyCrystalCount);
    }

    private static void applyInventoryCooldowns(ServerPlayer player, int remainingTicks) {
        for (ItemStack stack : player.getInventory().items) {
            applyCooldown(player, stack, remainingTicks);
        }
        for (ItemStack stack : player.getInventory().armor) {
            applyCooldown(player, stack, remainingTicks);
        }
        for (ItemStack stack : player.getInventory().offhand) {
            applyCooldown(player, stack, remainingTicks);
        }
    }

    private static void applyCooldown(ServerPlayer player, ItemStack stack, int remainingTicks) {
        if (!stack.isEmpty()) {
            player.getCooldowns().addCooldown(stack.getItem(), remainingTicks);
        }
    }

    private static void suppressDefenses(ServerPlayer player) {
        applyOrRefreshModifier(player, Attributes.ARMOR, GUILT_ARMOR_MODIFIER, -1024.0D);
        applyOrRefreshModifier(player, Attributes.ARMOR_TOUGHNESS, GUILT_TOUGHNESS_MODIFIER, -1024.0D);
        applyOrRefreshModifier(player, Attributes.KNOCKBACK_RESISTANCE, GUILT_KNOCKBACK_MODIFIER, -1.0D);

        List<MobEffectInstance> activeEffects = new ArrayList<>(player.getActiveEffects());
        for (MobEffectInstance effect : activeEffects) {
            if (effect.getEffect().getCategory() == MobEffectCategory.BENEFICIAL) {
                player.removeEffect(effect.getEffect());
            }
        }
    }

    private static void clearDefenseSuppression(ServerPlayer player) {
        removeModifier(player, Attributes.ARMOR, GUILT_ARMOR_MODIFIER);
        removeModifier(player, Attributes.ARMOR_TOUGHNESS, GUILT_TOUGHNESS_MODIFIER);
        removeModifier(player, Attributes.KNOCKBACK_RESISTANCE, GUILT_KNOCKBACK_MODIFIER);
    }

    private static void applyOrRefreshModifier(ServerPlayer player, Attribute attribute, UUID id, double amount) {
        if (player.getAttribute(attribute) == null) {
            return;
        }
        player.getAttribute(attribute).removeModifier(id);
        player.getAttribute(attribute).addTransientModifier(new AttributeModifier(id, "ramesses_curse_guilt_lock", amount, AttributeModifier.Operation.ADDITION));
    }

    private static void removeModifier(ServerPlayer player, Attribute attribute, UUID id) {
        if (player.getAttribute(attribute) != null) {
            player.getAttribute(attribute).removeModifier(id);
        }
    }

    @Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ClientOverlayEvents {
        private static int clientGuilt;
        private static boolean clientSealEquipped;
        private static int clientLockTicks;
        private static int clientSatietyCrystalCount;
        private static int clientHeartBattleStacks;
        private static int clientHeartCooldownTicks;
        private static boolean clientHeartWardReady;

        public static void updateClientGuilt(int guilt, boolean sealEquipped, int lockTicks, int satietyCrystalCount) {
            clientGuilt = guilt;
            clientSealEquipped = sealEquipped;
            clientLockTicks = lockTicks;
            clientSatietyCrystalCount = satietyCrystalCount;
        }

        public static void updateHeartState(int battleStacks, int cooldownTicks, boolean wardReady) {
            clientHeartBattleStacks = battleStacks;
            clientHeartCooldownTicks = cooldownTicks;
            clientHeartWardReady = wardReady;
        }

        public static int getClientHeartBattleStacks() {
            return clientHeartBattleStacks;
        }

        public static int getClientHeartCooldownTicks() {
            return clientHeartCooldownTicks;
        }

        public static boolean isClientHeartWardReady() {
            return clientHeartWardReady;
        }

        @SubscribeEvent
        public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
            if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) {
                return;
            }

            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft.player;
            if (player == null) {
                return;
            }

            if (clientSealEquipped) {
                int max = Config.CURSE_5_MAX_GUILT.get();
                renderGuiltBar(event.getGuiGraphics(), minecraft, clientGuilt, max);
            }
        }

        @SubscribeEvent
        public static void onItemTooltip(ItemTooltipEvent event) {
            Minecraft minecraft = Minecraft.getInstance();

            if (clientLockTicks <= 0) {
                return;
            }

            if (!(minecraft.screen instanceof AbstractContainerScreen<?> screen)) {
                return;
            }

            if (screen.getSlotUnderMouse() == null || !(screen.getSlotUnderMouse().container instanceof Inventory)) {
                return;
            }

            if (!ItemStack.isSameItemSameTags(screen.getSlotUnderMouse().getItem(), event.getItemStack())) {
                return;
            }

            float seconds = clientLockTicks / 20.0F;
            event.getToolTip().add(Component.translatable("hud.ramesses_curse.guilt_lock", String.format("%.1f", seconds))
                    .withStyle(net.minecraft.ChatFormatting.GOLD));
        }

        @SubscribeEvent
        public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
            if (!event.getEntity().hasEffect(ModMobEffects.INVERSION.get())) {
                return;
            }

            Input input = event.getInput();
            input.forwardImpulse = -input.forwardImpulse;
            input.leftImpulse = -input.leftImpulse;

            boolean up = input.up;
            input.up = input.down;
            input.down = up;

            boolean left = input.left;
            input.left = input.right;
            input.right = left;

            boolean jumping = input.jumping;
            input.jumping = input.shiftKeyDown;
            input.shiftKeyDown = jumping;
        }

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null) {
                return;
            }

            while (RamessesCurseMod.HEART_AWAKENING_KEY.consumeClick()) {
                ModNetwork.CHANNEL.sendToServer(new HeartAwakeningPacket());
            }

            while (RamessesCurseMod.HEART_WARD_KEY.consumeClick()) {
                ModNetwork.CHANNEL.sendToServer(new HeartWardActivatePacket());
            }
        }

        private static void renderGuiltBar(net.minecraft.client.gui.GuiGraphics guiGraphics, Minecraft minecraft, int guilt, int max) {
            int barWidth = 96;
            int barHeight = 8;
            int x = minecraft.getWindow().getGuiScaledWidth() - barWidth - 16;
            int y = minecraft.getWindow().getGuiScaledHeight() - 38;
            float ratio = max <= 0 ? 0.0F : Mth.clamp((float) guilt / (float) max, 0.0F, 1.0F);
            int fill = Mth.floor(ratio * barWidth);

            guiGraphics.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xCC2A110A);
            guiGraphics.fill(x, y, x + barWidth, y + barHeight, 0xFF31150E);
            guiGraphics.fill(x, y, x + fill, y + barHeight, 0xFFD6A63A);
            guiGraphics.drawString(minecraft.font, Component.translatable("hud.ramesses_curse.guilt", guilt, max), x, y - 10, 0xFFD6A63A, false);

            if (clientHeartBattleStacks > 0 || clientHeartWardReady || clientHeartCooldownTicks > 0) {
                int battleColor = clientHeartWardReady ? 0xFFFFD166 : 0xFF8BE9FD;
                guiGraphics.drawString(
                        minecraft.font,
                        Component.translatable("message.ramesses_curse.heart_battle_stacks", clientHeartBattleStacks, Config.HEART_BATTLE_STACK_LIMIT.get()),
                        x,
                        y + 12,
                        battleColor,
                        false);
            }
        }
    }
}
