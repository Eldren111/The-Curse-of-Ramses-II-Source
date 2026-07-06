package com.eldren.wutao.ramesses_curse;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
            ModEvents.DynastyShieldSnapshot shield = ModEvents.getDynastyShieldSnapshot(living);
            if (shield == null || shield.max() <= 0.0F || shield.current() <= 0.0F) {
                return;
            }
            data.putInt(SHIELD_LEVEL, shield.level());
            data.putFloat(SHIELD_CURRENT, shield.current());
            data.putFloat(SHIELD_MAX, shield.max());
            if (shield.dodgeLevel() > 0) {
                data.putInt(SHIELD_DODGE_LEVEL, shield.dodgeLevel());
            }
            if (shield.corrosionLevel() > 0) {
                data.putInt(SHIELD_CORROSION_LEVEL, shield.corrosionLevel());
            }
            if (shield.unyieldingLevel() > 0) {
                data.putInt(SHIELD_UNYIELDING_LEVEL, shield.unyieldingLevel());
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
            if (max <= 0.0F || current <= 0.0F) {
                return;
            }

            int currentDisplay = Math.max(0, Mth.ceil(current));
            int maxDisplay = Math.max(1, Mth.ceil(max));
            Component label = Component.translatable("jade.ramesses_curse.dynasty_shield", level, currentDisplay, maxDisplay)
                    .withStyle(ChatFormatting.GOLD);
            IElementHelper elements = tooltip.getElementHelper();
            tooltip.add(List.of(
                    elements.smallItem(new ItemStack(Items.IRON_CHESTPLATE)),
                    elements.text(label)));
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
