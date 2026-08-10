package com.eldren.wutao.ramesses_curse;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.client.player.Input;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.BossEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ClientChatEvent;
import vazkii.patchouli.api.BookDrawScreenEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.enchanting.EnchantmentLevelSetEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingUseTotemEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkDirection;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import org.joml.Vector3f;
import org.joml.Matrix4f;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

@Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {
    private static final Set<UUID> ETERNAL_TOTEM_SECOND_HIT_GUARD = new HashSet<>();
    private static final ResourceLocation NETHER_BRIDGE_CHEST = new ResourceLocation("minecraft", "chests/nether_bridge");
    private static final ResourceLocation BASTION_BRIDGE_CHEST = new ResourceLocation("minecraft", "chests/bastion_bridge");
    private static final ResourceLocation BASTION_HOGLIN_STABLE_CHEST = new ResourceLocation("minecraft", "chests/bastion_hoglin_stable");
    private static final ResourceLocation BASTION_OTHER_CHEST = new ResourceLocation("minecraft", "chests/bastion_other");
    private static final ResourceLocation BASTION_TREASURE_CHEST = new ResourceLocation("minecraft", "chests/bastion_treasure");
    private static final ResourceLocation RUINED_PORTAL_CHEST = new ResourceLocation("minecraft", "chests/ruined_portal");

    private static final String CURSE_DATA = "RamessesCurseData";
    private static final String EQUIP_TICKS = "EquipTicks";
    private static final String MAIN_LOCK_UNTIL = "MainHandLockUntil";
    private static final String DYNASTY_SILENCE_MAIN_HAND_UNTIL = "DynastySilenceMainHandUntil";
    private static final String DYNASTY_SILENCE_OFF_HAND_UNTIL = "DynastySilenceOffHandUntil";
    private static final String NEXT_RANDOM_LOCK = "NextRandomLock";
    private static final String LOCKED_SLOT = "LockedSlot";
    private static final String LOCKED_SLOT_UNTIL = "LockedSlotUntil";
    private static final String LOCKED_SLOT_STACK = "LockedSlotStack";
    private static final String NETHER_AWAKENED = "NetherAwakened";
    private static final String END_AWAKENED = "EndAwakened";
    private static final String CURSE_1_REVERSED = "CurseOneReversed";
    private static final String CURSE_1_REVERSED_COOLDOWN_UNTIL = "CurseOneReversedCooldownUntil";
    private static final String CURSE_1_RITUAL_ACTIVE = "CurseOneRitualActive";
    private static final String CURSE_1_RITUAL_STARTED_AT = "CurseOneRitualStartedAt";
    private static final String CURSE_1_RITUAL_MATCHED_AT = "CurseOneRitualMatchedAt";
    private static final String CURSE_1_RITUAL_DIMENSION = "CurseOneRitualDimension";
    private static final String CURSE_1_RITUAL_CAULDRON_X = "CurseOneRitualCauldronX";
    private static final String CURSE_1_RITUAL_CAULDRON_Y = "CurseOneRitualCauldronY";
    private static final String CURSE_1_RITUAL_CAULDRON_Z = "CurseOneRitualCauldronZ";
    private static final String CURSE_2_REVERSED = "CurseTwoReversed";
    private static final String CURSE_2_REVERSED_AT_EQUIP_TICKS = "CurseTwoReversedAtEquipTicks";
    private static final String CURSE_2_RETURNED_LUCK = "CurseTwoReturnedLuck";
    private static final String CURSE_5_REVERSED = "CurseFiveReversed";
    private static final String CURSE_5_FATE_DECIDED = "CurseFiveFateDecided";
    private static final String CURSE_5_DECISION_PENDING = "CurseFiveDecisionPending";
    private static final String CURSE_5_DECLINED = "CurseFiveDeclined";
    private static final String CURSE_5_PUNISHMENT_COMPLETED = "CurseFivePunishmentCompleted";
    private static final String CURSE_5_WAS_SLEEPING = "CurseFiveWasSleeping";
    private static final String CURSE_5_SLEEP_CHOICE_OPENED = "CurseFiveSleepChoiceOpened";
    private static final String CURSE_6_KEYWORD_UNLOCKED = "CurseSixKeywordUnlocked";
    private static final String CURSE_6_TERMINAL_SOLVED = "CurseSixTerminalSolved";
    private static final String CURSE_6_REVERSED = "CurseSixReversed";
    private static final String CURSE_6_FLIGHT_RESTORE_PENDING = "CurseSixFlightRestorePending";
    private static final String CURSE_6_SENTENCE_UNLOCKED = "CurseSixSentenceUnlocked";
    private static final String CURSE_6_SENTENCE_REVEAL_INDEX = "CurseSixSentenceRevealIndex";
    private static final String CURSE_6_SENTENCE_NEXT_REVEAL_AT = "CurseSixSentenceNextRevealAt";
    private static final String CURSE_7_REVERSED = "CurseSevenReversed";
    private static final String CURSE_6_STATE_MIGRATED = "CurseSixStateMigrated";
    private static final String CURSE_8_REVERSED = "CurseEightReversed";
    private static final String CURSE_9_REVERSED = "CurseNineReversed";
    private static final String CURSE_9_KILL_UNLOCKED = "CurseNineKillUnlocked";
    private static final String CURSE_9_FOOD_HISTORY = "CurseNineFoodHistory";
    private static final String CURSE_9_DEBT_TICKS = "CurseNineDebtTicks";
    private static final String CURSE_9_PENALTY_UNTIL = "CurseNinePenaltyUntil";
    private static final String CURSE_9_ALL_ATTRIBUTES_APPLIED = "CurseNineAllAttributesApplied";
    private static final String STARTER_GRANTED = "StarterGranted";
    private static final String MANUAL_GRANTED = "ManualGranted";
    private static final String GUILT_PROGRESS = "GuiltProgress";
    private static final String GUILT_SENTENCE_PENDING = "GuiltSentencePending";
    private static final String GUILT_LOCK_UNTIL = "GuiltLockUntil";
    private static final String MERCY_PROGRESS = "MercyProgress";
    private static final String MERCY_CHARGE_READY = "MercyChargeReady";
    private static final String MERCY_REVIVE_PENDING = "MercyRevivePending";
    private static final String MERCY_REVIVE_TELEPORT_PENDING = "MercyReviveTeleportPending";
    private static final String MERCY_REVIVE_DIMENSION = "MercyReviveDimension";
    private static final String MERCY_REVIVE_X = "MercyReviveX";
    private static final String MERCY_REVIVE_Y = "MercyReviveY";
    private static final String MERCY_REVIVE_Z = "MercyReviveZ";
    private static final String MERCY_REVIVE_YAW = "MercyReviveYaw";
    private static final String MERCY_REVIVE_PITCH = "MercyRevivePitch";
    private static final String MERCY_INVULNERABLE_UNTIL = "MercyInvulnerableUntil";
    private static final String MERCY_SURGE_PENDING_AT = "MercySurgePendingAt";
    private static final String MERCY_SURGE_UNTIL = "MercySurgeUntil";
    private static final String CURSE_5_FATAL_GUARD_INVULNERABLE_UNTIL = "CurseFiveFatalGuardInvulnerableUntil";
    private static final String CURSE_5_FATAL_GUARD_COOLDOWN_UNTIL = "CurseFiveFatalGuardCooldownUntil";
    private static final String SATIETY_CRYSTAL_OWNER = "SatietyCrystalOwner";
    private static final String SATIETY_CRYSTAL_ID = "SatietyCrystalId";
    private static final String SATIETY_CRYSTAL_COUNT = "SatietyCrystalCount";
    private static final String SATIETY_CRYSTAL_DIMENSION = "SatietyCrystalDimension";
    private static final String SATIETY_CRYSTAL_X = "SatietyCrystalX";
    private static final String SATIETY_CRYSTAL_Y = "SatietyCrystalY";
    private static final String SATIETY_CRYSTAL_Z = "SatietyCrystalZ";
    private static final String SATIETY_CRYSTAL_RECORDS = "SatietyCrystalRecords";
    private static final String SATIETY_CRYSTAL_PENDING_ITEMS = "SatietyCrystalPendingItems";
    private static final String SATIETY_CRYSTAL_ITEM_OWNER = "SatietyCrystalItemOwner";
    private static final String SATIETY_RIDDLE_ROUND = "SatietyRiddleRound";
    private static final String SATIETY_RIDDLE_INDEX = "SatietyRiddleIndex";
    private static final String SATIETY_RIDDLE_SOLVED = "SatietyRiddleSolved";
    private static final int SATIETY_RIDDLE_COUNT = 3;
    private static final String EMBER_RIDDLE_ID = "EmberRiddleId";
    private static final String EMBER_RIDDLE_UNLOCKED = "EmberRiddleUnlocked";
    private static final String EMBER_LAST_RIDDLE_ID = "EmberLastRiddleId";
    private static final String EMBER_RIDDLE_ROUND = "EmberRiddleRound";
    private static final String EMBER_RIDDLE_INDEX = "EmberRiddleIndex";
    private static final String EMBER_RIDDLE_SOLVED = "EmberRiddleSolved";
    private static final String EMBER_RIDDLE_UNLOCKED_SNAPSHOT = "EmberRiddleUnlockedSnapshot";
    private static final String EMBER_TRIAL_ACTIVE = "EmberTrialActive";
    private static final String EMBER_TRIAL_COMPLETED = "EmberTrialCompleted";
    private static final String EMBER_TRIAL_WAVE = "EmberTrialWave";
    private static final String EMBER_TRIAL_TOTAL = "EmberTrialTotal";
    private static final String EMBER_TRIAL_DEFEATED = "EmberTrialDefeated";
    private static final String EMBER_TRIAL_PENDING_SPAWNS = "EmberTrialPendingSpawns";
    private static final String EMBER_TRIAL_NEXT_SPAWN_AT = "EmberTrialNextSpawnAt";
    private static final String EMBER_TRIAL_MOB_UUIDS = "EmberTrialMobUuids";
    private static final String EMBER_TRIAL_MOB_UUID = "Uuid";
    private static final String EMBER_TRIAL_MOB = "EmberTrialMob";
    private static final String EMBER_TRIAL_OWNER = "EmberTrialOwner";
    private static final String LAST_SYNC_GUILT = "LastSyncGuilt";
    private static final String LAST_SYNC_SEAL = "LastSyncSeal";
    private static final String LAST_SYNC_SEAL_EQUIP_SECONDS = "LastSyncSealEquipSeconds";
    private static final String LAST_SYNC_NETHER_AWAKENED = "LastSyncNetherAwakened";
    private static final String LAST_SYNC_END_AWAKENED = "LastSyncEndAwakened";
    private static final String LAST_SYNC_CURSE_1_REVERSED = "LastSyncCurseOneReversed";
    private static final String LAST_SYNC_CURSE_2_REVERSED = "LastSyncCurseTwoReversed";
    private static final String LAST_SYNC_CURSE_5_REVERSED = "LastSyncCurseFiveReversed";
    private static final String LAST_SYNC_CURSE_5_DECLINED = "LastSyncCurseFiveDeclined";
    private static final String LAST_SYNC_CURSE_6_KEYWORD_UNLOCKED = "LastSyncCurseSixKeywordUnlocked";
    private static final String LAST_SYNC_CURSE_6_TERMINAL_SOLVED = "LastSyncCurseSixTerminalSolved";
    private static final String LAST_SYNC_CURSE_6_REVERSED = "LastSyncCurseSixReversed";
    private static final String LAST_SYNC_CURSE_7_REVERSED = "LastSyncCurseSevenReversed";
    private static final String LAST_SYNC_CURSE_6_SENTENCE_UNLOCKED = "LastSyncCurseSixSentenceUnlocked";
    private static final String LAST_SYNC_CURSE_8_REVERSED = "LastSyncCurseEightReversed";
    private static final String LAST_SYNC_CURSE_9_REVERSED = "LastSyncCurseNineReversed";
    private static final String LAST_SYNC_LOCK_SECONDS = "LastSyncLockSeconds";
    private static final String LAST_SYNC_SATIETY = "LastSyncSatiety";
    private static final String LAST_SYNC_CURSE_9_DEBT = "LastSyncCurseNineDebt";
    private static final String LAST_SYNC_LOCKED_SLOT = "LastSyncLockedSlot";
    private static final String LAST_SYNC_LOCKED_SLOT_TICKS = "LastSyncLockedSlotTicks";
    private static final String LAST_SYNC_DYNASTY_PRESSURE = "LastSyncDynastyPressure";
    private static final String LAST_SYNC_DYNASTY_SILENCE_MAIN_HAND_TICKS = "LastSyncDynastySilenceMainHandTicks";
    private static final String LAST_SYNC_DYNASTY_SILENCE_OFF_HAND_TICKS = "LastSyncDynastySilenceOffHandTicks";
    private static final String LAST_SYNC_MERCY = "LastSyncMercy";
    private static final String LAST_SYNC_MERCY_CHARGE = "LastSyncMercyCharge";
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
    private static final String CURSE_2_MINION = "RamessesCurseTwoMinion";
    private static final String CURSE_2_MINION_OWNER = "RamessesCurseTwoMinionOwner";
    private static final String CURSE_2_MINION_EXPIRES_AT = "RamessesCurseTwoMinionExpiresAt";
    private static final String CURSE_7_PENDING_HINT = "CurseSevenPendingHint";
    private static final String CURSE_7_HINT_TEXT = "CurseSevenHintText";
    private static final String CURSE_7_HINT_STYLE = "CurseSevenHintStyle";
    private static final String CURSE_7_HINT_STARTED_AT = "CurseSevenHintStartedAt";
    private static final String CURSE_3_4_LIFTED_HINT_SHOWN = "CurseThreeFourLiftedHintShown";
    private static final String CURSE_7_END_RITUAL_ACTIVE = "CurseSevenEndRitualActive";
    private static final String CURSE_7_END_RITUAL_DRAGON_READY = "CurseSevenEndRitualDragonReady";
    private static final String CURSE_7_END_RITUAL_STAR_READY = "CurseSevenEndRitualStarReady";
    private static final String CURSE_7_END_RITUAL_DRAGON_UUID = "CurseSevenEndRitualDragonUuid";
    private static final String CURSE_7_END_RITUAL_STAR_UUID = "CurseSevenEndRitualStarUuid";
    private static final String CURSE_7_END_RITUAL_STARTED_AT = "CurseSevenEndRitualStartedAt";
    private static final String CURSE_7_END_RITUAL_MATCHED_AT = "CurseSevenEndRitualMatchedAt";
    private static final String CURSE_7_END_RITUAL_SUCCESS_AT = "CurseSevenEndRitualSuccessAt";
    private static final String CURSE_7_END_RITUAL_LAST_MISSING = "CurseSevenEndRitualLastMissing";
    private static final String CURSE_7_END_RITUAL_DRAGON_ITEM = "CurseSevenEndRitualDragonItem";
    private static final String CURSE_7_END_RITUAL_STAR_ITEM = "CurseSevenEndRitualStarItem";
    private static final String CURSE_7_END_RITUAL_DRAGON_GLOW = "CurseSevenEndRitualDragonGlow";
    private static final String CURSE_7_END_RITUAL_STAR_GLOW = "CurseSevenEndRitualStarGlow";
    private static final String CURSE_7_END_RITUAL_MESSAGE = "CurseSevenEndRitualMessage";
    private static final String CURSE_7_END_RITUAL_MESSAGE_TICK = "CurseSevenEndRitualMessageTick";
    private static final String CURSE_7_END_RITUAL_MESSAGE_STYLE = "CurseSevenEndRitualMessageStyle";
    private static final String CURSE_7_END_RITUAL_RETURN_AT = "CurseSevenEndRitualReturnAt";
    private static final String CURSE_7_HEART_RITUAL_UUID = "CurseSevenHeartRitualUuid";
    private static final String CURSE_7_HEART_RITUAL_STARTED_AT = "CurseSevenHeartRitualStartedAt";
    private static final String CURSE_7_HEART_RITUAL_ORIGIN_Y = "CurseSevenHeartRitualOriginY";
    private static final String CURSE_7_HEART_RITUAL_ITEM = "CurseSevenHeartRitualItem";
    private static final String CURSE_7_CRYSTAL_BEAM_OWNER = "CurseSevenCrystalBeamOwner";
    private static final int CURSE_7_HEART_RITUAL_TICKS = 300;
    private static final int CURSE_7_REQUIRED_END_CRYSTALS = 10;
    private static final String RITUAL_OFFERING_OWNER = "RamessesCurseRitualOfferingOwner";
    private static final String CURSE_8_REBOUND_COOLDOWN_UNTIL = "RamessesCurseEightReboundCooldownUntil";
    private static final String DYNASTY_SHIELD_ASSIGNED = "RamessesDynastyShieldAssigned";
    private static final String DYNASTY_SHIELD_LEVEL = "RamessesDynastyShieldLevel";
    private static final String DYNASTY_SHIELD_CURRENT = "RamessesDynastyShieldCurrent";
    private static final String DYNASTY_SHIELD_MAX = "RamessesDynastyShieldMax";
    private static final String DYNASTY_SHIELD_DODGE_LEVEL = "RamessesDynastyShieldDodgeLevel";
    private static final String DYNASTY_SHIELD_CORROSION_LEVEL = "RamessesDynastyShieldCorrosionLevel";
    private static final String DYNASTY_SHIELD_UNYIELDING_LEVEL = "RamessesDynastyShieldUnyieldingLevel";
    private static final String DYNASTY_SHIELD_DAMAGE_CAP_LEVEL = "RamessesDynastyShieldDamageCapLevel";
    private static final String DYNASTY_SHIELD_SILENCE_LEVEL = "RamessesDynastyShieldSilenceLevel";
    private static final String DYNASTY_SHIELD_PURGE = "RamessesDynastyShieldPurge";
    private static final String DYNASTY_SHIELD_PROJECTILE_WARD = "RamessesDynastyShieldProjectileWard";
    private static final String DYNASTY_SHIELD_RAGE_LEVEL = "RamessesDynastyShieldRageLevel";
    private static final String DYNASTY_SHIELD_MOLTEN = "RamessesDynastyShieldMolten";
    private static final String DYNASTY_SHIELD_BLOOD_FORFEIT = "RamessesDynastyShieldBloodForfeit";
    private static final String DYNASTY_SHIELD_FLIGHT_FALL = "RamessesDynastyShieldFlightFall";
    private static final String DYNASTY_SHIELD_CLAW_RAGE_LEVEL = "RamessesDynastyShieldClawRageLevel";
    private static final String DYNASTY_SHIELD_STEEL_BONE = "RamessesDynastyShieldSteelBone";
    private static final String DYNASTY_SHIELD_FLESH_FEAST_LEVEL = "RamessesDynastyShieldFleshFeastLevel";
    private static final String DYNASTY_SHIELD_MIASMA_LEVEL = "RamessesDynastyShieldMiasmaLevel";
    private static final String DYNASTY_SHIELD_RESTORATION_LEVEL = "RamessesDynastyShieldRestorationLevel";
    private static final String DYNASTY_SHIELD_BLOOD_LIMIT = "RamessesDynastyShieldBloodLimit";
    private static final String DYNASTY_SHIELD_SHATTER = "RamessesDynastyShieldShatter";
    private static final String DYNASTY_SHIELD_DEATH_AUTHORITY = "RamessesDynastyShieldDeathAuthority";
    private static final String DYNASTY_SHIELD_HIT_SLOW = "RamessesDynastyShieldHitSlow";
    private static final String DYNASTY_SHIELD_VULNERABILITY_LEVEL = "RamessesDynastyShieldVulnerabilityLevel";
    private static final String DYNASTY_SHIELD_COUNTER = "RamessesDynastyShieldCounter";
    private static final String DYNASTY_SHIELD_COUNTER_USED = "RamessesDynastyShieldCounterUsed";
    private static final String DYNASTY_FULL_AFFIX_HEALTH_APPLIED = "RamessesDynastyFullAffixHealthApplied";
    private static final String DYNASTY_DEATH_AUTHORITY_READY_AT = "RamessesDynastyDeathAuthorityReadyAt";
    private static final String DYNASTY_BLOOD_LIMIT_WINDOW_START = "RamessesDynastyBloodLimitWindowStart";
    private static final String DYNASTY_BLOOD_LIMIT_WINDOW_DAMAGE = "RamessesDynastyBloodLimitWindowDamage";
    private static final String DYNASTY_CLAW_RAGE_ARMED = "RamessesDynastyClawRageArmed";
    public static final String URAEUS_NECKLACE_EFFECT_BYPASS_UNTIL = "RamessesUraeusNecklaceEffectBypassUntil";
    private static final String DYNASTY_SHIELD_UNYIELDING_REVIVES = "RamessesDynastyShieldUnyieldingRevives";
    private static final String DYNASTY_SHIELD_UNYIELDING_FIXED = "RamessesDynastyShieldUnyieldingFixed";
    private static final String DYNASTY_SHIELD_LAST_SYNC_CURRENT = "RamessesDynastyShieldLastSyncCurrent";
    private static final String DYNASTY_SHIELD_LAST_SYNC_MAX = "RamessesDynastyShieldLastSyncMax";
    private static final String DYNASTY_SHIELD_THREAT_ASSIGNED = "RamessesDynastyShieldThreatAssigned";
    private static final int DYNASTY_SHIELD_UNYIELDING_MAX_REVIVES = 1;
    private static final String DYNASTY_PRESSURE = "DynastyPressure";
    private static final String DYNASTY_PRESSURE_MULTIPLIER_TENTHS = "DynastyPressureMultiplierTenths";
    private static final String DYNASTY_PRESSURE_LAST_DAY = "DynastyPressureLastDay";
    private static final int DYNASTY_PRESSURE_POTION_AMOUNT = 1000;
    private static final int DYNASTY_PRESSURE_DAILY_AMOUNT = 10;
    private static final int DYNASTY_PRESSURE_DIMENSION_CHANGE_AMOUNT = 10;
    private static final int DYNASTY_PRESSURE_MAX = 9999;
    private static final int[] DYNASTY_AFFIX_THREAT_THRESHOLDS = {0, 500, 1000, 1750, 2500, 3500, 4500, 5750, 7000, 8500};
    private static final int DYNASTY_PRESSURE_MULTIPLIER_INITIAL_TENTHS = 10;
    private static final int DYNASTY_PRESSURE_MULTIPLIER_MAX_TENTHS = 100;
    private static final String DYNASTY_AFFIX_DODGE = "dodge";
    private static final String DYNASTY_AFFIX_CORROSION = "corrosion";
    private static final String DYNASTY_AFFIX_DAMAGE_CAP = "damage_cap";
    private static final String DYNASTY_AFFIX_UNYIELDING = "unyielding";
    private static final String DYNASTY_AFFIX_SILENCE = "silence";
    private static final String DYNASTY_AFFIX_PURGE = "purge";
    private static final String DYNASTY_AFFIX_PROJECTILE_WARD = "projectile_ward";
    private static final String DYNASTY_AFFIX_RAGE = "rage";
    private static final String DYNASTY_AFFIX_MOLTEN = "molten";
    private static final String DYNASTY_AFFIX_BLOOD_FORFEIT = "blood_forfeit";
    private static final String DYNASTY_AFFIX_FLIGHT_FALL = "flight_fall";
    private static final String DYNASTY_AFFIX_CLAW_RAGE = "claw_rage";
    private static final String DYNASTY_AFFIX_STEEL_BONE = "steel_bone";
    private static final String DYNASTY_AFFIX_FLESH_FEAST = "flesh_feast";
    private static final String DYNASTY_AFFIX_MIASMA = "miasma";
    private static final String DYNASTY_AFFIX_RESTORATION = "restoration";
    private static final String DYNASTY_AFFIX_BLOOD_LIMIT = "blood_limit";
    private static final String DYNASTY_AFFIX_SHIELD_SHATTER = "shield_shatter";
    private static final String DYNASTY_AFFIX_DEATH_AUTHORITY = "death_authority";
    private static final String DYNASTY_AFFIX_HIT_SLOW = "hit_slow";
    private static final String DYNASTY_AFFIX_VULNERABILITY = "vulnerability";
    private static final String DYNASTY_AFFIX_COUNTER = "counter";
    private static final String BLOOD_FORFEIT_LEDGER = "DynastyBloodForfeitLedger";
    private static final String BLOOD_FORFEIT_AMOUNT = "DynastyBloodForfeitAmount";
    private static final String BLOOD_FORFEIT_UNTIL = "DynastyBloodForfeitUntil";
    private static final int BLOOD_FORFEIT_DURATION_TICKS = 5 * 60 * 20;
    private static final String FLIGHT_FALL_LOCKED = "RamessesDynastyFlightFallLocked";
    private static final String FLIGHT_FALL_LANDED_AT = "RamessesDynastyFlightFallLandedAt";
    private static final String FLIGHT_FALL_RESTORE_MAYFLY = "RamessesDynastyFlightFallRestoreMayfly";
    private static final UUID BLOOD_FORFEIT_HEALTH_MODIFIER_ID = UUID.fromString("ae23b569-8d4b-49f0-b35e-599bc09b9003");
    private static final UUID DYNASTY_RAGE_ATTACK_SPEED_MODIFIER_ID = UUID.fromString("ae23b569-8d4b-49f0-b35e-599bc09b9001");
    private static final UUID DYNASTY_RAGE_MOVEMENT_SPEED_MODIFIER_ID = UUID.fromString("ae23b569-8d4b-49f0-b35e-599bc09b9002");
    private static final UUID DYNASTY_FLESH_FEAST_HEALTH_MODIFIER_ID = UUID.fromString("ae23b569-8d4b-49f0-b35e-599bc09b9004");
    private static final UUID DYNASTY_FLESH_FEAST_ARMOR_MODIFIER_ID = UUID.fromString("ae23b569-8d4b-49f0-b35e-599bc09b9005");
    private static final UUID DYNASTY_FULL_AFFIX_HEALTH_MODIFIER_ID = UUID.fromString("ae23b569-8d4b-49f0-b35e-599bc09b9010");
    private static final double DYNASTY_SHIELD_HEALTH_BONUS_PER_LEVEL = 100.0D;
    private static final String PITCHFIRE_LAVA_WALK_UNTIL = "RamessesPitchfireLavaWalkUntil";
    private static final String DYNASTY_MOLTEN_FIREBALL = "RamessesDynastyMoltenFireball";
    private static final String DYNASTY_MOLTEN_BURN_UNTIL = "RamessesDynastyMoltenBurnUntil";
    private static final String DYNASTY_MOLTEN_BURN_NEXT_DAMAGE_AT = "RamessesDynastyMoltenBurnNextDamageAt";
    private static final String DYNASTY_MOLTEN_BURN_SOURCE = "RamessesDynastyMoltenBurnSource";
    private static final int CURSE_5_MERCY_INVULNERABLE_TICKS = 200;
    private static final int CURSE_5_MERCY_SURGE_TICKS = 600;
    private static final int CURSE_5_FATAL_GUARD_INVULNERABLE_TICKS = 600;
    private static final int CURSE_5_FATAL_GUARD_COOLDOWN_TICKS = 3600;
    private static final int CURSE_2_LUCK_INTERVAL_TICKS = 5 * 60 * 20;
    private static final int CURSE_5_CHOICE_HINT_TICKS = 140;
    private static final int CURSE_5_SLEEP_FREEZE_TICKS = 20;
    private static final int CURSE_3_4_LIFTED_HINT_TICKS = 140;
    private static final int DYNASTY_SHIELD_TOGGLE_HINT_TICKS = 110;
    private static final Map<String, UUID> CURSE_5_SURGE_MODIFIER_IDS = new HashMap<>();
    private static final int CURSE_9_HUNGER_LOSS = 2;
    private static final int CURSE_9_DEBT_DRAIN_INTERVAL = 100;
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
    private static final UUID CURSE_5_DECLINED_ATTACK_MODIFIER = UUID.fromString("c16fc4c7-a88f-47f2-a0c9-86ed8cb4fe26");
    private static final UUID CURSE_5_DECLINED_HEALTH_MODIFIER = UUID.fromString("95b50734-3faa-4b58-a4cc-357d705d8698");
    private static final UUID CURSE_5_DECLINED_ARMOR_MODIFIER = UUID.fromString("ce9d3b0b-6528-4f9e-a4ef-58dd44153532");
    private static final UUID CURSE_5_DECLINED_SPEED_MODIFIER = UUID.fromString("38ab1468-ef3b-42fe-a7f5-d5fa2861850b");
    private static final UUID CURSE_9_HEALTH_MODIFIER = UUID.fromString("83d0fbf1-6e78-45af-bd25-126ba3cfd2f3");
    private static final UUID CURSE_9_LUCK_MODIFIER = UUID.fromString("d18bf2ef-3827-4d0c-88db-b0f445899a69");
    private static final UUID CURSE_9_ARMOR_MODIFIER = UUID.fromString("540b20f1-d413-4678-b13f-b0d53c5fe985");
    private static final UUID CURSE_9_BLOCK_REACH_MODIFIER = UUID.fromString("bf4fc6f7-2ce3-4c2a-a321-9db4ef4b8f72");
    private static final UUID CURSE_9_ENTITY_REACH_MODIFIER = UUID.fromString("53fe3106-b39d-44fe-afbc-5e62dce5bf3a");
    private static final UUID CURSE_9_ALL_ATTRIBUTES_MODIFIER = UUID.fromString("7549a0d1-a688-4fad-8a89-4dbaccc0f299");
    private static final Field PLAYER_SLEEP_COUNTER_FIELD = findPlayerSleepCounterField();
    private static final Map<ResourceKey<Level>, Map<BlockPos, TrackedMusicPlayback>> ACTIVE_MUSIC_PLAYBACKS = new HashMap<>();
    private static final Map<ResourceKey<Level>, Map<BlockPos, TemporaryMoltenBlock>> TEMPORARY_MOLTEN_BLOCKS = new HashMap<>();
    private static final Map<UUID, ServerBossEvent> ACTIVE_EMBER_TRIAL_BARS = new HashMap<>();
    private static final Map<Integer, List<EntityType<? extends Mob>>> EMBER_TRIAL_ENTITY_POOLS = new HashMap<>();
    private static final int[] EMBER_TRIAL_WAVE_COUNTS = new int[]{5, 7, 9};
    private static final int EMBER_TRIAL_SPAWN_INTERVAL_TICKS = 2;
    private static final int EMBER_TRIAL_BOSSBAR_UPDATE_INTERVAL_TICKS = 5;
    private static final String[] EMBER_CIPHER_GLYPHS = new String[]{
            "\uE000", "\uE001", "\uE002", "\uE003", "\uE004", "\uE005", "\uE006", "\uE007", "\uE008", "\uE009",
            "\uE00A", "\uE00B", "\uE00C", "\uE00D", "\uE00E", "\uE00F", "\uE010", "\uE011", "\uE012", "\uE013",
            "\uE014", "\uE015", "\uE016", "\uE017", "\uE018", "\uE019"
    };
    private static final String CURSE_7_CHAT_KEYWORD = "beyond the void the ash remains";
    private static final String CURSE_7_CHAT_SCRAMBLED = "ydeaobn eht viod eht hsa sniamer";
    private static final String CURSE_7_UI_GUIDANCE = "Seek the Ember of Duat for the next inscription.";
    private static final String CURSE_7_UI_GUIDANCE_ZH = "前往杜阿特余烬查看下一道碑录。";
    private static final String CURSE_7_DUAT_REVEAL = "an older line stirs the ember: descend where the dragon keeps its throne";
    private static final String CURSE_7_DUAT_REVEAL_ZH = "更古老的碑录搅动了余烬：前往龙王座下。";
    private static final String CURSE_7_SUCCESS_LINE = "exchange completed cooperation appreciated curse six reversed";
    private static final String CURSE_7_SUCCESS_LINE_ZH = "交换已完成，合作愉快。诅咒六已反转。";
    private static final String CURSE_7_MISSING_OFFERING_PREFIX_ZH = "仍缺祭品：";
    private static final String CURSE_7_VOID_REJECTION_ZH = "虚空拒绝了这场未完成的交换。";
    private static final int CURSE_7_HINT_DURATION = 90;
    private static final int CURSE_7_SENTENCE_REVEAL_INTERVAL = 4;
    private static final int CURSE_7_RITUAL_RETURN_TICKS = 200;
    private static final int CURSE_7_RITUAL_SUCCESS_DELAY = 100;
    private static final List<EmberRiddle> EMBER_RIDDLES = buildEmberRiddles();

    private static List<EmberRiddle> buildEmberRiddles() {
        List<EmberRiddle> riddles = new ArrayList<>();
        String data = """
01 | 碑文一 日轮 | 我总在黎明前照亮沙海，人人借我计算白昼。 | sun
02 | 碑文二 月华 | 我悬于夜幕高处，不发热也能照路。 | moon
03 | 碑文三 双冠 | 我束起两地权柄，常立于王者额前。 | crown
04 | 碑文四 生符 | 我像钥匙却不开门，我指向延续与呼吸。 | ankh
05 | 碑文五 推日之甲 | 我滚动光球前行，在墓与饰物间常被看见。 | scarab
06 | 碑文六 长河 | 我自南向北而流，泥土与王朝都因我存活。 | nile
07 | 碑文七 立王之人 | 我不耕田，却让名字先刻在石头上。 | pharaoh
08 | 碑文八 守墓之神 | 我以犬首引路，在静默中衡量归途。 | anubis
09 | 碑文九 伏石之问 | 我半人半兽，最擅长不张口的提问。 | sphinx
10 | 碑文十 向天之陵 | 我以四面收束大地，像坟墓也像长矛。 | pyramid
11 | 碑文十一 夜后之路 | 我不是夜色本身，却收容亡者归行。 | duat
12 | 碑文十二 白羽长喙 | 我立在浅水之间，抄写与月意常借我之形。 | ibis
13 | 碑文十三 归来之王 | 我曾被分开，后来又在绿意中归来。 | osiris
14 | 碑文十四 羽衡 | 我轻若一片羽，却能压住整座王朝。 | maat
15 | 碑文十五 鹰眼 | 我凝望王权，常在冠前而不在座上。 | horus
16 | 碑文十六 家门之守 | 我以猫形护火，白日温顺夜里警醒。 | bastet
17 | 碑文十七 隐名者 | 许多人敬我时，只说我难以被看见。 | amun
18 | 碑文十八 天轮之主 | 每一个黎明来临时，总有人先想起我。 | ra
19 | 碑文十九 旧纸之草 | 我被卷成书页，承载墨与旧时代。 | papyrus
20 | 碑文二十 直立之石 | 我笔直向上，把影子切成两半。 | obelisk
21 | 碑文二十一 缠布沉眠 | 我被布条紧紧包裹，却不是礼物。 | mummy
22 | 碑文二十二 石中之眠 | 石壳包住我的安静，里面护着一具遗身。 | sarcophagus
23 | 碑文二十三 冠前之蛇 | 我盘在王额之前，既是威吓也是庇护。 | uraeus
24 | 碑文二十四 晨光之甲 | 我在最早的光里推着新一天出现。 | khepri
25 | 碑文二十五 王座之母 | 我因治愈与王权之力而被反复呼唤。 | isis
26 | 碑文二十六 烈狮 | 我兼具怒火与战意，牙锋像灼人的日光。 | sekhmet
27 | 碑文二十七 记述之手 | 文字、计算与记忆都愿意向我靠拢。 | thoth
28 | 碑文二十八 吞日长影 | 我潜伏深处，总想把高天之火吞掉。 | apep
29 | 碑文二十九 同息之影 | 我像另一个你，共享同一口气。 | ka
30 | 碑文三十 翼魂 | 我能暂离肉身，看起来像会飞的灵。 | ba
31 | 碑文三十一 芦原 | 亡者向往那里，因为田野总是青绿无边。 | aaru
32 | 碑文三十二 芳涂 | 我抹在皮肤之上，以香气盖过尘土。 | balm
33 | 碑文三十三 身前小护 | 我常被挂在胸前，小而隐秘却让人安心。 | amulet
34 | 碑文三十四 行日之舟 | 我承载光明越过天海，明明是舟却不靠水。 | solar bark
35 | 碑文三十五 归后之书 | 我的页中写满亡者才能用上的言辞。 | book of the dead
36 | 碑文三十六 覆面之辉 | 我遮住王者的面容，却让他死后更明亮。 | golden mask
37 | 碑文三十七 柱火回声 | 香烟、立柱与祈祷都在这里聚集。 | temple
38 | 碑文三十八 诵词之人 | 我知道何时点香，也知道何时开口。 | priest
39 | 碑文三十九 献纳之前 | 祭品、誓言与祈愿都要先经过我。 | altar
40 | 碑文四十 隐答 | 有人发问之后，总会期待我给出看不见的回应。 | oracle
41 | 碑文四十一 深蓝之石 | 我的颜色像王夜，越看越沉静。 | lapis lazuli
42 | 碑文四十二 红色脉络 | 细小如尘，却总能把力量送往别处。 | redstone
43 | 碑文四十三 火记之黑 | 我像玻璃，却记得烈火与重压。 | obsidian
44 | 碑文四十四 硬光 | 人人都想拥有我，因为我明亮且难碎。 | diamond
45 | 碑文四十五 交易之绿 | 村人与我关系极近，而我总闪着温润的色泽。 | emerald
46 | 碑文四十六 熔火之后 | 我曾在炽热中沉没，回来时更沉更坚。 | netherite
47 | 碑文四十七 折距之珠 | 只要抛出我，距离便会被强行弯折。 | ender pearl
48 | 碑文四十八 世界尽头之影 | 最后的大影常守在黑曜边界的高空。 | ender dragon
49 | 碑文四十九 绿响 | 我先发出短促的警告，再让墙壁失去意义。 | creeper
50 | 碑文五十 迟步饥影 | 我走得不快，却总被空腹推动着前进。 | zombie
51 | 碑文五十一 骨与弓 | 黑暗里飞出的一箭，常由我先松手。 | skeleton
52 | 碑文五十二 织网者 | 我爬墙结丝，把空气也变成陷阱。 | spider
53 | 碑文五十三 瓶药与笑 | 我戴着尖帽，投来的从不是善意。 | witch
54 | 碑文五十四 旗前之夺 | 我举着旗帜靠近，从不懂敲门。 | pillager
55 | 碑文五十五 壳中之举 | 我藏在硬壳内，却能把人直接托离地面。 | shulker
56 | 碑文五十六 炎环 | 我被火焰绕着旋转，守着堡垒的空气。 | blaze
57 | 碑文五十七 远哭 | 我在下界长嚎，像漂浮在空中的悲声。 | ghast
58 | 碑文五十八 深暗之听 | 我看不见你，却几乎总能找到你。 | warden
59 | 碑文五十九 海下之息 | 有我在时，海水也能容人久留。 | conduit
60 | 碑文六十 吞水之黄 | 我能把整片水域喝干，让地面裸露出来。 | sponge
61 | 碑文六十一 翼布 | 只要纵身一跃，天空就会化作道路。 | elytra
62 | 碑文六十二 微光书页 | 纸页合拢时无声，打开后却许诺更锋利的力量。 | enchanted book
63 | 碑文六十三 重铁之台 | 我修补、命名，也在每次敲击后越发疲惫。 | anvil
64 | 碑文六十四 简火之箱 | 我只靠热，就把生与熟分成前后。 | furnace
65 | 碑文六十五 再铸之处 | 我让旧装备继续前进，又不忘原本的样子。 | smithing table
66 | 碑文六十六 独眼远观 | 我一次只借给一只眼，却能把远方拉近。 | spyglass
67 | 碑文六十七 归向 | 我总记得某个位置，并执拗地朝它指去。 | compass
68 | 碑文六十八 夜间暂停 | 只要周围足够安静，我便让黑夜直接略过。 | bed
69 | 碑文六十九 浮木之行 | 当我碰到水，水面就成了道路。 | boat
70 | 碑文七十 宝藏之口 | 我沉默地收着东西，直到有人来打开。 | chest
71 | 碑文七十一 小容万物 | 我看似只盛液体，却常装得比外形更多。 | bucket
72 | 碑文七十二 穴中之火 | 我让洞穴不再显得那么孤独。 | torch
73 | 碑文七十三 未成之树 | 我在长成参天之前，只是一点脆弱的开端。 | sapling
74 | 碑文七十四 河边之线 | 我把耐心垂进水里，等一个突然的回拽。 | fishing rod
75 | 碑文七十五 先张后发 | 我总是先蓄着力，再快速回答远处。 | crossbow
76 | 碑文七十六 海暴之叉 | 我属于风暴与深海，尖端一分为三。 | trident
77 | 碑文七十七 直去之羽 | 一旦被放手，我只想着朝前。 | arrow
78 | 碑文七十八 固名之片 | 我能让生物的名字从此不再改变。 | name tag
79 | 碑文七十九 不用笼子的束缚 | 我不把谁关起来，却能让谁别走远。 | lead
80 | 碑文八十 小焰 | 我安静而微弱，却足以照亮一间房。 | candle
81 | 碑文八十一 水下之灯 | 我在海里也能持续发亮，从不溺亡。 | sea lantern
82 | 碑文八十二 凝住的下界光 | 我像一块被冻住的明亮火色。 | glowstone
83 | 碑文八十三 会老去的金属 | 我先是温暖的橙，后来慢慢转绿。 | copper ingot
84 | 碑文八十四 奢侈一口 | 只需咬下去，身体便会想起某种珍贵。 | golden apple
85 | 碑文八十五 火后之食 | 火把原本粗粝的东西变成了可安心吞下的晚餐。 | steak
86 | 碑文八十六 最朴素的充饥 | 我柔软简单，却足以让人继续上路。 | bread
87 | 碑文八十七 六角甜壳 | 我黏而明亮，甜味被分隔成整齐的格子。 | honeycomb
88 | 碑文八十八 橙色切片 | 外壳、香甜和秋意都被烤进了一块。 | pumpkin pie
89 | 碑文八十九 快步 | 我还没想完，脚就已经先动了。 | swiftness
90 | 碑文九十 沉重一击 | 当我降临时，每次命中都比先前更实。 | strength
91 | 碑文九十一 黑中可见 | 我让黑暗变得像白昼一样可读。 | night vision
92 | 碑文九十二 闭目之幕 | 世界在我四周把眼睛全部合上。 | blindness
93 | 碑文九十三 倾斜之感 | 地平线开始晃动，胃也跟着抱怨。 | nausea
94 | 碑文九十四 空腹驱策 | 我先掏空你的食量，再催你去找更多。 | hunger
95 | 碑文九十五 偏爱 | 掉落与概率在我面前会稍微温柔一点。 | luck
96 | 碑文九十六 经耕之地 | 水、锄与等待共同把泥土变成收成。 | farmland
97 | 碑文九十七 钟与门之聚 | 钟声、房屋与交易把人们留在这里。 | village
98 | 碑文九十八 框中异界 | 只要把边框立好，另一头的世界就会回应。 | portal
        """;

        for (String line : data.strip().split("\\R")) {
            if (line.isBlank()) {
                continue;
            }
            String[] parts = parseEmberRiddleEntry(line);
            if (parts == null || parts.length != 3) {
                throw new IllegalStateException("Invalid ember riddle entry: " + line);
            }
            int page = riddles.size() + 1;
            String id = String.format(Locale.ROOT, "ember_%02d", page);
            riddles.add(new EmberRiddle(id, parts[0].trim(), page, parts[1].trim(), parts[2].trim()));
        }
        if (riddles.size() != 98) {
            throw new IllegalStateException("Expected 98 ember riddles but found " + riddles.size());
        }
        return riddles;
    }

    @Nullable
    private static String[] parseEmberRiddleEntry(String line) {
        String[] direct = line.split("\\|");
        if (direct.length >= 4) {
            String title = direct[1].trim();
            String question = direct[2].trim();
            String answer = direct[3].trim();
            if (!title.isEmpty() && !question.isEmpty() && !answer.isEmpty()) {
                return new String[]{title, question, answer};
            }
        }
        if (direct.length == 3) {
            String title = stripLeadingRiddleIndex(direct[0].trim());
            String question = direct[1].trim();
            String answer = direct[2].trim();
            if (!title.isEmpty() && !question.isEmpty() && !answer.isEmpty()) {
                return new String[]{title, question, answer};
            }
        }

        int answerStart = findTrailingAnswerStart(line);
        if (answerStart < 0) {
            return null;
        }

        String answer = line.substring(answerStart).trim();
        String remainder = stripTrailingRiddleDelimiter(line.substring(0, answerStart).trim());
        if (remainder.isEmpty()) {
            return null;
        }

        int firstDivider = remainder.indexOf('|');
        String title;
        String question;
        if (firstDivider >= 0) {
            title = remainder.substring(0, firstDivider).trim();
            question = remainder.substring(firstDivider + 1).trim();
        } else {
            int splitIndex = findFallbackTitleSplit(remainder);
            if (splitIndex < 0) {
                title = buildFallbackRiddleTitle(answer);
                question = remainder.trim();
            } else {
                title = remainder.substring(0, splitIndex).trim();
                question = remainder.substring(splitIndex).trim();
            }
        }

        question = stripTrailingRiddleDelimiter(question);
        title = stripLeadingRiddleIndex(title);
        if (title.isEmpty() || question.isEmpty() || answer.isEmpty()) {
            return null;
        }
        return new String[]{title, question, answer};
    }

    private static String stripLeadingRiddleIndex(String title) {
        return title.replaceFirst("^\\s*\\d+\\s*[.|、]\\s*", "").trim();
    }

    private static String buildFallbackRiddleTitle(String answer) {
        if (answer.isEmpty()) {
            return "Ember Inscription";
        }

        StringBuilder builder = new StringBuilder(answer.length());
        boolean capitalize = true;
        for (int i = 0; i < answer.length(); i++) {
            char c = answer.charAt(i);
            if (c == '_' || c == '-') {
                builder.append(' ');
                capitalize = true;
                continue;
            }
            builder.append(capitalize ? Character.toUpperCase(c) : c);
            capitalize = false;
        }
        return builder.toString();
    }

    private static int findTrailingAnswerStart(String line) {
        int answerStart = line.length();
        while (answerStart > 0) {
            char c = line.charAt(answerStart - 1);
            if (c >= 'a' && c <= 'z') {
                answerStart--;
                continue;
            }
            break;
        }
        return answerStart >= line.length() ? -1 : answerStart;
    }

    private static int findFallbackTitleSplit(String input) {
        boolean seenNonWhitespace = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (!Character.isWhitespace(c)) {
                seenNonWhitespace = true;
                continue;
            }
            if (!seenNonWhitespace) {
                continue;
            }

            int next = i;
            while (next < input.length() && Character.isWhitespace(input.charAt(next))) {
                next++;
            }
            if (next >= input.length()) {
                break;
            }
            return next;
        }
        return -1;
    }

    private static String stripTrailingRiddleDelimiter(String input) {
        int end = input.length();
        while (end > 0) {
            char c = input.charAt(end - 1);
            if ((c >= 'a' && c <= 'z') || Character.isDigit(c)) {
                end--;
                continue;
            }
            if (c == '|' || c == ':' || c == ';' || c == ',' || c == '.' || c == '?' || c == '!' || Character.isWhitespace(c)) {
                end--;
                continue;
            }
            if (end == input.length()) {
                end--;
            }
            break;
        }
        return input.substring(0, Math.max(0, end)).trim();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }

        Player player = event.player;
        ModItems.EternalSilenceTotem.tickRevivalEffects(player);
        ModItems.ChanceDieSixSided.tick(player);
        ModItems.TwilightCrystalRing.tickEquippedAttributes(player);
        ModMobEffects.tickDrought(player);
        tickPitchfireLavaWalking(player);
        tickDynastyMoltenHazards(player);
        tickBloodForfeit(player);
        if (player.tickCount % 20 == 0) {
            syncBloodForfeitHealthPenalty(player);
        }
        CompoundTag data = getCurseData(player);
        if (player instanceof ServerPlayer serverPlayer) {
            if (player.tickCount % 40 == 0) {
                deliverPendingSatietyCrystals(serverPlayer);
            }
            if (player.tickCount % 200 == 0) {
                reconcileSatietyCrystalItems(serverPlayer);
            }
            restorePendingCurseSixFlight(serverPlayer, data);
            tickDynastyPressureGrowth(serverPlayer, data);
            tickCurseFiveSleepChoice(serverPlayer, data);
        }
        boolean sealEquipped = sanitizeSealCurios(player);
        boolean curseSixReversed = data.getBoolean(CURSE_6_REVERSED);
        if (curseSixReversed && player instanceof ServerPlayer serverPlayer) {
            clearCurseSixLockState(serverPlayer, data);
        }
        int remainingGuiltLock = getRemainingGuiltLockTicks(player);
        normalizeLockedSlotState(player, data);

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

        int remainingMainHandSilence = getRemainingDynastySilenceTicks(player, InteractionHand.MAIN_HAND);
        int remainingOffHandSilence = getRemainingDynastySilenceTicks(player, InteractionHand.OFF_HAND);
        if (player instanceof ServerPlayer serverPlayer) {
            applyDynastySilenceCooldown(serverPlayer, remainingMainHandSilence, remainingOffHandSilence);
        }

        int remainingLockedSlotTicks = getRemainingLockedSlotTicks(player);
        if (remainingLockedSlotTicks > 0 && player instanceof ServerPlayer serverPlayer) {
            applyLockedSlotCooldown(serverPlayer, remainingLockedSlotTicks);
        }

        if (player.level() instanceof ServerLevel serverLevel) {
            updateCurseNineHunger(serverLevel, player, sealEquipped);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            finalizePendingMercyReviveTeleport(serverPlayer);
            tickEdictDomain(serverPlayer);
            tickCurseTwoMinions(serverPlayer);
            tickCurseOneCupRitual(serverPlayer);
            tickCurseFiveMercy(serverPlayer);
            tickCurseFiveDeclinedPower(serverPlayer);
            tickEmberTrial(serverPlayer);
            tickCurseSevenSentenceReveal(serverPlayer);
            tickCurseSevenHint(serverPlayer);
            tickCurseSevenEndRitual(serverPlayer);
            tickCurseSevenHeartRitual(serverPlayer);
            tickCurseThreeAndFourLiftedHint(serverPlayer);
        }

        if (player.containerMenu instanceof AnvilMenu anvilMenu) {
            syncCurseThreeAnvilCost(player, anvilMenu, sealEquipped);
        } else {
            data.putInt(CURSE_3_ANVIL_COST, 0);
        }

        cleanupTrackedMusicPlaybacks(player.level());
        ModItems.tickMusicRelic(player);

        boolean suppressInversion = player.invulnerableTime > 0;
        if (isHoldingEmberOfDuat(player) && !suppressInversion) {
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

        if (!data.getBoolean(END_AWAKENED) && player.level().dimension() == Level.END) {
            data.putBoolean(END_AWAKENED, true);
            syncClientState(player, true);
        }

        if (!curseSixReversed) {
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
        }

        if (curseSixReversed) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 220, 1, false, false, true));
            if (player instanceof ServerPlayer serverPlayer) {
                grantCurseSevenFlight(serverPlayer);
            }
        } else if (player instanceof ServerPlayer serverPlayer) {
            revokeCurseSevenFlight(serverPlayer);
        }

        if (data.getBoolean(GUILT_SENTENCE_PENDING) && player instanceof ServerPlayer serverPlayer) {
            applyGuiltLock(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ensureStarterSeal(player);
            ensureStarterManual(player);
            ensureEmberRiddle(player);
            syncDynastyPressureSharedData(player, getCurseData(player));
            syncClientState(player, true);
            ModItems.EternalSilenceRebirthTotem.syncCooldown(player);
            AffixKnowledge.sync(player);
        }
    }

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String rawText = event.getRawText() == null ? "" : event.getRawText().trim().toLowerCase(Locale.ROOT);
        if (!CURSE_7_CHAT_KEYWORD.equals(rawText)) {
            return;
        }
        handleCurseSevenKeyword(player);
    }

    public static void handleCurseSevenKeyword(ServerPlayer player) {
        if (player == null) {
            return;
        }
        if (!isSealEquipped(player)) {
            return;
        }

        CompoundTag data = getCurseData(player);
        if (data.getBoolean(CURSE_6_REVERSED)) {
            return;
        }
        if (data.getBoolean(CURSE_6_KEYWORD_UNLOCKED)) {
            return;
        }
        if (!data.getBoolean(CURSE_6_SENTENCE_UNLOCKED)) {
            return;
        }

        data.putBoolean(CURSE_6_KEYWORD_UNLOCKED, true);
        data.putBoolean(CURSE_6_TERMINAL_SOLVED, false);
        data.putBoolean(CURSE_7_PENDING_HINT, true);
        data.putString(CURSE_7_HINT_TEXT, CURSE_7_UI_GUIDANCE);
        data.putString(CURSE_7_HINT_TEXT + "Zh", CURSE_7_UI_GUIDANCE_ZH);
        data.putString(CURSE_7_HINT_STYLE, "gray_shadow");
        data.putLong(CURSE_7_HINT_STARTED_AT, player.level().getGameTime());
        queueCurseSevenHint(player, CURSE_7_UI_GUIDANCE, CURSE_7_UI_GUIDANCE_ZH, "gray_shadow");
        syncClientState(player, true);
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
                        .then(literal("mercy")
                                .requires(source -> source.hasPermission(2))
                                .then(literal("set")
                                        .then(argument("targets", EntityArgument.players())
                                                .then(argument("value", IntegerArgumentType.integer(0))
                                                        .executes(context -> adjustMercy(
                                                                context.getSource(),
                                                                EntityArgument.getPlayers(context, "targets"),
                                                                IntegerArgumentType.getInteger(context, "value"),
                                                                GuiltOperation.SET)))))
                                .then(literal("add")
                                        .then(argument("targets", EntityArgument.players())
                                                .then(argument("value", IntegerArgumentType.integer(0))
                                                        .executes(context -> adjustMercy(
                                                                context.getSource(),
                                                                EntityArgument.getPlayers(context, "targets"),
                                                                IntegerArgumentType.getInteger(context, "value"),
                                                                GuiltOperation.ADD)))))
                                .then(literal("remove")
                                        .then(argument("targets", EntityArgument.players())
                                                .then(argument("value", IntegerArgumentType.integer(0))
                                                        .executes(context -> adjustMercy(
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
                                                .executes(context -> clearEdictCooling(context.getSource(), EntityArgument.getPlayer(context, "target"))))))
                        .then(literal("sandstorm")
                                .requires(source -> source.hasPermission(2))
                                .then(literal("start")
                                        .executes(context -> DuatSandstormManager.start(context.getSource())))
                                .then(literal("stop")
                                        .executes(context -> DuatSandstormManager.stop(context.getSource())))
                                .then(literal("spawn")
                                        .executes(context -> DuatSandstormManager.spawn(context.getSource()))))
                        .then(literal("dynasty_shield")
                                .requires(source -> source.hasPermission(2))
                                .then(literal("on")
                                        .executes(context -> setDynastyShieldEnabled(context.getSource(), true)))
                                .then(literal("off")
                                        .executes(context -> setDynastyShieldEnabled(context.getSource(), false)))
                                .then(literal("affixes")
                                        .then(literal("on")
                                                .executes(context -> setDynastyShieldAffixesEnabled(context.getSource(), true)))
                                        .then(literal("off")
                                                .executes(context -> setDynastyShieldAffixesEnabled(context.getSource(), false))))
                                .then(literal("refill_loaded")
                                        .executes(context -> refillLoadedDynastyShields(context.getSource())))
                                .then(literal("reset_loaded")
                                        .executes(context -> resetLoadedDynastyShields(context.getSource())))
                                .then(literal("set_current")
                                        .then(argument("targets", EntityArgument.entities())
                                                .then(argument("value", IntegerArgumentType.integer(0))
                                                        .executes(context -> setDynastyShieldCurrent(
                                                                context.getSource(),
                                                                EntityArgument.getEntities(context, "targets"),
                                                                IntegerArgumentType.getInteger(context, "value"))))))
                                .then(literal("set_max")
                                        .then(argument("targets", EntityArgument.entities())
                                                .then(argument("value", IntegerArgumentType.integer(1))
                                                        .executes(context -> setDynastyShieldMax(
                                                                context.getSource(),
                                                                EntityArgument.getEntities(context, "targets"),
                                                                IntegerArgumentType.getInteger(context, "value"))))))
                                .then(literal("refill")
                                        .then(argument("targets", EntityArgument.entities())
                                                .executes(context -> refillDynastyShield(
                                                        context.getSource(),
                                                        EntityArgument.getEntities(context, "targets")))))
                                .then(literal("reset")
                                        .then(argument("targets", EntityArgument.entities())
                                                .executes(context -> resetDynastyShield(
                                                        context.getSource(),
                                                        EntityArgument.getEntities(context, "targets"))))))
                        );
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            Player originalPlayer = event.getOriginal();
            boolean restoreCurseSixFlight = getCurseData(originalPlayer).getBoolean(CURSE_6_REVERSED);
            CompoundTag original = event.getOriginal().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
            event.getEntity().getPersistentData().put(Player.PERSISTED_NBT_TAG, original.copy());
            Player newPlayer = event.getEntity();
            if (restoreCurseSixFlight) {
                getCurseData(newPlayer).putBoolean(CURSE_6_FLIGHT_RESTORE_PENDING, true);
            }
            if (isSealEquipped(originalPlayer) && !isCurseNineReversed(originalPlayer)) {
                int debt = getSatietyCrystalCount(newPlayer) * CURSE_9_HUNGER_LOSS;
                newPlayer.getFoodData().setFoodLevel(Math.max(0, 20 - debt));
            }
            if (event.getEntity() instanceof ServerPlayer player) {
                syncBloodForfeitHealthPenalty(player);
                finalizeCurseFiveMercyRevive(player);
                syncDynastyPressureSharedData(player, getCurseData(player));
                syncClientState(player, true);
                ModItems.EternalSilenceRebirthTotem.syncCooldown(player);
                AffixKnowledge.sync(player);
            }
        }
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (new ResourceLocation("minecraft", "chests/ancient_city").equals(event.getName())) {
            LootPool.Builder knowledgePool = LootPool.lootPool()
                    .name("ramesses_curse_dynasty_affix_knowledge")
                    .setRolls(ConstantValue.exactly(1.0F))
                    .when(LootItemRandomChanceCondition.randomChance(0.02F));
            ModItems.AFFIX_KNOWLEDGE_BOOKS.values().forEach(book ->
                    knowledgePool.add(LootItem.lootTableItem(book.get())));
            event.getTable().addPool(knowledgePool.build());
        }

        if (isContainerLoot(event.getName())) {
            LootPool twilightCrystalRingPool = LootPool.lootPool()
                    .name("ramesses_curse_twilight_crystal_ring")
                    .setRolls(ConstantValue.exactly(1.0F))
                    .when(LootItemRandomChanceCondition.randomChance(0.02F))
                    .add(LootItem.lootTableItem(ModItems.TWILIGHT_CRYSTAL_RING.get()))
                    .build();

            event.getTable().addPool(twilightCrystalRingPool);

            LootPool eternalSilenceGranulePool = LootPool.lootPool()
                    .name("ramesses_curse_eternal_silence_granule")
                    .setRolls(ConstantValue.exactly(1.0F))
                    .when(LootItemRandomChanceCondition.randomChance(0.0002F))
                    .add(LootItem.lootTableItem(ModItems.ETERNAL_SILENCE_GRANULE.get())
                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                    .build();

            event.getTable().addPool(eternalSilenceGranulePool);

            LootPool.Builder inscriptionFragmentPool = LootPool.lootPool()
                    .name("ramesses_curse_end_inscription_fragment")
                    .setRolls(ConstantValue.exactly(1.0F))
                    .when(LootItemRandomChanceCondition.randomChance(0.10F))
                    .when(LocationCheck.checkLocation(
                            LocationPredicate.Builder.location().setDimension(Level.END)));
            ModItems.INSCRIPTION_FRAGMENTS.forEach(fragment ->
                    inscriptionFragmentPool.add(LootItem.lootTableItem(fragment.get())));
            event.getTable().addPool(inscriptionFragmentPool.build());
        }

        if (isNetherContainerLoot(event.getName())) {
            LootPool emberPool = LootPool.lootPool()
                    .name("ramesses_curse_ember_of_duat")
                    .setRolls(ConstantValue.exactly(1.0F))
                    .when(LootItemRandomChanceCondition.randomChance(0.05F))
                    .add(LootItem.lootTableItem(ModItems.EMBER_OF_DUAT.get())
                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))))
                    .build();

            event.getTable().addPool(emberPool);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            addDynastyPressure(serverPlayer, DYNASTY_PRESSURE_DIMENSION_CHANGE_AMOUNT);
            ModItems.EternalSilenceRebirthTotem.syncCooldown(serverPlayer);
            AffixKnowledge.sync(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!isCurseNineReversed(player) && getSatietyCrystalCount(player) > 0 && event.getItemStack().isEdible()) {
            event.setCanceled(true);
            return;
        }
        if (isGuiltLocked(player)) {
            event.setCanceled(true);
            return;
        }
        if (isMainHandLocked(player) && event.getHand() == InteractionHand.MAIN_HAND) {
            event.setCanceled(true);
            return;
        }
        if (isDynastyHandSilenced(player, event.getHand())) {
            event.setCanceled(true);
            return;
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
        if (!isCurseNineReversed(player) && getSatietyCrystalCount(player) > 0 && event.getItemStack().isEdible()) {
            event.setCanceled(true);
            return;
        }
        if (isGuiltLocked(player)) {
            event.setCanceled(true);
            return;
        }
        if (isMainHandLocked(player) && event.getHand() == InteractionHand.MAIN_HAND) {
            event.setCanceled(true);
            return;
        }
        if (isDynastyHandSilenced(player, event.getHand())) {
            event.setCanceled(true);
            return;
        }
        if (isLockedInventoryStack(player, event.getItemStack())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (isDynastyHandSilenced(player, event.getHand())) {
            event.setCanceled(true);
            return;
        }
        if (!(event.getTarget() instanceof AbstractVillager villager)) {
            return;
        }
        if (!isSealEquipped(player) || areCurseThreeAndFourLifted(player)) {
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
            return;
        }
        if (isDynastyHandSilenced(player, event.getHand())) {
            event.setCanceled(true);
            return;
        }
        if (isLockedInventoryStack(player, player.getMainHandItem()) && event.getHand() == InteractionHand.MAIN_HAND) {
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
        if (event.getEntity() instanceof Player) {
            event.getDrops().removeIf(drop -> isSealEntity(drop));
            return;
        }

        DamageSource dropSource = event.getSource();
        if (event.getEntity() instanceof WitherBoss
                && dropSource != null
                && dropSource.is(DamageTypes.SONIC_BOOM)
                && dropSource.getEntity() != null
                && dropSource.getEntity().getType() == EntityType.WARDEN) {
            event.getDrops().add(new ItemEntity(
                    event.getEntity().level(),
                    event.getEntity().getX(),
                    event.getEntity().getY(),
                    event.getEntity().getZ(),
                    new ItemStack(ModItems.FISSION_WRAITH_HEART.get())));
        }

        CompoundTag defeatedData = event.getEntity().getPersistentData();
        int rageLevel = defeatedData.getInt(DYNASTY_SHIELD_RAGE_LEVEL);
        if (DynastyAffixData.enabled(DYNASTY_AFFIX_RAGE)
                && rageLevel > 0
                && event.getEntity().getRandom().nextFloat()
                < DynastyAffixData.number(DYNASTY_AFFIX_RAGE, rageLevel, "drop_chance")) {
            event.getDrops().add(new ItemEntity(
                    event.getEntity().level(),
                    event.getEntity().getX(),
                    event.getEntity().getY(),
                    event.getEntity().getZ(),
                    new ItemStack(ModItems.CALMING_TIBIA.get())));
        }
        if (DynastyAffixData.enabled(DYNASTY_AFFIX_MOLTEN)
                && defeatedData.getBoolean(DYNASTY_SHIELD_MOLTEN)
                && event.getEntity().getRandom().nextFloat()
                < DynastyAffixData.number(DYNASTY_AFFIX_MOLTEN, 1, "drop_chance")) {
            event.getDrops().add(new ItemEntity(
                    event.getEntity().level(),
                    event.getEntity().getX(),
                    event.getEntity().getY(),
                    event.getEntity().getZ(),
                    new ItemStack(ModItems.PITCHFIRE_CRYSTAL.get())));
        }
        if (DynastyAffixData.enabled(DYNASTY_AFFIX_BLOOD_FORFEIT)
                && defeatedData.getBoolean(DYNASTY_SHIELD_BLOOD_FORFEIT)
                && event.getEntity().getRandom().nextFloat()
                < DynastyAffixData.number(DYNASTY_AFFIX_BLOOD_FORFEIT, 1, "drop_chance")) {
            event.getDrops().add(new ItemEntity(
                    event.getEntity().level(),
                    event.getEntity().getX(),
                    event.getEntity().getY(),
                    event.getEntity().getZ(),
                    new ItemStack(ModItems.HEART_DEVOURING_REMNANT.get())));
        }
        int clawRageLevel = defeatedData.getInt(DYNASTY_SHIELD_CLAW_RAGE_LEVEL);
        if (DynastyAffixData.enabled(DYNASTY_AFFIX_CLAW_RAGE)
                && clawRageLevel > 0
                && event.getEntity().getRandom().nextFloat()
                < DynastyAffixData.number(DYNASTY_AFFIX_CLAW_RAGE, clawRageLevel, "drop_chance")) {
            event.getDrops().add(new ItemEntity(
                    event.getEntity().level(),
                    event.getEntity().getX(),
                    event.getEntity().getY(),
                    event.getEntity().getZ(),
                    new ItemStack(ModItems.RAVENOUS_CLAW_FRAGMENT.get())));
        }
        int fleshFeastLevel = defeatedData.getInt(DYNASTY_SHIELD_FLESH_FEAST_LEVEL);
        if (DynastyAffixData.enabled(DYNASTY_AFFIX_FLESH_FEAST)
                && fleshFeastLevel > 0
                && event.getEntity().getRandom().nextFloat()
                < DynastyAffixData.number(DYNASTY_AFFIX_FLESH_FEAST, fleshFeastLevel, "drop_chance")) {
            event.getDrops().add(new ItemEntity(
                    event.getEntity().level(),
                    event.getEntity().getX(),
                    event.getEntity().getY(),
                    event.getEntity().getZ(),
                    new ItemStack(ModItems.FAMINE_CORE.get())));
        }
        int miasmaLevel = defeatedData.getInt(DYNASTY_SHIELD_MIASMA_LEVEL);
        if (DynastyAffixData.enabled(DYNASTY_AFFIX_MIASMA)
                && miasmaLevel > 0
                && event.getEntity().getRandom().nextFloat()
                < DynastyAffixData.number(DYNASTY_AFFIX_MIASMA, miasmaLevel, "drop_chance")) {
            event.getDrops().add(new ItemEntity(
                    event.getEntity().level(),
                    event.getEntity().getX(),
                    event.getEntity().getY(),
                    event.getEntity().getZ(),
                    new ItemStack(ModItems.VENOM_ORB.get())));
        }
        int restorationLevel = defeatedData.getInt(DYNASTY_SHIELD_RESTORATION_LEVEL);
        if (DynastyAffixData.enabled(DYNASTY_AFFIX_RESTORATION)
                && restorationLevel > 0
                && event.getEntity().getRandom().nextFloat()
                < DynastyAffixData.number(DYNASTY_AFFIX_RESTORATION, restorationLevel, "drop_chance")) {
            event.getDrops().add(new ItemEntity(
                    event.getEntity().level(),
                    event.getEntity().getX(),
                    event.getEntity().getY(),
                    event.getEntity().getZ(),
                    new ItemStack(ModItems.KA_REMNANT_BREATH.get())));
        }
        if (DynastyAffixData.enabled(DYNASTY_AFFIX_BLOOD_LIMIT)
                && defeatedData.getBoolean(DYNASTY_SHIELD_BLOOD_LIMIT)
                && event.getEntity().getRandom().nextFloat()
                < DynastyAffixData.number(DYNASTY_AFFIX_BLOOD_LIMIT, 1, "drop_chance")) {
            event.getDrops().add(new ItemEntity(
                    event.getEntity().level(),
                    event.getEntity().getX(),
                    event.getEntity().getY(),
                    event.getEntity().getZ(),
                    new ItemStack(ModItems.LIFE_BINDING_REMNANT_CRYSTAL.get())));
        }
        if (DynastyAffixData.enabled(DYNASTY_AFFIX_DEATH_AUTHORITY)
                && defeatedData.getBoolean(DYNASTY_SHIELD_DEATH_AUTHORITY)
                && event.getEntity().getRandom().nextFloat()
                < DynastyAffixData.number(DYNASTY_AFFIX_DEATH_AUTHORITY, 1, "drop_chance")) {
            event.getDrops().add(new ItemEntity(
                    event.getEntity().level(),
                    event.getEntity().getX(),
                    event.getEntity().getY(),
                    event.getEntity().getZ(),
                    new ItemStack(ModItems.AUTHORITY_FRAGMENT.get())));
        }
        int vulnerabilityLevel = defeatedData.getInt(DYNASTY_SHIELD_VULNERABILITY_LEVEL);
        if (DynastyAffixData.enabled(DYNASTY_AFFIX_VULNERABILITY)
                && vulnerabilityLevel > 0
                && event.getEntity().getRandom().nextFloat()
                < DynastyAffixData.number(DYNASTY_AFFIX_VULNERABILITY, vulnerabilityLevel, "drop_chance")) {
            event.getDrops().add(new ItemEntity(
                    event.getEntity().level(),
                    event.getEntity().getX(),
                    event.getEntity().getY(),
                    event.getEntity().getZ(),
                    new ItemStack(ModItems.TRUTH.get())));
        }
        if (DynastyAffixData.enabled(DYNASTY_AFFIX_COUNTER)
                && defeatedData.getBoolean(DYNASTY_SHIELD_COUNTER)
                && event.getEntity().getRandom().nextFloat()
                < DynastyAffixData.number(DYNASTY_AFFIX_COUNTER, 1, "drop_chance")) {
            event.getDrops().add(new ItemEntity(
                    event.getEntity().level(),
                    event.getEntity().getX(),
                    event.getEntity().getY(),
                    event.getEntity().getZ(),
                    new ItemStack(ModItems.REVERSE_SCALE.get())));
        }

        List<ItemEntity> affixMaterialDrops = event.getDrops().stream()
                .filter(drop -> drop != null && drop.isAlive() && isDynastyAffixMaterial(drop.getItem()))
                .toList();
        if (affixMaterialDrops.size() > 1) {
            ItemEntity retained = affixMaterialDrops.get(event.getEntity().getRandom().nextInt(affixMaterialDrops.size()));
            event.getDrops().removeIf(drop -> drop != null
                    && isDynastyAffixMaterial(drop.getItem())
                    && drop != retained);
        }

        DamageSource source = event.getSource();
        ServerPlayer player = source != null && source.getEntity() instanceof ServerPlayer serverPlayer ? serverPlayer : null;

        if (player == null || !isCurseSevenReversed(player)) {
            return;
        }

        List<ItemEntity> doubledDrops = new ArrayList<>();
        for (ItemEntity drop : event.getDrops()) {
            if (drop == null || !drop.isAlive()) {
                continue;
            }
            ItemStack stack = drop.getItem();
            if (stack.isEmpty()) {
                continue;
            }
            ItemEntity duplicate = new ItemEntity(
                    drop.level(),
                    drop.getX(),
                    drop.getY(),
                    drop.getZ(),
                    stack.copy());
            duplicate.setPickUpDelay(10);
            duplicate.setDeltaMovement(drop.getDeltaMovement());
            doubledDrops.add(duplicate);
        }
        event.getDrops().addAll(doubledDrops);
    }

    private static boolean isDynastyAffixMaterial(ItemStack stack) {
        return stack.is(ModItems.CALMING_TIBIA.get())
                || stack.is(ModItems.PITCHFIRE_CRYSTAL.get())
                || stack.is(ModItems.HEART_DEVOURING_REMNANT.get())
                || stack.is(ModItems.RAVENOUS_CLAW_FRAGMENT.get())
                || stack.is(ModItems.FAMINE_CORE.get())
                || stack.is(ModItems.VENOM_ORB.get())
                || stack.is(ModItems.KA_REMNANT_BREATH.get())
                || stack.is(ModItems.LIFE_BINDING_REMNANT_CRYSTAL.get())
                || stack.is(ModItems.AUTHORITY_FRAGMENT.get())
                || stack.is(ModItems.TRUTH.get())
                || stack.is(ModItems.REVERSE_SCALE.get());
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (isGuiltLocked(player) || isMainHandLocked(player) || isDynastyHandSilenced(player, InteractionHand.MAIN_HAND) || isLockedInventoryStack(player, player.getMainHandItem())) {
                event.setCanceled(true);
                return;
            }
        }
        if (event.getEntity().level().isClientSide || !(event.getTarget() instanceof LivingEntity target)) {
            return;
        }

        Player player = event.getEntity();

        if (ModItems.ChanceDieSixSided.hasTrueDamage(player)
                && target.isAlive()
                && !(target instanceof Player)) {
            float damage = (float) Math.max(0.0D, player.getAttributeValue(Attributes.ATTACK_DAMAGE)
                    * Math.max(0.1F, player.getAttackStrengthScale(0.5F)));
            if (damage > 0.0F) {
                target.hurt(ModDamageTypes.chanceDieTrueDamage(player.level(), player), damage);
                event.setCanceled(true);
            }
            return;
        }

        if (ModItems.ChanceDieSixSided.hasHealthDamage(player)
                && target instanceof Enemy
                && target.isAlive()) {
            target.hurt(player.damageSources().playerAttack(player), player.getMaxHealth() * 0.35F);
        }

        if (canUseHeartSlot(player) && isHeartEquipped(player) && target instanceof Enemy) {
            ModItems.onHeartOfDuatAttack(player, target, 0.0F);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCurseSevenLootSuppression(LivingDropsEvent event) {
        if (event.getEntity() instanceof Player) {
            return;
        }
        Player sourcePlayer = getSourcePlayer(event.getSource());
        if (!(sourcePlayer instanceof ServerPlayer player)
                || !isSealEquipped(player)
                || !getCurseData(player).getBoolean(END_AWAKENED)
                || isCurseSevenReversed(player)) {
            return;
        }
        if (player.getRandom().nextDouble() < Config.CURSE_7_LOOT_SUPPRESSION_CHANCE.get()) {
            event.getDrops().clear();
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            restorePendingCurseSixFlight(player, getCurseData(player));
            AffixKnowledge.sync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            cancelCurseSevenHeartRitual(player, getCurseData(player));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void enforceDynastyFlightFall(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
            tickDynastyFlightFall(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTwilightCrystalRingMeleeHit(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        if (source.getEntity() instanceof ServerPlayer player
                && source.getDirectEntity() == player
                && !player.level().isClientSide
                && event.getAmount() > 0.0F) {
            ModItems.TwilightCrystalRing.onMeleeHit(player, event.getEntity());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEternalSilenceTotemMeleeHit(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)
                || source.getDirectEntity() != player
                || player.level().isClientSide
                || event.getAmount() <= 0.0F
                || !ModItems.EternalSilenceTotem.isEquipped(player)) {
            return;
        }

        LivingEntity target = event.getEntity();
        UUID targetId = target.getUUID();
        if (ETERNAL_TOTEM_SECOND_HIT_GUARD.contains(targetId)) {
            return;
        }

        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        float secondHitDamage = event.getAmount();
        server.execute(() -> {
            if (!player.isAlive()
                    || !target.isAlive()
                    || player.level() != target.level()
                    || !ModItems.EternalSilenceTotem.isEquipped(player)
                    || !ETERNAL_TOTEM_SECOND_HIT_GUARD.add(targetId)) {
                return;
            }
            try {
                target.invulnerableTime = 0;
                target.hurt(source, secondHitDamage);
            } finally {
                ETERNAL_TOTEM_SECOND_HIT_GUARD.remove(targetId);
            }
        });
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel serverLevel) {
            restoreExpiredMoltenBlocks(serverLevel);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMoltenFireballImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof SmallFireball fireball)
                || fireball.level().isClientSide
                || !fireball.getPersistentData().getBoolean(DYNASTY_MOLTEN_FIREBALL)) {
            return;
        }

        event.setCanceled(true);
        if (event.getRayTraceResult() instanceof EntityHitResult hitResult) {
            Entity target = hitResult.getEntity();
            Entity owner = fireball.getOwner();
            if (target instanceof LivingEntity livingTarget
                    && target != owner
                    && !isDynastyMoltenActive(livingTarget)
                    && (!(livingTarget instanceof Player player)
                    || !AffixKnowledge.hasLearned(player, DYNASTY_AFFIX_MOLTEN))) {
                applyDynastyMoltenBurn(livingTarget, owner instanceof LivingEntity livingOwner ? livingOwner : null);
            }
        }
        fireball.discard();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAffixKnowledgeProjectileImpact(ProjectileImpactEvent event) {
        if (event.getProjectile().level().isClientSide
                || !(event.getProjectile().getOwner() instanceof Player player)
                || !AffixKnowledge.hasLearned(player, DYNASTY_AFFIX_PURGE)
                || !(event.getRayTraceResult() instanceof EntityHitResult hitResult)
                || !(hitResult.getEntity() instanceof LivingEntity target)
                || !(target instanceof Enemy)) {
            return;
        }
        target.getPersistentData().putLong(
                "RamessesDynastyPurgeKnowledgeBypassUntil",
                target.level().getGameTime() + 100L);
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity living = event.getEntity();
        if (living.level().isClientSide) {
            return;
        }

        if (isDynastyMoltenActive(living)) {
            living.clearFire();
        }
        tickDynastyMoltenBurn(living);

        tickEdictSeal(living);
        if (living.tickCount % 40 == 0) {
            ensureDynastyShield(living, false);
            cleanseDynastyShieldHarmfulEffects(living);
        }
        if (living.tickCount % 20 == 0 && living instanceof Enemy) {
            tickDynastyFleshFeast(living);
        }

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
    public static void onMobEffectApplicable(MobEffectEvent.Applicable event) {
        LivingEntity living = event.getEntity();
        MobEffectInstance effect = event.getEffectInstance();
        if (effect != null
                && isDynastyShieldPurgeActive(living)
                && effect.getEffect().getCategory() == MobEffectCategory.HARMFUL
                && !isUraeusNecklaceBypassEffect(living, effect.getEffect())) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        Player affixKnowledgeAttacker = getSourcePlayer(event.getSource());
        if (affixKnowledgeAttacker != null
                && AffixKnowledge.hasLearned(affixKnowledgeAttacker, DYNASTY_AFFIX_PURGE)
                && event.getEntity() instanceof Enemy) {
            event.getEntity().getPersistentData().putLong(
                    "RamessesDynastyPurgeKnowledgeBypassUntil",
                    event.getEntity().level().getGameTime() + 100L);
        }
        if (isDynastyMoltenBurnActive(event.getEntity()) && event.getSource().is(DamageTypeTags.IS_FIRE)) {
            event.setCanceled(true);
            return;
        }
        if (isDynastyMoltenActive(event.getEntity()) && event.getSource().is(DamageTypeTags.IS_FIRE)) {
            event.setCanceled(true);
            event.getEntity().clearFire();
            return;
        }
        if (isDynastyShieldProjectileWardBlockedDamage(event.getEntity(), event.getSource())) {
            event.setCanceled(true);
            return;
        }
        if (event.getEntity() instanceof ServerPlayer player) {
            if (ModItems.ChanceDieSixSided.hasFallImmunity(player)
                    && event.getSource().is(DamageTypes.FALL)) {
                event.setCanceled(true);
                return;
            }
            if (ModItems.ChanceDieSixSided.hasDodge(player)
                    && player.getRandom().nextFloat() < 0.25F) {
                event.setCanceled(true);
                return;
            }
            if (ModItems.EternalSilenceTotem.isRevivalInvulnerable(player)) {
                event.setCanceled(true);
                return;
            }
            tryTriggerCurseOneReversed(player, event.getSource());
            if (isCurseFiveMercyInvulnerable(player) || isCurseFiveFatalGuardInvulnerable(player)) {
                event.setCanceled(true);
                return;
            }
        }
        if (isCurseTwoMinionFriendlyFire(event.getSource(), event.getEntity())) {
            event.setCanceled(true);
            return;
        }
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
        if (isDynastyMoltenBurnActive(event.getEntity()) && event.getSource().is(DamageTypeTags.IS_FIRE)) {
            event.setCanceled(true);
            event.setAmount(0.0F);
            return;
        }
        if (isDynastyMoltenActive(event.getEntity()) && event.getSource().is(DamageTypeTags.IS_FIRE)) {
            event.setCanceled(true);
            event.setAmount(0.0F);
            event.getEntity().clearFire();
            return;
        }
        if (isDynastyShieldProjectileWardBlockedDamage(event.getEntity(), event.getSource())) {
            event.setCanceled(true);
            event.setAmount(0.0F);
            return;
        }
        applyReversedCurseSevenBowDamage(event);
        if (isCurseTwoMinionDotOrPotionDamage(event.getEntity(), event.getSource())) {
            event.setCanceled(true);
            event.setAmount(0.0F);
            return;
        }
        if (isCurseTwoMinionFriendlyFire(event.getSource(), event.getEntity())) {
            event.setCanceled(true);
            event.setAmount(0.0F);
            return;
        }
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

        if (event.getEntity() instanceof ServerPlayer player
                && ModItems.EternalSilenceTotem.isRevivalInvulnerable(player)) {
            event.setCanceled(true);
            event.setAmount(0.0F);
            return;
        }

        if (event.getEntity() instanceof ServerPlayer player
                && (isCurseFiveMercyInvulnerable(player) || isCurseFiveFatalGuardInvulnerable(player))) {
            event.setCanceled(true);
            event.setAmount(0.0F);
            return;
        }

        if (!event.isCanceled()
                && !event.getSource().is(ModDamageTypes.DEATH_AUTHORITY)
                && !event.getSource().is(ModDamageTypes.VULNERABILITY)
                && event.getEntity() instanceof ServerPlayer player) {
            float adjusted = InscriptionBagItem.modifyIncomingDamage(player, event.getSource(), event.getAmount());
            event.setAmount(adjusted);
            if (adjusted <= 0.0F) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof LivingEntity living) {
            ensureDynastyShield(living, true);
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getTarget() instanceof LivingEntity living) {
            syncDynastyShieldToPlayer(living, player, false);
        }
    }

    @SubscribeEvent
    public static void onLivingEntityUseItemFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!isSealEquipped(player) || isCurseNineReversed(player)) {
            return;
        }

        ItemStack stack = event.getItem();
        if (stack.isEmpty() || !stack.isEdible()) {
            return;
        }

        FoodProperties food = stack.getFoodProperties(player);
        if (food == null || food.getNutrition() <= 0) {
            return;
        }

        if (!getCurseData(player).getBoolean(CURSE_9_KILL_UNLOCKED)) {
            return;
        }

        if (tryAdvanceCurseNineFoodVariety(player, stack)) {
            player.displayClientMessage(Component.literal("Satiety king penalty reversed.").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), false);
            syncClientState(player, true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamageFinal(LivingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (event.getSource().is(ModDamageTypes.CHANCE_DIE_TRUE_DAMAGE)) {
            return;
        }
        if (tryBlockDynastySteelBoneDamage(event)) {
            return;
        }
        event.setAmount(consumeDynastyClawRageBonus(target, event.getSource(), event.getAmount()));
        tryArmDynastyClawRage(target, event.getSource(), event.getAmount());
        if (event.getSource().is(ModDamageTypes.SEAL_ASSIST)) {
            absorbDynastyShieldDamage(event);
            applyDynastyBloodLimit(event);
            return;
        }
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
                && !player.level().isClientSide) {
            event.setAmount(ModItems.onUraeusNecklaceAttack(player, target, event.getAmount()));
        }

        Player player = getSourcePlayer(event.getSource());
        if (player != null && !player.level().isClientSide) {
            CompoundTag data = getCurseData(player);
            if (isSealEquipped(player) && data.getBoolean(NETHER_AWAKENED)) {
                long gameTime = player.level().getGameTime();
                if (data.getLong(CURSE_8_REBOUND_COOLDOWN_UNTIL) <= gameTime
                        && player.getRandom().nextDouble() < Config.HELL_REBOUND_CHANCE.get()) {
                    data.putLong(CURSE_8_REBOUND_COOLDOWN_UNTIL, gameTime + Config.HELL_REBOUND_COOLDOWN.get());
                    if (data.getBoolean(CURSE_8_REVERSED)) {
                        float assistDamage = Math.min(Config.HELL_REVERSED_ASSIST_DAMAGE_CAP.get().floatValue(), event.getAmount());
                        if (assistDamage > 0.0F && player instanceof ServerPlayer serverPlayer && serverPlayer.getServer() != null) {
                            serverPlayer.getServer().execute(() -> {
                                if (target.isAlive() && target.level() == serverPlayer.level()) {
                                    target.hurt(ModDamageTypes.sealAssist(target.level(), serverPlayer), assistDamage);
                                }
                            });
                        }
                        player.displayClientMessage(Component.translatable("message.ramesses_curse.curse_8_assist", String.format(java.util.Locale.ROOT, "%.1f", assistDamage))
                                .withStyle(net.minecraft.ChatFormatting.GREEN), true);
                        player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7F, 1.2F);
                    } else {
                    float rebound = (float) Math.max(1.0D, event.getAmount() * Config.HELL_REBOUND_DAMAGE_MULTIPLIER.get() + Config.HELL_REBOUND_FLAT_DAMAGE.get());
                    rebound = Math.min(Config.HELL_REBOUND_DAMAGE_CAP.get().floatValue(), rebound);
                    player.hurt(ModDamageTypes.sealBacklash(player.level()), rebound);
                    player.displayClientMessage(Component.translatable("message.ramesses_curse.hell_rebound", String.format(java.util.Locale.ROOT, "%.1f", rebound))
                            .withStyle(net.minecraft.ChatFormatting.RED), true);
                    player.level().playSound(null, player.blockPosition(), SoundEvents.BLAZE_HURT, SoundSource.PLAYERS, 0.85F, 0.75F);
                    }
                }
            }
        }

        ServerPlayer inscriptionAttacker = event.getSource().getEntity() instanceof ServerPlayer serverPlayer
                ? serverPlayer
                : null;
        if (inscriptionAttacker != null && !event.isCanceled()) {
            event.setAmount(InscriptionBagItem.modifyOutgoingDamage(
                    inscriptionAttacker, target, event.getSource(), event.getAmount()));
        }

        tryCorrodeDynastyShieldEquipment(event);
        absorbDynastyShieldDamage(event);
        applyDynastyBloodLimit(event);
        if (inscriptionAttacker != null && !event.isCanceled()) {
            InscriptionBagItem.afterDamageDealt(
                    inscriptionAttacker, target, event.getSource(), event.getAmount());
        }
        if (!event.isCanceled() && event.getAmount() > 0.0F && target instanceof ServerPlayer struckPlayer) {
            tryTriggerDynastyFlightFall(struckPlayer, event.getSource());
            tryTriggerDynastyDeathAuthority(struckPlayer, event.getSource());
            tryTriggerDynastyVulnerability(struckPlayer, event.getSource());
        }
        applyCurseFiveFatalGuard(event);
    }

    private static void applyCurseFiveFatalGuard(LivingDamageEvent event) {
        if (event.isCanceled() || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (isCurseFiveMercyInvulnerable(player) || isCurseFiveFatalGuardInvulnerable(player)) {
            event.setCanceled(true);
            event.setAmount(0.0F);
            return;
        }

        float lethalThreshold = player.getHealth() + player.getAbsorptionAmount();
        if (event.getAmount() >= lethalThreshold && tryTriggerCurseFiveFatalGuard(player)) {
            event.setCanceled(true);
            event.setAmount(0.0F);
        }
    }

    private static void applyReversedCurseSevenBowDamage(LivingDamageEvent event) {
        Entity direct = event.getSource().getDirectEntity();
        if (!ModItems.EternalSilenceCompoundBow.isSpecialProjectile(direct)
                || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || !isCurseSevenReversed(player)
                || event.getAmount() <= 0.0F) {
            return;
        }
        LivingEntity target = event.getEntity();
        CompoundTag data = target.getPersistentData();
        String guardKey = "RamessesEternalSilenceBowTrueDamageGuard";
        if (data.getBoolean(guardKey)) {
            return;
        }
        float extraDamage = event.getAmount() * 0.5F;
        if (extraDamage <= 0.0F) {
            return;
        }
        data.putBoolean(guardKey, true);
        try {
            target.hurt(ModDamageTypes.vulnerability(target.level(), player), extraDamage);
        } finally {
            data.remove(guardKey);
        }
    }

    private static void tryTriggerDynastyDeathAuthority(ServerPlayer target, DamageSource source) {
        if (AffixKnowledge.hasLearned(target, DYNASTY_AFFIX_DEATH_AUTHORITY)
                || source.is(ModDamageTypes.DEATH_AUTHORITY)
                || source.is(ModDamageTypes.VULNERABILITY)
                || !(source.getEntity() instanceof LivingEntity attacker)
                || attacker.level().isClientSide
                || !(attacker instanceof Enemy)
                || !attacker.isAlive()
                || !DynastyAffixData.enabled(DYNASTY_AFFIX_DEATH_AUTHORITY)) {
            return;
        }

        CompoundTag tag = attacker.getPersistentData();
        if (!tag.getBoolean(DYNASTY_SHIELD_ASSIGNED)
                || tag.getFloat(DYNASTY_SHIELD_CURRENT) <= 0.0F
                || !tag.getBoolean(DYNASTY_SHIELD_DEATH_AUTHORITY)) {
            return;
        }

        long gameTime = attacker.level().getGameTime();
        if (gameTime < tag.getLong(DYNASTY_DEATH_AUTHORITY_READY_AT)) {
            return;
        }

        int cooldownTicks = Math.max(0,
                DynastyAffixData.intNumber(DYNASTY_AFFIX_DEATH_AUTHORITY, 1, "cooldown_ticks"));
        float damage = Math.max(0.0F, target.getMaxHealth()
                * (float) DynastyAffixData.number(DYNASTY_AFFIX_DEATH_AUTHORITY, 1, "damage_ratio"));
        if (damage <= 0.0F) {
            return;
        }

        tag.putLong(DYNASTY_DEATH_AUTHORITY_READY_AT, gameTime + cooldownTicks);
        if (target.getServer() != null) {
            target.getServer().execute(() -> {
                if (target.isAlive() && target.level() == attacker.level()) {
                    target.hurt(ModDamageTypes.deathAuthority(target.level(), attacker), damage);
                }
            });
        }
    }

    private static void tryTriggerDynastyVulnerability(ServerPlayer target, DamageSource source) {
        if (AffixKnowledge.hasLearned(target, DYNASTY_AFFIX_VULNERABILITY)
                || source.is(ModDamageTypes.DEATH_AUTHORITY)
                || source.is(ModDamageTypes.VULNERABILITY)
                || !(source.getEntity() instanceof LivingEntity attacker)
                || attacker.level().isClientSide
                || !(attacker instanceof Enemy)
                || !attacker.isAlive()
                || !DynastyAffixData.enabled(DYNASTY_AFFIX_VULNERABILITY)) {
            return;
        }

        CompoundTag tag = attacker.getPersistentData();
        int level = tag.getInt(DYNASTY_SHIELD_VULNERABILITY_LEVEL);
        if (!tag.getBoolean(DYNASTY_SHIELD_ASSIGNED)
                || tag.getFloat(DYNASTY_SHIELD_CURRENT) <= 0.0F
                || level <= 0) {
            return;
        }

        float damage = Math.max(0.0F,
                (float) DynastyAffixData.number(DYNASTY_AFFIX_VULNERABILITY, level, "true_damage"));
        if (damage <= 0.0F || target.getServer() == null) {
            return;
        }
        target.getServer().execute(() -> {
            if (target.isAlive() && target.level() == attacker.level()) {
                target.hurt(ModDamageTypes.vulnerability(target.level(), attacker), damage);
            }
        });
    }

    private static void tryArmDynastyClawRage(LivingEntity target, DamageSource source, float incomingDamage) {
        if (incomingDamage <= 0.0F
                || target.level().isClientSide
                || !(target instanceof Enemy)
                || source.getEntity() == null
                || !DynastyAffixData.enabled(DYNASTY_AFFIX_CLAW_RAGE)) {
            return;
        }
        Player sourcePlayer = getSourcePlayer(source);
        if (sourcePlayer != null && AffixKnowledge.hasLearned(sourcePlayer, DYNASTY_AFFIX_CLAW_RAGE)) {
            return;
        }
        CompoundTag tag = target.getPersistentData();
        int level = tag.getInt(DYNASTY_SHIELD_CLAW_RAGE_LEVEL);
        if (!tag.getBoolean(DYNASTY_SHIELD_ASSIGNED)
                || tag.getFloat(DYNASTY_SHIELD_CURRENT) <= 0.0F
                || level <= 0
                || tag.getBoolean(DYNASTY_CLAW_RAGE_ARMED)) {
            return;
        }
        if (target.getRandom().nextDouble()
                < DynastyAffixData.number(DYNASTY_AFFIX_CLAW_RAGE, level, "trigger_chance")) {
            tag.putBoolean(DYNASTY_CLAW_RAGE_ARMED, true);
            target.level().playSound(null, target.blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 0.45F, 1.45F);
        }
    }

    private static float consumeDynastyClawRageBonus(LivingEntity target, DamageSource source, float damage) {
        if (damage <= 0.0F
                || !(source.getEntity() instanceof LivingEntity attacker)
                || attacker.level().isClientSide
                || !(attacker instanceof Enemy)) {
            return damage;
        }
        if (target instanceof Player player && AffixKnowledge.hasLearned(player, DYNASTY_AFFIX_CLAW_RAGE)) {
            return damage;
        }
        CompoundTag tag = attacker.getPersistentData();
        int level = tag.getInt(DYNASTY_SHIELD_CLAW_RAGE_LEVEL);
        if (!DynastyAffixData.enabled(DYNASTY_AFFIX_CLAW_RAGE)
                || level <= 0
                || !tag.getBoolean(DYNASTY_CLAW_RAGE_ARMED)) {
            return damage;
        }
        tag.remove(DYNASTY_CLAW_RAGE_ARMED);
        float bonus = (float) DynastyAffixData.number(DYNASTY_AFFIX_CLAW_RAGE, level, "bonus_damage");
        attacker.level().playSound(null, attacker.blockPosition(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.HOSTILE, 0.8F, 0.7F);
        return damage + Math.max(0.0F, bonus);
    }

    @SubscribeEvent
    public static void onLivingExperienceDrop(LivingExperienceDropEvent event) {
        if (!(event.getAttackingPlayer() instanceof ServerPlayer player)) {
            return;
        }
        if (event.getEntity() instanceof Enemy) {
            event.setDroppedExperience(InscriptionBagItem.modifyExperience(player, event.getDroppedExperience()));
        }
        if (!isSealEquipped(player)) {
            return;
        }
        double multiplier = areCurseThreeAndFourLifted(player) ? 1.0D : Config.CURSE_3_XP_MULTIPLIER.get();
        if (isCurseFiveDeclinedEmpowered(player)) {
            multiplier *= 2.0D;
        }
        if (isCurseNinePenaltyActive(player)) {
            multiplier *= 0.35D;
        }
        event.setDroppedExperience(Math.max(0, Mth.floor(event.getDroppedExperience() * multiplier)));
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
        if (areCurseThreeAndFourLifted(player)) {
            clearCurseThreeAnvilCosts(player);
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        Player player = event.getPlayer();
        if (!isSealEquipped(player)) {
            clearCurseThreeAnvilCosts(player);
            return;
        }
        if (areCurseThreeAndFourLifted(player)) {
            clearCurseThreeAnvilCosts(player);
            return;
        }
        int baseCost = Math.max(0, event.getCost());
        int adjustedCost = Math.max(0, Mth.ceil(baseCost * Config.CURSE_3_ANVIL_COST_MULTIPLIER.get()));
        event.setCost(adjustedCost);
        storeCurseThreeAnvilCosts(player, baseCost, adjustedCost);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEternalSilenceTotemDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && isCurseFiveReversed(player)
                && (isCurseFiveMercyInvulnerable(player) || isCurseFiveFatalGuardInvulnerable(player))) {
            player.setHealth(Math.min(1.0F, player.getMaxHealth()));
            event.setCanceled(true);
            return;
        }
        if (event.getEntity() instanceof ServerPlayer player
                && tryTriggerCurseFiveFatalGuard(player)) {
            event.setCanceled(true);
            return;
        }
        if (event.getEntity() instanceof ServerPlayer player
                && ModItems.ChanceDieSixSided.tryRevive(player)) {
            event.setCanceled(true);
            return;
        }
        if (event.getEntity() instanceof ServerPlayer player
                && ModItems.EternalSilenceTotem.tryRevive(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void prioritizeEternalSilenceTotem(LivingUseTotemEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && event.getTotem().is(Items.TOTEM_OF_UNDYING)
                && ModItems.EternalSilenceTotem.canRevive(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (tryReviveDynastyShieldEntity(event)) {
            return;
        }

        tryTriggerDynastyRestoration(event.getEntity(), event.getSource());

        if (event.getEntity() instanceof ServerPlayer fallenPlayer && isSealEquipped(fallenPlayer) && !isCurseNineReversed(fallenPlayer)) {
            addSatietyCrystalCount(fallenPlayer, 1);
            CompoundTag data = getCurseData(fallenPlayer);
            data.putInt(SATIETY_CRYSTAL_PENDING_ITEMS, data.getInt(SATIETY_CRYSTAL_PENDING_ITEMS) + 1);
            syncClientState(fallenPlayer, true);
        }

        if (event.getEntity() instanceof ServerPlayer fallenPlayer) {
            applyBloodForfeitOnPlayerDeath(fallenPlayer, event.getSource());
        }

        if (!(getSourcePlayer(event.getSource()) instanceof ServerPlayer player)) {
            return;
        }

        InscriptionBagItem.onKill(player, event.getEntity());

        if (event.getEntity().getPersistentData().getBoolean(DYNASTY_SHIELD_ASSIGNED)) {
            reduceDynastyPressure(player, 1);
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

        if (isSealEquipped(player) && !isCurseNineReversed(player) && Config.isCurseNineReversedTarget(event.getEntity().getType())) {
            CompoundTag data = getCurseData(player);
            if (!data.getBoolean(CURSE_9_KILL_UNLOCKED)) {
                data.putBoolean(CURSE_9_KILL_UNLOCKED, true);
                syncClientState(player, true);
            }
        }

        if (!isSealEquipped(player) || !isInnocentVictim(event.getEntity())) {
            if (event.getEntity() instanceof Enemy) {
                tryAdvanceCurseFiveMercy(player);
            }
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

    public static boolean isEndAwakened(Player player) {
        if (player == null) {
            return false;
        }
        if (player.level().isClientSide) {
            return ClientOverlayEvents.isClientEndAwakened();
        }
        return getCurseData(player).getBoolean(END_AWAKENED);
    }

    public static boolean isCurseSevenKeywordUnlocked(Player player) {
        if (player == null) {
            return false;
        }
        if (player.level().isClientSide) {
            return ClientOverlayEvents.isClientCurseSevenKeywordUnlocked();
        }
        return getCurseData(player).getBoolean(CURSE_6_KEYWORD_UNLOCKED);
    }

    public static boolean isCurseSevenSentenceUnlocked(Player player) {
        if (player == null) {
            return false;
        }
        if (player.level().isClientSide) {
            return ClientOverlayEvents.isClientCurseSevenSentenceUnlocked();
        }
        return getCurseData(player).getBoolean(CURSE_6_SENTENCE_UNLOCKED);
    }

    public static boolean isCurseSevenReversed(Player player) {
        if (player == null) {
            return false;
        }
        if (player.level().isClientSide) {
            return ClientOverlayEvents.isClientCurseSevenReversed();
        }
        return getCurseData(player).getBoolean(CURSE_7_REVERSED);
    }

    public static boolean isCurseSixReversed(Player player) {
        if (player == null) {
            return false;
        }
        if (player.level().isClientSide) {
            return ClientOverlayEvents.isClientCurseSixReversed();
        }
        return getCurseData(player).getBoolean(CURSE_6_REVERSED);
    }

    public static boolean isCurseSevenTerminalSolved(Player player) {
        if (player == null) {
            return false;
        }
        if (player.level().isClientSide) {
            return ClientOverlayEvents.isClientCurseSevenTerminalSolved();
        }
        return getCurseData(player).getBoolean(CURSE_6_TERMINAL_SOLVED);
    }

    public static boolean isLegacyCurseSevenReversed(Player player) {
        if (player == null) {
            return false;
        }
        if (player.level().isClientSide) {
            return getCurseData(player).getBoolean(CURSE_7_REVERSED);
        }
        return getCurseData(player).getBoolean(CURSE_7_REVERSED);
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

    public static boolean isCurseOneReversed(Player player) {
        if (player == null) {
            return false;
        }
        if (player.level().isClientSide) {
            return ClientOverlayEvents.isClientCurseOneReversed();
        }
        return getCurseData(player).getBoolean(CURSE_1_REVERSED);
    }

    public static boolean isCurseTwoReversed(Player player) {
        if (player == null) {
            return false;
        }
        if (player.level().isClientSide) {
            return ClientOverlayEvents.isClientCurseTwoReversed();
        }
        return getCurseData(player).getBoolean(CURSE_2_REVERSED);
    }

    public static double getCurseTwoLuckModifier(Player player) {
        if (player == null) {
            return 0.0D;
        }

        CompoundTag data = getCurseData(player);
        int equipTicks = Math.max(0, data.getInt(EQUIP_TICKS));
        if (!data.getBoolean(CURSE_2_REVERSED)) {
            int penaltySteps = equipTicks / CURSE_2_LUCK_INTERVAL_TICKS;
            return -penaltySteps * Config.CURSE_2_LUCK_PENALTY.get();
        }

        ensureCurseTwoLuckReversalSnapshot(data, equipTicks);
        int reversedAt = Math.max(0, data.getInt(CURSE_2_REVERSED_AT_EQUIP_TICKS));
        int bonusSteps = Math.max(0, equipTicks - reversedAt) / CURSE_2_LUCK_INTERVAL_TICKS;
        return data.getDouble(CURSE_2_RETURNED_LUCK)
                + bonusSteps * Config.CURSE_2_REVERSED_LUCK_BONUS.get();
    }

    private static void ensureCurseTwoLuckReversalSnapshot(CompoundTag data, int equipTicks) {
        if (data.contains(CURSE_2_REVERSED_AT_EQUIP_TICKS, Tag.TAG_ANY_NUMERIC)
                && data.contains(CURSE_2_RETURNED_LUCK, Tag.TAG_ANY_NUMERIC)) {
            return;
        }

        int completedSteps = Math.max(0, equipTicks) / CURSE_2_LUCK_INTERVAL_TICKS;
        data.putInt(CURSE_2_REVERSED_AT_EQUIP_TICKS, Math.max(0, equipTicks));
        data.putDouble(CURSE_2_RETURNED_LUCK, completedSteps * Config.CURSE_2_LUCK_PENALTY.get());
    }

    public static boolean isCurseFiveReversed(Player player) {
        if (player == null) {
            return false;
        }
        if (player.level().isClientSide) {
            return ClientOverlayEvents.isClientCurseFiveReversed();
        }
        return getCurseData(player).getBoolean(CURSE_5_REVERSED);
    }

    public static boolean isCurseNineReversed(Player player) {
        if (player == null) {
            return false;
        }
        if (player.level().isClientSide) {
            return ClientOverlayEvents.isClientCurseNineReversed();
        }
        return getCurseData(player).getBoolean(CURSE_9_REVERSED);
    }

    public static boolean areCurseThreeAndFourLifted(Player player) {
        if (player == null) {
            return false;
        }
        return isCurseOneReversed(player)
                && isCurseTwoReversed(player)
                && isCurseFiveFateDecided(player)
                && isCurseSevenReversed(player)
                && isCurseEightReversed(player)
                && isCurseNineReversed(player);
    }

    private static boolean isCurseFiveFateDecided(Player player) {
        if (player.level().isClientSide) {
            return ClientOverlayEvents.isClientCurseFiveReversed()
                    || ClientOverlayEvents.isClientCurseFiveDeclined();
        }
        return getCurseData(player).getBoolean(CURSE_5_FATE_DECIDED);
    }

    public static boolean areAllSealCursesReversed(Player player) {
        return areCurseThreeAndFourLifted(player);
    }

    public static void dropEquippedRamessesCurios(Player player) {
        if (player == null || player.level().isClientSide) {
            return;
        }

        CuriosApi.getCuriosInventory(player).ifPresent(inv ->
                inv.getCurios().forEach((slotId, handler) -> {
                    top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler stacks = handler.getStacks();
                    for (int slot = 0; slot < stacks.getSlots(); slot++) {
                        ItemStack equipped = stacks.getStackInSlot(slot);
                        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(equipped.getItem());
                        if (equipped.isEmpty()
                                || equipped.is(ModItems.SEAL_OF_KADESH.get())
                                || itemId == null
                                || !RamessesCurseMod.MODID.equals(itemId.getNamespace())) {
                            continue;
                        }

                        ItemStack dropped = equipped.copy();
                        stacks.setStackInSlot(slot, ItemStack.EMPTY);
                        player.drop(dropped, false);
                    }
                }));
    }

    private static boolean tryAdvanceCurseNineFoodVariety(ServerPlayer player, ItemStack stack) {
        CompoundTag data = getCurseData(player);
        ListTag history = data.getList(CURSE_9_FOOD_HISTORY, Tag.TAG_STRING);
        String itemId = ForgeRegistries.ITEMS.getKey(stack.getItem()) != null
                ? ForgeRegistries.ITEMS.getKey(stack.getItem()).toString()
                : null;
        if (itemId == null || itemId.isEmpty()) {
            return false;
        }

        for (int i = 0; i < history.size(); i++) {
            if (itemId.equals(history.getString(i))) {
                return false;
            }
        }

        history.add(StringTag.valueOf(itemId));
        data.put(CURSE_9_FOOD_HISTORY, history);
        if (history.size() < Config.CURSE_9_REVERSED_FOOD_TYPES.get()) {
            syncClientState(player, true);
            return false;
        }

        data.putBoolean(CURSE_9_REVERSED, true);
        data.putBoolean(CURSE_9_KILL_UNLOCKED, true);
        clearCurseNineDebt(player);
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(20.0F);
        return true;
    }

    public static boolean isCurseFiveDeclined(Player player) {
        if (player == null) {
            return false;
        }
        if (player.level().isClientSide) {
            return ClientOverlayEvents.isClientCurseFiveDeclined();
        }
        return getCurseData(player).getBoolean(CURSE_5_DECLINED);
    }

    public static int getMercyProgress(Player player) {
        return Mth.clamp(getCurseData(player).getInt(MERCY_PROGRESS), 0, Config.CURSE_5_MERCY_MAX.get());
    }

    public static boolean hasCurseFiveMercyCharge(Player player) {
        return getCurseData(player).getBoolean(MERCY_CHARGE_READY);
    }

    private static boolean isCurseFiveMercyInvulnerable(ServerPlayer player) {
        return getCurseData(player).getLong(MERCY_INVULNERABLE_UNTIL) > player.level().getGameTime();
    }

    private static boolean isCurseFiveFatalGuardInvulnerable(ServerPlayer player) {
        return getCurseData(player).getLong(CURSE_5_FATAL_GUARD_INVULNERABLE_UNTIL) > player.level().getGameTime();
    }

    private static boolean isCurseFiveMercySurgeActive(ServerPlayer player) {
        return getCurseData(player).getLong(MERCY_SURGE_UNTIL) > player.level().getGameTime();
    }

    private static boolean tryTriggerCurseFiveFatalGuard(ServerPlayer player) {
        if (!isCurseFiveReversed(player)) {
            return false;
        }

        CompoundTag data = getCurseData(player);
        long gameTime = player.level().getGameTime();
        if (data.getLong(CURSE_5_FATAL_GUARD_COOLDOWN_UNTIL) > gameTime) {
            return false;
        }

        data.putLong(CURSE_5_FATAL_GUARD_COOLDOWN_UNTIL, gameTime + CURSE_5_FATAL_GUARD_COOLDOWN_TICKS);
        data.putLong(CURSE_5_FATAL_GUARD_INVULNERABLE_UNTIL,
                gameTime + CURSE_5_FATAL_GUARD_INVULNERABLE_TICKS);
        player.setHealth(Math.min(1.0F, player.getMaxHealth()));
        player.invulnerableTime = CURSE_5_FATAL_GUARD_INVULNERABLE_TICKS;
        player.fallDistance = 0.0F;
        player.level().broadcastEntityEvent(player, (byte) 35);
        player.level().playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 0.8F);
        player.displayClientMessage(Component.translatable("message.ramesses_curse.curse_5_fatal_guard")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        syncClientState(player, true);
        return true;
    }

    private static void tryAdvanceCurseFiveMercy(ServerPlayer player) {
        if (!isSealEquipped(player) || !isCurseFiveReversed(player)) {
            return;
        }

        CompoundTag data = getCurseData(player);
        if (data.getBoolean(MERCY_CHARGE_READY)) {
            return;
        }

        int next = Math.min(Config.CURSE_5_MERCY_MAX.get(), data.getInt(MERCY_PROGRESS) + 1);
        data.putInt(MERCY_PROGRESS, next);
        player.displayClientMessage(Component.translatable("message.ramesses_curse.mercy_progress", next, Config.CURSE_5_MERCY_MAX.get()).withStyle(net.minecraft.ChatFormatting.AQUA), true);

        if (next >= Config.CURSE_5_MERCY_MAX.get()) {
            data.putInt(MERCY_PROGRESS, Config.CURSE_5_MERCY_MAX.get());
            data.putBoolean(MERCY_CHARGE_READY, true);
            player.displayClientMessage(Component.translatable("message.ramesses_curse.mercy_charge_ready").withStyle(net.minecraft.ChatFormatting.GOLD, net.minecraft.ChatFormatting.BOLD), false);
            player.level().playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.8F, 1.05F);
        }

        syncClientState(player, true);
    }

    private static void tryOpenCurseFiveChoice(ServerPlayer player) {
        if (!isSealEquipped(player)) {
            return;
        }

        CompoundTag data = getCurseData(player);
        if (!isCurseFiveChoicePending(data)) {
            return;
        }

        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new CurseFiveChoicePromptPacket());
    }

    private static void tickCurseFiveSleepChoice(ServerPlayer player, CompoundTag data) {
        boolean sleeping = player.isSleeping();
        data.putBoolean(CURSE_5_WAS_SLEEPING, sleeping);
        if (!sleeping) {
            if (!isCurseFiveChoicePending(data)) {
                data.putBoolean(CURSE_5_SLEEP_CHOICE_OPENED, false);
            }
            return;
        }

        if (!isCurseFiveChoicePending(data)) {
            data.putBoolean(CURSE_5_SLEEP_CHOICE_OPENED, false);
            return;
        }

        freezeCurseFiveSleep(player);
        if (!data.getBoolean(CURSE_5_SLEEP_CHOICE_OPENED)) {
            data.putBoolean(CURSE_5_SLEEP_CHOICE_OPENED, true);
            tryOpenCurseFiveChoice(player);
        }
    }

    private static boolean isCurseFiveChoicePending(CompoundTag data) {
        return data.getBoolean(CURSE_5_PUNISHMENT_COMPLETED)
                && data.getBoolean(CURSE_5_DECISION_PENDING)
                && !data.getBoolean(CURSE_5_FATE_DECIDED);
    }

    private static void freezeCurseFiveSleep(ServerPlayer player) {
        if (PLAYER_SLEEP_COUNTER_FIELD == null) {
            return;
        }
        try {
            int sleepCounter = PLAYER_SLEEP_COUNTER_FIELD.getInt(player);
            if (sleepCounter > CURSE_5_SLEEP_FREEZE_TICKS) {
                PLAYER_SLEEP_COUNTER_FIELD.setInt(player, CURSE_5_SLEEP_FREEZE_TICKS);
            }
        } catch (IllegalAccessException ignored) {
        }
    }

    @Nullable
    private static Field findPlayerSleepCounterField() {
        for (String fieldName : List.of("sleepCounter", "f_36110_")) {
            try {
                Field field = Player.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }

    public static void resolveCurseFiveChoice(ServerPlayer player, boolean reverse) {
        if (player == null) {
            return;
        }

        CompoundTag data = getCurseData(player);
        if (!isCurseFiveChoicePending(data)) {
            return;
        }

        data.putBoolean(CURSE_5_FATE_DECIDED, true);
        data.putBoolean(CURSE_5_DECISION_PENDING, false);
        data.putBoolean(CURSE_5_SLEEP_CHOICE_OPENED, false);
        data.putBoolean(CURSE_5_REVERSED, reverse);
        data.putBoolean(CURSE_5_DECLINED, !reverse);

        if (reverse) {
            data.putInt(MERCY_PROGRESS, 0);
            data.putBoolean(MERCY_CHARGE_READY, false);
            data.putLong(MERCY_INVULNERABLE_UNTIL, 0L);
            data.putLong(MERCY_SURGE_PENDING_AT, 0L);
            data.putLong(MERCY_SURGE_UNTIL, 0L);
            player.displayClientMessage(Component.translatable("message.ramesses_curse.curse_5_reversed").withStyle(net.minecraft.ChatFormatting.GREEN, net.minecraft.ChatFormatting.BOLD), false);
            queueCurseFiveOutcomeHint(
                    player,
                    "the scales break and mercy begins to stain the world with a gentler cruelty",
                    "天秤已碎，慈悲自此以更温柔的残酷浸染世间");
        } else {
            player.displayClientMessage(Component.translatable("message.ramesses_curse.curse_5_declined").withStyle(net.minecraft.ChatFormatting.RED, net.minecraft.ChatFormatting.BOLD), false);
            queueCurseFiveOutcomeHint(
                    player,
                    "the old verdict remains and the world will go on measuring your name in thunder",
                    "旧日裁决依然高悬，世界仍将在雷鸣中衡量你的名讳");
        }

        syncClientState(player, true);
    }

    private static void queueCurseFiveOutcomeHint(ServerPlayer player, String text, String translatedText) {
        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new CurseSevenHintPacket(text, translatedText, "gold_omen", CURSE_5_CHOICE_HINT_TICKS));
    }

    private static void tickCurseThreeAndFourLiftedHint(ServerPlayer player) {
        CompoundTag data = getCurseData(player);
        if (!areCurseThreeAndFourLifted(player)) {
            data.putBoolean(CURSE_3_4_LIFTED_HINT_SHOWN, false);
            return;
        }
        if (data.getBoolean(CURSE_3_4_LIFTED_HINT_SHOWN)) {
            return;
        }

        data.putBoolean(CURSE_3_4_LIFTED_HINT_SHOWN, true);
        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new CurseSevenHintPacket(
                        "the old tithe is gone, and no decree now taxes your craft or your bargains",
                        "旧税已被连根拔去，自此附魔、铁砧与交易都不再向你追讨王朝的余债。",
                        "gold_omen",
                        CURSE_3_4_LIFTED_HINT_TICKS));
    }

    private static boolean tryConsumeCurseFiveMercyCharge(ServerPlayer player) {
        if (!isSealEquipped(player) || !isCurseFiveReversed(player)) {
            return false;
        }

        CompoundTag data = getCurseData(player);
        if (!data.getBoolean(MERCY_CHARGE_READY)) {
            return false;
        }

        long gameTime = player.level().getGameTime();
        data.putBoolean(MERCY_CHARGE_READY, false);
        data.putInt(MERCY_PROGRESS, 0);
        data.putLong(MERCY_INVULNERABLE_UNTIL, gameTime + CURSE_5_MERCY_INVULNERABLE_TICKS);
        data.putLong(MERCY_SURGE_PENDING_AT, gameTime + CURSE_5_MERCY_INVULNERABLE_TICKS);
        data.putLong(MERCY_SURGE_UNTIL, 0L);
        clearCurseFiveMercySurgeModifiers(player);

        player.setHealth(Math.max(1.0F, player.getMaxHealth() * 0.5F));
        player.removeAllEffects();
        player.invulnerableTime = CURSE_5_MERCY_INVULNERABLE_TICKS;
        player.level().broadcastEntityEvent(player, (byte) 35);
        player.level().playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 0.75F);
        player.displayClientMessage(Component.translatable("message.ramesses_curse.mercy_death_reversed").withStyle(net.minecraft.ChatFormatting.GOLD, net.minecraft.ChatFormatting.BOLD), false);
        syncClientState(player, true);
        return true;
    }

    public static boolean triggerCurseFiveMercyRevive(ServerPlayer player) {
        if (player == null || !isCurseFiveReversed(player)) {
            return false;
        }

        CompoundTag data = getCurseData(player);
        if (!data.getBoolean(MERCY_CHARGE_READY)) {
            return false;
        }

        long gameTime = player.level().getGameTime();
        data.putBoolean(MERCY_CHARGE_READY, false);
        data.putInt(MERCY_PROGRESS, 0);
        data.putBoolean(MERCY_REVIVE_PENDING, true);
        data.putString(MERCY_REVIVE_DIMENSION, player.level().dimension().location().toString());
        data.putDouble(MERCY_REVIVE_X, player.getX());
        data.putDouble(MERCY_REVIVE_Y, player.getY());
        data.putDouble(MERCY_REVIVE_Z, player.getZ());
        data.putFloat(MERCY_REVIVE_YAW, player.getYRot());
        data.putFloat(MERCY_REVIVE_PITCH, player.getXRot());
        data.putLong(MERCY_INVULNERABLE_UNTIL, gameTime + CURSE_5_MERCY_INVULNERABLE_TICKS);
        data.putLong(MERCY_SURGE_PENDING_AT, gameTime + CURSE_5_MERCY_INVULNERABLE_TICKS);
        data.putLong(MERCY_SURGE_UNTIL, 0L);
        clearCurseFiveMercySurgeModifiers(player);
        return true;
    }

    private static void finalizeCurseFiveMercyRevive(ServerPlayer player) {
        CompoundTag data = getCurseData(player);
        if (!data.getBoolean(MERCY_REVIVE_PENDING)) {
            return;
        }

        data.putBoolean(MERCY_REVIVE_PENDING, false);
        data.putBoolean(MERCY_REVIVE_TELEPORT_PENDING, true);
        player.setHealth(Math.max(1.0F, player.getMaxHealth() * 0.5F));
        player.removeAllEffects();
        player.invulnerableTime = CURSE_5_MERCY_INVULNERABLE_TICKS;
        player.fallDistance = 0.0F;
        player.setDeltaMovement(Vec3.ZERO);
        player.level().broadcastEntityEvent(player, (byte) 35);
        player.level().playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 0.75F);
        player.displayClientMessage(Component.translatable("message.ramesses_curse.mercy_death_reversed").withStyle(net.minecraft.ChatFormatting.GOLD, net.minecraft.ChatFormatting.BOLD), false);
    }

    private static void finalizePendingMercyReviveTeleport(ServerPlayer player) {
        CompoundTag data = getCurseData(player);
        if (!data.getBoolean(MERCY_REVIVE_TELEPORT_PENDING)) {
            return;
        }

        data.putBoolean(MERCY_REVIVE_TELEPORT_PENDING, false);
        String dimensionId = data.getString(MERCY_REVIVE_DIMENSION);
        double reviveX = data.getDouble(MERCY_REVIVE_X);
        double reviveY = data.getDouble(MERCY_REVIVE_Y);
        double reviveZ = data.getDouble(MERCY_REVIVE_Z);
        float reviveYaw = data.getFloat(MERCY_REVIVE_YAW);
        float revivePitch = data.getFloat(MERCY_REVIVE_PITCH);
        if (!dimensionId.isEmpty()) {
            ServerLevel targetLevel = player.server.getLevel(ResourceKey.create(
                    net.minecraft.core.registries.Registries.DIMENSION,
                    new ResourceLocation(dimensionId)));
            if (targetLevel != null) {
                BlockPos reviveBasePos = BlockPos.containing(reviveX, reviveY, reviveZ);
                BlockPos safePos = findSafeRevivePos(targetLevel, reviveBasePos);
                player.teleportTo(targetLevel, safePos.getX() + 0.5D, safePos.getY(), safePos.getZ() + 0.5D, reviveYaw, revivePitch);
            }
        }
        player.fallDistance = 0.0F;
        player.setDeltaMovement(Vec3.ZERO);
        player.removeEffect(ModMobEffects.INVERSION.get());
        data.remove(MERCY_REVIVE_DIMENSION);
        data.remove(MERCY_REVIVE_X);
        data.remove(MERCY_REVIVE_Y);
        data.remove(MERCY_REVIVE_Z);
        data.remove(MERCY_REVIVE_YAW);
        data.remove(MERCY_REVIVE_PITCH);
    }

    private static BlockPos findSafeRevivePos(ServerLevel level, BlockPos origin) {
        for (int offsetY = 0; offsetY <= 3; offsetY++) {
            BlockPos candidate = origin.above(offsetY);
            if (isSafeReviveSpace(level, candidate)) {
                return candidate;
            }
        }

        for (int offsetY = 1; offsetY <= 6; offsetY++) {
            BlockPos candidate = origin.above(offsetY);
            if (isPassableForRevive(level, candidate) && isPassableForRevive(level, candidate.above())) {
                return candidate;
            }
        }

        return origin.above();
    }

    private static boolean isSafeReviveSpace(ServerLevel level, BlockPos pos) {
        return !isPassableForRevive(level, pos.below())
                && isPassableForRevive(level, pos)
                && isPassableForRevive(level, pos.above());
    }

    private static boolean isPassableForRevive(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.getCollisionShape(level, pos).isEmpty();
    }

    private static void tickCurseFiveMercy(ServerPlayer player) {
        CompoundTag data = getCurseData(player);
        long gameTime = player.level().getGameTime();

        if (!isSealEquipped(player) || !isCurseFiveReversed(player)) {
            clearCurseFiveMercyState(player, false);
            return;
        }

        long surgePendingAt = data.getLong(MERCY_SURGE_PENDING_AT);
        long surgeUntil = data.getLong(MERCY_SURGE_UNTIL);

        if (surgePendingAt > 0L && gameTime >= surgePendingAt) {
            data.putLong(MERCY_SURGE_PENDING_AT, 0L);
            data.putLong(MERCY_SURGE_UNTIL, gameTime + CURSE_5_MERCY_SURGE_TICKS);
            applyCurseFiveMercySurgeModifiers(player);
            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
            player.displayClientMessage(Component.translatable("message.ramesses_curse.mercy_surge_start").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE, net.minecraft.ChatFormatting.BOLD), false);
            player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.9F, 1.15F);
            syncClientState(player, true);
        } else if (surgeUntil > 0L && gameTime >= surgeUntil) {
            data.putLong(MERCY_SURGE_UNTIL, 0L);
            clearCurseFiveMercySurgeModifiers(player);
            player.displayClientMessage(Component.translatable("message.ramesses_curse.mercy_surge_end").withStyle(net.minecraft.ChatFormatting.GRAY), true);
            syncClientState(player, true);
        } else if (surgeUntil > 0L) {
            applyCurseFiveMercySurgeModifiers(player);
        }
    }

    private static void tickCurseFiveDeclinedPower(ServerPlayer player) {
        if (!isSealEquipped(player) || isCurseFiveReversed(player) || !isCurseFiveDeclined(player)) {
            clearCurseFiveDeclinedPower(player);
            return;
        }

        if (!isCurseFiveDeclinedEmpowered(player)) {
            clearCurseFiveDeclinedPower(player);
            return;
        }

        applyCurseFiveDeclinedModifier(player, Attributes.ATTACK_DAMAGE, CURSE_5_DECLINED_ATTACK_MODIFIER, 0.35D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        applyCurseFiveDeclinedModifier(player, Attributes.MAX_HEALTH, CURSE_5_DECLINED_HEALTH_MODIFIER, 8.0D, AttributeModifier.Operation.ADDITION);
        applyCurseFiveDeclinedModifier(player, Attributes.ARMOR, CURSE_5_DECLINED_ARMOR_MODIFIER, 6.0D, AttributeModifier.Operation.ADDITION);
        applyCurseFiveDeclinedModifier(player, Attributes.MOVEMENT_SPEED, CURSE_5_DECLINED_SPEED_MODIFIER, 0.18D, AttributeModifier.Operation.MULTIPLY_TOTAL);

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static boolean isCurseFiveDeclinedEmpowered(ServerPlayer player) {
        if (!isCurseFiveDeclined(player) || isCurseFiveReversed(player)) {
            return false;
        }
        return getGuiltProgress(player) >= Math.max(1, Config.CURSE_5_MAX_GUILT.get() - 2);
    }

    private static void applyCurseFiveDeclinedModifier(ServerPlayer player, Attribute attribute, UUID id, double amount, AttributeModifier.Operation operation) {
        if (player.getAttribute(attribute) == null) {
            return;
        }
        player.getAttribute(attribute).removeModifier(id);
        player.getAttribute(attribute).addTransientModifier(new AttributeModifier(id, "ramesses_curse_curse_five_declined", amount, operation));
    }

    private static void clearCurseFiveDeclinedPower(ServerPlayer player) {
        removeModifier(player, Attributes.ATTACK_DAMAGE, CURSE_5_DECLINED_ATTACK_MODIFIER);
        removeModifier(player, Attributes.MAX_HEALTH, CURSE_5_DECLINED_HEALTH_MODIFIER);
        removeModifier(player, Attributes.ARMOR, CURSE_5_DECLINED_ARMOR_MODIFIER);
        removeModifier(player, Attributes.MOVEMENT_SPEED, CURSE_5_DECLINED_SPEED_MODIFIER);
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void clearCurseFiveMercyState(ServerPlayer player, boolean resetCharge) {
        CompoundTag data = getCurseData(player);
        data.putLong(MERCY_INVULNERABLE_UNTIL, 0L);
        data.putLong(MERCY_SURGE_PENDING_AT, 0L);
        data.putLong(MERCY_SURGE_UNTIL, 0L);
        if (resetCharge) {
            data.putBoolean(MERCY_CHARGE_READY, false);
            data.putInt(MERCY_PROGRESS, 0);
        }
        clearCurseFiveMercySurgeModifiers(player);
    }

    private static void applyCurseFiveMercySurgeModifiers(ServerPlayer player) {
        applyCurseFiveMercySurgeModifier(player, Attributes.ATTACK_DAMAGE);
        applyCurseFiveMercySurgeModifier(player, Attributes.ATTACK_SPEED);
        applyCurseFiveMercySurgeModifier(player, Attributes.MAX_HEALTH);
        applyCurseFiveMercySurgeModifier(player, Attributes.ARMOR);
        applyCurseFiveMercySurgeModifier(player, Attributes.ARMOR_TOUGHNESS);
        applyCurseFiveMercySurgeModifier(player, Attributes.MOVEMENT_SPEED);
        applyCurseFiveMercySurgeModifier(player, Attributes.KNOCKBACK_RESISTANCE);
        applyCurseFiveMercySurgeModifier(player, Attributes.LUCK);
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void applyCurseFiveMercySurgeModifier(ServerPlayer player, Attribute attribute) {
        if (player.getAttribute(attribute) == null) {
            return;
        }
        UUID modifierId = getCurseFiveMercyModifierId(attribute);
        player.getAttribute(attribute).removeModifier(modifierId);
        player.getAttribute(attribute).addTransientModifier(new AttributeModifier(
                modifierId,
                "ramesses_curse_mercy_surge",
                1.0D,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    private static void clearCurseFiveMercySurgeModifiers(ServerPlayer player) {
        player.getAttributes().getSyncableAttributes().forEach(attributeInstance -> {
            if (attributeInstance == null || attributeInstance.getAttribute() == null) {
                return;
            }
            attributeInstance.removeModifier(getCurseFiveMercyModifierId(attributeInstance.getAttribute()));
        });
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static UUID getCurseFiveMercyModifierId(Attribute attribute) {
        ResourceLocation key = ForgeRegistries.ATTRIBUTES.getKey(attribute);
        String raw = key == null ? attribute.getDescriptionId() : key.toString();
        return CURSE_5_SURGE_MODIFIER_IDS.computeIfAbsent(raw, value ->
                UUID.nameUUIDFromBytes(("ramesses_curse_mercy_" + value).getBytes(StandardCharsets.UTF_8)));
    }

    private static void tryTriggerCurseOneReversed(ServerPlayer player, DamageSource source) {
        if (player == null || player.level().isClientSide || !isSealEquipped(player) || !isCurseOneReversed(player)) {
            return;
        }
        if (!(source.getEntity() instanceof LivingEntity attacker) || attacker == player) {
            return;
        }

        CompoundTag data = getCurseData(player);
        long gameTime = player.level().getGameTime();
        if (data.getLong(CURSE_1_REVERSED_COOLDOWN_UNTIL) > gameTime) {
            return;
        }

        List<MobEffect> pool = Config.CURSE_1_REVERSED_EFFECT_WHITELIST.get().stream()
                .map(ResourceLocation::new)
                .map(ForgeRegistries.MOB_EFFECTS::getValue)
                .filter(effect -> effect != null && effect.getCategory() == MobEffectCategory.BENEFICIAL)
                .toList();
        if (pool.isEmpty()) {
            return;
        }

        List<MobEffect> shuffledPool = new ArrayList<>(pool);
        Collections.shuffle(shuffledPool, new java.util.Random(player.getRandom().nextLong()));
        int effectCount = Math.min(Config.CURSE_1_REVERSED_EFFECT_COUNT.get(), shuffledPool.size());
        int minAmplifier = Math.min(Config.CURSE_1_REVERSED_AMPLIFIER_MIN.get(), Config.CURSE_1_REVERSED_AMPLIFIER_MAX.get());
        int maxAmplifier = Math.max(Config.CURSE_1_REVERSED_AMPLIFIER_MIN.get(), Config.CURSE_1_REVERSED_AMPLIFIER_MAX.get());
        int duration = Config.CURSE_1_REVERSED_EFFECT_DURATION.get();

        for (int i = 0; i < effectCount; i++) {
            int amplifier = minAmplifier;
            if (maxAmplifier > minAmplifier) {
                amplifier += player.getRandom().nextInt(maxAmplifier - minAmplifier + 1);
            }
            player.addEffect(new MobEffectInstance(shuffledPool.get(i), duration, amplifier, false, true, true));
        }

        data.putLong(CURSE_1_REVERSED_COOLDOWN_UNTIL, gameTime + Config.CURSE_1_REVERSED_TRIGGER_COOLDOWN.get());
        player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.75F, 1.1F);
    }

    private static void tickCurseOneCupRitual(ServerPlayer player) {
        if (player == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        CompoundTag data = getCurseData(player);
        if (!isSealEquipped(player) || isCurseOneReversed(player)) {
            clearCurseOneRitualState(data);
            return;
        }

        long gameTime = level.getGameTime();
        BlockPos ritualPos = null;
        if (data.getBoolean(CURSE_1_RITUAL_ACTIVE)) {
            if (!level.dimension().location().toString().equals(data.getString(CURSE_1_RITUAL_DIMENSION))) {
                clearCurseOneRitualState(data);
                return;
            }
            ritualPos = new BlockPos(
                    data.getInt(CURSE_1_RITUAL_CAULDRON_X),
                    data.getInt(CURSE_1_RITUAL_CAULDRON_Y),
                    data.getInt(CURSE_1_RITUAL_CAULDRON_Z));
            if (!isValidCurseOneRitualSite(level, ritualPos, player)) {
                clearCurseOneRitualState(data);
                return;
            }
            if (data.getLong(CURSE_1_RITUAL_MATCHED_AT) == 0L) {
                data.putLong(CURSE_1_RITUAL_MATCHED_AT, gameTime);
            }
            if (gameTime - data.getLong(CURSE_1_RITUAL_MATCHED_AT) < 100L) {
                return;
            }
            finishCurseOneRitual(player, level, data, ritualPos);
            return;
        }

        ritualPos = findCurseOneRitualSite(level, player.blockPosition(), player);
        if (ritualPos == null) {
            return;
        }

        data.putBoolean(CURSE_1_RITUAL_ACTIVE, true);
        data.putLong(CURSE_1_RITUAL_STARTED_AT, gameTime);
        data.putLong(CURSE_1_RITUAL_MATCHED_AT, gameTime);
        data.putString(CURSE_1_RITUAL_DIMENSION, level.dimension().location().toString());
        data.putInt(CURSE_1_RITUAL_CAULDRON_X, ritualPos.getX());
        data.putInt(CURSE_1_RITUAL_CAULDRON_Y, ritualPos.getY());
        data.putInt(CURSE_1_RITUAL_CAULDRON_Z, ritualPos.getZ());
        level.playSound(null, ritualPos, SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 0.9F, 0.85F);
        level.sendParticles(ParticleTypes.ENCHANT, ritualPos.getX() + 0.5D, ritualPos.getY() + 1.0D, ritualPos.getZ() + 0.5D, 24, 0.45D, 0.45D, 0.45D, 0.02D);
        player.displayClientMessage(Component.translatable("message.ramesses_curse.curse_1_ritual_start"), false);
    }

    private static BlockPos findCurseOneRitualSite(ServerLevel level, BlockPos center, ServerPlayer player) {
        int radius = 4;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int y = -2; y <= 2; y++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    mutable.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    if (isValidCurseOneRitualSite(level, mutable, player)) {
                        return mutable.immutable();
                    }
                }
            }
        }
        return null;
    }

    private static boolean isValidCurseOneRitualSite(ServerLevel level, BlockPos pos, ServerPlayer player) {
        if (!level.getBlockState(pos).is(Blocks.WATER_CAULDRON)) {
            return false;
        }
        Map<Item, Integer> ingredients = getCurseOneRitualIngredients();
        if (ingredients.isEmpty()) {
            return false;
        }
        for (Map.Entry<Item, Integer> ingredient : ingredients.entrySet()) {
            if (countItemEntities(level, pos, ingredient.getKey(), player) < ingredient.getValue()) {
                return false;
            }
        }
        return true;
    }

    private static Map<Item, Integer> getCurseOneRitualIngredients() {
        Map<Item, Integer> ingredients = new LinkedHashMap<>();
        for (String entry : Config.CURSE_1_REVERSAL_CAULDRON_ITEMS.get()) {
            int separator = entry.lastIndexOf('=');
            String itemId = separator >= 0 ? entry.substring(0, separator).trim() : entry.trim();
            int amount = 1;
            if (separator >= 0) {
                try {
                    amount = Integer.parseInt(entry.substring(separator + 1).trim());
                } catch (NumberFormatException ignored) {
                    return Collections.emptyMap();
                }
            }
            ResourceLocation resourceLocation = ResourceLocation.tryParse(itemId);
            Item item = resourceLocation == null ? null : ForgeRegistries.ITEMS.getValue(resourceLocation);
            if (item == null || item == Items.AIR || amount <= 0) {
                return Collections.emptyMap();
            }
            ingredients.merge(item, amount, (left, right) -> (int) Math.min(1000000L, (long) left + right));
        }
        return ingredients;
    }

    private static int countItemEntities(ServerLevel level, BlockPos pos, Item item, ServerPlayer player) {
        int count = 0;
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(1.35D, 1.0D, 1.35D),
                e -> e.isAlive() && e.tickCount > 5 && isRitualOfferingOwnedBy(e, player))) {
            if (entity.getItem().is(item)) {
                count += entity.getItem().getCount();
            }
        }
        return count;
    }

    private static boolean isRitualOfferingOwnedBy(ItemEntity itemEntity, ServerPlayer player) {
        CompoundTag itemData = itemEntity.getPersistentData();
        if (itemData.hasUUID(RITUAL_OFFERING_OWNER)) {
            return player.getUUID().equals(itemData.getUUID(RITUAL_OFFERING_OWNER));
        }
        Entity thrower = itemEntity.getOwner();
        return thrower != null && player.getUUID().equals(thrower.getUUID());
    }

    private static void assignRitualOfferingOwner(ItemEntity itemEntity, ServerPlayer player) {
        UUID playerId = player.getUUID();
        itemEntity.setThrower(playerId);
        itemEntity.setTarget(playerId);
        itemEntity.getPersistentData().putUUID(RITUAL_OFFERING_OWNER, playerId);
    }

    private static boolean isPotentialRitualOffering(ItemEntity itemEntity) {
        Level level = itemEntity.level();
        Item item = itemEntity.getItem().getItem();
        if (level.dimension() == Level.END
                && isOnEndMainIsland(itemEntity.position())
                && (item == getCurseSixVoidOfferingItem(0)
                    || item == getCurseSixVoidOfferingItem(1)
                    || item == ModItems.FISSION_WRAITH_HEART.get())) {
            return true;
        }
        if (!getCurseOneRitualIngredients().containsKey(item)) {
            return false;
        }
        BlockPos center = itemEntity.blockPosition();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int y = -2; y <= 2; y++) {
            for (int x = -4; x <= 4; x++) {
                for (int z = -4; z <= 4; z++) {
                    mutable.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    if (level.getBlockState(mutable).is(Blocks.WATER_CAULDRON)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void finishCurseOneRitual(ServerPlayer player, ServerLevel level, CompoundTag data, BlockPos ritualPos) {
        consumeCurseOneRitualItems(level, ritualPos, player);
        level.setBlockAndUpdate(ritualPos, Blocks.CAULDRON.defaultBlockState());
        level.playSound(null, ritualPos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 0.8F);
        level.playSound(null, ritualPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.2F);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, ritualPos.getX() + 0.5D, ritualPos.getY() + 1.1D, ritualPos.getZ() + 0.5D, 120, 0.9D, 0.7D, 0.9D, 0.04D);
        level.sendParticles(ParticleTypes.WITCH, ritualPos.getX() + 0.5D, ritualPos.getY() + 1.1D, ritualPos.getZ() + 0.5D, 30, 0.45D, 0.45D, 0.45D, 0.02D);
        data.putBoolean(CURSE_1_REVERSED, true);
        clearCurseOneNegativeEffects(player);
        clearCurseOneRitualState(data);
        player.displayClientMessage(Component.translatable("message.ramesses_curse.curse_1_reversed"), false);
        syncClientState(player, true);
    }

    private static void consumeCurseOneRitualItems(ServerLevel level, BlockPos pos, ServerPlayer player) {
        for (Map.Entry<Item, Integer> ingredient : getCurseOneRitualIngredients().entrySet()) {
            consumeItemEntities(level, pos, ingredient.getKey(), ingredient.getValue(), player);
        }
    }

    private static void consumeItemEntities(ServerLevel level, BlockPos pos, Item item, int required, ServerPlayer player) {
        if (required <= 0) {
            return;
        }
        int remaining = required;
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(1.35D, 1.0D, 1.35D),
                e -> e.isAlive() && e.tickCount > 5 && isRitualOfferingOwnedBy(e, player))) {
            ItemStack stack = entity.getItem();
            if (!stack.is(item) || remaining <= 0) {
                continue;
            }
            int taken = Math.min(remaining, stack.getCount());
            remaining -= taken;
            stack.shrink(taken);
            if (stack.isEmpty()) {
                entity.discard();
            }
        }
    }

    private static void clearCurseOneRitualState(CompoundTag data) {
        data.putBoolean(CURSE_1_RITUAL_ACTIVE, false);
        data.remove(CURSE_1_RITUAL_STARTED_AT);
        data.remove(CURSE_1_RITUAL_MATCHED_AT);
        data.remove(CURSE_1_RITUAL_DIMENSION);
        data.remove(CURSE_1_RITUAL_CAULDRON_X);
        data.remove(CURSE_1_RITUAL_CAULDRON_Y);
        data.remove(CURSE_1_RITUAL_CAULDRON_Z);
    }

    private static void queueCurseSevenHint(ServerPlayer player, String text, String translatedText, String style) {
        CompoundTag data = getCurseData(player);
        data.putBoolean(CURSE_7_PENDING_HINT, true);
        data.putString(CURSE_7_HINT_TEXT, text);
        data.putString(CURSE_7_HINT_TEXT + "Zh", translatedText);
        data.putString(CURSE_7_HINT_STYLE, style);
        data.putLong(CURSE_7_HINT_STARTED_AT, player.level().getGameTime());
        if (text != null && (text.indexOf('*') >= 0 || CURSE_7_CHAT_KEYWORD.equals(text))) {
            player.displayClientMessage(Component.literal(text), false);
        }
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new CurseSevenHintPacket(text, translatedText, style, CURSE_7_HINT_DURATION));
    }

    private static String getCurseSevenSentenceChatMask() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < CURSE_7_CHAT_KEYWORD.length(); i++) {
            char c = CURSE_7_CHAT_KEYWORD.charAt(i);
            builder.append(c == ' ' ? ' ' : '*');
        }
        return builder.toString();
    }

    private static String getCurseSevenBookCipherLine() {
        StringBuilder builder = new StringBuilder();
        String scrambled = CURSE_7_CHAT_SCRAMBLED.toUpperCase(Locale.ROOT);
        for (int i = 0; i < scrambled.length(); i++) {
            char c = scrambled.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                builder.append(EMBER_CIPHER_GLYPHS[c - 'A']);
            } else if (c == ' ') {
                builder.append("   ");
            } else {
                builder.append('*');
            }
        }
        return builder.toString();
    }

    private static String getCurseSevenBookMaskedLine() {
        return "* * * * * *  * * *  * * * *  * * *  * * *  * * * * * * *";
    }

    private static void tickCurseSevenHint(ServerPlayer player) {
        CompoundTag data = getCurseData(player);
        if (!data.getBoolean(CURSE_7_PENDING_HINT)) {
            return;
        }

        if (!data.contains(CURSE_7_HINT_TEXT, Tag.TAG_STRING)) {
            data.putBoolean(CURSE_7_PENDING_HINT, false);
            return;
        }

        long startedAt = data.getLong(CURSE_7_HINT_STARTED_AT);
        if (startedAt <= 0L) {
            data.putLong(CURSE_7_HINT_STARTED_AT, player.level().getGameTime());
            startedAt = player.level().getGameTime();
        }

        if (player.level().getGameTime() - startedAt >= CURSE_7_HINT_DURATION) {
            data.putBoolean(CURSE_7_PENDING_HINT, false);
            data.remove(CURSE_7_HINT_TEXT);
            data.remove(CURSE_7_HINT_TEXT + "Zh");
            data.remove(CURSE_7_HINT_STYLE);
            data.remove(CURSE_7_HINT_STARTED_AT);
        }
    }

    private static void tickCurseSevenSentenceReveal(ServerPlayer player) {
    }

    private static void tickCurseSevenHeartRitual(ServerPlayer player) {
        CompoundTag data = getCurseData(player);
        if (data.getBoolean(CURSE_7_REVERSED)) {
            cancelCurseSevenHeartRitual(player, data);
            return;
        }
        if (!isSealEquipped(player)
                || player.level().dimension() != Level.END
                || !(player.level() instanceof ServerLevel level)
                || !data.getBoolean(END_AWAKENED)
                || !isOnEndMainIsland(player.position())
                || level.getDragonFight() == null
                || !level.getDragonFight().hasPreviouslyKilledDragon()
                || countLivingMainIslandCrystals(level) < CURSE_7_REQUIRED_END_CRYSTALS) {
            cancelCurseSevenHeartRitual(player, data);
            return;
        }

        ItemEntity heart = null;
        if (data.hasUUID(CURSE_7_HEART_RITUAL_UUID)) {
            Entity entity = level.getEntity(data.getUUID(CURSE_7_HEART_RITUAL_UUID));
            if (entity instanceof ItemEntity itemEntity
                    && itemEntity.isAlive()
                    && itemEntity.getItem().is(ModItems.FISSION_WRAITH_HEART.get())) {
                heart = itemEntity;
            }
        }
        if (heart == null) {
            AABB searchBox = new AABB(
                    player.getX() - 96.0D, -64.0D, player.getZ() - 96.0D,
                    player.getX() + 96.0D, 128.0D, player.getZ() + 96.0D);
            List<ItemEntity> candidates = level.getEntitiesOfClass(ItemEntity.class, searchBox, item ->
                    item.isAlive()
                            && item.tickCount > 10
                            && item.getItem().is(ModItems.FISSION_WRAITH_HEART.get())
                            && isOnEndMainIsland(item.position())
                            && isRitualOfferingOwnedBy(item, player));
            if (!candidates.isEmpty()) {
                candidates.sort((left, right) -> Double.compare(
                        left.distanceToSqr(player), right.distanceToSqr(player)));
                heart = candidates.get(0);
                data.putUUID(CURSE_7_HEART_RITUAL_UUID, heart.getUUID());
                data.putLong(CURSE_7_HEART_RITUAL_STARTED_AT, level.getGameTime());
                data.putDouble(CURSE_7_HEART_RITUAL_ORIGIN_Y, heart.getY());
            }
        }
        if (heart == null) {
            return;
        }

        heart.getPersistentData().putBoolean(CURSE_7_HEART_RITUAL_ITEM, true);
        heart.setNoGravity(true);
        heart.setDeltaMovement(Vec3.ZERO);
        heart.setPickUpDelay(32767);
        heart.setUnlimitedLifetime();
        heart.setGlowingTag(true);
        if (!heart.getItem().getOrCreateTag().getBoolean(ModItems.FISSION_WRAITH_HEART_RITUAL_ACTIVE)) {
            ItemStack activeHeart = heart.getItem().copy();
            activeHeart.getOrCreateTag().putBoolean(ModItems.FISSION_WRAITH_HEART_RITUAL_ACTIVE, true);
            heart.setItem(activeHeart);
        }

        long startedAt = data.getLong(CURSE_7_HEART_RITUAL_STARTED_AT);
        if (startedAt <= 0L) {
            startedAt = level.getGameTime();
            data.putLong(CURSE_7_HEART_RITUAL_STARTED_AT, startedAt);
        }
        long elapsed = level.getGameTime() - startedAt;
        heart.setDeltaMovement(0.0D, 2.5D / CURSE_7_HEART_RITUAL_TICKS, 0.0D);
        updateCurseSevenCrystalBeams(level, player, heart);
        if (level.getGameTime() % 5L == 0L) {
            level.sendParticles(ParticleTypes.DRAGON_BREATH, heart.getX(), heart.getY() + 0.2D, heart.getZ(), 5, 0.28D, 0.32D, 0.28D, 0.01D);
            level.sendParticles(ParticleTypes.SMOKE, heart.getX(), heart.getY() + 0.2D, heart.getZ(), 3, 0.22D, 0.25D, 0.22D, 0.005D);
        }
        if (elapsed >= CURSE_7_HEART_RITUAL_TICKS) {
            clearCurseSevenCrystalBeams(level, player);
            heart.discard();
            data.putBoolean(CURSE_7_REVERSED, true);
            clearCurseSevenHeartRitualState(data);
            level.playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0F, 0.7F);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 80, 1.5D, 0.8D, 1.5D, 0.04D);
            queueCurseSevenHint(player, "curse seven has been reversed", "诅咒七已反转", "red_omen");
            syncClientState(player, true);
        }
    }

    private static int countLivingMainIslandCrystals(ServerLevel level) {
        return getMainIslandCrystals(level).size();
    }

    private static List<EndCrystal> getMainIslandCrystals(ServerLevel level) {
        AABB island = new AABB(-192.0D, -64.0D, -192.0D, 192.0D, 128.0D, 192.0D);
        return level.getEntitiesOfClass(EndCrystal.class, island, Entity::isAlive);
    }

    private static void updateCurseSevenCrystalBeams(ServerLevel level, ServerPlayer player, ItemEntity heart) {
        for (EndCrystal crystal : getMainIslandCrystals(level)) {
            CompoundTag crystalData = crystal.getPersistentData();
            boolean ownedBeam = crystalData.hasUUID(CURSE_7_CRYSTAL_BEAM_OWNER)
                    && player.getUUID().equals(crystalData.getUUID(CURSE_7_CRYSTAL_BEAM_OWNER));
            if (crystal.getBeamTarget() == null || ownedBeam) {
                crystalData.putUUID(CURSE_7_CRYSTAL_BEAM_OWNER, player.getUUID());
                crystal.setBeamTarget(BlockPos.containing(heart.getX(), heart.getY() - 0.5D, heart.getZ()));
            }
        }
    }

    private static void clearCurseSevenCrystalBeams(ServerLevel level, ServerPlayer player) {
        for (EndCrystal crystal : getMainIslandCrystals(level)) {
            CompoundTag crystalData = crystal.getPersistentData();
            if (crystalData.hasUUID(CURSE_7_CRYSTAL_BEAM_OWNER)
                    && player.getUUID().equals(crystalData.getUUID(CURSE_7_CRYSTAL_BEAM_OWNER))) {
                crystal.setBeamTarget(null);
                crystalData.remove(CURSE_7_CRYSTAL_BEAM_OWNER);
            }
        }
    }

    private static void cancelCurseSevenHeartRitual(ServerPlayer player, CompoundTag data) {
        MinecraftServer server = player.getServer();
        ServerLevel level = server == null ? null : server.getLevel(Level.END);
        if (level != null) {
            clearCurseSevenCrystalBeams(level, player);
        }
        if (level != null && data.hasUUID(CURSE_7_HEART_RITUAL_UUID)) {
            Entity entity = level.getEntity(data.getUUID(CURSE_7_HEART_RITUAL_UUID));
            if (entity instanceof ItemEntity itemEntity && itemEntity.isAlive()) {
                itemEntity.setNoGravity(false);
                itemEntity.setPickUpDelay(20);
                itemEntity.setGlowingTag(false);
                itemEntity.getPersistentData().remove(CURSE_7_HEART_RITUAL_ITEM);
                if (itemEntity.getItem().hasTag()) {
                    ItemStack inactiveHeart = itemEntity.getItem().copy();
                    inactiveHeart.getTag().remove(ModItems.FISSION_WRAITH_HEART_RITUAL_ACTIVE);
                    itemEntity.setItem(inactiveHeart);
                }
            }
        }
        clearCurseSevenHeartRitualState(data);
    }

    private static void clearCurseSevenHeartRitualState(CompoundTag data) {
        data.remove(CURSE_7_HEART_RITUAL_UUID);
        data.remove(CURSE_7_HEART_RITUAL_STARTED_AT);
        data.remove(CURSE_7_HEART_RITUAL_ORIGIN_Y);
    }

    private static void tickCurseSevenEndRitual(ServerPlayer player) {
        CompoundTag data = getCurseData(player);
        if (data.getBoolean(CURSE_6_REVERSED)) {
            clearCurseSevenRitualState(data);
            return;
        }

        if (player.level().dimension() != Level.END || !(player.level() instanceof ServerLevel serverLevel) || !data.getBoolean(CURSE_6_TERMINAL_SOLVED)) {
            if (data.getBoolean(CURSE_7_END_RITUAL_ACTIVE)) {
                clearCurseSevenRitualState(data);
            }
            return;
        }

        trackCurseSevenVoidOfferings(player, serverLevel, data);
        if (!data.getBoolean(CURSE_7_END_RITUAL_ACTIVE)) {
            return;
        }

        long gameTime = serverLevel.getGameTime();
        boolean dragonReady = data.getBoolean(CURSE_7_END_RITUAL_DRAGON_READY);
        boolean starReady = data.getBoolean(CURSE_7_END_RITUAL_STAR_READY);

        if (dragonReady ^ starReady) {
            long returnAt = data.getLong(CURSE_7_END_RITUAL_RETURN_AT);
            if (returnAt > 0L && gameTime >= returnAt) {
                returnCurseSevenOffering(player, serverLevel, data, dragonReady);
            }
            return;
        }

        if (!dragonReady || !starReady) {
            return;
        }

        long matchedAt = data.getLong(CURSE_7_END_RITUAL_MATCHED_AT);
        if (matchedAt <= 0L) {
            data.putLong(CURSE_7_END_RITUAL_MATCHED_AT, gameTime);
            matchedAt = gameTime;
        }

        long elapsed = gameTime - matchedAt;
        if (elapsed == CURSE_7_RITUAL_SUCCESS_DELAY - 20L) {
            playCurseSevenRitualPrelude(serverLevel, player.position());
        }

        if (elapsed >= CURSE_7_RITUAL_SUCCESS_DELAY) {
            finishCurseSevenRitual(player, serverLevel, data);
        }
    }

    private static void trackCurseSevenVoidOfferings(ServerPlayer player, ServerLevel level, CompoundTag data) {
        AABB searchBox = new AABB(
                player.getX() - 96.0D, -64.0D, player.getZ() - 96.0D,
                player.getX() + 96.0D, 96.0D, player.getZ() + 96.0D);
        List<ItemEntity> offerings = level.getEntitiesOfClass(ItemEntity.class, searchBox, item ->
                item.isAlive()
                        && item.tickCount > 10
                        && isOnEndMainIsland(item.position())
                        && item.getY() < 48.0D
                        && isRitualOfferingOwnedBy(item, player));

        Item firstOfferingItem = getCurseSixVoidOfferingItem(0);
        Item secondOfferingItem = getCurseSixVoidOfferingItem(1);
        ItemEntity dragonEgg = null;
        ItemEntity netherStar = null;
        for (ItemEntity item : offerings) {
            if (dragonEgg == null && item.getItem().is(firstOfferingItem)) {
                dragonEgg = item;
                continue;
            }
            if (netherStar == null && item != dragonEgg && item.getItem().is(secondOfferingItem)) {
                netherStar = item;
            }
        }

        if (dragonEgg == null && netherStar == null) {
            return;
        }

        data.putBoolean(CURSE_7_END_RITUAL_ACTIVE, true);
        if (!data.contains(CURSE_7_END_RITUAL_STARTED_AT, Tag.TAG_LONG)) {
            data.putLong(CURSE_7_END_RITUAL_STARTED_AT, level.getGameTime());
        }

        if (dragonEgg != null) {
            dragonEgg.setGlowingTag(true);
            dragonEgg.setUnlimitedLifetime();
            data.putBoolean(CURSE_7_END_RITUAL_DRAGON_READY, true);
            data.putUUID(CURSE_7_END_RITUAL_DRAGON_UUID, dragonEgg.getUUID());
        }

        if (netherStar != null) {
            netherStar.setUnlimitedLifetime();
            data.putBoolean(CURSE_7_END_RITUAL_STAR_READY, true);
            data.putUUID(CURSE_7_END_RITUAL_STAR_UUID, netherStar.getUUID());
        }

        data.putLong(CURSE_7_END_RITUAL_RETURN_AT, level.getGameTime() + CURSE_7_RITUAL_RETURN_TICKS);

        if (data.getBoolean(CURSE_7_END_RITUAL_DRAGON_READY) && !data.getBoolean(CURSE_7_END_RITUAL_STAR_READY)) {
            sendCurseSevenMissingPrompt(player, secondOfferingItem);
        } else if (!data.getBoolean(CURSE_7_END_RITUAL_DRAGON_READY) && data.getBoolean(CURSE_7_END_RITUAL_STAR_READY)) {
            sendCurseSevenMissingPrompt(player, firstOfferingItem);
        }
    }

    private static void sendCurseSevenMissingPrompt(ServerPlayer player, Item missingItem) {
        String missing = getReadableItemName(missingItem);
        CompoundTag data = getCurseData(player);
        if (missing.equals(data.getString(CURSE_7_END_RITUAL_LAST_MISSING))) {
            return;
        }
        data.putString(CURSE_7_END_RITUAL_LAST_MISSING, missing);
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new CurseSevenHintPacket("missing offering: " + missing, CURSE_7_MISSING_OFFERING_PREFIX_ZH + missing, "ritual_warn", 50));
    }

    private static void returnCurseSevenOffering(ServerPlayer player, ServerLevel level, CompoundTag data, boolean dragonReady) {
        ItemStack returnedStack = new ItemStack(getCurseSixVoidOfferingItem(dragonReady ? 0 : 1));
        ItemEntity returned = new ItemEntity(level, player.getX(), player.getY() + 1.0D, player.getZ(), returnedStack);
        returned.setPickUpDelay(10);
        assignRitualOfferingOwner(returned, player);
        level.addFreshEntity(returned);
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new CurseSevenHintPacket("the void rejects an unfinished exchange", CURSE_7_VOID_REJECTION_ZH, "ritual_warn", 70));
        clearCurseSevenRitualState(data);
    }

    private static Item getCurseSixVoidOfferingItem(int index) {
        List<? extends String> offerings = Config.CURSE_6_VOID_OFFERINGS.get();
        String fallback = index == 0 ? "minecraft:dragon_egg" : "minecraft:nether_star";
        String itemId = index >= 0 && index < offerings.size() ? offerings.get(index) : fallback;
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
        if (item == null) {
            item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(fallback));
        }
        return item == null ? Items.AIR : item;
    }

    private static String getReadableItemName(Item item) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        if (key == null) {
            return "unknown offering";
        }
        return key.getPath().replace('_', ' ');
    }

    private static void playCurseSevenRitualPrelude(ServerLevel level, Vec3 center) {
        BlockPos soundPos = BlockPos.containing(center.x, Math.max(8.0D, center.y - 16.0D), center.z);
        level.playSound(null, soundPos, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.55F, 0.6F);
        level.playSound(null, soundPos, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.45F, 0.5F);
        level.sendParticles(ParticleTypes.PORTAL, center.x, 10.0D, center.z, 60, 2.0D, 5.0D, 2.0D, 0.05D);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, 12.0D, center.z, 48, 1.2D, 4.0D, 1.2D, 0.03D);
    }

    private static void finishCurseSevenRitual(ServerPlayer player, ServerLevel level, CompoundTag data) {
        discardCurseSevenOffering(level, data, CURSE_7_END_RITUAL_DRAGON_UUID);
        discardCurseSevenOffering(level, data, CURSE_7_END_RITUAL_STAR_UUID);
        level.playSound(null, player.blockPosition(), SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.0F, 0.8F);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 160, 2.8D, 0.8D, 2.8D, 0.05D);
        level.sendParticles(new DustParticleOptions(new Vector3f(0.2F, 0.9F, 0.4F), 1.3F), player.getX(), player.getY() + 1.0D, player.getZ(), 45, 1.5D, 0.5D, 1.5D, 0.02D);
        data.putBoolean(CURSE_6_REVERSED, true);
        clearCurseSixLockState(player, data);
        clearCurseSevenRitualState(data);
        queueCurseSevenHint(player, CURSE_7_SUCCESS_LINE, CURSE_7_SUCCESS_LINE_ZH, "green_shadow");
        syncClientState(player, true);
    }

    private static void discardCurseSevenOffering(ServerLevel level, CompoundTag data, String uuidKey) {
        if (!data.hasUUID(uuidKey)) {
            return;
        }
        Entity entity = level.getEntity(data.getUUID(uuidKey));
        if (entity != null) {
            entity.discard();
        }
    }

    private static void clearCurseSevenRitualState(CompoundTag data) {
        data.putBoolean(CURSE_7_END_RITUAL_ACTIVE, false);
        data.putBoolean(CURSE_7_END_RITUAL_DRAGON_READY, false);
        data.putBoolean(CURSE_7_END_RITUAL_STAR_READY, false);
        data.remove(CURSE_7_END_RITUAL_DRAGON_UUID);
        data.remove(CURSE_7_END_RITUAL_STAR_UUID);
        data.remove(CURSE_7_END_RITUAL_STARTED_AT);
        data.remove(CURSE_7_END_RITUAL_MATCHED_AT);
        data.remove(CURSE_7_END_RITUAL_RETURN_AT);
        data.remove(CURSE_7_END_RITUAL_LAST_MISSING);
    }

    private static boolean isOnEndMainIsland(Vec3 position) {
        return Math.abs(position.x) <= 192.0D && Math.abs(position.z) <= 192.0D;
    }

    public static void handleCurseSevenReveal(ServerPlayer player, String answer) {
        if (player == null || !isSealEquipped(player) || isCurseSevenReversed(player) || !isCurseSevenKeywordUnlocked(player)) {
            return;
        }

        CompoundTag data = getCurseData(player);
        String normalized = answer == null ? "" : answer.trim().toLowerCase(Locale.ROOT);
        if (!"end main island".equals(normalized)) {
            data.putBoolean(CURSE_6_KEYWORD_UNLOCKED, false);
            data.putBoolean(CURSE_6_TERMINAL_SOLVED, false);
            data.putBoolean(CURSE_7_PENDING_HINT, false);
            data.remove(CURSE_7_HINT_TEXT);
            data.remove(CURSE_7_HINT_TEXT + "Zh");
            data.remove(CURSE_7_HINT_STYLE);
            data.remove(CURSE_7_HINT_STARTED_AT);
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new CurseSevenHintPacket("", "", "", 0, true));
            syncClientState(player, true);
            return;
        }

        data.putBoolean(CURSE_6_TERMINAL_SOLVED, true);
        data.putBoolean(CURSE_7_PENDING_HINT, true);
        data.putString(CURSE_7_HINT_TEXT, CURSE_7_DUAT_REVEAL);
        data.putString(CURSE_7_HINT_TEXT + "Zh", CURSE_7_DUAT_REVEAL_ZH);
        data.putString(CURSE_7_HINT_STYLE, "gray_shadow");
        data.putLong(CURSE_7_HINT_STARTED_AT, player.level().getGameTime());
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new CurseSevenHintPacket(CURSE_7_DUAT_REVEAL, CURSE_7_DUAT_REVEAL_ZH, "gray_shadow", CURSE_7_HINT_DURATION, true));
        syncClientState(player, true);
    }

    public static String getCurseSevenSentenceMaskedForManual() {
        return getCurseSevenBookMaskedLine();
    }

    public static String getCurseSevenSentenceCipherForManual() {
        return getCurseSevenBookCipherLine();
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean shouldShowCurseSevenSentenceInDuatUi() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null
                && isSealEquipped(minecraft.player)
                && ClientOverlayEvents.isClientCurseSevenSentenceUnlocked()
                && !ClientOverlayEvents.isClientCurseSevenKeywordUnlocked()
                && !ClientOverlayEvents.isClientCurseSevenReversed();
    }

    @OnlyIn(Dist.CLIENT)
    public static Component getClientCurseSevenSentenceUiHint() {
        return Component.literal("余烬侧壁浮现出一段被遮蔽的残句，试着将它重组为完整的英文句子。");
    }

    @OnlyIn(Dist.CLIENT)
    public static String getClientCurseSevenSentenceUiInscription() {
        return getCurseSevenBookMaskedLine() + "\n" + getCurseSevenBookCipherLine();
    }

    private static void grantCurseSevenFlight(ServerPlayer player) {
        if (isDynastyFlightFallLocked(player)) {
            return;
        }
        if (!player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }
    }

    private static void restorePendingCurseSixFlight(ServerPlayer player, CompoundTag data) {
        if (!data.getBoolean(CURSE_6_FLIGHT_RESTORE_PENDING)) {
            return;
        }
        if (!data.getBoolean(CURSE_6_REVERSED)) {
            data.remove(CURSE_6_FLIGHT_RESTORE_PENDING);
            return;
        }
        if (!isSealEquipped(player)) {
            return;
        }

        CompoundTag playerData = player.getPersistentData();
        playerData.remove(FLIGHT_FALL_LOCKED);
        playerData.remove(FLIGHT_FALL_LANDED_AT);
        playerData.remove(FLIGHT_FALL_RESTORE_MAYFLY);
        player.removeEffect(ModMobEffects.FLIGHT_FALL.get());
        grantCurseSevenFlight(player);
        data.remove(CURSE_6_FLIGHT_RESTORE_PENDING);
    }

    private static void revokeCurseSevenFlight(ServerPlayer player) {
        if (player.gameMode.isCreative() || player.isSpectator()) {
            return;
        }
        if (player.getAbilities().mayfly || player.getAbilities().flying) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
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
        if (areCurseThreeAndFourLifted(player)) {
            return fallbackCost;
        }

        int baseCost = getCurseData(player).getInt(CURSE_3_ANVIL_BASE_COST);
        return baseCost > 0 ? baseCost : fallbackCost;
    }

    public static int getAdjustedEnchantLevelCost(Player player, int vanillaCost) {
        if (player == null || vanillaCost <= 0 || !isSealEquipped(player) || areCurseThreeAndFourLifted(player)) {
            return vanillaCost;
        }
        return Math.max(vanillaCost, Mth.ceil(vanillaCost * Config.CURSE_3_ENCHANT_COST_MULTIPLIER.get()));
    }

    public static int getAdjustedEnchantSpend(Player player, int vanillaSpend) {
        if (player == null || vanillaSpend <= 0 || !isSealEquipped(player) || areCurseThreeAndFourLifted(player)) {
            return vanillaSpend;
        }
        return Math.max(vanillaSpend, Mth.ceil(vanillaSpend * Config.CURSE_3_ENCHANT_COST_MULTIPLIER.get()));
    }

    private static void clearCurseOneNegativeEffects(ServerPlayer player) {
        Config.CURSE_1_EFFECT_WHITELIST.get().stream()
                .map(ResourceLocation::new)
                .map(ForgeRegistries.MOB_EFFECTS::getValue)
                .filter(effect -> effect != null && effect.getCategory() == MobEffectCategory.HARMFUL)
                .forEach(player::removeEffect);
    }

    public static int getAdjustedEnchantPointCost(Player player, int vanillaPointCost) {
        if (player == null || vanillaPointCost <= 0 || !isSealEquipped(player) || areCurseThreeAndFourLifted(player)) {
            return vanillaPointCost;
        }
        return Math.max(vanillaPointCost, Mth.ceil(vanillaPointCost * Config.CURSE_3_ENCHANT_COST_MULTIPLIER.get()));
    }

    public static int resolveAdjustedEnchantSpend(Player player, int vanillaSpend) {
        if (player == null || vanillaSpend <= 0 || !isSealEquipped(player) || areCurseThreeAndFourLifted(player)) {
            return vanillaSpend;
        }

        CompoundTag data = getCurseData(player);
        int pendingCost = data.getInt(CURSE_3_PENDING_ENCHANT_COST);
        if (pendingCost > 0) {
            data.putInt(CURSE_3_PENDING_ENCHANT_COST, 0);
            return pendingCost;
        }

        return getAdjustedEnchantSpend(player, vanillaSpend);
    }

    @OnlyIn(Dist.CLIENT)
    public static Component getCurseThreeEnchantTooltip(Player player, int vanillaCost) {
        if (player == null || areCurseThreeAndFourLifted(player)) {
            return Component.empty();
        }
        return Component.literal("诅咒三已生效：附魔额外 +"
                        + getCurseThreeEnchantExtraCost(player, vanillaCost)
                        + "，当前倍率 x"
                        + ClientOverlayEvents.formatMultiplier(Config.CURSE_3_ENCHANT_COST_MULTIPLIER.get()))
                .withStyle(net.minecraft.ChatFormatting.RED);
    }

    @OnlyIn(Dist.CLIENT)
    public static Component getCurseThreeAnvilTooltip(Player player, int vanillaCost) {
        if (player == null || areCurseThreeAndFourLifted(player)) {
            return Component.empty();
        }
        return Component.literal("诅咒三已生效：铁砧额外 +"
                        + getCurseThreeAnvilExtraCost(player, vanillaCost)
                        + "，当前倍率 x"
                        + ClientOverlayEvents.formatMultiplier(Config.CURSE_3_ANVIL_COST_MULTIPLIER.get()))
                .withStyle(net.minecraft.ChatFormatting.RED);
    }

    @OnlyIn(Dist.CLIENT)
    public static Component getCurseThreeInsufficientXpMessage() {
        if (Minecraft.getInstance().player != null && areCurseThreeAndFourLifted(Minecraft.getInstance().player)) {
            return Component.empty();
        }
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
    public static void onItemToss(ItemTossEvent event) {
        ItemEntity itemEntity = event.getEntity();
        if (itemEntity.getItem().is(ModItems.SATIETY_CRYSTAL.get())) {
            event.setCanceled(true);
            event.getPlayer().displayClientMessage(
                    Component.translatable("message.ramesses_curse.satiety_crystal_cannot_drop")
                            .withStyle(net.minecraft.ChatFormatting.RED),
                    true);
            return;
        }
        UUID playerId = event.getPlayer().getUUID();
        if (itemEntity.getItem().is(ModItems.FISSION_WRAITH_HEART.get())) {
            ItemStack heartStack = itemEntity.getItem().copy();
            CompoundTag heartTag = heartStack.getOrCreateTag();
            if (!heartTag.hasUUID(ModItems.FISSION_WRAITH_HEART_OWNER)) {
                heartTag.putUUID(ModItems.FISSION_WRAITH_HEART_OWNER, playerId);
                heartTag.putString(ModItems.FISSION_WRAITH_HEART_OWNER_NAME, event.getPlayer().getGameProfile().getName());
            }
            playerId = heartTag.getUUID(ModItems.FISSION_WRAITH_HEART_OWNER);
            itemEntity.setItem(heartStack);
        }
        itemEntity.setThrower(playerId);
        itemEntity.getPersistentData().putUUID(RITUAL_OFFERING_OWNER, playerId);
        if (isPotentialRitualOffering(itemEntity)) {
            itemEntity.setTarget(playerId);
        }
    }

    @SubscribeEvent
    public static void onEntityItemPickup(EntityItemPickupEvent event) {
        ItemEntity itemEntity = event.getItem();
        if (itemEntity.getItem().is(ModItems.FISSION_WRAITH_HEART.get())) {
            CompoundTag itemData = itemEntity.getPersistentData();
            ItemStack heartStack = itemEntity.getItem();
            CompoundTag heartTag = heartStack.getOrCreateTag();
            UUID pickerId = event.getEntity().getUUID();
            UUID boundOwner = heartTag.hasUUID(ModItems.FISSION_WRAITH_HEART_OWNER)
                    ? heartTag.getUUID(ModItems.FISSION_WRAITH_HEART_OWNER)
                    : null;
            UUID entityOwner = itemData.hasUUID(RITUAL_OFFERING_OWNER)
                    ? itemData.getUUID(RITUAL_OFFERING_OWNER)
                    : null;
            if (itemData.getBoolean(CURSE_7_HEART_RITUAL_ITEM)
                    || (boundOwner != null && !pickerId.equals(boundOwner))
                    || (entityOwner != null && !pickerId.equals(entityOwner))) {
                event.setCanceled(true);
            } else if (boundOwner == null) {
                ItemStack boundHeart = heartStack.copy();
                boundHeart.getOrCreateTag().putUUID(ModItems.FISSION_WRAITH_HEART_OWNER, pickerId);
                boundHeart.getOrCreateTag().putString(
                        ModItems.FISSION_WRAITH_HEART_OWNER_NAME,
                        event.getEntity().getGameProfile().getName());
                itemEntity.setItem(boundHeart);
            }
            return;
        }
        if (!itemEntity.getItem().is(ModItems.SATIETY_CRYSTAL.get())) {
            return;
        }
        ItemStack stack = itemEntity.getItem();
        if (stack.hasTag() && stack.getTag().hasUUID(SATIETY_CRYSTAL_ITEM_OWNER)
                && !stack.getTag().getUUID(SATIETY_CRYSTAL_ITEM_OWNER).equals(event.getEntity().getUUID())) {
            event.setCanceled(true);
            event.getEntity().displayClientMessage(
                    Component.translatable("message.ramesses_curse.satiety_crystal_wrong_owner")
                            .withStyle(net.minecraft.ChatFormatting.RED),
                    true);
            return;
        }
        stack.getOrCreateTag().putUUID(SATIETY_CRYSTAL_ITEM_OWNER, event.getEntity().getUUID());
    }

    public static boolean bindOrValidateSatietyCrystal(ServerPlayer player, ItemStack stack) {
        if (player == null || stack == null || !stack.is(ModItems.SATIETY_CRYSTAL.get())) {
            return false;
        }
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.hasUUID(SATIETY_CRYSTAL_ITEM_OWNER)
                && !tag.getUUID(SATIETY_CRYSTAL_ITEM_OWNER).equals(player.getUUID())) {
            player.displayClientMessage(Component.translatable("message.ramesses_curse.satiety_crystal_wrong_owner")
                    .withStyle(net.minecraft.ChatFormatting.RED), true);
            return false;
        }
        tag.putUUID(SATIETY_CRYSTAL_ITEM_OWNER, player.getUUID());
        return true;
    }

    public static boolean tryUseSatietyCrystalFromInventory(ServerPlayer player) {
        if (player == null || isCurseNineReversed(player) || !hasRecoverableSatietyCrystalDebt(player)) {
            if (player != null) {
                player.displayClientMessage(Component.translatable("message.ramesses_curse.satiety_riddle_no_debt")
                        .withStyle(net.minecraft.ChatFormatting.GRAY), true);
            }
            return false;
        }
        ensureSatietyRiddleRound(player);
        syncSatietyRiddleState(player, true);
        return true;
    }

    private static void deliverPendingSatietyCrystals(ServerPlayer player) {
        if (!player.isAlive()) {
            return;
        }
        CompoundTag data = getCurseData(player);
        int pending = Math.max(0, data.getInt(SATIETY_CRYSTAL_PENDING_ITEMS));
        if (pending <= 0) {
            return;
        }

        ItemStack stack = new ItemStack(ModItems.SATIETY_CRYSTAL.get(), Math.min(pending, 64));
        stack.getOrCreateTag().putUUID(SATIETY_CRYSTAL_ITEM_OWNER, player.getUUID());
        player.getInventory().add(stack);

        int delivered = Math.min(pending, 64) - stack.getCount();
        if (delivered > 0) {
            data.putInt(SATIETY_CRYSTAL_PENDING_ITEMS, pending - delivered);
        }
        if (!stack.isEmpty() && player.tickCount % 200 == 0) {
            player.displayClientMessage(Component.translatable("message.ramesses_curse.satiety_crystal_pending")
                    .withStyle(net.minecraft.ChatFormatting.RED), true);
        }
    }

    private static void reconcileSatietyCrystalItems(ServerPlayer player) {
        int debtCount = getSatietyCrystalCount(player);
        if (debtCount <= 0 || isCurseNineReversed(player)) {
            return;
        }
        CompoundTag data = getCurseData(player);
        int ownedInInventory = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.is(ModItems.SATIETY_CRYSTAL.get())) {
                continue;
            }
            CompoundTag tag = stack.getOrCreateTag();
            if (!tag.hasUUID(SATIETY_CRYSTAL_ITEM_OWNER)) {
                tag.putUUID(SATIETY_CRYSTAL_ITEM_OWNER, player.getUUID());
            }
            if (tag.getUUID(SATIETY_CRYSTAL_ITEM_OWNER).equals(player.getUUID())) {
                ownedInInventory += stack.getCount();
            }
        }
        int pending = Math.max(0, data.getInt(SATIETY_CRYSTAL_PENDING_ITEMS));
        int missing = debtCount - ownedInInventory - pending;
        if (missing > 0) {
            data.putInt(SATIETY_CRYSTAL_PENDING_ITEMS, pending + missing);
            player.displayClientMessage(Component.translatable("message.ramesses_curse.satiety_crystal_reissued")
                    .withStyle(net.minecraft.ChatFormatting.GOLD), false);
        }
    }

    private static boolean consumeSatietyCrystal(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(ModItems.SATIETY_CRYSTAL.get()) && bindOrValidateSatietyCrystal(player, stack)) {
                stack.shrink(1);
                player.getInventory().setChanged();
                player.containerMenu.broadcastChanges();
                return true;
            }
        }
        return false;
    }

    public static boolean tryRecoverSatietyCrystalFromEntity(ServerPlayer player) {
        if (isCurseNineReversed(player)) {
            return true;
        }
        if (!hasRecoverableSatietyCrystalDebt(player)) {
            return false;
        }
        restoreSatietyCrystalDebt(player);
        return true;
    }

    private static boolean hasRecoverableSatietyCrystalDebt(ServerPlayer player) {
        CompoundTag data = getCurseData(player);
        return getSatietyCrystalCount(player) > 0
                || data.getLong(CURSE_9_PENALTY_UNTIL) > 0L
                || getCurseNineDebtTicks(player) < getCurseNineMaxSatiety()
                || isCurseNinePenaltyActive(player);
    }

    private static void restoreSatietyCrystalDebt(Player player) {
        player.getFoodData().eat(CURSE_9_HUNGER_LOSS, 0.0F);
        player.removeEffect(MobEffects.HUNGER);
        player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8F, 1.2F);
        addSatietyCrystalCount(player, -1);
        if (player instanceof ServerPlayer serverPlayer) {
            CompoundTag data = getCurseData(serverPlayer);
            clearCurseNinePenalty(serverPlayer);
            data.putInt(CURSE_9_DEBT_TICKS, getCurseNineMaxSatiety());
            data.remove(CURSE_9_PENALTY_UNTIL);
        }
        syncCurseState(player);
    }

    private static void syncCurseState(Player player) {
        CompoundTag data = getCurseData(player);
        syncGuiltState(
                player,
                data.getInt(GUILT_PROGRESS),
                data.getInt(MERCY_PROGRESS),
                data.getBoolean(MERCY_CHARGE_READY),
                isSealEquipped(player),
                data.getInt(EQUIP_TICKS) / 20,
                data.getBoolean(NETHER_AWAKENED),
                data.getBoolean(END_AWAKENED),
                data.getBoolean(CURSE_1_REVERSED),
                data.getBoolean(CURSE_2_REVERSED),
                data.getBoolean(CURSE_5_REVERSED),
                data.getBoolean(CURSE_5_DECLINED),
                data.getBoolean(CURSE_6_KEYWORD_UNLOCKED),
                data.getBoolean(CURSE_6_TERMINAL_SOLVED),
                data.getBoolean(CURSE_6_REVERSED),
                data.getBoolean(CURSE_7_REVERSED),
                data.getBoolean(CURSE_6_SENTENCE_UNLOCKED),
                data.getBoolean(CURSE_8_REVERSED),
                data.getBoolean(CURSE_9_REVERSED),
                getRemainingGuiltLockTicks(player),
                getSatietyCrystalCount(player),
                getCurseNineDebtTicks(player),
                data.getInt(LOCKED_SLOT),
                getRemainingLockedSlotTicks(player),
                getDynastyPressure(player),
                getRemainingDynastySilenceTicks(player, InteractionHand.MAIN_HAND),
                getRemainingDynastySilenceTicks(player, InteractionHand.OFF_HAND));
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

    public static int drinkDynastyPressurePotion(ServerPlayer player) {
        return addDynastyPressure(player, DYNASTY_PRESSURE_POTION_AMOUNT);
    }

    public static int addDynastyPressure(ServerPlayer player, int amount) {
        if (player == null || amount <= 0) {
            return getDynastyPressure(player);
        }
        CompoundTag data = getCurseData(player);
        int next = Mth.clamp(data.getInt(DYNASTY_PRESSURE) + amount, 0, DYNASTY_PRESSURE_MAX);
        data.putInt(DYNASTY_PRESSURE, next);
        ensureDynastyPressureMultiplier(data);
        syncDynastyPressureSharedData(player, data);
        syncClientState(player, true);
        return getDynastyPressure(player);
    }

    public static int reduceDynastyPressure(ServerPlayer player, int amount) {
        if (player == null || amount <= 0) {
            return getDynastyPressure(player);
        }
        CompoundTag data = getCurseData(player);
        ensureDynastyPressureMultiplier(data);
        int multiplierTenths = getDynastyPressureMultiplierTenths(data);
        int currentEffective = getDynastyPressure(player);
        int targetEffective = Mth.clamp(currentEffective - amount, 0, DYNASTY_PRESSURE_MAX);
        int nextBase = Mth.clamp(Mth.ceil(targetEffective * 10.0F / multiplierTenths), 0, DYNASTY_PRESSURE_MAX);
        data.putInt(DYNASTY_PRESSURE, nextBase);
        syncDynastyPressureSharedData(player, data);
        syncClientState(player, true);
        return getDynastyPressure(player);
    }

    public static int getDynastyPressure(Player player) {
        if (player == null) {
            return 0;
        }
        CompoundTag data = getCurseData(player);
        int base = Mth.clamp(data.getInt(DYNASTY_PRESSURE), 0, DYNASTY_PRESSURE_MAX);
        int multiplierTenths = getDynastyPressureMultiplierTenths(data);
        return Mth.clamp(Mth.floor(base * (multiplierTenths / 10.0F)), 0, DYNASTY_PRESSURE_MAX);
    }

    private static void tickDynastyPressureGrowth(ServerPlayer player, CompoundTag data) {
        if (player.server == null) {
            return;
        }
        ensureDynastyPressureMultiplier(data);
        long currentDay = Math.max(0L, player.server.overworld().getDayTime() / 24000L);
        if (!data.contains(DYNASTY_PRESSURE_LAST_DAY, Tag.TAG_LONG)) {
            data.putLong(DYNASTY_PRESSURE_LAST_DAY, currentDay);
            syncDynastyPressureSharedData(player, data);
            return;
        }
        long lastDay = data.getLong(DYNASTY_PRESSURE_LAST_DAY);
        if (currentDay <= lastDay) {
            return;
        }

        int daysPassed = Mth.clamp((int) (currentDay - lastDay), 1, 100000);
        data.putLong(DYNASTY_PRESSURE_LAST_DAY, currentDay);
        data.putInt(DYNASTY_PRESSURE, Mth.clamp(data.getInt(DYNASTY_PRESSURE) + DYNASTY_PRESSURE_DAILY_AMOUNT * daysPassed, 0, DYNASTY_PRESSURE_MAX));
        int multiplierTenths = Mth.clamp(
                getDynastyPressureMultiplierTenths(data) + daysPassed,
                DYNASTY_PRESSURE_MULTIPLIER_INITIAL_TENTHS,
                DYNASTY_PRESSURE_MULTIPLIER_MAX_TENTHS);
        data.putInt(DYNASTY_PRESSURE_MULTIPLIER_TENTHS, multiplierTenths);
        syncDynastyPressureSharedData(player, data);
        syncClientState(player, true);
    }

    private static void ensureDynastyPressureMultiplier(CompoundTag data) {
        if (data.getInt(DYNASTY_PRESSURE_MULTIPLIER_TENTHS) < DYNASTY_PRESSURE_MULTIPLIER_INITIAL_TENTHS) {
            data.putInt(DYNASTY_PRESSURE_MULTIPLIER_TENTHS, DYNASTY_PRESSURE_MULTIPLIER_INITIAL_TENTHS);
        }
    }

    private static int getDynastyPressureMultiplierTenths(CompoundTag data) {
        ensureDynastyPressureMultiplier(data);
        return Mth.clamp(
                data.getInt(DYNASTY_PRESSURE_MULTIPLIER_TENTHS),
                DYNASTY_PRESSURE_MULTIPLIER_INITIAL_TENTHS,
                DYNASTY_PRESSURE_MULTIPLIER_MAX_TENTHS);
    }

    private static void syncDynastyPressureSharedData(ServerPlayer player, CompoundTag data) {
        int base = Mth.clamp(data.getInt(DYNASTY_PRESSURE), 0, DYNASTY_PRESSURE_MAX);
        data.putInt(DYNASTY_PRESSURE, base);
        int multiplierTenths = getDynastyPressureMultiplierTenths(data);
        int effective = Mth.clamp(Mth.floor(base * (multiplierTenths / 10.0F)), 0, DYNASTY_PRESSURE_MAX);
        data.putInt("L2LibraryDynastyPressure", effective);
        data.putInt("L2LibraryDynastyPressureBase", base);
        data.putInt("L2LibraryDynastyPressureMultiplierTenths", multiplierTenths);
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persisted.putInt("ramesses_curse:dynasty_pressure", effective);
        persisted.putInt("ramesses_curse:dynasty_pressure_base", base);
        persisted.putInt("ramesses_curse:dynasty_pressure_multiplier_tenths", multiplierTenths);
        persisted.putInt("l2library:dynasty_pressure", effective);
        persisted.putInt("l2library:dynasty_pressure_base", base);
        persisted.putInt("l2library:dynasty_pressure_multiplier_tenths", multiplierTenths);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        syncDynastyPressureToL2Capability(player, base, multiplierTenths, effective);
    }

    private static void syncDynastyPressureToL2Capability(ServerPlayer player, int base, int multiplierTenths, int effective) {
        if (!ModList.get().isLoaded("l2library")) {
            return;
        }
        try {
            Class.forName("com.eldren.wutao.ramesses_curse.integration.l2.L2LibraryCompat")
                    .getMethod("sync", ServerPlayer.class, int.class, int.class, int.class)
                    .invoke(null, player, base, multiplierTenths, effective);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    public static void setMercyProgress(ServerPlayer player, int mercy) {
        CompoundTag data = getCurseData(player);
        int clamped = Mth.clamp(mercy, 0, Config.CURSE_5_MERCY_MAX.get());
        if (clamped >= Config.CURSE_5_MERCY_MAX.get()) {
            data.putInt(MERCY_PROGRESS, Config.CURSE_5_MERCY_MAX.get());
            data.putBoolean(MERCY_CHARGE_READY, true);
        } else {
            data.putInt(MERCY_PROGRESS, clamped);
            data.putBoolean(MERCY_CHARGE_READY, false);
        }
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

    public static boolean canUseEmberOfDuat(Player player) {
        if (player == null) {
            return false;
        }
        if (!isSealEquipped(player)) {
            return false;
        }
        CompoundTag data = getCurseData(player);
        return data.getBoolean(EMBER_RIDDLE_UNLOCKED) && !data.getBoolean(EMBER_TRIAL_ACTIVE);
    }

    public static Component getCurrentEmberRiddleHint(Player player) {
        EmberRiddle riddle = getCurrentEmberRiddle(player);
        if (riddle == null) {
            return Component.translatable("message.ramesses_curse.ember_riddle_missing");
        }
        return Component.translatable("message.ramesses_curse.ember_riddle_hint", riddle.title(), riddle.page(), riddle.question());
    }

    public static void submitEmberRiddleAnswer(ServerPlayer player, String answer) {
        if (player == null) {
            return;
        }
        if (!isSealEquipped(player)) {
            return;
        }

        CompoundTag data = getCurseData(player);
        if (data.getBoolean(EMBER_RIDDLE_UNLOCKED)) {
            return;
        }

        EmberRiddle riddle = getCurrentEmberRiddle(player);
        if (riddle == null) {
            startNewEmberRiddleRound(player, false);
            riddle = getCurrentEmberRiddle(player);
        }
        if (riddle == null) {
            return;
        }

        String normalized = normalizeRiddleAnswer(answer);
        if (!riddle.matches(normalized)) {
            startNewEmberRiddleRound(player, true);
            player.displayClientMessage(Component.translatable("message.ramesses_curse.ember_riddle_wrong")
                    .withStyle(net.minecraft.ChatFormatting.RED), false);
            return;
        }

        markEmberRiddleSolved(player);
    }

    public static void selectEmberRiddle(ServerPlayer player, int index) {
        if (player == null) {
            return;
        }
        if (!isSealEquipped(player)) {
            return;
        }
        CompoundTag data = getCurseData(player);
        if (data.getBoolean(EMBER_RIDDLE_UNLOCKED)) {
            return;
        }
        ListTag round = data.getList(EMBER_RIDDLE_ROUND, Tag.TAG_STRING);
        if (round.isEmpty()) {
            startNewEmberRiddleRound(player, false);
            round = data.getList(EMBER_RIDDLE_ROUND, Tag.TAG_STRING);
        }
        if (round.isEmpty()) {
            return;
        }
        int clamped = Mth.clamp(index, 0, round.size() - 1);
        if (isEmberRiddleSolved(data, clamped)) {
            return;
        }
        data.putInt(EMBER_RIDDLE_INDEX, clamped);
        syncEmberRiddleState(player);
    }

    public static void consumeEmberRiddleUse(ServerPlayer player) {
        if (player == null || !isSealEquipped(player)) {
            return;
        }
        startNewEmberRiddleRound(player, true);
    }

    public static EmberTrialActivationResult tryActivateEmberTrial(ServerPlayer player) {
        if (player == null || !isSealEquipped(player)) {
            return EmberTrialActivationResult.RIDDLE_LOCKED;
        }
        CompoundTag data = getCurseData(player);
        if (data.getBoolean(EMBER_TRIAL_ACTIVE)) {
            return EmberTrialActivationResult.ALREADY_ACTIVE;
        }
        if (!data.getBoolean(EMBER_RIDDLE_UNLOCKED)) {
            return EmberTrialActivationResult.RIDDLE_LOCKED;
        }

        data.putBoolean(EMBER_TRIAL_ACTIVE, true);
        data.putBoolean(EMBER_TRIAL_COMPLETED, false);
        data.putInt(EMBER_TRIAL_WAVE, 1);
        data.putInt(EMBER_TRIAL_TOTAL, Arrays.stream(EMBER_TRIAL_WAVE_COUNTS).sum());
        data.putInt(EMBER_TRIAL_DEFEATED, 0);
        data.putInt(EMBER_TRIAL_PENDING_SPAWNS, 0);
        data.remove(EMBER_TRIAL_NEXT_SPAWN_AT);
        data.put(EMBER_TRIAL_MOB_UUIDS, new ListTag());
        consumeEmberRiddleUse(player);
        spawnEmberTrialWave(player, 1);
        ensureEmberTrialBossBar(player);
        return EmberTrialActivationResult.STARTED;
    }

    public static void finishEmberTrial(ServerPlayer player, boolean success) {
        if (player == null || !isSealEquipped(player)) {
            return;
        }
        CompoundTag data = getCurseData(player);
        data.putBoolean(EMBER_TRIAL_ACTIVE, false);
        data.putBoolean(EMBER_TRIAL_COMPLETED, success);
        data.putInt(EMBER_TRIAL_WAVE, 0);
        data.putInt(EMBER_TRIAL_PENDING_SPAWNS, 0);
        data.remove(EMBER_TRIAL_NEXT_SPAWN_AT);
        if (success) {
            boolean newlyReversed = !data.getBoolean(CURSE_2_REVERSED);
            if (newlyReversed) {
                ensureCurseTwoLuckReversalSnapshot(data, data.getInt(EQUIP_TICKS));
            }
            data.putBoolean(CURSE_2_REVERSED, true);
            ModItems.SealOfKadesh.refreshCurseTwoLuck(player);
            unlockCurseSevenSentence(player, data);
            if (newlyReversed) {
                player.displayClientMessage(Component.translatable("message.ramesses_curse.ember_trial_success")
                        .withStyle(net.minecraft.ChatFormatting.GOLD), false);
            }
            syncClientState(player, true);
            data.remove(EMBER_TRIAL_MOB_UUIDS);
            removeEmberTrialBossBar(player);
            return;
        }
        clearEmberTrialMobs(player);
        data.remove(EMBER_TRIAL_MOB_UUIDS);
        removeEmberTrialBossBar(player);
        player.displayClientMessage(Component.translatable("message.ramesses_curse.ember_trial_failed")
                .withStyle(net.minecraft.ChatFormatting.RED), false);
    }

    private static void unlockCurseSevenSentence(ServerPlayer player, CompoundTag data) {
        data.putBoolean(CURSE_6_SENTENCE_UNLOCKED, true);
        data.putBoolean(CURSE_6_KEYWORD_UNLOCKED, false);
        data.putBoolean(CURSE_6_TERMINAL_SOLVED, false);
        data.putInt(CURSE_6_SENTENCE_REVEAL_INDEX, 0);
        data.putLong(CURSE_6_SENTENCE_NEXT_REVEAL_AT, 0L);
        data.putBoolean(CURSE_7_PENDING_HINT, false);
        data.remove(CURSE_7_HINT_TEXT);
        data.remove(CURSE_7_HINT_TEXT + "Zh");
        data.remove(CURSE_7_HINT_STYLE);
        data.remove(CURSE_7_HINT_STARTED_AT);
        String masked = getCurseSevenSentenceChatMask();
        queueCurseSevenHint(player, masked, masked, "gray_shadow");
    }

    public static void spawnCurseTwoMinion(ServerPlayer player) {
        if (player == null || !isSealEquipped(player)) {
            return;
        }
        EntityType<?> entityType = Config.getRandomCurseTwoMinionEntityType(player.getRandom().nextInt());
        if (entityType == null) {
            return;
        }

        BlockPos spawnPos = findSpawnPos(player);
        if (spawnPos == null) {
            return;
        }

        Entity spawned = entityType.spawn(player.serverLevel(), (CompoundTag) null, entity -> {
        }, spawnPos, MobSpawnType.EVENT, true, false);
        if (!(spawned instanceof Mob mob)) {
            if (spawned != null) {
                spawned.discard();
            }
            return;
        }

        CompoundTag tag = mob.getPersistentData();
        tag.putBoolean(CURSE_2_MINION, true);
        tag.putUUID(CURSE_2_MINION_OWNER, player.getUUID());
        tag.putLong(CURSE_2_MINION_EXPIRES_AT, player.level().getGameTime() + Config.CURSE_2_REVERSED_MINION_DURATION.get());
        mob.setCustomName(Component.literal("王权奴仆"));
        mob.setCustomNameVisible(true);
        mob.setPersistenceRequired();
        mob.setGlowingTag(true);
        assignCurseTwoMinionTarget(player, mob);
    }

    private static void tickCurseTwoMinions(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        long gameTime = level.getGameTime();
        double searchRadius = Math.max(24.0D, Config.CURSE_2_SPAWN_RADIUS.get() * 3.0D);
        List<Mob> minions = level.getEntitiesOfClass(
                Mob.class,
                player.getBoundingBox().inflate(searchRadius, 12.0D, searchRadius),
                mob -> isOwnedCurseTwoMinion(mob, player.getUUID()));

        for (Mob minion : minions) {
            CompoundTag tag = minion.getPersistentData();
            if (tag.getLong(CURSE_2_MINION_EXPIRES_AT) <= gameTime) {
                minion.discard();
                continue;
            }

            if (minion.getTarget() == null || !minion.getTarget().isAlive() || minion.getTarget() == player) {
                assignCurseTwoMinionTarget(player, minion);
            }
        }
    }

    private static void tickEmberTrial(ServerPlayer player) {
        CompoundTag data = getCurseData(player);
        if (!data.getBoolean(EMBER_TRIAL_ACTIVE)) {
            removeEmberTrialBossBar(player);
            return;
        }

        ensureEmberTrialMobIndex(player, data);
        tickPendingEmberTrialSpawn(player, data);
        List<Mob> alive = getAliveEmberTrialMobs(player, data);
        int pendingSpawns = Math.max(0, data.getInt(EMBER_TRIAL_PENDING_SPAWNS));
        int currentWave = Math.max(1, data.getInt(EMBER_TRIAL_WAVE));
        if (alive.isEmpty() && pendingSpawns <= 0) {
            if (currentWave >= EMBER_TRIAL_WAVE_COUNTS.length) {
                finishEmberTrial(player, true);
                return;
            }

            int nextWave = currentWave + 1;
            data.putInt(EMBER_TRIAL_WAVE, nextWave);
            spawnEmberTrialWave(player, nextWave);
            pendingSpawns = Math.max(0, data.getInt(EMBER_TRIAL_PENDING_SPAWNS));
        }

        if (player.tickCount % EMBER_TRIAL_BOSSBAR_UPDATE_INTERVAL_TICKS == 0 || alive.isEmpty()) {
            updateEmberTrialBossBar(player, alive.size() + pendingSpawns);
        }
    }

    private static void spawnEmberTrialWave(ServerPlayer player, int wave) {
        int count = EMBER_TRIAL_WAVE_COUNTS[Math.max(0, Math.min(EMBER_TRIAL_WAVE_COUNTS.length - 1, wave - 1))];
        CompoundTag data = getCurseData(player);
        data.putInt(EMBER_TRIAL_PENDING_SPAWNS, count);
        data.putLong(EMBER_TRIAL_NEXT_SPAWN_AT, player.serverLevel().getGameTime());

        player.displayClientMessage(Component.translatable("message.ramesses_curse.ember_trial_wave", wave, EMBER_TRIAL_WAVE_COUNTS.length)
                .withStyle(net.minecraft.ChatFormatting.GOLD), false);
    }

    private static void tickPendingEmberTrialSpawn(ServerPlayer player, CompoundTag data) {
        int pending = Math.max(0, data.getInt(EMBER_TRIAL_PENDING_SPAWNS));
        if (pending <= 0) {
            return;
        }

        ServerLevel level = player.serverLevel();
        long gameTime = level.getGameTime();
        if (gameTime < data.getLong(EMBER_TRIAL_NEXT_SPAWN_AT)) {
            return;
        }

        int wave = Math.max(1, data.getInt(EMBER_TRIAL_WAVE));
        int waveCount = EMBER_TRIAL_WAVE_COUNTS[Math.max(0, Math.min(EMBER_TRIAL_WAVE_COUNTS.length - 1, wave - 1))];
        int spawnedIndex = Math.max(0, waveCount - pending);
        spawnSingleEmberTrialMob(player, wave, spawnedIndex);
        pending--;
        data.putInt(EMBER_TRIAL_PENDING_SPAWNS, pending);
        if (pending > 0) {
            data.putLong(EMBER_TRIAL_NEXT_SPAWN_AT, gameTime + EMBER_TRIAL_SPAWN_INTERVAL_TICKS);
        } else {
            data.remove(EMBER_TRIAL_NEXT_SPAWN_AT);
        }
    }

    private static void spawnSingleEmberTrialMob(ServerPlayer player, int wave, int spawnedIndex) {
        ServerLevel level = player.serverLevel();
        BlockPos center = player.blockPosition();
        EntityType<? extends Mob> type = getEmberTrialWaveType(level, wave);
        BlockPos spawnPos = findTrialSpawnPos(level, center, player.getRandom(), 8 + (spawnedIndex % 3) * 2);
        Mob mob = type.create(level);
        if (mob == null) {
            return;
        }

        mob.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, player.getRandom().nextFloat() * 360.0F, 0.0F);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.EVENT, null, null);
        mob.setTarget(player);
        mob.setPersistenceRequired();
        mob.setGlowingTag(true);
        mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 60, 0, false, false, true));
        CompoundTag tag = mob.getPersistentData();
        tag.putBoolean(EMBER_TRIAL_MOB, true);
        tag.putUUID(EMBER_TRIAL_OWNER, player.getUUID());
        if (level.addFreshEntity(mob)) {
            trackEmberTrialMob(getCurseData(player), mob.getUUID());
        }
    }

    private static EntityType<? extends Mob> getEmberTrialWaveType(ServerLevel level, int wave) {
        int poolKey = Mth.clamp(wave, 1, EMBER_TRIAL_WAVE_COUNTS.length);
        List<EntityType<? extends Mob>> pool = EMBER_TRIAL_ENTITY_POOLS.computeIfAbsent(poolKey, ModEvents::buildEmberTrialWavePool);
        return pool.get(level.getRandom().nextInt(pool.size()));
    }

    private static List<EntityType<? extends Mob>> buildEmberTrialWavePool(int wave) {
        List<EntityType<? extends Mob>> pool = new ArrayList<>();
        switch (wave) {
            case 1 -> {
                pool.add(EntityType.ZOMBIE);
                pool.add(EntityType.SKELETON);
                pool.add(EntityType.SPIDER);
                pool.add(EntityType.CREEPER);
            }
            case 2 -> {
                pool.add(EntityType.HUSK);
                pool.add(EntityType.STRAY);
                pool.add(EntityType.DROWNED);
                pool.add(EntityType.WITCH);
                pool.add(EntityType.PILLAGER);
            }
            default -> {
                pool.add(EntityType.WITHER_SKELETON);
                pool.add(EntityType.HUSK);
                pool.add(EntityType.SKELETON);
                pool.add(EntityType.VINDICATOR);
                pool.add(EntityType.EVOKER);
                pool.add(EntityType.SLIME);
                pool.add(EntityType.BLAZE);
            }
        }

        for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES.getValues()) {
            if (type != null
                    && Mob.class.isAssignableFrom(type.getBaseClass())
                    && Enemy.class.isAssignableFrom(type.getBaseClass())
                    && type != EntityType.PLAYER
                    && type != EntityType.ENDER_DRAGON
                    && type != EntityType.WITHER
                    && !pool.contains(type)) {
                pool.add((EntityType<? extends Mob>) type);
            }
        }

        return List.copyOf(pool);
    }

    private static BlockPos findTrialSpawnPos(ServerLevel level, BlockPos center, RandomSource random, int radius) {
        for (int attempt = 0; attempt < 12; attempt++) {
            int offsetX = Mth.nextInt(random, -radius, radius);
            int offsetZ = Mth.nextInt(random, -radius, radius);
            BlockPos sample = center.offset(offsetX, 0, offsetZ);
            BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, sample);
            if (top.distManhattan(center) >= 4) {
                return top;
            }
        }
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, center.offset(radius, 0, 0));
    }

    private static void trackEmberTrialMob(CompoundTag data, UUID mobId) {
        ListTag ids = data.getList(EMBER_TRIAL_MOB_UUIDS, Tag.TAG_COMPOUND);
        CompoundTag entry = new CompoundTag();
        entry.putUUID(EMBER_TRIAL_MOB_UUID, mobId);
        ids.add(entry);
        data.put(EMBER_TRIAL_MOB_UUIDS, ids);
    }

    private static List<Mob> getAliveEmberTrialMobs(ServerPlayer player, CompoundTag data) {
        ServerLevel level = player.serverLevel();
        UUID ownerId = player.getUUID();
        ListTag ids = data.getList(EMBER_TRIAL_MOB_UUIDS, Tag.TAG_COMPOUND);
        List<Mob> alive = new ArrayList<>(ids.size());
        for (int index = ids.size() - 1; index >= 0; index--) {
            CompoundTag entry = ids.getCompound(index);
            if (!entry.hasUUID(EMBER_TRIAL_MOB_UUID)) {
                ids.remove(index);
                continue;
            }
            Entity entity = level.getEntity(entry.getUUID(EMBER_TRIAL_MOB_UUID));
            if (!(entity instanceof Mob mob) || !mob.isAlive() || !isOwnedEmberTrialMob(mob, ownerId)) {
                ids.remove(index);
                continue;
            }
            alive.add(mob);
        }
        data.put(EMBER_TRIAL_MOB_UUIDS, ids);
        return alive;
    }

    private static boolean isOwnedEmberTrialMob(Mob mob, UUID ownerId) {
        CompoundTag tag = mob.getPersistentData();
        return tag.getBoolean(EMBER_TRIAL_MOB)
                && tag.hasUUID(EMBER_TRIAL_OWNER)
                && ownerId.equals(tag.getUUID(EMBER_TRIAL_OWNER));
    }

    private static void ensureEmberTrialMobIndex(ServerPlayer player, CompoundTag data) {
        if (data.contains(EMBER_TRIAL_MOB_UUIDS, Tag.TAG_LIST)) {
            return;
        }

        ListTag ids = new ListTag();
        List<Mob> nearby = player.serverLevel().getEntitiesOfClass(
                Mob.class,
                player.getBoundingBox().inflate(128.0D, 64.0D, 128.0D),
                mob -> mob.isAlive() && isOwnedEmberTrialMob(mob, player.getUUID()));
        for (Mob mob : nearby) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID(EMBER_TRIAL_MOB_UUID, mob.getUUID());
            ids.add(entry);
        }
        data.put(EMBER_TRIAL_MOB_UUIDS, ids);
    }

    private static void clearEmberTrialMobs(ServerPlayer player) {
        CompoundTag data = getCurseData(player);
        for (Mob mob : getAliveEmberTrialMobs(player, data)) {
            mob.discard();
        }
        data.put(EMBER_TRIAL_MOB_UUIDS, new ListTag());
    }

    private static void ensureEmberTrialBossBar(ServerPlayer player) {
        ServerBossEvent bossBar = ACTIVE_EMBER_TRIAL_BARS.computeIfAbsent(
                player.getUUID(),
                uuid -> new ServerBossEvent(Component.translatable("bossbar.ramesses_curse.ember_trial"), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS));
        bossBar.addPlayer(player);
        bossBar.setVisible(true);
        updateEmberTrialBossBar(player, EMBER_TRIAL_WAVE_COUNTS[0]);
    }

    private static void updateEmberTrialBossBar(ServerPlayer player, int aliveCount) {
        ServerBossEvent bossBar = ACTIVE_EMBER_TRIAL_BARS.get(player.getUUID());
        if (bossBar == null) {
            return;
        }

        CompoundTag data = getCurseData(player);
        int total = Math.max(1, data.getInt(EMBER_TRIAL_TOTAL));
        int defeated = Math.max(0, total - aliveCount - getRemainingFutureWaveCount(data.getInt(EMBER_TRIAL_WAVE)));
        data.putInt(EMBER_TRIAL_DEFEATED, defeated);
        float progress = Mth.clamp((float) defeated / (float) total, 0.0F, 1.0F);
        bossBar.setProgress(progress);
        bossBar.setName(Component.translatable(
                "bossbar.ramesses_curse.ember_trial_status",
                data.getInt(EMBER_TRIAL_WAVE),
                EMBER_TRIAL_WAVE_COUNTS.length,
                aliveCount,
                total - defeated));
    }

    private static int getRemainingFutureWaveCount(int currentWave) {
        int remaining = 0;
        for (int i = Math.max(0, currentWave); i < EMBER_TRIAL_WAVE_COUNTS.length; i++) {
            remaining += EMBER_TRIAL_WAVE_COUNTS[i];
        }
        return remaining;
    }

    private static void removeEmberTrialBossBar(ServerPlayer player) {
        ServerBossEvent bossBar = ACTIVE_EMBER_TRIAL_BARS.remove(player.getUUID());
        if (bossBar != null) {
            bossBar.removeAllPlayers();
            bossBar.setVisible(false);
        }
    }

    private static void assignCurseTwoMinionTarget(ServerPlayer player, Mob minion) {
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
                LivingEntity.class,
                minion.getBoundingBox().inflate(24.0D),
                entity -> entity instanceof Enemy && entity.isAlive() && entity != player);
        LivingEntity target = targets.stream()
                .min((left, right) -> Double.compare(minion.distanceToSqr(left), minion.distanceToSqr(right)))
                .orElse(null);
        if (target != null) {
            minion.setTarget(target);
        }
    }

    private static boolean isCurseTwoMinionFriendlyFire(DamageSource source, LivingEntity target) {
        if (!(target instanceof Player)) {
            return false;
        }
        Entity attacker = source.getEntity();
        if (!(attacker instanceof Mob mob)) {
            return false;
        }
        CompoundTag tag = mob.getPersistentData();
        return tag.getBoolean(CURSE_2_MINION) && tag.hasUUID(CURSE_2_MINION_OWNER);
    }

    private static boolean isCurseTwoMinionDotOrPotionDamage(LivingEntity target, DamageSource source) {
        if (!(target instanceof Mob mob)) {
            return false;
        }
        CompoundTag tag = mob.getPersistentData();
        if (!tag.getBoolean(CURSE_2_MINION)) {
            return false;
        }

        return source.is(DamageTypes.MAGIC)
                || source.is(DamageTypes.WITHER)
                || source.is(DamageTypes.IN_FIRE)
                || source.is(DamageTypes.ON_FIRE)
                || source.is(DamageTypes.LAVA)
                || source.is(DamageTypes.HOT_FLOOR)
                || source.is(DamageTypes.CACTUS)
                || source.is(DamageTypes.SWEET_BERRY_BUSH)
                || source.is(DamageTypes.CRAMMING)
                || source.is(DamageTypes.DRY_OUT)
                || source.is(DamageTypes.FREEZE)
                || source.is(DamageTypes.STARVE);
    }

    private static boolean isOwnedCurseTwoMinion(Entity entity, UUID ownerId) {
        if (!(entity instanceof Mob mob)) {
            return false;
        }
        CompoundTag tag = mob.getPersistentData();
        return tag.getBoolean(CURSE_2_MINION)
                && tag.hasUUID(CURSE_2_MINION_OWNER)
                && ownerId.equals(tag.getUUID(CURSE_2_MINION_OWNER));
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
        return !data.getBoolean(CURSE_6_REVERSED)
                && data.getLong(MAIN_LOCK_UNTIL) > player.level().getGameTime();
    }

    public static boolean isDynastyHandSilenced(Player player, InteractionHand hand) {
        return getRemainingDynastySilenceTicks(player, hand) > 0;
    }

    private static int getRemainingDynastySilenceTicks(Player player, InteractionHand hand) {
        CompoundTag data = getCurseData(player);
        String key = hand == InteractionHand.OFF_HAND ? DYNASTY_SILENCE_OFF_HAND_UNTIL : DYNASTY_SILENCE_MAIN_HAND_UNTIL;
        return (int) Math.max(0L, data.getLong(key) - player.level().getGameTime());
    }

    private static int getRemainingMainHandLockTicks(Player player) {
        CompoundTag data = getCurseData(player);
        if (data.getBoolean(CURSE_6_REVERSED)) {
            return 0;
        }
        return (int) Math.max(0L, data.getLong(MAIN_LOCK_UNTIL) - player.level().getGameTime());
    }

    public static boolean isLockedInventoryStack(Player player, ItemStack stack) {
        if (isGuiltLocked(player)) {
            return !stack.isEmpty();
        }
        CompoundTag data = getCurseData(player);
        if (data.getBoolean(CURSE_6_REVERSED)) {
            return false;
        }
        if (!hasActiveLockedSlot(player, data)) {
            return false;
        }
        int slot = data.getInt(LOCKED_SLOT);
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
        if (data.getBoolean(CURSE_6_REVERSED)) {
            return 0;
        }
        if (!hasActiveLockedSlot(player, data)) {
            return 0;
        }
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

    private static void ensureStarterManual(ServerPlayer player) {
        if (!Config.START_WITH_RAMASSES_MANUAL.get()) {
            return;
        }

        CompoundTag data = getCurseData(player);
        if (data.getBoolean(MANUAL_GRANTED)) {
            return;
        }

        player.getInventory().add(new ItemStack(ModItems.RAMASSES_MANUAL.get()));
        data.putBoolean(MANUAL_GRANTED, true);
    }

    private static boolean isSealEquipped(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .resolve()
                .map(inv -> inv.getStacksHandler(RamessesCurseMod.CURSE_SLOT_ID)
                        .map(handler -> containsSeal(handler.getStacks()))
                        .orElse(false))
                .orElse(false);
    }

    static boolean isSealEquippedForPalace(Player player) {
        return isSealEquipped(player);
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
        if (sealEquipped) {
            return;
        }

        CuriosApi.getCuriosInventory(player).ifPresent(inv ->
            inv.getStacksHandler(RamessesCurseMod.HEART_SLOT_ID).ifPresent(handler -> {
                ejectHeartSlotContents(player, handler.getStacks());
            })
        );
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
        if (!tag.contains(EDICT_ORIGINAL_NO_GRAVITY)) {
            tag.putBoolean(EDICT_ORIGINAL_NO_GRAVITY, target.isNoGravity());
        }
        target.setNoGravity(true);

        if (!tag.getBoolean(EDICT_DOMAIN_HIT)) {
            tag.putBoolean(EDICT_DOMAIN_HIT, true);
        }

        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 10, 0, false, false, true));
        Vec3 center = new Vec3(centerX, centerY + target.getBbHeight() * 0.25D, centerZ);
        Vec3 toCenter = center.subtract(target.position());
        Vec3 horizontalPull = new Vec3(toCenter.x, 0.0D, toCenter.z);
        double distance = horizontalPull.length();
        float eased = progress < 0.65F ? progress * 0.55F : 0.3575F + (progress - 0.65F) * 1.8F;
        double tightening = EDICT_DOMAIN_PULL_STRENGTH * 0.45D + eased * 0.16D;
        Vec3 pull = distance > 0.001D
                ? horizontalPull.normalize().scale(Math.min(0.34D, tightening + distance * 0.012D))
                : Vec3.ZERO;
        double verticalAdjust = Mth.clamp(toCenter.y * 0.045D, -0.05D, 0.06D);
        Vec3 current = target.getDeltaMovement().scale(0.18D);
        target.setDeltaMovement(pull.x + current.x, verticalAdjust + current.y * 0.2D, pull.z + current.z);
        if (distance > 0.18D) {
            double step = Math.min(0.12D, distance * (0.018D + progress * 0.012D));
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
            DamageSource source = ModDamageTypes.percentageDamage(level, caster);
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
        if (tag.contains(EDICT_ORIGINAL_NO_GRAVITY)) {
            target.setNoGravity(tag.getBoolean(EDICT_ORIGINAL_NO_GRAVITY));
        } else {
            target.setNoGravity(false);
        }
        tag.remove(EDICT_DOMAIN_HIT);
        if (tag.contains(EDICT_ORIGINAL_NO_AI) && !tag.contains(EDICT_SEAL_UNTIL)) {
            tag.remove(EDICT_ORIGINAL_NO_AI);
        }
        if (tag.contains(EDICT_ORIGINAL_NO_GRAVITY) && !tag.contains(EDICT_SEAL_UNTIL)) {
            tag.remove(EDICT_ORIGINAL_NO_GRAVITY);
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

    private static void ensureDynastyShield(LivingEntity living, boolean forceSync) {
        if (!Config.DYNASTY_SHIELD_ENABLED.get() || living.level().isClientSide || !(living instanceof Enemy) || !living.isAlive()) {
            return;
        }

        CompoundTag tag = living.getPersistentData();
        boolean assigned = tag.getBoolean(DYNASTY_SHIELD_ASSIGNED);
        if (!assigned) {
            int level = getAssignedDynastyShieldLevel(living);
            float maxShield = getDynastyShieldMax(level);
            if (maxShield <= 0.0F) {
                return;
            }
            tag.putBoolean(DYNASTY_SHIELD_ASSIGNED, true);
            tag.putInt(DYNASTY_SHIELD_LEVEL, level);
            tag.putFloat(DYNASTY_SHIELD_MAX, maxShield);
            tag.putFloat(DYNASTY_SHIELD_CURRENT, maxShield);
            tag.putBoolean(DYNASTY_SHIELD_THREAT_ASSIGNED, true);
            int affixLevel = getDynastyShieldAffixLevelForShieldLevel(level);
            updateDynastyShieldDodgeTag(tag, affixLevel);
            updateDynastyShieldCorrosionTag(tag, affixLevel);
            updateDynastyShieldUnyieldingTag(tag, affixLevel);
            updateDynastyShieldDamageCapTag(tag, affixLevel);
            updateDynastyShieldSilenceTag(tag, affixLevel);
            updateDynastyShieldPurgeTag(tag, affixLevel);
            updateDynastyShieldProjectileWardTag(tag, affixLevel);
            updateDynastyShieldRage(living, tag, affixLevel);
            updateDynastyShieldMoltenTag(tag, affixLevel);
            updateDynastyShieldBloodForfeitTag(tag, affixLevel);
            updateDynastyShieldFlightFallTag(tag, affixLevel);
            updateDynastyShieldClawRageTag(tag, affixLevel);
            updateDynastyShieldSteelBoneTag(tag, affixLevel);
            updateDynastyShieldFleshFeastTag(tag, affixLevel);
            updateDynastyShieldMiasmaTag(tag, affixLevel);
            updateDynastyShieldRestorationTag(tag, affixLevel);
            updateDynastyShieldBloodLimitTag(tag, affixLevel);
            updateDynastyShieldShatterTag(tag, affixLevel);
            updateDynastyShieldDeathAuthorityTag(tag, affixLevel);
            updateDynastyShieldHitSlowTag(tag, affixLevel);
            updateDynastyShieldVulnerabilityTag(tag, affixLevel);
            updateDynastyShieldCounterTag(tag, affixLevel);
            updateDynastyFullAffixHealth(living, tag);
            cleanseDynastyShieldHarmfulEffects(living);
            syncDynastyShield(living, true);
            return;
        }

        if (!tag.getBoolean(DYNASTY_SHIELD_THREAT_ASSIGNED)) {
            migrateDynastyShieldToThreatLevel(living, tag);
        }
        int level = tag.getInt(DYNASTY_SHIELD_LEVEL);
        float maxShield = Math.max(0.0F, tag.getFloat(DYNASTY_SHIELD_MAX));
        if (maxShield <= 0.0F) {
            maxShield = getDynastyShieldMax(level);
            tag.putFloat(DYNASTY_SHIELD_MAX, maxShield);
        }
        float current = Mth.clamp(tag.getFloat(DYNASTY_SHIELD_CURRENT), 0.0F, maxShield);
        tag.putFloat(DYNASTY_SHIELD_CURRENT, current);
        int affixLevel = getDynastyShieldAffixLevelForShieldLevel(level);
        updateDynastyShieldDodgeTag(tag, affixLevel);
        updateDynastyShieldCorrosionTag(tag, affixLevel);
        updateDynastyShieldUnyieldingTag(tag, affixLevel);
        updateDynastyShieldDamageCapTag(tag, affixLevel);
        updateDynastyShieldSilenceTag(tag, affixLevel);
        updateDynastyShieldPurgeTag(tag, affixLevel);
        updateDynastyShieldProjectileWardTag(tag, affixLevel);
        updateDynastyShieldRage(living, tag, affixLevel);
        updateDynastyShieldMoltenTag(tag, affixLevel);
        updateDynastyShieldBloodForfeitTag(tag, affixLevel);
        updateDynastyShieldFlightFallTag(tag, affixLevel);
        updateDynastyShieldClawRageTag(tag, affixLevel);
        updateDynastyShieldSteelBoneTag(tag, affixLevel);
        updateDynastyShieldFleshFeastTag(tag, affixLevel);
        updateDynastyShieldMiasmaTag(tag, affixLevel);
        updateDynastyShieldRestorationTag(tag, affixLevel);
        updateDynastyShieldBloodLimitTag(tag, affixLevel);
        updateDynastyShieldShatterTag(tag, affixLevel);
        updateDynastyShieldDeathAuthorityTag(tag, affixLevel);
        updateDynastyShieldHitSlowTag(tag, affixLevel);
        updateDynastyShieldVulnerabilityTag(tag, affixLevel);
        updateDynastyShieldCounterTag(tag, affixLevel);
        updateDynastyFullAffixHealth(living, tag);
        syncDynastyShield(living, forceSync);
    }

    private static void updateDynastyFullAffixHealth(LivingEntity living, CompoundTag tag) {
        if (!tag.getBoolean(DYNASTY_SHIELD_ASSIGNED)) {
            return;
        }

        AttributeInstance maxHealth = living.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }

        int shieldLevel = Math.max(0, tag.getInt(DYNASTY_SHIELD_LEVEL));
        double healthBonus = shieldLevel * DYNASTY_SHIELD_HEALTH_BONUS_PER_LEVEL;
        AttributeModifier existing = maxHealth.getModifier(DYNASTY_FULL_AFFIX_HEALTH_MODIFIER_ID);
        if (existing != null && Math.abs(existing.getAmount() - healthBonus) < 1.0E-6D) {
            tag.putBoolean(DYNASTY_FULL_AFFIX_HEALTH_APPLIED, healthBonus > 0.0D);
            return;
        }

        double healthBefore = living.getHealth();
        double previousBonus = existing == null ? 0.0D : existing.getAmount();
        maxHealth.removeModifier(DYNASTY_FULL_AFFIX_HEALTH_MODIFIER_ID);
        if (healthBonus > 0.0D) {
            maxHealth.addPermanentModifier(new AttributeModifier(
                    DYNASTY_FULL_AFFIX_HEALTH_MODIFIER_ID,
                    "ramesses_curse_dynasty_shield_health",
                    healthBonus,
                    AttributeModifier.Operation.ADDITION));
        }
        double healthDelta = healthBonus - previousBonus;
        if (healthDelta > 0.0D) {
            living.setHealth((float) Math.min(living.getMaxHealth(), healthBefore + healthDelta));
        } else if (living.getHealth() > living.getMaxHealth()) {
            living.setHealth(living.getMaxHealth());
        }
        tag.putBoolean(DYNASTY_FULL_AFFIX_HEALTH_APPLIED, healthBonus > 0.0D);
    }

    private static void applyDynastyBloodLimit(LivingDamageEvent event) {
        LivingEntity target = event.getEntity();
        Player sourcePlayer = getSourcePlayer(event.getSource());
        if (event.isCanceled()
                || event.getAmount() <= 0.0F
                || target.level().isClientSide
                || !(target instanceof Enemy)
                || !Config.DYNASTY_SHIELD_ENABLED.get()
                || !DynastyAffixData.enabled(DYNASTY_AFFIX_BLOOD_LIMIT)) {
            return;
        }
        if (sourcePlayer != null && AffixKnowledge.hasLearned(sourcePlayer, DYNASTY_AFFIX_BLOOD_LIMIT)) {
            return;
        }

        CompoundTag tag = target.getPersistentData();
        if (!tag.getBoolean(DYNASTY_SHIELD_ASSIGNED)
                || !tag.getBoolean(DYNASTY_SHIELD_BLOOD_LIMIT)) {
            return;
        }

        long gameTime = target.level().getGameTime();
        int windowTicks = Math.max(1, DynastyAffixData.intNumber(DYNASTY_AFFIX_BLOOD_LIMIT, 1, "window_ticks"));
        long windowStart = tag.getLong(DYNASTY_BLOOD_LIMIT_WINDOW_START);
        if (!tag.contains(DYNASTY_BLOOD_LIMIT_WINDOW_START)
                || gameTime < windowStart
                || gameTime - windowStart >= windowTicks) {
            windowStart = gameTime;
            tag.putLong(DYNASTY_BLOOD_LIMIT_WINDOW_START, windowStart);
            tag.putFloat(DYNASTY_BLOOD_LIMIT_WINDOW_DAMAGE, 0.0F);
        }

        float maxWindowLoss = (float) (target.getMaxHealth() * Math.max(
                0.0D,
                DynastyAffixData.number(DYNASTY_AFFIX_BLOOD_LIMIT, 1, "max_health_loss_ratio")));
        float usedDamage = Mth.clamp(tag.getFloat(DYNASTY_BLOOD_LIMIT_WINDOW_DAMAGE), 0.0F, maxWindowLoss);
        float remainingDamage = Math.max(0.0F, maxWindowLoss - usedDamage);
        float incomingDamage = event.getAmount();
        float allowedDamage = Math.min(incomingDamage, remainingDamage);
        boolean limited = allowedDamage + 0.0001F < incomingDamage;

        if (limited && incomingDamage >= target.getHealth()) {
            float minimumHealth = (float) Math.max(
                    0.0D,
                    DynastyAffixData.number(DYNASTY_AFFIX_BLOOD_LIMIT, 1, "minimum_health"));
            allowedDamage = Math.min(allowedDamage, Math.max(0.0F, target.getHealth() - minimumHealth));
        }

        if (allowedDamage <= 0.0F) {
            event.setAmount(0.0F);
            event.setCanceled(true);
            return;
        }

        event.setAmount(allowedDamage);
        tag.putFloat(DYNASTY_BLOOD_LIMIT_WINDOW_DAMAGE, usedDamage + allowedDamage);
    }

    private static boolean tryBlockDynastySteelBoneDamage(LivingDamageEvent event) {
        LivingEntity target = event.getEntity();
        Player sourcePlayer = getSourcePlayer(event.getSource());
        if (!Config.DYNASTY_SHIELD_ENABLED.get()
                || target.level().isClientSide
                || !(target instanceof Enemy)
                || !event.getSource().is(ModDamageTypes.PERCENTAGE_DAMAGE)) {
            return false;
        }
        if (sourcePlayer != null && AffixKnowledge.hasLearned(sourcePlayer, DYNASTY_AFFIX_STEEL_BONE)) {
            return false;
        }

        ensureDynastyShield(target, false);
        CompoundTag tag = target.getPersistentData();
        if (!tag.getBoolean(DYNASTY_SHIELD_ASSIGNED)
                || !tag.getBoolean(DYNASTY_SHIELD_STEEL_BONE)
                || !DynastyAffixData.enabled(DYNASTY_AFFIX_STEEL_BONE)) {
            return false;
        }

        event.setAmount(0.0F);
        event.setCanceled(true);
        target.level().playSound(null, target.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 0.85F, 0.65F);
        return true;
    }

    private static void absorbDynastyShieldDamage(LivingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (!Config.DYNASTY_SHIELD_ENABLED.get() || target.level().isClientSide || !(target instanceof Enemy) || event.getAmount() <= 0.0F) {
            return;
        }

        ensureDynastyShield(target, false);
        CompoundTag tag = target.getPersistentData();
        if (!tag.getBoolean(DYNASTY_SHIELD_ASSIGNED)) {
            return;
        }

        float current = tag.getFloat(DYNASTY_SHIELD_CURRENT);
        if (current <= 0.0F) {
            return;
        }

        Player sourcePlayer = getSourcePlayer(event.getSource());
        fixDynastyShieldUnyieldingLevel(tag, sourcePlayer);

        if (tryDodgeDynastyShieldDamage(target, tag, event.getSource())) {
            event.setCanceled(true);
            event.setAmount(0.0F);
            target.level().playSound(null, target.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 0.8F, 1.35F + target.getRandom().nextFloat() * 0.25F);
            return;
        }

        tryTriggerDynastyMiasma(target, tag, event.getSource());
        tryTriggerDynastyHitSlow(tag, event.getSource());

        triggerDynastyShieldSilence(sourcePlayer, tag);

        float originalDamage = event.getAmount();
        float damage = applyDynastyShieldDamageCap(tag, event.getSource(), originalDamage);
        if (damage < originalDamage) {
            triggerDynastyShieldDamageCapGuard(target);
        }
        event.setAmount(damage);

        float remainingDamage = damage - current;
        float newShield = Math.max(0.0F, current - damage);
        tag.putFloat(DYNASTY_SHIELD_CURRENT, newShield);
        boolean shieldBroken = newShield <= 0.0F;
        if (shieldBroken) {
            removeDynastyShieldRageModifiers(target);
            removeDynastyFleshFeastModifiers(target);
            tag.remove(DYNASTY_CLAW_RAGE_ARMED);
        }
        syncDynastyShield(target, true);

        target.level().playSound(null, target.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 0.7F, 0.85F + target.getRandom().nextFloat() * 0.25F);
        if (shieldBroken) {
            triggerDynastyShieldBreakGuard(target);
            triggerDynastyShieldShatter(target, tag);
            tryTriggerDynastyShieldCounter(target, tag, sourcePlayer);
            target.level().playSound(null, target.blockPosition(), SoundEvents.SHIELD_BREAK, SoundSource.HOSTILE, 0.8F, 0.9F);
        }
        if (remainingDamage <= 0.0F) {
            event.setCanceled(true);
            event.setAmount(0.0F);
        } else {
            event.setAmount(remainingDamage);
        }
    }

    private static void tryTriggerDynastyShieldCounter(LivingEntity target, CompoundTag tag, @Nullable Player sourcePlayer) {
        if (!tag.getBoolean(DYNASTY_SHIELD_COUNTER)
                || tag.getBoolean(DYNASTY_SHIELD_COUNTER_USED)
                || (sourcePlayer != null && AffixKnowledge.hasLearned(sourcePlayer, DYNASTY_AFFIX_COUNTER))
                || !DynastyAffixData.enabled(DYNASTY_AFFIX_COUNTER)) {
            return;
        }

        tag.putBoolean(DYNASTY_SHIELD_COUNTER_USED, true);
        double chance = Mth.clamp(
                DynastyAffixData.number(DYNASTY_AFFIX_COUNTER, 1, "revival_chance"), 0.0D, 1.0D);
        if (target.getRandom().nextDouble() >= chance) {
            return;
        }

        CompoundTag data = target.getPersistentData();
        float maxShield = Math.max(0.0F, data.getFloat(DYNASTY_SHIELD_MAX));
        double restoreRatio = Mth.clamp(
                DynastyAffixData.number(DYNASTY_AFFIX_COUNTER, 1, "revival_ratio"), 0.0D, 1.0D);
        float restoredShield = (float) (maxShield * restoreRatio);
        if (restoredShield <= 0.0F) {
            return;
        }

        data.putFloat(DYNASTY_SHIELD_CURRENT, restoredShield);
        data.remove(DYNASTY_CLAW_RAGE_ARMED);
        ensureDynastyShield(target, true);
        target.level().playSound(null, target.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.HOSTILE, 0.85F, 0.8F);
        if (target.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, target.getX(), target.getY(0.5D), target.getZ(),
                    18, 0.45D, 0.65D, 0.45D, 0.1D);
        }
    }

    private static void triggerDynastyShieldShatter(LivingEntity source, CompoundTag sourceTag) {
        if (!sourceTag.getBoolean(DYNASTY_SHIELD_SHATTER)
                || !DynastyAffixData.enabled(DYNASTY_AFFIX_SHIELD_SHATTER)) {
            return;
        }

        double radius = Math.max(0.0D, DynastyAffixData.number(DYNASTY_AFFIX_SHIELD_SHATTER, 1, "radius"));
        float damage = Math.max(0.0F, (float) DynastyAffixData.number(DYNASTY_AFFIX_SHIELD_SHATTER, 1, "damage"));
        double knockback = Math.max(0.0D, DynastyAffixData.number(DYNASTY_AFFIX_SHIELD_SHATTER, 1, "knockback_strength"));
        if (radius <= 0.0D) {
            return;
        }

        AABB area = source.getBoundingBox().inflate(radius);
        for (LivingEntity target : source.level().getEntitiesOfClass(LivingEntity.class, area,
                target -> target != source && target.isAlive() && !source.isAlliedTo(target))) {
            if (target instanceof Player player && AffixKnowledge.hasLearned(player, DYNASTY_AFFIX_SHIELD_SHATTER)) {
                continue;
            }
            double dx = target.getX() - source.getX();
            double dz = target.getZ() - source.getZ();
            double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
            if (horizontalDistance > radius || horizontalDistance < 1.0E-4D) {
                continue;
            }

            double falloff = 1.0D - horizontalDistance / radius;
            double appliedKnockback = knockback * (0.65D + falloff * 0.35D);
            target.push(dx / horizontalDistance * appliedKnockback, 0.25D + falloff * 0.2D,
                    dz / horizontalDistance * appliedKnockback);
            target.hurtMarked = true;

            CompoundTag targetTag = target.getPersistentData();
            boolean hasActiveShield = targetTag.getBoolean(DYNASTY_SHIELD_ASSIGNED)
                    && targetTag.getFloat(DYNASTY_SHIELD_CURRENT) > 0.0F;
            if (!hasActiveShield && damage > 0.0F) {
                target.hurt(source.level().damageSources().mobAttack(source), damage);
            }
        }

        if (source.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.POOF, source.getX(), source.getY(0.5D), source.getZ(),
                    24, radius * 0.35D, 0.5D, radius * 0.35D, 0.12D);
        }
        source.level().playSound(null, source.blockPosition(), SoundEvents.GENERIC_EXPLODE,
                SoundSource.HOSTILE, 0.9F, 1.25F);
    }

    private static boolean tryDodgeDynastyShieldDamage(LivingEntity target, CompoundTag tag, DamageSource source) {
        Player sourcePlayer = getSourcePlayer(source);
        int affixLevel = getDynastyShieldAffixLevel(sourcePlayer);
        if (!DynastyAffixData.enabled(DYNASTY_AFFIX_DODGE)
                || sourcePlayer == null
                || AffixKnowledge.hasLearned(sourcePlayer, DYNASTY_AFFIX_DODGE)
                || !isDynastyShieldAffixUnlocked(affixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_DODGE))) {
            tag.remove(DYNASTY_SHIELD_DODGE_LEVEL);
            return false;
        }
        int dodgeLevel = getDynastyShieldDodgeLevel(affixLevel);
        tag.putInt(DYNASTY_SHIELD_DODGE_LEVEL, dodgeLevel);
        return target.getRandom().nextFloat() < DynastyAffixData.number(DYNASTY_AFFIX_DODGE, dodgeLevel, "chance");
    }

    private static float applyDynastyShieldDamageCap(CompoundTag tag, DamageSource source, float amount) {
        Player sourcePlayer = getSourcePlayer(source);
        int affixLevel = getDynastyShieldAffixLevel(sourcePlayer);
        if (!DynastyAffixData.enabled(DYNASTY_AFFIX_DAMAGE_CAP)
                || sourcePlayer == null
                || AffixKnowledge.hasLearned(sourcePlayer, DYNASTY_AFFIX_DAMAGE_CAP)
                || !isDynastyShieldAffixUnlocked(affixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_DAMAGE_CAP))) {
            tag.remove(DYNASTY_SHIELD_DAMAGE_CAP_LEVEL);
            return amount;
        }
        int capLevel = getDynastyShieldDamageCapLevel(affixLevel);
        tag.putInt(DYNASTY_SHIELD_DAMAGE_CAP_LEVEL, capLevel);
        return Math.min(amount, getDynastyShieldDamageCap(capLevel));
    }

    private static void triggerDynastyShieldSilence(@Nullable Player sourcePlayer, CompoundTag tag) {
        if (!DynastyAffixData.enabled(DYNASTY_AFFIX_SILENCE) || !(sourcePlayer instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (AffixKnowledge.hasLearned(serverPlayer, DYNASTY_AFFIX_SILENCE)) {
            return;
        }
        int affixLevel = getDynastyShieldAffixLevel(serverPlayer);
        if (!isDynastyShieldAffixUnlocked(affixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_SILENCE))) {
            tag.remove(DYNASTY_SHIELD_SILENCE_LEVEL);
            return;
        }
        int silenceLevel = getDynastyShieldSilenceLevel(affixLevel);
        tag.putInt(DYNASTY_SHIELD_SILENCE_LEVEL, silenceLevel);
        if (serverPlayer.getRandom().nextDouble() >= getDynastyShieldSilenceChance(silenceLevel)) {
            return;
        }
        long until = serverPlayer.level().getGameTime() + DynastyAffixData.intNumber(DYNASTY_AFFIX_SILENCE, silenceLevel, "duration_seconds") * 20L;
        CompoundTag playerData = getCurseData(serverPlayer);
        playerData.putLong(DYNASTY_SILENCE_MAIN_HAND_UNTIL, Math.max(playerData.getLong(DYNASTY_SILENCE_MAIN_HAND_UNTIL), until));
        playerData.putLong(DYNASTY_SILENCE_OFF_HAND_UNTIL, Math.max(playerData.getLong(DYNASTY_SILENCE_OFF_HAND_UNTIL), until));
        syncClientState(serverPlayer, true);
    }

    private static void triggerDynastyShieldDamageCapGuard(LivingEntity target) {
        target.invulnerableTime = Math.max(target.invulnerableTime, 60);
        addOrExtendDynastyShieldResistance(target, 300, 1);
    }

    private static void triggerDynastyShieldBreakGuard(LivingEntity target) {
        target.invulnerableTime = Math.max(target.invulnerableTime, 40);
        target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 0, false, false, true));
    }

    private static void addOrExtendDynastyShieldResistance(LivingEntity target, int addedTicks, int amplifier) {
        MobEffectInstance current = target.getEffect(MobEffects.DAMAGE_RESISTANCE);
        int duration = addedTicks;
        if (current != null) {
            if (current.getAmplifier() > amplifier) {
                return;
            }
            duration += current.getDuration();
        }
        target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, amplifier, false, false, true));
    }

    private static float getDynastyShieldDamageCap(int capLevel) {
        return (float) DynastyAffixData.number(DYNASTY_AFFIX_DAMAGE_CAP, capLevel, "max_damage");
    }

    private static void tryCorrodeDynastyShieldEquipment(LivingDamageEvent event) {
        if (!Config.DYNASTY_SHIELD_ENABLED.get() || !DynastyAffixData.enabled(DYNASTY_AFFIX_CORROSION)) {
            return;
        }

        LivingEntity target = event.getEntity();
        Player sourcePlayer = getSourcePlayer(event.getSource());
        if (sourcePlayer instanceof ServerPlayer serverPlayer && target instanceof Enemy) {
            corrodePlayerEquipment(serverPlayer, target);
        }

        Entity sourceEntity = event.getSource().getEntity();
        if (target instanceof ServerPlayer serverPlayer && sourceEntity instanceof LivingEntity attacker && attacker instanceof Enemy) {
            corrodePlayerEquipment(serverPlayer, attacker);
        }
    }

    private static void corrodePlayerEquipment(ServerPlayer player, LivingEntity corrosiveEntity) {
        if (corrosiveEntity.level().isClientSide || AffixKnowledge.hasLearned(player, DYNASTY_AFFIX_CORROSION)) {
            return;
        }

        ensureDynastyShield(corrosiveEntity, false);
        CompoundTag tag = corrosiveEntity.getPersistentData();
        if (!tag.getBoolean(DYNASTY_SHIELD_ASSIGNED) || tag.getFloat(DYNASTY_SHIELD_CURRENT) <= 0.0F) {
            return;
        }

        int affixLevel = getDynastyShieldAffixLevel(player);
        if (!isDynastyShieldAffixUnlocked(affixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_CORROSION))) {
            tag.remove(DYNASTY_SHIELD_CORROSION_LEVEL);
            return;
        }
        int corrosionLevel = getDynastyShieldCorrosionLevel(affixLevel);
        tag.putInt(DYNASTY_SHIELD_CORROSION_LEVEL, corrosionLevel);
        float damageRatio = (float) DynastyAffixData.number(DYNASTY_AFFIX_CORROSION, corrosionLevel, "durability_ratio");
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty() || !stack.isDamageableItem()) {
                continue;
            }
            int durabilityLoss = Math.max(1, Mth.ceil(stack.getMaxDamage() * damageRatio));
            damageEquipmentIgnoringProtection(player, slot, stack, durabilityLoss);
        }
    }

    private static void damageEquipmentIgnoringProtection(ServerPlayer player, EquipmentSlot slot, ItemStack stack, int amount) {
        int nextDamage = stack.getDamageValue() + amount;
        if (nextDamage >= stack.getMaxDamage()) {
            stack.setDamageValue(stack.getMaxDamage());
            player.broadcastBreakEvent(slot);
            stack.shrink(1);
            if (stack.isEmpty()) {
                player.setItemSlot(slot, ItemStack.EMPTY);
            }
        } else {
            stack.setDamageValue(nextDamage);
        }
    }

    private static boolean tryReviveDynastyShieldEntity(LivingDeathEvent event) {
        LivingEntity living = event.getEntity();
        if (!Config.DYNASTY_SHIELD_ENABLED.get()
                || !DynastyAffixData.enabled(DYNASTY_AFFIX_UNYIELDING)
                || living.level().isClientSide
                || !(living instanceof Enemy)
                || living instanceof Player) {
            return false;
        }

        ensureDynastyShield(living, false);
        CompoundTag tag = living.getPersistentData();
        if (!tag.getBoolean(DYNASTY_SHIELD_ASSIGNED)) {
            return false;
        }

        Player sourcePlayer = getSourcePlayer(event.getSource());
        if (sourcePlayer != null && AffixKnowledge.hasLearned(sourcePlayer, DYNASTY_AFFIX_UNYIELDING)) {
            return false;
        }
        fixDynastyShieldUnyieldingLevel(tag, sourcePlayer);
        int unyieldingLevel = tag.getInt(DYNASTY_SHIELD_UNYIELDING_LEVEL);
        if (unyieldingLevel <= 0) {
            return false;
        }
        int revives = Math.max(0, tag.getInt(DYNASTY_SHIELD_UNYIELDING_REVIVES));
        if (revives >= DYNASTY_SHIELD_UNYIELDING_MAX_REVIVES) {
            return false;
        }

        float chance = getDynastyShieldUnyieldingReviveChance(unyieldingLevel);
        if (living.getRandom().nextFloat() >= chance) {
            return false;
        }

        float healthRatio = getDynastyShieldUnyieldingHealthRatio(unyieldingLevel);
        float revivedHealth = Math.max(1.0F, living.getMaxHealth() * healthRatio);
        event.setCanceled(true);
        living.setHealth(Math.min(living.getMaxHealth(), revivedHealth));
        living.deathTime = 0;
        living.invulnerableTime = Math.max(living.invulnerableTime, 20);
        tag.putInt(DYNASTY_SHIELD_UNYIELDING_REVIVES, revives + 1);
        tag.remove(DYNASTY_SHIELD_UNYIELDING_LEVEL);
        tag.remove(DYNASTY_SHIELD_UNYIELDING_FIXED);
        living.level().broadcastEntityEvent(living, (byte) 35);
        living.level().playSound(null, living.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 0.9F + living.getRandom().nextFloat() * 0.2F);
        if (living.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, living.getX(), living.getY() + living.getBbHeight() * 0.5D, living.getZ(), 64, living.getBbWidth() * 0.55D, living.getBbHeight() * 0.45D, living.getBbWidth() * 0.55D, 0.12D);
        }
        return true;
    }

    private static float getDynastyShieldUnyieldingReviveChance(int unyieldingLevel) {
        return (float) DynastyAffixData.number(DYNASTY_AFFIX_UNYIELDING, unyieldingLevel, "revive_chance");
    }

    private static float getDynastyShieldUnyieldingHealthRatio(int unyieldingLevel) {
        return (float) DynastyAffixData.number(DYNASTY_AFFIX_UNYIELDING, unyieldingLevel, "health_ratio");
    }

    private static int getAssignedDynastyShieldLevel(LivingEntity living) {
        List<? extends Integer> levels = Config.DYNASTY_SHIELD_LEVELS.get();
        List<? extends Integer> thresholds = Config.DYNASTY_SHIELD_THREAT_THRESHOLDS.get();
        int count = Math.min(levels.size(), thresholds.size());
        if (count <= 0) {
            return 1;
        }

        Player nearestPlayer = living.level().getNearestPlayer(living, 128.0D);
        int pressure = getDynastyPressure(nearestPlayer);
        int selected = getShieldListValue(levels, 0, 1);
        int selectedThreshold = Integer.MIN_VALUE;
        for (int i = 0; i < count; i++) {
            int threshold = Math.max(0, thresholds.get(i));
            if (pressure >= threshold && threshold >= selectedThreshold) {
                selected = Math.max(1, levels.get(i));
                selectedThreshold = threshold;
            }
        }
        return selected;
    }

    private static void tryTriggerDynastyHitSlow(CompoundTag tag, DamageSource source) {
        if (!tag.getBoolean(DYNASTY_SHIELD_HIT_SLOW)
                || !DynastyAffixData.enabled(DYNASTY_AFFIX_HIT_SLOW)
                || !(getSourcePlayer(source) instanceof ServerPlayer player)) {
            return;
        }
        if (AffixKnowledge.hasLearned(player, DYNASTY_AFFIX_HIT_SLOW)) {
            return;
        }
        int durationTicks = Math.max(1,
                DynastyAffixData.intNumber(DYNASTY_AFFIX_HIT_SLOW, 1, "duration_ticks"));
        player.addEffect(new MobEffectInstance(ModMobEffects.HIT_SLOW.get(), durationTicks, 0, false, true, true));
    }

    private static void migrateDynastyShieldToThreatLevel(LivingEntity living, CompoundTag tag) {
        if (living.level().getNearestPlayer(living, 128.0D) == null) {
            return;
        }
        int newLevel = getAssignedDynastyShieldLevel(living);
        float oldMax = Math.max(0.0F, tag.getFloat(DYNASTY_SHIELD_MAX));
        float oldCurrent = Mth.clamp(tag.getFloat(DYNASTY_SHIELD_CURRENT), 0.0F, oldMax);
        float newMax = getDynastyShieldMax(newLevel);
        float remainingRatio = oldMax > 0.0F ? oldCurrent / oldMax : 1.0F;
        tag.putInt(DYNASTY_SHIELD_LEVEL, newLevel);
        tag.putFloat(DYNASTY_SHIELD_MAX, newMax);
        tag.putFloat(DYNASTY_SHIELD_CURRENT, Mth.clamp(newMax * remainingRatio, 0.0F, newMax));
        tag.putBoolean(DYNASTY_SHIELD_THREAT_ASSIGNED, true);
    }

    private static float getDynastyShieldMax(int level) {
        List<? extends Integer> levels = Config.DYNASTY_SHIELD_LEVELS.get();
        List<? extends Integer> values = Config.DYNASTY_SHIELD_MAX_VALUES.get();
        for (int i = 0; i < levels.size() && i < values.size(); i++) {
            if (levels.get(i) == level) {
                return Math.max(0, values.get(i));
            }
        }

        for (int i = Math.min(levels.size(), values.size()) - 1; i >= 0; i--) {
            if (level >= levels.get(i)) {
                return Math.max(0, values.get(i));
            }
        }
        return getShieldListValue(values, 0, 50);
    }

    private static int getShieldListValue(List<? extends Integer> values, int index, int fallback) {
        if (index >= 0 && index < values.size()) {
            return Math.max(0, values.get(index));
        }
        return fallback;
    }

    private static void updateDynastyShieldDodgeTag(CompoundTag tag, int affixLevel) {
        if (!DynastyAffixData.enabled(DYNASTY_AFFIX_DODGE)
                || !isDynastyShieldAffixUnlocked(affixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_DODGE))) {
            tag.remove(DYNASTY_SHIELD_DODGE_LEVEL);
            return;
        }
        tag.putInt(DYNASTY_SHIELD_DODGE_LEVEL, getDynastyShieldDodgeLevel(affixLevel));
    }

    private static int getDynastyShieldDodgeLevel(int affixLevel) {
        return DynastyAffixData.level(DYNASTY_AFFIX_DODGE, affixLevel);
    }

    private static void updateDynastyShieldCorrosionTag(CompoundTag tag, int affixLevel) {
        if (!DynastyAffixData.enabled(DYNASTY_AFFIX_CORROSION)
                || !isDynastyShieldAffixUnlocked(affixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_CORROSION))) {
            tag.remove(DYNASTY_SHIELD_CORROSION_LEVEL);
            return;
        }
        tag.putInt(DYNASTY_SHIELD_CORROSION_LEVEL, getDynastyShieldCorrosionLevel(affixLevel));
    }

    private static int getDynastyShieldCorrosionLevel(int affixLevel) {
        return DynastyAffixData.level(DYNASTY_AFFIX_CORROSION, affixLevel);
    }

    private static void updateDynastyShieldUnyieldingTag(CompoundTag tag, int affixLevel) {
        if (!DynastyAffixData.enabled(DYNASTY_AFFIX_UNYIELDING)
                || !isDynastyShieldAffixUnlocked(affixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_UNYIELDING))) {
            tag.remove(DYNASTY_SHIELD_UNYIELDING_LEVEL);
            tag.remove(DYNASTY_SHIELD_UNYIELDING_REVIVES);
            tag.remove(DYNASTY_SHIELD_UNYIELDING_FIXED);
            return;
        }
        if (tag.getInt(DYNASTY_SHIELD_UNYIELDING_REVIVES) >= DYNASTY_SHIELD_UNYIELDING_MAX_REVIVES) {
            tag.remove(DYNASTY_SHIELD_UNYIELDING_LEVEL);
            tag.remove(DYNASTY_SHIELD_UNYIELDING_FIXED);
            return;
        }
        if (!tag.getBoolean(DYNASTY_SHIELD_UNYIELDING_FIXED)) {
            int unyieldingLevel = getDynastyShieldUnyieldingLevel(affixLevel);
            if (unyieldingLevel <= 0) {
                tag.remove(DYNASTY_SHIELD_UNYIELDING_LEVEL);
                tag.remove(DYNASTY_SHIELD_UNYIELDING_FIXED);
                return;
            }
            tag.putInt(DYNASTY_SHIELD_UNYIELDING_LEVEL, unyieldingLevel);
            tag.putBoolean(DYNASTY_SHIELD_UNYIELDING_FIXED, true);
        }
    }

    private static void fixDynastyShieldUnyieldingLevel(CompoundTag tag, @Nullable Player player) {
        int affixLevel = getDynastyShieldAffixLevel(player);
        if (!DynastyAffixData.enabled(DYNASTY_AFFIX_UNYIELDING)
                || !isDynastyShieldAffixUnlocked(affixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_UNYIELDING))
                || tag.getInt(DYNASTY_SHIELD_UNYIELDING_REVIVES) >= DYNASTY_SHIELD_UNYIELDING_MAX_REVIVES) {
            tag.remove(DYNASTY_SHIELD_UNYIELDING_LEVEL);
            tag.remove(DYNASTY_SHIELD_UNYIELDING_FIXED);
            return;
        }
        if (!tag.getBoolean(DYNASTY_SHIELD_UNYIELDING_FIXED)) {
            int unyieldingLevel = getDynastyShieldUnyieldingLevel(affixLevel);
            if (unyieldingLevel <= 0) {
                tag.remove(DYNASTY_SHIELD_UNYIELDING_LEVEL);
                tag.remove(DYNASTY_SHIELD_UNYIELDING_FIXED);
                return;
            }
            tag.putInt(DYNASTY_SHIELD_UNYIELDING_LEVEL, unyieldingLevel);
            tag.putBoolean(DYNASTY_SHIELD_UNYIELDING_FIXED, true);
        }
    }

    private static int getDynastyShieldUnyieldingLevel(int affixLevel) {
        if (!isDynastyShieldAffixUnlocked(affixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_UNYIELDING))) {
            return 0;
        }
        return DynastyAffixData.level(DYNASTY_AFFIX_UNYIELDING, affixLevel);
    }

    private static void updateDynastyShieldDamageCapTag(CompoundTag tag, int affixLevel) {
        if (!DynastyAffixData.enabled(DYNASTY_AFFIX_DAMAGE_CAP)
                || !isDynastyShieldAffixUnlocked(affixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_DAMAGE_CAP))) {
            tag.remove(DYNASTY_SHIELD_DAMAGE_CAP_LEVEL);
            return;
        }
        tag.putInt(DYNASTY_SHIELD_DAMAGE_CAP_LEVEL, getDynastyShieldDamageCapLevel(affixLevel));
    }

    private static int getDynastyShieldDamageCapLevel(int affixLevel) {
        return DynastyAffixData.level(DYNASTY_AFFIX_DAMAGE_CAP, affixLevel);
    }

    private static void updateDynastyShieldSilenceTag(CompoundTag tag, int affixLevel) {
        if (!DynastyAffixData.enabled(DYNASTY_AFFIX_SILENCE)
                || !isDynastyShieldAffixUnlocked(affixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_SILENCE))) {
            tag.remove(DYNASTY_SHIELD_SILENCE_LEVEL);
            return;
        }
        tag.putInt(DYNASTY_SHIELD_SILENCE_LEVEL, getDynastyShieldSilenceLevel(affixLevel));
    }

    private static int getDynastyShieldSilenceLevel(int affixLevel) {
        return DynastyAffixData.level(DYNASTY_AFFIX_SILENCE, affixLevel);
    }

    private static void updateDynastyShieldPurgeTag(CompoundTag tag, int affixLevel) {
        if (!DynastyAffixData.enabled(DYNASTY_AFFIX_PURGE)
                || !isDynastyShieldAffixUnlocked(affixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_PURGE))) {
            tag.remove(DYNASTY_SHIELD_PURGE);
            return;
        }
        tag.putBoolean(DYNASTY_SHIELD_PURGE, true);
    }

    private static void updateDynastyShieldProjectileWardTag(CompoundTag tag, int affixLevel) {
        if (!DynastyAffixData.enabled(DYNASTY_AFFIX_PROJECTILE_WARD)
                || !isDynastyShieldAffixUnlocked(affixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_PROJECTILE_WARD))) {
            tag.remove(DYNASTY_SHIELD_PROJECTILE_WARD);
            return;
        }
        tag.putBoolean(DYNASTY_SHIELD_PROJECTILE_WARD, true);
    }

    private static void updateDynastyShieldSteelBoneTag(CompoundTag tag, int affixLevel) {
        if (!DynastyAffixData.enabled(DYNASTY_AFFIX_STEEL_BONE)
                || !isDynastyShieldAffixUnlocked(affixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_STEEL_BONE))) {
            tag.remove(DYNASTY_SHIELD_STEEL_BONE);
            return;
        }
        tag.putBoolean(DYNASTY_SHIELD_STEEL_BONE, true);
    }

    private static void updateDynastyShieldFleshFeastTag(CompoundTag tag, int affixLevel) {
        if (!DynastyAffixData.enabled(DYNASTY_AFFIX_FLESH_FEAST)
                || !isDynastyShieldAffixUnlocked(affixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_FLESH_FEAST))) {
            tag.remove(DYNASTY_SHIELD_FLESH_FEAST_LEVEL);
            return;
        }
        tag.putInt(DYNASTY_SHIELD_FLESH_FEAST_LEVEL,
                DynastyAffixData.level(DYNASTY_AFFIX_FLESH_FEAST, affixLevel));
    }

    private static void updateDynastyShieldMiasmaTag(CompoundTag tag, int affixLevel) {
        if (!DynastyAffixData.enabled(DYNASTY_AFFIX_MIASMA)
                || !isDynastyShieldAffixUnlocked(affixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_MIASMA))) {
            tag.remove(DYNASTY_SHIELD_MIASMA_LEVEL);
            return;
        }
        tag.putInt(DYNASTY_SHIELD_MIASMA_LEVEL,
                DynastyAffixData.level(DYNASTY_AFFIX_MIASMA, affixLevel));
    }

    private static void updateDynastyShieldRestorationTag(CompoundTag tag, int affixLevel) {
        if (!DynastyAffixData.enabled(DYNASTY_AFFIX_RESTORATION)
                || !isDynastyShieldAffixUnlocked(affixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_RESTORATION))) {
            tag.remove(DYNASTY_SHIELD_RESTORATION_LEVEL);
            return;
        }
        tag.putInt(DYNASTY_SHIELD_RESTORATION_LEVEL,
                DynastyAffixData.level(DYNASTY_AFFIX_RESTORATION, affixLevel));
    }

    private static void updateDynastyShieldBloodLimitTag(CompoundTag tag, int affixLevel) {
        if (!DynastyAffixData.enabled(DYNASTY_AFFIX_BLOOD_LIMIT)
                || !isDynastyShieldAffixUnlocked(affixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_BLOOD_LIMIT))) {
            tag.remove(DYNASTY_SHIELD_BLOOD_LIMIT);
            tag.remove(DYNASTY_BLOOD_LIMIT_WINDOW_START);
            tag.remove(DYNASTY_BLOOD_LIMIT_WINDOW_DAMAGE);
            return;
        }
        tag.putBoolean(DYNASTY_SHIELD_BLOOD_LIMIT, true);
    }

    private static void updateDynastyShieldShatterTag(CompoundTag tag, int affixLevel) {
        if (!DynastyAffixData.enabled(DYNASTY_AFFIX_SHIELD_SHATTER)
                || !isDynastyShieldAffixUnlocked(affixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_SHIELD_SHATTER))) {
            tag.remove(DYNASTY_SHIELD_SHATTER);
            return;
        }
        tag.putBoolean(DYNASTY_SHIELD_SHATTER, true);
    }

    private static void updateDynastyShieldDeathAuthorityTag(CompoundTag tag, int affixLevel) {
        if (!DynastyAffixData.enabled(DYNASTY_AFFIX_DEATH_AUTHORITY)
                || !isDynastyShieldAffixUnlocked(affixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_DEATH_AUTHORITY))) {
            tag.remove(DYNASTY_SHIELD_DEATH_AUTHORITY);
            tag.remove(DYNASTY_DEATH_AUTHORITY_READY_AT);
            return;
        }
        tag.putBoolean(DYNASTY_SHIELD_DEATH_AUTHORITY, true);
    }

    private static void updateDynastyShieldHitSlowTag(CompoundTag tag, int affixLevel) {
        if (!DynastyAffixData.enabled(DYNASTY_AFFIX_HIT_SLOW)
                || !isDynastyShieldAffixUnlocked(affixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_HIT_SLOW))) {
            tag.remove(DYNASTY_SHIELD_HIT_SLOW);
            return;
        }
        tag.putBoolean(DYNASTY_SHIELD_HIT_SLOW, true);
    }

    private static void updateDynastyShieldVulnerabilityTag(CompoundTag tag, int affixLevel) {
        if (!DynastyAffixData.enabled(DYNASTY_AFFIX_VULNERABILITY)
                || !isDynastyShieldAffixUnlocked(affixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_VULNERABILITY))) {
            tag.remove(DYNASTY_SHIELD_VULNERABILITY_LEVEL);
            return;
        }
        tag.putInt(DYNASTY_SHIELD_VULNERABILITY_LEVEL,
                DynastyAffixData.level(DYNASTY_AFFIX_VULNERABILITY, affixLevel));
    }

    private static void updateDynastyShieldCounterTag(CompoundTag tag, int affixLevel) {
        if (!DynastyAffixData.enabled(DYNASTY_AFFIX_COUNTER)
                || !isDynastyShieldAffixUnlocked(affixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_COUNTER))) {
            tag.remove(DYNASTY_SHIELD_COUNTER);
            return;
        }
        tag.putBoolean(DYNASTY_SHIELD_COUNTER, true);
    }

    private static void tryTriggerDynastyRestoration(LivingEntity defeated, DamageSource source) {
        if (!(source.getEntity() instanceof LivingEntity attacker)
                || attacker.level().isClientSide
                || !(attacker instanceof Enemy)
                || !attacker.isAlive()) {
            return;
        }
        if (defeated instanceof Player player && AffixKnowledge.hasLearned(player, DYNASTY_AFFIX_RESTORATION)) {
            return;
        }

        CompoundTag tag = attacker.getPersistentData();
        int level = tag.getInt(DYNASTY_SHIELD_RESTORATION_LEVEL);
        if (!Config.DYNASTY_SHIELD_ENABLED.get()
                || !DynastyAffixData.enabled(DYNASTY_AFFIX_RESTORATION)
                || !tag.getBoolean(DYNASTY_SHIELD_ASSIGNED)
                || level <= 0) {
            return;
        }

        float healAmount = (float) (attacker.getMaxHealth()
                * Math.max(0.0D, DynastyAffixData.number(DYNASTY_AFFIX_RESTORATION, level, "heal_ratio")));
        if (healAmount <= 0.0F || attacker.getHealth() >= attacker.getMaxHealth()) {
            return;
        }
        attacker.heal(healAmount);
        if (attacker.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.HEART,
                    attacker.getX(),
                    attacker.getY() + attacker.getBbHeight() * 0.65D,
                    attacker.getZ(),
                    8,
                    attacker.getBbWidth() * 0.35D,
                    attacker.getBbHeight() * 0.25D,
                    attacker.getBbWidth() * 0.35D,
                    0.02D);
        }
    }

    private static void tryTriggerDynastyMiasma(LivingEntity target, CompoundTag tag, DamageSource source) {
        int level = tag.getInt(DYNASTY_SHIELD_MIASMA_LEVEL);
        if (!DynastyAffixData.enabled(DYNASTY_AFFIX_MIASMA)
                || level <= 0
                || !(source.getEntity() instanceof LivingEntity attacker)
                || attacker == target
                || !attacker.isAlive()
                || target.getRandom().nextDouble()
                >= DynastyAffixData.number(DYNASTY_AFFIX_MIASMA, level, "chance")) {
            return;
        }
        if (attacker instanceof Player player && AffixKnowledge.hasLearned(player, DYNASTY_AFFIX_MIASMA)) {
            return;
        }

        int duration = Math.max(1, DynastyAffixData.intNumber(DYNASTY_AFFIX_MIASMA, level, "duration_ticks"));
        int poisonLevel = Math.max(1, DynastyAffixData.intNumber(DYNASTY_AFFIX_MIASMA, level, "poison_level"));
        attacker.addEffect(new MobEffectInstance(MobEffects.POISON, duration, poisonLevel - 1), target);
    }

    private static void tickDynastyFleshFeast(LivingEntity living) {
        CompoundTag tag = living.getPersistentData();
        int level = tag.getInt(DYNASTY_SHIELD_FLESH_FEAST_LEVEL);
        if (!Config.DYNASTY_SHIELD_ENABLED.get()
                || !DynastyAffixData.enabled(DYNASTY_AFFIX_FLESH_FEAST)
                || !tag.getBoolean(DYNASTY_SHIELD_ASSIGNED)
                || tag.getFloat(DYNASTY_SHIELD_CURRENT) <= 0.0F
                || level <= 0
                || !(living.level() instanceof ServerLevel serverLevel)) {
            removeDynastyFleshFeastModifiers(living);
            return;
        }

        double radius = Math.max(0.0D, DynastyAffixData.number(DYNASTY_AFFIX_FLESH_FEAST, level, "radius"));
        double radiusSqr = radius * radius;
        int nearbyPlayers = serverLevel.getEntitiesOfClass(
                ServerPlayer.class,
                living.getBoundingBox().inflate(radius),
                player -> player.isAlive()
                        && !player.isSpectator()
                        && !player.isCreative()
                        && !AffixKnowledge.hasLearned(player, DYNASTY_AFFIX_FLESH_FEAST)
                        && player.distanceToSqr(living) <= radiusSqr).size();
        double healthBonus = DynastyAffixData.number(
                DYNASTY_AFFIX_FLESH_FEAST, level, "health_bonus_per_player") * nearbyPlayers;
        double armorBonus = DynastyAffixData.number(
                DYNASTY_AFFIX_FLESH_FEAST, level, "armor_bonus_per_player") * nearbyPlayers;
        applyDynastyFleshFeastModifiers(living, healthBonus, armorBonus);
    }

    private static void applyDynastyFleshFeastModifiers(LivingEntity living, double healthBonus, double armorBonus) {
        AttributeInstance maxHealth = living.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance armor = living.getAttribute(Attributes.ARMOR);
        if (maxHealth == null) {
            return;
        }

        float oldMaxHealth = living.getMaxHealth();
        float healthRatio = oldMaxHealth > 0.0F ? living.getHealth() / oldMaxHealth : 1.0F;
        boolean healthChanged = replaceDynastyFleshFeastModifier(
                maxHealth,
                DYNASTY_FLESH_FEAST_HEALTH_MODIFIER_ID,
                "ramesses_dynasty_flesh_feast_health",
                Math.max(0.0D, healthBonus));
        if (armor != null) {
            replaceDynastyFleshFeastModifier(
                    armor,
                    DYNASTY_FLESH_FEAST_ARMOR_MODIFIER_ID,
                    "ramesses_dynasty_flesh_feast_armor",
                    Math.max(0.0D, armorBonus));
        }
        if (healthChanged && living.isAlive()) {
            living.setHealth(Mth.clamp(living.getMaxHealth() * healthRatio, 1.0F, living.getMaxHealth()));
        }
    }

    private static boolean replaceDynastyFleshFeastModifier(
            AttributeInstance instance,
            UUID modifierId,
            String modifierName,
            double amount) {
        AttributeModifier existing = instance.getModifier(modifierId);
        if (amount <= 0.0D) {
            if (existing == null) {
                return false;
            }
            instance.removeModifier(existing);
            return true;
        }
        if (existing != null
                && Double.compare(existing.getAmount(), amount) == 0
                && existing.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL) {
            return false;
        }
        if (existing != null) {
            instance.removeModifier(existing);
        }
        instance.addTransientModifier(new AttributeModifier(
                modifierId,
                modifierName,
                amount,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        return true;
    }

    private static void removeDynastyFleshFeastModifiers(LivingEntity living) {
        applyDynastyFleshFeastModifiers(living, 0.0D, 0.0D);
    }

    private static void updateDynastyShieldRage(LivingEntity living, CompoundTag tag, int affixLevel) {
        if (!DynastyAffixData.enabled(DYNASTY_AFFIX_RAGE)
                || !isDynastyShieldAffixUnlocked(affixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_RAGE))) {
            tag.remove(DYNASTY_SHIELD_RAGE_LEVEL);
            removeDynastyShieldRageModifiers(living);
            return;
        }

        int rageLevel = DynastyAffixData.level(DYNASTY_AFFIX_RAGE, affixLevel);
        tag.putInt(DYNASTY_SHIELD_RAGE_LEVEL, rageLevel);
        if (tag.getFloat(DYNASTY_SHIELD_CURRENT) <= 0.0F) {
            removeDynastyShieldRageModifiers(living);
            return;
        }
        if (living instanceof Mob mob
                && mob.getTarget() instanceof Player player
                && AffixKnowledge.hasLearned(player, DYNASTY_AFFIX_RAGE)) {
            removeDynastyShieldRageModifiers(living);
            return;
        }

        applyDynastyShieldRageModifier(
                living,
                Attributes.ATTACK_SPEED,
                DYNASTY_RAGE_ATTACK_SPEED_MODIFIER_ID,
                "ramesses_dynasty_rage_attack_speed",
                DynastyAffixData.number(DYNASTY_AFFIX_RAGE, rageLevel, "attack_speed_bonus"));
        applyDynastyShieldRageModifier(
                living,
                Attributes.MOVEMENT_SPEED,
                DYNASTY_RAGE_MOVEMENT_SPEED_MODIFIER_ID,
                "ramesses_dynasty_rage_movement_speed",
                DynastyAffixData.number(DYNASTY_AFFIX_RAGE, rageLevel, "movement_speed_bonus"));
    }

    private static void applyDynastyShieldRageModifier(
            LivingEntity living,
            Attribute attribute,
            UUID modifierId,
            String modifierName,
            double amount) {
        AttributeInstance instance = living.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        AttributeModifier existing = instance.getModifier(modifierId);
        if (existing != null
                && Double.compare(existing.getAmount(), amount) == 0
                && existing.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL) {
            return;
        }
        if (existing != null) {
            instance.removeModifier(existing);
        }
        instance.addTransientModifier(new AttributeModifier(
                modifierId,
                modifierName,
                amount,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    private static void removeDynastyShieldRageModifiers(LivingEntity living) {
        removeDynastyShieldRageModifier(living, Attributes.ATTACK_SPEED, DYNASTY_RAGE_ATTACK_SPEED_MODIFIER_ID);
        removeDynastyShieldRageModifier(living, Attributes.MOVEMENT_SPEED, DYNASTY_RAGE_MOVEMENT_SPEED_MODIFIER_ID);
    }

    private static void removeDynastyShieldRageModifier(LivingEntity living, Attribute attribute, UUID modifierId) {
        AttributeInstance instance = living.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(modifierId);
        }
    }

    public static double getDynastyRageAttackSpeedBonus(LivingEntity living) {
        CompoundTag tag = living.getPersistentData();
        if (!Config.DYNASTY_SHIELD_ENABLED.get()
                || !DynastyAffixData.enabled(DYNASTY_AFFIX_RAGE)
                || !tag.getBoolean(DYNASTY_SHIELD_ASSIGNED)
                || tag.getFloat(DYNASTY_SHIELD_CURRENT) <= 0.0F) {
            return 0.0D;
        }
        int rageLevel = tag.getInt(DYNASTY_SHIELD_RAGE_LEVEL);
        return rageLevel > 0
                ? DynastyAffixData.number(DYNASTY_AFFIX_RAGE, rageLevel, "attack_speed_bonus")
                : 0.0D;
    }

    private static void updateDynastyShieldMoltenTag(CompoundTag tag, int affixLevel) {
        if (!DynastyAffixData.enabled(DYNASTY_AFFIX_MOLTEN)
                || !isDynastyShieldAffixUnlocked(affixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_MOLTEN))) {
            tag.remove(DYNASTY_SHIELD_MOLTEN);
            return;
        }
        tag.putBoolean(DYNASTY_SHIELD_MOLTEN, true);
    }

    private static void updateDynastyShieldBloodForfeitTag(CompoundTag tag, int affixLevel) {
        if (!DynastyAffixData.enabled(DYNASTY_AFFIX_BLOOD_FORFEIT)
                || !isDynastyShieldAffixUnlocked(affixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_BLOOD_FORFEIT))) {
            tag.remove(DYNASTY_SHIELD_BLOOD_FORFEIT);
            return;
        }
        tag.putBoolean(DYNASTY_SHIELD_BLOOD_FORFEIT, true);
    }

    private static boolean isDynastyBloodForfeitActive(LivingEntity living) {
        return Config.DYNASTY_SHIELD_ENABLED.get()
                && DynastyAffixData.enabled(DYNASTY_AFFIX_BLOOD_FORFEIT)
                && living instanceof Enemy
                && living.getPersistentData().getBoolean(DYNASTY_SHIELD_ASSIGNED)
                && living.getPersistentData().getBoolean(DYNASTY_SHIELD_BLOOD_FORFEIT);
    }

    private static void updateDynastyShieldFlightFallTag(CompoundTag tag, int affixLevel) {
        if (!DynastyAffixData.enabled(DYNASTY_AFFIX_FLIGHT_FALL)
                || !isDynastyShieldAffixUnlocked(affixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_FLIGHT_FALL))) {
            tag.remove(DYNASTY_SHIELD_FLIGHT_FALL);
            return;
        }
        tag.putBoolean(DYNASTY_SHIELD_FLIGHT_FALL, true);
    }

    private static void updateDynastyShieldClawRageTag(CompoundTag tag, int affixLevel) {
        if (!DynastyAffixData.enabled(DYNASTY_AFFIX_CLAW_RAGE)
                || !isDynastyShieldAffixUnlocked(affixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_CLAW_RAGE))) {
            tag.remove(DYNASTY_SHIELD_CLAW_RAGE_LEVEL);
            tag.remove(DYNASTY_CLAW_RAGE_ARMED);
            return;
        }
        tag.putInt(DYNASTY_SHIELD_CLAW_RAGE_LEVEL,
                DynastyAffixData.level(DYNASTY_AFFIX_CLAW_RAGE, affixLevel));
        if (tag.getFloat(DYNASTY_SHIELD_CURRENT) <= 0.0F) {
            tag.remove(DYNASTY_CLAW_RAGE_ARMED);
        }
    }

    private static boolean isDynastyFlightFallActive(LivingEntity living) {
        CompoundTag tag = living.getPersistentData();
        return Config.DYNASTY_SHIELD_ENABLED.get()
                && DynastyAffixData.enabled(DYNASTY_AFFIX_FLIGHT_FALL)
                && living instanceof Enemy
                && tag.getBoolean(DYNASTY_SHIELD_ASSIGNED)
                && tag.getBoolean(DYNASTY_SHIELD_FLIGHT_FALL)
                && tag.getFloat(DYNASTY_SHIELD_CURRENT) > 0.0F;
    }

    private static void tryTriggerDynastyFlightFall(ServerPlayer player, DamageSource source) {
        if (AffixKnowledge.hasLearned(player, DYNASTY_AFFIX_FLIGHT_FALL)
                || !player.getAbilities().flying
                || !(source.getEntity() instanceof LivingEntity attacker)
                || !isDynastyFlightFallActive(attacker)) {
            return;
        }

        int duration = Math.max(1, DynastyAffixData.intNumber(DYNASTY_AFFIX_FLIGHT_FALL, 1, "grounded_duration_ticks"));
        if (!player.addEffect(new MobEffectInstance(ModMobEffects.FLIGHT_FALL.get(), duration, 0, false, false, true))) {
            return;
        }
        CompoundTag data = player.getPersistentData();
        data.putBoolean(FLIGHT_FALL_RESTORE_MAYFLY,
                data.getBoolean(FLIGHT_FALL_RESTORE_MAYFLY) || player.getAbilities().mayfly);
        data.putBoolean(FLIGHT_FALL_LOCKED, true);
        data.remove(FLIGHT_FALL_LANDED_AT);
        suppressDynastyFlight(player);
        Vec3 movement = player.getDeltaMovement();
        player.setDeltaMovement(movement.x, Math.min(movement.y, -0.35D), movement.z);
        player.displayClientMessage(Component.translatable("message.ramesses_curse.flight_fall_triggered")
                .withStyle(ChatFormatting.DARK_RED), true);
    }

    private static void tickDynastyFlightFall(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.getBoolean(FLIGHT_FALL_LOCKED)) {
            return;
        }

        if (player.hasEffect(ModMobEffects.FLIGHT_FALL.get())) {
            suppressDynastyFlight(player);
            return;
        }

        boolean restoreMayfly = data.getBoolean(FLIGHT_FALL_RESTORE_MAYFLY);
        data.remove(FLIGHT_FALL_LOCKED);
        data.remove(FLIGHT_FALL_LANDED_AT);
        data.remove(FLIGHT_FALL_RESTORE_MAYFLY);
        if (restoreMayfly && !player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }
        player.displayClientMessage(Component.translatable("message.ramesses_curse.flight_fall_recovered")
                .withStyle(ChatFormatting.GREEN), true);
    }

    private static void suppressDynastyFlight(ServerPlayer player) {
        if (player.getAbilities().mayfly || player.getAbilities().flying) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
    }

    private static boolean isDynastyFlightFallLocked(Player player) {
        return player.getPersistentData().getBoolean(FLIGHT_FALL_LOCKED);
    }

    private static void applyBloodForfeitOnPlayerDeath(ServerPlayer player, DamageSource source) {
        if (AffixKnowledge.hasLearned(player, DYNASTY_AFFIX_BLOOD_FORFEIT)
                || !(source.getEntity() instanceof LivingEntity killer)
                || !isDynastyBloodForfeitActive(killer)) {
            return;
        }

        double minimumHealth = Math.max(1.0D, DynastyAffixData.number(DYNASTY_AFFIX_BLOOD_FORFEIT, 1, "minimum_max_health"));
        double configuredLoss = Math.max(0.0D, DynastyAffixData.number(DYNASTY_AFFIX_BLOOD_FORFEIT, 1, "max_health_loss"));
        double actualLoss = Math.min(configuredLoss, Math.max(0.0D, player.getMaxHealth() - minimumHealth));
        if (actualLoss <= 0.0D) {
            return;
        }

        CompoundTag data = getCurseData(player);
        double currentLoss = getBloodForfeitAmount(data);
        data.putDouble(BLOOD_FORFEIT_AMOUNT, currentLoss + actualLoss);
        data.remove(BLOOD_FORFEIT_LEDGER);
        data.putLong(BLOOD_FORFEIT_UNTIL, player.level().getGameTime() + BLOOD_FORFEIT_DURATION_TICKS);
        player.addEffect(new MobEffectInstance(
                ModMobEffects.BLOOD_FORFEIT.get(), BLOOD_FORFEIT_DURATION_TICKS, 0, false, true, true));
        syncBloodForfeitHealthPenalty(player);
        player.displayClientMessage(Component.translatable(
                "message.ramesses_curse.blood_forfeit_lost",
                formatBloodForfeitAmount(actualLoss)).withStyle(ChatFormatting.DARK_RED), false);
    }

    private static void tickBloodForfeit(Player player) {
        CompoundTag data = getCurseData(player);
        double amount = getBloodForfeitAmount(data);
        if (amount <= 0.0D) {
            return;
        }

        long until = data.getLong(BLOOD_FORFEIT_UNTIL);
        if (until > player.level().getGameTime()) {
            int remaining = (int) Math.min(Integer.MAX_VALUE, until - player.level().getGameTime());
            MobEffectInstance effect = player.getEffect(ModMobEffects.BLOOD_FORFEIT.get());
            if (effect == null || effect.getDuration() < remaining - 1) {
                player.addEffect(new MobEffectInstance(
                        ModMobEffects.BLOOD_FORFEIT.get(), remaining, 0, false, true, true));
            }
            return;
        }

        float previousMaxHealth = player.getMaxHealth();
        data.remove(BLOOD_FORFEIT_AMOUNT);
        data.remove(BLOOD_FORFEIT_UNTIL);
        data.remove(BLOOD_FORFEIT_LEDGER);
        player.removeEffect(ModMobEffects.BLOOD_FORFEIT.get());
        syncBloodForfeitHealthPenalty(player);
        float restoredMaxHealth = player.getMaxHealth();
        if (restoredMaxHealth > previousMaxHealth) {
            player.setHealth(Math.min(restoredMaxHealth, player.getHealth() + restoredMaxHealth - previousMaxHealth));
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundUpdateAttributesPacket(
                    player.getId(), List.of(player.getAttribute(Attributes.MAX_HEALTH))));
        }
        player.displayClientMessage(Component.translatable(
                "message.ramesses_curse.blood_forfeit_restored",
                formatBloodForfeitAmount(amount)).withStyle(ChatFormatting.GREEN), false);
    }

    private static double getBloodForfeitAmount(CompoundTag data) {
        double amount = Math.max(0.0D, data.getDouble(BLOOD_FORFEIT_AMOUNT));
        if (amount > 0.0D) {
            return amount;
        }
        CompoundTag legacyLedger = data.getCompound(BLOOD_FORFEIT_LEDGER);
        for (String key : legacyLedger.getAllKeys()) {
            amount += Math.max(0.0D, legacyLedger.getDouble(key));
        }
        return amount;
    }

    private static void syncBloodForfeitHealthPenalty(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }

        double totalLoss = getBloodForfeitAmount(getCurseData(player));

        maxHealth.removeModifier(BLOOD_FORFEIT_HEALTH_MODIFIER_ID);
        double minimumHealth = Math.max(1.0D, DynastyAffixData.number(DYNASTY_AFFIX_BLOOD_FORFEIT, 1, "minimum_max_health"));
        double appliedLoss = Math.min(totalLoss, Math.max(0.0D, player.getMaxHealth() - minimumHealth));
        if (appliedLoss > 0.0D) {
            maxHealth.addTransientModifier(new AttributeModifier(
                    BLOOD_FORFEIT_HEALTH_MODIFIER_ID,
                    "ramesses_dynasty_blood_forfeit",
                    -appliedLoss,
                    AttributeModifier.Operation.ADDITION));
        }
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static String formatBloodForfeitAmount(double amount) {
        return amount == Math.rint(amount) ? Integer.toString((int) amount) : String.format(Locale.ROOT, "%.1f", amount);
    }

    public static boolean isDynastyMoltenActive(LivingEntity living) {
        if (!Config.DYNASTY_SHIELD_ENABLED.get()
                || !DynastyAffixData.enabled(DYNASTY_AFFIX_MOLTEN)
                || living.level().isClientSide
                || !(living instanceof Enemy)
                || !living.isAlive()) {
            return false;
        }
        CompoundTag tag = living.getPersistentData();
        return tag.getBoolean(DYNASTY_SHIELD_ASSIGNED)
                && tag.getBoolean(DYNASTY_SHIELD_MOLTEN)
                && tag.getFloat(DYNASTY_SHIELD_CURRENT) > 0.0F;
    }

    private static void applyDynastyMoltenBurn(LivingEntity target, @Nullable LivingEntity source) {
        if (target.level().isClientSide || isDynastyMoltenActive(target)) {
            return;
        }
        int duration = Math.max(1,
                DynastyAffixData.intNumber(DYNASTY_AFFIX_MOLTEN, 1, "burn_duration_ticks"));
        long gameTime = target.level().getGameTime();
        CompoundTag tag = target.getPersistentData();
        tag.putLong(DYNASTY_MOLTEN_BURN_UNTIL,
                Math.max(tag.getLong(DYNASTY_MOLTEN_BURN_UNTIL), gameTime + duration));
        if (!tag.contains(DYNASTY_MOLTEN_BURN_NEXT_DAMAGE_AT, Tag.TAG_LONG)
                || tag.getLong(DYNASTY_MOLTEN_BURN_NEXT_DAMAGE_AT) < gameTime) {
            tag.putLong(DYNASTY_MOLTEN_BURN_NEXT_DAMAGE_AT, gameTime);
        }
        if (source != null) {
            tag.putUUID(DYNASTY_MOLTEN_BURN_SOURCE, source.getUUID());
        } else {
            tag.remove(DYNASTY_MOLTEN_BURN_SOURCE);
        }
        target.setSecondsOnFire(Mth.ceil(duration / 20.0F));
    }

    private static boolean isDynastyMoltenBurnActive(LivingEntity living) {
        return living.getPersistentData().getLong(DYNASTY_MOLTEN_BURN_UNTIL) > living.level().getGameTime();
    }

    private static void tickDynastyMoltenBurn(LivingEntity living) {
        CompoundTag tag = living.getPersistentData();
        long gameTime = living.level().getGameTime();
        long burnUntil = tag.getLong(DYNASTY_MOLTEN_BURN_UNTIL);
        if (burnUntil <= gameTime
                || isDynastyMoltenActive(living)
                || (living instanceof Player player && AffixKnowledge.hasLearned(player, DYNASTY_AFFIX_MOLTEN))) {
            tag.remove(DYNASTY_MOLTEN_BURN_UNTIL);
            tag.remove(DYNASTY_MOLTEN_BURN_NEXT_DAMAGE_AT);
            tag.remove(DYNASTY_MOLTEN_BURN_SOURCE);
            return;
        }
        if (gameTime < tag.getLong(DYNASTY_MOLTEN_BURN_NEXT_DAMAGE_AT)) {
            return;
        }

        int interval = Math.max(1,
                DynastyAffixData.intNumber(DYNASTY_AFFIX_MOLTEN, 1, "burn_interval_ticks"));
        float damage = Math.max(0.0F,
                (float) DynastyAffixData.number(DYNASTY_AFFIX_MOLTEN, 1, "burn_true_damage"));
        tag.putLong(DYNASTY_MOLTEN_BURN_NEXT_DAMAGE_AT, gameTime + interval);
        if (damage <= 0.0F) {
            return;
        }

        LivingEntity source = null;
        if (tag.hasUUID(DYNASTY_MOLTEN_BURN_SOURCE) && living.level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(tag.getUUID(DYNASTY_MOLTEN_BURN_SOURCE));
            if (entity instanceof LivingEntity livingSource) {
                source = livingSource;
            }
        }
        living.hurt(ModDamageTypes.moltenBurn(living.level(), source), damage);
    }

    private static void tickDynastyMoltenHazards(Player player) {
        if (AffixKnowledge.hasLearned(player, DYNASTY_AFFIX_MOLTEN)
                || !(player.level() instanceof ServerLevel level)
                || !DynastyAffixData.enabled(DYNASTY_AFFIX_MOLTEN)) {
            return;
        }
        int interval = Math.max(1, DynastyAffixData.intNumber(DYNASTY_AFFIX_MOLTEN, 1, "interval_ticks"));
        if ((level.getGameTime() + player.getId()) % interval != 0L) {
            return;
        }

        double radius = Math.max(1.0D, DynastyAffixData.number(DYNASTY_AFFIX_MOLTEN, 1, "radius"));
        LivingEntity source = level.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(radius),
                        ModEvents::isDynastyMoltenActive)
                .stream()
                .min((left, right) -> Double.compare(player.distanceToSqr(left), player.distanceToSqr(right)))
                .orElse(null);
        if (source == null) {
            return;
        }

        int cobwebDuration = Math.max(1, DynastyAffixData.intNumber(DYNASTY_AFFIX_MOLTEN, 1, "cobweb_duration_ticks"));
        placeTemporaryMoltenBlock(level, player.blockPosition(), Blocks.COBWEB, cobwebDuration);

        int configuredFireballCount = Math.max(0, DynastyAffixData.intNumber(DYNASTY_AFFIX_MOLTEN, 1, "fireball_count"));
        int fireballCount = configuredFireballCount == 0
                ? 0
                : 3 + level.random.nextInt(2);
        for (int index = 0; index < fireballCount; index++) {
            spawnMoltenFallingFireball(level, source, player);
        }
        int upwardFireballCount = Math.max(0,
                DynastyAffixData.intNumber(DYNASTY_AFFIX_MOLTEN, 1, "upward_fireball_count"));
        for (int index = 0; index < upwardFireballCount; index++) {
            spawnMoltenUpwardFireball(level, source, player);
        }
    }

    private static void spawnMoltenFallingFireball(ServerLevel level, LivingEntity source, Player player) {
        double angle = level.random.nextDouble() * Math.PI * 2.0D;
        double distance = Math.sqrt(level.random.nextDouble()) * 5.0D;
        double x = player.getX() + Math.cos(angle) * distance;
        double y = player.getY() + 8.0D + level.random.nextDouble() * 6.0D;
        double z = player.getZ() + Math.sin(angle) * distance;
        Vec3 target = new Vec3(x, player.getY(), z).subtract(x, y, z);
        SmallFireball fireball = new SmallFireball(level, source, target.x, target.y, target.z);
        fireball.setPos(x, y, z);
        fireball.getPersistentData().putBoolean(DYNASTY_MOLTEN_FIREBALL, true);
        level.addFreshEntity(fireball);
    }

    private static void spawnMoltenUpwardFireball(ServerLevel level, LivingEntity source, Player player) {
        double angle = level.random.nextDouble() * Math.PI * 2.0D;
        double distance = 2.0D + level.random.nextDouble() * 3.0D;
        double x = player.getX() + Math.cos(angle) * distance;
        double y = player.getY() + 0.15D;
        double z = player.getZ() + Math.sin(angle) * distance;
        Vec3 target = player.getEyePosition()
                .add((level.random.nextDouble() - 0.5D) * 0.8D, 0.5D, (level.random.nextDouble() - 0.5D) * 0.8D)
                .subtract(x, y, z);
        SmallFireball fireball = new SmallFireball(level, source, target.x, target.y, target.z);
        fireball.setPos(x, y, z);
        fireball.getPersistentData().putBoolean(DYNASTY_MOLTEN_FIREBALL, true);
        level.addFreshEntity(fireball);
    }

    private static void placeTemporaryMoltenBlock(ServerLevel level, BlockPos pos, Block block, int durationTicks) {
        if (!level.isLoaded(pos) || !level.getBlockState(pos).isAir()) {
            return;
        }
        Map<BlockPos, TemporaryMoltenBlock> blocks = TEMPORARY_MOLTEN_BLOCKS.computeIfAbsent(level.dimension(), ignored -> new HashMap<>());
        BlockPos immutablePos = pos.immutable();
        TemporaryMoltenBlock existing = blocks.get(immutablePos);
        long expiresAt = level.getGameTime() + durationTicks;
        if (existing == null) {
            blocks.put(immutablePos, new TemporaryMoltenBlock(level.getBlockState(pos), block, expiresAt));
        } else {
            blocks.put(immutablePos, new TemporaryMoltenBlock(existing.originalState(), existing.expectedBlock(), Math.max(existing.expiresAt(), expiresAt)));
        }
        level.setBlock(pos, block.defaultBlockState(), Block.UPDATE_ALL);
    }

    private static void restoreExpiredMoltenBlocks(ServerLevel level) {
        Map<BlockPos, TemporaryMoltenBlock> blocks = TEMPORARY_MOLTEN_BLOCKS.get(level.dimension());
        long gameTime = level.getGameTime();
        if (blocks != null && !blocks.isEmpty()) {
            blocks.entrySet().removeIf(entry -> {
                TemporaryMoltenBlock temporary = entry.getValue();
                if (gameTime < temporary.expiresAt()) {
                    return false;
                }
                BlockPos pos = entry.getKey();
                if (level.isLoaded(pos) && level.getBlockState(pos).is(temporary.expectedBlock())) {
                    level.setBlock(pos, temporary.originalState(), Block.UPDATE_ALL);
                }
                return true;
            });
            if (blocks.isEmpty()) {
                TEMPORARY_MOLTEN_BLOCKS.remove(level.dimension());
            }
        }
    }

    public static void activatePitchfireLavaWalking(ServerPlayer player, int durationTicks) {
        int duration = Math.max(1, durationTicks);
        long until = player.level().getGameTime() + duration;
        CompoundTag data = player.getPersistentData();
        data.putLong(PITCHFIRE_LAVA_WALK_UNTIL, Math.max(data.getLong(PITCHFIRE_LAVA_WALK_UNTIL), until));
        player.addEffect(new MobEffectInstance(ModMobEffects.PITCHFIRE_LAVA_WALK.get(), duration, 0, false, false, false));
    }

    public static boolean isPitchfireLavaWalkingActive(LivingEntity living) {
        return living.hasEffect(ModMobEffects.PITCHFIRE_LAVA_WALK.get())
                || (!living.level().isClientSide
                && living.getPersistentData().getLong(PITCHFIRE_LAVA_WALK_UNTIL) > living.level().getGameTime());
    }

    private static void tickPitchfireLavaWalking(Player player) {
        long until = player.getPersistentData().getLong(PITCHFIRE_LAVA_WALK_UNTIL);
        if (until <= player.level().getGameTime()) {
            player.getPersistentData().remove(PITCHFIRE_LAVA_WALK_UNTIL);
            return;
        }
        if (!player.hasEffect(ModMobEffects.PITCHFIRE_LAVA_WALK.get())) {
            int remaining = (int) Math.min(Integer.MAX_VALUE, until - player.level().getGameTime());
            player.addEffect(new MobEffectInstance(ModMobEffects.PITCHFIRE_LAVA_WALK.get(), remaining, 0, false, false, false));
        }
    }

    private static boolean isDynastyShieldPurgeActive(LivingEntity living) {
        if (!Config.DYNASTY_SHIELD_ENABLED.get()
                || !DynastyAffixData.enabled(DYNASTY_AFFIX_PURGE)
                || living.level().isClientSide
                || !(living instanceof Enemy)
                || !living.isAlive()) {
            return false;
        }
        CompoundTag tag = living.getPersistentData();
        return tag.getBoolean(DYNASTY_SHIELD_ASSIGNED)
                && tag.getBoolean(DYNASTY_SHIELD_PURGE)
                && tag.getLong("RamessesDynastyPurgeKnowledgeBypassUntil") <= living.level().getGameTime()
                && tag.getFloat(DYNASTY_SHIELD_CURRENT) > 0.0F;
    }

    public static void onAffixKnowledgeLearned(ServerPlayer player, String affixId) {
        CompoundTag data = getCurseData(player);
        switch (affixId) {
            case DYNASTY_AFFIX_SILENCE -> {
                data.remove(DYNASTY_SILENCE_MAIN_HAND_UNTIL);
                data.remove(DYNASTY_SILENCE_OFF_HAND_UNTIL);
                syncClientState(player, true);
            }
            case DYNASTY_AFFIX_FLIGHT_FALL -> {
                player.removeEffect(ModMobEffects.FLIGHT_FALL.get());
                tickDynastyFlightFall(player);
            }
            case DYNASTY_AFFIX_HIT_SLOW -> player.removeEffect(ModMobEffects.HIT_SLOW.get());
            case DYNASTY_AFFIX_BLOOD_FORFEIT -> {
                data.remove(BLOOD_FORFEIT_AMOUNT);
                data.remove(BLOOD_FORFEIT_UNTIL);
                data.remove(BLOOD_FORFEIT_LEDGER);
                player.removeEffect(ModMobEffects.BLOOD_FORFEIT.get());
                syncBloodForfeitHealthPenalty(player);
            }
            default -> {
            }
        }
    }

    private static boolean isUraeusNecklaceBypassEffect(LivingEntity living, MobEffect effect) {
        MobEffect necklaceEffect = Config.getNecklaceEffect();
        if (necklaceEffect == null || effect != necklaceEffect) {
            return false;
        }
        return living.getPersistentData().getLong(URAEUS_NECKLACE_EFFECT_BYPASS_UNTIL) >= living.level().getGameTime();
    }

    private static boolean isDynastyShieldProjectileWardActive(LivingEntity living) {
        if (!Config.DYNASTY_SHIELD_ENABLED.get()
                || !DynastyAffixData.enabled(DYNASTY_AFFIX_PROJECTILE_WARD)
                || living.level().isClientSide
                || !(living instanceof Enemy)
                || !living.isAlive()) {
            return false;
        }
        CompoundTag tag = living.getPersistentData();
        return tag.getBoolean(DYNASTY_SHIELD_ASSIGNED)
                && tag.getBoolean(DYNASTY_SHIELD_PROJECTILE_WARD)
                && tag.getFloat(DYNASTY_SHIELD_CURRENT) > 0.0F;
    }

    private static boolean isDynastyShieldProjectileWardBlockedDamage(LivingEntity living, DamageSource source) {
        if (!isDynastyShieldProjectileWardActive(living)) {
            return false;
        }
        if (ModItems.EternalSilenceCompoundBow.isSpecialProjectile(source.getDirectEntity())) {
            return false;
        }
        Player sourcePlayer = getSourcePlayer(source);
        if (sourcePlayer == null
                || AffixKnowledge.hasLearned(sourcePlayer, DYNASTY_AFFIX_PROJECTILE_WARD)
                || !isDynastyShieldAffixUnlocked(getDynastyShieldAffixLevel(sourcePlayer), DynastyAffixData.unlockTier(DYNASTY_AFFIX_PROJECTILE_WARD))) {
            return false;
        }
        Entity directEntity = source.getDirectEntity();
        return source.is(DamageTypeTags.IS_PROJECTILE)
                || directEntity instanceof Projectile
                || isLikelyModdedProjectileEntity(directEntity);
    }

    private static boolean isLikelyModdedProjectileEntity(@Nullable Entity entity) {
        if (entity == null || entity instanceof LivingEntity) {
            return false;
        }
        String className = entity.getClass().getName().toLowerCase(Locale.ROOT);
        return className.contains("projectile")
                || className.contains("bullet")
                || className.contains("ammo")
                || className.contains("arrow")
                || className.contains("bolt")
                || className.contains("shell");
    }

    private static void cleanseDynastyShieldHarmfulEffects(LivingEntity living) {
        if (!isDynastyShieldPurgeActive(living)) {
            return;
        }
        List<MobEffect> harmfulEffects = new ArrayList<>();
        for (MobEffectInstance effect : living.getActiveEffects()) {
            if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL
                    && !ModItems.isUraeusNecklaceStackEffect(living, effect.getEffect())) {
                harmfulEffects.add(effect.getEffect());
            }
        }
        for (MobEffect effect : harmfulEffects) {
            living.removeEffect(effect);
        }
    }

    private static double getDynastyShieldSilenceChance(int silenceLevel) {
        return DynastyAffixData.number(DYNASTY_AFFIX_SILENCE, silenceLevel, "chance");
    }

    private static int getDynastyShieldAffixLevel(@Nullable Player player) {
        int pressure = getDynastyPressure(player);
        int tier = 1;
        for (int index = 0; index < DYNASTY_AFFIX_THREAT_THRESHOLDS.length; index++) {
            if (pressure >= DYNASTY_AFFIX_THREAT_THRESHOLDS[index]) {
                tier = index + 1;
            }
        }
        return Mth.clamp(tier, 1, 10);
    }

    private static boolean isDynastyShieldAffixUnlocked(int affixLevel, int requiredLevel) {
        return affixLevel >= requiredLevel;
    }

    private static int getDynastyShieldAffixLevelForShieldLevel(int shieldLevel) {
        if (shieldLevel >= 9) {
            return 10;
        }
        if (shieldLevel >= 7) {
            return 7;
        }
        if (shieldLevel >= 5) {
            return 5;
        }
        if (shieldLevel >= 3) {
            return 3;
        }
        return 1;
    }

    private static void syncDynastyShield(LivingEntity living, boolean force) {
        syncDynastyShieldToPlayer(living, null, force);
    }

    private static void syncDynastyShieldToPlayer(LivingEntity living, @Nullable ServerPlayer player, boolean force) {
    }

    public static @Nullable DynastyShieldSnapshot getDynastyShieldSnapshot(LivingEntity living) {
        return getDynastyShieldSnapshot(living, null);
    }

    public static @Nullable DynastyShieldSnapshot getDynastyShieldSnapshot(LivingEntity living, @Nullable Player viewer) {
        if (!Config.DYNASTY_SHIELD_ENABLED.get() || !(living instanceof Enemy)) {
            return null;
        }
        CompoundTag tag = living.getPersistentData();
        if (!tag.getBoolean(DYNASTY_SHIELD_ASSIGNED)) {
            return null;
        }
        float max = tag.getFloat(DYNASTY_SHIELD_MAX);
        if (max <= 0.0F) {
            return null;
        }
        int viewerAffixLevel = getDynastyShieldAffixLevel(viewer);
        int unyieldingLevel = 0;
        if (DynastyAffixData.enabled(DYNASTY_AFFIX_UNYIELDING)
                && isDynastyShieldAffixUnlocked(viewerAffixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_UNYIELDING))
                && tag.getInt(DYNASTY_SHIELD_UNYIELDING_REVIVES) < DYNASTY_SHIELD_UNYIELDING_MAX_REVIVES) {
            unyieldingLevel = tag.getBoolean(DYNASTY_SHIELD_UNYIELDING_FIXED)
                    ? tag.getInt(DYNASTY_SHIELD_UNYIELDING_LEVEL)
                    : getDynastyShieldUnyieldingLevel(viewerAffixLevel);
        }
        if (unyieldingLevel <= 0
                && tag.getBoolean(DYNASTY_SHIELD_UNYIELDING_FIXED)
                && isDynastyShieldAffixUnlocked(viewerAffixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_UNYIELDING))) {
            unyieldingLevel = getDynastyShieldUnyieldingLevel(viewerAffixLevel);
        }
        return new DynastyShieldSnapshot(
                tag.getInt(DYNASTY_SHIELD_LEVEL),
                Mth.clamp(tag.getFloat(DYNASTY_SHIELD_CURRENT), 0.0F, max),
                max,
                DynastyAffixData.enabled(DYNASTY_AFFIX_DODGE) && isDynastyShieldAffixUnlocked(viewerAffixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_DODGE)) ? getDynastyShieldDodgeLevel(viewerAffixLevel) : 0,
                DynastyAffixData.enabled(DYNASTY_AFFIX_CORROSION) && isDynastyShieldAffixUnlocked(viewerAffixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_CORROSION)) ? getDynastyShieldCorrosionLevel(viewerAffixLevel) : 0,
                Mth.clamp(unyieldingLevel, 0, 3),
                DynastyAffixData.enabled(DYNASTY_AFFIX_DAMAGE_CAP) && isDynastyShieldAffixUnlocked(viewerAffixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_DAMAGE_CAP)) ? getDynastyShieldDamageCapLevel(viewerAffixLevel) : 0,
                DynastyAffixData.enabled(DYNASTY_AFFIX_SILENCE) && isDynastyShieldAffixUnlocked(viewerAffixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_SILENCE)) ? getDynastyShieldSilenceLevel(viewerAffixLevel) : 0,
                DynastyAffixData.enabled(DYNASTY_AFFIX_RAGE) ? Mth.clamp(tag.getInt(DYNASTY_SHIELD_RAGE_LEVEL), 0, 3) : 0,
                DynastyAffixData.enabled(DYNASTY_AFFIX_CLAW_RAGE) ? Mth.clamp(tag.getInt(DYNASTY_SHIELD_CLAW_RAGE_LEVEL), 0, 5) : 0,
                DynastyAffixData.enabled(DYNASTY_AFFIX_FLESH_FEAST) ? Mth.clamp(tag.getInt(DYNASTY_SHIELD_FLESH_FEAST_LEVEL), 0, 3) : 0,
                DynastyAffixData.enabled(DYNASTY_AFFIX_MIASMA) ? Mth.clamp(tag.getInt(DYNASTY_SHIELD_MIASMA_LEVEL), 0, 3) : 0,
                DynastyAffixData.enabled(DYNASTY_AFFIX_RESTORATION) ? Mth.clamp(tag.getInt(DYNASTY_SHIELD_RESTORATION_LEVEL), 0, 2) : 0,
                DynastyAffixData.enabled(DYNASTY_AFFIX_VULNERABILITY) ? Mth.clamp(tag.getInt(DYNASTY_SHIELD_VULNERABILITY_LEVEL), 0, 2) : 0,
                DynastyAffixData.enabled(DYNASTY_AFFIX_MOLTEN) && tag.getBoolean(DYNASTY_SHIELD_MOLTEN),
                DynastyAffixData.enabled(DYNASTY_AFFIX_PURGE) && isDynastyShieldAffixUnlocked(viewerAffixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_PURGE)) && tag.getBoolean(DYNASTY_SHIELD_PURGE),
                DynastyAffixData.enabled(DYNASTY_AFFIX_PROJECTILE_WARD) && isDynastyShieldAffixUnlocked(viewerAffixLevel, DynastyAffixData.unlockTier(DYNASTY_AFFIX_PROJECTILE_WARD)) && tag.getBoolean(DYNASTY_SHIELD_PROJECTILE_WARD),
                DynastyAffixData.enabled(DYNASTY_AFFIX_BLOOD_FORFEIT) && tag.getBoolean(DYNASTY_SHIELD_BLOOD_FORFEIT),
                DynastyAffixData.enabled(DYNASTY_AFFIX_FLIGHT_FALL) && tag.getBoolean(DYNASTY_SHIELD_FLIGHT_FALL),
                DynastyAffixData.enabled(DYNASTY_AFFIX_STEEL_BONE) && tag.getBoolean(DYNASTY_SHIELD_STEEL_BONE),
                DynastyAffixData.enabled(DYNASTY_AFFIX_BLOOD_LIMIT) && tag.getBoolean(DYNASTY_SHIELD_BLOOD_LIMIT),
                DynastyAffixData.enabled(DYNASTY_AFFIX_SHIELD_SHATTER) && tag.getBoolean(DYNASTY_SHIELD_SHATTER),
                DynastyAffixData.enabled(DYNASTY_AFFIX_DEATH_AUTHORITY) && tag.getBoolean(DYNASTY_SHIELD_DEATH_AUTHORITY),
                DynastyAffixData.enabled(DYNASTY_AFFIX_HIT_SLOW) && tag.getBoolean(DYNASTY_SHIELD_HIT_SLOW),
                DynastyAffixData.enabled(DYNASTY_AFFIX_COUNTER) && tag.getBoolean(DYNASTY_SHIELD_COUNTER));
    }

    public record DynastyShieldSnapshot(int level, float current, float max, int dodgeLevel, int corrosionLevel, int unyieldingLevel, int damageCapLevel, int silenceLevel, int rageLevel, int clawRageLevel, int fleshFeastLevel, int miasmaLevel, int restorationLevel, int vulnerabilityLevel, boolean molten, boolean purge, boolean projectileWard, boolean bloodForfeit, boolean flightFall, boolean steelBone, boolean bloodLimit, boolean shieldShatter, boolean deathAuthority, boolean hitSlow, boolean counter) {
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

    private record TemporaryMoltenBlock(BlockState originalState, Block expectedBlock, long expiresAt) {
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
        Vec3 crystalSpawnPos = getSatietyCrystalSpawnPos(player);
        SatietyCrystalEntity crystal = new SatietyCrystalEntity(player.level(), crystalSpawnPos, player.getUUID());
        player.level().addFreshEntity(crystal);
        rememberSatietyCrystal(player, crystal);
    }

    private static Vec3 getSatietyCrystalSpawnPos(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        double x = player.getX();
        double y = player.getY() + 2.2D;
        double z = player.getZ();
        double minSafeY = level.getMinBuildHeight() + 8.0D;
        if (y >= minSafeY) {
            return new Vec3(x, y, z);
        }

        BlockPos respawnPos = player.getRespawnPosition();
        if (respawnPos != null && level.dimension().equals(player.getRespawnDimension())) {
            return Vec3.atCenterOf(respawnPos.above());
        }

        int columnX = Mth.floor(x);
        int columnZ = Mth.floor(z);
        int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, columnX, columnZ);
        if (topY > level.getMinBuildHeight()) {
            return new Vec3(x, topY + 1.2D, z);
        }

        return Vec3.atCenterOf(level.getSharedSpawnPos().above());
    }

    private static void updateCurseNineHunger(ServerLevel level, Player player, boolean sealEquipped) {
        if (!sealEquipped) {
            return;
        }

        if (isCurseNineReversed(player)) {
            player.removeEffect(MobEffects.HUNGER);
            player.getFoodData().setFoodLevel(20);
            player.getFoodData().setSaturation(20.0F);
            if (player instanceof ServerPlayer serverPlayer) {
                CompoundTag data = getCurseData(serverPlayer);
                int maxDebt = getCurseNineMaxSatiety();
                boolean needsDebtCleanup = getSatietyCrystalCount(serverPlayer) > 0
                        || data.getLong(CURSE_9_PENALTY_UNTIL) > 0L
                        || data.getInt(CURSE_9_DEBT_TICKS) != maxDebt
                        || isCurseNinePenaltyActive(serverPlayer);
                if (needsDebtCleanup) {
                    clearCurseNineDebt(serverPlayer);
                }
            }
            return;
        }

        tickCurseNineDebt(level, player);
        int crystalCount = getSatietyCrystalCount(player);
        if (crystalCount <= 0) {
            return;
        }

        int maxFoodLevel = Math.max(0, 20 - (crystalCount * CURSE_9_HUNGER_LOSS));
        if (player.getFoodData().getFoodLevel() > maxFoodLevel) {
            player.getFoodData().setFoodLevel(maxFoodLevel);
        }
        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 220, 4, false, false, true));
    }

    public static void clearSealUnequipEffects(Player player) {
        if (player == null || player.level().isClientSide) {
            return;
        }
        player.removeEffect(MobEffects.HUNGER);
    }

    private static void tickCurseNineDebt(ServerLevel level, Player player) {
        CompoundTag data = getCurseData(player);
        long gameTime = level.getGameTime();
        int crystalCount = getSatietyCrystalCount(player);
        int maxDebt = getCurseNineMaxSatiety();

        if (crystalCount <= 0) {
            if (data.getInt(CURSE_9_DEBT_TICKS) != maxDebt) {
                data.putInt(CURSE_9_DEBT_TICKS, maxDebt);
            }
            return;
        }

        int debtTicks = data.contains(CURSE_9_DEBT_TICKS) ? Mth.clamp(data.getInt(CURSE_9_DEBT_TICKS), 0, maxDebt) : maxDebt;
        if (gameTime % CURSE_9_DEBT_DRAIN_INTERVAL == 0 && debtTicks > 0) {
            debtTicks = Math.max(0, debtTicks - 1);
            data.putInt(CURSE_9_DEBT_TICKS, debtTicks);
            if (debtTicks == 0 && player instanceof ServerPlayer serverPlayer) {
                applyCurseNinePenalty(serverPlayer, false);
            }
        }

        if (debtTicks <= 0 && crystalCount > 0) {
            if (player instanceof ServerPlayer serverPlayer && !data.getBoolean(CURSE_9_ALL_ATTRIBUTES_APPLIED)) {
                applyCurseNinePenalty(serverPlayer, data.contains(CURSE_9_PENALTY_UNTIL));
            }
            return;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            if (data.getBoolean(CURSE_9_ALL_ATTRIBUTES_APPLIED) || data.contains(CURSE_9_PENALTY_UNTIL)) {
                clearCurseNinePenalty(serverPlayer);
                data.remove(CURSE_9_PENALTY_UNTIL);
            }
        }
    }

    private static void applyCurseNinePenalty(ServerPlayer player, boolean clearLegacyEffects) {
        if (clearLegacyEffects) {
            removeLegacyCurseNineEffects(player);
        }
        removeLegacyCurseNineModifiers(player);
        for (Attribute attribute : ForgeRegistries.ATTRIBUTES.getValues()) {
            applyCurseNineModifier(
                    player,
                    attribute,
                    CURSE_9_ALL_ATTRIBUTES_MODIFIER,
                    -0.99D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
        }
        getCurseData(player).putBoolean(CURSE_9_ALL_ATTRIBUTES_APPLIED, true);
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void removeLegacyCurseNineEffects(ServerPlayer player) {
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        player.removeEffect(MobEffects.DARKNESS);
        player.removeEffect(MobEffects.WITHER);
        player.removeEffect(MobEffects.CONFUSION);
        player.removeEffect(MobEffects.WEAKNESS);
    }

    private static void removeLegacyCurseNineModifiers(ServerPlayer player) {
        removeModifier(player, Attributes.MAX_HEALTH, CURSE_9_HEALTH_MODIFIER);
        removeModifier(player, Attributes.LUCK, CURSE_9_LUCK_MODIFIER);
        removeModifier(player, Attributes.ARMOR, CURSE_9_ARMOR_MODIFIER);
        removeModifier(player, ForgeMod.BLOCK_REACH.get(), CURSE_9_BLOCK_REACH_MODIFIER);
        removeModifier(player, ForgeMod.ENTITY_REACH.get(), CURSE_9_ENTITY_REACH_MODIFIER);
    }

    private static void applyCurseNineModifier(ServerPlayer player, Attribute attribute, UUID id, double amount, AttributeModifier.Operation operation) {
        if (player.getAttribute(attribute) == null) {
            return;
        }
        player.getAttribute(attribute).removeModifier(id);
        player.getAttribute(attribute).addTransientModifier(new AttributeModifier(id, "ramesses_curse_curse_nine_penalty", amount, operation));
    }

    private static void clearCurseNinePenalty(ServerPlayer player) {
        CompoundTag data = getCurseData(player);
        if (!data.getBoolean(CURSE_9_ALL_ATTRIBUTES_APPLIED)) {
            removeLegacyCurseNineEffects(player);
        }
        removeLegacyCurseNineModifiers(player);
        for (Attribute attribute : ForgeRegistries.ATTRIBUTES.getValues()) {
            removeModifier(player, attribute, CURSE_9_ALL_ATTRIBUTES_MODIFIER);
        }
        data.remove(CURSE_9_ALL_ATTRIBUTES_APPLIED);
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static boolean isCurseNinePenaltyActive(ServerPlayer player) {
        CompoundTag data = getCurseData(player);
        return data.getBoolean(CURSE_9_ALL_ATTRIBUTES_APPLIED)
                || data.getLong(CURSE_9_PENALTY_UNTIL) > player.level().getGameTime();
    }

    private static void clearCurseNineDebt(ServerPlayer player) {
        int crystalCount = getSatietyCrystalCount(player);
        if (crystalCount > 0) {
            addSatietyCrystalCount(player, -crystalCount);
        }
        player.removeEffect(MobEffects.HUNGER);
        clearCurseNinePenalty(player);
        CompoundTag data = getCurseData(player);
        data.putInt(CURSE_9_DEBT_TICKS, getCurseNineMaxSatiety());
        data.remove(CURSE_9_PENALTY_UNTIL);
        MinecraftServer server = player.server;
        if (server != null && crystalCount > 0) {
            for (SatietyCrystalEntity crystal : findOwnedSatietyCrystalEntities(server.getAllLevels(), player.getUUID())) {
                crystal.discard();
            }
        }
        clearRememberedSatietyCrystal(player);
    }

    private static boolean isNetherContainerLoot(ResourceLocation lootTableId) {
        String id = lootTableId.toString();
        String path = lootTableId.getPath();
        return isContainerLoot(lootTableId)
                && (NETHER_BRIDGE_CHEST.equals(lootTableId)
                || BASTION_BRIDGE_CHEST.equals(lootTableId)
                || BASTION_HOGLIN_STABLE_CHEST.equals(lootTableId)
                || BASTION_OTHER_CHEST.equals(lootTableId)
                || BASTION_TREASURE_CHEST.equals(lootTableId)
                || RUINED_PORTAL_CHEST.equals(lootTableId)
                || id.contains("nether")
                || id.contains("bastion")
                || path.contains("ruined_portal"));
    }

    private static boolean isContainerLoot(ResourceLocation lootTableId) {
        return lootTableId.getPath().startsWith("chests/");
    }

    private static boolean isHoldingEmberOfDuat(Player player) {
        return player.getMainHandItem().is(ModItems.EMBER_OF_DUAT.get())
                || player.getOffhandItem().is(ModItems.EMBER_OF_DUAT.get());
    }

    private static List<SatietyCrystalEntity> findOwnedSatietyCrystalEntities(Iterable<ServerLevel> levels, UUID ownerId) {
        List<SatietyCrystalEntity> results = new ArrayList<>();
        for (ServerLevel level : levels) {
            results.addAll(level.getEntitiesOfClass(
                    SatietyCrystalEntity.class,
                    new AABB(-30000000, level.getMinBuildHeight() - 64, -30000000, 30000000, level.getMaxBuildHeight() + 64, 30000000),
                    entity -> ownerId.equals(entity.getOwnerUuid())));
        }
        return results;
    }

    private static int getSatietyCrystalCount(Player player) {
        return getCurseData(player).getInt(SATIETY_CRYSTAL_COUNT);
    }

    private static int getCurseNineDebtTicks(Player player) {
        CompoundTag data = getCurseData(player);
        int maxDebt = getCurseNineMaxSatiety();
        return data.contains(CURSE_9_DEBT_TICKS) ? Mth.clamp(data.getInt(CURSE_9_DEBT_TICKS), 0, maxDebt) : maxDebt;
    }

    private static int getCurseNineMaxSatiety() {
        return Math.max(1, Config.CURSE_9_MAX_SATIETY.get());
    }

    private static void addSatietyCrystalCount(Player player, int delta) {
        CompoundTag data = getCurseData(player);
        int maxDebt = getCurseNineMaxSatiety();
        int next = Math.max(0, data.getInt(SATIETY_CRYSTAL_COUNT) + delta);
        data.putInt(SATIETY_CRYSTAL_COUNT, next);
        if (next == 0) {
            data.putInt(CURSE_9_DEBT_TICKS, maxDebt);
            clearRememberedSatietyCrystal(player);
        } else if (!data.contains(CURSE_9_DEBT_TICKS)) {
            data.putInt(CURSE_9_DEBT_TICKS, maxDebt);
        }
    }

    private static void rememberSatietyCrystal(Player player, SatietyCrystalEntity crystal) {
        rememberSatietyCrystal(player, crystal.getUUID(), crystal.level().dimension(), crystal.blockPosition());
    }

    private static void rememberSatietyCrystal(Player player, UUID crystalId, ResourceKey<Level> dimension, BlockPos pos) {
        List<SatietyCrystalRecord> records = getRememberedSatietyCrystals(player);
        SatietyCrystalRecord updatedRecord = new SatietyCrystalRecord(crystalId, dimension, pos);
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
        data.putString(SATIETY_CRYSTAL_DIMENSION, dimension.location().toString());
        data.putInt(SATIETY_CRYSTAL_X, pos.getX());
        data.putInt(SATIETY_CRYSTAL_Y, pos.getY());
        data.putInt(SATIETY_CRYSTAL_Z, pos.getZ());
    }

    private static void clearRememberedSatietyCrystal(Player player) {
        CompoundTag data = getCurseData(player);
        data.remove(SATIETY_CRYSTAL_RECORDS);
        data.remove(SATIETY_CRYSTAL_DIMENSION);
        data.remove(SATIETY_CRYSTAL_X);
        data.remove(SATIETY_CRYSTAL_Y);
        data.remove(SATIETY_CRYSTAL_Z);
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
        for (SatietyCrystalEntity crystal : findOwnedSatietyCrystalEntities(server.getAllLevels(), target.getUUID())) {
            rememberSatietyCrystal(target, crystal);
            records.put(crystal.getUUID(), new SatietyCrystalRecord(crystal.getUUID(), crystal.level().dimension(), crystal.blockPosition()));
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
        data.put(LOCKED_SLOT_STACK, player.getInventory().items.get(slot).copy().save(new CompoundTag()));
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
        if (Config.isCurseFiveInnocentEntity(entity.getType())) {
            return true;
        }
        if (entity instanceof Enemy) {
            return false;
        }
        // Villagers, fish, and several passive vanilla mobs do not inherit Animal or NeutralMob.
        return entity instanceof Mob;
    }

    private static void applyGuiltLock(ServerPlayer player) {
        CompoundTag data = getCurseData(player);
        data.putBoolean(GUILT_SENTENCE_PENDING, false);
        data.putInt(GUILT_PROGRESS, 0);
        data.putLong(GUILT_LOCK_UNTIL, player.level().getGameTime() + Config.CURSE_5_LOCK_DURATION.get());
        data.putBoolean(CURSE_5_PUNISHMENT_COMPLETED, true);
        data.putBoolean(CURSE_5_DECISION_PENDING, true);
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
        CompoundTag curseData = persisted.getCompound(CURSE_DATA);
        migrateCurseSixTerminalState(curseData);
        return curseData;
    }

    private static void migrateCurseSixTerminalState(CompoundTag data) {
        if (!data.contains(CURSE_6_KEYWORD_UNLOCKED, Tag.TAG_BYTE) && data.contains("CurseSevenKeywordUnlocked", Tag.TAG_BYTE)) {
            data.putBoolean(CURSE_6_KEYWORD_UNLOCKED, data.getBoolean("CurseSevenKeywordUnlocked"));
        }
        if (!data.contains(CURSE_6_TERMINAL_SOLVED, Tag.TAG_BYTE) && data.contains("CurseSevenTerminalSolved", Tag.TAG_BYTE)) {
            data.putBoolean(CURSE_6_TERMINAL_SOLVED, data.getBoolean("CurseSevenTerminalSolved"));
        }
        if (!data.getBoolean(CURSE_6_STATE_MIGRATED)) {
            if (data.contains(CURSE_7_REVERSED, Tag.TAG_BYTE)) {
                if (!data.contains(CURSE_6_REVERSED, Tag.TAG_BYTE)) {
                    data.putBoolean(CURSE_6_REVERSED, data.getBoolean(CURSE_7_REVERSED));
                }
                data.remove(CURSE_7_REVERSED);
            }
            data.putBoolean(CURSE_6_STATE_MIGRATED, true);
        }
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

    private static int adjustMercy(CommandSourceStack source, Collection<ServerPlayer> targets, int value, GuiltOperation operation) {
        int affected = 0;

        for (ServerPlayer target : targets) {
            int current = getMercyProgress(target);
            int next = switch (operation) {
                case SET -> value;
                case ADD -> current + value;
                case REMOVE -> current - value;
            };

            setMercyProgress(target, next);
            affected++;
        }

        if (affected == 1) {
            ServerPlayer target = targets.iterator().next();
            source.sendSuccess(() -> Component.translatable(
                    "command.ramesses_curse.mercy.single",
                    target.getDisplayName(),
                    getMercyProgress(target),
                    Config.CURSE_5_MERCY_MAX.get()), true);
        } else {
            int affectedPlayers = affected;
            source.sendSuccess(() -> Component.translatable(
                    "command.ramesses_curse.mercy.multiple",
                    affectedPlayers,
                    Config.CURSE_5_MERCY_MAX.get()), true);
        }

        return affected;
    }

    private static int setDynastyShieldEnabled(CommandSourceStack source, boolean enabled) {
        Config.DYNASTY_SHIELD_ENABLED.set(enabled);
        Config.SPEC.save();
        if (!(source.getEntity() instanceof ServerPlayer)) {
            source.sendSuccess(() -> Component.literal(enabled
                    ? "Dynasty shield enabled."
                    : "Dynasty shield disabled."), true);
        }
        queueDynastyShieldToggleHint(source, enabled);
        return 1;
    }

    private static void queueDynastyShieldToggleHint(CommandSourceStack source, boolean enabled) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception ignored) {
            return;
        }

        String text = enabled
                ? "royal aegis restored; hostile blood now wears the old dynasty again"
                : "royal aegis withdrawn; hostile blood stands without the old dynasty";
        String translatedText = enabled
                ? "王胄已归位；敌对之血重新披上旧王朝的律甲"
                : "王胄已撤回；敌对之血暂离旧王朝的庇护";
        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new CurseSevenHintPacket(text, translatedText, "red_omen", DYNASTY_SHIELD_TOGGLE_HINT_TICKS));
    }

    private static int setDynastyShieldAffixesEnabled(CommandSourceStack source, boolean enabled) {
        DynastyAffixData.setAffixesEnabled(enabled);
        int refreshed = refreshLoadedDynastyShieldAffixes(source);
        source.sendSuccess(() -> Component.literal("Dynasty shield affix generation " + (enabled ? "enabled" : "disabled")
                + ". Refreshed " + refreshed + " loaded hostile entities."), true);
        return 1;
    }

    private static int refreshLoadedDynastyShieldAffixes(CommandSourceStack source) {
        int changed = 0;
        for (LivingEntity living : getLoadedHostileEntities(source)) {
            ensureDynastyShield(living, true);
            changed++;
        }
        return changed;
    }


    private static int refillLoadedDynastyShields(CommandSourceStack source) {
        return refillDynastyShield(source, getLoadedHostileEntities(source));
    }

    private static int resetLoadedDynastyShields(CommandSourceStack source) {
        return resetDynastyShield(source, getLoadedHostileEntities(source));
    }

    private static Collection<LivingEntity> getLoadedHostileEntities(CommandSourceStack source) {
        List<LivingEntity> entities = new ArrayList<>();
        for (ServerLevel level : source.getServer().getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof LivingEntity living && living instanceof Enemy && living.isAlive()) {
                    entities.add(living);
                }
            }
        }
        return entities;
    }

    private static int setDynastyShieldCurrent(CommandSourceStack source, Collection<? extends Entity> targets, int value) {
        int changed = 0;
        for (Entity entity : targets) {
            if (!(entity instanceof LivingEntity living) || !(living instanceof Enemy) || living.level().isClientSide) {
                continue;
            }
            ensureDynastyShield(living, false);
            CompoundTag tag = living.getPersistentData();
            if (!tag.getBoolean(DYNASTY_SHIELD_ASSIGNED)) {
                continue;
            }
            float max = Math.max(0.0F, tag.getFloat(DYNASTY_SHIELD_MAX));
            tag.putFloat(DYNASTY_SHIELD_CURRENT, Mth.clamp((float) value, 0.0F, max));
            syncDynastyShield(living, true);
            changed++;
        }
        int result = changed;
        source.sendSuccess(() -> Component.literal("Updated dynasty shields for " + result + " hostile entities."), true);
        return changed;
    }

    private static int setDynastyShieldMax(CommandSourceStack source, Collection<? extends Entity> targets, int value) {
        int changed = 0;
        for (Entity entity : targets) {
            if (!(entity instanceof LivingEntity living) || !(living instanceof Enemy) || living.level().isClientSide) {
                continue;
            }
            ensureDynastyShield(living, false);
            CompoundTag tag = living.getPersistentData();
            if (!tag.getBoolean(DYNASTY_SHIELD_ASSIGNED)) {
                continue;
            }
            float current = Mth.clamp(tag.getFloat(DYNASTY_SHIELD_CURRENT), 0.0F, value);
            tag.putFloat(DYNASTY_SHIELD_MAX, value);
            tag.putFloat(DYNASTY_SHIELD_CURRENT, current);
            syncDynastyShield(living, true);
            changed++;
        }
        int result = changed;
        source.sendSuccess(() -> Component.literal("Updated dynasty shields for " + result + " hostile entities."), true);
        return changed;
    }

    private static int refillDynastyShield(CommandSourceStack source, Collection<? extends Entity> targets) {
        int changed = 0;
        for (Entity entity : targets) {
            if (!(entity instanceof LivingEntity living) || !(living instanceof Enemy) || living.level().isClientSide) {
                continue;
            }
            ensureDynastyShield(living, false);
            CompoundTag tag = living.getPersistentData();
            if (!tag.getBoolean(DYNASTY_SHIELD_ASSIGNED)) {
                continue;
            }
            tag.putFloat(DYNASTY_SHIELD_CURRENT, Math.max(0.0F, tag.getFloat(DYNASTY_SHIELD_MAX)));
            syncDynastyShield(living, true);
            changed++;
        }
        int result = changed;
        source.sendSuccess(() -> Component.literal("Updated dynasty shields for " + result + " hostile entities."), true);
        return changed;
    }

    private static int resetDynastyShield(CommandSourceStack source, Collection<? extends Entity> targets) {
        int changed = 0;
        for (Entity entity : targets) {
            if (!(entity instanceof LivingEntity living) || !(living instanceof Enemy) || living.level().isClientSide) {
                continue;
            }
            CompoundTag tag = living.getPersistentData();
            tag.remove(DYNASTY_SHIELD_ASSIGNED);
            tag.remove(DYNASTY_SHIELD_LEVEL);
            tag.remove(DYNASTY_SHIELD_CURRENT);
            tag.remove(DYNASTY_SHIELD_MAX);
            tag.remove(DYNASTY_SHIELD_DODGE_LEVEL);
            tag.remove(DYNASTY_SHIELD_CORROSION_LEVEL);
            tag.remove(DYNASTY_SHIELD_UNYIELDING_LEVEL);
            tag.remove(DYNASTY_SHIELD_DAMAGE_CAP_LEVEL);
            tag.remove(DYNASTY_SHIELD_SILENCE_LEVEL);
            tag.remove(DYNASTY_SHIELD_PURGE);
            tag.remove(DYNASTY_SHIELD_PROJECTILE_WARD);
            tag.remove(DYNASTY_SHIELD_STEEL_BONE);
            tag.remove(DYNASTY_SHIELD_FLESH_FEAST_LEVEL);
            tag.remove(DYNASTY_SHIELD_MIASMA_LEVEL);
            tag.remove(DYNASTY_SHIELD_RESTORATION_LEVEL);
            tag.remove(DYNASTY_SHIELD_BLOOD_LIMIT);
            tag.remove(DYNASTY_SHIELD_SHATTER);
            tag.remove(DYNASTY_SHIELD_DEATH_AUTHORITY);
            tag.remove(DYNASTY_DEATH_AUTHORITY_READY_AT);
            tag.remove(DYNASTY_SHIELD_HIT_SLOW);
            tag.remove(DYNASTY_SHIELD_VULNERABILITY_LEVEL);
            tag.remove(DYNASTY_SHIELD_COUNTER);
            tag.remove(DYNASTY_SHIELD_COUNTER_USED);
            tag.remove(DYNASTY_FULL_AFFIX_HEALTH_APPLIED);
            tag.remove(DYNASTY_BLOOD_LIMIT_WINDOW_START);
            tag.remove(DYNASTY_BLOOD_LIMIT_WINDOW_DAMAGE);
            tag.remove(DYNASTY_SHIELD_RAGE_LEVEL);
            tag.remove(DYNASTY_SHIELD_CLAW_RAGE_LEVEL);
            tag.remove(DYNASTY_CLAW_RAGE_ARMED);
            tag.remove(DYNASTY_SHIELD_MOLTEN);
            tag.remove(DYNASTY_SHIELD_UNYIELDING_REVIVES);
            tag.remove(DYNASTY_SHIELD_UNYIELDING_FIXED);
            tag.remove(DYNASTY_SHIELD_LAST_SYNC_CURRENT);
            tag.remove(DYNASTY_SHIELD_LAST_SYNC_MAX);
            tag.remove(DYNASTY_SHIELD_THREAT_ASSIGNED);
            removeDynastyShieldRageModifiers(living);
            removeDynastyFleshFeastModifiers(living);
            AttributeInstance maxHealth = living.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.removeModifier(DYNASTY_FULL_AFFIX_HEALTH_MODIFIER_ID);
            }
            ensureDynastyShield(living, true);
            changed++;
        }
        int result = changed;
        source.sendSuccess(() -> Component.literal("Updated dynasty shields for " + result + " hostile entities."), true);
        return changed;
    }

    private static void ensureEmberRiddle(ServerPlayer player) {
        CompoundTag data = getCurseData(player);
        if (!data.contains(EMBER_RIDDLE_ROUND, Tag.TAG_LIST) || data.getList(EMBER_RIDDLE_ROUND, Tag.TAG_STRING).isEmpty()) {
            startNewEmberRiddleRound(player, false);
        }
    }

    private static EmberRiddle getCurrentEmberRiddle(Player player) {
        CompoundTag data = getCurseData(player);
        ListTag round = data.getList(EMBER_RIDDLE_ROUND, Tag.TAG_STRING);
        if (round.isEmpty()) {
            return null;
        }
        int index = Mth.clamp(data.getInt(EMBER_RIDDLE_INDEX), 0, round.size() - 1);
        String id = round.getString(index);
        return EMBER_RIDDLES.stream()
                .filter(riddle -> riddle.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    public static void startNewEmberRiddleRound(ServerPlayer player, boolean notify) {
        if (player == null || !isSealEquipped(player)) {
            return;
        }
        CompoundTag data = getCurseData(player);
        List<EmberRiddle> pool = new ArrayList<>(EMBER_RIDDLES);
        Collections.shuffle(pool, new java.util.Random(player.getRandom().nextLong()));
        int size = Math.min(12, pool.size());
        ListTag round = new ListTag();
        for (int i = 0; i < size; i++) {
            round.add(StringTag.valueOf(pool.get(i).id()));
        }
        data.put(EMBER_RIDDLE_ROUND, round);
        data.put(EMBER_RIDDLE_SOLVED, new ListTag());
        data.putInt(EMBER_RIDDLE_INDEX, 0);
        data.putBoolean(EMBER_RIDDLE_UNLOCKED, false);
        data.putBoolean(EMBER_RIDDLE_UNLOCKED_SNAPSHOT, false);
        syncEmberRiddleState(player);
        if (notify) {
            EmberRiddle first = getCurrentEmberRiddle(player);
            if (first != null) {
                player.displayClientMessage(Component.translatable("message.ramesses_curse.ember_riddle_next", first.title(), first.question())
                        .withStyle(net.minecraft.ChatFormatting.GRAY), false);
            } else {
                player.displayClientMessage(Component.translatable("message.ramesses_curse.ember_riddle_refresh")
                        .withStyle(net.minecraft.ChatFormatting.DARK_PURPLE), false);
            }
        }
    }

    private static void markEmberRiddleSolved(ServerPlayer player) {
        CompoundTag data = getCurseData(player);
        ListTag round = data.getList(EMBER_RIDDLE_ROUND, Tag.TAG_STRING);
        if (round.isEmpty()) {
            startNewEmberRiddleRound(player, false);
            round = data.getList(EMBER_RIDDLE_ROUND, Tag.TAG_STRING);
        }

        int currentIndex = Mth.clamp(data.getInt(EMBER_RIDDLE_INDEX), 0, Math.max(0, round.size() - 1));
        setEmberRiddleSolved(data, currentIndex, true);

        int nextIndex = getNextUnsolvedEmberRiddleIndex(data, round, currentIndex);
        if (nextIndex < 0) {
            data.putBoolean(EMBER_RIDDLE_UNLOCKED, true);
            data.putBoolean(EMBER_RIDDLE_UNLOCKED_SNAPSHOT, true);
            data.putInt(EMBER_RIDDLE_INDEX, currentIndex);
            syncEmberRiddleState(player);
            player.displayClientMessage(Component.translatable("message.ramesses_curse.ember_riddle_solved")
                    .withStyle(net.minecraft.ChatFormatting.GOLD), false);
            player.level().playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.9F, 1.2F);
            return;
        }

        data.putInt(EMBER_RIDDLE_INDEX, nextIndex);
        syncEmberRiddleState(player);
        EmberRiddle next = getCurrentEmberRiddle(player);
        if (next != null) {
            player.displayClientMessage(Component.translatable("message.ramesses_curse.ember_riddle_next", next.title(), next.question())
                    .withStyle(net.minecraft.ChatFormatting.GRAY), false);
        }
    }

    private static void syncEmberRiddleState(ServerPlayer player) {
        CompoundTag data = getCurseData(player);
        ListTag round = data.getList(EMBER_RIDDLE_ROUND, Tag.TAG_STRING);
        List<String> ids = new ArrayList<>(round.size());
        List<Boolean> solved = new ArrayList<>(round.size());
        for (int i = 0; i < round.size(); i++) {
            ids.add(round.getString(i));
            solved.add(isEmberRiddleSolved(data, i));
        }
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new EmberRiddleSyncPacket(ids, data.getInt(EMBER_RIDDLE_INDEX), data.getBoolean(EMBER_RIDDLE_UNLOCKED), solved));
    }

    private static EmberRiddle getEmberRiddleById(String id) {
        return EMBER_RIDDLES.stream()
                .filter(riddle -> riddle.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    private static void ensureSatietyRiddleRound(ServerPlayer player) {
        CompoundTag data = getCurseData(player);
        ListTag round = data.getList(SATIETY_RIDDLE_ROUND, Tag.TAG_STRING);
        if (round.size() == SATIETY_RIDDLE_COUNT) {
            return;
        }
        List<EmberRiddle> pool = new ArrayList<>(EMBER_RIDDLES);
        Collections.shuffle(pool, new java.util.Random(player.getRandom().nextLong()));
        round = new ListTag();
        for (int i = 0; i < Math.min(SATIETY_RIDDLE_COUNT, pool.size()); i++) {
            round.add(StringTag.valueOf(pool.get(i).id()));
        }
        data.put(SATIETY_RIDDLE_ROUND, round);
        data.put(SATIETY_RIDDLE_SOLVED, new ListTag());
        data.putInt(SATIETY_RIDDLE_INDEX, 0);
    }

    private static EmberRiddle getCurrentSatietyRiddle(Player player) {
        CompoundTag data = getCurseData(player);
        ListTag round = data.getList(SATIETY_RIDDLE_ROUND, Tag.TAG_STRING);
        if (round.isEmpty()) {
            return null;
        }
        int index = Mth.clamp(data.getInt(SATIETY_RIDDLE_INDEX), 0, round.size() - 1);
        return getEmberRiddleById(round.getString(index));
    }

    public static void submitSatietyRiddleAnswer(ServerPlayer player, String answer) {
        if (player == null || isCurseNineReversed(player) || !hasRecoverableSatietyCrystalDebt(player)) {
            return;
        }
        ensureSatietyRiddleRound(player);
        EmberRiddle riddle = getCurrentSatietyRiddle(player);
        if (riddle == null || !riddle.matches(normalizeRiddleAnswer(answer))) {
            player.displayClientMessage(Component.translatable("message.ramesses_curse.satiety_riddle_wrong")
                    .withStyle(net.minecraft.ChatFormatting.RED), false);
            return;
        }

        CompoundTag data = getCurseData(player);
        ListTag round = data.getList(SATIETY_RIDDLE_ROUND, Tag.TAG_STRING);
        int current = Mth.clamp(data.getInt(SATIETY_RIDDLE_INDEX), 0, round.size() - 1);
        setRiddleSolved(data, SATIETY_RIDDLE_SOLVED, round.size(), current, true);
        int next = getNextUnsolvedRiddleIndex(data, SATIETY_RIDDLE_SOLVED, round.size(), current);
        if (next >= 0) {
            data.putInt(SATIETY_RIDDLE_INDEX, next);
            syncSatietyRiddleState(player, false);
            player.displayClientMessage(Component.translatable("message.ramesses_curse.satiety_riddle_next")
                    .withStyle(net.minecraft.ChatFormatting.GRAY), false);
            return;
        }

        if (!consumeSatietyCrystal(player)) {
            player.displayClientMessage(Component.translatable("message.ramesses_curse.satiety_riddle_missing_crystal")
                    .withStyle(net.minecraft.ChatFormatting.RED), false);
            resetSatietyRiddleRound(data);
            syncSatietyRiddleState(player, false);
            return;
        }
        restoreSatietyCrystalDebt(player);
        resetSatietyRiddleRound(data);
        syncSatietyRiddleState(player, false);
        player.displayClientMessage(Component.translatable("message.ramesses_curse.satiety_riddle_solved")
                .withStyle(net.minecraft.ChatFormatting.GOLD), false);
    }

    public static void selectSatietyRiddle(ServerPlayer player, int index) {
        if (player == null || !hasRecoverableSatietyCrystalDebt(player)) {
            return;
        }
        ensureSatietyRiddleRound(player);
        CompoundTag data = getCurseData(player);
        ListTag round = data.getList(SATIETY_RIDDLE_ROUND, Tag.TAG_STRING);
        int clamped = Mth.clamp(index, 0, Math.max(0, round.size() - 1));
        if (!isRiddleSolved(data, SATIETY_RIDDLE_SOLVED, clamped)) {
            data.putInt(SATIETY_RIDDLE_INDEX, clamped);
            syncSatietyRiddleState(player, false);
        }
    }

    private static void syncSatietyRiddleState(ServerPlayer player, boolean openScreen) {
        CompoundTag data = getCurseData(player);
        ListTag round = data.getList(SATIETY_RIDDLE_ROUND, Tag.TAG_STRING);
        List<String> ids = new ArrayList<>(round.size());
        List<Boolean> solved = new ArrayList<>(round.size());
        for (int i = 0; i < round.size(); i++) {
            ids.add(round.getString(i));
            solved.add(isRiddleSolved(data, SATIETY_RIDDLE_SOLVED, i));
        }
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new SatietyRiddleSyncPacket(ids, data.getInt(SATIETY_RIDDLE_INDEX), solved, openScreen));
    }

    private static void resetSatietyRiddleRound(CompoundTag data) {
        data.remove(SATIETY_RIDDLE_ROUND);
        data.remove(SATIETY_RIDDLE_SOLVED);
        data.remove(SATIETY_RIDDLE_INDEX);
    }

    private static boolean isRiddleSolved(CompoundTag data, String solvedKey, int index) {
        ListTag solved = data.getList(solvedKey, Tag.TAG_BYTE);
        return index >= 0 && index < solved.size()
                && solved.get(index) instanceof net.minecraft.nbt.ByteTag byteTag
                && byteTag.getAsByte() != 0;
    }

    private static void setRiddleSolved(CompoundTag data, String solvedKey, int roundSize, int index, boolean value) {
        ListTag solved = data.getList(solvedKey, Tag.TAG_BYTE);
        while (solved.size() < roundSize) {
            solved.add(net.minecraft.nbt.ByteTag.valueOf(false));
        }
        if (index >= 0 && index < solved.size()) {
            solved.set(index, net.minecraft.nbt.ByteTag.valueOf(value));
        }
        data.put(solvedKey, solved);
    }

    private static int getNextUnsolvedRiddleIndex(CompoundTag data, String solvedKey, int roundSize, int startIndex) {
        for (int step = 1; step <= roundSize; step++) {
            int candidate = (startIndex + step) % roundSize;
            if (!isRiddleSolved(data, solvedKey, candidate)) {
                return candidate;
            }
        }
        return -1;
    }

    private static String normalizeRiddleAnswer(String input) {
        return input == null ? "" : input.toLowerCase(Locale.ROOT).replace(" ", "").replace("銆€", "");
    }

    @OnlyIn(Dist.CLIENT)
    public static Component getClientCurrentEmberRiddleHint() {
        EmberRiddle riddle = ClientOverlayEvents.getClientCurrentRiddle();
        if (riddle == null) {
            return Component.translatable("message.ramesses_curse.ember_riddle_missing");
        }
        return Component.translatable("message.ramesses_curse.ember_riddle_hint", riddle.title(), riddle.question());
    }

    @OnlyIn(Dist.CLIENT)
    public static String getClientCurrentEmberRiddleInscription() {
        EmberRiddle riddle = ClientOverlayEvents.getClientCurrentRiddle();
        if (riddle == null || riddle.answers().isEmpty()) {
            return "";
        }
        return getEmberCipherGlyphs(riddle.answers().get(0));
    }

    @OnlyIn(Dist.CLIENT)
    public static Component getClientCurrentSatietyRiddleHint() {
        EmberRiddle riddle = ClientOverlayEvents.getClientCurrentSatietyRiddle();
        return riddle == null
                ? Component.translatable("message.ramesses_curse.ember_riddle_missing")
                : Component.translatable("message.ramesses_curse.ember_riddle_hint", riddle.title(), riddle.question());
    }

    @OnlyIn(Dist.CLIENT)
    public static String getClientCurrentSatietyRiddleInscription() {
        EmberRiddle riddle = ClientOverlayEvents.getClientCurrentSatietyRiddle();
        return riddle == null || riddle.answers().isEmpty() ? "" : getEmberCipherGlyphs(riddle.answers().get(0));
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean shouldShowCurseSevenDuatEntry() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null
                && isSealEquipped(minecraft.player)
                && ClientOverlayEvents.isClientCurseSevenKeywordUnlocked()
                && !ClientOverlayEvents.isClientCurseSevenReversed();
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean isClientCurseSevenKeywordLockedForRiddleRefresh() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null
                && isSealEquipped(minecraft.player)
                && ClientOverlayEvents.isClientCurseSevenKeywordUnlocked()
                && !ClientOverlayEvents.isClientCurseSevenReversed();
    }

    @OnlyIn(Dist.CLIENT)
    public static Component getClientCurseSevenDuatHint() {
        return Component.literal("余烬侧壁浮现出一行更古老的抄录，它不再追问答案，而是指向终末王座下的虚空。");
    }

    @OnlyIn(Dist.CLIENT)
    public static String getClientCurseSevenDuatInscription() {
        return getEmberCipherGlyphs("end main island");
    }

    private static boolean isEmberRiddleSolved(CompoundTag data, int index) {
        ListTag solved = data.getList(EMBER_RIDDLE_SOLVED, Tag.TAG_BYTE);
        return index >= 0 && index < solved.size() && solved.get(index) instanceof net.minecraft.nbt.ByteTag byteTag && byteTag.getAsByte() != 0;
    }

    private static void setEmberRiddleSolved(CompoundTag data, int index, boolean value) {
        ListTag round = data.getList(EMBER_RIDDLE_ROUND, Tag.TAG_STRING);
        ListTag solved = data.getList(EMBER_RIDDLE_SOLVED, Tag.TAG_BYTE);
        while (solved.size() < round.size()) {
            solved.add(net.minecraft.nbt.ByteTag.valueOf(false));
        }
        if (index >= 0 && index < solved.size()) {
            solved.set(index, net.minecraft.nbt.ByteTag.valueOf(value));
        }
        data.put(EMBER_RIDDLE_SOLVED, solved);
    }

    private static int getNextUnsolvedEmberRiddleIndex(CompoundTag data, ListTag round, int startIndex) {
        if (round.isEmpty()) {
            return -1;
        }
        for (int step = 1; step <= round.size(); step++) {
            int candidate = (startIndex + step) % round.size();
            if (!isEmberRiddleSolved(data, candidate)) {
                return candidate;
            }
        }
        return -1;
    }

    public static String getEmberCipherLetters(String answer) {
        if (answer == null || answer.isBlank()) {
            return "";
        }
        String english = answer.toUpperCase(Locale.ROOT);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < english.length(); i++) {
            char c = english.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    public static String getEmberCipherGlyphs(String answer) {
        String letters = getEmberCipherLetters(answer);
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < letters.length(); i++) {
            if (i > 0 && i % 12 == 0) {
                builder.append('\n');
            }
            builder.append(EMBER_CIPHER_GLYPHS[letters.charAt(i) - 'A']);
        }
        return builder.toString();
    }

    public static String getEmberCipherAlphabetPreview() {
        return "\uE000 A  \uE001 B  \uE002 C  \uE003 D  \uE004 E  \uE005 F\n"
                + "\uE006 G  \uE007 H  \uE008 I  \uE009 J  \uE00A K  \uE00B L\n"
                + "\uE00C M  \uE00D N  \uE00E O  \uE00F P  \uE010 Q  \uE011 R\n"
                + "\uE012 S  \uE013 T  \uE014 U  \uE015 V  \uE016 W  \uE017 X\n"
                + "\uE018 Y  \uE019 Z";
    }

    @OnlyIn(Dist.CLIENT)
    public static String getClientCurrentEmberRiddleAnswerLetters() {
        EmberRiddle riddle = ClientOverlayEvents.getClientCurrentRiddle();
        if (riddle == null || riddle.answers().isEmpty()) {
            return "";
        }
        return getEmberCipherLetters(riddle.answers().get(0));
    }

    @OnlyIn(Dist.CLIENT)
    public static String getClientCurrentEmberRiddleGlyphs() {
        EmberRiddle riddle = ClientOverlayEvents.getClientCurrentRiddle();
        if (riddle == null || riddle.answers().isEmpty()) {
            return "";
        }
        return getEmberCipherGlyphs(riddle.answers().get(0));
    }

    private enum GuiltOperation {
        SET,
        ADD,
        REMOVE
    }

    public enum EmberTrialActivationResult {
        STARTED,
        RIDDLE_LOCKED,
        ALREADY_ACTIVE
    }

    private record EmberRiddle(String id, String title, int page, String question, List<String> answers) {
        private EmberRiddle(String id, String title, int page, String question, String... answers) {
            this(id, title, page, question, Arrays.asList(answers));
        }

        private boolean matches(String input) {
            if (input == null || input.isEmpty()) {
                return false;
            }
            return answers.stream()
                    .map(ModEvents::normalizeRiddleAnswer)
                    .anyMatch(input::equals);
        }
    }

    private static void syncGuiltState(Player player, int guilt, int mercy, boolean mercyChargeReady, boolean sealEquipped, int sealEquipSeconds, boolean netherAwakened, boolean endAwakened, boolean curseOneReversed, boolean curseTwoReversed, boolean curseFiveReversed, boolean curseFiveDeclined, boolean curseSevenKeywordUnlocked, boolean curseSevenTerminalSolved, boolean curseSixReversed, boolean curseSevenReversed, boolean curseSevenSentenceUnlocked, boolean curseEightReversed, boolean curseNineReversed, int lockTicks, int satietyCrystalCount, int curseNineDebtTicks, int lockedSlot, int lockedSlotTicks, int dynastyThreat, int dynastySilenceMainHandTicks, int dynastySilenceOffHandTicks) {
        if (player instanceof ServerPlayer serverPlayer) {
            ModNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new GuiltSyncPacket(guilt, mercy, mercyChargeReady, sealEquipped, sealEquipSeconds, netherAwakened, endAwakened, curseOneReversed, curseTwoReversed, curseFiveReversed, curseFiveDeclined, curseSevenKeywordUnlocked, curseSevenTerminalSolved, curseSixReversed, curseSevenReversed, curseSevenSentenceUnlocked, curseEightReversed, curseNineReversed, lockTicks, satietyCrystalCount, curseNineDebtTicks, lockedSlot, lockedSlotTicks, dynastyThreat, dynastySilenceMainHandTicks, dynastySilenceOffHandTicks));
        }
    }

    private static void syncClientState(Player player, boolean force) {
        CompoundTag data = getCurseData(player);
        int guilt = data.getInt(GUILT_PROGRESS);
        int mercy = data.getInt(MERCY_PROGRESS);
        boolean mercyChargeReady = data.getBoolean(MERCY_CHARGE_READY);
        boolean sealEquipped = isSealEquipped(player);
        int sealEquipSeconds = data.getInt(EQUIP_TICKS) / 20;
        boolean netherAwakened = data.getBoolean(NETHER_AWAKENED);
        boolean endAwakened = data.getBoolean(END_AWAKENED);
        boolean curseOneReversed = data.getBoolean(CURSE_1_REVERSED);
        boolean curseTwoReversed = data.getBoolean(CURSE_2_REVERSED);
        boolean curseFiveReversed = data.getBoolean(CURSE_5_REVERSED);
        boolean curseFiveDeclined = data.getBoolean(CURSE_5_DECLINED);
        boolean curseSevenKeywordUnlocked = data.getBoolean(CURSE_6_KEYWORD_UNLOCKED);
        boolean curseSevenTerminalSolved = data.getBoolean(CURSE_6_TERMINAL_SOLVED);
        boolean curseSixReversed = data.getBoolean(CURSE_6_REVERSED);
        boolean curseSevenReversed = data.getBoolean(CURSE_7_REVERSED);
        boolean curseSevenSentenceUnlocked = data.getBoolean(CURSE_6_SENTENCE_UNLOCKED);
        boolean curseEightReversed = data.getBoolean(CURSE_8_REVERSED);
        boolean curseNineReversed = data.getBoolean(CURSE_9_REVERSED);
        int lockSeconds = Mth.ceil(getRemainingGuiltLockTicks(player) / 20.0F);
        int satietyCrystalCount = getSatietyCrystalCount(player);
        int curseNineDebtTicks = getCurseNineDebtTicks(player);
        int lockedSlot = data.getInt(LOCKED_SLOT);
        int lockedSlotTicks = getRemainingLockedSlotTicks(player);
        int dynastyThreat = getDynastyPressure(player);
        int dynastySilenceMainHandTicks = getRemainingDynastySilenceTicks(player, InteractionHand.MAIN_HAND);
        int dynastySilenceOffHandTicks = getRemainingDynastySilenceTicks(player, InteractionHand.OFF_HAND);

        if (!force
                && data.getInt(LAST_SYNC_GUILT) == guilt
                && data.getInt(LAST_SYNC_MERCY) == mercy
                && data.getBoolean(LAST_SYNC_MERCY_CHARGE) == mercyChargeReady
                && data.getBoolean(LAST_SYNC_SEAL) == sealEquipped
                && data.getInt(LAST_SYNC_SEAL_EQUIP_SECONDS) == sealEquipSeconds
                && data.getBoolean(LAST_SYNC_NETHER_AWAKENED) == netherAwakened
                && data.getBoolean(LAST_SYNC_END_AWAKENED) == endAwakened
                && data.getBoolean(LAST_SYNC_CURSE_1_REVERSED) == curseOneReversed
                && data.getBoolean(LAST_SYNC_CURSE_2_REVERSED) == curseTwoReversed
                && data.getBoolean(LAST_SYNC_CURSE_5_REVERSED) == curseFiveReversed
                && data.getBoolean(LAST_SYNC_CURSE_5_DECLINED) == curseFiveDeclined
                && data.getBoolean(LAST_SYNC_CURSE_6_KEYWORD_UNLOCKED) == curseSevenKeywordUnlocked
                && data.getBoolean(LAST_SYNC_CURSE_6_TERMINAL_SOLVED) == curseSevenTerminalSolved
                && data.getBoolean(LAST_SYNC_CURSE_6_REVERSED) == curseSixReversed
                && data.getBoolean(LAST_SYNC_CURSE_7_REVERSED) == curseSevenReversed
                && data.getBoolean(LAST_SYNC_CURSE_6_SENTENCE_UNLOCKED) == curseSevenSentenceUnlocked
                && data.getBoolean(LAST_SYNC_CURSE_8_REVERSED) == curseEightReversed
                && data.getBoolean(LAST_SYNC_CURSE_9_REVERSED) == curseNineReversed
                && data.getInt(LAST_SYNC_LOCK_SECONDS) == lockSeconds
                && data.getInt(LAST_SYNC_SATIETY) == satietyCrystalCount
                && data.getInt(LAST_SYNC_CURSE_9_DEBT) == curseNineDebtTicks
                && data.getInt(LAST_SYNC_LOCKED_SLOT) == lockedSlot
                && data.getInt(LAST_SYNC_LOCKED_SLOT_TICKS) == lockedSlotTicks
                && data.getInt(LAST_SYNC_DYNASTY_PRESSURE) == dynastyThreat
                && data.getInt(LAST_SYNC_DYNASTY_SILENCE_MAIN_HAND_TICKS) == dynastySilenceMainHandTicks
                && data.getInt(LAST_SYNC_DYNASTY_SILENCE_OFF_HAND_TICKS) == dynastySilenceOffHandTicks) {
            return;
        }

        data.putInt(LAST_SYNC_GUILT, guilt);
        data.putInt(LAST_SYNC_MERCY, mercy);
        data.putBoolean(LAST_SYNC_MERCY_CHARGE, mercyChargeReady);
        data.putBoolean(LAST_SYNC_SEAL, sealEquipped);
        data.putInt(LAST_SYNC_SEAL_EQUIP_SECONDS, sealEquipSeconds);
        data.putBoolean(LAST_SYNC_NETHER_AWAKENED, netherAwakened);
        data.putBoolean(LAST_SYNC_END_AWAKENED, endAwakened);
        data.putBoolean(LAST_SYNC_CURSE_1_REVERSED, curseOneReversed);
        data.putBoolean(LAST_SYNC_CURSE_2_REVERSED, curseTwoReversed);
        data.putBoolean(LAST_SYNC_CURSE_5_REVERSED, curseFiveReversed);
        data.putBoolean(LAST_SYNC_CURSE_5_DECLINED, curseFiveDeclined);
        data.putBoolean(LAST_SYNC_CURSE_6_KEYWORD_UNLOCKED, curseSevenKeywordUnlocked);
        data.putBoolean(LAST_SYNC_CURSE_6_TERMINAL_SOLVED, curseSevenTerminalSolved);
        data.putBoolean(LAST_SYNC_CURSE_6_REVERSED, curseSixReversed);
        data.putBoolean(LAST_SYNC_CURSE_7_REVERSED, curseSevenReversed);
        data.putBoolean(LAST_SYNC_CURSE_6_SENTENCE_UNLOCKED, curseSevenSentenceUnlocked);
        data.putBoolean(LAST_SYNC_CURSE_8_REVERSED, curseEightReversed);
        data.putBoolean(LAST_SYNC_CURSE_9_REVERSED, curseNineReversed);
        data.putInt(LAST_SYNC_LOCK_SECONDS, lockSeconds);
        data.putInt(LAST_SYNC_SATIETY, satietyCrystalCount);
        data.putInt(LAST_SYNC_CURSE_9_DEBT, curseNineDebtTicks);
        data.putInt(LAST_SYNC_LOCKED_SLOT, lockedSlot);
        data.putInt(LAST_SYNC_LOCKED_SLOT_TICKS, lockedSlotTicks);
        data.putInt(LAST_SYNC_DYNASTY_PRESSURE, dynastyThreat);
        data.putInt(LAST_SYNC_DYNASTY_SILENCE_MAIN_HAND_TICKS, dynastySilenceMainHandTicks);
        data.putInt(LAST_SYNC_DYNASTY_SILENCE_OFF_HAND_TICKS, dynastySilenceOffHandTicks);
        syncGuiltState(player, guilt, mercy, mercyChargeReady, sealEquipped, sealEquipSeconds, netherAwakened, endAwakened, curseOneReversed, curseTwoReversed, curseFiveReversed, curseFiveDeclined, curseSevenKeywordUnlocked, curseSevenTerminalSolved, curseSixReversed, curseSevenReversed, curseSevenSentenceUnlocked, curseEightReversed, curseNineReversed, lockSeconds * 20, satietyCrystalCount, curseNineDebtTicks, lockedSlot, lockedSlotTicks, dynastyThreat, dynastySilenceMainHandTicks, dynastySilenceOffHandTicks);
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

    private static void applyDynastySilenceCooldown(ServerPlayer player, int mainHandTicks, int offHandTicks) {
        if (mainHandTicks > 0) {
            applyCooldown(player, player.getMainHandItem(), mainHandTicks);
        }
        if (offHandTicks > 0) {
            applyCooldown(player, player.getOffhandItem(), offHandTicks);
        }
    }

    private static void applyLockedSlotCooldown(ServerPlayer player, int remainingTicks) {
        CompoundTag data = getCurseData(player);
        if (!hasActiveLockedSlot(player, data)) {
            return;
        }
        int slot = data.getInt(LOCKED_SLOT);
        if (slot < 0 || slot >= player.getInventory().items.size()) {
            return;
        }

        ItemStack lockedStack = player.getInventory().items.get(slot);
        if (!lockedStack.isEmpty()) {
            player.getCooldowns().addCooldown(lockedStack.getItem(), remainingTicks);
        }
    }

    private static boolean hasActiveLockedSlot(Player player, CompoundTag data) {
        long until = data.getLong(LOCKED_SLOT_UNTIL);
        if (until <= player.level().getGameTime()) {
            clearLockedSlotState(data);
            return false;
        }
        int slot = data.getInt(LOCKED_SLOT);
        if (slot < 0 || slot >= player.getInventory().items.size() || !data.contains(LOCKED_SLOT_STACK, Tag.TAG_COMPOUND)) {
            clearLockedSlotState(data);
            return false;
        }
        ItemStack current = player.getInventory().items.get(slot);
        ItemStack lockedSnapshot = ItemStack.of(data.getCompound(LOCKED_SLOT_STACK));
        if (current.isEmpty() || lockedSnapshot.isEmpty() || !ItemStack.isSameItemSameTags(current, lockedSnapshot)) {
            clearLockedSlotState(data);
            return false;
        }
        return true;
    }

    private static void normalizeLockedSlotState(Player player, CompoundTag data) {
        hasActiveLockedSlot(player, data);
    }

    private static void clearLockedSlotState(CompoundTag data) {
        data.putInt(LOCKED_SLOT, -1);
        data.putLong(LOCKED_SLOT_UNTIL, 0L);
        data.remove(LOCKED_SLOT_STACK);
    }

    private static void clearCurseSixLockState(ServerPlayer player, CompoundTag data) {
        boolean hasMainLock = data.getLong(MAIN_LOCK_UNTIL) > 0L;
        boolean hasRandomLock = data.getLong(LOCKED_SLOT_UNTIL) > 0L
                || data.contains(LOCKED_SLOT_STACK, Tag.TAG_COMPOUND);
        boolean hasSchedule = data.getLong(NEXT_RANDOM_LOCK) > 0L;
        if (!hasMainLock && !hasRandomLock && !hasSchedule) {
            return;
        }

        if (hasMainLock && !player.getMainHandItem().isEmpty()) {
            player.getCooldowns().removeCooldown(player.getMainHandItem().getItem());
        }

        int slot = data.getInt(LOCKED_SLOT);
        if (slot >= 0 && slot < player.getInventory().items.size()) {
            ItemStack current = player.getInventory().items.get(slot);
            if (!current.isEmpty()) {
                player.getCooldowns().removeCooldown(current.getItem());
            }
        }
        if (data.contains(LOCKED_SLOT_STACK, Tag.TAG_COMPOUND)) {
            ItemStack snapshot = ItemStack.of(data.getCompound(LOCKED_SLOT_STACK));
            if (!snapshot.isEmpty()) {
                player.getCooldowns().removeCooldown(snapshot.getItem());
            }
        }

        data.putLong(MAIN_LOCK_UNTIL, 0L);
        data.putLong(NEXT_RANDOM_LOCK, 0L);
        clearLockedSlotState(data);
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
    @OnlyIn(Dist.CLIENT)
    public static class ClientOverlayEvents {
        private enum EdictChargeMode {
            NONE,
            X_CAST,
            Y_DOMAIN
        }

        private static int clientGuilt;
        private static int clientMercy;
        private static boolean clientMercyChargeReady;
        private static boolean clientSealEquipped;
        private static int clientSealEquipSeconds;
        private static boolean clientNetherAwakened;
        private static boolean clientEndAwakened;
        private static boolean clientCurseOneReversed;
        private static boolean clientCurseTwoReversed;
        private static boolean clientCurseFiveReversed;
        private static boolean clientCurseFiveDeclined;
        private static boolean clientCurseSevenKeywordUnlocked;
        private static boolean clientCurseSevenTerminalSolved;
        private static boolean clientCurseSixReversed;
        private static boolean clientCurseSevenReversed;
        private static boolean clientCurseSevenSentenceUnlocked;
        private static boolean clientCurseEightReversed;
        private static boolean clientCurseNineReversed;
        private static int clientLockTicks;
        private static int clientSatietyCrystalCount;
        private static int clientCurseNineDebtTicks = 40;
        private static int clientDynastyThreat;
        private static int clientHeartBattleStacks;
        private static int clientHeartCooldownTicks;
        private static boolean clientHeartWardReady;
        private static int clientLockedSlot = -1;
        private static int clientLockedSlotTicks;
        private static int clientDynastySilenceMainHandTicks;
        private static int clientDynastySilenceOffHandTicks;
        private static int clientEdictChargeTicks;
        private static boolean clientEdictTriggeredThisHold;
        private static EdictChargeMode clientEdictChargeMode = EdictChargeMode.NONE;
        private static int clientEdictCooldownTicks;
        private static int clientUraeusStormCooldownTicks;
        private static int clientEdictShakeTicks;
        private static int clientEdictShakeTotalTicks;
        private static float clientEdictShakeStrength;
        private static boolean clientEdictCooldownPromptedThisHold;
        private static final List<EdictWorldEffect> clientEdictWorldEffects = new ArrayList<>();
        private static String clientCurseSevenHintText = "";
        private static String clientCurseSevenHintTranslatedText = "";
        private static String clientCurseSevenHintStyle = "";
        private static int clientCurseSevenHintTicks;
        private static int clientCurseSevenHintTotalTicks;
        private static int clientCurseSevenLastVisibleChars;
        private static int clientChanceDieTicks;
        private static int clientChanceDieTotalTicks;
        private static int clientChanceDieOutcome;
        private static int clientChanceDieDisplayedSide;
        private static int clientChanceDieFrame;
        private static boolean clientChanceDieAttackSpeed;
        private static boolean clientChanceDieFallImmunity;
        private static boolean clientChanceDieDodge;
        private static int clientChanceDieUndyingCharges;
        private static int clientChanceDieHealthDamageSeconds;
        private static int clientChanceDieTrueDamageSeconds;
        private static int[] clientChanceDieSequence = new int[45];
        private static float clientChanceDieOffset;
        private static int clientChanceDieLastSoundIndex = -1;
        private static boolean clientChanceDieStopSoundPlayed;
        private static final ResourceLocation[] CHANCE_DIE_OUTCOME_TEXTURES = {
                new ResourceLocation(RamessesCurseMod.MODID, "textures/gui/chance_die/chance_die_outcome_1.png"),
                new ResourceLocation(RamessesCurseMod.MODID, "textures/gui/chance_die/chance_die_outcome_2.png"),
                new ResourceLocation(RamessesCurseMod.MODID, "textures/gui/chance_die/chance_die_outcome_3.png"),
                new ResourceLocation(RamessesCurseMod.MODID, "textures/gui/chance_die/chance_die_outcome_4.png"),
                new ResourceLocation(RamessesCurseMod.MODID, "textures/gui/chance_die/chance_die_outcome_5.png"),
                new ResourceLocation(RamessesCurseMod.MODID, "textures/gui/chance_die/chance_die_outcome_6.png")
        };
        private static final String[] ETERNAL_TOTEM_TYPEWRITER_KEYS = {
                "item.ramesses_curse.eternal_silence_totem.double_strike",
                "item.ramesses_curse.eternal_silence_totem.revive",
                "item.ramesses_curse.eternal_silence_totem.surge"
        };
        private static final String[] ETERNAL_REBIRTH_TOTEM_TYPEWRITER_KEYS = {
                "item.ramesses_curse.eternal_silence_rebirth_totem.double_strike",
                "item.ramesses_curse.eternal_silence_rebirth_totem.revive",
                "item.ramesses_curse.eternal_silence_rebirth_totem.surge"
        };
        private static final long ETERNAL_TOTEM_TYPEWRITER_CHAR_MILLIS = 30L;
        private static final long ETERNAL_TOTEM_HOVER_RESET_MILLIS = 250L;
        private static long clientEternalTotemTooltipStartedAt;
        private static long clientEternalTotemTooltipLastSeenAt;
        private static Screen clientEternalTotemTooltipScreen;
        private static Slot clientEternalTotemTooltipSlot;
        private static final List<String> clientEmberRiddleRound = new ArrayList<>();
        private static final List<Boolean> clientEmberRiddleSolved = new ArrayList<>();
        private static int clientEmberRiddleIndex;
        private static boolean clientEmberRiddleUnlocked;
        private static final List<String> clientSatietyRiddleRound = new ArrayList<>();
        private static final List<Boolean> clientSatietyRiddleSolved = new ArrayList<>();
        private static int clientSatietyRiddleIndex;

        public static void updateClientGuilt(int guilt, int mercy, boolean mercyChargeReady, boolean sealEquipped, int sealEquipSeconds, boolean netherAwakened, boolean endAwakened, boolean curseOneReversed, boolean curseTwoReversed, boolean curseFiveReversed, boolean curseFiveDeclined, boolean curseSevenKeywordUnlocked, boolean curseSevenTerminalSolved, boolean curseSixReversed, boolean curseSevenReversed, boolean curseSevenSentenceUnlocked, boolean curseEightReversed, boolean curseNineReversed, int lockTicks, int satietyCrystalCount, int curseNineDebtTicks, int lockedSlot, int lockedSlotTicks, int dynastyThreat, int dynastySilenceMainHandTicks, int dynastySilenceOffHandTicks) {
            clientGuilt = guilt;
            clientMercy = mercy;
            clientMercyChargeReady = mercyChargeReady;
            clientSealEquipped = sealEquipped;
            clientSealEquipSeconds = sealEquipSeconds;
            clientNetherAwakened = netherAwakened;
            clientEndAwakened = endAwakened;
            clientCurseOneReversed = curseOneReversed;
            clientCurseTwoReversed = curseTwoReversed;
            clientCurseFiveReversed = curseFiveReversed;
            clientCurseFiveDeclined = curseFiveDeclined;
            clientCurseSevenKeywordUnlocked = curseSevenKeywordUnlocked;
            clientCurseSevenTerminalSolved = curseSevenTerminalSolved;
            clientCurseSixReversed = curseSixReversed;
            clientCurseSevenReversed = curseSevenReversed;
            clientCurseSevenSentenceUnlocked = curseSevenSentenceUnlocked;
            clientCurseEightReversed = curseEightReversed;
            clientCurseNineReversed = curseNineReversed;
            clientLockTicks = lockTicks;
            clientSatietyCrystalCount = satietyCrystalCount;
            clientCurseNineDebtTicks = curseNineDebtTicks;
            clientLockedSlot = lockedSlot;
            clientLockedSlotTicks = lockedSlotTicks;
            clientDynastyThreat = dynastyThreat;
            clientDynastySilenceMainHandTicks = dynastySilenceMainHandTicks;
            clientDynastySilenceOffHandTicks = dynastySilenceOffHandTicks;
        }

        public static int getClientDynastyThreat() {
            return clientDynastyThreat;
        }

        public static boolean isClientSealEquipped() {
            return clientSealEquipped;
        }

        public static int getClientSealEquipSeconds() {
            return clientSealEquipSeconds;
        }

        public static void openCurseFiveChoiceScreen() {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft == null) {
                return;
            }
            minecraft.setScreen(new CurseFiveChoiceScreen());
        }

        public static void showCurseSevenHint(String text, String translatedText, String style, int durationTicks) {
            clientCurseSevenHintText = text == null ? "" : text;
            clientCurseSevenHintTranslatedText = translatedText == null ? "" : translatedText;
            clientCurseSevenHintStyle = style == null ? "" : style;
            int minimumTicks = Math.max(40, clientCurseSevenHintText.length() * 3 + 24);
            int totalTicks = Math.max(durationTicks, minimumTicks);
            clientCurseSevenHintTicks = Math.max(0, totalTicks);
            clientCurseSevenHintTotalTicks = Math.max(1, totalTicks);
            clientCurseSevenLastVisibleChars = 0;
        }

        public static void showChanceDieRoll(int outcome) {
            clientChanceDieOutcome = Mth.clamp(outcome, 1, 6);
            clientChanceDieTotalTicks = 100;
            clientChanceDieTicks = clientChanceDieTotalTicks;
            clientChanceDieDisplayedSide = 1;
            clientChanceDieFrame = 0;
            clientChanceDieOffset = 0.0F;
            clientChanceDieLastSoundIndex = -1;
            clientChanceDieStopSoundPlayed = false;
            for (int index = 0; index < clientChanceDieSequence.length; index++) {
                clientChanceDieSequence[index] = 1 + RandomSource.create().nextInt(6);
            }
            clientChanceDieSequence[32] = clientChanceDieOutcome;
        }

        public static void updateClientChanceDieState(boolean attackSpeed, boolean fallImmunity, boolean dodge,
                int undyingCharges, int healthDamageSeconds, int trueDamageSeconds) {
            clientChanceDieAttackSpeed = attackSpeed;
            clientChanceDieFallImmunity = fallImmunity;
            clientChanceDieDodge = dodge;
            clientChanceDieUndyingCharges = Math.max(0, undyingCharges);
            clientChanceDieHealthDamageSeconds = Math.max(0, healthDamageSeconds);
            clientChanceDieTrueDamageSeconds = Math.max(0, trueDamageSeconds);
        }

        public static boolean hasClientChanceDieAttackSpeed() {
            return clientChanceDieAttackSpeed;
        }

        public static boolean hasClientChanceDieFallImmunity() {
            return clientChanceDieFallImmunity;
        }

        public static boolean hasClientChanceDieDodge() {
            return clientChanceDieDodge;
        }

        public static int getClientChanceDieUndyingCharges() {
            return clientChanceDieUndyingCharges;
        }

        public static int getClientChanceDieHealthDamageSeconds() {
            return clientChanceDieHealthDamageSeconds;
        }

        public static int getClientChanceDieTrueDamageSeconds() {
            return clientChanceDieTrueDamageSeconds;
        }

        public static boolean isClientCurseOneReversed() {
            return clientCurseOneReversed;
        }

        public static boolean isClientCurseTwoReversed() {
            return clientCurseTwoReversed;
        }

        public static boolean isClientCurseFiveReversed() {
            return clientCurseFiveReversed;
        }

        public static boolean isClientCurseFiveDeclined() {
            return clientCurseFiveDeclined;
        }

        public static boolean isClientCurseSevenKeywordUnlocked() {
            return clientCurseSevenKeywordUnlocked;
        }

        public static boolean isClientCurseSevenTerminalSolved() {
            return clientCurseSevenTerminalSolved;
        }

        public static boolean isClientCurseSevenReversed() {
            return clientCurseSevenReversed;
        }

        public static boolean isClientCurseSixReversed() {
            return clientCurseSixReversed;
        }

        public static boolean isClientCurseSevenSentenceUnlocked() {
            return clientCurseSevenSentenceUnlocked;
        }

        public static boolean isClientNetherAwakened() {
            return clientNetherAwakened;
        }

        public static boolean isClientEndAwakened() {
            return clientEndAwakened;
        }

        public static boolean isClientCurseEightReversed() {
            return clientCurseEightReversed;
        }

        public static boolean isClientCurseNineReversed() {
            return clientCurseNineReversed;
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

        public static void addEdictWorldEffect(double x, double y, double z, float radius, int durationTicks, boolean domain) {
            clientEdictWorldEffects.add(new EdictWorldEffect(x, y, z, Math.max(1.0F, radius), Math.max(1, durationTicks), domain));
            if (clientEdictWorldEffects.size() > 12) {
                clientEdictWorldEffects.remove(0);
            }
        }

        public static int getClientEdictCooldownTicks() {
            return clientEdictCooldownTicks;
        }

        public static void updateClientUraeusStormCooldown(int cooldownTicks) {
            clientUraeusStormCooldownTicks = Math.max(0, cooldownTicks);
        }

        public static int getClientUraeusStormCooldownTicks() {
            return clientUraeusStormCooldownTicks;
        }

        public static void updateEmberRiddleRound(List<String> round, int index, boolean unlocked, List<Boolean> solved) {
            clientEmberRiddleRound.clear();
            clientEmberRiddleRound.addAll(round);
            clientEmberRiddleSolved.clear();
            clientEmberRiddleSolved.addAll(solved);
            clientEmberRiddleIndex = Math.max(0, index);
            clientEmberRiddleUnlocked = unlocked;
        }

        public static boolean isClientEmberRiddleUnlocked() {
            return clientEmberRiddleUnlocked;
        }

        public static int getClientEmberRiddleIndex() {
            return clientEmberRiddleIndex;
        }

        public static int getClientEmberRiddleRoundSize() {
            return clientEmberRiddleRound.size();
        }

        public static List<Component> getClientEmberRiddleTitles() {
            List<Component> titles = new ArrayList<>();
            for (String id : clientEmberRiddleRound) {
                EmberRiddle riddle = getEmberRiddleById(id);
                if (riddle != null) {
                    titles.add(Component.literal(riddle.title()));
                }
            }
            return titles;
        }

        public static boolean isClientEmberRiddleSolved(int index) {
            return index >= 0 && index < clientEmberRiddleSolved.size() && Boolean.TRUE.equals(clientEmberRiddleSolved.get(index));
        }

        @Nullable
        public static EmberRiddle getClientCurrentRiddle() {
            if (clientEmberRiddleRound.isEmpty()) {
                return null;
            }
            int index = Mth.clamp(clientEmberRiddleIndex, 0, clientEmberRiddleRound.size() - 1);
            return getEmberRiddleById(clientEmberRiddleRound.get(index));
        }

        public static void updateSatietyRiddleRound(List<String> round, int index, List<Boolean> solved, boolean openScreen) {
            clientSatietyRiddleRound.clear();
            clientSatietyRiddleRound.addAll(round);
            clientSatietyRiddleSolved.clear();
            clientSatietyRiddleSolved.addAll(solved);
            clientSatietyRiddleIndex = Math.max(0, index);
            Minecraft minecraft = Minecraft.getInstance();
            if (round.isEmpty()
                    && minecraft.screen instanceof EmberRiddleInputScreen screen
                    && screen.isSatietyMode()) {
                minecraft.setScreen(null);
                return;
            }
            if (openScreen) {
                minecraft.setScreen(new EmberRiddleInputScreen(true));
            }
        }

        public static int getClientSatietyRiddleIndex() {
            return clientSatietyRiddleIndex;
        }

        public static int getClientSatietyRiddleRoundSize() {
            return clientSatietyRiddleRound.size();
        }

        public static List<Component> getClientSatietyRiddleTitles() {
            List<Component> titles = new ArrayList<>();
            for (String id : clientSatietyRiddleRound) {
                EmberRiddle riddle = getEmberRiddleById(id);
                if (riddle != null) {
                    titles.add(Component.literal(riddle.title()));
                }
            }
            return titles;
        }

        public static boolean isClientSatietyRiddleSolved(int index) {
            return index >= 0 && index < clientSatietyRiddleSolved.size()
                    && Boolean.TRUE.equals(clientSatietyRiddleSolved.get(index));
        }

        @Nullable
        public static EmberRiddle getClientCurrentSatietyRiddle() {
            if (clientSatietyRiddleRound.isEmpty()) {
                return null;
            }
            int index = Mth.clamp(clientSatietyRiddleIndex, 0, clientSatietyRiddleRound.size() - 1);
            return getEmberRiddleById(clientSatietyRiddleRound.get(index));
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
                if (clientCurseFiveReversed) {
                    renderMercyBar(event.getGuiGraphics(), minecraft, clientMercy, Config.CURSE_5_MERCY_MAX.get(), clientMercyChargeReady);
                }
                if (!clientCurseNineReversed) {
                    renderCurseNineDebtBar(event.getGuiGraphics(), minecraft, clientCurseNineDebtTicks, getCurseNineMaxSatiety());
                }
            }

            renderEdictChargeBar(event.getGuiGraphics(), minecraft);
            renderCurseSevenHint(event.getGuiGraphics(), minecraft);
            renderChanceDieRoll(event.getGuiGraphics(), minecraft);
        }

        private static void renderChanceDieRoll(net.minecraft.client.gui.GuiGraphics guiGraphics, Minecraft minecraft) {
            if (clientChanceDieTicks <= 0 || minecraft.player == null) {
                return;
            }
            int centerX = minecraft.getWindow().getGuiScaledWidth() / 2;
            int centerY = minecraft.getWindow().getGuiScaledHeight() / 2 - 18;
            int alpha = clientChanceDieTicks < 8 ? clientChanceDieTicks * 31 : 255;
            boolean revealed = clientChanceDieTicks <= 30;
            int panelLeft = centerX - 250;
            int panelTop = centerY - 47;
            int panelBottom = centerY + 42;
            int backgroundAlpha = Math.min(112, alpha * 112 / 255);
            int innerAlpha = Math.min(42, alpha * 42 / 255);
            int edgeAlpha = Math.min(105, alpha * 105 / 255);
            // Layered translucent fills give the panel a soft, frosted look without a purple solid block.
            guiGraphics.fill(panelLeft, panelTop, centerX + 250, panelBottom,
                    (backgroundAlpha << 24) | 0x12171B);
            guiGraphics.fill(panelLeft + 5, panelTop + 5, centerX + 245, panelBottom - 5,
                    (innerAlpha << 24) | 0xD8DDE0);
            guiGraphics.fill(panelLeft + 2, panelTop + 2, centerX + 248, panelTop + 3,
                    (edgeAlpha << 24) | 0xD5D9DA);
            guiGraphics.fill(panelLeft + 2, panelBottom - 3, centerX + 248, panelBottom - 2,
                    (edgeAlpha << 24) | 0xD5D9DA);
            Component title = Component.translatable(revealed
                            ? "hud.ramesses_curse.chance_die.result"
                            : "hud.ramesses_curse.chance_die.rolling")
                    .withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD);
            guiGraphics.drawCenteredString(minecraft.font, title, centerX, panelTop + 7, (alpha << 24) | 0xF1E7C6);

            int cardSize = 40;
            int spacing = 54;
            int cardTop = centerY - 23;
            for (int index = 0; index < clientChanceDieSequence.length; index++) {
                int side = clientChanceDieSequence[index];
                int cardX = Math.round(centerX + index * spacing - clientChanceDieOffset - cardSize / 2.0F);
                if (cardX < panelLeft - cardSize || cardX > centerX + 250) {
                    continue;
                }
                boolean selectedCard = revealed && index == 32;
                int cardAlpha = selectedCard ? alpha : Math.min(190, alpha);
                int cardColor = selectedCard ? 0xFF9A7329 : 0xFF30363A;
                guiGraphics.fill(cardX - 2, cardTop - 2, cardX + cardSize + 2, cardTop + cardSize + 2,
                        (cardAlpha << 24) | (cardColor & 0xFFFFFF));
                guiGraphics.blit(CHANCE_DIE_OUTCOME_TEXTURES[side - 1], cardX, cardTop,
                        cardSize, cardSize, 0.0F, 0.0F, 16, 16, 16, 16);
            }

            guiGraphics.fill(centerX - 2, cardTop - 8, centerX + 2, cardTop + cardSize + 8,
                    (alpha << 24) | 0xFFD35A);
            guiGraphics.fill(centerX - 7, cardTop - 8, centerX + 7, cardTop - 5,
                    (alpha << 24) | 0xFFD35A);
        }

        @SubscribeEvent
        public static void onClientChat(ClientChatEvent event) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null) {
                return;
            }

            String message = event.getMessage() == null ? "" : event.getMessage().trim().toLowerCase(Locale.ROOT);
            if (!CURSE_7_CHAT_KEYWORD.equals(message)) {
                return;
            }

            if (clientCurseSevenReversed) {
                return;
            }

            ModNetwork.CHANNEL.sendToServer(new CurseSevenKeywordPacket());
        }

        @SubscribeEvent
        public static void onBookDraw(BookDrawScreenEvent event) {
        }

        private static void renderCurseSevenBookFallback(ScreenEvent.Render.Post event) {
        }

        @SubscribeEvent
        public static void onItemTooltip(ItemTooltipEvent event) {
            Minecraft minecraft = Minecraft.getInstance();
            applyEternalTotemTypewriter(event, minecraft);

            if (!(minecraft.screen instanceof AbstractContainerScreen<?> screen)) {
                return;
            }

            Slot hoveredSlot = screen.getSlotUnderMouse();
            boolean hoveringInventoryStack = hoveredSlot != null
                    && hoveredSlot.container instanceof Inventory
                    && ItemStack.isSameItemSameTags(hoveredSlot.getItem(), event.getItemStack());
            ItemStack carriedStack = screen.getMenu().getCarried();
            boolean hoveringCarriedStack = !carriedStack.isEmpty()
                    && ItemStack.isSameItemSameTags(carriedStack, event.getItemStack());

            if (!hoveringInventoryStack && !hoveringCarriedStack) {
                return;
            }

            if (clientLockTicks > 0) {
                float seconds = clientLockTicks / 20.0F;
                event.getToolTip().add(Component.translatable("hud.ramesses_curse.guilt_lock", String.format("%.1f", seconds))
                        .withStyle(net.minecraft.ChatFormatting.GOLD));
                event.getToolTip().add(Component.translatable("hud.ramesses_curse.guilt_lock_no_damage")
                        .withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
            }

            boolean hoveringLockedSlot = hoveringInventoryStack && hoveredSlot.getContainerSlot() == clientLockedSlot;
            if (clientLockedSlotTicks > 0 && (hoveringLockedSlot || hoveringCarriedStack)) {
                float lockedSeconds = clientLockedSlotTicks / 20.0F;
                event.getToolTip().add(Component.translatable("hud.ramesses_curse.locked_slot", String.format("%.1f", lockedSeconds))
                        .withStyle(net.minecraft.ChatFormatting.RED));
            }

            Player player = minecraft.player;
            if (player != null) {
                boolean hoveringMainHand = clientDynastySilenceMainHandTicks > 0
                        && ItemStack.isSameItemSameTags(player.getMainHandItem(), event.getItemStack());
                boolean hoveringOffHand = clientDynastySilenceOffHandTicks > 0
                        && ItemStack.isSameItemSameTags(player.getOffhandItem(), event.getItemStack());
                int silenceTicks = Math.max(hoveringMainHand ? clientDynastySilenceMainHandTicks : 0, hoveringOffHand ? clientDynastySilenceOffHandTicks : 0);
                if (silenceTicks > 0) {
                    float seconds = silenceTicks / 20.0F;
                    event.getToolTip().add(Component.translatable("hud.ramesses_curse.dynasty_silence", String.format("%.1f", seconds))
                            .withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));
                }
            }
        }

        private static void applyEternalTotemTypewriter(ItemTooltipEvent event, Minecraft minecraft) {
            if (!event.getItemStack().is(ModItems.ETERNAL_SILENCE_TOTEM.get())
                    && !event.getItemStack().is(ModItems.ETERNAL_SILENCE_REBIRTH_TOTEM.get())) {
                return;
            }

            String[] typewriterKeys = event.getItemStack().is(ModItems.ETERNAL_SILENCE_REBIRTH_TOTEM.get())
                    ? ETERNAL_REBIRTH_TOTEM_TYPEWRITER_KEYS
                    : ETERNAL_TOTEM_TYPEWRITER_KEYS;

            Screen currentScreen = minecraft.screen;
            Slot currentSlot = currentScreen instanceof AbstractContainerScreen<?> containerScreen
                    ? containerScreen.getSlotUnderMouse()
                    : null;
            long now = System.currentTimeMillis();
            if (currentScreen != clientEternalTotemTooltipScreen
                    || currentSlot != clientEternalTotemTooltipSlot
                    || now - clientEternalTotemTooltipLastSeenAt > ETERNAL_TOTEM_HOVER_RESET_MILLIS) {
                clientEternalTotemTooltipStartedAt = now;
                clientEternalTotemTooltipScreen = currentScreen;
                clientEternalTotemTooltipSlot = currentSlot;
            }
            clientEternalTotemTooltipLastSeenAt = now;

            int visibleCharacters = (int) Math.min(
                    Integer.MAX_VALUE,
                    (now - clientEternalTotemTooltipStartedAt) / ETERNAL_TOTEM_TYPEWRITER_CHAR_MILLIS + 1L);
            int consumedCharacters = 0;
            List<Component> tooltip = event.getToolTip();
            for (String translationKey : typewriterKeys) {
                String expectedText = Component.translatable(translationKey).getString();
                int lineIndex = findTooltipLine(tooltip, expectedText);
                if (lineIndex < 0) {
                    consumedCharacters += expectedText.codePointCount(0, expectedText.length());
                    continue;
                }

                Component original = tooltip.get(lineIndex);
                int lineLength = expectedText.codePointCount(0, expectedText.length());
                int lineVisible = Mth.clamp(visibleCharacters - consumedCharacters, 0, lineLength);
                consumedCharacters += lineLength;
                if (lineVisible >= lineLength) {
                    continue;
                }

                int splitIndex = expectedText.offsetByCodePoints(0, lineVisible);
                MutableComponent animatedLine = Component.literal(expectedText.substring(0, splitIndex))
                        .setStyle(original.getStyle());
                animatedLine.append(Component.literal(expectedText.substring(splitIndex))
                        .setStyle(original.getStyle().withColor(0x050308)));
                tooltip.set(lineIndex, animatedLine);
            }
        }

        private static int findTooltipLine(List<Component> tooltip, String expectedText) {
            for (int index = 0; index < tooltip.size(); index++) {
                if (tooltip.get(index).getString().equals(expectedText)) {
                    return index;
                }
            }
            return -1;
        }

        @SubscribeEvent
        public static void onScreenRender(ScreenEvent.Render.Post event) {
            if (event.getScreen() instanceof AbstractContainerScreen<?> screen && clientLockedSlotTicks > 0) {
                screen.getMenu().slots.stream()
                        .filter(slot -> slot.container instanceof Inventory && slot.getContainerSlot() == clientLockedSlot)
                        .findFirst()
                        .ifPresent(slot -> renderLockedSlot(event, screen, slot.x, slot.y));
            }

            renderCurseSevenBookFallback(event);
            renderCurseThreeCompatSuffix(event);
            renderCurseThreeGenericMarker(event);
            renderCurseThreeHoverTooltip(event);
        }

        @SubscribeEvent
        public static void onScreenInit(ScreenEvent.Init.Post event) {
            if (!(event.getScreen() instanceof DeathScreen screen)) {
                return;
            }

            if (!clientSealEquipped || !clientCurseFiveReversed || !clientMercyChargeReady) {
                return;
            }

            int buttonWidth = 200;
            int buttonHeight = 20;
            int x = screen.width / 2 - buttonWidth / 2;
            int y = screen.height / 4 + 120;
            event.addListener(Button.builder(
                    Component.translatable("screen.ramesses_curse.mercy_revive"),
                    button -> ModNetwork.CHANNEL.sendToServer(new CurseFiveMercyRevivePacket()))
                    .bounds(x, y, buttonWidth, buttonHeight)
                    .build());
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
            if (clientUraeusStormCooldownTicks > 0) {
                clientUraeusStormCooldownTicks--;
            }

            clientEdictWorldEffects.removeIf(EdictWorldEffect::tick);

            if (clientCurseSevenHintTicks > 0) {
                clientCurseSevenHintTicks--;
                if (clientCurseSevenHintTicks == 0) {
                    clientCurseSevenHintText = "";
                    clientCurseSevenHintStyle = "";
                    clientCurseSevenHintTotalTicks = 0;
                    clientCurseSevenLastVisibleChars = 0;
                }
            }
            if (clientChanceDieTicks > 0 && !Minecraft.getInstance().isPaused()) {
                clientChanceDieTicks--;
                clientChanceDieFrame++;
                if (clientChanceDieTicks > 30) {
                    float progress = Mth.clamp((clientChanceDieTotalTicks - clientChanceDieTicks) / 70.0F, 0.0F, 1.0F);
                    float eased = 1.0F - (float) Math.pow(1.0F - progress, 3.0D);
                    clientChanceDieOffset = eased * 32 * 54;
                    int centerIndex = Mth.clamp(Math.round(clientChanceDieOffset / 54.0F),
                            0, clientChanceDieSequence.length - 1);
                    clientChanceDieDisplayedSide = clientChanceDieSequence[centerIndex];
                    if (centerIndex != clientChanceDieLastSoundIndex && Minecraft.getInstance().player != null) {
                        clientChanceDieLastSoundIndex = centerIndex;
                        Minecraft.getInstance().player.playSound(
                                SoundEvents.NOTE_BLOCK_HAT.value(), 0.24F,
                                0.72F + centerIndex * 0.012F);
                    }
                } else if (clientChanceDieTicks <= 30) {
                    clientChanceDieDisplayedSide = clientChanceDieOutcome;
                    clientChanceDieOffset = 32 * 54;
                    if (!clientChanceDieStopSoundPlayed && Minecraft.getInstance().player != null) {
                        clientChanceDieStopSoundPlayed = true;
                        Minecraft.getInstance().player.playSound(
                                SoundEvents.EXPERIENCE_ORB_PICKUP, 0.42F, 1.05F);
                    }
                }
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

            boolean xDown = RamessesCurseMod.ClientModEvents.EDICT_OF_TWO_LANDS_KEY.isDown();
            boolean yDown = RamessesCurseMod.ClientModEvents.EDICT_OF_TWO_LANDS_DOMAIN_KEY.isDown();

            if (yDown && !hasEdictDomainTargets(player)) {
                if (!clientEdictCooldownPromptedThisHold) {
                    player.displayClientMessage(
                            Component.translatable("message.ramesses_curse.edict_domain_no_targets").withStyle(net.minecraft.ChatFormatting.RED),
                            true);
                    clientEdictCooldownPromptedThisHold = true;
                }
                resetEdictCharge();
                return;
            }

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
            if (intendedCooldownTicks > 0) {
                if (!clientEdictCooldownPromptedThisHold) {
                    float cooldownSeconds = Math.max(0.1F, intendedCooldownTicks / 20.0F);
                    MutableComponent message = Component.translatable(
                            "message.ramesses_curse.edict_other_skill_cooling",
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

            int guiltCost = ModItems.getEdictOfTwoLandsGuiltCost(clientEdictChargeMode == EdictChargeMode.Y_DOMAIN);
            if (clientGuilt < guiltCost) {
                if (!clientEdictCooldownPromptedThisHold) {
                    player.displayClientMessage(
                            Component.translatable("message.ramesses_curse.edict_no_guilt").withStyle(net.minecraft.ChatFormatting.RED),
                            true);
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
        public static void onRenderLevel(RenderLevelStageEvent event) {
            if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || clientEdictWorldEffects.isEmpty()) {
                return;
            }

            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null || minecraft.player == null) {
                return;
            }

            Vec3 camera = event.getCamera().getPosition();
            PoseStack poseStack = event.getPoseStack();

            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.getBuilder();

            poseStack.pushPose();
            poseStack.translate(-camera.x, -camera.y, -camera.z);
            Matrix4f matrix = poseStack.last().pose();
            float partialTick = event.getPartialTick();

            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            Vector3f cameraLeft = event.getCamera().getLeftVector();
            Vector3f cameraUp = event.getCamera().getUpVector();
            for (EdictWorldEffect effect : clientEdictWorldEffects) {
                renderEdictWorldCustomEffect(buffer, matrix, effect, partialTick, cameraLeft, cameraUp);
            }
            tesselator.end();

            RenderSystem.lineWidth(3.5F);
            buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
            for (EdictWorldEffect effect : clientEdictWorldEffects) {
                renderEdictWorldEffect(buffer, matrix, effect, partialTick);
            }
            tesselator.end();
            poseStack.popPose();

            RenderSystem.lineWidth(1.0F);
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        }

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null) {
                return;
            }

            while (RamessesCurseMod.ClientModEvents.HEART_AWAKENING_KEY.consumeClick()) {
                ModNetwork.CHANNEL.sendToServer(new HeartAwakeningPacket());
            }

            while (RamessesCurseMod.ClientModEvents.HEART_WARD_KEY.consumeClick()) {
                ModNetwork.CHANNEL.sendToServer(new HeartWardActivatePacket());
            }

            while (RamessesCurseMod.ClientModEvents.COMPOUND_BOW_AMMO_KEY.consumeClick()) {
                ModNetwork.CHANNEL.sendToServer(new OpenCompoundBowAmmoPacket());
            }
        }

        private static void renderEdictWorldEffect(BufferBuilder buffer, Matrix4f matrix, EdictWorldEffect effect, float partialTick) {
            float age = Math.min(effect.durationTicks, effect.age + partialTick);
            float life = Mth.clamp(age / effect.durationTicks, 0.0F, 1.0F);
            float fade = 1.0F - life;
            float pulse = 0.82F + 0.18F * Mth.sin((age + effect.radius) * 0.22F);
            int alpha = Mth.clamp(Mth.floor(210.0F * fade * pulse), 28, 210);
            int brightAlpha = Mth.clamp(Mth.floor(255.0F * fade), 32, 255);
            float radius = effect.radius * (0.86F + 0.14F * Mth.sin(age * 0.09F));
            float y = (float) effect.y + 0.035F;

            drawCircle(buffer, matrix, (float) effect.x, y, (float) effect.z, radius, 96, 255, 214, 92, alpha);
            drawCircle(buffer, matrix, (float) effect.x, y + 0.025F, (float) effect.z, radius * 0.72F, 72, 118, 255, 238, Math.max(24, alpha / 2));
            drawStar(buffer, matrix, (float) effect.x, y + 0.045F, (float) effect.z, radius * 0.78F, 255, 232, 128, brightAlpha);

            if (effect.domain) {
                drawDome(buffer, matrix, (float) effect.x, y, (float) effect.z, radius, 255, 215, 108, Math.max(22, alpha / 2));
            }
        }

        private static void renderEdictWorldCustomEffect(BufferBuilder buffer, Matrix4f matrix, EdictWorldEffect effect, float partialTick, Vector3f cameraLeft, Vector3f cameraUp) {
            float age = Math.min(effect.durationTicks, effect.age + partialTick);
            float life = Mth.clamp(age / effect.durationTicks, 0.0F, 1.0F);
            float fade = 1.0F - life;
            float centerX = (float) effect.x;
            float centerY = (float) effect.y + 0.12F;
            float centerZ = (float) effect.z;
            float radius = effect.radius * (0.9F + 0.08F * Mth.sin(age * 0.08F));

            drawOrbitingStreaks(buffer, matrix, centerX, centerY, centerZ, radius, age, fade, false);
            drawOrbitingStreaks(buffer, matrix, centerX, centerY + 0.16F, centerZ, radius * 0.62F, -age * 1.18F, fade, true);
            drawRisingStreaks(buffer, matrix, centerX, centerY, centerZ, radius, age, fade, effect.domain);
            drawRuneSparks(buffer, matrix, centerX, centerY, centerZ, radius, age, fade, effect.domain, cameraLeft, cameraUp);
        }

        private static void drawOrbitingStreaks(BufferBuilder buffer, Matrix4f matrix, float centerX, float y, float centerZ, float radius, float age, float fade, boolean cyan) {
            int count = cyan ? 18 : 28;
            float width = cyan ? 0.045F : 0.055F;
            float length = cyan ? 0.72F : 1.08F;
            int red = cyan ? 85 : 255;
            int green = cyan ? 245 : 224;
            int blue = cyan ? 255 : 98;
            int alpha = Mth.clamp(Mth.floor((cyan ? 132.0F : 166.0F) * fade), 10, cyan ? 132 : 166);
            for (int i = 0; i < count; i++) {
                float randomOffset = pseudoRandom(i * 31 + (cyan ? 17 : 3)) * 0.28F;
                float angle = (float) ((Math.PI * 2.0D * i) / count + age * (cyan ? -0.035F : 0.028F) + randomOffset);
                float wave = 0.86F + 0.14F * Mth.sin(age * 0.09F + i * 1.7F);
                float r = radius * wave;
                float x = centerX + Mth.cos(angle) * r;
                float z = centerZ + Mth.sin(angle) * r;
                float tangentX = -Mth.sin(angle);
                float tangentZ = Mth.cos(angle);
                float radialX = Mth.cos(angle);
                float radialZ = Mth.sin(angle);
                addQuad(buffer, matrix,
                        x - tangentX * length - radialX * width, y, z - tangentZ * length - radialZ * width,
                        x + tangentX * length - radialX * width, y, z + tangentZ * length - radialZ * width,
                        x + tangentX * length + radialX * width, y + 0.035F, z + tangentZ * length + radialZ * width,
                        x - tangentX * length + radialX * width, y + 0.035F, z - tangentZ * length + radialZ * width,
                        red, green, blue, alpha);
            }
        }

        private static void drawRisingStreaks(BufferBuilder buffer, Matrix4f matrix, float centerX, float y, float centerZ, float radius, float age, float fade, boolean domain) {
            int count = domain ? 42 : 24;
            float maxHeight = domain ? radius * 1.05F : radius * 0.58F;
            for (int i = 0; i < count; i++) {
                float seed = pseudoRandom(i * 97 + 11);
                float angle = (float) (i * 2.399963D + age * 0.012D);
                float r = radius * (0.18F + 0.78F * seed);
                float progress = (age * (0.012F + seed * 0.018F) + seed) % 1.0F;
                float bottom = y + progress * maxHeight;
                float height = 0.48F + seed * 1.15F;
                float width = 0.035F + seed * 0.035F;
                float x = centerX + Mth.cos(angle) * r;
                float z = centerZ + Mth.sin(angle) * r;
                float sideX = -Mth.sin(angle) * width;
                float sideZ = Mth.cos(angle) * width;
                int alpha = Mth.clamp(Mth.floor(112.0F * fade * (1.0F - Math.abs(progress - 0.5F) * 0.8F)), 8, 112);
                addQuad(buffer, matrix,
                        x - sideX, bottom, z - sideZ,
                        x + sideX, bottom, z + sideZ,
                        x + sideX * 0.45F, bottom + height, z + sideZ * 0.45F,
                        x - sideX * 0.45F, bottom + height, z - sideZ * 0.45F,
                        seed > 0.62F ? 105 : 255,
                        seed > 0.62F ? 245 : 226,
                        seed > 0.62F ? 255 : 112,
                        alpha);
            }
        }

        private static void drawRuneSparks(BufferBuilder buffer, Matrix4f matrix, float centerX, float y, float centerZ, float radius, float age, float fade, boolean domain, Vector3f cameraLeft, Vector3f cameraUp) {
            int count = domain ? 36 : 20;
            for (int i = 0; i < count; i++) {
                float seed = pseudoRandom(i * 53 + 29);
                float angle = (float) (i * 2.618D + age * (0.018D + seed * 0.018D));
                float r = radius * (0.28F + seed * 0.66F);
                float bob = Mth.sin(age * 0.11F + i) * 0.18F;
                float x = centerX + Mth.cos(angle) * r;
                float z = centerZ + Mth.sin(angle) * r;
                float sparkY = y + 0.35F + bob + ((age * 0.018F + seed) % 1.0F) * (domain ? 2.6F : 1.35F);
                float size = 0.08F + seed * 0.08F;
                int alpha = Mth.clamp(Mth.floor(178.0F * fade * (0.72F + 0.28F * Mth.sin(age * 0.24F + i))), 10, 178);
                drawBillboardDiamond(buffer, matrix, x, sparkY, z, size, cameraLeft, cameraUp,
                        seed > 0.56F ? 92 : 255,
                        seed > 0.56F ? 245 : 232,
                        seed > 0.56F ? 255 : 128,
                        alpha);
            }
        }

        private static float pseudoRandom(int seed) {
            int mixed = seed * 1103515245 + 12345;
            mixed ^= mixed >>> 16;
            return (mixed & 0x7FFFFFFF) / (float) 0x7FFFFFFF;
        }

        private static void drawBillboardDiamond(BufferBuilder buffer, Matrix4f matrix, float x, float y, float z, float size, Vector3f cameraLeft, Vector3f cameraUp, int red, int green, int blue, int alpha) {
            float lx = cameraLeft.x() * size;
            float ly = cameraLeft.y() * size;
            float lz = cameraLeft.z() * size;
            float ux = cameraUp.x() * size;
            float uy = cameraUp.y() * size;
            float uz = cameraUp.z() * size;
            addQuad(buffer, matrix,
                    x, y + uy, z + uz,
                    x + lx, y + ly, z + lz,
                    x, y - uy, z - uz,
                    x - lx, y - ly, z - lz,
                    red, green, blue, alpha);
        }

        private static void drawCircle(BufferBuilder buffer, Matrix4f matrix, float centerX, float y, float centerZ, float radius, int segments, int red, int green, int blue, int alpha) {
            for (int i = 0; i < segments; i++) {
                double angleA = (Math.PI * 2.0D * i) / segments;
                double angleB = (Math.PI * 2.0D * (i + 1)) / segments;
                addLine(
                        buffer,
                        matrix,
                        centerX + (float) Math.cos(angleA) * radius,
                        y,
                        centerZ + (float) Math.sin(angleA) * radius,
                        centerX + (float) Math.cos(angleB) * radius,
                        y,
                        centerZ + (float) Math.sin(angleB) * radius,
                        red,
                        green,
                        blue,
                        alpha);
            }
        }

        private static void drawStar(BufferBuilder buffer, Matrix4f matrix, float centerX, float y, float centerZ, float radius, int red, int green, int blue, int alpha) {
            float[][] points = new float[5][2];
            for (int i = 0; i < points.length; i++) {
                double angle = -Math.PI / 2.0D + (Math.PI * 2.0D * i) / points.length;
                points[i][0] = centerX + (float) Math.cos(angle) * radius;
                points[i][1] = centerZ + (float) Math.sin(angle) * radius;
            }
            for (int i = 0; i < points.length; i++) {
                int next = (i + 2) % points.length;
                addLine(buffer, matrix, points[i][0], y, points[i][1], points[next][0], y, points[next][1], red, green, blue, alpha);
            }
            for (int i = 0; i < points.length; i++) {
                addLine(buffer, matrix, centerX, y + 0.025F, centerZ, points[i][0], y, points[i][1], 100, 255, 232, Math.max(20, alpha / 3));
            }
        }

        private static void drawDome(BufferBuilder buffer, Matrix4f matrix, float centerX, float y, float centerZ, float radius, int red, int green, int blue, int alpha) {
            int verticalSegments = 36;
            int arcSegments = 18;
            for (int meridian = 0; meridian < 8; meridian++) {
                double angle = (Math.PI * 2.0D * meridian) / 8.0D;
                float dirX = (float) Math.cos(angle);
                float dirZ = (float) Math.sin(angle);
                for (int i = 0; i < arcSegments; i++) {
                    double tA = (Math.PI * 0.5D * i) / arcSegments;
                    double tB = (Math.PI * 0.5D * (i + 1)) / arcSegments;
                    float rA = (float) Math.cos(tA) * radius;
                    float rB = (float) Math.cos(tB) * radius;
                    addLine(
                            buffer,
                            matrix,
                            centerX + dirX * rA,
                            y + (float) Math.sin(tA) * radius,
                            centerZ + dirZ * rA,
                            centerX + dirX * rB,
                            y + (float) Math.sin(tB) * radius,
                            centerZ + dirZ * rB,
                            red,
                            green,
                            blue,
                            alpha);
                }
            }

            for (int band = 1; band <= 3; band++) {
                double t = (Math.PI * 0.5D * band) / 4.0D;
                float bandRadius = (float) Math.cos(t) * radius;
                float bandY = y + (float) Math.sin(t) * radius;
                drawCircle(buffer, matrix, centerX, bandY, centerZ, bandRadius, verticalSegments, red, green, blue, Math.max(18, alpha / 2));
            }
        }

        private static void addLine(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, int red, int green, int blue, int alpha) {
            buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).endVertex();
        }

        private static void addQuad(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, int red, int green, int blue, int alpha) {
            buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrix, x3, y3, z3).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrix, x4, y4, z4).color(red, green, blue, alpha).endVertex();
        }

        private static void renderGuiltBar(net.minecraft.client.gui.GuiGraphics guiGraphics, Minecraft minecraft, int guilt, int max) {
            int barWidth = 96;
            int barHeight = 8;
            int x = minecraft.getWindow().getGuiScaledWidth() - barWidth - 16 + Config.HUD_GUILT_X_OFFSET.get();
            int y = minecraft.getWindow().getGuiScaledHeight() - 38 + Config.HUD_GUILT_Y_OFFSET.get();
            float ratio = max <= 0 ? 0.0F : Mth.clamp((float) guilt / (float) max, 0.0F, 1.0F);
            int fill = Mth.floor(ratio * barWidth);

            guiGraphics.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xCC2A110A);
            guiGraphics.fill(x, y, x + barWidth, y + barHeight, 0xFF31150E);
            guiGraphics.fill(x, y, x + fill, y + barHeight, 0xFFD6A63A);
            guiGraphics.drawString(minecraft.font, Component.translatable("hud.ramesses_curse.guilt", guilt, max), x, y - 10, 0xFFD6A63A, false);

            if (clientHeartBattleStacks > 0 || clientHeartWardReady || clientHeartCooldownTicks > 0) {
                int battleColor = clientHeartWardReady ? 0xFFFFD166 : 0xFF8BE9FD;
                int battleX = x + Config.HUD_HEART_BATTLE_X_OFFSET.get();
                int battleY = y - 22 + Config.HUD_HEART_BATTLE_Y_OFFSET.get();
                guiGraphics.drawString(
                        minecraft.font,
                        Component.translatable("message.ramesses_curse.heart_battle_stacks", clientHeartBattleStacks, Config.HEART_BATTLE_STACK_LIMIT.get()),
                        battleX,
                        battleY,
                        battleColor,
                        false);
            }
        }

        private static void renderMercyBar(net.minecraft.client.gui.GuiGraphics guiGraphics, Minecraft minecraft, int mercy, int max, boolean chargeReady) {
            int barWidth = 96;
            int barHeight = 8;
            int x = minecraft.getWindow().getGuiScaledWidth() - barWidth - 16 + Config.HUD_MERCY_X_OFFSET.get();
            int y = minecraft.getWindow().getGuiScaledHeight() - 18 + Config.HUD_MERCY_Y_OFFSET.get();
            float ratio = max <= 0 ? 0.0F : Mth.clamp((float) mercy / (float) max, 0.0F, 1.0F);
            int fill = Mth.floor(ratio * barWidth);

            guiGraphics.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xCC0C1D24);
            guiGraphics.fill(x, y, x + barWidth, y + barHeight, 0xFF102C35);
            guiGraphics.fill(x, y, x + fill, y + barHeight, chargeReady ? 0xFFFFD166 : 0xFF5ED1FF);
            guiGraphics.drawString(minecraft.font, Component.translatable("hud.ramesses_curse.mercy", mercy, max), x, y - 10, chargeReady ? 0xFFFFD166 : 0xFF7BE0FF, false);

            if (chargeReady) {
                guiGraphics.drawString(minecraft.font, Component.translatable("hud.ramesses_curse.mercy_ready"), x, y + 11, 0xFFFFE08A, false);
            }
        }

        private static void renderCurseNineDebtBar(net.minecraft.client.gui.GuiGraphics guiGraphics, Minecraft minecraft, int debtTicks, int max) {
            int screenWidth = minecraft.getWindow().getGuiScaledWidth();
            int barWidth = Mth.clamp(screenWidth / 5, 132, 212);
            int barHeight = 7;
            int x = 16 + Config.HUD_SATIETY_DEBT_X_OFFSET.get();
            int y = minecraft.getWindow().getGuiScaledHeight() - 24 + Config.HUD_SATIETY_DEBT_Y_OFFSET.get();
            float ratio = max <= 0 ? 0.0F : Mth.clamp((float) debtTicks / (float) max, 0.0F, 1.0F);
            int fill = Mth.floor(ratio * barWidth);
            int color = ratio <= 0.25F ? 0xFFC55B5B : ratio <= 0.5F ? 0xFFD79058 : 0xFFE3B65C;

            guiGraphics.fill(x - 3, y - 3, x + barWidth + 3, y + barHeight + 3, 0x88110805);
            guiGraphics.fill(x - 2, y - 2, x + barWidth + 2, y + barHeight + 2, 0xCC2A1A0D);
            guiGraphics.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xFF4B2D16);
            guiGraphics.fill(x, y, x + barWidth, y + barHeight, 0xFF241A10);
            guiGraphics.fill(x, y, x + fill, y + barHeight, color);
            for (int i = 1; i < 8; i++) {
                int markX = x + (barWidth * i / 8);
                guiGraphics.fill(markX, y + 1, markX + 1, y + barHeight - 1, 0x553A2A1C);
            }
            guiGraphics.fill(x + 2, y + 1, x + Math.max(2, fill - 1), y + 2, 0x44FFF1C2);
            guiGraphics.drawString(minecraft.font, Component.translatable("hud.ramesses_curse.curse_9_debt", debtTicks, max), x, y - 10, color, false);
            if (clientSatietyCrystalCount > 0) {
                guiGraphics.drawString(minecraft.font, Component.literal("Satiety crystal missing").withStyle(ChatFormatting.DARK_RED), x, y + 10, 0xFFB85A5A, false);
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

        private static void renderCurseSevenHint(net.minecraft.client.gui.GuiGraphics guiGraphics, Minecraft minecraft) {
            if (clientCurseSevenHintTicks <= 0 || clientCurseSevenHintText.isBlank()) {
                return;
            }
            if (clientCurseSevenHintText.indexOf('*') >= 0 || CURSE_7_CHAT_KEYWORD.equals(clientCurseSevenHintText)) {
                return;
            }

            int elapsed = Math.max(0, clientCurseSevenHintTotalTicks - clientCurseSevenHintTicks);
            int visibleChars = Math.min(clientCurseSevenHintText.length(), Math.max(1, elapsed / 2 + 1));
            playCurseSevenTypewriterSound(minecraft, visibleChars);
            String visibleText = clientCurseSevenHintText.substring(0, visibleChars);
            String visibleTranslatedText = revealTranslatedHint(visibleChars);
            int centerX = minecraft.getWindow().getGuiScaledWidth() / 2;
            int y = minecraft.getWindow().getGuiScaledHeight() - 78;
            Component text = Component.literal(visibleText);
            Component translatedText = Component.literal(visibleTranslatedText);

            int color = 0xFFB3B3B3;
            boolean shadow = true;
            if ("green_shadow".equals(clientCurseSevenHintStyle)) {
                color = 0xFF6EE783;
            } else if ("gold_omen".equals(clientCurseSevenHintStyle)) {
                color = 0xFFFFD54A;
            } else if ("red_omen".equals(clientCurseSevenHintStyle)) {
                color = 0xFFFF4D4D;
            } else if ("ritual_warn".equals(clientCurseSevenHintStyle)) {
                color = 0xFFE2D0A0;
            }

            Component styled = text.copy().withStyle(net.minecraft.ChatFormatting.BOLD, net.minecraft.ChatFormatting.ITALIC);
            Component translatedStyled = translatedText.copy().withStyle(net.minecraft.ChatFormatting.BOLD, net.minecraft.ChatFormatting.ITALIC);
            guiGraphics.drawString(minecraft.font, translatedStyled, centerX - minecraft.font.width(translatedStyled) / 2 + 1, y + 1, 0x663A3A3A, false);
            guiGraphics.drawString(minecraft.font, translatedStyled, centerX - minecraft.font.width(translatedStyled) / 2, y, color, shadow);
            guiGraphics.drawString(minecraft.font, styled, centerX - minecraft.font.width(styled) / 2 + 1, y + 12, 0x663A3A3A, false);
            guiGraphics.drawString(minecraft.font, styled, centerX - minecraft.font.width(styled) / 2, y + 11, color, shadow);
        }

        private static String revealTranslatedHint(int visibleChars) {
            if (clientCurseSevenHintTranslatedText.isBlank() || clientCurseSevenHintText.isBlank()) {
                return "";
            }
            float ratio = visibleChars / (float) Math.max(1, clientCurseSevenHintText.length());
            int translatedChars = Math.min(clientCurseSevenHintTranslatedText.length(),
                    Math.max(1, Mth.ceil(clientCurseSevenHintTranslatedText.length() * ratio)));
            return clientCurseSevenHintTranslatedText.substring(0, translatedChars);
        }

        private static void playCurseSevenTypewriterSound(Minecraft minecraft, int visibleChars) {
            if (minecraft.player == null || visibleChars <= clientCurseSevenLastVisibleChars) {
                return;
            }

            for (int i = clientCurseSevenLastVisibleChars; i < visibleChars; i++) {
                char revealed = clientCurseSevenHintText.charAt(i);
                if (Character.isWhitespace(revealed)) {
                    continue;
                }
                float pitch = 1.55F + ((i % 4) * 0.05F);
                minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.08F, pitch);
            }

            clientCurseSevenLastVisibleChars = visibleChars;
        }

        private static void resetEdictCharge() {
            clientEdictChargeTicks = 0;
            clientEdictTriggeredThisHold = false;
            clientEdictCooldownPromptedThisHold = false;
            clientEdictChargeMode = EdictChargeMode.NONE;
        }

        private static boolean hasEdictDomainTargets(Player player) {
            return !player.level().getEntitiesOfClass(
                    LivingEntity.class,
                    player.getBoundingBox().inflate(EDICT_DOMAIN_RANGE),
                    entity -> entity instanceof Enemy && entity.isAlive() && entity != player).isEmpty();
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
            if (player == null
                    || !ModEvents.canUseSealBoundItem(player)
                    || ModEvents.areCurseThreeAndFourLifted(player)) {
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
            if (player == null
                    || !ModEvents.canUseSealBoundItem(player)
                    || ModEvents.areCurseThreeAndFourLifted(player)) {
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
            if (player == null
                    || !clientSealEquipped
                    || ModEvents.areCurseThreeAndFourLifted(player)) {
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

        private static class EdictWorldEffect {
            private final double x;
            private final double y;
            private final double z;
            private final float radius;
            private final int durationTicks;
            private final boolean domain;
            private int age;

            private EdictWorldEffect(double x, double y, double z, float radius, int durationTicks, boolean domain) {
                this.x = x;
                this.y = y;
                this.z = z;
                this.radius = radius;
                this.durationTicks = durationTicks;
                this.domain = domain;
            }

            private boolean tick() {
                age++;
                return age >= durationTicks;
            }
        }
    }
}

