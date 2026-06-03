package com.eldren.wutao.ramesses_curse;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, RamessesCurseMod.MODID);

    public static final RegistryObject<Item> SEAL_OF_KADESH =
            ITEMS.register("seal_of_kadesh", SealOfKadesh::new);
    public static final RegistryObject<Item> PURIFICATION_SCROLL =
            ITEMS.register("purification_scroll", PurificationScroll::new);
    public static final RegistryObject<Item> SATIETY_CRYSTAL =
            ITEMS.register("satiety_crystal", SatietyCrystal::new);

    public static class SealOfKadesh extends Item implements ICurioItem {
        private static final String WARNING_KEY = "message.ramesses_curse.curse_wrapping";
        private static final UUID CURSE_2_LUCK_MODIFIER_ID = UUID.fromString("d5b8dc2a-6541-46b2-8d8a-f642f6f40b91");

        public SealOfKadesh() {
            super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
        }

        @Override
        public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
            LivingEntity wearer = slotContext.entity();
            if (wearer instanceof ServerPlayer player) {
                player.displayClientMessage(Component.translatable(WARNING_KEY).withStyle(ChatFormatting.DARK_RED), true);
            }
        }

        @Override
        public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
            LivingEntity wearer = slotContext.entity();
            return wearer instanceof Player player && player.getAbilities().instabuild;
        }

        @Override
        public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
            LivingEntity wearer = slotContext.entity();
            if (wearer instanceof Player player) {
                removeLuckPenalty(player);
            }
        }

        @Override
        public ICurio.DropRule getDropRule(SlotContext slotContext, DamageSource source, int lootingLevel, boolean recentlyHit, ItemStack stack) {
            return ICurio.DropRule.ALWAYS_KEEP;
        }

        @Override
        public void curioTick(SlotContext slotContext, ItemStack stack) {
            LivingEntity wearer = slotContext.entity();

            if (wearer.level().isClientSide) {
                return;
            }

            long tick = wearer.tickCount;

            applyFavorOrDamnation(wearer, tick);
            applyCrownOfRegency(wearer, tick);
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable("item.ramesses_curse.seal_of_kadesh.desc").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("item.ramesses_curse.seal_of_kadesh.hint_shift").withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.translatable("item.ramesses_curse.seal_of_kadesh.hint_ctrl").withStyle(ChatFormatting.AQUA));

            if (Screen.hasShiftDown()) {
                tooltip.add(Component.empty());
                tooltip.add(Component.translatable("item.ramesses_curse.seal_of_kadesh.section_curses").withStyle(ChatFormatting.DARK_RED));
                tooltip.add(Component.translatable("item.ramesses_curse.seal_of_kadesh.curse_1_title").withStyle(ChatFormatting.LIGHT_PURPLE));
                tooltip.add(Component.translatable(
                        "item.ramesses_curse.seal_of_kadesh.curse_1_desc",
                        redNumber(formatTicksAsMinutes(Config.RANDOM_CURSE_INTERVAL.get())),
                        redNumber(formatTicksAsSeconds(Config.RANDOM_CURSE_DURATION.get())),
                        redNumber(Config.RANDOM_CURSE_AMPLIFIER_MIN.get()),
                        redNumber(Math.max(Config.RANDOM_CURSE_AMPLIFIER_MIN.get(), Config.RANDOM_CURSE_AMPLIFIER_MAX.get())))
                        .withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("item.ramesses_curse.seal_of_kadesh.curse_2_title").withStyle(ChatFormatting.GOLD));
                tooltip.add(Component.translatable(
                        "item.ramesses_curse.seal_of_kadesh.curse_2_desc",
                        redNumber(formatNumber(Config.CURSE_2_LUCK_PENALTY.get())),
                        redNumber(formatTicksAsMinutes(Config.CURSE_2_SPAWN_INTERVAL.get())),
                        redPercent(Config.CURSE_2_SPAWN_CHANCE.get()),
                        redNumber(Config.CURSE_2_SPAWN_RADIUS.get()))
                        .withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("item.ramesses_curse.seal_of_kadesh.curse_3_title").withStyle(ChatFormatting.RED));
                tooltip.add(Component.translatable(
                        "item.ramesses_curse.seal_of_kadesh.curse_3_desc",
                        redPercentInverse(Config.CURSE_3_XP_MULTIPLIER.get()),
                        redNumber(formatNumber(Config.CURSE_3_ENCHANT_COST_MULTIPLIER.get())),
                        redNumber(formatNumber(Config.CURSE_3_ANVIL_COST_MULTIPLIER.get())))
                        .withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("item.ramesses_curse.seal_of_kadesh.curse_4_title").withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(Component.translatable(
                        "item.ramesses_curse.seal_of_kadesh.curse_4_desc",
                        redPercent(Config.CURSE_4_TRADE_SURCHARGE.get()))
                        .withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("item.ramesses_curse.seal_of_kadesh.curse_5_title").withStyle(ChatFormatting.AQUA));
                tooltip.add(Component.translatable(
                        "item.ramesses_curse.seal_of_kadesh.curse_5_desc",
                        redNumber(Config.CURSE_5_MAX_GUILT.get()),
                        redNumber(formatTicksAsMinutes(Config.CURSE_5_LOCK_DURATION.get())),
                        redNumber(Config.CURSE_5_LIGHTNING_STRIKES.get()))
                        .withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("item.ramesses_curse.seal_of_kadesh.curse_6_title").withStyle(ChatFormatting.BLUE));
                tooltip.add(Component.translatable(
                        "item.ramesses_curse.seal_of_kadesh.curse_6_desc",
                        redNumber(formatTicksAsMinutes(Config.MAIN_HAND_LOCK_DELAY.get())),
                        redNumber(formatTicksAsMinutes(Config.MAIN_HAND_LOCK_DURATION.get())),
                        redNumber(formatTicksAsMinutes(Config.INVENTORY_LOCK_INTERVAL.get())),
                        redNumber(formatTicksAsMinutes(Config.INVENTORY_LOCK_DURATION.get())))
                        .withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("item.ramesses_curse.seal_of_kadesh.curse_7_title").withStyle(ChatFormatting.DARK_PURPLE));
                tooltip.add(Component.translatable(
                        "item.ramesses_curse.seal_of_kadesh.curse_7_desc",
                        redNumber(Config.END_WEAKNESS_AMPLIFIER.get() + 1))
                        .withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("item.ramesses_curse.seal_of_kadesh.curse_8_title").withStyle(ChatFormatting.DARK_GREEN));
                tooltip.add(Component.translatable(
                        "item.ramesses_curse.seal_of_kadesh.curse_8_desc",
                        redPercent(Config.HELL_REBOUND_CHANCE.get()),
                        redPercent(Config.HELL_REBOUND_DAMAGE_MULTIPLIER.get() - 1.0D))
                        .withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("item.ramesses_curse.seal_of_kadesh.curse_9_title").withStyle(ChatFormatting.WHITE));
                tooltip.add(Component.translatable("item.ramesses_curse.seal_of_kadesh.curse_9_desc").withStyle(ChatFormatting.GRAY));
            }

            if (Screen.hasControlDown()) {
                tooltip.add(Component.empty());
                tooltip.add(Component.translatable("item.ramesses_curse.seal_of_kadesh.section_story").withStyle(ChatFormatting.AQUA));
                for (int i = 1; i <= 5; i++) {
                    tooltip.add(Component.translatable("item.ramesses_curse.seal_of_kadesh.story_" + i).withStyle(ChatFormatting.GRAY));
                }
            }
        }

        private void applyFavorOrDamnation(LivingEntity wearer, long tick) {
            int interval = Config.RANDOM_CURSE_INTERVAL.get();
            if (tick % interval != 0L) {
                return;
            }

            MobEffect effect = Config.getRandomNegativeEffect(wearer.getRandom().nextInt());

            if (effect == null || effect == MobEffects.DARKNESS || effect == MobEffects.DIG_SLOWDOWN || effect == MobEffects.CONFUSION) {
                return;
            }

            int minAmp = Config.RANDOM_CURSE_AMPLIFIER_MIN.get();
            int maxAmp = Math.max(minAmp, Config.RANDOM_CURSE_AMPLIFIER_MAX.get());
            int amplifier = minAmp + wearer.getRandom().nextInt(maxAmp - minAmp + 1);
            wearer.addEffect(new MobEffectInstance(effect, Config.RANDOM_CURSE_DURATION.get(), amplifier, false, false, true));
        }

        private void applyCrownOfRegency(LivingEntity wearer, long tick) {
            if (!(wearer instanceof ServerPlayer player)) {
                return;
            }

            applyLuckPenalty(player);

            int equipTicks = ModEvents.getSealEquipTicks(player);
            int interval = Config.CURSE_2_SPAWN_INTERVAL.get();
            if (equipTicks <= 0 || equipTicks % interval != 0) {
                return;
            }

            if (player.getRandom().nextDouble() >= Config.CURSE_2_SPAWN_CHANCE.get()) {
                return;
            }

            ModEvents.spawnCurseTwoMob(player);
        }

        private void applyLuckPenalty(Player player) {
            AttributeInstance luck = player.getAttribute(Attributes.LUCK);
            if (luck == null || luck.getModifier(CURSE_2_LUCK_MODIFIER_ID) != null) {
                return;
            }

            luck.addTransientModifier(new AttributeModifier(
                    CURSE_2_LUCK_MODIFIER_ID,
                    "seal_of_kadesh_curse_2_penalty",
                    -Config.CURSE_2_LUCK_PENALTY.get(),
                    AttributeModifier.Operation.ADDITION));
        }

        private void removeLuckPenalty(Player player) {
            AttributeInstance luck = player.getAttribute(Attributes.LUCK);
            if (luck != null) {
                luck.removeModifier(CURSE_2_LUCK_MODIFIER_ID);
            }
        }

        private static double formatTicksAsMinutes(int ticks) {
            return ticks / 1200.0D;
        }

        private static double formatTicksAsSeconds(int ticks) {
            return ticks / 20.0D;
        }

        private static double formatPercent(double value) {
            return value * 100.0D;
        }

        private static String formatNumber(double value) {
            if (Math.abs(value - Math.rint(value)) < 1.0E-6D) {
                return Integer.toString((int) Math.rint(value));
            }
            return String.format(Locale.ROOT, "%.1f", value);
        }

        private static Component redNumber(double value) {
            return Component.literal(formatNumber(value)).withStyle(ChatFormatting.RED);
        }

        private static Component redNumber(int value) {
            return Component.literal(Integer.toString(value)).withStyle(ChatFormatting.RED);
        }

        private static Component redNumber(String value) {
            return Component.literal(value).withStyle(ChatFormatting.RED);
        }

        private static Component redPercent(double value) {
            return Component.literal(formatNumber(formatPercent(value)) + "%").withStyle(ChatFormatting.RED);
        }

        private static Component redPercentInverse(double multiplier) {
            return Component.literal(formatNumber(multiplier * 100.0D) + "%").withStyle(ChatFormatting.RED);
        }
    }

    public static class PurificationScroll extends Item {

        public PurificationScroll() {
            super(new Item.Properties().stacksTo(16));
        }

        @Override
        public Component getName(ItemStack stack) {
            return super.getName(stack).copy().withStyle(ChatFormatting.GOLD);
        }

        @Override
        public boolean isFoil(ItemStack stack) {
            return true;
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
            ItemStack stack = player.getItemInHand(usedHand);

            if (!ModEvents.canUseSealBoundItem(player)) {
                return InteractionResultHolder.fail(stack);
            }

            if (!(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
            }

            int removed = ModEvents.consumePurificationScroll(serverPlayer);
            if (removed <= 0) {
                return InteractionResultHolder.fail(stack);
            }

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            player.displayClientMessage(
                    Component.translatable("message.ramesses_curse.purification_used", removed).withStyle(ChatFormatting.GOLD),
                    true);
            return InteractionResultHolder.success(stack);
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable("item.ramesses_curse.purification_scroll.desc").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("item.ramesses_curse.purification_scroll.effect", Component.literal(Integer.toString(Config.CURSE_5_PURIFICATION_AMOUNT.get())).withStyle(ChatFormatting.RED)).withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("item.ramesses_curse.requires_seal").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        }
    }

    public static class SatietyCrystal extends Item {

        public SatietyCrystal() {
            super(new Item.Properties().stacksTo(64).rarity(Rarity.RARE));
        }
    }
}
