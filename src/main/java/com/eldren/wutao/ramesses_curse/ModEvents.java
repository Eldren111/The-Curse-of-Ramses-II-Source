package com.eldren.wutao.ramesses_curse;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraft.world.BossEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ClientChatEvent;
import vazkii.patchouli.api.BookDrawScreenEvent;
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
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import org.joml.Vector3f;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
    private static final String LOCKED_SLOT_STACK = "LockedSlotStack";
    private static final String NETHER_AWAKENED = "NetherAwakened";
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
    private static final String CURSE_6_SENTENCE_UNLOCKED = "CurseSixSentenceUnlocked";
    private static final String CURSE_6_SENTENCE_REVEAL_INDEX = "CurseSixSentenceRevealIndex";
    private static final String CURSE_6_SENTENCE_NEXT_REVEAL_AT = "CurseSixSentenceNextRevealAt";
    private static final String CURSE_7_REVERSED = "CurseSevenReversed";
    private static final String CURSE_8_REVERSED = "CurseEightReversed";
    private static final String CURSE_9_REVERSED = "CurseNineReversed";
    private static final String CURSE_9_KILL_UNLOCKED = "CurseNineKillUnlocked";
    private static final String CURSE_9_FOOD_HISTORY = "CurseNineFoodHistory";
    private static final String CURSE_9_DEBT_TICKS = "CurseNineDebtTicks";
    private static final String CURSE_9_PENALTY_UNTIL = "CurseNinePenaltyUntil";
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
    private static final String SATIETY_CRYSTAL_OWNER = "SatietyCrystalOwner";
    private static final String SATIETY_CRYSTAL_ID = "SatietyCrystalId";
    private static final String SATIETY_CRYSTAL_COUNT = "SatietyCrystalCount";
    private static final String SATIETY_CRYSTAL_DIMENSION = "SatietyCrystalDimension";
    private static final String SATIETY_CRYSTAL_X = "SatietyCrystalX";
    private static final String SATIETY_CRYSTAL_Y = "SatietyCrystalY";
    private static final String SATIETY_CRYSTAL_Z = "SatietyCrystalZ";
    private static final String SATIETY_CRYSTAL_RECORDS = "SatietyCrystalRecords";
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
    private static final String EMBER_TRIAL_MOB = "EmberTrialMob";
    private static final String EMBER_TRIAL_OWNER = "EmberTrialOwner";
    private static final String LAST_SYNC_GUILT = "LastSyncGuilt";
    private static final String LAST_SYNC_SEAL = "LastSyncSeal";
    private static final String LAST_SYNC_NETHER_AWAKENED = "LastSyncNetherAwakened";
    private static final String LAST_SYNC_CURSE_1_REVERSED = "LastSyncCurseOneReversed";
    private static final String LAST_SYNC_CURSE_2_REVERSED = "LastSyncCurseTwoReversed";
    private static final String LAST_SYNC_CURSE_5_REVERSED = "LastSyncCurseFiveReversed";
    private static final String LAST_SYNC_CURSE_5_DECLINED = "LastSyncCurseFiveDeclined";
    private static final String LAST_SYNC_CURSE_6_KEYWORD_UNLOCKED = "LastSyncCurseSixKeywordUnlocked";
    private static final String LAST_SYNC_CURSE_6_TERMINAL_SOLVED = "LastSyncCurseSixTerminalSolved";
    private static final String LAST_SYNC_CURSE_6_REVERSED = "LastSyncCurseSixReversed";
    private static final String LAST_SYNC_CURSE_6_SENTENCE_UNLOCKED = "LastSyncCurseSixSentenceUnlocked";
    private static final String LAST_SYNC_CURSE_8_REVERSED = "LastSyncCurseEightReversed";
    private static final String LAST_SYNC_CURSE_9_REVERSED = "LastSyncCurseNineReversed";
    private static final String LAST_SYNC_LOCK_SECONDS = "LastSyncLockSeconds";
    private static final String LAST_SYNC_SATIETY = "LastSyncSatiety";
    private static final String LAST_SYNC_CURSE_9_DEBT = "LastSyncCurseNineDebt";
    private static final String LAST_SYNC_LOCKED_SLOT = "LastSyncLockedSlot";
    private static final String LAST_SYNC_LOCKED_SLOT_TICKS = "LastSyncLockedSlotTicks";
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
    private static final int CURSE_5_MERCY_MAX = 1299;
    private static final int CURSE_5_MERCY_INVULNERABLE_TICKS = 200;
    private static final int CURSE_5_MERCY_SURGE_TICKS = 600;
    private static final int CURSE_5_CHOICE_HINT_TICKS = 140;
    private static final int CURSE_5_SLEEP_FREEZE_TICKS = 20;
    private static final int CURSE_3_4_LIFTED_HINT_TICKS = 140;
    private static final Map<String, UUID> CURSE_5_SURGE_MODIFIER_IDS = new HashMap<>();
    private static final int CURSE_9_HUNGER_LOSS = 2;
    private static final int CURSE_9_REVERSED_FOOD_TARGET = 16;
    private static final int CURSE_9_DEBT_DRAIN_INTERVAL = 100;
    private static final int CURSE_9_PENALTY_REFRESH_INTERVAL = 40;
    private static final int CURSE_9_PENALTY_DURATION = 20 * 60 * 3;
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
    private static final Field PLAYER_SLEEP_COUNTER_FIELD = findPlayerSleepCounterField();
    private static final Map<ResourceKey<Level>, Map<BlockPos, TrackedMusicPlayback>> ACTIVE_MUSIC_PLAYBACKS = new HashMap<>();
    private static final Map<UUID, ServerBossEvent> ACTIVE_EMBER_TRIAL_BARS = new HashMap<>();
    private static final int[] EMBER_TRIAL_WAVE_COUNTS = new int[]{5, 7, 9};
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
        CompoundTag data = getCurseData(player);
        if (player instanceof ServerPlayer serverPlayer) {
            tickCurseFiveSleepChoice(serverPlayer, data);
        }
        boolean sealEquipped = sanitizeSealCurios(player);
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

        int remainingLockedSlotTicks = getRemainingLockedSlotTicks(player);
        if (remainingLockedSlotTicks > 0 && player instanceof ServerPlayer serverPlayer) {
            applyLockedSlotCooldown(serverPlayer, remainingLockedSlotTicks);
        }

        if (player.level() instanceof ServerLevel serverLevel) {
            updateCurseNineHunger(serverLevel, player);
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

        boolean curseSevenReversed = data.getBoolean(CURSE_7_REVERSED);
        boolean curseSixReversed = data.getBoolean(CURSE_6_REVERSED);
        if (player.level().dimension() == Level.END && !curseSevenReversed) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.WEAKNESS,
                    220,
                    Config.END_WEAKNESS_AMPLIFIER.get(),
                    false,
                    false,
                    true));
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
            syncClientState(player, true);
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
                        );
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            CompoundTag original = event.getOriginal().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
            event.getEntity().getPersistentData().put(Player.PERSISTED_NBT_TAG, original.copy());
            Player originalPlayer = event.getOriginal();
            Player newPlayer = event.getEntity();
            if (isSealEquipped(originalPlayer) && !isCurseNineReversed(originalPlayer)) {
                int debt = getSatietyCrystalCount(newPlayer) * CURSE_9_HUNGER_LOSS + CURSE_9_HUNGER_LOSS;
                newPlayer.getFoodData().setFoodLevel(Math.max(0, 20 - debt));
            }
            if (event.getEntity() instanceof ServerPlayer player) {
                finalizeCurseFiveMercyRevive(player);
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
        if (isLockedInventoryStack(player, event.getItemStack())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(event.getTarget() instanceof AbstractVillager villager)) {
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

        DamageSource source = event.getSource();
        ServerPlayer player = source != null && source.getEntity() instanceof ServerPlayer serverPlayer ? serverPlayer : null;

        if (event.getEntity() instanceof Enemy
                && player != null
                && !event.getEntity().level().isClientSide
                && event.getEntity().getRandom().nextFloat() < 0.002F) {
            ItemEntity emberDrop = new ItemEntity(
                    event.getEntity().level(),
                    event.getEntity().getX(),
                    event.getEntity().getY(),
                    event.getEntity().getZ(),
                    new ItemStack(ModItems.EMBER_OF_DUAT.get()));
            emberDrop.setPickUpDelay(10);
            event.getDrops().add(emberDrop);
        }

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

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (isGuiltLocked(player) || isMainHandLocked(player) || isLockedInventoryStack(player, player.getMainHandItem())) {
                event.setCanceled(true);
                return;
            }
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
        if (event.getEntity() instanceof ServerPlayer player) {
            tryTriggerCurseOneReversed(player, event.getSource());
            if (isCurseFiveMercyInvulnerable(player)) {
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

        if (event.getEntity() instanceof ServerPlayer player && isCurseFiveMercyInvulnerable(player)) {
            event.setCanceled(true);
            event.setAmount(0.0F);
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
            player.displayClientMessage(Component.literal("饥王遗罚已反转。").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), false);
            syncClientState(player, true);
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

    @SubscribeEvent
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

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer fallenPlayer && isSealEquipped(fallenPlayer) && !isCurseNineReversed(fallenPlayer)) {
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
                && isCurseFiveReversed(player)
                && isCurseSevenReversed(player)
                && isCurseEightReversed(player)
                && isCurseNineReversed(player);
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
        if (history.size() < CURSE_9_REVERSED_FOOD_TARGET) {
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
        return Mth.clamp(getCurseData(player).getInt(MERCY_PROGRESS), 0, CURSE_5_MERCY_MAX);
    }

    public static boolean hasCurseFiveMercyCharge(Player player) {
        return getCurseData(player).getBoolean(MERCY_CHARGE_READY);
    }

    private static boolean isCurseFiveMercyInvulnerable(ServerPlayer player) {
        return getCurseData(player).getLong(MERCY_INVULNERABLE_UNTIL) > player.level().getGameTime();
    }

    private static boolean isCurseFiveMercySurgeActive(ServerPlayer player) {
        return getCurseData(player).getLong(MERCY_SURGE_UNTIL) > player.level().getGameTime();
    }

    private static void tryAdvanceCurseFiveMercy(ServerPlayer player) {
        if (!isSealEquipped(player) || !isCurseFiveReversed(player)) {
            return;
        }

        CompoundTag data = getCurseData(player);
        if (data.getBoolean(MERCY_CHARGE_READY)) {
            return;
        }

        int next = Math.min(CURSE_5_MERCY_MAX, data.getInt(MERCY_PROGRESS) + 1);
        data.putInt(MERCY_PROGRESS, next);
        player.displayClientMessage(Component.translatable("message.ramesses_curse.mercy_progress", next, CURSE_5_MERCY_MAX).withStyle(net.minecraft.ChatFormatting.AQUA), true);

        if (next >= CURSE_5_MERCY_MAX) {
            data.putInt(MERCY_PROGRESS, CURSE_5_MERCY_MAX);
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
                    "天秤将碎，慈悲会以更温柔也更残酷的方式染上世界。");
        } else {
            player.displayClientMessage(Component.translatable("message.ramesses_curse.curse_5_declined").withStyle(net.minecraft.ChatFormatting.RED, net.minecraft.ChatFormatting.BOLD), false);
            queueCurseFiveOutcomeHint(
                    player,
                    "the old verdict remains and the world will go on measuring your name in thunder",
                    "旧日判词仍在，世界会继续用雷霆衡量你的名字。");
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
            if (!isValidCurseOneRitualSite(level, ritualPos)) {
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

        ritualPos = findCurseOneRitualSite(level, player.blockPosition());
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

    private static BlockPos findCurseOneRitualSite(ServerLevel level, BlockPos center) {
        int radius = 4;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int y = -2; y <= 2; y++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    mutable.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    if (isValidCurseOneRitualSite(level, mutable)) {
                        return mutable.immutable();
                    }
                }
            }
        }
        return null;
    }

    private static boolean isValidCurseOneRitualSite(ServerLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).is(Blocks.WATER_CAULDRON)) {
            return false;
        }
        return countItemEntities(level, pos, Items.ENCHANTED_GOLDEN_APPLE) >= 1
                && countItemEntities(level, pos, Items.MILK_BUCKET) >= 2
                && countItemEntities(level, pos, ModItems.EMBER_OF_DUAT.get()) >= 1
                && countItemEntities(level, pos, Items.HONEY_BOTTLE) >= 1;
    }

    private static int countItemEntities(ServerLevel level, BlockPos pos, Item item) {
        int count = 0;
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(1.35D, 1.0D, 1.35D), e -> e.isAlive() && e.tickCount > 5)) {
            if (entity.getItem().is(item)) {
                count += entity.getItem().getCount();
            }
        }
        return count;
    }

    private static void finishCurseOneRitual(ServerPlayer player, ServerLevel level, CompoundTag data, BlockPos ritualPos) {
        consumeCurseOneRitualItems(level, ritualPos);
        level.setBlockAndUpdate(ritualPos, Blocks.CAULDRON.defaultBlockState());
        level.playSound(null, ritualPos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 0.8F);
        level.playSound(null, ritualPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.2F);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, ritualPos.getX() + 0.5D, ritualPos.getY() + 1.1D, ritualPos.getZ() + 0.5D, 120, 0.9D, 0.7D, 0.9D, 0.04D);
        level.sendParticles(ParticleTypes.WITCH, ritualPos.getX() + 0.5D, ritualPos.getY() + 1.1D, ritualPos.getZ() + 0.5D, 30, 0.45D, 0.45D, 0.45D, 0.02D);
        data.putBoolean(CURSE_1_REVERSED, true);
        clearCurseOneRitualState(data);
        player.displayClientMessage(Component.translatable("message.ramesses_curse.curse_1_reversed"), false);
        syncClientState(player, true);
    }

    private static void consumeCurseOneRitualItems(ServerLevel level, BlockPos pos) {
        consumeItemEntities(level, pos, Items.ENCHANTED_GOLDEN_APPLE, 1);
        consumeItemEntities(level, pos, Items.MILK_BUCKET, 2);
        consumeItemEntities(level, pos, ModItems.EMBER_OF_DUAT.get(), 1);
        consumeItemEntities(level, pos, Items.HONEY_BOTTLE, 1);
    }

    private static void consumeItemEntities(ServerLevel level, BlockPos pos, Item item, int required) {
        if (required <= 0) {
            return;
        }
        int remaining = required;
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(1.35D, 1.0D, 1.35D), e -> e.isAlive() && e.tickCount > 5)) {
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
                item.isAlive() && item.tickCount > 10 && isOnEndMainIsland(item.position()) && item.getY() < 48.0D);

        ItemEntity dragonEgg = null;
        ItemEntity netherStar = null;
        for (ItemEntity item : offerings) {
            if (dragonEgg == null && item.getItem().is(Items.DRAGON_EGG)) {
                dragonEgg = item;
                continue;
            }
            if (netherStar == null && item.getItem().is(Items.NETHER_STAR)) {
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
            sendCurseSevenMissingPrompt(player, "nether star");
        } else if (!data.getBoolean(CURSE_7_END_RITUAL_DRAGON_READY) && data.getBoolean(CURSE_7_END_RITUAL_STAR_READY)) {
            sendCurseSevenMissingPrompt(player, "dragon egg");
        }
    }

    private static void sendCurseSevenMissingPrompt(ServerPlayer player, String missing) {
        CompoundTag data = getCurseData(player);
        if (missing.equals(data.getString(CURSE_7_END_RITUAL_LAST_MISSING))) {
            return;
        }
        data.putString(CURSE_7_END_RITUAL_LAST_MISSING, missing);
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new CurseSevenHintPacket("missing offering: " + missing, CURSE_7_MISSING_OFFERING_PREFIX_ZH + missing, "ritual_warn", 50));
    }

    private static void returnCurseSevenOffering(ServerPlayer player, ServerLevel level, CompoundTag data, boolean dragonReady) {
        ItemStack returnedStack = new ItemStack(dragonReady ? Items.DRAGON_EGG : Items.NETHER_STAR);
        ItemEntity returned = new ItemEntity(level, player.getX(), player.getY() + 1.0D, player.getZ(), returnedStack);
        returned.setPickUpDelay(10);
        level.addFreshEntity(returned);
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new CurseSevenHintPacket("the void rejects an unfinished exchange", CURSE_7_VOID_REJECTION_ZH, "ritual_warn", 70));
        clearCurseSevenRitualState(data);
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

    public static boolean shouldShowCurseSevenSentenceInDuatUi() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null
                && isSealEquipped(minecraft.player)
                && ClientOverlayEvents.isClientCurseSevenSentenceUnlocked()
                && !ClientOverlayEvents.isClientCurseSevenKeywordUnlocked()
                && !ClientOverlayEvents.isClientCurseSevenReversed();
    }

    public static Component getClientCurseSevenSentenceUiHint() {
        return Component.literal("余烬侧壁浮现出一段被遮蔽的残句，试着将它重组为完整的英文句子。");
    }

    public static String getClientCurseSevenSentenceUiInscription() {
        return getCurseSevenBookMaskedLine() + "\n" + getCurseSevenBookCipherLine();
    }

    private static void grantCurseSevenFlight(ServerPlayer player) {
        if (!player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }
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

        if (player.containerMenu instanceof EnchantmentMenu menu
                && vanillaSpend >= 1
                && vanillaSpend <= menu.costs.length) {
            return getAdjustedEnchantLevelCost(player, menu.costs[vanillaSpend - 1]);
        }

        return getAdjustedEnchantSpend(player, vanillaSpend);
    }

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
        if (isCurseNineReversed(player)) {
            itemEntity.discard();
            return;
        }
        player.getFoodData().eat(CURSE_9_HUNGER_LOSS, 0.0F);
        player.removeEffect(MobEffects.HUNGER);
        player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8F, 1.2F);
        addSatietyCrystalCount(player, -1);
        if (player instanceof ServerPlayer serverPlayer) {
            clearCurseNinePenalty(serverPlayer);
            getCurseData(serverPlayer).remove(CURSE_9_PENALTY_UNTIL);
        }
        itemEntity.discard();
        syncGuiltState(
                player,
                getCurseData(player).getInt(GUILT_PROGRESS),
                getCurseData(player).getInt(MERCY_PROGRESS),
                getCurseData(player).getBoolean(MERCY_CHARGE_READY),
                isSealEquipped(player),
                getCurseData(player).getBoolean(NETHER_AWAKENED),
                getCurseData(player).getBoolean(CURSE_1_REVERSED),
                getCurseData(player).getBoolean(CURSE_2_REVERSED),
                getCurseData(player).getBoolean(CURSE_5_REVERSED),
                getCurseData(player).getBoolean(CURSE_5_DECLINED),
                getCurseData(player).getBoolean(CURSE_6_KEYWORD_UNLOCKED),
                getCurseData(player).getBoolean(CURSE_6_TERMINAL_SOLVED),
                getCurseData(player).getBoolean(CURSE_6_REVERSED),
                getCurseData(player).getBoolean(CURSE_6_SENTENCE_UNLOCKED),
                getCurseData(player).getBoolean(CURSE_8_REVERSED),
                getCurseData(player).getBoolean(CURSE_9_REVERSED),
                getRemainingGuiltLockTicks(player),
                getSatietyCrystalCount(player),
                getCurseNineDebtTicks(player),
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

    public static void setMercyProgress(ServerPlayer player, int mercy) {
        CompoundTag data = getCurseData(player);
        int clamped = Mth.clamp(mercy, 0, CURSE_5_MERCY_MAX);
        if (clamped >= CURSE_5_MERCY_MAX) {
            data.putInt(MERCY_PROGRESS, CURSE_5_MERCY_MAX);
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
        if (success) {
            boolean newlyReversed = !data.getBoolean(CURSE_2_REVERSED);
            data.putBoolean(CURSE_2_REVERSED, true);
            unlockCurseSevenSentence(player, data);
            if (newlyReversed) {
                player.displayClientMessage(Component.translatable("message.ramesses_curse.ember_trial_success")
                        .withStyle(net.minecraft.ChatFormatting.GOLD), false);
            }
            syncClientState(player, true);
            removeEmberTrialBossBar(player);
            return;
        }
        clearEmberTrialMobs(player.serverLevel(), player.getUUID());
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

        List<Mob> alive = getAliveEmberTrialMobs(player.serverLevel(), player.getUUID());
        int currentWave = Math.max(1, data.getInt(EMBER_TRIAL_WAVE));
        if (alive.isEmpty()) {
            if (currentWave >= EMBER_TRIAL_WAVE_COUNTS.length) {
                finishEmberTrial(player, true);
                return;
            }

            int nextWave = currentWave + 1;
            data.putInt(EMBER_TRIAL_WAVE, nextWave);
            spawnEmberTrialWave(player, nextWave);
            player.displayClientMessage(Component.translatable("message.ramesses_curse.ember_trial_wave", nextWave, EMBER_TRIAL_WAVE_COUNTS.length)
                    .withStyle(net.minecraft.ChatFormatting.GOLD), false);
            alive = getAliveEmberTrialMobs(player.serverLevel(), player.getUUID());
        }

        updateEmberTrialBossBar(player, alive.size());
    }

    private static void spawnEmberTrialWave(ServerPlayer player, int wave) {
        ServerLevel level = player.serverLevel();
        BlockPos center = player.blockPosition();
        int count = EMBER_TRIAL_WAVE_COUNTS[Math.max(0, Math.min(EMBER_TRIAL_WAVE_COUNTS.length - 1, wave - 1))];

        for (int i = 0; i < count; i++) {
            EntityType<? extends Mob> type = getEmberTrialWaveType(level, wave);
            BlockPos spawnPos = findTrialSpawnPos(level, center, player.getRandom(), 8 + (i % 3) * 2);
            Mob mob = type.create(level);
            if (mob == null) {
                continue;
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
            level.addFreshEntity(mob);
        }

        player.displayClientMessage(Component.translatable("message.ramesses_curse.ember_trial_wave", wave, EMBER_TRIAL_WAVE_COUNTS.length)
                .withStyle(net.minecraft.ChatFormatting.GOLD), false);
    }

    private static EntityType<? extends Mob> getEmberTrialWaveType(ServerLevel level, int wave) {
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

        return pool.get(level.getRandom().nextInt(pool.size()));
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

    private static List<Mob> getAliveEmberTrialMobs(ServerLevel level, UUID ownerId) {
        return level.getEntitiesOfClass(
                Mob.class,
                new AABB(-30000000, level.getMinBuildHeight(), -30000000, 30000000, level.getMaxBuildHeight(), 30000000),
                mob -> mob.isAlive() && mob.getPersistentData().getBoolean(EMBER_TRIAL_MOB)
                        && mob.getPersistentData().hasUUID(EMBER_TRIAL_OWNER)
                        && ownerId.equals(mob.getPersistentData().getUUID(EMBER_TRIAL_OWNER)));
    }

    private static void clearEmberTrialMobs(ServerLevel level, UUID ownerId) {
        for (Mob mob : getAliveEmberTrialMobs(level, ownerId)) {
            mob.discard();
        }
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
        Vec3 crystalSpawnPos = getSatietyCrystalSpawnPos(player);
        ItemEntity crystal = new ItemEntity(
                player.level(),
                crystalSpawnPos.x(),
                crystalSpawnPos.y(),
                crystalSpawnPos.z(),
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

    private static void updateCurseNineHunger(ServerLevel level, Player player) {
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
            player.removeEffect(MobEffects.HUNGER);
            return;
        }

        int maxFoodLevel = Math.max(0, 20 - (crystalCount * CURSE_9_HUNGER_LOSS));
        if (player.getFoodData().getFoodLevel() > maxFoodLevel) {
            player.getFoodData().setFoodLevel(maxFoodLevel);
        }
        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 220, 4, false, false, true));
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
                data.putLong(CURSE_9_PENALTY_UNTIL, gameTime + CURSE_9_PENALTY_DURATION);
                applyCurseNinePenalty(serverPlayer, CURSE_9_PENALTY_DURATION);
            }
        }

        long penaltyUntil = data.getLong(CURSE_9_PENALTY_UNTIL);
        if (penaltyUntil > gameTime) {
            return;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            clearCurseNinePenalty(serverPlayer);
            if (debtTicks <= 0 && crystalCount > 0 && (gameTime % CURSE_9_PENALTY_REFRESH_INTERVAL) == 0L) {
                data.putLong(CURSE_9_PENALTY_UNTIL, gameTime + CURSE_9_PENALTY_DURATION);
                applyCurseNinePenalty(serverPlayer, CURSE_9_PENALTY_DURATION);
            }
        }
    }

    private static void applyCurseNinePenalty(ServerPlayer player, long remainingTicks) {
        int duration = (int) Math.max(40L, Math.min(Integer.MAX_VALUE, remainingTicks + 40L));
        player.addEffect(nonCurableEffect(MobEffects.MOVEMENT_SLOWDOWN, duration, 4));
        player.addEffect(nonCurableEffect(MobEffects.DARKNESS, duration, 0));
        player.addEffect(nonCurableEffect(MobEffects.WITHER, duration, 0));
        player.addEffect(nonCurableEffect(MobEffects.CONFUSION, duration, 0));
        player.addEffect(nonCurableEffect(MobEffects.WEAKNESS, duration, 0));
        applyCurseNineModifier(player, Attributes.MAX_HEALTH, CURSE_9_HEALTH_MODIFIER, -8.0D, AttributeModifier.Operation.ADDITION);
        applyCurseNineModifier(player, Attributes.LUCK, CURSE_9_LUCK_MODIFIER, -4.0D, AttributeModifier.Operation.ADDITION);
        applyCurseNineModifier(player, Attributes.ARMOR, CURSE_9_ARMOR_MODIFIER, -6.0D, AttributeModifier.Operation.ADDITION);
        applyCurseNineModifier(player, ForgeMod.BLOCK_REACH.get(), CURSE_9_BLOCK_REACH_MODIFIER, -1.5D, AttributeModifier.Operation.ADDITION);
        applyCurseNineModifier(player, ForgeMod.ENTITY_REACH.get(), CURSE_9_ENTITY_REACH_MODIFIER, -1.5D, AttributeModifier.Operation.ADDITION);
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static MobEffectInstance nonCurableEffect(MobEffect effect, int duration, int amplifier) {
        MobEffectInstance instance = new MobEffectInstance(effect, duration, amplifier, false, false, true);
        instance.setCurativeItems(List.of());
        return instance;
    }

    private static void applyCurseNineModifier(ServerPlayer player, Attribute attribute, UUID id, double amount, AttributeModifier.Operation operation) {
        if (player.getAttribute(attribute) == null) {
            return;
        }
        player.getAttribute(attribute).removeModifier(id);
        player.getAttribute(attribute).addTransientModifier(new AttributeModifier(id, "ramesses_curse_curse_nine_penalty", amount, operation));
    }

    private static void clearCurseNinePenalty(ServerPlayer player) {
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        player.removeEffect(MobEffects.DARKNESS);
        player.removeEffect(MobEffects.WITHER);
        player.removeEffect(MobEffects.CONFUSION);
        player.removeEffect(MobEffects.WEAKNESS);
        removeModifier(player, Attributes.MAX_HEALTH, CURSE_9_HEALTH_MODIFIER);
        removeModifier(player, Attributes.LUCK, CURSE_9_LUCK_MODIFIER);
        removeModifier(player, Attributes.ARMOR, CURSE_9_ARMOR_MODIFIER);
        removeModifier(player, ForgeMod.BLOCK_REACH.get(), CURSE_9_BLOCK_REACH_MODIFIER);
        removeModifier(player, ForgeMod.ENTITY_REACH.get(), CURSE_9_ENTITY_REACH_MODIFIER);
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static boolean isCurseNinePenaltyActive(ServerPlayer player) {
        return getCurseData(player).getLong(CURSE_9_PENALTY_UNTIL) > player.level().getGameTime();
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
            for (ItemEntity crystal : findOwnedSatietyCrystals(server.getAllLevels(), player.getUUID())) {
                crystal.discard();
            }
        }
        clearRememberedSatietyCrystal(player);
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
        if (!data.contains(CURSE_6_REVERSED, Tag.TAG_BYTE) && data.contains(CURSE_7_REVERSED, Tag.TAG_BYTE)) {
            data.putBoolean(CURSE_6_REVERSED, data.getBoolean(CURSE_7_REVERSED));
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
                    CURSE_5_MERCY_MAX), true);
        } else {
            int affectedPlayers = affected;
            source.sendSuccess(() -> Component.translatable(
                    "command.ramesses_curse.mercy.multiple",
                    affectedPlayers,
                    CURSE_5_MERCY_MAX), true);
        }

        return affected;
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

    private static String normalizeRiddleAnswer(String input) {
        return input == null ? "" : input.toLowerCase(Locale.ROOT).replace(" ", "").replace("銆€", "");
    }

    public static Component getClientCurrentEmberRiddleHint() {
        EmberRiddle riddle = ClientOverlayEvents.getClientCurrentRiddle();
        if (riddle == null) {
            return Component.translatable("message.ramesses_curse.ember_riddle_missing");
        }
        return Component.translatable("message.ramesses_curse.ember_riddle_hint", riddle.title(), riddle.question());
    }

    public static String getClientCurrentEmberRiddleInscription() {
        EmberRiddle riddle = ClientOverlayEvents.getClientCurrentRiddle();
        if (riddle == null || riddle.answers().isEmpty()) {
            return "";
        }
        return getEmberCipherGlyphs(riddle.answers().get(0));
    }

    public static boolean shouldShowCurseSevenDuatEntry() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null
                && isSealEquipped(minecraft.player)
                && ClientOverlayEvents.isClientCurseSevenKeywordUnlocked()
                && !ClientOverlayEvents.isClientCurseSevenReversed();
    }

    public static boolean isClientCurseSevenKeywordLockedForRiddleRefresh() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null
                && isSealEquipped(minecraft.player)
                && ClientOverlayEvents.isClientCurseSevenKeywordUnlocked()
                && !ClientOverlayEvents.isClientCurseSevenReversed();
    }

    public static Component getClientCurseSevenDuatHint() {
        return Component.literal("余烬侧壁浮现出一行更古老的抄录，它不再追问答案，而是指向终末王座下的虚空。");
    }

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

    public static String getClientCurrentEmberRiddleAnswerLetters() {
        EmberRiddle riddle = ClientOverlayEvents.getClientCurrentRiddle();
        if (riddle == null || riddle.answers().isEmpty()) {
            return "";
        }
        return getEmberCipherLetters(riddle.answers().get(0));
    }

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

    private static void syncGuiltState(Player player, int guilt, int mercy, boolean mercyChargeReady, boolean sealEquipped, boolean netherAwakened, boolean curseOneReversed, boolean curseTwoReversed, boolean curseFiveReversed, boolean curseFiveDeclined, boolean curseSevenKeywordUnlocked, boolean curseSevenTerminalSolved, boolean curseSevenReversed, boolean curseSevenSentenceUnlocked, boolean curseEightReversed, boolean curseNineReversed, int lockTicks, int satietyCrystalCount, int curseNineDebtTicks, int lockedSlot, int lockedSlotTicks) {
        if (player instanceof ServerPlayer serverPlayer) {
            ModNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new GuiltSyncPacket(guilt, mercy, mercyChargeReady, sealEquipped, netherAwakened, curseOneReversed, curseTwoReversed, curseFiveReversed, curseFiveDeclined, curseSevenKeywordUnlocked, curseSevenTerminalSolved, curseSevenReversed, curseSevenSentenceUnlocked, curseEightReversed, curseNineReversed, lockTicks, satietyCrystalCount, curseNineDebtTicks, lockedSlot, lockedSlotTicks));
        }
    }

    private static void syncClientState(Player player, boolean force) {
        CompoundTag data = getCurseData(player);
        int guilt = data.getInt(GUILT_PROGRESS);
        int mercy = data.getInt(MERCY_PROGRESS);
        boolean mercyChargeReady = data.getBoolean(MERCY_CHARGE_READY);
        boolean sealEquipped = isSealEquipped(player);
        boolean netherAwakened = data.getBoolean(NETHER_AWAKENED);
        boolean curseOneReversed = data.getBoolean(CURSE_1_REVERSED);
        boolean curseTwoReversed = data.getBoolean(CURSE_2_REVERSED);
        boolean curseFiveReversed = data.getBoolean(CURSE_5_REVERSED);
        boolean curseFiveDeclined = data.getBoolean(CURSE_5_DECLINED);
        boolean curseSevenKeywordUnlocked = data.getBoolean(CURSE_6_KEYWORD_UNLOCKED);
        boolean curseSevenTerminalSolved = data.getBoolean(CURSE_6_TERMINAL_SOLVED);
        boolean curseSevenReversed = data.getBoolean(CURSE_6_REVERSED);
        boolean curseSevenSentenceUnlocked = data.getBoolean(CURSE_6_SENTENCE_UNLOCKED);
        boolean curseEightReversed = data.getBoolean(CURSE_8_REVERSED);
        boolean curseNineReversed = data.getBoolean(CURSE_9_REVERSED);
        int lockSeconds = Mth.ceil(getRemainingGuiltLockTicks(player) / 20.0F);
        int satietyCrystalCount = getSatietyCrystalCount(player);
        int curseNineDebtTicks = getCurseNineDebtTicks(player);
        int lockedSlot = data.getInt(LOCKED_SLOT);
        int lockedSlotTicks = getRemainingLockedSlotTicks(player);

        if (!force
                && data.getInt(LAST_SYNC_GUILT) == guilt
                && data.getInt(LAST_SYNC_MERCY) == mercy
                && data.getBoolean(LAST_SYNC_MERCY_CHARGE) == mercyChargeReady
                && data.getBoolean(LAST_SYNC_SEAL) == sealEquipped
                && data.getBoolean(LAST_SYNC_NETHER_AWAKENED) == netherAwakened
                && data.getBoolean(LAST_SYNC_CURSE_1_REVERSED) == curseOneReversed
                && data.getBoolean(LAST_SYNC_CURSE_2_REVERSED) == curseTwoReversed
                && data.getBoolean(LAST_SYNC_CURSE_5_REVERSED) == curseFiveReversed
                && data.getBoolean(LAST_SYNC_CURSE_5_DECLINED) == curseFiveDeclined
                && data.getBoolean(LAST_SYNC_CURSE_6_KEYWORD_UNLOCKED) == curseSevenKeywordUnlocked
                && data.getBoolean(LAST_SYNC_CURSE_6_TERMINAL_SOLVED) == curseSevenTerminalSolved
                && data.getBoolean(LAST_SYNC_CURSE_6_REVERSED) == curseSevenReversed
                && data.getBoolean(LAST_SYNC_CURSE_6_SENTENCE_UNLOCKED) == curseSevenSentenceUnlocked
                && data.getBoolean(LAST_SYNC_CURSE_8_REVERSED) == curseEightReversed
                && data.getBoolean(LAST_SYNC_CURSE_9_REVERSED) == curseNineReversed
                && data.getInt(LAST_SYNC_LOCK_SECONDS) == lockSeconds
                && data.getInt(LAST_SYNC_SATIETY) == satietyCrystalCount
                && data.getInt(LAST_SYNC_CURSE_9_DEBT) == curseNineDebtTicks
                && data.getInt(LAST_SYNC_LOCKED_SLOT) == lockedSlot
                && data.getInt(LAST_SYNC_LOCKED_SLOT_TICKS) == lockedSlotTicks) {
            return;
        }

        data.putInt(LAST_SYNC_GUILT, guilt);
        data.putInt(LAST_SYNC_MERCY, mercy);
        data.putBoolean(LAST_SYNC_MERCY_CHARGE, mercyChargeReady);
        data.putBoolean(LAST_SYNC_SEAL, sealEquipped);
        data.putBoolean(LAST_SYNC_NETHER_AWAKENED, netherAwakened);
        data.putBoolean(LAST_SYNC_CURSE_1_REVERSED, curseOneReversed);
        data.putBoolean(LAST_SYNC_CURSE_2_REVERSED, curseTwoReversed);
        data.putBoolean(LAST_SYNC_CURSE_5_REVERSED, curseFiveReversed);
        data.putBoolean(LAST_SYNC_CURSE_5_DECLINED, curseFiveDeclined);
        data.putBoolean(LAST_SYNC_CURSE_6_KEYWORD_UNLOCKED, curseSevenKeywordUnlocked);
        data.putBoolean(LAST_SYNC_CURSE_6_TERMINAL_SOLVED, curseSevenTerminalSolved);
        data.putBoolean(LAST_SYNC_CURSE_6_REVERSED, curseSevenReversed);
        data.putBoolean(LAST_SYNC_CURSE_6_SENTENCE_UNLOCKED, curseSevenSentenceUnlocked);
        data.putBoolean(LAST_SYNC_CURSE_8_REVERSED, curseEightReversed);
        data.putBoolean(LAST_SYNC_CURSE_9_REVERSED, curseNineReversed);
        data.putInt(LAST_SYNC_LOCK_SECONDS, lockSeconds);
        data.putInt(LAST_SYNC_SATIETY, satietyCrystalCount);
        data.putInt(LAST_SYNC_CURSE_9_DEBT, curseNineDebtTicks);
        data.putInt(LAST_SYNC_LOCKED_SLOT, lockedSlot);
        data.putInt(LAST_SYNC_LOCKED_SLOT_TICKS, lockedSlotTicks);
        syncGuiltState(player, guilt, mercy, mercyChargeReady, sealEquipped, netherAwakened, curseOneReversed, curseTwoReversed, curseFiveReversed, curseFiveDeclined, curseSevenKeywordUnlocked, curseSevenTerminalSolved, curseSevenReversed, curseSevenSentenceUnlocked, curseEightReversed, curseNineReversed, lockSeconds * 20, satietyCrystalCount, curseNineDebtTicks, lockedSlot, lockedSlotTicks);
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
        private static int clientMercy;
        private static boolean clientMercyChargeReady;
        private static boolean clientSealEquipped;
        private static boolean clientNetherAwakened;
        private static boolean clientCurseOneReversed;
        private static boolean clientCurseTwoReversed;
        private static boolean clientCurseFiveReversed;
        private static boolean clientCurseFiveDeclined;
        private static boolean clientCurseSevenKeywordUnlocked;
        private static boolean clientCurseSevenTerminalSolved;
        private static boolean clientCurseSevenReversed;
        private static boolean clientCurseSevenSentenceUnlocked;
        private static boolean clientCurseEightReversed;
        private static boolean clientCurseNineReversed;
        private static int clientLockTicks;
        private static int clientSatietyCrystalCount;
        private static int clientCurseNineDebtTicks = 40;
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
        private static String clientCurseSevenHintText = "";
        private static String clientCurseSevenHintTranslatedText = "";
        private static String clientCurseSevenHintStyle = "";
        private static int clientCurseSevenHintTicks;
        private static int clientCurseSevenHintTotalTicks;
        private static int clientCurseSevenLastVisibleChars;
        private static final List<String> clientEmberRiddleRound = new ArrayList<>();
        private static final List<Boolean> clientEmberRiddleSolved = new ArrayList<>();
        private static int clientEmberRiddleIndex;
        private static boolean clientEmberRiddleUnlocked;

        public static void updateClientGuilt(int guilt, int mercy, boolean mercyChargeReady, boolean sealEquipped, boolean netherAwakened, boolean curseOneReversed, boolean curseTwoReversed, boolean curseFiveReversed, boolean curseFiveDeclined, boolean curseSevenKeywordUnlocked, boolean curseSevenTerminalSolved, boolean curseSevenReversed, boolean curseSevenSentenceUnlocked, boolean curseEightReversed, boolean curseNineReversed, int lockTicks, int satietyCrystalCount, int curseNineDebtTicks, int lockedSlot, int lockedSlotTicks) {
            clientGuilt = guilt;
            clientMercy = mercy;
            clientMercyChargeReady = mercyChargeReady;
            clientSealEquipped = sealEquipped;
            clientNetherAwakened = netherAwakened;
            clientCurseOneReversed = curseOneReversed;
            clientCurseTwoReversed = curseTwoReversed;
            clientCurseFiveReversed = curseFiveReversed;
            clientCurseFiveDeclined = curseFiveDeclined;
            clientCurseSevenKeywordUnlocked = curseSevenKeywordUnlocked;
            clientCurseSevenTerminalSolved = curseSevenTerminalSolved;
            clientCurseSevenReversed = curseSevenReversed;
            clientCurseSevenSentenceUnlocked = curseSevenSentenceUnlocked;
            clientCurseEightReversed = curseEightReversed;
            clientCurseNineReversed = curseNineReversed;
            clientLockTicks = lockTicks;
            clientSatietyCrystalCount = satietyCrystalCount;
            clientCurseNineDebtTicks = curseNineDebtTicks;
            clientLockedSlot = lockedSlot;
            clientLockedSlotTicks = lockedSlotTicks;
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

        public static boolean isClientCurseSevenSentenceUnlocked() {
            return clientCurseSevenSentenceUnlocked;
        }

        public static boolean isClientNetherAwakened() {
            return clientNetherAwakened;
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

        public static int getClientEdictCooldownTicks() {
            return clientEdictCooldownTicks;
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
                    renderMercyBar(event.getGuiGraphics(), minecraft, clientMercy, CURSE_5_MERCY_MAX, clientMercyChargeReady);
                }
                if (!clientCurseNineReversed) {
                    renderCurseNineDebtBar(event.getGuiGraphics(), minecraft, clientCurseNineDebtTicks, getCurseNineMaxSatiety());
                }
            }

            renderEdictChargeBar(event.getGuiGraphics(), minecraft);
            renderCurseSevenHint(event.getGuiGraphics(), minecraft);
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

            if (!(minecraft.screen instanceof AbstractContainerScreen<?> screen)) {
                return;
            }

            if (screen.getSlotUnderMouse() == null || !(screen.getSlotUnderMouse().container instanceof Inventory)) {
                return;
            }

            if (!ItemStack.isSameItemSameTags(screen.getSlotUnderMouse().getItem(), event.getItemStack())) {
                return;
            }

            if (clientLockTicks > 0) {
                float seconds = clientLockTicks / 20.0F;
                event.getToolTip().add(Component.translatable("hud.ramesses_curse.guilt_lock", String.format("%.1f", seconds))
                        .withStyle(net.minecraft.ChatFormatting.GOLD));
                event.getToolTip().add(Component.translatable("hud.ramesses_curse.guilt_lock_no_damage")
                        .withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
            }

            if (clientLockedSlotTicks > 0 && screen.getSlotUnderMouse().getContainerSlot() == clientLockedSlot) {
                float lockedSeconds = clientLockedSlotTicks / 20.0F;
                event.getToolTip().add(Component.translatable("hud.ramesses_curse.locked_slot", String.format("%.1f", lockedSeconds))
                        .withStyle(net.minecraft.ChatFormatting.RED));
            }
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

            if (clientCurseSevenHintTicks > 0) {
                clientCurseSevenHintTicks--;
                if (clientCurseSevenHintTicks == 0) {
                    clientCurseSevenHintText = "";
                    clientCurseSevenHintStyle = "";
                    clientCurseSevenHintTotalTicks = 0;
                    clientCurseSevenLastVisibleChars = 0;
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

            boolean xDown = RamessesCurseMod.EDICT_OF_TWO_LANDS_KEY.isDown();
            boolean yDown = RamessesCurseMod.EDICT_OF_TWO_LANDS_DOMAIN_KEY.isDown();

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
                        y - 22,
                        battleColor,
                        false);
            }
        }

        private static void renderMercyBar(net.minecraft.client.gui.GuiGraphics guiGraphics, Minecraft minecraft, int mercy, int max, boolean chargeReady) {
            int barWidth = 96;
            int barHeight = 8;
            int x = minecraft.getWindow().getGuiScaledWidth() - barWidth - 16;
            int y = minecraft.getWindow().getGuiScaledHeight() - 18;
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
            int x = 16;
            int y = minecraft.getWindow().getGuiScaledHeight() - 24;
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
                guiGraphics.drawString(minecraft.font, Component.literal("饱食水晶未归还").withStyle(ChatFormatting.DARK_RED), x, y + 10, 0xFFB85A5A, false);
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
    }
}

