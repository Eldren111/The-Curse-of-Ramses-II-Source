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
        BUILDER.push("sealOfKadesh");

        RANDOM_CURSE_INTERVAL = BUILDER
                .comment("[Curse 1] Trigger interval in ticks for the random negative effect. 6000 ticks = 5 minutes.")
                .defineInRange("randomCurseInterval", 6000, 20, 72000);

        RANDOM_CURSE_DURATION = BUILDER
                .comment("[Curse 1] Duration in ticks of the random negative effect applied each time.")
                .defineInRange("randomCurseDuration", 160, 20, 72000);

        RANDOM_CURSE_AMPLIFIER_MIN = BUILDER
                .comment("[Curse 1] Minimum amplifier of the random negative effect.")
                .defineInRange("randomCurseAmplifierMin", 0, 0, 10);

        RANDOM_CURSE_AMPLIFIER_MAX = BUILDER
                .comment("[Curse 1] Maximum amplifier of the random negative effect.")
                .defineInRange("randomCurseAmplifierMax", 1, 0, 10);

        RANDOM_CURSE_NEGATIVE_POOL = BUILDER
                .comment("[Curse 1] Negative effect pool. Fill in effect ids here. Darkness, mining fatigue, and nausea are filtered out by code.")
                .defineListAllowEmpty("randomCurseNegativePool",
                        List.of("minecraft:weakness", "minecraft:slowness", "minecraft:hunger", "minecraft:blindness", "minecraft:poison", "minecraft:wither", "minecraft:unluck", "minecraft:levitation"),
                        Config::validateEffectName);

        CURSE_2_SPAWN_INTERVAL = BUILDER
                .comment("[Curse 2] Interval in ticks for the hostile mob summon check. 12000 ticks = 10 minutes.")
                .defineInRange("curse2SpawnInterval", 12000, 20, 720000);

        CURSE_2_SPAWN_CHANCE = BUILDER
                .comment("[Curse 2] Chance to summon a hostile mob whenever the curse 2 interval is reached.")
                .defineInRange("curse2SpawnChance", 0.15D, 0.0D, 1.0D);

        CURSE_2_LUCK_PENALTY = BUILDER
                .comment("[Curse 2] How much luck is removed while the Seal of Kadesh is worn.")
                .defineInRange("curse2LuckPenalty", 2.0D, 0.0D, 1024.0D);

        CURSE_2_SPAWN_RADIUS = BUILDER
                .comment("[Curse 2] Horizontal spawn radius around the player for summoned hostile mobs.")
                .defineInRange("curse2SpawnRadius", 8, 2, 64);

        CURSE_2_ENTITY_BLACKLIST = BUILDER
                .comment("[Curse 2] Hostile mob blacklist. Curse 2 scans all MONSTER entities automatically, then excludes any entity id listed here.")
                .defineListAllowEmpty("curse2EntityBlacklist", List.of(), Config::validateEntityTypeName);

        CURSE_3_XP_MULTIPLIER = BUILDER
                .comment("[Curse 3] Experience multiplier applied after killing any creature. 0.5 means the player only gets 50 percent of the normal experience.")
                .defineInRange("curse3XpMultiplier", 0.5D, 0.0D, 10.0D);

        CURSE_3_ENCHANT_COST_MULTIPLIER = BUILDER
                .comment("[Curse 3] Enchanting table level cost multiplier. 2.0 means the displayed level requirement is doubled.")
                .defineInRange("curse3EnchantCostMultiplier", 2.0D, 0.0D, 10.0D);

        CURSE_3_ANVIL_COST_MULTIPLIER = BUILDER
                .comment("[Curse 3] Anvil level cost multiplier. 2.0 means the anvil level cost is doubled.")
                .defineInRange("curse3AnvilCostMultiplier", 2.0D, 0.0D, 10.0D);

        CURSE_4_TRADE_SURCHARGE = BUILDER
                .comment("[Curse 4] Villager trade price surcharge multiplier. 0.25 means prices increase by 25 percent.")
                .defineInRange("curse4TradeSurcharge", 0.25D, 0.0D, 10.0D);

        CURSE_5_MAX_GUILT = BUILDER
                .comment("[Curse 5] Maximum King's Guilt value. When reached, curse 5 triggers execution, thunderstorm, lightning, and inventory lock.")
                .defineInRange("curse5MaxGuilt", 399, 1, 100000);//王罪值

        CURSE_5_PURIFICATION_AMOUNT = BUILDER
                .comment("[Curse 5] How much King's Guilt is removed by one Purification Scroll.")
                .defineInRange("curse5PurificationAmount", 50, 1, 100000);

        CURSE_5_LIGHTNING_STRIKES = BUILDER
                .comment("[Curse 5] Number of lightning strikes called down when King's Guilt reaches the maximum.")
                .defineInRange("curse5LightningStrikes", 10, 1, 200);

        CURSE_5_LOCK_DURATION = BUILDER
                .comment("[Curse 5] Duration in ticks of the full inventory lock after King's Guilt reaches the maximum.")
                .defineInRange("curse5LockDuration", 1200, 20, 72000);//背包锁定时间，单位tick

        MAIN_HAND_LOCK_DELAY = BUILDER
                .comment("[Curse 6] Delay in ticks before the first main-hand seal triggers. 36000 ticks = 30 minutes.")
                .defineInRange("mainHandLockDelay", 36000, 20, 720000);

        MAIN_HAND_LOCK_DURATION = BUILDER
                .comment("[Curse 6] Duration in ticks of the first main-hand seal. 1200 ticks = 1 minute.")
                .defineInRange("mainHandLockDuration", 1200, 20, 72000);

        INVENTORY_LOCK_INTERVAL = BUILDER
                .comment("[Curse 6] After the first main-hand seal, this is the interval in ticks for locking a random inventory item. 6000 ticks = 5 minutes.")
                .defineInRange("inventoryLockInterval", 6000, 20, 720000);

        INVENTORY_LOCK_DURATION = BUILDER
                .comment("[Curse 6] Duration in ticks for how long the random inventory item remains locked.")
                .defineInRange("inventoryLockDuration", 1200, 20, 72000);

        END_WEAKNESS_AMPLIFIER = BUILDER
                .comment("[Curse 7] Weakness amplifier applied only while the player is in the End.")
                .defineInRange("endWeaknessAmplifier", 0, 0, 10);

        HELL_REBOUND_CHANCE = BUILDER
                .comment("[Curse 8] After curse 8 is awakened in the Nether, this is the chance that attacking a mob will rebound damage back to the player.")
                .defineInRange("hellReboundChance", 0.15D, 0.0D, 1.0D);

        HELL_REBOUND_DAMAGE_MULTIPLIER = BUILDER
                .comment("[Curse 8] Self-damage multiplier for rebound. 1.10 means the player takes 110 percent of the dealt damage back.")
                .defineInRange("hellReboundDamageMultiplier", 1.10D, 0.0D, 10.0D);

        START_WITH_SEAL = BUILDER
                .comment("[General] If true, new players start with the Seal of Kadesh.")
                .define("startWithSeal", true);

        START_WITH_SEAL_EQUIPPED = BUILDER
                .comment("[General] If true, new players start with the Seal of Kadesh already equipped in the curse slot. When enabled, the seal is not also placed into the starting inventory.")
                .define("startWithSealEquipped", false);

        BUILDER.pop();
    }

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static final ForgeConfigSpec.IntValue RANDOM_CURSE_INTERVAL;
    public static final ForgeConfigSpec.IntValue RANDOM_CURSE_DURATION;
    public static final ForgeConfigSpec.IntValue RANDOM_CURSE_AMPLIFIER_MIN;
    public static final ForgeConfigSpec.IntValue RANDOM_CURSE_AMPLIFIER_MAX;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RANDOM_CURSE_NEGATIVE_POOL;
    public static final ForgeConfigSpec.IntValue CURSE_2_SPAWN_INTERVAL;
    public static final ForgeConfigSpec.DoubleValue CURSE_2_SPAWN_CHANCE;
    public static final ForgeConfigSpec.DoubleValue CURSE_2_LUCK_PENALTY;
    public static final ForgeConfigSpec.IntValue CURSE_2_SPAWN_RADIUS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CURSE_2_ENTITY_BLACKLIST;
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
    public static final ForgeConfigSpec.BooleanValue START_WITH_SEAL;
    public static final ForgeConfigSpec.BooleanValue START_WITH_SEAL_EQUIPPED;

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
        return getEffectFromPool(RANDOM_CURSE_NEGATIVE_POOL.get(), seed);
    }

    public static EntityType<?> getRandomCurseTwoEntityType(int seed) {
        List<EntityType<?>> filteredPool = ForgeRegistries.ENTITY_TYPES.getValues().stream()
                .filter(entityType -> entityType.getCategory() == MobCategory.MONSTER)
                .filter(entityType -> {
                    ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
                    return key != null && !CURSE_2_ENTITY_BLACKLIST.get().contains(key.toString());
                })
                .toList();
        return getEntityTypeFromPool(filteredPool, seed);
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
