package com.eldren.wutao.ramesses_curse;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

@Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    static {
        BUILDER.push("卡迭石咒印");

        BUILDER.push("诅咒一");
        RANDOM_CURSE_INTERVAL = BUILDER
                .comment("[诅咒1] 随机负面效果的触发间隔，单位为 tick。6000 tick = 5 分钟。")
                .defineInRange("randomCurseInterval", 6000, 20, 72000);

        RANDOM_CURSE_DURATION = BUILDER
                .comment("[诅咒1] 每次随机负面效果的持续时间，单位为 tick。")
                .defineInRange("randomCurseDuration", 160, 20, 72000);

        RANDOM_CURSE_AMPLIFIER_MIN = BUILDER
                .comment("[诅咒1] 随机负面效果的最小等级。")
                .defineInRange("randomCurseAmplifierMin", 0, 0, 10);

        RANDOM_CURSE_AMPLIFIER_MAX = BUILDER
                .comment("[诅咒1] 随机负面效果的最大等级。")
                .defineInRange("randomCurseAmplifierMax", 1, 0, 10);

        CURSE_1_EFFECT_WHITELIST = BUILDER
                .comment("[诅咒1] 负面效果白名单。这里只会抽取你填写的效果 ID，也支持其他模组效果。")
                .defineListAllowEmpty("curse1EffectWhitelist",
                        List.of("minecraft:weakness", "minecraft:slowness", "minecraft:hunger", "minecraft:blindness", "minecraft:poison", "minecraft:wither", "minecraft:unluck", "minecraft:levitation"),
                        Config::validateEffectName);
        CURSE_1_REVERSED_TRIGGER_COOLDOWN = BUILDER
                .comment("[curse1-reversed] Internal cooldown in ticks before the reactive buff can trigger again.")
                .defineInRange("curse1ReversedTriggerCooldown", 200, 1, 72000);

        CURSE_1_REVERSED_EFFECT_DURATION = BUILDER
                .comment("[curse1-reversed] Duration in ticks for each granted beneficial effect.")
                .defineInRange("curse1ReversedEffectDuration", 100, 20, 72000);

        CURSE_1_REVERSED_EFFECT_COUNT = BUILDER
                .comment("[curse1-reversed] Number of beneficial effects granted per trigger.")
                .defineInRange("curse1ReversedEffectCount", 2, 1, 16);

        CURSE_1_REVERSED_AMPLIFIER_MIN = BUILDER
                .comment("[curse1-reversed] Minimum amplifier for the granted beneficial effects.")
                .defineInRange("curse1ReversedAmplifierMin", 0, 0, 10);

        CURSE_1_REVERSED_AMPLIFIER_MAX = BUILDER
                .comment("[curse1-reversed] Maximum amplifier for the granted beneficial effects.")
                .defineInRange("curse1ReversedAmplifierMax", 1, 0, 10);

        CURSE_1_REVERSED_EFFECT_WHITELIST = BUILDER
                .comment("[curse1-reversed] Candidate whitelist for reactive beneficial effects. Non-beneficial effects are ignored at runtime.")
                .defineListAllowEmpty("curse1ReversedEffectWhitelist",
                        List.of("minecraft:regeneration", "minecraft:resistance", "minecraft:speed", "minecraft:fire_resistance", "minecraft:damage_boost"),
                        Config::validateEffectName);
        BUILDER.pop();

        BUILDER.push("诅咒二");
        CURSE_2_SPAWN_INTERVAL = BUILDER
                .comment("[诅咒2] 敌对生物生成检测的时间间隔，单位为 tick。12000 tick = 10 分钟。")
                .defineInRange("curse2SpawnInterval", 12000, 20, 720000);

        CURSE_2_SPAWN_CHANCE = BUILDER
                .comment("[诅咒2] 每次到达触发间隔后，召唤敌对生物的概率。")
                .defineInRange("curse2SpawnChance", 0.15D, 0.0D, 1.0D);

        CURSE_2_LUCK_PENALTY = BUILDER
                .comment("[诅咒2] 佩戴卡迭石咒印时减少的幸运值。")
                .defineInRange("curse2LuckPenalty", 2.0D, 0.0D, 1024.0D);

        CURSE_2_SPAWN_RADIUS = BUILDER
                .comment("[诅咒2] 召唤敌对生物时，围绕玩家的水平生成半径。")
                .defineInRange("curse2SpawnRadius", 8, 2, 64);

        CURSE_2_ENTITY_WHITELIST = BUILDER
                .comment("[诅咒2] 敌对生物白名单。诅咒2只会生成这里填写的实体 ID，也支持其他模组生物。")
                .defineListAllowEmpty(
                        "curse2EntityWhitelist",
                        List.of("minecraft:zombie", "minecraft:skeleton", "minecraft:spider", "minecraft:creeper"),
                        Config::validateEntityTypeName);

        CURSE_2_REVERSED_LUCK_BONUS = BUILDER
                .comment("[诅咒2-反转] 反转后持续给予的幸运值加成。")
                .defineInRange("curse2ReversedLuckBonus", 10.0D, 0.0D, 1024.0D);

        CURSE_2_REVERSED_SPAWN_INTERVAL = BUILDER
                .comment("[诅咒2-反转] 奴仆召唤检测间隔，单位为 tick。12000 tick = 10 分钟。")
                .defineInRange("curse2ReversedSpawnInterval", 12000, 20, 720000);

        CURSE_2_REVERSED_SPAWN_CHANCE = BUILDER
                .comment("[诅咒2-反转] 每次到达检测间隔后，召来王权奴仆的概率。")
                .defineInRange("curse2ReversedSpawnChance", 0.15D, 0.0D, 1.0D);

        CURSE_2_REVERSED_MINION_DURATION = BUILDER
                .comment("[诅咒2-反转] 王权奴仆持续时间，单位为 tick。400 tick = 20 秒。")
                .defineInRange("curse2ReversedMinionDuration", 400, 20, 72000);

        CURSE_2_REVERSED_ENTITY_WHITELIST = BUILDER
                .comment("[诅咒2-反转] 王权奴仆白名单。只会从这里召唤实体，也支持其他模组生物。")
                .defineListAllowEmpty(
                        "curse2ReversedEntityWhitelist",
                        List.of("minecraft:wither_skeleton", "minecraft:husk", "minecraft:stray", "minecraft:vindicator"),
                        Config::validateEntityTypeName);
        BUILDER.pop();

        BUILDER.push("诅咒三");
        CURSE_3_XP_MULTIPLIER = BUILDER
                .comment("[诅咒3] 击杀任意生物后的经验倍率。0.5 表示只能获得原本 50% 的经验。")
                .defineInRange("curse3XpMultiplier", 0.5D, 0.0D, 10.0D);

        CURSE_3_ENCHANT_COST_MULTIPLIER = BUILDER
                .comment("[诅咒3] 附魔台等级消耗倍率。2.0 表示显示需求翻倍。")
                .defineInRange("curse3EnchantCostMultiplier", 2.0D, 0.0D, 10.0D);

        CURSE_3_ANVIL_COST_MULTIPLIER = BUILDER
                .comment("[诅咒3] 铁砧等级消耗倍率。2.0 表示铁砧经验消耗翻倍。")
                .defineInRange("curse3AnvilCostMultiplier", 2.0D, 0.0D, 10.0D);
        BUILDER.pop();

        BUILDER.push("诅咒四");
        CURSE_4_TRADE_SURCHARGE = BUILDER
                .comment("[诅咒4] 村民与流浪商人交易价格上浮倍率。0.25 表示价格上涨 25%。")
                .defineInRange("curse4TradeSurcharge", 0.25D, 0.0D, 10.0D);
        BUILDER.pop();

        BUILDER.push("诅咒五");
        CURSE_5_MAX_GUILT = BUILDER
                .comment("[诅咒5] 王罪最大值。达到后会触发强制处决、雷暴、雷击与背包封印。")
                .defineInRange("curse5MaxGuilt", 399, 1, 100000);//王罪值

        CURSE_5_PURIFICATION_AMOUNT = BUILDER
                .comment("[诅咒5] 每张净化卷轴可消除的王罪值。")
                .defineInRange("curse5PurificationAmount", 50, 1, 100000);

        CURSE_5_LIGHTNING_STRIKES = BUILDER
                .comment("[诅咒5] 王罪满值时降下的闪电次数。")
                .defineInRange("curse5LightningStrikes", 10, 1, 200);

        CURSE_5_LOCK_DURATION = BUILDER
                .comment("[诅咒5] 王罪满值后，整套背包被封印的持续时间，单位为 tick。")
                .defineInRange("curse5LockDuration", 1200, 20, 72000);//背包锁定时间，单位tick
        BUILDER.pop();

        BUILDER.push("诅咒六");
        MAIN_HAND_LOCK_DELAY = BUILDER
                .comment("[诅咒6] 首次主手封印触发前的延迟时间，单位为 tick。36000 tick = 30 分钟。")
                .defineInRange("mainHandLockDelay", 36000, 20, 720000);

        MAIN_HAND_LOCK_DURATION = BUILDER
                .comment("[诅咒6] 首次主手封印的持续时间，单位为 tick。1200 tick = 1 分钟。")
                .defineInRange("mainHandLockDuration", 1200, 20, 72000);

        INVENTORY_LOCK_INTERVAL = BUILDER
                .comment("[诅咒6] 首次主手封印后，随机封印背包物品的触发间隔，单位为 tick。6000 tick = 5 分钟。")
                .defineInRange("inventoryLockInterval", 6000, 20, 720000);

        INVENTORY_LOCK_DURATION = BUILDER
                .comment("[诅咒6] 随机封印背包物品的持续时间，单位为 tick。")
                .defineInRange("inventoryLockDuration", 1200, 20, 72000);
        BUILDER.pop();

        BUILDER.push("诅咒七");
        END_WEAKNESS_AMPLIFIER = BUILDER
                .comment("[诅咒7] 玩家位于末地时获得的虚弱等级。")
                .defineInRange("endWeaknessAmplifier", 0, 0, 10);
        BUILDER.pop();

        BUILDER.push("诅咒八");
        HELL_REBOUND_CHANCE = BUILDER
                .comment("[诅咒8] 诅咒8在地狱生效后，攻击怪物时反伤到自身的概率。")
                .defineInRange("hellReboundChance", 0.25D, 0.0D, 1.0D);

        HELL_REBOUND_DAMAGE_MULTIPLIER = BUILDER
                .comment("[诅咒8] 反伤时玩家承受的伤害倍率。1.10 表示承受造成伤害的 110%。")
                .defineInRange("hellReboundDamageMultiplier", 1.25D, 0.0D, 10.0D);

        HELL_REBOUND_FLAT_DAMAGE = BUILDER
                .comment("[诅咒8] 反伤时额外追加的固定伤害。最终伤害 = 原伤害 * 倍率 + 固定额外伤害。")
                .defineInRange("hellReboundFlatDamage", 0.0D, 0.0D, 100.0D);

        CURSE_8_BOSS_WHITELIST = BUILDER
                .comment("[诅咒8] 用于完成诅咒8反转的 Boss 白名单。支持其他模组的生物实体 ID，默认是末影龙。")
                .defineListAllowEmpty(
                        "curse8BossWhitelist",
                        List.of("minecraft:ender_dragon"),
                        Config::validateEntityTypeName);
        BUILDER.pop();

        BUILDER.push("诅咒九");
        CURSE_9_MAX_SATIETY = BUILDER
                .comment("[诅咒9] 饥饿债 HUD 与内部最大饱食值。反转前未拾取饱食水晶时会从这里开始递减。")
                .defineInRange("curse9MaxSatiety", 40, 1, 200);
        CURSE_9_REVERSED_KILL_TARGET = BUILDER
                .comment("[诅咒9-反转] 只有击杀过这里配置的 1 个生物后，才会开始累计不同食物种类。支持其他模组生物实体 ID。")
                .define("curse9ReversedKillTarget", "minecraft:husk", Config::validateEntityTypeName);
        BUILDER.pop();

        BUILDER.pop();

        BUILDER.push("杜阿特之心");
        HEART_TRIGGER_HOSTILE_COUNT = BUILDER
                .comment("[杜阿特之心] 可使用主动觉醒前，范围内所需的敌对实体数量。")
                .defineInRange("heartTriggerHostileCount", 6, 1, 256);

        HEART_TRIGGER_RADIUS = BUILDER
                .comment("[杜阿特之心] 检测附近敌对实体数量的范围半径，单位为格。")
                .defineInRange("heartTriggerRadius", 16, 1, 128);

        HEART_ACTIVE_DURATION = BUILDER
                .comment("[杜阿特之心] 主动觉醒的持续时间，单位为 tick。600 tick = 30 秒。")
                .defineInRange("heartActiveDuration", 600, 20, 72000);

        HEART_ACTIVE_XP_COST = BUILDER
                .comment("[杜阿特之心] 每次触发主动觉醒时需要支付的经验等级。")
                .defineInRange("heartActiveXpCost", 3, 0, 100);

        HEART_PROMPT_INTERVAL = BUILDER
                .comment("[杜阿特之心] 觉醒提示再次出现前的间隔，单位为 tick。")
                .defineInRange("heartPromptInterval", 40, 1, 72000);

        HEART_BATTLE_STACK_LIMIT = BUILDER
                .comment("[杜阿特之心] 通过击杀敌对怪物可叠加的战意最大层数。")
                .defineInRange("heartBattleStackLimit", 10, 1, 100);

        HEART_BATTLE_STACK_TIMEOUT = BUILDER
                .comment("[杜阿特之心] 战意层数超时时间，单位为 tick。若在此时间内未再次击杀敌对怪物，则层数清零。100 tick = 5 秒。")
                .defineInRange("heartBattleStackTimeout", 100, 1, 72000);

        HEART_BATTLE_DAMAGE_PER_STACK = BUILDER
                .comment("[杜阿特之心] 每层战意额外提供的攻击伤害。")
                .defineInRange("heartBattleDamagePerStack", 3.0D, 0.0D, 1000.0D);

        HEART_WARD_COOLDOWN = BUILDER
                .comment("[杜阿特之心] 满层战意触发免伤后的冷却时间，单位为 tick。7200 tick = 360 秒。")
                .defineInRange("heartWardCooldown", 7200, 20, 720000);

        HEART_WARD_INVULNERABILITY = BUILDER
                .comment("[杜阿特之心] 触发免伤后获得的无敌时间，单位为 tick。100 tick = 5 秒。")
                .defineInRange("heartWardInvulnerability", 100, 1, 72000);

        HEART_WARD_MANUAL_TRIGGER = BUILDER
                .comment("[杜阿特之心] 为 true 时，战意满层后需要玩家手动按键触发免伤；为 false 时，满层立即自动触发。")
                .define("heartWardManualTrigger", true);

        HEART_DESERT_REACH_BONUS = BUILDER
                .comment("[杜阿特之心] 佩戴者位于沙漠或恶地群系时获得的交互距离加成。")
                .defineInRange("heartDesertReachBonus", 5.0D, 0.0D, 64.0D);
        BUILDER.pop();

        BUILDER.push("乌拉埃乌斯项链");
        NECKLACE_EFFECT_ID = BUILDER
                .comment("[项链] 项链命中怪物时允许附着的唯一效果 ID。仅填写 1 个，支持其他模组效果。")
                .define("necklaceEffectId", "minecraft:poison", Config::validateEffectName);

        NECKLACE_STACK_TIMEOUT = BUILDER
                .comment("[项链] 每层效果的持续时间，单位 tick。60 tick = 3 秒。")
                .defineInRange("necklaceStackTimeout", 200, 1, 72000);

        NECKLACE_MAX_STACKS = BUILDER
                .comment("[项链] 爆发前需要叠满的层数。")
                .defineInRange("necklaceMaxStacks", 8, 2, 64);

        NECKLACE_BURST_DAMAGE_MULTIPLIER = BUILDER
                .comment("[项链] 叠满后转化出的爆发伤害倍率。3.0 表示 300%。")
                .defineInRange("necklaceBurstDamageMultiplier", 3.0D, 0.1D, 100.0D);

        NECKLACE_RAIN_BONUS_MULTIPLIER = BUILDER
                .comment("[项链] 雨天或雷暴时，爆发伤害额外乘上的倍率。1.5 表示额外 50%。")
                .defineInRange("necklaceRainBonusMultiplier", 1.5D, 1.0D, 100.0D);

        NECKLACE_RAIN_REWARD_DURATION = BUILDER
                .comment("[项链] 雨天爆发后，玩家获得伤害吸收与抗性提升的持续时间，单位 tick。3600 tick = 3 分钟。")
                .defineInRange("necklaceRainRewardDuration", 3600, 20, 72000);

        NECKLACE_RAIN_ABSORPTION_AMPLIFIER = BUILDER
                .comment("[项链] 雨天爆发后给予玩家的伤害吸收等级。4 表示伤害吸收 V。")
                .defineInRange("necklaceRainAbsorptionAmplifier", 4, 0, 255);

        NECKLACE_RAIN_RESISTANCE_AMPLIFIER = BUILDER
                .comment("[项链] 雨天爆发后给予玩家的抗性提升等级。4 表示抗性提升 V。")
                .defineInRange("necklaceRainResistanceAmplifier", 4, 0, 255);
        BUILDER.pop();

        BUILDER.push("通用");
        START_WITH_SEAL = BUILDER
                .comment("[通用] 为 true 时，新玩家开局获得卡迭石咒印。")
                .define("startWithSeal", true);

        START_WITH_SEAL_EQUIPPED = BUILDER
                .comment("[通用] 为 true 时，新玩家开局直接将卡迭石咒印佩戴在诅咒槽位中。开启后不会再额外放入初始背包。")
                .define("startWithSealEquipped", false);

        START_WITH_RAMASSES_MANUAL = BUILDER
                .comment("[通用] 为 true 时，新玩家开局获得一本拉美西斯手册。")
                .define("startWithRamessesManual", true);
        BUILDER.pop();
    }

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static final ForgeConfigSpec.IntValue RANDOM_CURSE_INTERVAL;
    public static final ForgeConfigSpec.IntValue RANDOM_CURSE_DURATION;
    public static final ForgeConfigSpec.IntValue RANDOM_CURSE_AMPLIFIER_MIN;
    public static final ForgeConfigSpec.IntValue RANDOM_CURSE_AMPLIFIER_MAX;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CURSE_1_EFFECT_WHITELIST;
    public static final ForgeConfigSpec.IntValue CURSE_1_REVERSED_TRIGGER_COOLDOWN;
    public static final ForgeConfigSpec.IntValue CURSE_1_REVERSED_EFFECT_DURATION;
    public static final ForgeConfigSpec.IntValue CURSE_1_REVERSED_EFFECT_COUNT;
    public static final ForgeConfigSpec.IntValue CURSE_1_REVERSED_AMPLIFIER_MIN;
    public static final ForgeConfigSpec.IntValue CURSE_1_REVERSED_AMPLIFIER_MAX;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CURSE_1_REVERSED_EFFECT_WHITELIST;
    public static final ForgeConfigSpec.IntValue CURSE_2_SPAWN_INTERVAL;
    public static final ForgeConfigSpec.DoubleValue CURSE_2_SPAWN_CHANCE;
    public static final ForgeConfigSpec.DoubleValue CURSE_2_LUCK_PENALTY;
    public static final ForgeConfigSpec.IntValue CURSE_2_SPAWN_RADIUS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CURSE_2_ENTITY_WHITELIST;
    public static final ForgeConfigSpec.DoubleValue CURSE_2_REVERSED_LUCK_BONUS;
    public static final ForgeConfigSpec.IntValue CURSE_2_REVERSED_SPAWN_INTERVAL;
    public static final ForgeConfigSpec.DoubleValue CURSE_2_REVERSED_SPAWN_CHANCE;
    public static final ForgeConfigSpec.IntValue CURSE_2_REVERSED_MINION_DURATION;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CURSE_2_REVERSED_ENTITY_WHITELIST;
    public static final ForgeConfigSpec.DoubleValue CURSE_3_XP_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue CURSE_3_ENCHANT_COST_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue CURSE_3_ANVIL_COST_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue CURSE_4_TRADE_SURCHARGE;
    public static final ForgeConfigSpec.IntValue CURSE_5_MAX_GUILT;
    public static final ForgeConfigSpec.IntValue CURSE_5_PURIFICATION_AMOUNT;
    public static final ForgeConfigSpec.IntValue CURSE_5_LIGHTNING_STRIKES;
    public static final ForgeConfigSpec.IntValue CURSE_5_LOCK_DURATION;
    public static final ForgeConfigSpec.IntValue MAIN_HAND_LOCK_DELAY;
    public static final ForgeConfigSpec.IntValue MAIN_HAND_LOCK_DURATION;
    public static final ForgeConfigSpec.IntValue INVENTORY_LOCK_INTERVAL;
    public static final ForgeConfigSpec.IntValue INVENTORY_LOCK_DURATION;
    public static final ForgeConfigSpec.IntValue END_WEAKNESS_AMPLIFIER;
    public static final ForgeConfigSpec.DoubleValue HELL_REBOUND_CHANCE;
    public static final ForgeConfigSpec.DoubleValue HELL_REBOUND_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue HELL_REBOUND_FLAT_DAMAGE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CURSE_8_BOSS_WHITELIST;
    public static final ForgeConfigSpec.IntValue CURSE_9_MAX_SATIETY;
    public static final ForgeConfigSpec.ConfigValue<String> CURSE_9_REVERSED_KILL_TARGET;
    public static final ForgeConfigSpec.BooleanValue START_WITH_SEAL;
    public static final ForgeConfigSpec.BooleanValue START_WITH_SEAL_EQUIPPED;
    public static final ForgeConfigSpec.BooleanValue START_WITH_RAMASSES_MANUAL;
    public static final ForgeConfigSpec.IntValue HEART_TRIGGER_HOSTILE_COUNT;
    public static final ForgeConfigSpec.IntValue HEART_TRIGGER_RADIUS;
    public static final ForgeConfigSpec.IntValue HEART_ACTIVE_DURATION;
    public static final ForgeConfigSpec.IntValue HEART_ACTIVE_XP_COST;
    public static final ForgeConfigSpec.IntValue HEART_PROMPT_INTERVAL;
    public static final ForgeConfigSpec.IntValue HEART_BATTLE_STACK_LIMIT;
    public static final ForgeConfigSpec.IntValue HEART_BATTLE_STACK_TIMEOUT;
    public static final ForgeConfigSpec.DoubleValue HEART_BATTLE_DAMAGE_PER_STACK;
    public static final ForgeConfigSpec.IntValue HEART_WARD_COOLDOWN;
    public static final ForgeConfigSpec.IntValue HEART_WARD_INVULNERABILITY;
    public static final ForgeConfigSpec.BooleanValue HEART_WARD_MANUAL_TRIGGER;
    public static final ForgeConfigSpec.DoubleValue HEART_DESERT_REACH_BONUS;
    public static final ForgeConfigSpec.ConfigValue<String> NECKLACE_EFFECT_ID;
    public static final ForgeConfigSpec.IntValue NECKLACE_STACK_TIMEOUT;
    public static final ForgeConfigSpec.IntValue NECKLACE_MAX_STACKS;
    public static final ForgeConfigSpec.DoubleValue NECKLACE_BURST_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue NECKLACE_RAIN_BONUS_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue NECKLACE_RAIN_REWARD_DURATION;
    public static final ForgeConfigSpec.IntValue NECKLACE_RAIN_ABSORPTION_AMPLIFIER;
    public static final ForgeConfigSpec.IntValue NECKLACE_RAIN_RESISTANCE_AMPLIFIER;

    private static boolean validateEffectName(final Object obj) {
        return obj instanceof final String effectName && ForgeRegistries.MOB_EFFECTS.containsKey(new ResourceLocation(effectName));
    }

    private static boolean validateEntityTypeName(final Object obj) {
        return obj instanceof final String entityId && ForgeRegistries.ENTITY_TYPES.containsKey(new ResourceLocation(entityId));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
    }

    public static MobEffect getRandomNegativeEffect(int seed) {
        return getEffectFromPool(CURSE_1_EFFECT_WHITELIST.get(), seed);
    }

    public static MobEffect getRandomCurseOneReversedEffect(int seed) {
        return getEffectFromPool(CURSE_1_REVERSED_EFFECT_WHITELIST.get(), seed);
    }

    public static EntityType<?> getRandomCurseTwoEntityType(int seed) {
        List<EntityType<?>> filteredPool = CURSE_2_ENTITY_WHITELIST.get().stream()
                .map(ResourceLocation::new)
                .map(ForgeRegistries.ENTITY_TYPES::getValue)
                .filter(entityType -> entityType != null && entityType.getCategory() == MobCategory.MONSTER)
                .toList();
        return getEntityTypeFromPool(filteredPool, seed);
    }

    public static EntityType<?> getRandomCurseTwoMinionEntityType(int seed) {
        List<EntityType<?>> filteredPool = CURSE_2_REVERSED_ENTITY_WHITELIST.get().stream()
                .map(ResourceLocation::new)
                .map(ForgeRegistries.ENTITY_TYPES::getValue)
                .filter(entityType -> entityType != null)
                .toList();
        return getEntityTypeFromPool(filteredPool, seed);
    }

    public static MobEffect getNecklaceEffect() {
        return ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(NECKLACE_EFFECT_ID.get()));
    }

    public static boolean isCurseEightBoss(EntityType<?> entityType) {
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
        if (entityId == null) {
            return false;
        }
        return CURSE_8_BOSS_WHITELIST.get().stream()
                .map(ResourceLocation::new)
                .anyMatch(entityId::equals);
    }

    public static boolean isCurseNineReversedTarget(EntityType<?> entityType) {
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
        if (entityId == null) {
            return false;
        }
        return entityId.equals(new ResourceLocation(CURSE_9_REVERSED_KILL_TARGET.get()));
    }

    private static MobEffect getEffectFromPool(List<? extends String> pool, int seed) {
        if (pool.isEmpty()) {
            return null;
        }
        String effectId = pool.get(Math.floorMod(seed, pool.size()));
        return ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectId));
    }

    private static EntityType<?> getEntityTypeFromPool(List<EntityType<?>> pool, int seed) {
        if (pool.isEmpty()) {
            return null;
        }
        return pool.get(Math.floorMod(seed, pool.size()));
    }
}
