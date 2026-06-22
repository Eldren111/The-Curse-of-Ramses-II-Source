package com.eldren.wutao.ramesses_curse;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.player.Input;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.enchanting.EnchantmentLevelSetEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraft.world.entity.projectile.Projectile;
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
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private static final String CURSE_8_REVERSED = "CurseEightReversed";
    private static final String STARTER_GRANTED = "StarterGranted";
    private static final String GUILT_PROGRESS = "GuiltProgress";
    private static final String GUILT_SENTENCE_PENDING = "GuiltSentencePending";
    private static final String GUILT_LOCK_UNTIL = "GuiltLockUntil";
    private static final String SATIETY_CRYSTAL_OWNER = "SatietyCrystalOwner";
    private static final String SATIETY_CRYSTAL_ID = "SatietyCrystalId";
    private static final String SATIETY_CRYSTAL_COUNT = "SatietyCrystalCount";
    private static final String SATIETY_CRYSTAL_DIMENSION = "SatietyCrystalDimension";
    private static final String SATIETY_CRYSTAL_X = "SatietyCrystalX";
    private static final String SATIETY_CRYSTAL_Y = "SatietyCrystalY";
    private static final String SATIETY_CRYSTAL_Z = "SatietyCrystalZ";
    private static final String SATIETY_CRYSTAL_RECORDS = "SatietyCrystalRecords";
    private static final String LAST_SYNC_GUILT = "LastSyncGuilt";
    private static final String LAST_SYNC_SEAL = "LastSyncSeal";
    private static final String LAST_SYNC_NETHER_AWAKENED = "LastSyncNetherAwakened";
    private static final String LAST_SYNC_CURSE_8_REVERSED = "LastSyncCurseEightReversed";
    private static final String LAST_SYNC_LOCK_SECONDS = "LastSyncLockSeconds";
    private static final String LAST_SYNC_SATIETY = "LastSyncSatiety";
    private static final String LAST_SYNC_LOCKED_SLOT = "LastSyncLockedSlot";
    private static final String LAST_SYNC_LOCKED_SLOT_TICKS = "LastSyncLockedSlotTicks";
    private static final String CURSE_3_ANVIL_COST = "Curse3AnvilCost";
    private static final String CURSE_3_ANVIL_BASE_COST = "Curse3AnvilBaseCost";
    private static final String CURSE_3_PENDING_ENCHANT_COST = "Curse3PendingEnchantCost";
    private static final String EDICT_SEAL_UNTIL = "RamessesEdictSealUntil";
    private static final String EDICT_LIFT_UNTIL = "RamessesEdictLiftUntil";
    private static final String EDICT_SLAM_AT = "RamessesEdictSlamAt";
    private static final String EDICT_SLAMMED = "RamessesEdictSlammed";
    private static final String EDICT_SPELL_BURST_DONE = "RamessesEdictSpellBurstDone";
    private static final String EDICT_AIR_TICK_DAMAGE_AT = "RamessesEdictAirTickDamageAt";
    private static final String EDICT_CASTER_UUID = "RamessesEdictCasterUuid";
    private static final String EDICT_CAST_ID = "RamessesEdictCastId";
    private static final String EDICT_ORIGINAL_NO_AI = "RamessesEdictOriginalNoAi";
    private static final String EDICT_ORIGINAL_NO_GRAVITY = "RamessesEdictOriginalNoGravity";
    private static final String EDICT_DOMAIN_UNTIL = "RamessesEdictDomainUntil";
    private static final String EDICT_DOMAIN_CENTER_X = "RamessesEdictDomainCenterX";
    private static final String EDICT_DOMAIN_CENTER_Y = "RamessesEdictDomainCenterY";
    private static final String EDICT_DOMAIN_CENTER_Z = "RamessesEdictDomainCenterZ";
    private static final String EDICT_DOMAIN_HIT = "RamessesEdictDomainHit";
    private static final int CURSE_9_HUNGER_LOSS = 2;
    private static final int EDICT_RANGE = 12;
    private static final int EDICT_DOMAIN_RANGE = 15;
    private static final int EDICT_CHARGE_TICKS = 60;
    private static final int EDICT_SEAL_TICKS = 200;
    private static final int EDICT_LIFT_TICKS = 12;
    private static final float EDICT_SPELL_DAMAGE = 10.0F;
    private static final float EDICT_SLAM_DAMAGE = 20.0F;
    private static final int EDICT_DOMAIN_DURATION = 200;
    private static final int EDICT_AIR_DAMAGE_INTERVAL = 40;
    private static final double EDICT_CHAIN_PULL_STRENGTH = 0.18D;
    private static final double EDICT_DOMAIN_PULL_STRENGTH = 0.18D;
    private static final UUID GUILT_ARMOR_MODIFIER = UUID.fromString("2d7c5100-0a8b-4e93-bf56-fca78f6d7fd3");
    private static final UUID GUILT_TOUGHNESS_MODIFIER = UUID.fromString("8b962d09-4d48-4a89-930f-d23456848976");
    private static final UUID GUILT_KNOCKBACK_MODIFIER = UUID.fromString("4c133cc4-96d4-4d7f-a69e-d7f2fb74e148");
    private static final Map<ResourceKey<Level>, Map<BlockPos, TrackedMusicPlayback>> ACTIVE_MUSIC_PLAYBACKS = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }

        Player player = event.player;
        CompoundTag data = getCurseData(player);
        boolean sealEquipped = sanitizeSealCurios(player);
        int remainingGuiltLock = getRemainingGuiltLockTicks(player);

        updateHeartSlotAccess(player, sealEquipped);
        syncClientState(player, false);

        if (remainingGuiltLock > 0 && player instanceof ServerPlayer serverPlayer) {
            applyInventoryCooldowns(serverPlayer, remainingGuiltLock);
            suppressDefenses(serverPlayer);
        } else if (player instanceof ServerPlayer serverPlayer) {
            clearDefenseSuppression(serverPlayer);
        }

        int remainingMainHandLock = getRemainingMainHandLockTicks(player);
        if (remainingMainHandLock > 0 && player instanceof ServerPlayer serverPlayer) {
            applyMainHandCooldown(serverPlayer, remainingMainHandLock);
        }

        int remainingLockedSlotTicks = getRemainingLockedSlotTicks(player);
        if (remainingLockedSlotTicks > 0 && player instanceof ServerPlayer serverPlayer) {
            applyLockedSlotCooldown(serverPlayer, remainingLockedSlotTicks);
        }

        if (player.level() instanceof ServerLevel serverLevel) {
            updateCurseNineHunger(serverLevel, player);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            tickEdictDomain(serverPlayer);
        }

        if (player.containerMenu instanceof AnvilMenu anvilMenu) {
            syncCurseThreeAnvilCost(player, anvilMenu, sealEquipped);
        } else {
            data.putInt(CURSE_3_ANVIL_COST, 0);
        }

        cleanupTrackedMusicPlaybacks(player.level());
        ModItems.tickMusicRelic(player);

        if (isHoldingEmberOfDuat(player)) {
            player.addEffect(new MobEffectInstance(ModMobEffects.INVERSION.get(), 10, 0, false, false, true));
        } else if (player.hasEffect(ModMobEffects.INVERSION.get())) {
            player.removeEffect(ModMobEffects.INVERSION.get());
        }

        if (!sealEquipped) {
            ModItems.SealOfKadesh.clearSealCurses(player);
            return;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            awardSealAdvancement(serverPlayer);
        }

        long gameTime = player.level().getGameTime();
        data.putInt(EQUIP_TICKS, data.getInt(EQUIP_TICKS) + 1);
        if (player instanceof ServerPlayer serverPlayer) {
            ModItems.SealOfKadesh.tickSealCurses(serverPlayer, data.getInt(EQUIP_TICKS));
        }

        if (!data.getBoolean(NETHER_AWAKENED) && player.level().dimension() == Level.NETHER) {
            data.putBoolean(NETHER_AWAKENED, true);
            syncClientState(player, true);
        }

        if (data.getInt(EQUIP_TICKS) >= Config.MAIN_HAND_LOCK_DELAY.get() && data.getLong(MAIN_LOCK_UNTIL) == 0L) {
            data.putLong(MAIN_LOCK_UNTIL, gameTime + Config.MAIN_HAND_LOCK_DURATION.get());
            data.putLong(NEXT_RANDOM_LOCK, gameTime + Config.INVENTORY_LOCK_INTERVAL.get());
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(Component.translatable("message.ramesses_curse.main_hand_lock_started").withStyle(net.minecraft.ChatFormatting.GOLD), true);
            }
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
                                .executes(context -> teleportToSatietyCrystal(context.getSource(), context.getSource().getPlayerOrException(), 0))
                                .then(argument("index", IntegerArgumentType.integer(1))
                                        .executes(context -> teleportToSatietyCrystal(
                                                context.getSource(),
                                                context.getSource().getPlayerOrException(),
                                                IntegerArgumentType.getInteger(context, "index") - 1)))
                                .then(argument("target", EntityArgument.player())
                                        .requires(source -> source.hasPermission(2))
                                        .executes(context -> teleportToSatietyCrystal(context.getSource(), EntityArgument.getPlayer(context, "target"), 0))
                                        .then(argument("index", IntegerArgumentType.integer(1))
                                                .executes(context -> teleportToSatietyCrystal(
                                                        context.getSource(),
                                                        EntityArgument.getPlayer(context, "target"),
                                                        IntegerArgumentType.getInteger(context, "index") - 1)))))
                        .then(literal("cooling")
                                .then(literal("duat")
                                        .requires(source -> source.hasPermission(2))
                                        .executes(context -> clearDuatCooling(context.getSource(), context.getSource().getPlayerOrException()))
                                        .then(argument("target", EntityArgument.player())
                                                .executes(context -> clearDuatCooling(context.getSource(), EntityArgument.getPlayer(context, "target")))))
                                .then(literal("edict")
                                        .requires(source -> source.hasPermission(2))
                                        .executes(context -> clearEdictCooling(context.getSource(), context.getSource().getPlayerOrException()))
                                        .then(argument("target", EntityArgument.player())
                                                .executes(context -> clearEdictCooling(context.getSource(), EntityArgument.getPlayer(context, "target")))))));
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

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity living = event.getEntity();
        if (living.level().isClientSide) {
            return;
        }

        tickEdictSeal(living);

        if (living instanceof Mob mob) {
            CompoundTag tag = mob.getPersistentData();
            if (tag.contains(EDICT_DOMAIN_HIT) && living.level() instanceof ServerLevel serverLevel) {
                boolean insideAnyDomain = !serverLevel.getEntitiesOfClass(
                        ServerPlayer.class,
                        mob.getBoundingBox().inflate(EDICT_DOMAIN_RANGE + 2.0D),
                        player -> player.isAlive()
                                && player.getPersistentData().getLong(EDICT_DOMAIN_UNTIL) > player.level().getGameTime()
                                && isInsideEdictDomain(player.getPersistentData(), mob.position()))
                        .isEmpty();

                if (!insideAnyDomain) {
                    clearEdictDomainTarget(mob);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (isEdictSealed(event.getSource().getEntity())) {
            event.setCanceled(true);
            return;
        }
        Player lockedAttacker = getLockedAttacker(event.getSource());
        if (lockedAttacker != null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (isEdictSealed(event.getSource().getEntity())) {
            event.setCanceled(true);
            event.setAmount(0.0F);
            return;
        }
        Player lockedAttacker = getLockedAttacker(event.getSource());
        if (lockedAttacker != null) {
            event.setCanceled(true);
            event.setAmount(0.0F);
            return;
        }

        if (event.getEntity() instanceof ServerPlayer player && canUseHeartSlot(player) && isHeartEquipped(player)) {
            if (ModItems.isHeartOfDuatInvulnerable(player)) {
                event.setCanceled(true);
                event.setAmount(0.0F);
                return;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamageFinal(LivingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (event.getSource().getEntity() instanceof Player player
                && !player.level().isClientSide
                && ModItems.isMusicRelicAwakened(player)
                && !ModItems.hasMusicAttackModifier(player)) {
            event.setAmount(event.getAmount() * ModItems.getMusicAttackMultiplier());
        }

        if (event.getSource().getEntity() instanceof Player player
                && canUseHeartSlot(player)
                && isHeartEquipped(player)
                && target instanceof Enemy) {
            event.setAmount(event.getAmount() + ModItems.HeartOfDuat.getDamageBonus(player));
        }

        if (event.getSource().getEntity() instanceof Player player
                && !player.level().isClientSide
                && target instanceof Enemy) {
            event.setAmount(ModItems.onUraeusNecklaceAttack(player, target, event.getAmount()));
        }

        if (!(event.getSource().getEntity() instanceof Player player) || player.level().isClientSide) {
            return;
        }

        CompoundTag data = getCurseData(player);
        if (!isSealEquipped(player) || !data.getBoolean(NETHER_AWAKENED)) {
            return;
        }

        if (data.getBoolean(CURSE_8_REVERSED)) {
            if (player.getMainHandItem().isEmpty()) {
                return;
            }
            if (player.getRandom().nextDouble() < Config.HELL_REBOUND_CHANCE.get()) {
                float assistDamage = event.getAmount();
                event.setAmount(event.getAmount() + assistDamage);
                player.displayClientMessage(Component.translatable("message.ramesses_curse.curse_8_assist", String.format(java.util.Locale.ROOT, "%.1f", assistDamage))
                        .withStyle(net.minecraft.ChatFormatting.GREEN), true);
                player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7F, 1.2F);
            }
            return;
        }

        if (player.getRandom().nextDouble() < Config.HELL_REBOUND_CHANCE.get()) {
            float rebound = (float) Math.max(1.0D, event.getAmount() * Config.HELL_REBOUND_DAMAGE_MULTIPLIER.get() + Config.HELL_REBOUND_FLAT_DAMAGE.get());
            player.hurt(player.damageSources().magic(), rebound);
            player.displayClientMessage(Component.translatable("message.ramesses_curse.hell_rebound", String.format(java.util.Locale.ROOT, "%.1f", rebound))
                    .withStyle(net.minecraft.ChatFormatting.RED), true);
            player.level().playSound(null, player.blockPosition(), SoundEvents.BLAZE_HURT, SoundSource.PLAYERS, 0.85F, 0.75F);
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
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerLevelChange(PlayerXpEvent.LevelChange event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || player.getAbilities().instabuild || event.getLevels() >= 0 || !isSealEquipped(player)) {
            return;
        }

        if (player.containerMenu instanceof EnchantmentMenu) {
            int spentLevels = -event.getLevels();
            int adjustedLevels = resolveAdjustedEnchantSpend(player, spentLevels);
            event.setLevels(-adjustedLevels);
            return;
        }

        if (player.containerMenu instanceof AnvilMenu anvilMenu) {
            int storedCost = getCurseData(player).getInt(CURSE_3_ANVIL_COST);
            if (storedCost <= 0) {
                storedCost = Math.max(0, Mth.ceil(anvilMenu.getCost() * Config.CURSE_3_ANVIL_COST_MULTIPLIER.get()));
                getCurseData(player).putInt(CURSE_3_ANVIL_COST, storedCost);
            }
            if (storedCost > 0) {
                event.setLevels(-storedCost);
            }
        }
    }

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        Player player = event.getPlayer();
        if (!isSealEquipped(player)) {
            clearCurseThreeAnvilCosts(player);
            return;
        }
        int baseCost = Math.max(0, event.getCost());
        int adjustedCost = Math.max(0, Mth.ceil(baseCost * Config.CURSE_3_ANVIL_COST_MULTIPLIER.get()));
        event.setCost(adjustedCost);
        storeCurseThreeAnvilCosts(player, baseCost, adjustedCost);
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

        if (isSealEquipped(player) && Config.isCurseEightBoss(event.getEntity().getType())) {
            CompoundTag data = getCurseData(player);
            if (data.getBoolean(NETHER_AWAKENED) && !data.getBoolean(CURSE_8_REVERSED)) {
                data.putBoolean(CURSE_8_REVERSED, true);
                player.displayClientMessage(Component.translatable("message.ramesses_curse.curse_8_reversed").withStyle(net.minecraft.ChatFormatting.GREEN, net.minecraft.ChatFormatting.BOLD), false);
                syncClientState(player, true);
            }
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

    public static boolean isNetherAwakened(Player player) {
        if (player == null) {
            return false;
        }
        if (player.level().isClientSide) {
            return ClientOverlayEvents.isClientNetherAwakened();
        }
        return getCurseData(player).getBoolean(NETHER_AWAKENED);
    }

    public static boolean isCurseEightReversed(Player player) {
        if (player == null) {
            return false;
        }
        if (player.level().isClientSide) {
            return ClientOverlayEvents.isClientCurseEightReversed();
        }
        return getCurseData(player).getBoolean(CURSE_8_REVERSED);
    }

    public static int getDisplayedEnchantCost(Player player, int vanillaCost) {
        return getAdjustedEnchantLevelCost(player, vanillaCost);
    }

    public static int getCurseThreeEnchantExtraCost(Player player, int vanillaCost) {
        return Math.max(0, getDisplayedEnchantCost(player, vanillaCost) - Math.max(0, vanillaCost));
    }

    public static int getDisplayedAnvilCost(Player player, int currentCost) {
        if (player == null || currentCost <= 0 || !isSealEquipped(player)) {
            return currentCost;
        }

        int storedCost = getCurseData(player).getInt(CURSE_3_ANVIL_COST);
        return storedCost > 0 ? storedCost : currentCost;
    }

    public static int getCurseThreeAnvilExtraCost(Player player, int vanillaCost) {
        return Math.max(0, getDisplayedAnvilCost(player, vanillaCost) - Math.max(0, vanillaCost));
    }

    public static void storeCurseThreeAnvilCosts(Player player, int baseCost, int adjustedCost) {
        CompoundTag data = getCurseData(player);
        data.putInt(CURSE_3_ANVIL_BASE_COST, Math.max(0, baseCost));
        data.putInt(CURSE_3_ANVIL_COST, Math.max(0, adjustedCost));
    }

    public static void clearCurseThreeAnvilCosts(Player player) {
        storeCurseThreeAnvilCosts(player, 0, 0);
    }

    public static int getDisplayedAnvilBaseCost(Player player, int fallbackCost) {
        if (player == null || fallbackCost <= 0 || !isSealEquipped(player)) {
            return fallbackCost;
        }

        int baseCost = getCurseData(player).getInt(CURSE_3_ANVIL_BASE_COST);
        return baseCost > 0 ? baseCost : fallbackCost;
    }

    public static int getAdjustedEnchantLevelCost(Player player, int vanillaCost) {
        if (player == null || vanillaCost <= 0 || !isSealEquipped(player)) {
            return vanillaCost;
        }
        return Math.max(vanillaCost, Mth.ceil(vanillaCost * Config.CURSE_3_ENCHANT_COST_MULTIPLIER.get()));
    }

    public static int getAdjustedEnchantSpend(Player player, int vanillaSpend) {
        if (player == null || vanillaSpend <= 0 || !isSealEquipped(player)) {
            return vanillaSpend;
        }
        return Math.max(vanillaSpend, Mth.ceil(vanillaSpend * Config.CURSE_3_ENCHANT_COST_MULTIPLIER.get()));
    }

    public static int resolveAdjustedEnchantSpend(Player player, int vanillaSpend) {
        if (player == null || vanillaSpend <= 0 || !isSealEquipped(player)) {
            return vanillaSpend;
        }

        CompoundTag data = getCurseData(player);
        int pendingCost = data.getInt(CURSE_3_PENDING_ENCHANT_COST);
        if (pendingCost > 0) {
            data.putInt(CURSE_3_PENDING_ENCHANT_COST, 0);
            return pendingCost;
        }

        if (player.containerMenu instanceof EnchantmentMenu menu
                && vanillaSpend >= 1
                && vanillaSpend <= menu.costs.length) {
            return getAdjustedEnchantLevelCost(player, menu.costs[vanillaSpend - 1]);
        }

        return getAdjustedEnchantSpend(player, vanillaSpend);
    }

    public static Component getCurseThreeEnchantTooltip(Player player, int vanillaCost) {
        return Component.literal("诅咒三已生效：附魔额外 +"
                        + getCurseThreeEnchantExtraCost(player, vanillaCost)
                        + "，当前倍率 x"
                        + ClientOverlayEvents.formatMultiplier(Config.CURSE_3_ENCHANT_COST_MULTIPLIER.get()))
                .withStyle(net.minecraft.ChatFormatting.RED);
    }

    public static Component getCurseThreeAnvilTooltip(Player player, int vanillaCost) {
        return Component.literal("诅咒三已生效：铁砧额外 +"
                        + getCurseThreeAnvilExtraCost(player, vanillaCost)
                        + "，当前倍率 x"
                        + ClientOverlayEvents.formatMultiplier(Config.CURSE_3_ANVIL_COST_MULTIPLIER.get()))
                .withStyle(net.minecraft.ChatFormatting.RED);
    }

    public static Component getCurseThreeInsufficientXpMessage() {
        return Component.literal("经验不足，无法附魔")
                .withStyle(net.minecraft.ChatFormatting.RED);
    }
    public static void setPendingEnchantCost(Player player, int cost) {
        getCurseData(player).putInt(CURSE_3_PENDING_ENCHANT_COST, Math.max(0, cost));
    }

    public static boolean canUseHeartSlot(Player player) {
        return isSealEquipped(player);
    }

    public enum EdictCastResult {
        SUCCESS,
        NO_TARGETS,
        OTHER_MODE_ACTIVE
    }

    public static EdictCastResult activateEdictOfTwoLands(ServerPlayer player) {
        if (isEdictDomainActive(player)) {
            return EdictCastResult.OTHER_MODE_ACTIVE;
        }

        List<LivingEntity> targets = player.level().getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(EDICT_RANGE),
                entity -> entity instanceof Enemy && entity.isAlive() && entity != player);

        if (targets.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.ramesses_curse.edict_no_targets")
                    .withStyle(net.minecraft.ChatFormatting.RED), true);
            return EdictCastResult.NO_TARGETS;
        }

        long gameTime = player.level().getGameTime();
        ServerLevel serverLevel = player.serverLevel();
        UUID castId = UUID.randomUUID();

        player.level().playSound(null, player.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, 0.75F);
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new EdictOfTwoLandsTriggeredPacket());

        for (LivingEntity target : targets) {
            CompoundTag tag = target.getPersistentData();
            tag.putLong(EDICT_SEAL_UNTIL, gameTime + EDICT_LIFT_TICKS + EDICT_SEAL_TICKS);
            tag.putLong(EDICT_LIFT_UNTIL, gameTime + EDICT_LIFT_TICKS);
            tag.putLong(EDICT_SLAM_AT, gameTime + EDICT_LIFT_TICKS + EDICT_SEAL_TICKS);
            tag.putBoolean(EDICT_SLAMMED, false);
            tag.putBoolean(EDICT_SPELL_BURST_DONE, false);
            tag.putLong(EDICT_AIR_TICK_DAMAGE_AT, gameTime + EDICT_LIFT_TICKS + EDICT_AIR_DAMAGE_INTERVAL);
            tag.putUUID(EDICT_CASTER_UUID, player.getUUID());
            tag.putUUID(EDICT_CAST_ID, castId);
            tag.putBoolean(EDICT_ORIGINAL_NO_GRAVITY, target.isNoGravity());
            if (target instanceof Mob mob) {
                tag.putBoolean(EDICT_ORIGINAL_NO_AI, mob.isNoAi());
            }
            spawnEdictLaunchBurst(serverLevel, target.position().add(0.0D, 0.2D, 0.0D));
            target.setNoGravity(false);
            target.fallDistance = 0.0F;
            target.hasImpulse = true;
            target.hurtMarked = true;
            target.setDeltaMovement(0.0D, 0.68D, 0.0D);
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, EDICT_LIFT_TICKS + EDICT_SEAL_TICKS, 0, false, false, true));

            spawnEdictDebris(serverLevel, target);
        }

        return EdictCastResult.SUCCESS;
    }

    public static EdictCastResult activateEdictOfTwoLandsDomain(ServerPlayer player) {
        if (hasActiveEdictSeal(player)) {
            return EdictCastResult.OTHER_MODE_ACTIVE;
        }

        List<LivingEntity> targets = player.level().getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(EDICT_DOMAIN_RANGE),
                entity -> entity instanceof Enemy && entity.isAlive() && entity != player);

        if (targets.isEmpty()) {
            return EdictCastResult.NO_TARGETS;
        }

        long gameTime = player.level().getGameTime();
        CompoundTag playerData = player.getPersistentData();
        playerData.putLong(EDICT_DOMAIN_UNTIL, gameTime + EDICT_DOMAIN_DURATION);
        playerData.putDouble(EDICT_DOMAIN_CENTER_X, player.getX());
        playerData.putDouble(EDICT_DOMAIN_CENTER_Y, player.getY() + 0.08D);
        playerData.putDouble(EDICT_DOMAIN_CENTER_Z, player.getZ());
        player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 0.6F);
        player.level().playSound(null, player.blockPosition(), SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.PLAYERS, 0.9F, 0.55F);
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new EdictOfTwoLandsTriggeredPacket());

        for (LivingEntity target : targets) {
            applyEdictDomainToTarget(
                    player,
                    target,
                    playerData.getDouble(EDICT_DOMAIN_CENTER_X),
                    playerData.getDouble(EDICT_DOMAIN_CENTER_Y),
                    playerData.getDouble(EDICT_DOMAIN_CENTER_Z),
                    0.0F);
        }

        return EdictCastResult.SUCCESS;
    }

    public static boolean isMusicRelicEquipped(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .resolve()
                .map(inv -> inv.getStacksHandler(RamessesCurseMod.MUSIC_SLOT_ID)
                        .map(handler -> containsMusicRelic(handler.getStacks()))
                        .orElse(false))
                .orElse(false);
    }

    public static void registerMusicDiscPlayback(Level level, BlockPos pos, int lengthTicks) {
        if (level.isClientSide || lengthTicks <= 0) {
            return;
        }

        ACTIVE_MUSIC_PLAYBACKS
                .computeIfAbsent(level.dimension(), key -> new HashMap<>())
                .put(pos.immutable(), new TrackedMusicPlayback(level.getGameTime(), level.getGameTime() + lengthTicks));
    }

    public static MusicPlaybackState findNearbyTrackedDisc(Player player, int radius) {
        Map<BlockPos, TrackedMusicPlayback> playbacks = ACTIVE_MUSIC_PLAYBACKS.get(player.level().dimension());
        if (playbacks == null || playbacks.isEmpty()) {
            return null;
        }

        long gameTime = player.level().getGameTime();
        double maxDistanceSq = radius * radius;
        MusicPlaybackState closest = null;
        double closestDistanceSq = Double.MAX_VALUE;

        for (Map.Entry<BlockPos, TrackedMusicPlayback> entry : playbacks.entrySet()) {
            BlockPos pos = entry.getKey();
            TrackedMusicPlayback playback = entry.getValue();
            if (gameTime > playback.endTick || !player.level().isLoaded(pos)) {
                continue;
            }

            double distanceSq = pos.distToCenterSqr(player.position());
            if (distanceSq > maxDistanceSq || distanceSq >= closestDistanceSq) {
                continue;
            }

            closestDistanceSq = distanceSq;
            closest = new MusicPlaybackState(pos, playback.startTick, (int) Math.max(1L, playback.endTick - playback.startTick));
        }

        return closest;
    }

    public static void cancelNearbyTrackedMusicDisc(Player player, int radius) {
        MusicPlaybackState playbackState = findNearbyTrackedDisc(player, radius);
        if (playbackState != null) {
            consumeTrackedMusicDisc(player.level(), playbackState.pos());
        }
    }

    public static void consumeTrackedMusicDisc(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof JukeboxBlockEntity jukebox) {
            level.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.RECORDS, 1.0F, 0.9F);
            jukebox.removeFirstItem();
            level.setBlock(pos, level.getBlockState(pos).setValue(JukeboxBlock.HAS_RECORD, false), 2);
        }

        Map<BlockPos, TrackedMusicPlayback> playbacks = ACTIVE_MUSIC_PLAYBACKS.get(level.dimension());
        if (playbacks != null) {
            playbacks.remove(pos);
            if (playbacks.isEmpty()) {
                ACTIVE_MUSIC_PLAYBACKS.remove(level.dimension());
            }
        }
    }

    public static boolean hasSealAnywhere(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .resolve()
                .map(inv -> inv.getCurios().values().stream()
                        .anyMatch(handler -> containsSeal(handler.getStacks())))
                .orElse(false);
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

    private static int clearEdictCooling(CommandSourceStack source, ServerPlayer target) {
        ModItems.clearEdictCooldown(target);
        source.sendSuccess(() -> Component.translatable("command.ramesses_curse.cooling.edict", target.getDisplayName()), true);
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
                getCurseData(player).getBoolean(NETHER_AWAKENED),
                getCurseData(player).getBoolean(CURSE_8_REVERSED),
                getRemainingGuiltLockTicks(player),
                getSatietyCrystalCount(player),
                getCurseData(player).getInt(LOCKED_SLOT),
                getRemainingLockedSlotTicks(player));
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

    private static int getRemainingMainHandLockTicks(Player player) {
        CompoundTag data = getCurseData(player);
        return (int) Math.max(0L, data.getLong(MAIN_LOCK_UNTIL) - player.level().getGameTime());
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

    private static int getRemainingLockedSlotTicks(Player player) {
        CompoundTag data = getCurseData(player);
        return (int) Math.max(0L, data.getLong(LOCKED_SLOT_UNTIL) - player.level().getGameTime());
    }

    private static void syncCurseThreeAnvilCost(Player player, AnvilMenu anvilMenu, boolean sealEquipped) {
        CompoundTag data = getCurseData(player);
        if (!sealEquipped) {
            clearCurseThreeAnvilCosts(player);
            return;
        }

        int storedCost = data.getInt(CURSE_3_ANVIL_COST);
        if (storedCost > 0 && anvilMenu.getCost() != storedCost) {
            anvilMenu.setMaximumCost(storedCost);
            player.containerMenu.broadcastChanges();
        }
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
                .map(inv -> inv.getStacksHandler(RamessesCurseMod.CURSE_SLOT_ID)
                        .map(handler -> containsSeal(handler.getStacks()))
                        .orElse(false))
                .orElse(false);
    }

    private static boolean sanitizeSealCurios(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .resolve()
                .map(inv -> {
                    boolean validSeal = false;

                    if (inv.getStacksHandler(RamessesCurseMod.CURSE_SLOT_ID).isPresent()) {
                        var curseHandler = inv.getStacksHandler(RamessesCurseMod.CURSE_SLOT_ID).get().getStacks();
                        for (int i = 0; i < curseHandler.getSlots(); i++) {
                            ItemStack stack = curseHandler.getStackInSlot(i);
                            if (!stack.is(ModItems.SEAL_OF_KADESH.get())) {
                                continue;
                            }
                            if (!validSeal) {
                                validSeal = true;
                            } else {
                                ejectCurioStack(player, curseHandler, i);
                            }
                        }
                    }

                    inv.getCurios().forEach((slotId, handler) -> {
                        if (RamessesCurseMod.CURSE_SLOT_ID.equals(slotId)) {
                            return;
                        }
                        top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler stacks = handler.getStacks();
                        for (int i = 0; i < stacks.getSlots(); i++) {
                            if (stacks.getStackInSlot(i).is(ModItems.SEAL_OF_KADESH.get())) {
                                ejectCurioStack(player, stacks, i);
                            }
                        }
                    });

                    return validSeal;
                })
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

            ejectCurioStack(player, stacks, i);
        }
    }

    private static boolean containsSeal(top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler stacks) {
        for (int i = 0; i < stacks.getSlots(); i++) {
            if (stacks.getStackInSlot(i).is(ModItems.SEAL_OF_KADESH.get())) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsMusicRelic(top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler stacks) {
        for (int i = 0; i < stacks.getSlots(); i++) {
            if (stacks.getStackInSlot(i).is(ModItems.RESONANT_SCARAB.get())) {
                return true;
            }
        }
        return false;
    }

    private static void tickEdictSeal(LivingEntity target) {
        CompoundTag tag = target.getPersistentData();
        long sealUntil = tag.getLong(EDICT_SEAL_UNTIL);
        if (sealUntil <= 0L) {
            return;
        }

        long gameTime = target.level().getGameTime();

        long slamAt = tag.getLong(EDICT_SLAM_AT);
        long liftUntil = tag.getLong(EDICT_LIFT_UNTIL);

        if (!tag.getBoolean(EDICT_SLAMMED)) {
            if (gameTime < liftUntil) {
                target.fallDistance = 0.0F;
                target.hasImpulse = true;
                target.setNoGravity(false);
                if (target.getDeltaMovement().y < 0.18D) {
                    target.setDeltaMovement(target.getDeltaMovement().x * 0.12D, 0.56D, target.getDeltaMovement().z * 0.12D);
                }
            } else if (gameTime < slamAt) {
                if (target instanceof Mob mob) {
                    mob.setNoAi(true);
                }
                target.setNoGravity(true);
                target.fallDistance = 0.0F;
                target.setDeltaMovement(Vec3.ZERO);
                if (target.level() instanceof ServerLevel serverLevel) {
                    ServerPlayer caster = getEdictCaster(serverLevel, tag);
                    if (caster != null) {
                        pullEdictTargetTowardSealCenter(serverLevel, target, tag, gameTime);
                        if (!tag.getBoolean(EDICT_SPELL_BURST_DONE)) {
                            tag.putBoolean(EDICT_SPELL_BURST_DONE, true);
                            triggerEdictSpellBurst(serverLevel, caster, tag);
                        }
                        long nextAirDamageAt = tag.getLong(EDICT_AIR_TICK_DAMAGE_AT);
                        if (nextAirDamageAt <= 0L || gameTime >= nextAirDamageAt) {
                            target.hurt(caster.damageSources().magic(), EDICT_SLAM_DAMAGE);
                            tag.putLong(EDICT_AIR_TICK_DAMAGE_AT, gameTime + EDICT_AIR_DAMAGE_INTERVAL);
                        }
                    }
                }
            } else {
                tag.putBoolean(EDICT_SLAMMED, true);
                if (target instanceof Mob mob) {
                    mob.setNoAi(false);
                }
                target.setNoGravity(false);
                target.hasImpulse = true;
                target.setDeltaMovement(0.0D, -3.4D, 0.0D);
                target.hurtMarked = true;
            }
        } else {
            if (target instanceof Mob mob) {
                mob.setNoAi(false);
            }
            target.setNoGravity(false);
            target.hasImpulse = true;
            target.fallDistance = 0.0F;
            if (target.getDeltaMovement().y > -3.4D) {
                target.setDeltaMovement(target.getDeltaMovement().x * 0.08D, -3.4D, target.getDeltaMovement().z * 0.08D);
            }
            target.hurtMarked = true;

            if (hasEdictImpacted(target)) {
                if (target.level() instanceof ServerLevel serverLevel) {
                    Vec3 impactPos = getEdictImpactPosition(target);
                    strikeEdictLightning(serverLevel, target, impactPos);
                    spawnEdictImpactBurst(serverLevel, impactPos);
                }
                clearEdictSeal(target);
                return;
            }
        }

        if (!tag.getBoolean(EDICT_SLAMMED) && gameTime >= liftUntil && gameTime < slamAt && target.level() instanceof ServerLevel serverLevel) {
            spawnEdictSealSigil(serverLevel, target, tag, gameTime);
        }
    }

    private static void tickEdictDomain(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        long domainUntil = data.getLong(EDICT_DOMAIN_UNTIL);
        long gameTime = player.level().getGameTime();
        if (domainUntil <= gameTime) {
            if (domainUntil > 0L) {
                detonateEdictDomain(player.serverLevel(), player, data);
                clearExpiredEdictDomainTargets(player.serverLevel(), player);
                data.remove(EDICT_DOMAIN_UNTIL);
                data.remove(EDICT_DOMAIN_CENTER_X);
                data.remove(EDICT_DOMAIN_CENTER_Y);
                data.remove(EDICT_DOMAIN_CENTER_Z);
            }
            return;
        }

        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        double centerX = data.getDouble(EDICT_DOMAIN_CENTER_X);
        double centerY = data.getDouble(EDICT_DOMAIN_CENTER_Y);
        double centerZ = data.getDouble(EDICT_DOMAIN_CENTER_Z);
        float progress = Mth.clamp((EDICT_DOMAIN_DURATION - Math.max(0L, domainUntil - gameTime)) / (float) EDICT_DOMAIN_DURATION, 0.0F, 1.0F);

        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(
                        centerX - EDICT_DOMAIN_RANGE,
                        centerY - 3.0D,
                        centerZ - EDICT_DOMAIN_RANGE,
                        centerX + EDICT_DOMAIN_RANGE,
                        centerY + 6.0D,
                        centerZ + EDICT_DOMAIN_RANGE),
                entity -> entity instanceof Enemy && entity.isAlive() && entity != player);

        for (LivingEntity target : targets) {
            applyEdictDomainToTarget(player, target, centerX, centerY, centerZ, progress);
        }

        spawnEdictDomainMatrix(serverLevel, data, gameTime, domainUntil);
    }

    private static void clearExpiredEdictDomainTargets(ServerLevel level, ServerPlayer caster) {
        CompoundTag data = caster.getPersistentData();
        double centerX = data.getDouble(EDICT_DOMAIN_CENTER_X);
        double centerY = data.getDouble(EDICT_DOMAIN_CENTER_Y);
        double centerZ = data.getDouble(EDICT_DOMAIN_CENTER_Z);
        List<Mob> affected = level.getEntitiesOfClass(
                Mob.class,
                new AABB(
                        centerX - EDICT_DOMAIN_RANGE - 4.0D,
                        centerY - 4.0D,
                        centerZ - EDICT_DOMAIN_RANGE - 4.0D,
                        centerX + EDICT_DOMAIN_RANGE + 4.0D,
                        centerY + 8.0D,
                        centerZ + EDICT_DOMAIN_RANGE + 4.0D),
                entity -> entity.isAlive() && entity.getPersistentData().contains(EDICT_DOMAIN_HIT));
        for (Mob mob : affected) {
            clearEdictDomainTarget(mob);
        }
    }

    private static void applyEdictDomainToTarget(ServerPlayer caster, LivingEntity target, double centerX, double centerY, double centerZ, float progress) {
        CompoundTag tag = target.getPersistentData();
        if (target instanceof Mob mob) {
            if (!tag.contains(EDICT_ORIGINAL_NO_AI)) {
                tag.putBoolean(EDICT_ORIGINAL_NO_AI, mob.isNoAi());
            }
            mob.setNoAi(true);
        }

        if (!tag.getBoolean(EDICT_DOMAIN_HIT)) {
            tag.putBoolean(EDICT_DOMAIN_HIT, true);
        }

        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 10, 0, false, false, true));
        Vec3 center = new Vec3(centerX, centerY + target.getBbHeight() * 0.25D, centerZ);
        Vec3 toCenter = center.subtract(target.position());
        Vec3 current = target.getDeltaMovement().scale(0.35D);
        Vec3 horizontalPull = new Vec3(toCenter.x, 0.0D, toCenter.z);
        double distance = horizontalPull.length();
        double tightening = EDICT_DOMAIN_PULL_STRENGTH + progress * 0.1D;
        Vec3 pull = distance > 0.001D
                ? horizontalPull.normalize().scale(Math.min(0.42D, tightening + distance * 0.018D))
                : Vec3.ZERO;
        double verticalAdjust = Mth.clamp(toCenter.y * 0.085D, -0.08D, 0.1D);
        target.setDeltaMovement(pull.x + current.x * 0.15D, verticalAdjust, pull.z + current.z * 0.15D);
        if (distance > 0.2D && distance < 1.6D) {
            double step = Math.min(0.3D, distance * (0.08D + progress * 0.06D));
            Vec3 snap = horizontalPull.normalize().scale(step);
            target.setPos(target.getX() + snap.x, target.getY(), target.getZ() + snap.z);
        }
        target.fallDistance = 0.0F;
        target.hasImpulse = true;
        target.hurtMarked = true;
    }

    private static void spawnEdictDomainMatrix(ServerLevel level, CompoundTag data, long gameTime, long domainUntil) {
        if (gameTime % 2L != 0L) {
            return;
        }

        double radius = EDICT_DOMAIN_RANGE;
        double centerX = data.getDouble(EDICT_DOMAIN_CENTER_X);
        double centerY = data.getDouble(EDICT_DOMAIN_CENTER_Y);
        double centerZ = data.getDouble(EDICT_DOMAIN_CENTER_Z);
        long elapsedTicks = Math.max(0L, gameTime - (domainUntil - EDICT_DOMAIN_DURATION));
        long remainingTicks = Math.max(0L, domainUntil - gameTime);
        float progress = Mth.clamp(elapsedTicks / (float) EDICT_DOMAIN_DURATION, 0.0F, 1.0F);
        float collapse = Mth.clamp(remainingTicks / 40.0F, 0.0F, 1.0F);
        boolean finalPhase = remainingTicks <= 40L;

        int ringPoints = finalPhase ? 96 : 64;
        int visiblePoints = finalPhase
                ? ringPoints
                : Mth.clamp((int) (ringPoints * Math.max(0.18F, progress * 1.2F)), 12, ringPoints);
        double spin = finalPhase ? gameTime * 0.32D : gameTime * 0.08D;
        double ringRadius = radius * (finalPhase ? (0.72D + collapse * 0.28D) : 1.0D);
        float outerSize = finalPhase ? 1.25F : 1.0F;
        Vector3f outerColor = finalPhase
                ? new Vector3f(0.55F, 0.82F, 1.0F)
                : new Vector3f(0.22F, 0.68F, 1.0F);

        for (int i = 0; i < visiblePoints; i++) {
            double angle = spin + (Math.PI * 2.0D * i) / ringPoints;
            double x = centerX + Math.cos(angle) * ringRadius;
            double z = centerZ + Math.sin(angle) * ringRadius;
            level.sendParticles(
                    new DustParticleOptions(outerColor, outerSize),
                    x,
                    centerY + 0.15D,
                    z,
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D);
        }

        Vec3[] points = new Vec3[5];
        double starRadius = ringRadius * 0.72D;
        double starSpin = -Math.PI / 2.0D + (finalPhase ? gameTime * 0.21D : gameTime * 0.03D);
        for (int i = 0; i < points.length; i++) {
            double angle = starSpin + (Math.PI * 2.0D * i) / points.length;
            points[i] = new Vec3(
                    centerX + Math.cos(angle) * starRadius,
                    centerY,
                    centerZ + Math.sin(angle) * starRadius);
        }

        int starRevealSteps = finalPhase
                ? 22
                : Mth.clamp((int) (4 + progress * 18.0F), 4, 18);
        for (int i = 0; i < points.length; i++) {
            drawEdictGroundLine(level, points[i], points[(i + 2) % points.length], starRevealSteps, finalPhase ? 1.0F : 0.75F);
        }
        for (int i = 0; i < points.length; i++) {
            drawEdictGroundLine(level, points[i], points[(i + 1) % points.length], Math.max(4, starRevealSteps / 2), finalPhase ? 0.85F : 0.6F);
        }

        if (finalPhase) {
            int spokeCount = 10;
            int activeSpokes = 3 + (int) ((1.0F - collapse) * 5.0F);
            double spokeSpin = gameTime * 0.14D;
            Vec3 center = new Vec3(centerX, centerY, centerZ);
            for (int i = 0; i < activeSpokes; i++) {
                int spokeIndex = (int) ((gameTime / 2L + i * 2L) % spokeCount);
                double angle = spokeSpin + (Math.PI * 2.0D * spokeIndex) / spokeCount;
                Vec3 spokeEnd = new Vec3(
                        centerX + Math.cos(angle) * ringRadius * 0.96D,
                        centerY,
                        centerZ + Math.sin(angle) * ringRadius * 0.96D);
                drawEdictGroundLine(level, center, spokeEnd, 10, 0.95F + (1.0F - collapse) * 0.45F);
            }
        }

        if (finalPhase && gameTime % 4L == 0L) {
            level.sendParticles(
                    new DustParticleOptions(new Vector3f(0.75F, 0.9F, 1.0F), 1.55F),
                    centerX,
                    centerY + 0.18D,
                    centerZ,
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D);
        }

        spawnEdictDomainSkyPressure(level, centerX, centerY, centerZ, ringRadius, progress, collapse, finalPhase, gameTime);
    }

    private static void drawEdictGroundLine(ServerLevel level, Vec3 start, Vec3 end, int steps, float size) {
        Vec3 delta = end.subtract(start);
        for (int i = 0; i <= steps; i++) {
            Vec3 pos = start.add(delta.scale(i / (double) steps));
            level.sendParticles(
                    new DustParticleOptions(new Vector3f(0.22F, 0.68F, 1.0F), size),
                    pos.x,
                    pos.y,
                    pos.z,
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D);
        }
    }

    private static void spawnEdictDomainSkyPressure(ServerLevel level, double centerX, double centerY, double centerZ, double radius, float progress, float collapse, boolean finalPhase, long gameTime) {
        if (gameTime % 4L != 0L) {
            return;
        }

        int columnCount = finalPhase ? 18 : 10;
        double height = finalPhase ? 8.5D : 6.0D;
        float alpha = finalPhase ? 1.15F : 0.8F;
        Vector3f color = finalPhase
                ? new Vector3f(0.75F, 0.88F, 1.0F)
                : new Vector3f(0.36F, 0.7F, 1.0F);

        for (int i = 0; i < columnCount; i++) {
            double angle = (Math.PI * 2.0D * i) / columnCount + gameTime * (finalPhase ? 0.04D : 0.015D);
            double x = centerX + Math.cos(angle) * radius * 0.9D;
            double z = centerZ + Math.sin(angle) * radius * 0.9D;
            level.sendParticles(
                    new DustParticleOptions(color, alpha),
                    x,
                    centerY + height,
                    z,
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D);
            level.sendParticles(
                    ParticleTypes.END_ROD,
                    x,
                    centerY + height - 0.35D,
                    z,
                    finalPhase ? 2 : 1,
                    0.03D,
                    0.45D,
                    0.03D,
                    -0.06D - progress * 0.04D);
        }

        if (finalPhase && gameTime % 8L == 0L) {
            int pulseCount = 24;
            double pulseRadius = radius * (0.65D + collapse * 0.25D);
            for (int i = 0; i < pulseCount; i++) {
                double angle = (Math.PI * 2.0D * i) / pulseCount;
                double x = centerX + Math.cos(angle) * pulseRadius;
                double z = centerZ + Math.sin(angle) * pulseRadius;
                level.sendParticles(
                        ParticleTypes.ELECTRIC_SPARK,
                        x,
                        centerY + 1.2D,
                        z,
                        1,
                        0.05D,
                        0.15D,
                        0.05D,
                        0.01D);
            }
            float pitch = 0.55F + (1.0F - collapse) * 0.35F;
            level.playSound(null, BlockPos.containing(centerX, centerY, centerZ), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.45F, pitch);
        }
    }

    private static void detonateEdictDomain(ServerLevel level, ServerPlayer caster, CompoundTag data) {
        double centerX = data.getDouble(EDICT_DOMAIN_CENTER_X);
        double centerY = data.getDouble(EDICT_DOMAIN_CENTER_Y);
        double centerZ = data.getDouble(EDICT_DOMAIN_CENTER_Z);
        Vec3 center = new Vec3(centerX, centerY, centerZ);
        AABB bounds = new AABB(
                centerX - EDICT_DOMAIN_RANGE,
                centerY - 4.0D,
                centerZ - EDICT_DOMAIN_RANGE,
                centerX + EDICT_DOMAIN_RANGE,
                centerY + 8.0D,
                centerZ + EDICT_DOMAIN_RANGE);
        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                bounds,
                entity -> entity instanceof Enemy && entity.isAlive() && entity.distanceToSqr(center) <= EDICT_DOMAIN_RANGE * EDICT_DOMAIN_RANGE);

        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, centerX, centerY + 0.4D, centerZ, 6, 1.2D, 0.3D, 1.2D, 0.0D);
        level.sendParticles(
                new DustParticleOptions(new Vector3f(0.72F, 0.88F, 1.0F), 2.6F),
                centerX,
                centerY + 0.1D,
                centerZ,
                72,
                2.4D,
                0.2D,
                2.4D,
                0.01D);
        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                centerX,
                centerY + 0.3D,
                centerZ,
                120,
                3.0D,
                0.45D,
                3.0D,
                0.16D);
        level.playSound(null, BlockPos.containing(centerX, centerY, centerZ), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.35F, 0.72F);
        level.playSound(null, BlockPos.containing(centerX, centerY, centerZ), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.0F, 0.86F);

        for (LivingEntity target : targets) {
            Vec3 impact = target.position().lerp(center, 0.45D);
            level.sendParticles(
                    ParticleTypes.END_ROD,
                    impact.x,
                    target.getY() + target.getBbHeight() * 0.5D,
                    impact.z,
                    14,
                    0.08D,
                    0.18D,
                    0.08D,
                    0.02D);
            DamageSource source = caster.damageSources().genericKill();
            target.hurt(source, target.getMaxHealth());
            target.invulnerableTime = 0;
            if (target.isAlive()) {
                target.hurt(source, Math.max(target.getHealth(), 1.0F));
            }
        }
    }

    private static void clearEdictDomainTarget(LivingEntity target) {
        CompoundTag tag = target.getPersistentData();
        if (target instanceof Mob mob && tag.contains(EDICT_ORIGINAL_NO_AI)) {
            mob.setNoAi(tag.getBoolean(EDICT_ORIGINAL_NO_AI));
        }
        tag.remove(EDICT_DOMAIN_HIT);
        if (tag.contains(EDICT_ORIGINAL_NO_AI) && !tag.contains(EDICT_SEAL_UNTIL)) {
            tag.remove(EDICT_ORIGINAL_NO_AI);
        }
    }

    private static boolean isInsideEdictDomain(CompoundTag domainData, Vec3 position) {
        if (!domainData.contains(EDICT_DOMAIN_UNTIL)) {
            return false;
        }
        double centerX = domainData.getDouble(EDICT_DOMAIN_CENTER_X);
        double centerZ = domainData.getDouble(EDICT_DOMAIN_CENTER_Z);
        return position.distanceToSqr(centerX, position.y, centerZ) <= EDICT_DOMAIN_RANGE * EDICT_DOMAIN_RANGE;
    }

    private static boolean isEdictDomainActive(ServerPlayer player) {
        return player.getPersistentData().getLong(EDICT_DOMAIN_UNTIL) > player.level().getGameTime();
    }

    private static boolean hasActiveEdictSeal(ServerPlayer player) {
        return !player.serverLevel().getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(EDICT_RANGE + 16.0D),
                entity -> entity.isAlive()
                        && entity.getPersistentData().hasUUID(EDICT_CASTER_UUID)
                        && player.getUUID().equals(entity.getPersistentData().getUUID(EDICT_CASTER_UUID))
                        && entity.getPersistentData().getLong(EDICT_SEAL_UNTIL) > player.level().getGameTime())
                .isEmpty();
    }

    private static void spawnEdictSealSigil(ServerLevel level, LivingEntity anchor, CompoundTag anchorTag, long gameTime) {
        if (gameTime % 4L != 0L) {
            return;
        }

        List<LivingEntity> sealedTargets = getEdictCastTargets(level, anchor, anchorTag, gameTime);
        if (sealedTargets.isEmpty() || sealedTargets.stream().mapToInt(Entity::getId).min().orElse(anchor.getId()) != anchor.getId()) {
            return;
        }

        AABB bounds = getEdictSealBounds(sealedTargets);
        double centerX = (bounds.minX + bounds.maxX) * 0.5D;
        double centerZ = (bounds.minZ + bounds.maxZ) * 0.5D;
        double y = bounds.maxY + 0.85D;
        double radius = Math.max(1.8D, Math.sqrt(Mth.square((float) (bounds.maxX - bounds.minX)) + Mth.square((float) (bounds.maxZ - bounds.minZ))) * 0.5D + 1.15D);
        int ringPoints = Mth.clamp(Mth.ceil((float) (radius * 18.0D)), 48, 112);
        int starSteps = Mth.clamp(Mth.ceil((float) (radius * 3.2D)), 10, 26);
        long liftUntil = sealedTargets.stream()
                .mapToLong(entity -> entity.getPersistentData().getLong(EDICT_LIFT_UNTIL))
                .min()
                .orElse(gameTime);
        int elapsedTicks = Math.max(0, (int) (gameTime - liftUntil));
        int ringReveal = Mth.clamp(1 + elapsedTicks * 5, 1, ringPoints);
        int starRevealSteps = Mth.clamp(1 + elapsedTicks, 1, starSteps);

        for (int i = 0; i < ringReveal; i++) {
            double angle = (Math.PI * 2.0D * i) / ringPoints;
            spawnEdictOuterSigilParticle(level, centerX + Math.cos(angle) * radius, y, centerZ + Math.sin(angle) * radius);
        }

        Vec3[] points = new Vec3[5];
        for (int i = 0; i < points.length; i++) {
            double angle = -Math.PI / 2.0D + (Math.PI * 2.0D * i) / points.length;
            points[i] = new Vec3(centerX + Math.cos(angle) * radius * 0.78D, y + 0.02D, centerZ + Math.sin(angle) * radius * 0.78D);
        }

        for (int i = 0; i < points.length; i++) {
            drawEdictBlueFlameLine(level, new Vec3(centerX, y + 0.02D, centerZ), points[i], starRevealSteps);
        }
        for (int i = 0; i < points.length; i++) {
            drawEdictBlueFlameLine(level, points[i], points[(i + 2) % points.length], starRevealSteps);
        }
        if (starRevealSteps >= starSteps) {
            for (int i = 0; i < points.length; i++) {
                drawEdictBlueFlameLine(level, points[i], points[(i + 1) % points.length], Math.max(5, starSteps / 2));
            }
        }

        spawnEdictBlueFlame(level, centerX, y + 0.08D, centerZ);
    }

    private static void pullEdictTargetTowardSealCenter(ServerLevel level, LivingEntity target, CompoundTag anchorTag, long gameTime) {
        List<LivingEntity> sealedTargets = getEdictCastTargets(level, target, anchorTag, gameTime);
        if (sealedTargets.size() <= 1) {
            return;
        }

        AABB bounds = getEdictSealBounds(sealedTargets);
        double centerX = (bounds.minX + bounds.maxX) * 0.5D;
        double centerZ = (bounds.minZ + bounds.maxZ) * 0.5D;
        Vec3 toCenter = new Vec3(centerX - target.getX(), 0.0D, centerZ - target.getZ());
        double horizontalDistance = toCenter.horizontalDistance();
        if (horizontalDistance < 0.18D) {
            return;
        }

        Vec3 pull = toCenter.normalize().scale(Math.min(0.12D, horizontalDistance * 0.14D));
        target.setDeltaMovement(pull.x, 0.0D, pull.z);
        target.hasImpulse = true;
        target.hurtMarked = true;
    }

    private static AABB getEdictSealBounds(List<LivingEntity> sealedTargets) {
        AABB bounds = sealedTargets.get(0).getBoundingBox();
        for (int i = 1; i < sealedTargets.size(); i++) {
            bounds = bounds.minmax(sealedTargets.get(i).getBoundingBox());
        }
        return bounds;
    }

    private static List<LivingEntity> getEdictCastTargets(ServerLevel level, LivingEntity anchor, CompoundTag anchorTag, long gameTime) {
        if (!anchorTag.hasUUID(EDICT_CAST_ID)) {
            return List.of(anchor);
        }

        UUID castId = anchorTag.getUUID(EDICT_CAST_ID);
        return level.getEntitiesOfClass(
                LivingEntity.class,
                anchor.getBoundingBox().inflate(EDICT_RANGE * 2.0D + 4.0D),
                entity -> entity.isAlive()
                        && entity.getPersistentData().hasUUID(EDICT_CAST_ID)
                        && castId.equals(entity.getPersistentData().getUUID(EDICT_CAST_ID))
                        && !entity.getPersistentData().getBoolean(EDICT_SLAMMED)
                        && entity.getPersistentData().getLong(EDICT_SEAL_UNTIL) > gameTime
                        && entity.getPersistentData().getLong(EDICT_LIFT_UNTIL) <= gameTime
                        && entity.getPersistentData().getLong(EDICT_SLAM_AT) > gameTime);
    }

    private static void drawEdictBlueFlameLine(ServerLevel level, Vec3 start, Vec3 end, int steps) {
        Vec3 delta = end.subtract(start);
        for (int i = 0; i <= steps; i++) {
            Vec3 pos = start.add(delta.scale(i / (double) steps));
            spawnEdictBlueFlame(level, pos.x, pos.y, pos.z);
        }
    }

    private static void spawnEdictBlueFlame(ServerLevel level, double x, double y, double z) {
        level.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                x,
                y,
                z,
                1,
                0.0D,
                0.0D,
                0.0D,
                0.0D);
    }

    private static void spawnEdictOuterSigilParticle(ServerLevel level, double x, double y, double z) {
        level.sendParticles(
                new DustParticleOptions(new Vector3f(0.12F, 0.55F, 1.0F), 0.9F),
                x,
                y,
                z,
                1,
                0.0D,
                0.0D,
                0.0D,
                0.0D);
    }

    private static void clearEdictSeal(LivingEntity target) {
        CompoundTag tag = target.getPersistentData();
        if (target instanceof Mob mob) {
            mob.setNoAi(tag.getBoolean(EDICT_ORIGINAL_NO_AI));
        }
        target.setNoGravity(tag.getBoolean(EDICT_ORIGINAL_NO_GRAVITY));
        tag.remove(EDICT_SEAL_UNTIL);
        tag.remove(EDICT_LIFT_UNTIL);
        tag.remove(EDICT_SLAM_AT);
        tag.remove(EDICT_SLAMMED);
        tag.remove(EDICT_SPELL_BURST_DONE);
        tag.remove(EDICT_AIR_TICK_DAMAGE_AT);
        tag.remove(EDICT_CASTER_UUID);
        tag.remove(EDICT_CAST_ID);
        tag.remove(EDICT_ORIGINAL_NO_AI);
        tag.remove(EDICT_ORIGINAL_NO_GRAVITY);
    }

    private static boolean isEdictSealed(Entity entity) {
        return entity instanceof LivingEntity living
                && living.getPersistentData().getLong(EDICT_SEAL_UNTIL) > living.level().getGameTime();
    }

    private static boolean hasEdictImpacted(LivingEntity target) {
        return target.onGround() || target.isInWaterOrBubble() || target.verticalCollisionBelow;
    }

    private static Vec3 getEdictImpactPosition(LivingEntity target) {
        BlockPos impactBlockPos = BlockPos.containing(target.getX(), target.getBoundingBox().minY - 0.05D, target.getZ());
        if (target.level().getBlockState(impactBlockPos).isAir()) {
            impactBlockPos = target.blockPosition().below();
        }
        return new Vec3(
                impactBlockPos.getX() + 0.5D,
                Math.max(target.level().getMinBuildHeight(), impactBlockPos.getY() + 0.05D),
                impactBlockPos.getZ() + 0.5D);
    }

    private static void spawnEdictDebris(ServerLevel level, LivingEntity target) {
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SANDSTONE.defaultBlockState()),
                target.getX(),
                target.getY() + 0.2D,
                target.getZ(),
                28,
                0.55D,
                0.25D,
                0.55D,
                0.08D);
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SMOOTH_SANDSTONE.defaultBlockState()),
                target.getX(),
                target.getY() + 0.4D,
                target.getZ(),
                18,
                0.4D,
                0.3D,
                0.4D,
                0.05D);
    }

    private static void spawnEdictLink(ServerLevel level, Vec3 start, Vec3 end) {
        Vec3 delta = end.subtract(start);
        int points = Math.max(8, Mth.ceil((float) delta.length() * 2.0F));
        for (int i = 0; i <= points; i++) {
            double progress = i / (double) points;
            Vec3 pos = start.add(delta.scale(progress));
            level.sendParticles(
                new DustParticleOptions(new Vector3f(0.98F, 0.83F, 0.18F), 1.0F),
                    pos.x,
                    pos.y,
                    pos.z,
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D);
        }
    }

    private static void triggerEdictSpellBurst(ServerLevel level, ServerPlayer caster, CompoundTag tag) {
        if (!tag.hasUUID(EDICT_CAST_ID)) {
            return;
        }

        UUID castId = tag.getUUID(EDICT_CAST_ID);
        List<LivingEntity> chainedTargets = level.getEntitiesOfClass(
                LivingEntity.class,
                caster.getBoundingBox().inflate(EDICT_RANGE + 8),
                entity -> entity instanceof Enemy
                        && entity.getPersistentData().hasUUID(EDICT_CAST_ID)
                        && castId.equals(entity.getPersistentData().getUUID(EDICT_CAST_ID))
                        && entity.getPersistentData().getLong(EDICT_SEAL_UNTIL) > level.getGameTime());

        if (chainedTargets.isEmpty()) {
            return;
        }

        chainedTargets.sort((left, right) -> Double.compare(
                left.distanceToSqr(caster),
                right.distanceToSqr(caster)));

        Vec3 previous = caster.position().add(0.0D, caster.getBbHeight() * 0.75D, 0.0D);
        for (LivingEntity chainedTarget : chainedTargets) {
            Vec3 current = chainedTarget.position().add(0.0D, chainedTarget.getBbHeight() * 0.5D, 0.0D);
            spawnEdictLink(level, previous, current);
            pullEdictTargetTowardChain(chainedTarget, previous, current);
            chainedTarget.hurt(caster.damageSources().magic(), EDICT_SPELL_DAMAGE);
            level.sendParticles(
                    new DustParticleOptions(new Vector3f(1.0F, 0.86F, 0.22F), 1.0F),
                    current.x,
                    current.y,
                    current.z,
                    10,
                    0.22D,
                    0.22D,
                    0.22D,
                    0.02D);
            previous = current;
        }
    }

    private static void pullEdictTargetTowardChain(LivingEntity target, Vec3 previous, Vec3 current) {
        Vec3 drag = current.subtract(previous);
        if (drag.lengthSqr() <= 1.0E-4D) {
            return;
        }

        Vec3 normalized = drag.normalize();
        target.setDeltaMovement(
                normalized.x * EDICT_CHAIN_PULL_STRENGTH,
                Math.max(target.getDeltaMovement().y, 0.22D),
                normalized.z * EDICT_CHAIN_PULL_STRENGTH);
        target.hasImpulse = true;
        target.hurtMarked = true;
    }

    private static void strikeEdictLightning(ServerLevel level, LivingEntity target, Vec3 impactPos) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(impactPos.x, impactPos.y, impactPos.z);
            level.addFreshEntity(bolt);
        }
        target.thunderHit(level, bolt);
    }

    private static void spawnEdictLaunchBurst(ServerLevel level, Vec3 pos) {
        level.playSound(null, BlockPos.containing(pos), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 0.45F, 1.35F);
        level.sendParticles(ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(
                new DustParticleOptions(new Vector3f(1.0F, 0.84F, 0.18F), 1.0F),
                pos.x,
                pos.y,
                pos.z,
                14,
                0.28D,
                0.12D,
                0.28D,
                0.03D);
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SANDSTONE.defaultBlockState()),
                pos.x,
                pos.y,
                pos.z,
                20,
                0.45D,
                0.1D,
                0.45D,
                0.08D);
    }

    private static void spawnEdictImpactBurst(ServerLevel level, Vec3 pos) {
        BlockPos groundPos = BlockPos.containing(pos.x, pos.y, pos.z);
        BlockState groundState = level.getBlockState(groundPos);
        if (groundState.isAir()) {
            BlockPos belowPos = BlockPos.containing(pos.x, pos.y - 0.35D, pos.z);
            BlockState belowState = level.getBlockState(belowPos);
            if (!belowState.isAir()) {
                groundPos = belowPos;
                groundState = belowState;
            } else {
                groundState = Blocks.SANDSTONE.defaultBlockState();
            }
        }

        level.levelEvent(2001, groundPos, net.minecraft.world.level.block.Block.getId(groundState));
        level.sendParticles(ParticleTypes.POOF, pos.x, pos.y + 0.08D, pos.z, 18, 0.6D, 0.05D, 0.6D, 0.04D);
        level.sendParticles(
                new DustParticleOptions(new Vector3f(1.0F, 0.82F, 0.16F), 1.35F),
                pos.x,
                pos.y + 0.1D,
                pos.z,
                24,
                0.45D,
                0.08D,
                0.45D,
                0.05D);
        // Ground shock ring.
        for (int i = 0; i < 16; i++) {
            double angle = (Math.PI * 2.0D * i) / 16.0D;
            double offsetX = Math.cos(angle) * 1.6D;
            double offsetZ = Math.sin(angle) * 1.6D;
            level.sendParticles(
                    ParticleTypes.CLOUD,
                    pos.x + offsetX,
                    pos.y + 0.05D,
                    pos.z + offsetZ,
                    2,
                    0.05D,
                    0.02D,
                    0.05D,
                    0.02D);
        }
        for (int i = 0; i < 20; i++) {
            double angle = (Math.PI * 2.0D * i) / 20.0D;
            double offsetX = Math.cos(angle) * 2.2D;
            double offsetZ = Math.sin(angle) * 2.2D;
            level.sendParticles(
                    ParticleTypes.CLOUD,
                    pos.x + offsetX,
                    pos.y + 0.04D,
                    pos.z + offsetZ,
                    3,
                    0.08D,
                    0.03D,
                    0.08D,
                    0.03D);
        }
        spawnEdictGroundHeave(level, pos, groundPos, groundState);
        // "Blocks lifted by shock" visual.
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, groundState),
                pos.x,
                pos.y + 0.12D,
                pos.z,
                50,
                1.1D,
                0.08D,
                1.1D,
                0.18D);
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, groundState),
                pos.x,
                pos.y + 0.12D,
                pos.z,
                28,
                0.85D,
                0.05D,
                0.85D,
                0.14D);
        level.sendParticles(
                ParticleTypes.CAMPFIRE_COSY_SMOKE,
                pos.x,
                pos.y + 0.08D,
                pos.z,
                8,
                0.45D,
                0.02D,
                0.45D,
                0.01D);
        for (int i = 0; i < 12; i++) {
            double angle = (Math.PI * 2.0D * i) / 12.0D;
            double ringX = Math.cos(angle) * 0.9D;
            double ringZ = Math.sin(angle) * 0.9D;
            level.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, groundState),
                    pos.x + ringX,
                    pos.y + 0.18D,
                    pos.z + ringZ,
                    8,
                    0.08D,
                    0.28D,
                    0.08D,
                    0.2D);
        }
    }

    private static void spawnEdictGroundHeave(ServerLevel level, Vec3 pos, BlockPos centerPos, BlockState defaultState) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                double distance = Math.sqrt(x * x + z * z);
                if (distance < 1.25D || distance > 2.35D) {
                    continue;
                }

                BlockPos samplePos = centerPos.offset(x, 0, z);
                BlockState sampleState = level.getBlockState(samplePos);
                if (sampleState.isAir()) {
                    sampleState = defaultState;
                }
                if (sampleState.isAir() || sampleState.hasBlockEntity()) {
                    continue;
                }

                level.levelEvent(2001, samplePos, net.minecraft.world.level.block.Block.getId(sampleState));
                spawnEdictLiftedBlock(level, samplePos, sampleState, x, z);
                level.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, sampleState),
                        samplePos.getX() + 0.5D,
                        samplePos.getY() + 1.02D,
                        samplePos.getZ() + 0.5D,
                        16,
                        0.12D,
                        0.34D,
                        0.12D,
                        0.18D);
                level.sendParticles(
                        ParticleTypes.CLOUD,
                        samplePos.getX() + 0.5D,
                        samplePos.getY() + 1.0D,
                        samplePos.getZ() + 0.5D,
                        5,
                        0.08D,
                        0.05D,
                        0.08D,
                        0.03D);
            }
        }
    }

    private static void spawnEdictLiftedBlock(ServerLevel level, BlockPos sourcePos, BlockState state, int offsetX, int offsetZ) {
        if (state.isAir() || state.hasBlockEntity()) {
            return;
        }

        FallingBlockEntity liftedBlock = FallingBlockEntity.fall(level, sourcePos, state);
        level.setBlock(sourcePos, state, 3);
        liftedBlock.dropItem = false;
        liftedBlock.disableDrop();
        liftedBlock.time = 1 + level.random.nextInt(3);
        liftedBlock.setNoGravity(false);
        liftedBlock.setInvulnerable(true);
        liftedBlock.setStartPos(sourcePos);
        liftedBlock.setPos(sourcePos.getX() + 0.5D, sourcePos.getY() + 1.03D, sourcePos.getZ() + 0.5D);

        Vec3 outward = new Vec3(offsetX, 0.0D, offsetZ);
        if (outward.lengthSqr() > 0.0D) {
            outward = outward.normalize();
        }
        double lift = 0.36D + level.random.nextDouble() * 0.08D;
        double spread = 0.018D + level.random.nextDouble() * 0.025D;
        liftedBlock.setDeltaMovement(outward.x * spread, lift, outward.z * spread);
        liftedBlock.hasImpulse = true;
        liftedBlock.hurtMarked = true;
    }

    @org.jetbrains.annotations.Nullable
    private static ServerPlayer getEdictCaster(ServerLevel level, CompoundTag tag) {
        return tag.hasUUID(EDICT_CASTER_UUID) ? level.getServer().getPlayerList().getPlayer(tag.getUUID(EDICT_CASTER_UUID)) : null;
    }

    private static void ejectCurioStack(Player player, top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler stacks, int slot) {
        ItemStack stack = stacks.getStackInSlot(slot);
        if (stack.isEmpty()) {
            return;
        }

        ItemStack extracted = stack.copy();
        stacks.setStackInSlot(slot, ItemStack.EMPTY);

        if (!player.getInventory().add(extracted)) {
            player.drop(extracted, false);
        }
    }

    private static Player getLockedAttacker(net.minecraft.world.damagesource.DamageSource source) {
        Player player = getSourcePlayer(source);
        return player != null && isGuiltLocked(player) ? player : null;
    }

    private static void cleanupTrackedMusicPlaybacks(Level level) {
        Map<BlockPos, TrackedMusicPlayback> playbacks = ACTIVE_MUSIC_PLAYBACKS.get(level.dimension());
        if (playbacks == null || playbacks.isEmpty()) {
            return;
        }

        long gameTime = level.getGameTime();
        playbacks.entrySet().removeIf(entry -> gameTime > entry.getValue().endTick || !level.isLoaded(entry.getKey()));
        if (playbacks.isEmpty()) {
            ACTIVE_MUSIC_PLAYBACKS.remove(level.dimension());
        }
    }

    private static Player getSourcePlayer(net.minecraft.world.damagesource.DamageSource source) {
        Entity entity = source.getEntity();
        if (entity instanceof Player player) {
            return player;
        }

        Entity directEntity = source.getDirectEntity();
        if (directEntity instanceof Player player) {
            return player;
        }
        if (directEntity instanceof Projectile projectile && projectile.getOwner() instanceof Player player) {
            return player;
        }

        return null;
    }

    private record TrackedMusicPlayback(long startTick, long endTick) {
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
        crystal.getPersistentData().putUUID(SATIETY_CRYSTAL_ID, crystal.getUUID());
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
        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 220, 4, false, false, true));
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

    private static List<ItemEntity> findOwnedSatietyCrystals(Iterable<ServerLevel> levels, UUID ownerId) {
        List<ItemEntity> results = new ArrayList<>();
        for (ServerLevel level : levels) {
            results.addAll(level.getEntitiesOfClass(
                    ItemEntity.class,
                    new AABB(-30000000, level.getMinBuildHeight() - 64, -30000000, 30000000, level.getMaxBuildHeight() + 64, 30000000),
                    entity -> entity.getItem().is(ModItems.SATIETY_CRYSTAL.get())
                            && entity.getPersistentData().hasUUID(SATIETY_CRYSTAL_OWNER)
                            && ownerId.equals(entity.getPersistentData().getUUID(SATIETY_CRYSTAL_OWNER))));
        }
        return results;
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
        List<SatietyCrystalRecord> records = getRememberedSatietyCrystals(player);
        UUID crystalId = crystal.getPersistentData().hasUUID(SATIETY_CRYSTAL_ID)
                ? crystal.getPersistentData().getUUID(SATIETY_CRYSTAL_ID)
                : crystal.getUUID();
        SatietyCrystalRecord updatedRecord = new SatietyCrystalRecord(crystalId, crystal.level().dimension(), crystal.blockPosition());
        boolean replaced = false;

        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).id().equals(crystalId)) {
                records.set(i, updatedRecord);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            records.add(0, updatedRecord);
        }
        setRememberedSatietyCrystals(player, records);

        CompoundTag data = getCurseData(player);
        data.putString(SATIETY_CRYSTAL_DIMENSION, crystal.level().dimension().location().toString());
        data.putInt(SATIETY_CRYSTAL_X, crystal.blockPosition().getX());
        data.putInt(SATIETY_CRYSTAL_Y, crystal.blockPosition().getY());
        data.putInt(SATIETY_CRYSTAL_Z, crystal.blockPosition().getZ());
    }

    private static void clearRememberedSatietyCrystal(Player player) {
        CompoundTag data = getCurseData(player);
        data.remove(SATIETY_CRYSTAL_RECORDS);
        data.remove(SATIETY_CRYSTAL_DIMENSION);
        data.remove(SATIETY_CRYSTAL_X);
        data.remove(SATIETY_CRYSTAL_Y);
        data.remove(SATIETY_CRYSTAL_Z);
    }

    private static void forgetSatietyCrystal(Player player, ItemEntity crystal) {
        UUID crystalId = crystal.getPersistentData().hasUUID(SATIETY_CRYSTAL_ID)
                ? crystal.getPersistentData().getUUID(SATIETY_CRYSTAL_ID)
                : crystal.getUUID();
        List<SatietyCrystalRecord> records = getRememberedSatietyCrystals(player);
        records.removeIf(record -> record.id().equals(crystalId));

        if (records.isEmpty()) {
            clearRememberedSatietyCrystal(player);
            return;
        }

        setRememberedSatietyCrystals(player, records);
        SatietyCrystalRecord latest = records.get(0);
        CompoundTag data = getCurseData(player);
        data.putString(SATIETY_CRYSTAL_DIMENSION, latest.dimension().location().toString());
        data.putInt(SATIETY_CRYSTAL_X, latest.pos().getX());
        data.putInt(SATIETY_CRYSTAL_Y, latest.pos().getY());
        data.putInt(SATIETY_CRYSTAL_Z, latest.pos().getZ());
    }

    private static List<SatietyCrystalRecord> getRememberedSatietyCrystals(Player player) {
        List<SatietyCrystalRecord> records = new ArrayList<>();
        CompoundTag data = getCurseData(player);

        if (data.contains(SATIETY_CRYSTAL_RECORDS, Tag.TAG_LIST)) {
            ListTag listTag = data.getList(SATIETY_CRYSTAL_RECORDS, Tag.TAG_COMPOUND);
            for (Tag tag : listTag) {
                if (!(tag instanceof CompoundTag entry)) {
                    continue;
                }
                if (!entry.hasUUID(SATIETY_CRYSTAL_ID)
                        || !entry.contains(SATIETY_CRYSTAL_DIMENSION)
                        || !entry.contains(SATIETY_CRYSTAL_X)
                        || !entry.contains(SATIETY_CRYSTAL_Y)
                        || !entry.contains(SATIETY_CRYSTAL_Z)) {
                    continue;
                }
                records.add(new SatietyCrystalRecord(
                        entry.getUUID(SATIETY_CRYSTAL_ID),
                        ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, new ResourceLocation(entry.getString(SATIETY_CRYSTAL_DIMENSION))),
                        new BlockPos(entry.getInt(SATIETY_CRYSTAL_X), entry.getInt(SATIETY_CRYSTAL_Y), entry.getInt(SATIETY_CRYSTAL_Z))));
            }
            return records;
        }

        if (data.contains(SATIETY_CRYSTAL_DIMENSION) && data.contains(SATIETY_CRYSTAL_X) && data.contains(SATIETY_CRYSTAL_Y) && data.contains(SATIETY_CRYSTAL_Z)) {
            records.add(new SatietyCrystalRecord(
                    UUID.nameUUIDFromBytes((data.getString(SATIETY_CRYSTAL_DIMENSION) + ":" + data.getInt(SATIETY_CRYSTAL_X) + ":" + data.getInt(SATIETY_CRYSTAL_Y) + ":" + data.getInt(SATIETY_CRYSTAL_Z)).getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                    ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, new ResourceLocation(data.getString(SATIETY_CRYSTAL_DIMENSION))),
                    new BlockPos(data.getInt(SATIETY_CRYSTAL_X), data.getInt(SATIETY_CRYSTAL_Y), data.getInt(SATIETY_CRYSTAL_Z))));
        }
        return records;
    }

    private static void setRememberedSatietyCrystals(Player player, List<SatietyCrystalRecord> records) {
        CompoundTag data = getCurseData(player);
        ListTag listTag = new ListTag();

        for (SatietyCrystalRecord record : records) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID(SATIETY_CRYSTAL_ID, record.id());
            entry.putString(SATIETY_CRYSTAL_DIMENSION, record.dimension().location().toString());
            entry.putInt(SATIETY_CRYSTAL_X, record.pos().getX());
            entry.putInt(SATIETY_CRYSTAL_Y, record.pos().getY());
            entry.putInt(SATIETY_CRYSTAL_Z, record.pos().getZ());
            listTag.add(entry);
        }

        data.put(SATIETY_CRYSTAL_RECORDS, listTag);
    }

    private static List<SatietyCrystalRecord> getAvailableSatietyCrystals(MinecraftServer server, ServerPlayer target) {
        LinkedHashMap<UUID, SatietyCrystalRecord> records = new LinkedHashMap<>();

        for (SatietyCrystalRecord remembered : getRememberedSatietyCrystals(target)) {
            records.put(remembered.id(), remembered);
        }
        for (ItemEntity crystal : findOwnedSatietyCrystals(server.getAllLevels(), target.getUUID())) {
            rememberSatietyCrystal(target, crystal);
            UUID crystalId = crystal.getPersistentData().hasUUID(SATIETY_CRYSTAL_ID)
                    ? crystal.getPersistentData().getUUID(SATIETY_CRYSTAL_ID)
                    : crystal.getUUID();
            records.put(crystalId, new SatietyCrystalRecord(crystalId, crystal.level().dimension(), crystal.blockPosition()));
        }

        return new ArrayList<>(records.values());
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
        List<SatietyCrystalRecord> records = getAvailableSatietyCrystals(source.getServer(), target);
        if (records.isEmpty()) {
            source.sendFailure(Component.translatable("command.ramesses_curse.locate.none", target.getDisplayName()));
            return 0;
        }

        for (int i = 0; i < records.size(); i++) {
            SatietyCrystalRecord record = records.get(i);
            int displayIndex = i + 1;
            BlockPos crystalPos = record.pos();
            String clickCommand = target == source.getEntity()
                    ? "/ramessescurse locate_tp " + displayIndex
                    : "/ramessescurse locate_tp " + target.getGameProfile().getName() + " " + displayIndex;

            MutableComponent clickable = Component.literal("[" + displayIndex + "] ")
                    .append(Component.translatable(
                            "command.ramesses_curse.locate.found",
                            target.getDisplayName(),
                            crystalPos.getX(),
                            crystalPos.getY(),
                            crystalPos.getZ()))
                    .append(Component.literal(" (" + record.dimension().location() + ")"))
                    .withStyle(style -> style
                            .withColor(0xF6D365)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, clickCommand))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("command.ramesses_curse.locate.hover"))));

            source.sendSuccess(() -> clickable, false);
        }
        return records.size();
    }

    private static int teleportToSatietyCrystal(CommandSourceStack source, ServerPlayer target, int recordIndex) throws CommandSyntaxException {
        List<SatietyCrystalRecord> records = getAvailableSatietyCrystals(source.getServer(), target);
        if (records.isEmpty() || recordIndex < 0 || recordIndex >= records.size()) {
            source.sendFailure(Component.translatable("command.ramesses_curse.locate.none", target.getDisplayName()));
            return 0;
        }

        SatietyCrystalRecord selected = records.get(recordIndex);
        ResourceKey<Level> dimension = selected.dimension();
        BlockPos crystalPos = selected.pos();

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

    private record SatietyCrystalRecord(UUID id, ResourceKey<Level> dimension, BlockPos pos) {
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

    private static void syncGuiltState(Player player, int guilt, boolean sealEquipped, boolean netherAwakened, boolean curseEightReversed, int lockTicks, int satietyCrystalCount, int lockedSlot, int lockedSlotTicks) {
        if (player instanceof ServerPlayer serverPlayer) {
            ModNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new GuiltSyncPacket(guilt, sealEquipped, netherAwakened, curseEightReversed, lockTicks, satietyCrystalCount, lockedSlot, lockedSlotTicks));
        }
    }

    private static void syncClientState(Player player, boolean force) {
        CompoundTag data = getCurseData(player);
        int guilt = data.getInt(GUILT_PROGRESS);
        boolean sealEquipped = isSealEquipped(player);
        boolean netherAwakened = data.getBoolean(NETHER_AWAKENED);
        boolean curseEightReversed = data.getBoolean(CURSE_8_REVERSED);
        int lockSeconds = Mth.ceil(getRemainingGuiltLockTicks(player) / 20.0F);
        int satietyCrystalCount = getSatietyCrystalCount(player);
        int lockedSlot = data.getInt(LOCKED_SLOT);
        int lockedSlotTicks = getRemainingLockedSlotTicks(player);

        if (!force
                && data.getInt(LAST_SYNC_GUILT) == guilt
                && data.getBoolean(LAST_SYNC_SEAL) == sealEquipped
                && data.getBoolean(LAST_SYNC_NETHER_AWAKENED) == netherAwakened
                && data.getBoolean(LAST_SYNC_CURSE_8_REVERSED) == curseEightReversed
                && data.getInt(LAST_SYNC_LOCK_SECONDS) == lockSeconds
                && data.getInt(LAST_SYNC_SATIETY) == satietyCrystalCount
                && data.getInt(LAST_SYNC_LOCKED_SLOT) == lockedSlot
                && data.getInt(LAST_SYNC_LOCKED_SLOT_TICKS) == lockedSlotTicks) {
            return;
        }

        data.putInt(LAST_SYNC_GUILT, guilt);
        data.putBoolean(LAST_SYNC_SEAL, sealEquipped);
        data.putBoolean(LAST_SYNC_NETHER_AWAKENED, netherAwakened);
        data.putBoolean(LAST_SYNC_CURSE_8_REVERSED, curseEightReversed);
        data.putInt(LAST_SYNC_LOCK_SECONDS, lockSeconds);
        data.putInt(LAST_SYNC_SATIETY, satietyCrystalCount);
        data.putInt(LAST_SYNC_LOCKED_SLOT, lockedSlot);
        data.putInt(LAST_SYNC_LOCKED_SLOT_TICKS, lockedSlotTicks);
        syncGuiltState(player, guilt, sealEquipped, netherAwakened, curseEightReversed, lockSeconds * 20, satietyCrystalCount, lockedSlot, lockedSlotTicks);
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

    private static void applyMainHandCooldown(ServerPlayer player, int remainingTicks) {
        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty()) {
            player.getCooldowns().addCooldown(mainHand.getItem(), remainingTicks);
        }
    }

    private static void applyLockedSlotCooldown(ServerPlayer player, int remainingTicks) {
        CompoundTag data = getCurseData(player);
        int slot = data.getInt(LOCKED_SLOT);
        if (slot < 0 || slot >= player.getInventory().items.size()) {
            return;
        }

        ItemStack lockedStack = player.getInventory().items.get(slot);
        if (!lockedStack.isEmpty()) {
            player.getCooldowns().addCooldown(lockedStack.getItem(), remainingTicks);
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
        private enum EdictChargeMode {
            NONE,
            X_CAST,
            Y_DOMAIN
        }

        private static int clientGuilt;
        private static boolean clientSealEquipped;
        private static boolean clientNetherAwakened;
        private static boolean clientCurseEightReversed;
        private static int clientLockTicks;
        private static int clientSatietyCrystalCount;
        private static int clientHeartBattleStacks;
        private static int clientHeartCooldownTicks;
        private static boolean clientHeartWardReady;
        private static int clientLockedSlot = -1;
        private static int clientLockedSlotTicks;
        private static int clientEdictChargeTicks;
        private static boolean clientEdictTriggeredThisHold;
        private static EdictChargeMode clientEdictChargeMode = EdictChargeMode.NONE;
        private static int clientEdictCooldownTicks;
        private static int clientEdictShakeTicks;
        private static int clientEdictShakeTotalTicks;
        private static float clientEdictShakeStrength;
        private static boolean clientEdictCooldownPromptedThisHold;

        public static void updateClientGuilt(int guilt, boolean sealEquipped, boolean netherAwakened, boolean curseEightReversed, int lockTicks, int satietyCrystalCount, int lockedSlot, int lockedSlotTicks) {
            clientGuilt = guilt;
            clientSealEquipped = sealEquipped;
            clientNetherAwakened = netherAwakened;
            clientCurseEightReversed = curseEightReversed;
            clientLockTicks = lockTicks;
            clientSatietyCrystalCount = satietyCrystalCount;
            clientLockedSlot = lockedSlot;
            clientLockedSlotTicks = lockedSlotTicks;
        }

        public static boolean isClientNetherAwakened() {
            return clientNetherAwakened;
        }

        public static boolean isClientCurseEightReversed() {
            return clientCurseEightReversed;
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

        public static void triggerEdictScreenShake(int ticks, float strength) {
            clientEdictShakeTicks = Math.max(clientEdictShakeTicks, ticks);
            clientEdictShakeTotalTicks = Math.max(clientEdictShakeTotalTicks, ticks);
            clientEdictShakeStrength = Math.max(clientEdictShakeStrength, strength);
        }

        public static void cancelEdictCharge() {
            resetEdictCharge();
        }

        public static void updateClientEdictCooldown(int cooldownTicks) {
            clientEdictCooldownTicks = Math.max(0, cooldownTicks);
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

            renderEdictChargeBar(event.getGuiGraphics(), minecraft);
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
            event.getToolTip().add(Component.translatable("hud.ramesses_curse.guilt_lock_no_damage")
                    .withStyle(net.minecraft.ChatFormatting.DARK_GRAY));

        }

        @SubscribeEvent
        public static void onScreenRender(ScreenEvent.Render.Post event) {
            if (event.getScreen() instanceof AbstractContainerScreen<?> screen && clientLockedSlotTicks > 0) {
                screen.getMenu().slots.stream()
                        .filter(slot -> slot.container instanceof Inventory && slot.getContainerSlot() == clientLockedSlot)
                        .findFirst()
                        .ifPresent(slot -> renderLockedSlot(event, screen, slot.x, slot.y));
            }

            renderCurseThreeCompatSuffix(event);
            renderCurseThreeGenericMarker(event);
            renderCurseThreeHoverTooltip(event);
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
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }

            if (clientEdictCooldownTicks > 0) {
                clientEdictCooldownTicks--;
            }

            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft.player;
            if (player == null || minecraft.isPaused() || minecraft.screen != null) {
                resetEdictCharge();
                return;
            }

            if (!ModItems.isEdictOfTwoLandsEquipped(player)) {
                resetEdictCharge();
                return;
            }

            boolean xDown = RamessesCurseMod.EDICT_OF_TWO_LANDS_KEY.isDown();
            boolean yDown = RamessesCurseMod.EDICT_OF_TWO_LANDS_DOMAIN_KEY.isDown();

            if (xDown && yDown) {
                if (!clientEdictCooldownPromptedThisHold) {
                    player.displayClientMessage(
                            Component.translatable("message.ramesses_curse.edict_other_mode_active").withStyle(net.minecraft.ChatFormatting.RED),
                            true);
                    clientEdictCooldownPromptedThisHold = true;
                }
                resetEdictCharge();
                return;
            }

            if (!xDown && !yDown) {
                resetEdictCharge();
                return;
            }

            EdictChargeMode intendedMode = yDown ? EdictChargeMode.Y_DOMAIN : EdictChargeMode.X_CAST;
            int intendedCooldownTicks = intendedMode == EdictChargeMode.Y_DOMAIN
                    ? ModItems.getEdictOfTwoLandsDomainCooldownTicks(player)
                    : ModItems.getEdictOfTwoLandsCooldownTicks(player);
            intendedCooldownTicks = Math.max(intendedCooldownTicks, clientEdictCooldownTicks);
            if (player.getCooldowns().isOnCooldown(ModItems.EDICT_OF_TWO_LANDS.get())) {
                intendedCooldownTicks = Math.max(intendedCooldownTicks, 1);
            }
            if (intendedCooldownTicks > 0) {
                if (!clientEdictCooldownPromptedThisHold) {
                    float cooldownSeconds = Math.max(0.1F, intendedCooldownTicks / 20.0F);
                    MutableComponent message = Component.translatable(
                            "message.ramesses_curse.edict_other_skill_cooling",
                            intendedMode == EdictChargeMode.Y_DOMAIN ? "X" : "Y",
                            String.format(Locale.ROOT, "%.1f", cooldownSeconds));
                    player.displayClientMessage(message.withStyle(net.minecraft.ChatFormatting.RED), true);
                    clientEdictCooldownPromptedThisHold = true;
                }
                clientEdictChargeTicks = 0;
                clientEdictTriggeredThisHold = false;
                clientEdictChargeMode = EdictChargeMode.NONE;
                return;
            }

            if (clientEdictChargeMode == EdictChargeMode.NONE) {
                clientEdictChargeMode = intendedMode;
            }

            if (clientEdictChargeMode == EdictChargeMode.X_CAST && !xDown) {
                resetEdictCharge();
                return;
            }

            if (clientEdictChargeMode == EdictChargeMode.Y_DOMAIN && !yDown) {
                resetEdictCharge();
                return;
            }

            if (clientEdictChargeMode == EdictChargeMode.X_CAST && ModItems.isEdictOfTwoLandsDomainActive(player)) {
                if (!clientEdictCooldownPromptedThisHold) {
                    player.displayClientMessage(
                            Component.translatable("message.ramesses_curse.edict_other_mode_active").withStyle(net.minecraft.ChatFormatting.RED),
                            true);
                    clientEdictCooldownPromptedThisHold = true;
                }
                resetEdictCharge();
                return;
            }

            int cooldownTicks = clientEdictChargeMode == EdictChargeMode.Y_DOMAIN
                    ? ModItems.getEdictOfTwoLandsDomainCooldownTicks(player)
                    : ModItems.getEdictOfTwoLandsCooldownTicks(player);
            if (cooldownTicks > 0) {
                if (!clientEdictCooldownPromptedThisHold) {
                    float cooldownSeconds = Math.max(0.1F, cooldownTicks / 20.0F);
                    MutableComponent message = clientEdictChargeMode == EdictChargeMode.Y_DOMAIN
                            ? Component.translatable("message.ramesses_curse.edict_domain_cooldown", String.format(Locale.ROOT, "%.1f", cooldownSeconds))
                            : Component.translatable("message.ramesses_curse.edict_cooldown", String.format(Locale.ROOT, "%.1f", cooldownSeconds));
                    player.displayClientMessage(message.withStyle(net.minecraft.ChatFormatting.RED), true);
                    clientEdictCooldownPromptedThisHold = true;
                }
                resetEdictCharge();
                return;
            }

            if (clientEdictChargeTicks < EDICT_CHARGE_TICKS) {
                clientEdictChargeTicks++;
            }

            if (clientEdictChargeTicks >= EDICT_CHARGE_TICKS && !clientEdictTriggeredThisHold) {
                clientEdictTriggeredThisHold = true;
                if (clientEdictChargeMode == EdictChargeMode.Y_DOMAIN) {
                    ModNetwork.CHANNEL.sendToServer(new EdictOfTwoLandsDomainActivatePacket());
                } else {
                    ModNetwork.CHANNEL.sendToServer(new EdictOfTwoLandsActivatePacket());
                }
            }

            if (clientEdictShakeTicks > 0) {
                clientEdictShakeTicks--;
                if (clientEdictShakeTicks <= 0) {
                    clientEdictShakeStrength = 0.0F;
                    clientEdictShakeTotalTicks = 0;
                }
            }
        }

        @SubscribeEvent
        public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
            if (clientEdictShakeTicks <= 0 || clientEdictShakeStrength <= 0.0F) {
                return;
            }

            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null) {
                return;
            }

            float progress = clientEdictShakeTicks / (float) Math.max(1, clientEdictShakeTotalTicks);
            float intensity = clientEdictShakeStrength * Math.max(0.2F, progress);
            float tick = (float) (minecraft.player.tickCount + event.getPartialTick());
            event.setYaw(event.getYaw() + (float) Math.sin(tick * 1.35F) * intensity);
            event.setPitch(event.getPitch() + (float) Math.cos(tick * 1.7F) * intensity * 0.55F);
            event.setRoll(event.getRoll() + (float) Math.sin(tick * 1.9F) * intensity * 0.4F);
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

        private static void renderEdictChargeBar(net.minecraft.client.gui.GuiGraphics guiGraphics, Minecraft minecraft) {
            if (clientEdictChargeTicks <= 0) {
                return;
            }

            String glyphBar = "IIIIIIIIIII";
            int barWidth = minecraft.font.width(glyphBar);
            int barHeight = minecraft.font.lineHeight;
            int x = (minecraft.getWindow().getGuiScaledWidth() - barWidth) / 2;
            int y = minecraft.getWindow().getGuiScaledHeight() - 50;
            float ratio = Mth.clamp(clientEdictChargeTicks / (float) EDICT_CHARGE_TICKS, 0.0F, 1.0F);
            int fill = Mth.floor(barWidth * ratio);

            guiGraphics.drawString(minecraft.font, glyphBar, x, y, 0xFF5B4630, false);
            if (fill > 0) {
                guiGraphics.enableScissor(x, y, x + fill, y + barHeight);
                guiGraphics.drawString(minecraft.font, glyphBar, x, y, 0xFFE0B84A, false);
                guiGraphics.disableScissor();
            }
            guiGraphics.drawCenteredString(
                    minecraft.font,
                    Component.translatable(
                            clientEdictChargeMode == EdictChargeMode.Y_DOMAIN
                                    ? "hud.ramesses_curse.edict_domain_charge"
                                    : "hud.ramesses_curse.edict_charge",
                            String.format(java.util.Locale.ROOT, "%.1f", clientEdictChargeTicks / 20.0F)),
                    minecraft.getWindow().getGuiScaledWidth() / 2,
                    y - 11,
                    0xFFE0C76B);
        }

        private static void resetEdictCharge() {
            clientEdictChargeTicks = 0;
            clientEdictTriggeredThisHold = false;
            clientEdictCooldownPromptedThisHold = false;
            clientEdictChargeMode = EdictChargeMode.NONE;
        }

        private static void renderLockedSlot(ScreenEvent.Render.Post event, AbstractContainerScreen<?> screen, int slotX, int slotY) {
            int x = screen.getGuiLeft() + slotX;
            int y = screen.getGuiTop() + slotY;
            float ratio = Mth.clamp(clientLockedSlotTicks / (float) Math.max(1, Config.INVENTORY_LOCK_DURATION.get()), 0.0F, 1.0F);
            int fill = Mth.ceil(ratio * 16.0F);

            event.getGuiGraphics().fill(x, y, x + 16, y + 16, 0xAA111111);
            event.getGuiGraphics().fill(x, y + 16 - fill, x + 16, y + 16, 0x883A3A3A);
            event.getGuiGraphics().fill(x, y + 15, x + 16, y + 16, 0xFF5C5C5C);
        }

        private static void renderCurseThreeCompatSuffix(ScreenEvent.Render.Post event) {
            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft.player;
            if (player == null || !ModEvents.canUseSealBoundItem(player)) {
                return;
            }

            if (!(event.getScreen() instanceof EnchantmentScreen screen) || event.getScreen().getClass() == EnchantmentScreen.class) {
                return;
            }

            int left = screen.getGuiLeft();
            int top = screen.getGuiTop();
            boolean apothScreen = screen.getClass().getName().equals("dev.shadowsoffire.apotheosis.ench.table.ApothEnchantScreen");
            for (int i = 0; i < screen.getMenu().costs.length; i++) {
                int vanillaCost = screen.getMenu().costs[i];
                int extraCost = ModEvents.getCurseThreeEnchantExtraCost(player, vanillaCost);
                if (vanillaCost <= 0 || extraCost <= 0) {
                    continue;
                }

                String text = Integer.toString(vanillaCost);
                int textX = apothScreen
                        ? left + 60 + 108 - minecraft.font.width("+" + extraCost) - 4
                        : left + 60 + 20 + 86 - minecraft.font.width(text);
                int textY = apothScreen
                        ? top + 14 + 19 * i + 5
                        : top + 16 + 19 * i + 7;
                if (apothScreen) {
                    event.getGuiGraphics().drawString(minecraft.font, "+" + extraCost, textX, textY, 0xFFFF5555, false);
                } else {
                    event.getGuiGraphics().drawString(minecraft.font, "+" + extraCost, textX + minecraft.font.width(text) + 2, textY, 0xFFFF5555, false);
                }
            }
        }

        private static void renderCurseThreeGenericMarker(ScreenEvent.Render.Post event) {
        }

        private static void renderCurseThreeHoverTooltip(ScreenEvent.Render.Post event) {
            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft.player;
            if (player == null || !ModEvents.canUseSealBoundItem(player)) {
                return;
            }

            int mouseX = (int) event.getMouseX();
            int mouseY = (int) event.getMouseY();

            if (event.getScreen() instanceof EnchantmentScreen screen) {
                int left = screen.getGuiLeft();
                int top = screen.getGuiTop();
                boolean apothScreen = screen.getClass().getName().equals("dev.shadowsoffire.apotheosis.ench.table.ApothEnchantScreen");
                for (int i = 0; i < screen.getMenu().costs.length; i++) {
                    int vanillaCost = screen.getMenu().costs[i];
                    int extraCost = ModEvents.getCurseThreeEnchantExtraCost(player, vanillaCost);
                    if (vanillaCost <= 0 || extraCost <= 0) {
                        continue;
                    }

                    if (apothScreen) {
                        int suffixX = left + 60 + 108 - minecraft.font.width("+" + extraCost) - 4;
                        int suffixY = top + 14 + 19 * i + 5;
                        int suffixWidth = minecraft.font.width("+" + extraCost);
                        if (mouseX >= suffixX && mouseX <= suffixX + suffixWidth
                                && mouseY >= suffixY && mouseY <= suffixY + minecraft.font.lineHeight) {
                            event.getGuiGraphics().renderTooltip(
                                    minecraft.font,
                                    ModEvents.getCurseThreeEnchantTooltip(player, vanillaCost),
                                    mouseX + 12,
                                    mouseY + 12);
                            return;
                        }
                    } else {
                        String text = Integer.toString(vanillaCost);
                        int textX = left + 60 + 20 + 86 - minecraft.font.width(text);
                        int textY = top + 16 + 19 * i + 7;
                        int suffixX = textX + minecraft.font.width(text) + 2;
                        int suffixWidth = minecraft.font.width("+" + extraCost);
                        if (mouseX >= suffixX && mouseX <= suffixX + suffixWidth && mouseY >= textY && mouseY <= textY + minecraft.font.lineHeight) {
                            event.getGuiGraphics().renderTooltip(minecraft.font, ModEvents.getCurseThreeEnchantTooltip(player, vanillaCost), mouseX, mouseY);
                            return;
                        }
                    }
                }
            }

            if (event.getScreen() instanceof AnvilScreen screen) {
                int vanillaCost = ModEvents.getDisplayedAnvilBaseCost(player, screen.getMenu().getCost());
                int extraCost = ModEvents.getCurseThreeAnvilExtraCost(player, vanillaCost);
                if (vanillaCost <= 0 || extraCost <= 0 || !screen.getMenu().getSlot(2).hasItem()) {
                    return;
                }

                Component costText = Component.translatable("container.repair.cost", vanillaCost);
                int left = screen.getGuiLeft();
                int top = screen.getGuiTop();
                int textX = left + 176 - 8 - minecraft.font.width(costText) - 2;
                int textY = top + 69;
                int suffixX = textX + minecraft.font.width(costText) + 2;
                int suffixWidth = minecraft.font.width("+" + extraCost);
                if (mouseX >= suffixX && mouseX <= suffixX + suffixWidth && mouseY >= textY && mouseY <= textY + minecraft.font.lineHeight) {
                    event.getGuiGraphics().renderTooltip(minecraft.font, ModEvents.getCurseThreeAnvilTooltip(player, vanillaCost), mouseX, mouseY);
                }
            }
        }

        private static void renderCurseThreeScreenInfo(ScreenEvent.Render.Post event) {
            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft.player;
            if (player == null || !clientSealEquipped) {
                return;
            }

            if (event.getScreen() instanceof EnchantmentScreen screen) {
                EnchantmentMenu menu = screen.getMenu();
                int x = (screen.width - 176) / 2;
                int y = (screen.height - 166) / 2;
                event.getGuiGraphics().drawString(
                        minecraft.font,
                        Component.translatable("screen.ramesses_curse.enchant_multiplier", formatMultiplier(Config.CURSE_3_ENCHANT_COST_MULTIPLIER.get())),
                        x,
                        y - 20,
                        0xFFD6A63A,
                        false);
                for (int i = 0; i < menu.costs.length; i++) {
                    int displayed = menu.costs[i];
                    if (displayed > 0) {
                        event.getGuiGraphics().drawString(
                                minecraft.font,
                                Component.literal(Integer.toString(displayed)).withStyle(net.minecraft.ChatFormatting.GOLD),
                                x + 62,
                                y + 14 + i * 19,
                                0xFFD6A63A,
                                false);
                    }
                }
                return;
            }

            if (event.getScreen() instanceof AnvilScreen screen) {
                AnvilMenu menu = screen.getMenu();
                int x = (screen.width - 176) / 2;
                int y = (screen.height - 166) / 2;
                int cost = menu.getCost();
                event.getGuiGraphics().drawString(
                        minecraft.font,
                        Component.translatable("screen.ramesses_curse.anvil_multiplier", formatMultiplier(Config.CURSE_3_ANVIL_COST_MULTIPLIER.get())),
                        x,
                        y - 20,
                        0xFFD6A63A,
                        false);
                if (cost > 0) {
                    event.getGuiGraphics().drawString(
                            minecraft.font,
                            Component.translatable("screen.ramesses_curse.anvil_cost", cost),
                            x,
                            y - 10,
                            0xFFFFD166,
                            false);
                }
            }
        }

        public static String formatMultiplier(double value) {
            if (Math.abs(value - Math.rint(value)) < 0.0001D) {
                return Integer.toString((int) Math.rint(value));
            }
            return String.format(java.util.Locale.ROOT, "%.1f", value);
        }
    }
}
