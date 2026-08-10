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
                .comment("[诅咒一] 随机负面效果触发间隔，单位为 tick。6000 tick = 5 分钟。")
                .defineInRange("randomCurseInterval", 6000, 20, 72000);

        RANDOM_CURSE_DURATION = BUILDER
                .comment("[诅咒一] 每次随机负面效果的持续时间，单位为 tick。")
                .defineInRange("randomCurseDuration", 160, 20, 72000);

        RANDOM_CURSE_AMPLIFIER_MIN = BUILDER
                .comment("[诅咒一] 随机负面效果的最小等级。0 表示 I 级。")
                .defineInRange("randomCurseAmplifierMin", 0, 0, 10);

        RANDOM_CURSE_AMPLIFIER_MAX = BUILDER
                .comment("[诅咒一] 随机负面效果的最大等级。0 表示 I 级。")
                .defineInRange("randomCurseAmplifierMax", 1, 0, 10);

        CURSE_1_EFFECT_WHITELIST = BUILDER
                .comment("[诅咒一] 可被随机抽取的负面效果 ID 白名单，支持其他模组效果。")
                .defineListAllowEmpty("curse1EffectWhitelist",
                        List.of("minecraft:weakness", "minecraft:slowness", "minecraft:hunger", "minecraft:blindness", "minecraft:poison", "minecraft:wither", "minecraft:unluck", "minecraft:levitation"),
                        Config::validateEffectName);
        CURSE_1_REVERSED_TRIGGER_COOLDOWN = BUILDER
                .comment("[诅咒一-反转] 反应式增益的内部冷却时间，单位为 tick。")
                .defineInRange("curse1ReversedTriggerCooldown", 200, 1, 72000);

        CURSE_1_REVERSED_EFFECT_DURATION = BUILDER
                .comment("[诅咒一-反转] 每个增益效果的持续时间，单位为 tick。")
                .defineInRange("curse1ReversedEffectDuration", 100, 20, 72000);

        CURSE_1_REVERSED_EFFECT_COUNT = BUILDER
                .comment("[诅咒一-反转] 每次触发时给予的增益效果数量。")
                .defineInRange("curse1ReversedEffectCount", 2, 1, 16);

        CURSE_1_REVERSED_AMPLIFIER_MIN = BUILDER
                .comment("[诅咒一-反转] 给予增益效果的最小等级。0 表示 I 级。")
                .defineInRange("curse1ReversedAmplifierMin", 0, 0, 10);

        CURSE_1_REVERSED_AMPLIFIER_MAX = BUILDER
                .comment("[诅咒一-反转] 给予增益效果的最大等级。0 表示 I 级。")
                .defineInRange("curse1ReversedAmplifierMax", 1, 0, 10);

        CURSE_1_REVERSED_EFFECT_WHITELIST = BUILDER
                .comment("[诅咒一-反转] 反应式增益候选白名单；非增益效果会在运行时被忽略。")
                .defineListAllowEmpty("curse1ReversedEffectWhitelist",
                        List.of("minecraft:regeneration", "minecraft:resistance", "minecraft:speed", "minecraft:fire_resistance", "minecraft:damage_boost"),
                        Config::validateEffectName);

        CURSE_1_REVERSAL_CAULDRON_ITEMS = BUILDER
                .comment("[诅咒一-反转] 双神试杯需要投入水炼药锅附近的供物，格式为 物品ID=数量。支持其他模组物品；空列表会禁用该反转仪式。")
                .defineListAllowEmpty("curse1ReversalCauldronItems",
                        List.of(
                                "minecraft:enchanted_golden_apple=1",
                                "minecraft:milk_bucket=2",
                                "ramesses_curse:ember_of_duat=1",
                                "minecraft:honey_bottle=1"),
                        Config::validateItemAmountEntry);
        BUILDER.pop();

        BUILDER.push("诅咒二");
        CURSE_2_SPAWN_INTERVAL = BUILDER
                .comment("[诅咒二] 敌对生物召唤检测间隔，单位为 tick。12000 tick = 10 分钟。")
                .defineInRange("curse2SpawnInterval", 12000, 20, 720000);

        CURSE_2_SPAWN_CHANCE = BUILDER
                .comment("[诅咒二] 每次达到检测间隔后，召唤敌对生物的概率。")
                .defineInRange("curse2SpawnChance", 0.15D, 0.0D, 1.0D);

        CURSE_2_LUCK_PENALTY = BUILDER
                .comment("[诅咒二] 每累计佩戴 5 分钟减少的幸运值。")
                .defineInRange("curse2LuckPenalty", 2.0D, 0.0D, 1024.0D);

        CURSE_2_SPAWN_RADIUS = BUILDER
                .comment("[诅咒二] 召唤敌对生物时，围绕玩家的水平生成半径，单位为格。")
                .defineInRange("curse2SpawnRadius", 8, 2, 64);

        CURSE_2_ENTITY_WHITELIST = BUILDER
                .comment("[诅咒二] 可被召唤的敌对生物实体 ID 白名单，支持其他模组生物。")
                .defineListAllowEmpty(
                        "curse2EntityWhitelist",
                        List.of("minecraft:zombie", "minecraft:skeleton", "minecraft:spider", "minecraft:creeper"),
                        Config::validateEntityTypeName);

        CURSE_2_REVERSED_LUCK_BONUS = BUILDER
                .comment("[诅咒二-反转] 返还既有幸运损失后，每继续佩戴 5 分钟增加的幸运值。")
                .defineInRange("curse2ReversedLuckPerFiveMinutes", 2.0D, 0.0D, 1024.0D);

        CURSE_2_REVERSED_SPAWN_INTERVAL = BUILDER
                .comment("[诅咒二-反转] 王权奴仆召唤检测间隔，单位为 tick。12000 tick = 10 分钟。")
                .defineInRange("curse2ReversedSpawnInterval", 12000, 20, 720000);

        CURSE_2_REVERSED_SPAWN_CHANCE = BUILDER
                .comment("[诅咒二-反转] 每次达到检测间隔后，召唤王权奴仆的概率。")
                .defineInRange("curse2ReversedSpawnChance", 0.15D, 0.0D, 1.0D);

        CURSE_2_REVERSED_MINION_DURATION = BUILDER
                .comment("[诅咒二-反转] 王权奴仆持续时间，单位为 tick。400 tick = 20 秒。")
                .defineInRange("curse2ReversedMinionDuration", 400, 20, 72000);

        CURSE_2_REVERSED_ENTITY_WHITELIST = BUILDER
                .comment("[诅咒二-反转] 王权奴仆实体 ID 白名单，支持其他模组生物。")
                .defineListAllowEmpty(
                        "curse2ReversedEntityWhitelist",
                        List.of("minecraft:wither_skeleton", "minecraft:husk", "minecraft:stray", "minecraft:vindicator"),
                        Config::validateEntityTypeName);
        BUILDER.pop();

        BUILDER.push("诅咒三");
        CURSE_3_XP_MULTIPLIER = BUILDER
                .comment("[诅咒三] 击杀任意生物后的经验倍率。0.5 表示只获得原本 50% 的经验。")
                .defineInRange("curse3XpMultiplier", 0.5D, 0.0D, 10.0D);

        CURSE_3_ENCHANT_COST_MULTIPLIER = BUILDER
                .comment("[诅咒三] 附魔台等级消耗倍率。2.0 表示显示需求翻倍。")
                .defineInRange("curse3EnchantCostMultiplier", 2.0D, 0.0D, 10.0D);

        CURSE_3_ANVIL_COST_MULTIPLIER = BUILDER
                .comment("[诅咒三] 铁砧等级消耗倍率。2.0 表示铁砧经验消耗翻倍。")
                .defineInRange("curse3AnvilCostMultiplier", 2.0D, 0.0D, 10.0D);
        BUILDER.pop();

        BUILDER.push("诅咒四");
        CURSE_4_TRADE_SURCHARGE = BUILDER
                .comment("[诅咒四] 村民与流浪商人交易价格上浮倍率。0.25 表示价格上涨 25%。")
                .defineInRange("curse4TradeSurcharge", 0.25D, 0.0D, 10.0D);
        BUILDER.pop();

        BUILDER.push("诅咒五");
        CURSE_5_MAX_GUILT = BUILDER
                .comment("[诅咒五] 王罪最大值。达到后会触发处决、雷暴、雷击与背包封印。")
                .defineInRange("curse5MaxGuilt", 399, 1, 100000);
        CURSE_5_MERCY_MAX = BUILDER
                .comment("[诅咒五-反转] 接受反转后，慈悲值达到该数值即可储存一次逆转死亡。范围：299-99999。")
                .defineInRange("curse5MercyMax", 1299, 299, 99999);
        CURSE_5_PURIFICATION_AMOUNT = BUILDER
                .comment("[诅咒五] 每张净化卷轴可消除的王罪值。")
                .defineInRange("curse5PurificationAmount", 50, 1, 100000);

        CURSE_5_LIGHTNING_STRIKES = BUILDER
                .comment("[诅咒五] 王罪满值时降下的闪电次数。")
                .defineInRange("curse5LightningStrikes", 10, 1, 200);

        CURSE_5_LOCK_DURATION = BUILDER
                .comment("[诅咒五] 王罪满值后背包被封印的持续时间，单位为 tick。1200 tick = 1 分钟。")
                .defineInRange("curse5LockDuration", 1200, 20, 72000);

        CURSE_5_INNOCENT_ENTITY_WHITELIST = BUILDER
                .comment("[诅咒五] 杀死后会增加王罪值的额外实体 ID 白名单，支持其他模组生物。白名单优先于原版中立生物分类判定。")
                .defineListAllowEmpty(
                        "curse5InnocentEntityWhitelist",
                        List.<String>of(),
                        Config::validateEntityTypeName);
        BUILDER.pop();

        BUILDER.push("诅咒六");
        MAIN_HAND_LOCK_DELAY = BUILDER
                .comment("[诅咒六] 首次主手封印触发前的延迟时间，单位为 tick。36000 tick = 30 分钟。")
                .defineInRange("mainHandLockDelay", 36000, 20, 720000);

        MAIN_HAND_LOCK_DURATION = BUILDER
                .comment("[诅咒六] 首次主手封印的持续时间，单位为 tick。1200 tick = 1 分钟。")
                .defineInRange("mainHandLockDuration", 1200, 20, 72000);

        INVENTORY_LOCK_INTERVAL = BUILDER
                .comment("[诅咒六] 首次主手封印后，随机封印背包物品的触发间隔，单位为 tick。6000 tick = 5 分钟。")
                .defineInRange("inventoryLockInterval", 6000, 20, 720000);

        INVENTORY_LOCK_DURATION = BUILDER
                .comment("[诅咒六] 随机封印背包物品的持续时间，单位为 tick。")
                .defineInRange("inventoryLockDuration", 1200, 20, 72000);

        CURSE_6_VOID_OFFERINGS = BUILDER
                .comment("[诅咒六-反转] 丢入末地虚空完成交换所需的两个物品 ID。默认需要龙蛋和下界之星，支持其他模组物品。")
                .defineList("curse6VoidOfferings", List.of("minecraft:dragon_egg", "minecraft:nether_star"), Config::validateItemName);
        BUILDER.pop();

        BUILDER.push("诅咒七");
        CURSE_7_LOOT_SUPPRESSION_CHANCE = BUILDER
                .comment("[诅咒七] 踏足末地并留下印记后，玩家击杀实体时不产生任何战利品的概率。0.05 表示 5%。")
                .defineInRange("curse7LootSuppressionChance", 0.05D, 0.0D, 1.0D);
        BUILDER.pop();

        BUILDER.push("诅咒八");
        HELL_REBOUND_CHANCE = BUILDER
                .comment("[诅咒八] 诅咒八在下界生效后，攻击怪物时触发反伤或反转助攻的概率。")
                .defineInRange("hellReboundChance", 0.25D, 0.0D, 1.0D);

        HELL_REBOUND_COOLDOWN = BUILDER
                .comment("[诅咒八] 反伤或反转助攻成功触发后的冷却时间，单位为 tick。默认 60 tick = 3 秒。")
                .defineInRange("hellReboundCooldown", 60, 0, 72000);

        HELL_REBOUND_DAMAGE_MULTIPLIER = BUILDER
                .comment("[诅咒八] 未反转时玩家承受反伤的倍率。1.25 表示造成伤害的 125%。")
                .defineInRange("hellReboundDamageMultiplier", 1.25D, 0.0D, 10.0D);

        HELL_REBOUND_FLAT_DAMAGE = BUILDER
                .comment("[诅咒八] 未反转时额外追加的固定反伤。最终反伤 = 原伤害 * 倍率 + 固定值。")
                .defineInRange("hellReboundFlatDamage", 0.0D, 0.0D, 100.0D);

        HELL_REBOUND_DAMAGE_CAP = BUILDER
                .comment("[诅咒八] 未反转时单次反伤的最高伤害。默认最高反弹 50 点伤害。")
                .defineInRange("hellReboundDamageCap", 50.0D, 0.0D, 1000000.0D);

        HELL_REVERSED_ASSIST_DAMAGE_CAP = BUILDER
                .comment("[诅咒八-反转] 单次助推能够额外追加的最高伤害。默认最高追加 200 点伤害。")
                .defineInRange("hellReversedAssistDamageCap", 200.0D, 0.0D, 1000000.0D);

        CURSE_8_BOSS_WHITELIST = BUILDER
                .comment("[诅咒八] 用于完成诅咒八反转的 Boss 实体 ID 白名单，支持其他模组生物。")
                .defineListAllowEmpty(
                        "curse8BossWhitelist",
                        List.of("minecraft:ender_dragon"),
                        Config::validateEntityTypeName);
        BUILDER.pop();

        BUILDER.push("诅咒九");
        CURSE_9_MAX_SATIETY = BUILDER
                .comment("[诅咒九] 饥饿债与内部最大饱食值。反转前未取回饱食水晶时会从这里开始递减。")
                .defineInRange("curse9MaxSatiety", 40, 1, 200);
        CURSE_9_REVERSED_KILL_TARGET = BUILDER
                .comment("[诅咒九-反转] 击杀这里配置的生物后，才会开始累计不同食物种类。支持其他模组实体 ID。")
                .define("curse9ReversedKillTarget", "minecraft:husk", Config::validateEntityTypeName);

        CURSE_9_REVERSED_FOOD_TYPES = BUILDER
                .comment("[诅咒九-反转] 击杀指定生物后，需要累计食用多少种不同的可恢复饱食度食物才会完成反转。")
                .defineInRange("curse9ReversedFoodTypes", 16, 1, 1000);
        BUILDER.pop();

        BUILDER.pop();

        BUILDER.push("杜阿特之心");
        HEART_TRIGGER_HOSTILE_COUNT = BUILDER
                .comment("[杜阿特之心] 可使用主动觉醒前，范围内需要存在的敌对实体数量。")
                .defineInRange("heartTriggerHostileCount", 6, 1, 256);

        HEART_TRIGGER_RADIUS = BUILDER
                .comment("[杜阿特之心] 检测附近敌对实体数量的半径，单位为格。")
                .defineInRange("heartTriggerRadius", 16, 1, 128);

        HEART_ACTIVE_DURATION = BUILDER
                .comment("[杜阿特之心] 主动觉醒持续时间，单位为 tick。600 tick = 30 秒。")
                .defineInRange("heartActiveDuration", 600, 20, 72000);

        HEART_ACTIVE_XP_COST = BUILDER
                .comment("[杜阿特之心] 每次触发主动觉醒需要支付的经验等级。")
                .defineInRange("heartActiveXpCost", 3, 0, 100);

        HEART_PROMPT_INTERVAL = BUILDER
                .comment("[杜阿特之心] 觉醒提示再次出现前的间隔，单位为 tick。")
                .defineInRange("heartPromptInterval", 40, 1, 72000);

        HEART_BATTLE_STACK_LIMIT = BUILDER
                .comment("[杜阿特之心] 击杀敌对怪物可叠加的战意最大层数。")
                .defineInRange("heartBattleStackLimit", 10, 1, 100);

        HEART_BATTLE_STACK_TIMEOUT = BUILDER
                .comment("[杜阿特之心] 战意层数超时时间，单位为 tick。超过此时间未再次击杀敌对怪物则清零。100 tick = 5 秒。")
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
                .comment("[杜阿特之心] 为 true 时，战意满层后需要玩家手动按键触发免伤；为 false 时满层立即自动触发。")
                .define("heartWardManualTrigger", true);

        HEART_DESERT_REACH_BONUS = BUILDER
                .comment("[杜阿特之心] 佩戴者位于沙漠或恶地群系时获得的交互距离加成。")
                .defineInRange("heartDesertReachBonus", 5.0D, 0.0D, 64.0D);
        BUILDER.pop();

        BUILDER.push("乌拉埃乌斯项链");
        NECKLACE_EFFECT_ID = BUILDER
                .comment("[乌拉埃乌斯项链] 项链命中怪物时允许附着的唯一效果 ID，支持其他模组效果。")
                .define("necklaceEffectId", "minecraft:poison", Config::validateEffectName);

        NECKLACE_STACK_TIMEOUT = BUILDER
                .comment("[乌拉埃乌斯项链] 每层效果持续时间，单位为 tick。200 tick = 10 秒。")
                .defineInRange("necklaceStackTimeout", 200, 1, 72000);

        NECKLACE_MAX_STACKS = BUILDER
                .comment("[乌拉埃乌斯项链] 爆发前需要叠满的层数。")
                .defineInRange("necklaceMaxStacks", 8, 2, 64);

        NECKLACE_BURST_DAMAGE_MULTIPLIER = BUILDER
                .comment("[乌拉埃乌斯项链] 叠满后爆发伤害倍率。3.0 表示 300%。")
                .defineInRange("necklaceBurstDamageMultiplier", 3.0D, 0.1D, 100.0D);

        NECKLACE_RAIN_BONUS_MULTIPLIER = BUILDER
                .comment("[乌拉埃乌斯项链] 雨天或雷雨天爆发伤害的额外倍率。1.5 表示额外 50%。")
                .defineInRange("necklaceRainBonusMultiplier", 1.5D, 1.0D, 100.0D);

        NECKLACE_RAIN_REWARD_DURATION = BUILDER
                .comment("[乌拉埃乌斯项链] 雨天爆发后，玩家获得伤害吸收与抗性提升的持续时间，单位为 tick。3600 tick = 3 分钟。")
                .defineInRange("necklaceRainRewardDuration", 3600, 20, 72000);

        NECKLACE_RAIN_ABSORPTION_AMPLIFIER = BUILDER
                .comment("[乌拉埃乌斯项链] 雨天爆发后给予玩家的伤害吸收等级。4 表示伤害吸收 V。")
                .defineInRange("necklaceRainAbsorptionAmplifier", 4, 0, 255);

        NECKLACE_RAIN_RESISTANCE_AMPLIFIER = BUILDER
                .comment("[乌拉埃乌斯项链] 雨天爆发后给予玩家的抗性提升等级。4 表示抗性提升 V。")
                .defineInRange("necklaceRainResistanceAmplifier", 4, 0, 255);
        BUILDER.pop();

        BUILDER.push("通用");
        DYNASTY_SHIELD_ENABLED = BUILDER
                .comment("[通用] 为 true 时启用王朝护盾；为 false 时敌对生物不会获得护盾，已有护盾也不会生效。")
                .define("dynastyShieldEnabled", true);

        DYNASTY_SHIELD_LEVELS = BUILDER
                .comment("[通用] 王朝护盾可用等级列表。需要与 dynastyShieldMaxValues 一一对应。")
                .defineList("dynastyShieldLevels", List.of(1, 3, 5, 7, 9), Config::validatePositiveInteger);

        DYNASTY_SHIELD_MAX_VALUES = BUILDER
                .comment("[通用] 王朝护盾每个等级对应的最大护盾值。")
                .defineList("dynastyShieldMaxValues", List.of(120, 160, 220, 300, 400), Config::validatePositiveInteger);

        DYNASTY_SHIELD_THREAT_THRESHOLDS = BUILDER
                .comment("[通用] 每个王胄等级所需的最低有效王朝威胁值。需要与 dynastyShieldLevels 一一对应。")
                .defineList("dynastyShieldThreatThresholds", List.of(0, 1000, 2500, 4500, 7000), Config::validateNonNegativeInteger);

        START_WITH_SEAL = BUILDER
                .comment("[通用] 为 true 时，新玩家开局获得卡迭石咒印。")
                .define("startWithSeal", true);

        START_WITH_SEAL_EQUIPPED = BUILDER
                .comment("[通用] 为 true 时，新玩家开局直接将卡迭石咒印佩戴在咒印槽位中。开启后不会再额外放入初始背包。")
                .define("startWithSealEquipped", false);

        START_WITH_RAMASSES_MANUAL = BUILDER
                .comment("[通用] 为 true 时，新玩家开局获得一本拉美西斯手册。")
                .define("startWithRamessesManual", true);

        HUD_GUILT_X_OFFSET = BUILDER
                .comment("[HUD] 王罪条 X 偏移。正数向右，负数向左。")
                .defineInRange("hudGuiltXOffset", 0, -10000, 10000);
        HUD_GUILT_Y_OFFSET = BUILDER
                .comment("[HUD] 王罪条 Y 偏移。正数向下，负数向上。")
                .defineInRange("hudGuiltYOffset", 0, -10000, 10000);
        HUD_MERCY_X_OFFSET = BUILDER
                .comment("[HUD] 慈悲条 X 偏移。正数向右，负数向左。")
                .defineInRange("hudMercyXOffset", 0, -10000, 10000);
        HUD_MERCY_Y_OFFSET = BUILDER
                .comment("[HUD] 慈悲条 Y 偏移。正数向下，负数向上。")
                .defineInRange("hudMercyYOffset", 0, -10000, 10000);
        HUD_SATIETY_DEBT_X_OFFSET = BUILDER
                .comment("[HUD] 饥饿债条 X 偏移。正数向右，负数向左。")
                .defineInRange("hudSatietyDebtXOffset", 0, -10000, 10000);
        HUD_SATIETY_DEBT_Y_OFFSET = BUILDER
                .comment("[HUD] 饥饿债条 Y 偏移。正数向下，负数向上。")
                .defineInRange("hudSatietyDebtYOffset", 0, -10000, 10000);
        HUD_HEART_BATTLE_X_OFFSET = BUILDER
                .comment("[HUD] 战意值文字 X 偏移。正数向右，负数向左。")
                .defineInRange("hudHeartBattleXOffset", 0, -10000, 10000);
        HUD_HEART_BATTLE_Y_OFFSET = BUILDER
                .comment("[HUD] 战意值文字 Y 偏移。正数向下，负数向上。")
                .defineInRange("hudHeartBattleYOffset", 0, -10000, 10000);
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
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CURSE_1_REVERSAL_CAULDRON_ITEMS;
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
    public static final ForgeConfigSpec.IntValue CURSE_5_MERCY_MAX;
    public static final ForgeConfigSpec.IntValue CURSE_5_PURIFICATION_AMOUNT;
    public static final ForgeConfigSpec.IntValue CURSE_5_LIGHTNING_STRIKES;
    public static final ForgeConfigSpec.IntValue CURSE_5_LOCK_DURATION;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CURSE_5_INNOCENT_ENTITY_WHITELIST;
    public static final ForgeConfigSpec.IntValue MAIN_HAND_LOCK_DELAY;
    public static final ForgeConfigSpec.IntValue MAIN_HAND_LOCK_DURATION;
    public static final ForgeConfigSpec.IntValue INVENTORY_LOCK_INTERVAL;
    public static final ForgeConfigSpec.IntValue INVENTORY_LOCK_DURATION;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CURSE_6_VOID_OFFERINGS;
    public static final ForgeConfigSpec.DoubleValue CURSE_7_LOOT_SUPPRESSION_CHANCE;
    public static final ForgeConfigSpec.DoubleValue HELL_REBOUND_CHANCE;
    public static final ForgeConfigSpec.IntValue HELL_REBOUND_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue HELL_REBOUND_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue HELL_REBOUND_FLAT_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue HELL_REBOUND_DAMAGE_CAP;
    public static final ForgeConfigSpec.DoubleValue HELL_REVERSED_ASSIST_DAMAGE_CAP;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CURSE_8_BOSS_WHITELIST;
    public static final ForgeConfigSpec.IntValue CURSE_9_MAX_SATIETY;
    public static final ForgeConfigSpec.ConfigValue<String> CURSE_9_REVERSED_KILL_TARGET;
    public static final ForgeConfigSpec.IntValue CURSE_9_REVERSED_FOOD_TYPES;
    public static final ForgeConfigSpec.BooleanValue START_WITH_SEAL;
    public static final ForgeConfigSpec.BooleanValue START_WITH_SEAL_EQUIPPED;
    public static final ForgeConfigSpec.BooleanValue START_WITH_RAMASSES_MANUAL;
    public static final ForgeConfigSpec.IntValue HUD_GUILT_X_OFFSET;
    public static final ForgeConfigSpec.IntValue HUD_GUILT_Y_OFFSET;
    public static final ForgeConfigSpec.IntValue HUD_MERCY_X_OFFSET;
    public static final ForgeConfigSpec.IntValue HUD_MERCY_Y_OFFSET;
    public static final ForgeConfigSpec.IntValue HUD_SATIETY_DEBT_X_OFFSET;
    public static final ForgeConfigSpec.IntValue HUD_SATIETY_DEBT_Y_OFFSET;
    public static final ForgeConfigSpec.IntValue HUD_HEART_BATTLE_X_OFFSET;
    public static final ForgeConfigSpec.IntValue HUD_HEART_BATTLE_Y_OFFSET;
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
    public static final ForgeConfigSpec.BooleanValue DYNASTY_SHIELD_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> DYNASTY_SHIELD_LEVELS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> DYNASTY_SHIELD_MAX_VALUES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> DYNASTY_SHIELD_THREAT_THRESHOLDS;
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

    private static boolean validateItemName(final Object obj) {
        return obj instanceof final String itemId && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemId));
    }

    private static boolean validateItemAmountEntry(final Object obj) {
        if (!(obj instanceof String entry) || entry.isBlank()) {
            return false;
        }
        int separator = entry.lastIndexOf('=');
        String itemId = separator >= 0 ? entry.substring(0, separator).trim() : entry.trim();
        if (ResourceLocation.tryParse(itemId) == null) {
            return false;
        }
        if (separator < 0) {
            return true;
        }
        try {
            int amount = Integer.parseInt(entry.substring(separator + 1).trim());
            return amount > 0 && amount <= 1000000;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private static boolean validatePositiveInteger(final Object obj) {
        return obj instanceof Integer value && value > 0;
    }

    private static boolean validateNonNegativeInteger(final Object obj) {
        return obj instanceof Integer value && value >= 0;
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
                .<EntityType<?>>map(ForgeRegistries.ENTITY_TYPES::getValue)
                .filter(entityType -> entityType != null && entityType.getCategory() == MobCategory.MONSTER)
                .toList();
        return getEntityTypeFromPool(filteredPool, seed);
    }

    public static EntityType<?> getRandomCurseTwoMinionEntityType(int seed) {
        List<EntityType<?>> filteredPool = CURSE_2_REVERSED_ENTITY_WHITELIST.get().stream()
                .map(ResourceLocation::new)
                .<EntityType<?>>map(ForgeRegistries.ENTITY_TYPES::getValue)
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

    public static boolean isCurseFiveInnocentEntity(EntityType<?> entityType) {
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
        if (entityId == null) {
            return false;
        }
        return CURSE_5_INNOCENT_ENTITY_WHITELIST.get().stream()
                .map(ResourceLocation::tryParse)
                .filter(java.util.Objects::nonNull)
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
