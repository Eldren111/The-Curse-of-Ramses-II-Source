package com.eldren.wutao.ramesses_curse;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
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
        BUILDER.push("饰品配置");

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
        CURSE_2_LUCK_PENALTY = BUILDER
                .comment("[诅咒二] 每累计佩戴 5 分钟减少的幸运值。")
                .defineInRange("curse2LuckPenalty", 2.0D, 0.0D, 1024.0D);

        CURSE_2_VILLAGER_ZOMBIFICATION_ENABLED = BUILDER
                .comment("[诅咒二] 是否让诅咒生效期间玩家遇到的村民必定转化为僵尸村民。")
                .define("curse2VillagerZombificationEnabled", true);

        CURSE_2_VILLAGER_ZOMBIFICATION_RADIUS = BUILDER
                .comment("[诅咒二] 检测并转化村民的半径，单位为格。")
                .defineInRange("curse2VillagerZombificationRadius", 24, 1, 128);

        CURSE_2_REVERSED_LUCK_BONUS = BUILDER
                .comment("[诅咒二-反转] 返还既有幸运损失后，每继续佩戴 5 分钟增加的幸运值。")
                .defineInRange("curse2ReversedLuckPerFiveMinutes", 2.0D, 0.0D, 1024.0D);

        CURSE_2_REVERSED_SPAWN_INTERVAL = BUILDER
                .comment("[诅咒二-反转] 王权奴仆召唤检测间隔，单位为 tick。12000 tick = 10 分钟。")
                .defineInRange("curse2ReversedSpawnInterval", 12000, 20, 720000);

        CURSE_2_REVERSED_SPAWN_CHANCE = BUILDER
                .comment("[诅咒二-反转] 每次达到检测间隔后，召唤王权奴仆的概率。")
                .defineInRange("curse2ReversedSpawnChance", 0.15D, 0.0D, 1.0D);

        CURSE_2_REVERSED_SPAWN_RADIUS = BUILDER
                .comment("[诅咒二-反转] 王权奴仆围绕玩家生成的水平半径，单位为格。")
                .defineInRange("curse2ReversedSpawnRadius", 8, 2, 64);

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
        CURSE_7_LOOT_TRIGGER_MAX_ATTACKS = BUILDER
                .comment("[诅咒七] 每轮最多经过多少次有效击杀攻击，必定随机触发一次无战利品。")
                .defineInRange("curse7LootTriggerMaxAttacks", 7, 1, 100);
        CURSE_7_ENCHANTMENT_ERASURE_ENABLED = BUILDER
                .comment("[诅咒七] 是否启用背包内附魔装备或武器的附魔清除判定。")
                .define("curse7EnchantmentErasureEnabled", true);
        CURSE_7_ENCHANTMENT_ERASURE_CHANCE = BUILDER
                .comment("[诅咒七] 装备或武器进入玩家背包、或其附魔状态发生变化后，失去全部附魔的单次判定概率。0.35 表示 35%。")
                .defineInRange("curse7EnchantmentErasureChance", 0.35D, 0.0D, 1.0D);

        CURSE_7_REVERSED_ATTACK_DAMAGE_BONUS = BUILDER
                .comment("[诅咒七-反转] 常驻攻击伤害加成。0.40 表示增加 40%。")
                .defineInRange("curse7ReversedAttackDamageBonus", 0.40D, 0.0D, 10.0D);
        CURSE_7_REVERSED_ATTACK_SPEED_BONUS = BUILDER
                .comment("[诅咒七-反转] 常驻攻击速度加成。0.25 表示增加 25%。")
                .defineInRange("curse7ReversedAttackSpeedBonus", 0.25D, 0.0D, 10.0D);
        CURSE_7_REVERSED_MOVEMENT_SPEED_BONUS = BUILDER
                .comment("[诅咒七-反转] 常驻移动速度加成。0.20 表示增加 20%。")
                .defineInRange("curse7ReversedMovementSpeedBonus", 0.20D, 0.0D, 10.0D);
        CURSE_7_REVERSED_MAX_HEALTH_BONUS = BUILDER
                .comment("[诅咒七-反转] 常驻最大生命值加成，单位为生命值点数。")
                .defineInRange("curse7ReversedMaxHealthBonus", 8.0D, 0.0D, 1024.0D);
        CURSE_7_REVERSED_ARMOR_TOUGHNESS_BONUS = BUILDER
                .comment("[诅咒七-反转] 常驻盔甲韧性加成。")
                .defineInRange("curse7ReversedArmorToughnessBonus", 4.0D, 0.0D, 1024.0D);
        CURSE_7_REVERSED_END_DAMAGE_REDUCTION = BUILDER
                .comment("[诅咒七-反转] 在末地受到的伤害减免。0.50 表示减免 50%。")
                .defineInRange("curse7ReversedEndDamageReduction", 0.50D, 0.0D, 1.0D);
        CURSE_7_REVERSED_END_XP_MULTIPLIER = BUILDER
                .comment("[诅咒七-反转] 在末地击杀生物获得的经验倍率。2.0 表示获得双倍经验。")
                .defineInRange("curse7ReversedEndXpMultiplier", 2.0D, 0.0D, 10.0D);

        CURSE_7_REVERSED_JUDGMENT_ENABLED = BUILDER
                .comment("[诅咒七-反转] 是否启用每轮攻击触发一次的审判机制。")
                .define("curse7ReversedJudgmentEnabled", true);
        CURSE_7_REVERSED_JUDGMENT_MAX_ATTACKS = BUILDER
                .comment("[诅咒七-反转] 审判每轮最多等待的有效攻击次数，达到后必定触发。")
                .defineInRange("curse7ReversedJudgmentMaxAttacks", 7, 1, 100);
        CURSE_7_REVERSED_JUDGMENT_EXTRA_DAMAGE_RATIO = BUILDER
                .comment("[诅咒七-反转] 审判追加的基础伤害比例。1.0 表示追加 100% 基础伤害。")
                .defineInRange("curse7ReversedJudgmentExtraDamageRatio", 1.0D, 0.0D, 20.0D);
        CURSE_7_REVERSED_JUDGMENT_MAX_HEALTH_RATIO = BUILDER
                .comment("[诅咒七-反转] 审判追加的目标最大生命值真实伤害比例。")
                .defineInRange("curse7ReversedJudgmentMaxHealthRatio", 0.12D, 0.0D, 1.0D);
        CURSE_7_REVERSED_JUDGMENT_EXECUTE_THRESHOLD = BUILDER
                .comment("[诅咒七-反转] 普通目标低于该生命比例时可被审判处决。")
                .defineInRange("curse7ReversedJudgmentExecuteThreshold", 0.35D, 0.0D, 1.0D);
        CURSE_7_REVERSED_JUDGMENT_BOSS_DAMAGE_RATIO = BUILDER
                .comment("[诅咒七-反转] Boss 目标无法被直接处决时追加的最大生命值伤害比例。")
                .defineInRange("curse7ReversedJudgmentBossDamageRatio", 0.10D, 0.0D, 1.0D);
        CURSE_7_REVERSED_JUDGMENT_CHAIN_RADIUS = BUILDER
                .comment("[诅咒七-反转] 审判连锁搜索半径，单位为格。")
                .defineInRange("curse7ReversedJudgmentChainRadius", 7.0D, 0.0D, 64.0D);
        CURSE_7_REVERSED_JUDGMENT_CHAIN_COUNT = BUILDER
                .comment("[诅咒七-反转] 每次审判最多额外影响的连锁目标数量。")
                .defineInRange("curse7ReversedJudgmentChainCount", 7, 0, 64);
        CURSE_7_REVERSED_JUDGMENT_CHAIN_DAMAGE_RATIO = BUILDER
                .comment("[诅咒七-反转] 连锁审判相对于主目标审判伤害的比例。")
                .defineInRange("curse7ReversedJudgmentChainDamageRatio", 0.35D, 0.0D, 1.0D);
        CURSE_7_REVERSED_JUDGMENT_MARK_DURATION = BUILDER
                .comment("[诅咒七-反转] 审判标记持续时间，单位为 tick。")
                .defineInRange("curse7ReversedJudgmentMarkDuration", 140, 1, 72000);
        CURSE_7_REVERSED_MARK_VFX_ENABLED = BUILDER
                .comment("[诅咒七-反转] 是否在被标记敌人头顶显示程序生成的红色箭头特效。")
                .define("curse7ReversedMarkVfxEnabled", true);

        CURSE_7_REVERSED_ORDER_ENABLED = BUILDER
                .comment("[诅咒七-反转] 是否启用审判击杀累计后的随机王令。")
                .define("curse7ReversedOrderEnabled", true);
        CURSE_7_REVERSED_ORDER_KILL_THRESHOLD = BUILDER
                .comment("[诅咒七-反转] 累计多少个被标记目标死亡后触发一次随机王令。")
                .defineInRange("curse7ReversedOrderKillThreshold", 7, 1, 1000);
        CURSE_7_REVERSED_ORDER_DURATION = BUILDER
                .comment("[诅咒七-反转] 随机王令持续时间，单位为 tick。")
                .defineInRange("curse7ReversedOrderDuration", 140, 1, 72000);
        CURSE_7_REVERSED_ORDER_RANGE = BUILDER
                .comment("[诅咒七-反转] 王令影响范围，单位为格。")
                .defineInRange("curse7ReversedOrderRange", 7.0D, 0.0D, 64.0D);
        CURSE_7_REVERSED_ORDER_PUNISHMENT_RATIO = BUILDER
                .comment("[诅咒七-反转] 惩戒王令对普通目标造成的最大生命值伤害比例。")
                .defineInRange("curse7ReversedOrderPunishmentRatio", 0.35D, 0.0D, 1.0D);
        CURSE_7_REVERSED_ORDER_BOSS_DAMAGE_RATIO = BUILDER
                .comment("[诅咒七-反转] 惩戒王令对 Boss 目标造成的最大生命值伤害比例。")
                .defineInRange("curse7ReversedOrderBossDamageRatio", 0.12D, 0.0D, 1.0D);
        CURSE_7_REVERSED_ORDER_LIGHTNING_COUNT = BUILDER
                .comment("[诅咒七-反转] 雷霆王令在持续期间最多召唤的闪电次数。")
                .defineInRange("curse7ReversedOrderLightningCount", 7, 0, 100);
        CURSE_7_REVERSED_ORDER_BOUNTY_KILLS = BUILDER
                .comment("[诅咒七-反转] 赏赐王令期间可获得额外战利品判定的击杀次数。")
                .defineInRange("curse7ReversedOrderBountyKills", 7, 0, 1000);
        CURSE_7_REVERSED_ORDER_ABSORPTION = BUILDER
                .comment("[诅咒七-反转] 护佑王令给予的吸收生命值点数。")
                .defineInRange("curse7ReversedOrderAbsorption", 7.0D, 0.0D, 1024.0D);
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

        BUILDER.push("机会骰子6面体");
        CHANCE_DIE_ENABLED = BUILDER.comment("是否启用机会骰子6面体的使用与祝福效果。")
                .define("enabled", true);
        CHANCE_DIE_OUTCOME_WEIGHTS = BUILDER.comment("六个面的相对权重，必须依次提供6个非负整数。")
                .defineList("outcomeWeights", List.of(195, 195, 117, 181, 234, 78), Config::validateNonNegativeInteger);
        CHANCE_DIE_EFFECT_DURATION_SECONDS = BUILDER.comment("第2面追加伤害效果持续时间，单位为秒。")
                .defineInRange("timedEffectDurationSeconds", 60, 1, 86400);
        CHANCE_DIE_ATTACK_SPEED_BONUS = BUILDER.comment("第1面攻击速度加成。0.5 表示提高50%。")
                .defineInRange("attackSpeedBonus", 0.5D, 0.0D, 100.0D);
        CHANCE_DIE_HEALTH_DAMAGE_RATIO = BUILDER.comment("第2面追加玩家最大生命值伤害比例。0.35 表示35%。")
                .defineInRange("healthDamageRatio", 0.35D, 0.0D, 100.0D);
        CHANCE_DIE_FALL_IMMUNITY_ENABLED = BUILDER.comment("第3面是否给予永久摔落伤害免疫。")
                .define("fallImmunityEnabled", true);
        CHANCE_DIE_UNDYING_CHARGES_PER_ROLL = BUILDER.comment("第4面每次抽中时增加的不死图腾复活次数。")
                .defineInRange("undyingChargesPerRoll", 1, 0, 1000000);
        CHANCE_DIE_UNDYING_MAX_CHARGES = BUILDER.comment("第4面最多能够储存的复活次数。0 表示不限制。")
                .defineInRange("undyingMaxCharges", 0, 0, 1000000);
        CHANCE_DIE_UNDYING_REVIVE_HEALTH = BUILDER.comment("第4面消耗复活次数后恢复的生命值点数。")
                .defineInRange("undyingReviveHealth", 1.0D, 0.1D, 1000000.0D);
        CHANCE_DIE_DODGE_CHANCE = BUILDER.comment("第5面闪避概率。0.25 表示25%。")
                .defineInRange("dodgeChance", 0.25D, 0.0D, 1.0D);
        CHANCE_DIE_TRUE_DAMAGE_DURATION_SECONDS = BUILDER.comment("第6面真实伤害效果持续时间，单位为秒。")
                .defineInRange("trueDamageDurationSeconds", 60, 1, 86400);
        CHANCE_DIE_TRUE_DAMAGE_MULTIPLIER = BUILDER.comment("第6面转换后的真实伤害倍率。1.0 表示100%原伤害。")
                .defineInRange("trueDamageMultiplier", 1.0D, 0.0D, 100.0D);
        BUILDER.pop();

        BUILDER.push("大型机会骰子");
        LARGE_CHANCE_DIE_ENABLED = BUILDER.comment("是否允许使用大型机会骰子。")
                .define("enabled", true);
        LARGE_CHANCE_DIE_UNLOCK_SECONDS = BUILDER.comment("抽奖动画结束后允许选择结果的等待时间，单位为秒。")
                .defineInRange("selectionUnlockSeconds", 8.25D, 0.0D, 60.0D);
        BUILDER.pop();

        BUILDER.push("永寂玄金图腾");
        ETERNAL_SILENCE_TOTEM_ENABLED = BUILDER.comment("是否启用永寂玄金图腾。")
                .define("enabled", true);
        ETERNAL_SILENCE_TOTEM_DURABILITY_PER_USE = BUILDER.comment("耐久图腾每次复活消耗的耐久。")
                .defineInRange("durabilityPerUse", 6, 1, 30);
        ETERNAL_SILENCE_TOTEM_INVULNERABLE_SECONDS = BUILDER.comment("复活后的无敌时间，单位为秒。")
                .defineInRange("invulnerableSeconds", 30, 0, 3600);
        ETERNAL_SILENCE_TOTEM_SURGE_SECONDS = BUILDER.comment("复活后攻速与伤害增益持续时间，单位为秒。")
                .defineInRange("surgeSeconds", 15, 0, 3600);
        ETERNAL_SILENCE_TOTEM_MAX_HEALTH_PENALTY = BUILDER.comment("佩戴时减少的最大生命值点数。")
                .defineInRange("maxHealthPenalty", 5.0D, 0.0D, 1000.0D);
        ETERNAL_SILENCE_TOTEM_EQUIPPED_DAMAGE_BONUS = BUILDER.comment("佩戴时伤害加成。0.5 表示提高50%。")
                .defineInRange("equippedDamageBonus", 0.5D, 0.0D, 100.0D);
        ETERNAL_SILENCE_TOTEM_SURGE_ATTACK_SPEED_BONUS = BUILDER.comment("复活增益的攻击速度加成。1.0 表示提高100%。")
                .defineInRange("surgeAttackSpeedBonus", 1.0D, 0.0D, 100.0D);
        ETERNAL_SILENCE_TOTEM_SURGE_DAMAGE_BONUS = BUILDER.comment("复活增益的攻击伤害加成。0.5 表示提高50%。")
                .defineInRange("surgeDamageBonus", 0.5D, 0.0D, 100.0D);
        ETERNAL_SILENCE_TOTEM_SECOND_HIT_MULTIPLIER = BUILDER.comment("近战第二段攻击相对原伤害的倍率。1.0 表示完整复制一次伤害。")
                .defineInRange("secondHitDamageMultiplier", 1.0D, 0.0D, 100.0D);
        BUILDER.pop();

        BUILDER.push("永寂玄金轮回图腾");
        ETERNAL_SILENCE_REBIRTH_TOTEM_ENABLED = BUILDER.comment("是否启用永寂玄金轮回图腾。")
                .define("enabled", true);
        ETERNAL_SILENCE_REBIRTH_TOTEM_COOLDOWN_SECONDS = BUILDER.comment("轮回图腾触发后的冷却时间，单位为秒。")
                .defineInRange("cooldownSeconds", 90, 0, 86400);
        BUILDER.pop();

        BUILDER.push("裂变恶灵之心");
        FISSION_WRAITH_HEART_ENABLED = BUILDER.comment("是否启用裂变恶灵之心掉落与诅咒七反转仪式。")
                .define("enabled", true);
        FISSION_WRAITH_HEART_DROP_CHANCE = BUILDER.comment("监守者音爆击杀凋灵时的掉落概率。1.0 表示100%。")
                .defineInRange("dropChance", 1.0D, 0.0D, 1.0D);
        FISSION_WRAITH_HEART_RITUAL_SECONDS = BUILDER.comment("末地水晶汲取仪式持续时间，单位为秒。")
                .defineInRange("ritualSeconds", 15, 1, 3600);
        BUILDER.pop();

        BUILDER.push("暮晶戒指");
        TWILIGHT_CRYSTAL_RING_ENABLED = BUILDER.comment("是否启用暮晶戒指的宝箱掉落与佩戴效果。")
                .define("enabled", true);
        TWILIGHT_CRYSTAL_RING_LOOT_CHANCE = BUILDER.comment("全维度战利品宝箱出现概率。0.02 表示2%。")
                .defineInRange("lootChance", 0.02D, 0.0D, 1.0D);
        TWILIGHT_CRYSTAL_RING_MOVEMENT_SPEED_BONUS = BUILDER.comment("佩戴时移动速度加成。0.05 表示5%。")
                .defineInRange("movementSpeedBonus", 0.05D, 0.0D, 100.0D);
        TWILIGHT_CRYSTAL_RING_ATTACK_SPEED_BONUS = BUILDER.comment("佩戴时攻击速度加成。0.08 表示8%。")
                .defineInRange("attackSpeedBonus", 0.08D, 0.0D, 100.0D);
        TWILIGHT_CRYSTAL_RING_COMBO_HITS = BUILDER.comment("触发额外伤害所需的连续近战命中次数。")
                .defineInRange("comboHits", 3, 1, 1000);
        TWILIGHT_CRYSTAL_RING_COMBO_WINDOW_SECONDS = BUILDER.comment("连击保持时间，单位为秒。")
                .defineInRange("comboWindowSeconds", 4.0D, 0.05D, 3600.0D);
        TWILIGHT_CRYSTAL_RING_BONUS_DAMAGE = BUILDER.comment("完成连击后追加的魔法伤害。")
                .defineInRange("bonusMagicDamage", 2.0D, 0.0D, 1000000.0D);
        BUILDER.pop();

        BUILDER.push("缄声护符");
        HUSHED_DEPTHS_AMULET_ENABLED = BUILDER.comment("是否启用缄声护符对尖啸体和监守者的保护。")
                .define("enabled", true);
        BUILDER.pop();

        BUILDER.push("破灭");
        RUIN_TALISMAN_ENABLED = BUILDER.comment("是否启用破灭护符。")
                .define("enabled", true);
        RUIN_DAMAGE_THRESHOLD_RATIO = BUILDER.comment("获得1点计数所需的累计伤害占自身最大生命值比例。1.0 表示100%。")
                .defineInRange("counterDamageThresholdRatio", 1.0D, 0.001D, 1000.0D);
        RUIN_COUNTER_COOLDOWN_SECONDS = BUILDER.comment("获得计数的内置冷却，单位为秒。")
                .defineInRange("counterCooldownSeconds", 0.5D, 0.0D, 3600.0D);
        RUIN_ATTACKS_PER_TRIGGER = BUILDER.comment("触发追加伤害所需攻击次数。")
                .defineInRange("attacksPerTrigger", 3, 1, 1000);
        RUIN_ATTACK_COUNTER_COST = BUILDER.comment("每次追加伤害消耗的计数。")
                .defineInRange("attackCounterCost", 1, 0, 1000000);
        RUIN_ATTACK_DAMAGE_RATIO = BUILDER.comment("追加虚空伤害占目标最大生命值比例。0.01 表示1%。")
                .defineInRange("attackDamageRatio", 0.01D, 0.0D, 100.0D);
        RUIN_REVIVE_COUNTER_COST = BUILDER.comment("满血复活所需计数。")
                .defineInRange("reviveCounterCost", 10, 0, 1000000);
        RUIN_REVIVE_BURST_DAMAGE_RATIO = BUILDER.comment("复活冲击伤害占目标最大生命值比例。0.05 表示5%。")
                .defineInRange("reviveBurstDamageRatio", 0.05D, 0.0D, 100.0D);
        RUIN_REVIVE_BURST_RADIUS = BUILDER.comment("复活冲击的水平半径，单位为方块。")
                .defineInRange("reviveBurstRadius", 3.5D, 0.0D, 128.0D);
        BUILDER.pop();

        BUILDER.push("永寂复合弓");
        ETERNAL_SILENCE_COMPOUND_BOW_ENABLED = BUILDER.comment("是否启用永寂复合弓的特殊射击逻辑。")
                .define("enabled", true);
        ETERNAL_SILENCE_COMPOUND_BOW_RAPID_DURATION_SECONDS = BUILDER.comment("连射模式最长持续时间，单位为秒。")
                .defineInRange("rapidFireDurationSeconds", 5.0D, 0.05D, 3600.0D);
        ETERNAL_SILENCE_COMPOUND_BOW_RAPID_INTERVAL_TICKS = BUILDER.comment("连射发射间隔，单位为 tick。")
                .defineInRange("rapidFireIntervalTicks", 2, 1, 1200);
        ETERNAL_SILENCE_COMPOUND_BOW_COOLDOWN_CHANCE = BUILDER.comment("连射结束后进入冷却的概率。0.25 表示25%。")
                .defineInRange("rapidFireCooldownChance", 0.25D, 0.0D, 1.0D);
        ETERNAL_SILENCE_COMPOUND_BOW_COOLDOWN_SECONDS = BUILDER.comment("触发连射冷却时的持续时间，单位为秒。")
                .defineInRange("rapidFireCooldownSeconds", 5.0D, 0.0D, 3600.0D);
        ETERNAL_SILENCE_COMPOUND_BOW_PROJECTILES = BUILDER.comment("普通蓄力发射时生成的弹种数量。建议使用奇数。")
                .defineInRange("projectilesPerShot", 3, 1, 101);
        ETERNAL_SILENCE_COMPOUND_BOW_SPREAD = BUILDER.comment("多弹种发射时相邻弹道的水平偏移。")
                .defineInRange("projectileSpread", 0.10D, 0.0D, 10.0D);
        ETERNAL_SILENCE_COMPOUND_BOW_DRAGON_BREATH_DAMAGE = BUILDER.comment("龙息弹种的基础伤害参数。")
                .defineInRange("dragonBreathDamage", 20.0D, 0.0D, 1000000.0D);
        ETERNAL_SILENCE_COMPOUND_BOW_REVERSED_DAMAGE_RATIO = BUILDER.comment("诅咒七反转后追加真实伤害比例。0.5 表示原伤害的50%。")
                .defineInRange("reversedCurseDamageRatio", 0.5D, 0.0D, 100.0D);
        BUILDER.pop();

        BUILDER.push("穿甲箭");
        ARMOR_PIERCING_ARROW_ENABLED = BUILDER.comment("是否启用穿甲箭及其破除王胄护盾的能力。")
                .define("enabled", true);
        ARMOR_PIERCING_ARROW_BREAKS_DYNASTY_SHIELD = BUILDER.comment("穿甲箭命中时是否直接破除王胄护盾。")
                .define("breaksDynastyShield", true);
        BUILDER.pop();

        BUILDER.push("两地敕印");
        EDICT_OF_TWO_LANDS_ENABLED = BUILDER.comment("是否启用两地敕印的佩戴与技能效果。")
                .define("enabled", true);
        EDICT_OF_TWO_LANDS_CAST_GUILT_COST = BUILDER.comment("X 技能消耗的王罪值。")
                .defineInRange("castGuiltCost", 20, 0, 1000000);
        EDICT_OF_TWO_LANDS_DOMAIN_GUILT_COST = BUILDER.comment("Y 技能消耗的王罪值。")
                .defineInRange("domainGuiltCost", 30, 0, 1000000);
        EDICT_OF_TWO_LANDS_CAST_COOLDOWN_SECONDS = BUILDER.comment("X 技能冷却时间，单位为秒。")
                .defineInRange("castCooldownSeconds", 90.0D, 0.0D, 86400.0D);
        EDICT_OF_TWO_LANDS_DOMAIN_COOLDOWN_SECONDS = BUILDER.comment("Y 技能冷却时间，单位为秒。")
                .defineInRange("domainCooldownSeconds", 360.0D, 0.0D, 86400.0D);
        EDICT_OF_TWO_LANDS_CHARGE_SECONDS = BUILDER.comment("X、Y 技能长按凝聚时间，单位为秒。")
                .defineInRange("chargeSeconds", 3.0D, 0.05D, 60.0D);
        EDICT_OF_TWO_LANDS_CAST_RANGE = BUILDER.comment("X 技能搜索敌对实体的半径，单位为方块。")
                .defineInRange("castRange", 12.0D, 1.0D, 256.0D);
        EDICT_OF_TWO_LANDS_DOMAIN_RANGE = BUILDER.comment("Y 技能矩阵半径，单位为方块。")
                .defineInRange("domainRange", 15.0D, 1.0D, 256.0D);
        EDICT_OF_TWO_LANDS_LIFT_SECONDS = BUILDER.comment("X 技能将目标掀起的时间，单位为秒。")
                .defineInRange("liftSeconds", 0.6D, 0.05D, 60.0D);
        EDICT_OF_TWO_LANDS_SEAL_SECONDS = BUILDER.comment("X 技能将目标悬空封印的时间，单位为秒。")
                .defineInRange("sealSeconds", 10.0D, 0.05D, 3600.0D);
        EDICT_OF_TWO_LANDS_AIR_DAMAGE_INTERVAL_SECONDS = BUILDER.comment("X 技能悬空期间的伤害间隔，单位为秒。")
                .defineInRange("airDamageIntervalSeconds", 2.0D, 0.05D, 3600.0D);
        EDICT_OF_TWO_LANDS_AIR_DAMAGE = BUILDER.comment("X 技能悬空期间每次造成的魔法伤害。")
                .defineInRange("airDamage", 20.0D, 0.0D, 1000000.0D);
        EDICT_OF_TWO_LANDS_CHAIN_DAMAGE = BUILDER.comment("X 技能目标之间连锁时造成的魔法伤害。")
                .defineInRange("chainDamage", 10.0D, 0.0D, 1000000.0D);
        EDICT_OF_TWO_LANDS_CHAIN_PULL_STRENGTH = BUILDER.comment("X 技能连锁拉扯强度。")
                .defineInRange("chainPullStrength", 0.18D, 0.0D, 10.0D);
        EDICT_OF_TWO_LANDS_DOMAIN_DURATION_SECONDS = BUILDER.comment("Y 技能矩阵持续时间，单位为秒；结束时执行处决。")
                .defineInRange("domainDurationSeconds", 10.0D, 0.05D, 3600.0D);
        EDICT_OF_TWO_LANDS_DOMAIN_PULL_STRENGTH = BUILDER.comment("Y 技能矩阵向中心收束目标的强度。")
                .defineInRange("domainPullStrength", 0.18D, 0.0D, 10.0D);
        BUILDER.pop();

        BUILDER.push("万世征途");
        ETERNAL_CONQUEST_ENABLED = BUILDER.comment("是否启用万世征途及其成长效果。")
                .define("enabled", true);
        ETERNAL_CONQUEST_KILLS_PER_LEVEL = BUILDER.comment("每提升1层淬炼所需亲手击杀的9级王胄护盾实体数量。")
                .defineInRange("killsPerLevel", 5, 1, 1000000);
        ETERNAL_CONQUEST_ATTACK_DAMAGE_PER_LEVEL = BUILDER.comment("每层提供的攻击伤害加成。0.01表示1%。")
                .defineInRange("attackDamagePerLevel", 0.01D, 0.0D, 100.0D);
        ETERNAL_CONQUEST_REFLECTION_PER_LEVEL = BUILDER.comment("每层提供的伤害反弹比例。0.01表示1%。")
                .defineInRange("reflectionPerLevel", 0.01D, 0.0D, 1.0D);
        ETERNAL_CONQUEST_REFLECTION_MAX = BUILDER.comment("伤害反弹率上限。0.75表示75%。")
                .defineInRange("reflectionMax", 0.75D, 0.0D, 1.0D);
        ETERNAL_CONQUEST_LIFESTEAL_PER_LEVEL = BUILDER.comment("每层提供的吸血比例。0.01表示1%。")
                .defineInRange("lifestealPerLevel", 0.01D, 0.0D, 1.0D);
        ETERNAL_CONQUEST_LIFESTEAL_MAX = BUILDER.comment("吸血率上限。0.90表示90%。")
                .defineInRange("lifestealMax", 0.90D, 0.0D, 1.0D);
        BUILDER.pop();

        BUILDER.pop();

        BUILDER.push("通用");
        DYNASTY_SHIELD_ENABLED = BUILDER
                .comment("[通用] 为 true 时启用王朝护盾；为 false 时敌对生物不会获得护盾，已有护盾也不会生效。")
                .define("dynastyShieldEnabled", true);

        DYNASTY_SHIELD_FORCE_ENABLED = BUILDER
                .comment("[通用] 强制开启王胄。默认 false 时，仅在附近存在佩戴卡迭石咒印的玩家时启用；/ramessescurse dynasty_shield on 会将其设为 true。")
                .define("dynastyShieldForceEnabled", false);

        DYNASTY_SHIELD_LEVELS = BUILDER
                .comment("[通用] 王朝护盾可用等级列表。需要与 dynastyShieldMaxValues 一一对应。")
                .defineList("dynastyShieldLevels", List.of(1, 3, 5, 7, 9), Config::validatePositiveInteger);

        DYNASTY_SHIELD_MAX_VALUES = BUILDER
                .comment("[通用] 王朝护盾每个等级对应的最大护盾值。")
                .defineList("dynastyShieldMaxValues", List.of(120, 160, 220, 300, 400), Config::validatePositiveInteger);

        DYNASTY_SHIELD_THREAT_THRESHOLDS = BUILDER
                .comment("[通用] 每个王胄等级所需的最低有效王朝威胁值。需要与 dynastyShieldLevels 一一对应。")
                .defineList("dynastyShieldThreatThresholds", List.of(0, 1000, 2500, 4500, 7000), Config::validateNonNegativeInteger);

        BUILDER.push("词条材料掉落");
        CALMING_TIBIA_DROP_ENABLED = BUILDER
                .comment("[词条材料掉落] 是否允许狂怒词条掉落镇定胫骨。")
                .define("calmingTibiaDropEnabled", true);
        PITCHFIRE_CRYSTAL_DROP_ENABLED = BUILDER
                .comment("[词条材料掉落] 是否允许熔融词条掉落沥火凝晶。")
                .define("pitchfireCrystalDropEnabled", true);
        HEART_DEVOURING_REMNANT_DROP_ENABLED = BUILDER
                .comment("[词条材料掉落] 是否允许弃血词条掉落噬心残印。")
                .define("heartDevouringRemnantDropEnabled", true);
        RAVENOUS_CLAW_FRAGMENT_DROP_ENABLED = BUILDER
                .comment("[词条材料掉落] 是否允许嗜爪之怒词条掉落嗜爪碎片。")
                .define("ravenousClawFragmentDropEnabled", true);
        FAMINE_CORE_DROP_ENABLED = BUILDER
                .comment("[词条材料掉落] 是否允许血肉盛宴词条掉落饥荒之核。")
                .define("famineCoreDropEnabled", true);
        VENOM_ORB_DROP_ENABLED = BUILDER
                .comment("[词条材料掉落] 是否允许瘴气词条掉落剧毒宝珠。")
                .define("venomOrbDropEnabled", true);
        KA_REMNANT_BREATH_DROP_ENABLED = BUILDER
                .comment("[词条材料掉落] 是否允许复原词条掉落卡之残息。")
                .define("kaRemnantBreathDropEnabled", true);
        LIFE_BINDING_REMNANT_CRYSTAL_DROP_ENABLED = BUILDER
                .comment("[词条材料掉落] 是否允许血限桎梏词条掉落缚命残晶。")
                .define("lifeBindingRemnantCrystalDropEnabled", true);
        AUTHORITY_FRAGMENT_DROP_ENABLED = BUILDER
                .comment("[词条材料掉落] 是否允许死亡权柄词条掉落权柄碎片。")
                .define("authorityFragmentDropEnabled", true);
        TRUTH_DROP_ENABLED = BUILDER
                .comment("[词条材料掉落] 是否允许易伤词条掉落真理。")
                .define("truthDropEnabled", true);
        REVERSE_SCALE_DROP_ENABLED = BUILDER
                .comment("[词条材料掉落] 是否允许反制词条掉落逆鳞片。")
                .define("reverseScaleDropEnabled", true);

        CALMING_TIBIA_DROP_CHANCE = BUILDER
                .comment("[词条材料掉落] 镇定胫骨掉落概率。0.002 表示 0.2%。")
                .defineInRange("calmingTibiaDropChance", 0.002D, 0.0D, 1.0D);
        PITCHFIRE_CRYSTAL_DROP_CHANCE = BUILDER
                .comment("[词条材料掉落] 沥火凝晶掉落概率。0.002 表示 0.2%。")
                .defineInRange("pitchfireCrystalDropChance", 0.002D, 0.0D, 1.0D);
        HEART_DEVOURING_REMNANT_DROP_CHANCE = BUILDER
                .comment("[词条材料掉落] 噬心残印掉落概率。0.002 表示 0.2%。")
                .defineInRange("heartDevouringRemnantDropChance", 0.002D, 0.0D, 1.0D);
        RAVENOUS_CLAW_FRAGMENT_DROP_CHANCE = BUILDER
                .comment("[词条材料掉落] 嗜爪碎片掉落概率。0.002 表示 0.2%。")
                .defineInRange("ravenousClawFragmentDropChance", 0.002D, 0.0D, 1.0D);
        FAMINE_CORE_DROP_CHANCE = BUILDER
                .comment("[词条材料掉落] 饥荒之核掉落概率。0.002 表示 0.2%。")
                .defineInRange("famineCoreDropChance", 0.002D, 0.0D, 1.0D);
        VENOM_ORB_DROP_CHANCE = BUILDER
                .comment("[词条材料掉落] 剧毒宝珠掉落概率。0.002 表示 0.2%。")
                .defineInRange("venomOrbDropChance", 0.002D, 0.0D, 1.0D);
        KA_REMNANT_BREATH_DROP_CHANCE = BUILDER
                .comment("[词条材料掉落] 卡之残息掉落概率。0.002 表示 0.2%。")
                .defineInRange("kaRemnantBreathDropChance", 0.002D, 0.0D, 1.0D);
        LIFE_BINDING_REMNANT_CRYSTAL_DROP_CHANCE = BUILDER
                .comment("[词条材料掉落] 缚命残晶掉落概率。0.002 表示 0.2%。")
                .defineInRange("lifeBindingRemnantCrystalDropChance", 0.002D, 0.0D, 1.0D);
        AUTHORITY_FRAGMENT_DROP_CHANCE = BUILDER
                .comment("[词条材料掉落] 权柄碎片掉落概率。0.002 表示 0.2%。")
                .defineInRange("authorityFragmentDropChance", 0.002D, 0.0D, 1.0D);
        TRUTH_DROP_CHANCE = BUILDER
                .comment("[词条材料掉落] 真理掉落概率。0.002 表示 0.2%。")
                .defineInRange("truthDropChance", 0.002D, 0.0D, 1.0D);
        REVERSE_SCALE_DROP_CHANCE = BUILDER
                .comment("[词条材料掉落] 逆鳞片掉落概率。0.002 表示 0.2%。")
                .defineInRange("reverseScaleDropChance", 0.002D, 0.0D, 1.0D);
        BUILDER.pop();

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
    public static final ForgeConfigSpec.DoubleValue CURSE_2_LUCK_PENALTY;
    public static final ForgeConfigSpec.BooleanValue CURSE_2_VILLAGER_ZOMBIFICATION_ENABLED;
    public static final ForgeConfigSpec.IntValue CURSE_2_VILLAGER_ZOMBIFICATION_RADIUS;
    public static final ForgeConfigSpec.DoubleValue CURSE_2_REVERSED_LUCK_BONUS;
    public static final ForgeConfigSpec.IntValue CURSE_2_REVERSED_SPAWN_INTERVAL;
    public static final ForgeConfigSpec.DoubleValue CURSE_2_REVERSED_SPAWN_CHANCE;
    public static final ForgeConfigSpec.IntValue CURSE_2_REVERSED_SPAWN_RADIUS;
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
    public static final ForgeConfigSpec.IntValue CURSE_7_LOOT_TRIGGER_MAX_ATTACKS;
    public static final ForgeConfigSpec.BooleanValue CURSE_7_ENCHANTMENT_ERASURE_ENABLED;
    public static final ForgeConfigSpec.DoubleValue CURSE_7_ENCHANTMENT_ERASURE_CHANCE;
    public static final ForgeConfigSpec.DoubleValue CURSE_7_REVERSED_ATTACK_DAMAGE_BONUS;
    public static final ForgeConfigSpec.DoubleValue CURSE_7_REVERSED_ATTACK_SPEED_BONUS;
    public static final ForgeConfigSpec.DoubleValue CURSE_7_REVERSED_MOVEMENT_SPEED_BONUS;
    public static final ForgeConfigSpec.DoubleValue CURSE_7_REVERSED_MAX_HEALTH_BONUS;
    public static final ForgeConfigSpec.DoubleValue CURSE_7_REVERSED_ARMOR_TOUGHNESS_BONUS;
    public static final ForgeConfigSpec.DoubleValue CURSE_7_REVERSED_END_DAMAGE_REDUCTION;
    public static final ForgeConfigSpec.DoubleValue CURSE_7_REVERSED_END_XP_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue CURSE_7_REVERSED_JUDGMENT_ENABLED;
    public static final ForgeConfigSpec.IntValue CURSE_7_REVERSED_JUDGMENT_MAX_ATTACKS;
    public static final ForgeConfigSpec.DoubleValue CURSE_7_REVERSED_JUDGMENT_EXTRA_DAMAGE_RATIO;
    public static final ForgeConfigSpec.DoubleValue CURSE_7_REVERSED_JUDGMENT_MAX_HEALTH_RATIO;
    public static final ForgeConfigSpec.DoubleValue CURSE_7_REVERSED_JUDGMENT_EXECUTE_THRESHOLD;
    public static final ForgeConfigSpec.DoubleValue CURSE_7_REVERSED_JUDGMENT_BOSS_DAMAGE_RATIO;
    public static final ForgeConfigSpec.DoubleValue CURSE_7_REVERSED_JUDGMENT_CHAIN_RADIUS;
    public static final ForgeConfigSpec.IntValue CURSE_7_REVERSED_JUDGMENT_CHAIN_COUNT;
    public static final ForgeConfigSpec.DoubleValue CURSE_7_REVERSED_JUDGMENT_CHAIN_DAMAGE_RATIO;
    public static final ForgeConfigSpec.IntValue CURSE_7_REVERSED_JUDGMENT_MARK_DURATION;
    public static final ForgeConfigSpec.BooleanValue CURSE_7_REVERSED_MARK_VFX_ENABLED;
    public static final ForgeConfigSpec.BooleanValue CURSE_7_REVERSED_ORDER_ENABLED;
    public static final ForgeConfigSpec.IntValue CURSE_7_REVERSED_ORDER_KILL_THRESHOLD;
    public static final ForgeConfigSpec.IntValue CURSE_7_REVERSED_ORDER_DURATION;
    public static final ForgeConfigSpec.DoubleValue CURSE_7_REVERSED_ORDER_RANGE;
    public static final ForgeConfigSpec.DoubleValue CURSE_7_REVERSED_ORDER_PUNISHMENT_RATIO;
    public static final ForgeConfigSpec.DoubleValue CURSE_7_REVERSED_ORDER_BOSS_DAMAGE_RATIO;
    public static final ForgeConfigSpec.IntValue CURSE_7_REVERSED_ORDER_LIGHTNING_COUNT;
    public static final ForgeConfigSpec.IntValue CURSE_7_REVERSED_ORDER_BOUNTY_KILLS;
    public static final ForgeConfigSpec.DoubleValue CURSE_7_REVERSED_ORDER_ABSORPTION;
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
    public static final ForgeConfigSpec.BooleanValue DYNASTY_SHIELD_FORCE_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> DYNASTY_SHIELD_LEVELS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> DYNASTY_SHIELD_MAX_VALUES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> DYNASTY_SHIELD_THREAT_THRESHOLDS;
    public static final ForgeConfigSpec.BooleanValue CALMING_TIBIA_DROP_ENABLED;
    public static final ForgeConfigSpec.BooleanValue PITCHFIRE_CRYSTAL_DROP_ENABLED;
    public static final ForgeConfigSpec.BooleanValue HEART_DEVOURING_REMNANT_DROP_ENABLED;
    public static final ForgeConfigSpec.BooleanValue RAVENOUS_CLAW_FRAGMENT_DROP_ENABLED;
    public static final ForgeConfigSpec.BooleanValue FAMINE_CORE_DROP_ENABLED;
    public static final ForgeConfigSpec.BooleanValue VENOM_ORB_DROP_ENABLED;
    public static final ForgeConfigSpec.BooleanValue KA_REMNANT_BREATH_DROP_ENABLED;
    public static final ForgeConfigSpec.BooleanValue LIFE_BINDING_REMNANT_CRYSTAL_DROP_ENABLED;
    public static final ForgeConfigSpec.BooleanValue AUTHORITY_FRAGMENT_DROP_ENABLED;
    public static final ForgeConfigSpec.BooleanValue TRUTH_DROP_ENABLED;
    public static final ForgeConfigSpec.BooleanValue REVERSE_SCALE_DROP_ENABLED;
    public static final ForgeConfigSpec.DoubleValue CALMING_TIBIA_DROP_CHANCE;
    public static final ForgeConfigSpec.DoubleValue PITCHFIRE_CRYSTAL_DROP_CHANCE;
    public static final ForgeConfigSpec.DoubleValue HEART_DEVOURING_REMNANT_DROP_CHANCE;
    public static final ForgeConfigSpec.DoubleValue RAVENOUS_CLAW_FRAGMENT_DROP_CHANCE;
    public static final ForgeConfigSpec.DoubleValue FAMINE_CORE_DROP_CHANCE;
    public static final ForgeConfigSpec.DoubleValue VENOM_ORB_DROP_CHANCE;
    public static final ForgeConfigSpec.DoubleValue KA_REMNANT_BREATH_DROP_CHANCE;
    public static final ForgeConfigSpec.DoubleValue LIFE_BINDING_REMNANT_CRYSTAL_DROP_CHANCE;
    public static final ForgeConfigSpec.DoubleValue AUTHORITY_FRAGMENT_DROP_CHANCE;
    public static final ForgeConfigSpec.DoubleValue TRUTH_DROP_CHANCE;
    public static final ForgeConfigSpec.DoubleValue REVERSE_SCALE_DROP_CHANCE;
    public static final ForgeConfigSpec.BooleanValue CHANCE_DIE_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> CHANCE_DIE_OUTCOME_WEIGHTS;
    public static final ForgeConfigSpec.IntValue CHANCE_DIE_EFFECT_DURATION_SECONDS;
    public static final ForgeConfigSpec.DoubleValue CHANCE_DIE_ATTACK_SPEED_BONUS;
    public static final ForgeConfigSpec.DoubleValue CHANCE_DIE_HEALTH_DAMAGE_RATIO;
    public static final ForgeConfigSpec.BooleanValue CHANCE_DIE_FALL_IMMUNITY_ENABLED;
    public static final ForgeConfigSpec.IntValue CHANCE_DIE_UNDYING_CHARGES_PER_ROLL;
    public static final ForgeConfigSpec.IntValue CHANCE_DIE_UNDYING_MAX_CHARGES;
    public static final ForgeConfigSpec.DoubleValue CHANCE_DIE_UNDYING_REVIVE_HEALTH;
    public static final ForgeConfigSpec.DoubleValue CHANCE_DIE_DODGE_CHANCE;
    public static final ForgeConfigSpec.IntValue CHANCE_DIE_TRUE_DAMAGE_DURATION_SECONDS;
    public static final ForgeConfigSpec.DoubleValue CHANCE_DIE_TRUE_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue LARGE_CHANCE_DIE_ENABLED;
    public static final ForgeConfigSpec.DoubleValue LARGE_CHANCE_DIE_UNLOCK_SECONDS;
    public static final ForgeConfigSpec.BooleanValue ETERNAL_SILENCE_TOTEM_ENABLED;
    public static final ForgeConfigSpec.IntValue ETERNAL_SILENCE_TOTEM_DURABILITY_PER_USE;
    public static final ForgeConfigSpec.IntValue ETERNAL_SILENCE_TOTEM_INVULNERABLE_SECONDS;
    public static final ForgeConfigSpec.IntValue ETERNAL_SILENCE_TOTEM_SURGE_SECONDS;
    public static final ForgeConfigSpec.DoubleValue ETERNAL_SILENCE_TOTEM_MAX_HEALTH_PENALTY;
    public static final ForgeConfigSpec.DoubleValue ETERNAL_SILENCE_TOTEM_EQUIPPED_DAMAGE_BONUS;
    public static final ForgeConfigSpec.DoubleValue ETERNAL_SILENCE_TOTEM_SURGE_ATTACK_SPEED_BONUS;
    public static final ForgeConfigSpec.DoubleValue ETERNAL_SILENCE_TOTEM_SURGE_DAMAGE_BONUS;
    public static final ForgeConfigSpec.DoubleValue ETERNAL_SILENCE_TOTEM_SECOND_HIT_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue ETERNAL_SILENCE_REBIRTH_TOTEM_ENABLED;
    public static final ForgeConfigSpec.IntValue ETERNAL_SILENCE_REBIRTH_TOTEM_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.BooleanValue FISSION_WRAITH_HEART_ENABLED;
    public static final ForgeConfigSpec.DoubleValue FISSION_WRAITH_HEART_DROP_CHANCE;
    public static final ForgeConfigSpec.IntValue FISSION_WRAITH_HEART_RITUAL_SECONDS;
    public static final ForgeConfigSpec.BooleanValue TWILIGHT_CRYSTAL_RING_ENABLED;
    public static final ForgeConfigSpec.DoubleValue TWILIGHT_CRYSTAL_RING_LOOT_CHANCE;
    public static final ForgeConfigSpec.DoubleValue TWILIGHT_CRYSTAL_RING_MOVEMENT_SPEED_BONUS;
    public static final ForgeConfigSpec.DoubleValue TWILIGHT_CRYSTAL_RING_ATTACK_SPEED_BONUS;
    public static final ForgeConfigSpec.IntValue TWILIGHT_CRYSTAL_RING_COMBO_HITS;
    public static final ForgeConfigSpec.DoubleValue TWILIGHT_CRYSTAL_RING_COMBO_WINDOW_SECONDS;
    public static final ForgeConfigSpec.DoubleValue TWILIGHT_CRYSTAL_RING_BONUS_DAMAGE;
    public static final ForgeConfigSpec.BooleanValue HUSHED_DEPTHS_AMULET_ENABLED;
    public static final ForgeConfigSpec.BooleanValue ETERNAL_CONQUEST_ENABLED;
    public static final ForgeConfigSpec.IntValue ETERNAL_CONQUEST_KILLS_PER_LEVEL;
    public static final ForgeConfigSpec.DoubleValue ETERNAL_CONQUEST_ATTACK_DAMAGE_PER_LEVEL;
    public static final ForgeConfigSpec.DoubleValue ETERNAL_CONQUEST_REFLECTION_PER_LEVEL;
    public static final ForgeConfigSpec.DoubleValue ETERNAL_CONQUEST_REFLECTION_MAX;
    public static final ForgeConfigSpec.DoubleValue ETERNAL_CONQUEST_LIFESTEAL_PER_LEVEL;
    public static final ForgeConfigSpec.DoubleValue ETERNAL_CONQUEST_LIFESTEAL_MAX;
    public static final ForgeConfigSpec.BooleanValue RUIN_TALISMAN_ENABLED;
    public static final ForgeConfigSpec.DoubleValue RUIN_DAMAGE_THRESHOLD_RATIO;
    public static final ForgeConfigSpec.DoubleValue RUIN_COUNTER_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.IntValue RUIN_ATTACKS_PER_TRIGGER;
    public static final ForgeConfigSpec.IntValue RUIN_ATTACK_COUNTER_COST;
    public static final ForgeConfigSpec.DoubleValue RUIN_ATTACK_DAMAGE_RATIO;
    public static final ForgeConfigSpec.IntValue RUIN_REVIVE_COUNTER_COST;
    public static final ForgeConfigSpec.DoubleValue RUIN_REVIVE_BURST_DAMAGE_RATIO;
    public static final ForgeConfigSpec.DoubleValue RUIN_REVIVE_BURST_RADIUS;
    public static final ForgeConfigSpec.BooleanValue ETERNAL_SILENCE_COMPOUND_BOW_ENABLED;
    public static final ForgeConfigSpec.DoubleValue ETERNAL_SILENCE_COMPOUND_BOW_RAPID_DURATION_SECONDS;
    public static final ForgeConfigSpec.IntValue ETERNAL_SILENCE_COMPOUND_BOW_RAPID_INTERVAL_TICKS;
    public static final ForgeConfigSpec.DoubleValue ETERNAL_SILENCE_COMPOUND_BOW_COOLDOWN_CHANCE;
    public static final ForgeConfigSpec.DoubleValue ETERNAL_SILENCE_COMPOUND_BOW_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.IntValue ETERNAL_SILENCE_COMPOUND_BOW_PROJECTILES;
    public static final ForgeConfigSpec.DoubleValue ETERNAL_SILENCE_COMPOUND_BOW_SPREAD;
    public static final ForgeConfigSpec.DoubleValue ETERNAL_SILENCE_COMPOUND_BOW_DRAGON_BREATH_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue ETERNAL_SILENCE_COMPOUND_BOW_REVERSED_DAMAGE_RATIO;
    public static final ForgeConfigSpec.BooleanValue ARMOR_PIERCING_ARROW_ENABLED;
    public static final ForgeConfigSpec.BooleanValue ARMOR_PIERCING_ARROW_BREAKS_DYNASTY_SHIELD;
    public static final ForgeConfigSpec.BooleanValue EDICT_OF_TWO_LANDS_ENABLED;
    public static final ForgeConfigSpec.IntValue EDICT_OF_TWO_LANDS_CAST_GUILT_COST;
    public static final ForgeConfigSpec.IntValue EDICT_OF_TWO_LANDS_DOMAIN_GUILT_COST;
    public static final ForgeConfigSpec.DoubleValue EDICT_OF_TWO_LANDS_CAST_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.DoubleValue EDICT_OF_TWO_LANDS_DOMAIN_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.DoubleValue EDICT_OF_TWO_LANDS_CHARGE_SECONDS;
    public static final ForgeConfigSpec.DoubleValue EDICT_OF_TWO_LANDS_CAST_RANGE;
    public static final ForgeConfigSpec.DoubleValue EDICT_OF_TWO_LANDS_DOMAIN_RANGE;
    public static final ForgeConfigSpec.DoubleValue EDICT_OF_TWO_LANDS_LIFT_SECONDS;
    public static final ForgeConfigSpec.DoubleValue EDICT_OF_TWO_LANDS_SEAL_SECONDS;
    public static final ForgeConfigSpec.DoubleValue EDICT_OF_TWO_LANDS_AIR_DAMAGE_INTERVAL_SECONDS;
    public static final ForgeConfigSpec.DoubleValue EDICT_OF_TWO_LANDS_AIR_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue EDICT_OF_TWO_LANDS_CHAIN_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue EDICT_OF_TWO_LANDS_CHAIN_PULL_STRENGTH;
    public static final ForgeConfigSpec.DoubleValue EDICT_OF_TWO_LANDS_DOMAIN_DURATION_SECONDS;
    public static final ForgeConfigSpec.DoubleValue EDICT_OF_TWO_LANDS_DOMAIN_PULL_STRENGTH;
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
