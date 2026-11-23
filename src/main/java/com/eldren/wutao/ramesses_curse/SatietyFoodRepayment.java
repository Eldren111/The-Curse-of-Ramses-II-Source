package com.eldren.wutao.ramesses_curse;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** The persisted player data owns the round; crystal NBT is only a tooltip mirror. */
final class SatietyFoodRepayment {
    private static final String TARGETS = "SatietyFoodTargets";
    private static final String EATEN = "SatietyFoodsEaten";
    private static final String SEAL_SECONDS = "SatietyFoodSealSeconds";

    private SatietyFoodRepayment() {}

    static boolean isFood(ItemStack stack, Player player) {
        if (stack.isEmpty()) return false;
        // Forge food properties cover vanilla food and untagged mod food as well.
        if (stack.getFoodProperties(player) != null) return true;
        UseAnim animation = stack.getUseAnimation();
        return (animation == UseAnim.EAT || animation == UseAnim.DRINK)
                && stack.getTags().anyMatch(tag -> {
                    String path = tag.location().getPath();
                    return path.equals("food") || path.equals("foods")
                            || path.startsWith("food/") || path.startsWith("foods/");
                });
    }

    static void ensureRound(ServerPlayer player, CompoundTag data) {
        // Migrate the old assigned-food round without discarding foods already eaten.
        data.remove(TARGETS);
    }

    static boolean record(CompoundTag data, ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) return false;
        Set<String> eaten = new HashSet<>(read(data, EATEN));
        boolean added = SatietyFoodSelection.record(eaten, id.toString());
        if (added) write(data, EATEN, new ArrayList<>(eaten));
        return added;
    }

    static boolean complete(CompoundTag data) {
        return SatietyFoodSelection.complete(new HashSet<>(read(data, EATEN)));
    }

    static void clear(CompoundTag data) {
        data.remove(TARGETS);
        data.remove(EATEN);
    }

    static void showRequirements(ServerPlayer player, CompoundTag data) {
        List<Component> lines = new ArrayList<>();
        appendRequirements(data, lines);
        for (Component line : lines) player.displayClientMessage(line, false);
    }

    static void mirrorToCrystals(ServerPlayer player, CompoundTag data, boolean active) {
        boolean changed = false;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.is(ModItems.SATIETY_CRYSTAL.get())) continue;
            CompoundTag tag = stack.getOrCreateTag();
            CompoundTag before = tag.copy();
            if (active) {
                writeCrystalHints(stack, data);
            } else {
                clear(tag);
                tag.remove(SEAL_SECONDS);
            }
            changed |= !tag.equals(before);
        }
        if (changed) player.getInventory().setChanged();
    }

    static void writeCrystalHints(ItemStack crystal, CompoundTag data) {
        CompoundTag tag = crystal.getOrCreateTag();
        tag.remove(TARGETS);
        tag.put(EATEN, data.getList(EATEN, Tag.TAG_STRING).copy());
        tag.putInt(SEAL_SECONDS, Config.CURSE_9_FOOD_SEAL_SECONDS.get());
    }

    static void appendTooltip(ItemStack crystal, List<Component> tooltip) {
        CompoundTag tag = crystal.getTag();
        if (tag == null || !tag.contains(SEAL_SECONDS)) return;
        tooltip.add(Component.translatable("item.ramesses_curse.satiety_crystal.food_route",
                tag.getInt(SEAL_SECONDS)).withStyle(ChatFormatting.YELLOW));
        appendRequirements(tag, tooltip);
    }

    private static void appendRequirements(CompoundTag data, List<Component> lines) {
        lines.add(Component.translatable("message.ramesses_curse.satiety_food_need_types")
                .withStyle(ChatFormatting.GRAY));
        Set<String> eaten = new HashSet<>(read(data, EATEN));
        lines.add(Component.translatable("message.ramesses_curse.satiety_food_progress", eaten.size(), SatietyFoodSelection.REQUIRED)
                .withStyle(ChatFormatting.GOLD));
        for (String id : eaten.stream().sorted().toList()) {
            ResourceLocation location = ResourceLocation.tryParse(id);
            Item item = location == null ? null : ForgeRegistries.ITEMS.getValue(location);
            Component name = item == null ? Component.literal(id) : item.getDescription();
            lines.add(Component.literal("✓ ").append(name).withStyle(ChatFormatting.GREEN));
        }
    }

    private static List<String> read(CompoundTag data, String key) {
        ListTag tag = data.getList(key, Tag.TAG_STRING);
        List<String> values = new ArrayList<>();
        for (int index = 0; index < tag.size(); index++) values.add(tag.getString(index));
        return values;
    }

    private static void write(CompoundTag data, String key, List<String> values) {
        ListTag tag = new ListTag();
        values.forEach(value -> tag.add(StringTag.valueOf(value)));
        data.put(key, tag);
    }
}
