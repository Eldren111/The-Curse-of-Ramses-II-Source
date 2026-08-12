package com.eldren.wutao.ramesses_curse;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID)
public class DynastyAffixData extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DIRECTORY = "ramesses_curse";
    private static final String COMBINED_FILE = "dynasty_affixes";
    private static final String LEGACY_DIRECTORY = "dynasty_affixes/";
    private static final Map<String, AffixDefinition> DEFINITIONS = new HashMap<>();
    private static boolean affixesEnabled = true;
    public static final List<String> KNOWN_AFFIXES = List.of(
            "dodge", "corrosion", "damage_cap", "unyielding", "silence", "purge", "projectile_ward",
            "rage", "molten", "blood_forfeit", "flight_fall", "claw_rage", "steel_bone", "flesh_feast",
            "miasma", "restoration", "blood_limit", "shield_shatter", "death_authority", "hit_slow",
            "vulnerability", "counter");

    public DynastyAffixData() {
        super(GSON, DIRECTORY);
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new DynastyAffixData());
    }

    public static int unlockTier(String id) {
        return get(id).unlockTier;
    }

    public static boolean enabled(String id) {
        return affixesEnabled && get(id).enabled;
    }

    public static void setAffixesEnabled(boolean enabled) {
        affixesEnabled = enabled;
    }

    public static boolean affixesEnabled() {
        return affixesEnabled;
    }

    public static int level(String id, int threatTier) {
        AffixDefinition definition = get(id);
        if (threatTier < definition.unlockTier) {
            return 0;
        }
        return Math.max(1, Math.min(definition.maxLevel, (threatTier - definition.unlockTier) / Math.max(1, definition.levelStep) + 1));
    }

    public static double number(String id, int level, String key) {
        AffixDefinition definition = get(id);
        JsonObject values = definition.levelValues.get(Math.max(1, level));
        if (values == null || !values.has(key)) {
            return definition.defaultNumber(level, key);
        }
        return values.get(key).getAsDouble();
    }

    public static int intNumber(String id, int level, String key) {
        return (int) Math.round(number(id, level, key));
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<String, AffixDefinition> loaded = defaults();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            String path = entry.getKey().getPath();
            if (!(entry.getValue() instanceof JsonObject json)) {
                LOGGER.warn("Ignoring dynasty affix data {} because it is not a JSON object", entry.getKey());
                continue;
            }
            try {
                if (COMBINED_FILE.equals(path)) {
                    loadCombinedDefinitions(entry.getKey(), json, loaded);
                } else if (path.startsWith(LEGACY_DIRECTORY)) {
                    String id = path.substring(path.lastIndexOf('/') + 1);
                    loaded.put(id, AffixDefinition.fromJson(id, json, loaded.getOrDefault(id, AffixDefinition.defaultFor(id))));
                }
            } catch (RuntimeException exception) {
                LOGGER.warn("Failed to load dynasty affix data {}, keeping default", entry.getKey(), exception);
            }
        }
        DEFINITIONS.clear();
        DEFINITIONS.putAll(loaded);
        LOGGER.info("Loaded {} dynasty affix definitions", DEFINITIONS.size());
    }

    private static void loadCombinedDefinitions(ResourceLocation source, JsonObject json, Map<String, AffixDefinition> loaded) {
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String id = entry.getKey();
            if (!entry.getValue().isJsonObject()) {
                LOGGER.warn("Ignoring dynasty affix {} in {} because it is not a JSON object", id, source);
                continue;
            }
            loaded.put(id, AffixDefinition.fromJson(id, entry.getValue().getAsJsonObject(), loaded.getOrDefault(id, AffixDefinition.defaultFor(id))));
        }
    }

    private static AffixDefinition get(String id) {
        return DEFINITIONS.computeIfAbsent(id, key -> AffixDefinition.defaultFor(key));
    }

    private static Map<String, AffixDefinition> defaults() {
        Map<String, AffixDefinition> defaults = new HashMap<>();
        for (String id : KNOWN_AFFIXES) {
            defaults.put(id, AffixDefinition.defaultFor(id));
        }
        return defaults;
    }

    private static class AffixDefinition {
        final String id;
        final boolean enabled;
        final int unlockTier;
        final int maxLevel;
        final int levelStep;
        final Map<Integer, JsonObject> levelValues;

        AffixDefinition(String id, boolean enabled, int unlockTier, int maxLevel, int levelStep, Map<Integer, JsonObject> levelValues) {
            this.id = id;
            this.enabled = enabled;
            this.unlockTier = unlockTier;
            this.maxLevel = maxLevel;
            this.levelStep = levelStep;
            this.levelValues = levelValues;
        }

        static AffixDefinition fromJson(String id, JsonObject json, AffixDefinition fallback) {
            boolean enabled = readBoolean(json, "enabled", fallback.enabled);
            int unlockTier = readInt(json, "unlock_tier", fallback.unlockTier);
            int maxLevel = readInt(json, "max_level", fallback.maxLevel);
            int levelStep = readInt(json, "level_step", fallback.levelStep);
            Map<Integer, JsonObject> values = new HashMap<>(fallback.levelValues);
            if (json.has("levels") && json.get("levels").isJsonObject()) {
                JsonObject levels = json.getAsJsonObject("levels");
                for (Map.Entry<String, JsonElement> entry : levels.entrySet()) {
                    if (entry.getValue().isJsonObject()) {
                        values.put(Integer.parseInt(entry.getKey()), entry.getValue().getAsJsonObject());
                    }
                }
            }
            return new AffixDefinition(id, enabled, Math.max(1, unlockTier), Math.max(1, maxLevel), Math.max(1, levelStep), values);
        }

        static AffixDefinition defaultFor(String id) {
            return switch (id) {
                case "dodge" -> of(id, 1, 5, 2, new double[][]{{1, 0.10D}, {2, 0.20D}, {3, 0.30D}, {4, 0.40D}, {5, 0.50D}}, "chance");
                case "corrosion" -> of(id, 3, 5, 2, new double[][]{{1, 0.001D}, {2, 0.002D}, {3, 0.003D}, {4, 0.004D}, {5, 0.005D}}, "durability_ratio");
                case "damage_cap" -> of(id, 5, 5, 2, new double[][]{{1, 200.0D}, {2, 180.0D}, {3, 160.0D}, {4, 140.0D}, {5, 100.0D}}, "max_damage");
                case "unyielding" -> unyieldingDefault();
                case "silence" -> silenceDefault();
                case "purge" -> new AffixDefinition(id, true, 9, 1, 1, Map.of());
                case "projectile_ward" -> new AffixDefinition(id, true, 10, 1, 1, Map.of());
                case "rage" -> rageDefault();
                case "molten" -> moltenDefault();
                case "blood_forfeit" -> bloodForfeitDefault();
                case "flight_fall" -> flightFallDefault();
                case "claw_rage" -> clawRageDefault();
                case "steel_bone" -> new AffixDefinition(id, true, 9, 1, 1, Map.of());
                case "flesh_feast" -> fleshFeastDefault();
                case "miasma" -> miasmaDefault();
                case "restoration" -> restorationDefault();
                case "blood_limit" -> bloodLimitDefault();
                case "shield_shatter" -> shieldShatterDefault();
                case "death_authority" -> deathAuthorityDefault();
                case "hit_slow" -> hitSlowDefault();
                case "vulnerability" -> vulnerabilityDefault();
                case "counter" -> counterDefault();
                default -> new AffixDefinition(id, true, 1, 1, 1, Map.of());
            };
        }

        double defaultNumber(int level, String key) {
            return defaultFor(id).levelValues.getOrDefault(level, new JsonObject()).has(key)
                    ? defaultFor(id).levelValues.get(level).get(key).getAsDouble()
                    : 0.0D;
        }

        private static AffixDefinition of(String id, int unlockTier, int maxLevel, int levelStep, double[][] values, String key) {
            Map<Integer, JsonObject> levels = new HashMap<>();
            for (double[] value : values) {
                JsonObject json = new JsonObject();
                json.addProperty(key, value[1]);
                levels.put((int) value[0], json);
            }
            return new AffixDefinition(id, true, unlockTier, maxLevel, levelStep, levels);
        }

        private static AffixDefinition unyieldingDefault() {
            Map<Integer, JsonObject> levels = new HashMap<>();
            double[][] values = {{1, 0.20D, 0.50D}, {2, 0.40D, 0.60D}, {3, 0.60D, 0.70D}};
            for (double[] value : values) {
                JsonObject json = new JsonObject();
                json.addProperty("revive_chance", value[1]);
                json.addProperty("health_ratio", value[2]);
                levels.put((int) value[0], json);
            }
            return new AffixDefinition("unyielding", true, 5, 3, 2, levels);
        }

        private static AffixDefinition silenceDefault() {
            Map<Integer, JsonObject> levels = new HashMap<>();
            double[][] values = {{1, 0.01D, 1}, {2, 0.015D, 2}, {3, 0.02D, 3}, {4, 0.025D, 4}, {5, 0.03D, 5}};
            for (double[] value : values) {
                JsonObject json = new JsonObject();
                json.addProperty("chance", value[1]);
                json.addProperty("duration_seconds", value[2]);
                levels.put((int) value[0], json);
            }
            return new AffixDefinition("silence", true, 7, 5, 2, levels);
        }

        private static AffixDefinition rageDefault() {
            Map<Integer, JsonObject> levels = new HashMap<>();
            double[][] values = {{1, 0.40D, 0.10D}, {2, 0.60D, 0.15D}, {3, 0.80D, 0.20D}};
            for (double[] value : values) {
                JsonObject json = new JsonObject();
                json.addProperty("attack_speed_bonus", value[1]);
                json.addProperty("movement_speed_bonus", value[2]);
                json.addProperty("drop_chance", 0.002D);
                levels.put((int) value[0], json);
            }
            return new AffixDefinition("rage", true, 5, 3, 2, levels);
        }

        private static AffixDefinition moltenDefault() {
            JsonObject values = new JsonObject();
            values.addProperty("radius", 8.0D);
            values.addProperty("interval_ticks", 60);
            values.addProperty("cobweb_duration_ticks", 60);
            values.addProperty("fireball_count", 1);
            values.addProperty("upward_fireball_count", 2);
            values.addProperty("drop_chance", 0.002D);
            return new AffixDefinition("molten", true, 5, 1, 1, Map.of(1, values));
        }

        private static AffixDefinition bloodForfeitDefault() {
            JsonObject values = new JsonObject();
            values.addProperty("max_health_loss", 2.0D);
            values.addProperty("minimum_max_health", 2.0D);
            values.addProperty("drop_chance", 0.002D);
            return new AffixDefinition("blood_forfeit", true, 9, 1, 1, Map.of(1, values));
        }

        private static AffixDefinition flightFallDefault() {
            JsonObject values = new JsonObject();
            values.addProperty("grounded_duration_ticks", 100);
            return new AffixDefinition("flight_fall", true, 7, 1, 1, Map.of(1, values));
        }

        private static AffixDefinition clawRageDefault() {
            Map<Integer, JsonObject> levels = new HashMap<>();
            double[][] values = {
                    {1, 0.10D, 5.0D},
                    {2, 0.20D, 7.0D},
                    {3, 0.30D, 9.0D},
                    {4, 0.40D, 11.0D},
                    {5, 0.50D, 13.0D}
            };
            for (double[] value : values) {
                JsonObject json = new JsonObject();
                json.addProperty("trigger_chance", value[1]);
                json.addProperty("bonus_damage", value[2]);
                json.addProperty("drop_chance", 0.002D);
                levels.put((int) value[0], json);
            }
            return new AffixDefinition("claw_rage", true, 1, 5, 2, levels);
        }

        private static AffixDefinition fleshFeastDefault() {
            Map<Integer, JsonObject> levels = new HashMap<>();
            double[][] values = {
                    {1, 0.10D},
                    {2, 0.30D},
                    {3, 0.50D}
            };
            for (double[] value : values) {
                JsonObject json = new JsonObject();
                json.addProperty("radius", 5.0D);
                json.addProperty("health_bonus_per_player", value[1]);
                json.addProperty("armor_bonus_per_player", value[1]);
                json.addProperty("drop_chance", 0.002D);
                levels.put((int) value[0], json);
            }
            return new AffixDefinition("flesh_feast", true, 5, 3, 2, levels);
        }

        private static AffixDefinition miasmaDefault() {
            Map<Integer, JsonObject> levels = new HashMap<>();
            for (int level = 1; level <= 3; level++) {
                JsonObject json = new JsonObject();
                json.addProperty("chance", 0.15D);
                json.addProperty("duration_ticks", 100);
                json.addProperty("poison_level", level);
                json.addProperty("drop_chance", 0.002D);
                levels.put(level, json);
            }
            return new AffixDefinition("miasma", true, 5, 3, 2, levels);
        }

        private static AffixDefinition restorationDefault() {
            Map<Integer, JsonObject> levels = new HashMap<>();
            double[][] values = {{1, 0.50D}, {2, 1.00D}};
            for (double[] value : values) {
                JsonObject json = new JsonObject();
                json.addProperty("heal_ratio", value[1]);
                json.addProperty("drop_chance", 0.002D);
                levels.put((int) value[0], json);
            }
            return new AffixDefinition("restoration", true, 7, 2, 2, levels);
        }

        private static AffixDefinition bloodLimitDefault() {
            JsonObject values = new JsonObject();
            values.addProperty("window_ticks", 100);
            values.addProperty("max_health_loss_ratio", 0.35D);
            values.addProperty("minimum_health", 1.0D);
            values.addProperty("drop_chance", 0.002D);
            return new AffixDefinition("blood_limit", true, 9, 1, 1, Map.of(1, values));
        }

        private static AffixDefinition shieldShatterDefault() {
            JsonObject values = new JsonObject();
            values.addProperty("radius", 5.0D);
            values.addProperty("damage", 8.0D);
            values.addProperty("knockback_strength", 1.5D);
            return new AffixDefinition("shield_shatter", true, 1, 1, 1, Map.of(1, values));
        }

        private static AffixDefinition deathAuthorityDefault() {
            JsonObject values = new JsonObject();
            values.addProperty("damage_ratio", 0.15D);
            values.addProperty("cooldown_ticks", 80);
            values.addProperty("drop_chance", 0.002D);
            return new AffixDefinition("death_authority", true, 9, 1, 1, Map.of(1, values));
        }

        private static AffixDefinition hitSlowDefault() {
            JsonObject values = new JsonObject();
            values.addProperty("attack_speed_reduction", 0.50D);
            values.addProperty("duration_ticks", 100);
            return new AffixDefinition("hit_slow", true, 5, 1, 1, Map.of(1, values));
        }

        private static AffixDefinition vulnerabilityDefault() {
            Map<Integer, JsonObject> levels = new HashMap<>();
            JsonObject levelOne = new JsonObject();
            levelOne.addProperty("true_damage", 5.0D);
            levelOne.addProperty("drop_chance", 0.002D);
            levels.put(1, levelOne);
            JsonObject levelTwo = new JsonObject();
            levelTwo.addProperty("true_damage", 10.0D);
            levelTwo.addProperty("drop_chance", 0.002D);
            levels.put(2, levelTwo);
            return new AffixDefinition("vulnerability", true, 7, 2, 2, levels);
        }

        private static AffixDefinition counterDefault() {
            JsonObject values = new JsonObject();
            values.addProperty("revival_chance", 0.25D);
            values.addProperty("revival_ratio", 1.0D);
            values.addProperty("drop_chance", 0.002D);
            return new AffixDefinition("counter", true, 9, 1, 1, Map.of(1, values));
        }

        private static int readInt(JsonObject json, String key, int fallback) {
            return json.has(key) ? json.get(key).getAsInt() : fallback;
        }

        private static boolean readBoolean(JsonObject json, String key, boolean fallback) {
            return json.has(key) ? json.get(key).getAsBoolean() : fallback;
        }
    }
}
