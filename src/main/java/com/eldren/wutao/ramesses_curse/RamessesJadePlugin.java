package com.eldren.wutao.ramesses_curse;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

import java.util.List;

@WailaPlugin(RamessesCurseMod.MODID)
public class RamessesJadePlugin implements IWailaPlugin {
    private static final ResourceLocation DYNASTY_SHIELD = new ResourceLocation(RamessesCurseMod.MODID, "dynasty_shield");
    private static final String SHIELD_LEVEL = "RamessesDynastyShieldLevel";
    private static final String SHIELD_CURRENT = "RamessesDynastyShieldCurrent";
    private static final String SHIELD_MAX = "RamessesDynastyShieldMax";
    private static final String SHIELD_DODGE_LEVEL = "RamessesDynastyShieldDodgeLevel";
    private static final String SHIELD_CORROSION_LEVEL = "RamessesDynastyShieldCorrosionLevel";
    private static final String SHIELD_UNYIELDING_LEVEL = "RamessesDynastyShieldUnyieldingLevel";
    private static final String SHIELD_DAMAGE_CAP_LEVEL = "RamessesDynastyShieldDamageCapLevel";
    private static final String SHIELD_SILENCE_LEVEL = "RamessesDynastyShieldSilenceLevel";
    private static final String SHIELD_PURGE = "RamessesDynastyShieldPurge";
    private static final String SHIELD_PROJECTILE_WARD = "RamessesDynastyShieldProjectileWard";
    private static final String SHIELD_RAGE_LEVEL = "RamessesDynastyShieldRageLevel";
    private static final String SHIELD_MOLTEN = "RamessesDynastyShieldMolten";
    private static final String SHIELD_BLOOD_FORFEIT = "RamessesDynastyShieldBloodForfeit";
    private static final String SHIELD_FLIGHT_FALL = "RamessesDynastyShieldFlightFall";
    private static final String SHIELD_CLAW_RAGE_LEVEL = "RamessesDynastyShieldClawRageLevel";
    private static final String SHIELD_STEEL_BONE = "RamessesDynastyShieldSteelBone";
    private static final String SHIELD_FLESH_FEAST_LEVEL = "RamessesDynastyShieldFleshFeastLevel";
    private static final String SHIELD_MIASMA_LEVEL = "RamessesDynastyShieldMiasmaLevel";
    private static final String SHIELD_RESTORATION_LEVEL = "RamessesDynastyShieldRestorationLevel";
    private static final String SHIELD_BLOOD_LIMIT = "RamessesDynastyShieldBloodLimit";
    private static final String SHIELD_SHATTER = "RamessesDynastyShieldShatter";
    private static final String SHIELD_DEATH_AUTHORITY = "RamessesDynastyShieldDeathAuthority";
    private static final String SHIELD_HIT_SLOW = "RamessesDynastyShieldHitSlow";
    private static final String SHIELD_VULNERABILITY_LEVEL = "RamessesDynastyShieldVulnerabilityLevel";
    private static final String SHIELD_COUNTER = "RamessesDynastyShieldCounter";

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerEntityDataProvider(DynastyShieldProvider.INSTANCE, LivingEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addConfig(DYNASTY_SHIELD, true);
        registration.registerEntityComponent(DynastyShieldProvider.INSTANCE, LivingEntity.class);
    }

    private enum DynastyShieldProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
        INSTANCE;

        @Override
        public ResourceLocation getUid() {
            return DYNASTY_SHIELD;
        }

