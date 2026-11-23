package com.eldren.wutao.ramesses_requiem;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import com.eldren.wutao.ramesses_curse.dynamictext.api.DynamicTextApi;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.Level;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public final class CrimsonRequiemItem extends SwordItem {
    public CrimsonRequiemItem() {
        super(Tiers.NETHERITE, 15, -2.4F, new Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
    }

    @Override public boolean isDamageable(ItemStack stack) { return false; }
    @Override public String getCreatorModId(ItemStack stack) { return RequiemAddon.MODID; }
    @Override public void setDamage(ItemStack stack, int damage) {}
    @Override public boolean isBarVisible(ItemStack stack) { return false; }
    @Override public boolean isFoil(ItemStack stack) { return true; }
    @Override public int getDefaultTooltipHideFlags(ItemStack stack) { return super.getDefaultTooltipHideFlags(stack) | 7; }

    @Override public Component getName(ItemStack stack) {
        return DynamicTextApi.parse(Component.literal("&m").append(super.getName(stack)));
    }

    @Override public int getEnchantmentLevel(ItemStack stack, Enchantment enchantment) {
        int level = super.getEnchantmentLevel(stack, enchantment);
        if (enchantment == Enchantments.SHARPNESS) return Math.max(20, level);
        if (enchantment == Enchantments.MOB_LOOTING) return Math.max(10, level);
        return level;
    }

    @Override public Map<Enchantment, Integer> getAllEnchantments(ItemStack stack) {
        Map<Enchantment, Integer> enchantments = new HashMap<>(super.getAllEnchantments(stack));
        enchantments.merge(Enchantments.SHARPNESS, 20, Math::max);
        enchantments.merge(Enchantments.MOB_LOOTING, 10, Math::max);
        return enchantments;
    }

    public static void ensureEnchantments(ItemStack stack) {
        if (EnchantmentHelper.getTagEnchantmentLevel(Enchantments.SHARPNESS, stack) < 20
                || EnchantmentHelper.getTagEnchantmentLevel(Enchantments.MOB_LOOTING, stack) < 10) {
            Map<Enchantment, Integer> enchantments = new HashMap<>(EnchantmentHelper.getEnchantments(stack));
            enchantments.merge(Enchantments.SHARPNESS, 20, Math::max);
            enchantments.merge(Enchantments.MOB_LOOTING, 10, Math::max);
            EnchantmentHelper.setEnchantments(enchantments, stack);
        }
        if (!stack.getOrCreateTag().getBoolean("Unbreakable")) stack.getOrCreateTag().putBoolean("Unbreakable", true);
    }

    @Override public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        ensureEnchantments(stack);
        return stack;
    }

    @Override public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide) {
            ensureEnchantments(stack);
            CrimsonRequiemFlight.expire(stack, level);
        }
    }

    @Override public boolean onDroppedByPlayer(ItemStack stack, Player player) {
        return !CrimsonRequiemFlight.locked(stack);
    }

    @Override public boolean onEntityItemUpdate(ItemStack stack, net.minecraft.world.entity.item.ItemEntity entity) {
        CrimsonRequiemFlight.expire(stack, entity.level());
        return false;
    }

    @Override public boolean canAttackBlock(net.minecraft.world.level.block.state.BlockState state, Level level,
            net.minecraft.core.BlockPos pos, Player player) {
        return !CrimsonRequiemFlight.locked(player.getMainHandItem()) && super.canAttackBlock(state, level, pos, player);
    }

    @Override public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot != EquipmentSlot.MAINHAND) return super.getAttributeModifiers(slot, stack);
        return ImmutableMultimap.of(
                Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier",
                        CrimsonRequiemConfig.ATTACK_DAMAGE.get() - 1D, AttributeModifier.Operation.ADDITION),
                Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier",
                        CrimsonRequiemConfig.ATTACK_SPEED.get() - 4D, AttributeModifier.Operation.ADDITION));
    }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand != CrimsonRequiemActions.weaponHand(player)) return InteractionResultHolder.pass(player.getItemInHand(hand));
        if (!level.isClientSide) CrimsonRequiemActions.rightClick(player);
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        boolean attributes = Screen.hasShiftDown();
        boolean skills = Screen.hasControlDown();
        Player viewer = CrimsonRequiemClient.tooltipPlayer();
        String trueDamage = formatDamage(CrimsonRequiemDamage.boomerangTrueDamage(viewer));
        String magicDamage = formatDamage(CrimsonRequiemDamage.boomerangMagicDamage(viewer));
        // Vanilla enchantment lines are hidden with the built-in stats; show applied upgrades explicitly.
        stack.getAllEnchantments().entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .filter(entry -> entry.getKey() != Enchantments.SHARPNESS || entry.getValue() > 20)
                .filter(entry -> entry.getKey() != Enchantments.MOB_LOOTING || entry.getValue() > 10)
                .sorted(java.util.Comparator.comparing(entry -> entry.getKey().getDescriptionId()))
                .forEach(entry -> tooltip.add(entry.getKey().getFullname(entry.getValue())));
        if (!attributes && !skills) {
            for (int line = 1; line <= 3; line++) {
                tooltip.add(DynamicTextApi.parse(Component.literal(line == 3 ? "&u" : "&m")
                        .append(Component.translatable("tooltip.ramesses_curse.crimson_requiem.lore_" + line))));
            }
        }
        if (attributes) {
            tooltip.add(Component.translatable("tooltip.ramesses_curse.crimson_requiem.attributes").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
            for (String key : List.of("damage", "speed", "sharpness", "looting", "piercing")) {
                tooltip.add(Component.translatable("tooltip.ramesses_curse.crimson_requiem." + key).withStyle(ChatFormatting.BLUE));
            }
        }
        List<Integer> skillTitles = new java.util.ArrayList<>();
        if (skills) {
            tooltip.add(Component.translatable("tooltip.ramesses_curse.crimson_requiem.skills").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            for (String key : List.of("wave", "throw", "burst", "ward")) {
                tooltip.add(Component.empty());
                skillTitles.add(tooltip.size());
                tooltip.add(Component.translatable("tooltip.ramesses_curse.crimson_requiem." + key + "_title")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
                tooltip.add(Component.empty());
                tooltip.add((key.equals("throw")
                        ? Component.translatable("tooltip.ramesses_curse.crimson_requiem.throw", trueDamage, magicDamage)
                        : key.equals("ward") ? Component.translatable("tooltip.ramesses_curse.crimson_requiem.ward",
                                CrimsonRequiemConfig.EXECUTION_RANGE.get().intValue(), CrimsonRequiemConfig.WARD_COOLDOWN.get())
                        : Component.translatable("tooltip.ramesses_curse.crimson_requiem." + key)).withStyle(ChatFormatting.GRAY));
            }
        }
        if (!attributes) tooltip.add(Component.translatable("tooltip.ramesses_curse.crimson_requiem.hint_shift").withStyle(ChatFormatting.DARK_GRAY));
        if (!skills) tooltip.add(Component.translatable("tooltip.ramesses_curse.crimson_requiem.hint_ctrl").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.ramesses_curse.crimson_requiem.eternal").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("tooltip.ramesses_curse.crimson_requiem.true_damage", trueDamage).withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.translatable("tooltip.ramesses_curse.crimson_requiem.magic_damage", magicDamage).withStyle(ChatFormatting.BLUE));
        // Use rendered font widths so localized bracketed titles align with the tooltip body.
        if (!skillTitles.isEmpty()) {
            var font = net.minecraft.client.Minecraft.getInstance().font;
            int width = tooltip.stream().mapToInt(font::width).max().orElse(0);
            int spaceWidth = Math.max(1, font.width(" "));
            for (int index : skillTitles) {
                Component title = tooltip.get(index);
                int spaces = Math.max(0, Math.round((width - font.width(title)) / (2F * spaceWidth)));
                tooltip.set(index, Component.literal(" ".repeat(spaces)).append(title));
            }
        }
    }

    private static String formatDamage(float damage) {
        return String.format(java.util.Locale.ROOT, "%.2f", damage).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