        @Override
        public void appendServerData(CompoundTag data, EntityAccessor accessor) {
            if (!(accessor.getEntity() instanceof LivingEntity living)) {
                return;
            }
            ModEvents.DynastyShieldSnapshot shield = ModEvents.getDynastyShieldSnapshot(living, accessor.getPlayer());
            if (shield == null || shield.max() <= 0.0F || (shield.current() <= 0.0F
                    && shield.unyieldingLevel() <= 0
                    && shield.miasmaLevel() <= 0
                    && shield.restorationLevel() <= 0
                    && !shield.bloodForfeit()
                    && !shield.steelBone()
                    && !shield.bloodLimit())) {
                return;
            }
            data.putInt(SHIELD_LEVEL, shield.level());
            data.putFloat(SHIELD_CURRENT, shield.current());
            data.putFloat(SHIELD_MAX, shield.max());
            boolean shieldActive = shield.current() > 0.0F;
            if (shieldActive && shield.dodgeLevel() > 0) {
                data.putInt(SHIELD_DODGE_LEVEL, shield.dodgeLevel());
            }
            if (shieldActive && shield.corrosionLevel() > 0) {
                data.putInt(SHIELD_CORROSION_LEVEL, shield.corrosionLevel());
            }
            if (shield.unyieldingLevel() > 0) {
                data.putInt(SHIELD_UNYIELDING_LEVEL, shield.unyieldingLevel());
            }
            if (shieldActive && shield.damageCapLevel() > 0) {
                data.putInt(SHIELD_DAMAGE_CAP_LEVEL, shield.damageCapLevel());
            }
            if (shieldActive && shield.silenceLevel() > 0) {
                data.putInt(SHIELD_SILENCE_LEVEL, shield.silenceLevel());
            }
            if (shieldActive && shield.rageLevel() > 0) {
                data.putInt(SHIELD_RAGE_LEVEL, shield.rageLevel());
            }
            if (shieldActive && shield.clawRageLevel() > 0) {
                data.putInt(SHIELD_CLAW_RAGE_LEVEL, shield.clawRageLevel());
            }
            if (shieldActive && shield.fleshFeastLevel() > 0) {
                data.putInt(SHIELD_FLESH_FEAST_LEVEL, shield.fleshFeastLevel());
            }
            if (shield.miasmaLevel() > 0) {
                data.putInt(SHIELD_MIASMA_LEVEL, shield.miasmaLevel());
            }
            if (shield.restorationLevel() > 0) {
                data.putInt(SHIELD_RESTORATION_LEVEL, shield.restorationLevel());
            }
            if (shieldActive && shield.vulnerabilityLevel() > 0) {
                data.putInt(SHIELD_VULNERABILITY_LEVEL, shield.vulnerabilityLevel());
            }
            if (shieldActive && shield.molten()) {
                data.putBoolean(SHIELD_MOLTEN, true);
            }
            if (shieldActive && shield.purge()) {
                data.putBoolean(SHIELD_PURGE, true);
            }
            if (shieldActive && shield.projectileWard()) {
                data.putBoolean(SHIELD_PROJECTILE_WARD, true);
            }
            if (shield.bloodForfeit()) {
                data.putBoolean(SHIELD_BLOOD_FORFEIT, true);
            }
            if (shieldActive && shield.flightFall()) {
                data.putBoolean(SHIELD_FLIGHT_FALL, true);
            }
            if (shield.steelBone()) {
                data.putBoolean(SHIELD_STEEL_BONE, true);
            }
            if (shield.bloodLimit()) {
                data.putBoolean(SHIELD_BLOOD_LIMIT, true);
            }
            if (shieldActive && shield.shieldShatter()) {
                data.putBoolean(SHIELD_SHATTER, true);
            }
            if (shieldActive && shield.deathAuthority()) {
                data.putBoolean(SHIELD_DEATH_AUTHORITY, true);
            }
            if (shieldActive && shield.hitSlow()) {
                data.putBoolean(SHIELD_HIT_SLOW, true);
            }
            if (shieldActive && shield.counter()) {
                data.putBoolean(SHIELD_COUNTER, true);
            }
        }

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            if (!data.contains(SHIELD_MAX)) {
                return;
            }

            int level = data.getInt(SHIELD_LEVEL);
            float current = data.getFloat(SHIELD_CURRENT);
            float max = data.getFloat(SHIELD_MAX);
            if (max <= 0.0F || (current <= 0.0F
                    && !data.contains(SHIELD_UNYIELDING_LEVEL)
                    && !data.contains(SHIELD_MIASMA_LEVEL)
                    && !data.contains(SHIELD_RESTORATION_LEVEL)
                    && !data.getBoolean(SHIELD_BLOOD_FORFEIT)
                    && !data.getBoolean(SHIELD_STEEL_BONE))) {
                return;
            }

            IElementHelper elements = tooltip.getElementHelper();
            boolean shieldActive = current > 0.0F;
            if (shieldActive) {
                int currentDisplay = Math.max(0, Mth.ceil(current));
                int maxDisplay = Math.max(1, Mth.ceil(max));
                Component label = Component.translatable("jade.ramesses_curse.dynasty_shield", level, currentDisplay, maxDisplay)
                        .withStyle(ChatFormatting.GOLD);
                tooltip.add(List.of(
                        elements.smallItem(new ItemStack(Items.IRON_CHESTPLATE)),
                        elements.text(label)));
            }
            if (data.contains(SHIELD_DODGE_LEVEL)) {
                int dodgeLevel = Mth.clamp(data.getInt(SHIELD_DODGE_LEVEL), 1, 5);
                Component dodge = Component.translatable("jade.ramesses_curse.dynasty_shield.dodge", toRomanDodgeLevel(dodgeLevel))
                        .withStyle(ChatFormatting.AQUA);
                tooltip.add(List.of(elements.text(dodge)));
            }
            if (data.contains(SHIELD_CORROSION_LEVEL)) {
                int corrosionLevel = Mth.clamp(data.getInt(SHIELD_CORROSION_LEVEL), 1, 5);
                Component corrosion = Component.translatable("jade.ramesses_curse.dynasty_shield.corrosion", toRomanDodgeLevel(corrosionLevel))
                        .withStyle(ChatFormatting.DARK_GREEN);
                tooltip.add(List.of(elements.text(corrosion)));
            }
            if (data.contains(SHIELD_UNYIELDING_LEVEL)) {
                int unyieldingLevel = Mth.clamp(data.getInt(SHIELD_UNYIELDING_LEVEL), 1, 3);
                Component unyielding = Component.translatable("jade.ramesses_curse.dynasty_shield.unyielding", toRomanDodgeLevel(unyieldingLevel))
                        .withStyle(ChatFormatting.DARK_RED);
                tooltip.add(List.of(elements.text(unyielding)));
            }
            if (data.contains(SHIELD_DAMAGE_CAP_LEVEL)) {
                int damageCapLevel = Mth.clamp(data.getInt(SHIELD_DAMAGE_CAP_LEVEL), 1, 5);
                Component damageCap = Component.translatable("jade.ramesses_curse.dynasty_shield.damage_cap", toRomanDodgeLevel(damageCapLevel))
                        .withStyle(ChatFormatting.BLUE);
                tooltip.add(List.of(elements.text(damageCap)));
            }
            if (data.contains(SHIELD_SILENCE_LEVEL)) {
                int silenceLevel = Mth.clamp(data.getInt(SHIELD_SILENCE_LEVEL), 1, 5);
                Component silence = Component.translatable("jade.ramesses_curse.dynasty_shield.silence", toRomanDodgeLevel(silenceLevel))
                        .withStyle(ChatFormatting.DARK_PURPLE);
                tooltip.add(List.of(elements.text(silence)));
            }
            if (data.contains(SHIELD_RAGE_LEVEL)) {
                int rageLevel = Mth.clamp(data.getInt(SHIELD_RAGE_LEVEL), 1, 3);
                Component rage = Component.translatable("jade.ramesses_curse.dynasty_shield.rage", toRomanDodgeLevel(rageLevel))
                        .withStyle(ChatFormatting.RED);
                tooltip.add(List.of(elements.text(rage)));
            }
            if (data.contains(SHIELD_CLAW_RAGE_LEVEL)) {
                int clawRageLevel = Mth.clamp(data.getInt(SHIELD_CLAW_RAGE_LEVEL), 1, 5);
                Component clawRage = Component.translatable("jade.ramesses_curse.dynasty_shield.claw_rage", toRomanDodgeLevel(clawRageLevel))
                        .withStyle(ChatFormatting.YELLOW);
                tooltip.add(List.of(elements.text(clawRage)));
            }
            if (data.contains(SHIELD_FLESH_FEAST_LEVEL)) {
                int fleshFeastLevel = Mth.clamp(data.getInt(SHIELD_FLESH_FEAST_LEVEL), 1, 3);
                Component fleshFeast = Component.translatable(
                                "jade.ramesses_curse.dynasty_shield.flesh_feast",
                                toRomanDodgeLevel(fleshFeastLevel))
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0xC85A78)));
                tooltip.add(List.of(elements.text(fleshFeast)));
            }
            if (data.contains(SHIELD_MIASMA_LEVEL)) {
                int miasmaLevel = Mth.clamp(data.getInt(SHIELD_MIASMA_LEVEL), 1, 3);
                Component miasma = Component.translatable(
                                "jade.ramesses_curse.dynasty_shield.miasma",
                                toRomanDodgeLevel(miasmaLevel))
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0x82B440)));
                tooltip.add(List.of(elements.text(miasma)));
            }
            if (data.contains(SHIELD_RESTORATION_LEVEL)) {
                int restorationLevel = Mth.clamp(data.getInt(SHIELD_RESTORATION_LEVEL), 1, 2);
                Component restoration = Component.translatable(
                                "jade.ramesses_curse.dynasty_shield.restoration",
                                toRomanDodgeLevel(restorationLevel))
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0x5FD6A1)));
                tooltip.add(List.of(elements.text(restoration)));
            }
            if (data.contains(SHIELD_VULNERABILITY_LEVEL)) {
                int vulnerabilityLevel = Mth.clamp(data.getInt(SHIELD_VULNERABILITY_LEVEL), 1, 2);
                Component vulnerability = Component.translatable(
                                "jade.ramesses_curse.dynasty_shield.vulnerability",
                                toRomanDodgeLevel(vulnerabilityLevel))
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0xB75C8D)));
                tooltip.add(List.of(elements.text(vulnerability)));
            }
            if (data.getBoolean(SHIELD_MOLTEN)) {
                Component molten = Component.translatable("jade.ramesses_curse.dynasty_shield.molten")
                        .withStyle(ChatFormatting.GOLD);
                tooltip.add(List.of(elements.text(molten)));
            }
            if (data.getBoolean(SHIELD_BLOOD_FORFEIT)) {
                Component bloodForfeit = Component.translatable("jade.ramesses_curse.dynasty_shield.blood_forfeit")
                        .withStyle(ChatFormatting.DARK_RED);
                tooltip.add(List.of(elements.text(bloodForfeit)));
            }
            if (data.getBoolean(SHIELD_FLIGHT_FALL)) {
                Component flightFall = Component.translatable("jade.ramesses_curse.dynasty_shield.flight_fall")
                        .withStyle(ChatFormatting.DARK_AQUA);
                tooltip.add(List.of(elements.text(flightFall)));
            }
            if (data.getBoolean(SHIELD_PURGE)) {
                Component purge = Component.translatable("jade.ramesses_curse.dynasty_shield.purge")
                        .withStyle(ChatFormatting.LIGHT_PURPLE);
                tooltip.add(List.of(elements.text(purge)));
            }
            if (data.getBoolean(SHIELD_PROJECTILE_WARD)) {
                Component projectileWard = Component.translatable("jade.ramesses_curse.dynasty_shield.projectile_ward")
                        .withStyle(ChatFormatting.GRAY);
                tooltip.add(List.of(elements.text(projectileWard)));
            }
            if (data.getBoolean(SHIELD_STEEL_BONE)) {
                Component steelBone = Component.translatable("jade.ramesses_curse.dynasty_shield.steel_bone")
                        .withStyle(ChatFormatting.WHITE);
                tooltip.add(List.of(elements.text(steelBone)));
            }
            if (data.getBoolean(SHIELD_BLOOD_LIMIT)) {
                Component bloodLimit = Component.translatable("jade.ramesses_curse.dynasty_shield.blood_limit")
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0xD94B5B)));
                tooltip.add(List.of(elements.text(bloodLimit)));
            }
            if (data.getBoolean(SHIELD_SHATTER)) {
                Component shieldShatter = Component.translatable("jade.ramesses_curse.dynasty_shield.shield_shatter")
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0xE09A55)));
                tooltip.add(List.of(elements.text(shieldShatter)));
            }
            if (data.getBoolean(SHIELD_DEATH_AUTHORITY)) {
                Component deathAuthority = Component.translatable("jade.ramesses_curse.dynasty_shield.death_authority")
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0x8E3A4A)));
                tooltip.add(List.of(elements.text(deathAuthority)));
            }
            if (data.getBoolean(SHIELD_HIT_SLOW)) {
                Component hitSlow = Component.translatable("jade.ramesses_curse.dynasty_shield.hit_slow")
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0x65708A)));
                tooltip.add(List.of(elements.text(hitSlow)));
            }
            if (data.getBoolean(SHIELD_COUNTER)) {
                Component counter = Component.translatable("jade.ramesses_curse.dynasty_shield.counter")
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0xC9A13B)));
                tooltip.add(List.of(elements.text(counter)));
            }
        }

        private String toRomanDodgeLevel(int dodgeLevel) {
            return switch (dodgeLevel) {
                case 1 -> "I";
                case 2 -> "II";
                case 3 -> "III";
                case 4 -> "IV";
                default -> "V";
            };
        }

    }
}
