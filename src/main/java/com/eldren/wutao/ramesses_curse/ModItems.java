package com.eldren.wutao.ramesses_curse;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.network.PacketDistributor;
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
    public static final RegistryObject<Item> EMBER_OF_DUAT =
            ITEMS.register("ember_of_duat", EmberOfDuat::new);
    public static final RegistryObject<Item> HEART_OF_DUAT =
            ITEMS.register("heart_of_duat", HeartOfDuat::new);

    public static int getHeartOfDuatCooldownTicks(Player player) {
        return HeartOfDuat.getCooldownTicks(player);
    }

    public static int getHeartOfDuatActiveTicks(Player player) {
        return HeartOfDuat.getActiveTicks(player);
    }

    public static int getHeartOfDuatBattleStacks(Player player) {
        if (player.level().isClientSide) {
            return ModEvents.ClientOverlayEvents.getClientHeartBattleStacks();
        }
        return HeartOfDuat.getBattleStacks(player);
    }

    public static boolean isHeartOfDuatInvulnerable(Player player) {
        return HeartOfDuat.isWardInvulnerable(player);
    }

    public static void onHeartOfDuatAttack(Player player, LivingEntity target, float baseDamage) {
        HeartOfDuat.onAttackEnemy(player, target, baseDamage);
    }

    public static boolean tryTriggerHeartOfDuatWard(ServerPlayer player) {
        return HeartOfDuat.tryTriggerWard(player);
    }

    public static boolean canActivateHeartAwakening(Player player) {
        return HeartOfDuat.canActivate(player);
    }

    public static void activateHeartAwakening(Player player) {
        HeartOfDuat.activate(player);
    }

    public static boolean tryActivateHeartWard(ServerPlayer player) {
        return HeartOfDuat.tryTriggerWard(player);
    }

    public static boolean isHeartWardReady(Player player) {
        if (player.level().isClientSide) {
            return ModEvents.ClientOverlayEvents.isClientHeartWardReady();
        }
        return HeartOfDuat.isWardReady(player);
    }

    public static void clearHeartWardCooldown(ServerPlayer player) {
        HeartOfDuat.clearWardCooldown(player);
    }

    public static void syncHeartState(ServerPlayer player) {
        HeartOfDuat.syncHeartState(player, HeartOfDuat.getHeartDataStatic(player));
    }

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
            tooltip.add(purpleText("一枚承载王朝威令与余烬的古老咒印，佩戴者将逐步承受九重诅咒。"));
            tooltip.add(purpleText("按住 ").append(blueText("Shift")).append(purpleText(" 查看当前诅咒状态")));
            tooltip.add(purpleText("按住 ").append(blueText("Ctrl")).append(purpleText(" 查看当前故事状态")));

            if (Screen.hasShiftDown()) {
                tooltip.add(Component.empty());
                tooltip.add(purpleText("九重诅咒"));
                tooltip.add(purpleText("- ")
                        .append(blueText("双神翻令"))
                        .append(purpleText("：每 "))
                        .append(yellowNumber(formatTicksAsMinutes(Config.RANDOM_CURSE_INTERVAL.get())))
                        .append(purpleText(" 分钟获得 "))
                        .append(yellowNumber(1))
                        .append(purpleText(" 个随机负面状态，持续 "))
                        .append(yellowNumber(formatTicksAsSeconds(Config.RANDOM_CURSE_DURATION.get())))
                        .append(purpleText(" 秒，强度为 "))
                        .append(yellowNumber(Config.RANDOM_CURSE_AMPLIFIER_MIN.get()))
                        .append(purpleText(" 到 "))
                        .append(yellowNumber(Math.max(Config.RANDOM_CURSE_AMPLIFIER_MIN.get(), Config.RANDOM_CURSE_AMPLIFIER_MAX.get())))
                        .append(purpleText(" 级。")));
                tooltip.add(purpleText("- ")
                        .append(blueText("摄政王冠"))
                        .append(purpleText("：持续降低 "))
                        .append(yellowNumber(formatNumber(Config.CURSE_2_LUCK_PENALTY.get())))
                        .append(purpleText(" 点幸运值，每 "))
                        .append(yellowNumber(formatTicksAsMinutes(Config.CURSE_2_SPAWN_INTERVAL.get())))
                        .append(purpleText(" 分钟有 "))
                        .append(yellowPercent(Config.CURSE_2_SPAWN_CHANCE.get()))
                        .append(purpleText(" 在半径 "))
                        .append(yellowNumber(Config.CURSE_2_SPAWN_RADIUS.get()))
                        .append(purpleText(" 格内召来 "))
                        .append(yellowNumber(1))
                        .append(purpleText(" 个敌对生物。")));
                tooltip.add(purpleText("- ")
                        .append(blueText("御名税印"))
                        .append(purpleText("：击杀任意生物获得经验变为 "))
                        .append(yellowPercentInverse(Config.CURSE_3_XP_MULTIPLIER.get()))
                        .append(purpleText("，附魔等级消耗变为 "))
                        .append(yellowNumber(formatNumber(Config.CURSE_3_ENCHANT_COST_MULTIPLIER.get())))
                        .append(purpleText(" 倍，铁砧等级消耗变为 "))
                        .append(yellowNumber(formatNumber(Config.CURSE_3_ANVIL_COST_MULTIPLIER.get())))
                        .append(purpleText(" 倍。")));
                tooltip.add(purpleText("- ")
                        .append(blueText("两地税契"))
                        .append(purpleText("：村民与流浪商人交易价格上升 "))
                        .append(yellowPercent(Config.CURSE_4_TRADE_SURCHARGE.get()))
                        .append(purpleText("。")));
                tooltip.add(purpleText("- ")
                        .append(blueText("无辜者天秤"))
                        .append(purpleText("：每击杀 "))
                        .append(yellowNumber(1))
                        .append(purpleText(" 个无辜生物增加 "))
                        .append(yellowNumber(1))
                        .append(purpleText(" 点王罪；达到 "))
                        .append(yellowNumber(Config.CURSE_5_MAX_GUILT.get()))
                        .append(purpleText(" 后，背包封禁 "))
                        .append(yellowNumber(formatTicksAsMinutes(Config.CURSE_5_LOCK_DURATION.get())))
                        .append(purpleText(" 分钟并降下 "))
                        .append(yellowNumber(Config.CURSE_5_LIGHTNING_STRIKES.get()))
                        .append(purpleText(" 道雷罚。")));
                tooltip.add(purpleText("- ")
                        .append(blueText("和约枷锁"))
                        .append(purpleText("：佩戴 "))
                        .append(yellowNumber(formatTicksAsMinutes(Config.MAIN_HAND_LOCK_DELAY.get())))
                        .append(purpleText(" 分钟后封主手 "))
                        .append(yellowNumber(formatTicksAsMinutes(Config.MAIN_HAND_LOCK_DURATION.get())))
                        .append(purpleText(" 分钟，此后每 "))
                        .append(yellowNumber(formatTicksAsMinutes(Config.INVENTORY_LOCK_INTERVAL.get())))
                        .append(purpleText(" 分钟随机封锁 "))
                        .append(yellowNumber(1))
                        .append(purpleText(" 件物品，持续 "))
                        .append(yellowNumber(formatTicksAsMinutes(Config.INVENTORY_LOCK_DURATION.get())))
                        .append(purpleText(" 分钟。")));
                tooltip.add(purpleText("- ")
                        .append(blueText("末地审判"))
                        .append(purpleText("：身处末地时持续获得虚弱 "))
                        .append(yellowNumber(Config.END_WEAKNESS_AMPLIFIER.get() + 1))
                        .append(purpleText(" 级。")));
                tooltip.add(purpleText("- ")
                        .append(blueText("地狱烙印"))
                        .append(purpleText("：首次进入地狱后永久生效，攻击怪物时有 "))
                        .append(yellowPercent(Config.HELL_REBOUND_CHANCE.get()))
                        .append(purpleText(" 反噬自身，并额外承受 "))
                        .append(yellowPercent(Config.HELL_REBOUND_DAMAGE_MULTIPLIER.get() - 1.0D))
                        .append(purpleText(" 伤害。")));
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.player != null) {
                    CompoundTag persisted = minecraft.player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
                    CompoundTag curseData = persisted.getCompound("RamessesCurseData");
                    if (curseData.getBoolean("NetherAwakened")) {
                        tooltip.add(Component.literal("已踏足该维度诅咒已生效...")
                                .withStyle(ChatFormatting.RED));
                    }
                }
                tooltip.add(purpleText("- ")
                        .append(blueText("饥王遗罚"))
                        .append(purpleText("：每次死亡失去 "))
                        .append(blueText("2 点饱食度"))
                        .append(purpleText("，并在死亡处上方生成 "))
                        .append(blueText("1 枚饱食水晶"))
                        .append(purpleText("。拾取后返还失去的饱食度。")));
            }

            if (Screen.hasControlDown()) {
                tooltip.add(Component.empty());
                tooltip.add(blueText("王朝秘闻"));
                for (int i = 1; i <= 5; i++) {
                    tooltip.add(Component.translatable("item.ramesses_curse.seal_of_kadesh.story_" + i).withStyle(ChatFormatting.DARK_PURPLE));
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

        private static MutableComponent purpleText(String text) {
            return Component.literal(text).withStyle(ChatFormatting.DARK_PURPLE);
        }

        private static MutableComponent blueText(String text) {
            return Component.literal(text).withStyle(ChatFormatting.AQUA);
        }

        private static MutableComponent yellowNumber(double value) {
            return Component.literal(formatNumber(value)).withStyle(ChatFormatting.YELLOW);
        }

        private static MutableComponent yellowNumber(int value) {
            return Component.literal(Integer.toString(value)).withStyle(ChatFormatting.YELLOW);
        }

        private static MutableComponent yellowNumber(String value) {
            return Component.literal(value).withStyle(ChatFormatting.YELLOW);
        }

        private static MutableComponent yellowPercent(double value) {
            return Component.literal(formatNumber(formatPercent(value)) + "%").withStyle(ChatFormatting.YELLOW);
        }

        private static MutableComponent yellowPercentInverse(double multiplier) {
            return Component.literal(formatNumber(multiplier * 100.0D) + "%").withStyle(ChatFormatting.YELLOW);
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

    public static class EmberOfDuat extends Item {

        public EmberOfDuat() {
            super(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON));
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable("item.ramesses_curse.ember_of_duat.chance").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        }
    }

    public static class HeartOfDuat extends Item implements ICurioItem {
        private static final String HEART_DATA_KEY = "RamessesHeartOfDuat";
        private static final String CROWD_ACTIVE_UNTIL_KEY = "CrowdActiveUntil";
        private static final String CROWD_LAST_PROMPT_KEY = "CrowdLastPrompt";
        private static final String BATTLE_STACKS_KEY = "BattleStacks";
        private static final String LAST_STACK_TIME_KEY = "LastStackTime";
        private static final String WARD_READY_KEY = "WardReady";
        private static final String WARD_COOLDOWN_KEY = "WardCooldownUntil";
        private static final String WARD_INVULNERABLE_UNTIL_KEY = "WardInvulnerableUntil";
        private static final UUID HEART_BLOCK_REACH_MODIFIER_ID = UUID.fromString("b386ce8b-a852-47cf-bc77-a06f67b5811e");
        private static final UUID HEART_ENTITY_REACH_MODIFIER_ID = UUID.fromString("74f4aa7b-d364-46aa-aa89-90f62db0a644");

        public HeartOfDuat() {
            super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
        }

        @Override
        public boolean canEquip(SlotContext slotContext, ItemStack stack) {
            LivingEntity wearer = slotContext.entity();
            return RamessesCurseMod.HEART_SLOT_ID.equals(slotContext.identifier())
                    && wearer instanceof Player player
                    && ModEvents.canUseHeartSlot(player);
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("-主动技能：玩家周围")
                    .withStyle(ChatFormatting.DARK_PURPLE)
                    .append(Component.literal("16").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("格敌对实体").withStyle(ChatFormatting.DARK_PURPLE))
                    .append(Component.literal("≥6").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("时，给予玩家").withStyle(ChatFormatting.DARK_PURPLE))
                    .append(Component.literal("力量III").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("并").withStyle(ChatFormatting.DARK_PURPLE))
                    .append(Component.literal("高亮敌对实体").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("，每次开启消耗").withStyle(ChatFormatting.DARK_PURPLE))
                    .append(Component.literal("3").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("级经验。").withStyle(ChatFormatting.DARK_PURPLE)));
            tooltip.add(Component.literal("-被动I：玩家处于")
                    .withStyle(ChatFormatting.DARK_PURPLE)
                    .append(Component.literal("沙漠").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("、").withStyle(ChatFormatting.DARK_PURPLE))
                    .append(Component.literal("恶地").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("时，给予玩家").withStyle(ChatFormatting.DARK_PURPLE))
                    .append(Component.literal("幸运II").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("，").withStyle(ChatFormatting.DARK_PURPLE))
                    .append(Component.literal("+5实体距离").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("。").withStyle(ChatFormatting.DARK_PURPLE)));
            tooltip.add(Component.literal("-被动II：击杀敌对怪物时叠加")
                    .withStyle(ChatFormatting.DARK_PURPLE)
                    .append(Component.literal("1/10").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("层战意，间隔超过").withStyle(ChatFormatting.DARK_PURPLE))
                    .append(Component.literal("5").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("秒将重置；每层").withStyle(ChatFormatting.DARK_PURPLE))
                    .append(Component.literal("+3").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("攻击，最高").withStyle(ChatFormatting.DARK_PURPLE))
                    .append(Component.literal("+30").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("；叠满").withStyle(ChatFormatting.DARK_PURPLE))
                    .append(Component.literal("10").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("层后立即获得").withStyle(ChatFormatting.DARK_PURPLE))
                    .append(Component.literal("5").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("秒免伤。触发后消耗").withStyle(ChatFormatting.DARK_PURPLE))
                    .append(Component.literal("1").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("个杜阿特余烬并进入").withStyle(ChatFormatting.DARK_PURPLE))
                    .append(Component.literal("360").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("秒冷却。").withStyle(ChatFormatting.DARK_PURPLE)));

            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null) {
                return;
            }

            int activeTicks = getActiveTicks(minecraft.player);
            if (activeTicks > 0) {
                float seconds = activeTicks / 20.0F;
                tooltip.add(Component.translatable("hud.ramesses_curse.heart_active", String.format(Locale.ROOT, "%.1f", seconds))
                        .withStyle(ChatFormatting.GOLD));
            }

            int cooldownTicks = getCooldownTicks(minecraft.player);
            if (cooldownTicks > 0) {
                float seconds = cooldownTicks / 20.0F;
                tooltip.add(Component.translatable("hud.ramesses_curse.heart_cooldown", String.format(Locale.ROOT, "%.1f", seconds))
                        .withStyle(ChatFormatting.GRAY));
            }

            if (isWardReady(minecraft.player)) {
                tooltip.add(Component.translatable("message.ramesses_curse.heart_ward_ready")
                        .withStyle(ChatFormatting.GOLD));
            }
        }

        @Override
        public void curioTick(SlotContext slotContext, ItemStack stack) {
            LivingEntity wearer = slotContext.entity();
            if (!(wearer instanceof Player player) || player.level().isClientSide) {
                return;
            }

            updateCrowdBattlefieldState(player);
            applyDesertRegencyEffect(player);
            if (player instanceof ServerPlayer serverPlayer) {
                syncHeartState(serverPlayer, getHeartData(serverPlayer));
            }
        }

        @Override
        public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
            if (slotContext.entity() instanceof Player player) {
                removeReachBonus(player);
            }
        }

        private void updateCrowdBattlefieldState(Player player) {
            long gameTime = player.level().getGameTime();
            CompoundTag data = getHeartData(player);
            long activeUntil = data.getLong(CROWD_ACTIVE_UNTIL_KEY);

            if (activeUntil > 0L && gameTime >= activeUntil) {
                data.putLong(CROWD_ACTIVE_UNTIL_KEY, 0L);
                player.level().playSound(null, player.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 0.9F);
                player.displayClientMessage(Component.translatable("message.ramesses_curse.heart_awakening_off").withStyle(ChatFormatting.GRAY), true);
            }

            if (data.getLong(CROWD_ACTIVE_UNTIL_KEY) > gameTime || player.experienceLevel < getActiveXpCost() || !hasEnoughHostilesNearby(player)) {
                return;
            }

            if (gameTime - data.getLong(CROWD_LAST_PROMPT_KEY) < Config.HEART_PROMPT_INTERVAL.get()) {
                return;
            }

            data.putLong(CROWD_LAST_PROMPT_KEY, gameTime);
            player.displayClientMessage(Component.translatable("message.ramesses_curse.heart_awakening_ready").withStyle(ChatFormatting.YELLOW), true);
        }

        private static boolean canActivate(Player player) {
            CompoundTag data = getHeartDataStatic(player);
            long gameTime = player.level().getGameTime();
            if (data.getLong(CROWD_ACTIVE_UNTIL_KEY) > gameTime) {
                return false;
            }
            return player.experienceLevel >= getActiveXpCost() && hasEnoughHostilesNearby(player);
        }

        public static boolean hasActivationXp(Player player) {
            return player.experienceLevel >= getActiveXpCost();
        }

        private static void activate(Player player) {
            if (!canActivate(player)) {
                return;
            }

            long gameTime = player.level().getGameTime();
            CompoundTag data = getHeartDataStatic(player);
            List<LivingEntity> nearby = getNearbyHostiles(player);
            player.giveExperienceLevels(-getActiveXpCost());
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, Config.HEART_ACTIVE_DURATION.get(), 2, false, false, true));
            for (LivingEntity entity : nearby) {
                entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, Config.HEART_ACTIVE_DURATION.get(), 0, false, false, true));
            }
            data.putLong(CROWD_ACTIVE_UNTIL_KEY, gameTime + Config.HEART_ACTIVE_DURATION.get());
            player.level().playSound(null, player.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.1F);
            player.displayClientMessage(Component.translatable("message.ramesses_curse.heart_awakening_on").withStyle(ChatFormatting.GOLD), true);
        }

        private void applyDesertRegencyEffect(Player player) {
            if (isDesertOrBadlands(player.level(), player.blockPosition())) {
                addReachBonus(player);
                player.addEffect(new MobEffectInstance(MobEffects.LUCK, 220, 1, false, false, true));
                return;
            }

            removeReachBonus(player);
            player.removeEffect(MobEffects.LUCK);
        }

        private void addReachBonus(Player player) {
            applyReachModifier(player, ForgeMod.BLOCK_REACH.get(), HEART_BLOCK_REACH_MODIFIER_ID);
            applyReachModifier(player, ForgeMod.ENTITY_REACH.get(), HEART_ENTITY_REACH_MODIFIER_ID);
        }

        private void removeReachBonus(Player player) {
            removeReachModifier(player, ForgeMod.BLOCK_REACH.get(), HEART_BLOCK_REACH_MODIFIER_ID);
            removeReachModifier(player, ForgeMod.ENTITY_REACH.get(), HEART_ENTITY_REACH_MODIFIER_ID);
        }

        private void applyReachModifier(Player player, net.minecraft.world.entity.ai.attributes.Attribute attribute, UUID id) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance == null || instance.getModifier(id) != null) {
                return;
            }

            instance.addTransientModifier(new AttributeModifier(
                    id,
                    "heart_of_duat_reach_bonus",
                    Config.HEART_DESERT_REACH_BONUS.get(),
                    AttributeModifier.Operation.ADDITION));
        }

        private void removeReachModifier(Player player, net.minecraft.world.entity.ai.attributes.Attribute attribute, UUID id) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) {
                instance.removeModifier(id);
            }
        }

        private boolean isDesertOrBadlands(Level level, BlockPos pos) {
            return level.getBiome(pos).is(Biomes.DESERT)
                    || level.getBiome(pos).is(Biomes.BADLANDS)
                    || level.getBiome(pos).is(Biomes.ERODED_BADLANDS)
                    || level.getBiome(pos).is(Biomes.WOODED_BADLANDS);
        }

        private static List<LivingEntity> getNearbyHostiles(Player player) {
            return player.level().getEntitiesOfClass(
                    LivingEntity.class,
                    AABB.ofSize(player.position(), Config.HEART_TRIGGER_RADIUS.get() * 2.0D, Config.HEART_TRIGGER_RADIUS.get() * 2.0D, Config.HEART_TRIGGER_RADIUS.get() * 2.0D),
                    entity -> entity.isAlive() && entity != player && entity instanceof Enemy);
        }

        private static boolean hasEnoughHostilesNearby(Player player) {
            return getNearbyHostiles(player).size() >= Config.HEART_TRIGGER_HOSTILE_COUNT.get();
        }

        private CompoundTag getHeartData(Player player) {
            return getHeartDataStatic(player);
        }

        private static CompoundTag getHeartDataStatic(Player player) {
            CompoundTag persistent = player.getPersistentData();
            if (!persistent.contains(Player.PERSISTED_NBT_TAG)) {
                persistent.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
            }

            CompoundTag persisted = persistent.getCompound(Player.PERSISTED_NBT_TAG);
            if (!persisted.contains(HEART_DATA_KEY)) {
                persisted.put(HEART_DATA_KEY, new CompoundTag());
            }
            return persisted.getCompound(HEART_DATA_KEY);
        }

        private static int getCooldownTicks(Player player) {
            if (player.level().isClientSide) {
                return ModEvents.ClientOverlayEvents.getClientHeartCooldownTicks();
            }
            CompoundTag heartData = getHeartDataStatic(player);
            long cooldownUntil = heartData.getLong(WARD_COOLDOWN_KEY);
            return (int) Math.max(0L, cooldownUntil - player.level().getGameTime());
        }

        private static int getActiveTicks(Player player) {
            CompoundTag heartData = getHeartDataStatic(player);
            long activeUntil = heartData.getLong(CROWD_ACTIVE_UNTIL_KEY);
            return (int) Math.max(0L, activeUntil - player.level().getGameTime());
        }

        private static int getBattleStacks(Player player) {
            return Mth.clamp(getHeartDataStatic(player).getInt(BATTLE_STACKS_KEY), 0, getBattleStackLimit());
        }

        private static boolean isWardInvulnerable(Player player) {
            return getHeartDataStatic(player).getLong(WARD_INVULNERABLE_UNTIL_KEY) > player.level().getGameTime();
        }

        private static void onAttackEnemy(Player player, LivingEntity target, float baseDamage) {
            if (!(target instanceof Enemy)) {
                return;
            }
        }

        private static boolean tryTriggerWard(ServerPlayer player) {
            CompoundTag heartData = getHeartDataStatic(player);
            long gameTime = player.level().getGameTime();
            if (!heartData.getBoolean(WARD_READY_KEY) || heartData.getLong(WARD_COOLDOWN_KEY) > gameTime) {
                return false;
            }

            if (!consumeEmber(player)) {
                player.displayClientMessage(Component.translatable("message.ramesses_curse.heart_ember_missing").withStyle(ChatFormatting.RED), true);
                resetBattleStacks(heartData);
                return false;
            }

            resetBattleStacks(heartData);
            heartData.putLong(WARD_COOLDOWN_KEY, gameTime + Config.HEART_WARD_COOLDOWN.get());
            heartData.putLong(WARD_INVULNERABLE_UNTIL_KEY, gameTime + Config.HEART_WARD_INVULNERABILITY.get());
            player.getCooldowns().addCooldown(ModItems.HEART_OF_DUAT.get(), Config.HEART_WARD_COOLDOWN.get());
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new HeartWardTriggeredPacket());
            syncHeartState(player, heartData);
            return true;
        }

        private static boolean consumeEmber(ServerPlayer player) {
            Inventory inventory = player.getInventory();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack stack = inventory.getItem(i);
                if (stack.is(ModItems.EMBER_OF_DUAT.get())) {
                    stack.shrink(1);
                    return true;
                }
            }
            return false;
        }

        private static boolean hasEmber(Player player) {
            Inventory inventory = player.getInventory();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                if (inventory.getItem(i).is(ModItems.EMBER_OF_DUAT.get())) {
                    return true;
                }
            }
            return false;
        }

        public static float getDamageBonus(Player player) {
            return (float) (getBattleStacks(player) * Config.HEART_BATTLE_DAMAGE_PER_STACK.get());
        }

        public static void onKillEnemy(ServerPlayer player, LivingEntity target) {
            if (!(target instanceof Enemy)) {
                return;
            }

            CompoundTag heartData = getHeartDataStatic(player);
            long gameTime = player.level().getGameTime();
            if (heartData.getBoolean(WARD_READY_KEY)) {
                if (!Config.HEART_WARD_MANUAL_TRIGGER.get()) {
                    tryTriggerWard(player);
                }
                return;
            }

            if (getCooldownTicks(player) > 0) {
                return;
            }

            long lastStackTime = heartData.getLong(LAST_STACK_TIME_KEY);
            int stackLimit = getBattleStackLimit();
            int stacks = gameTime - lastStackTime > Config.HEART_BATTLE_STACK_TIMEOUT.get() ? 0 : Mth.clamp(heartData.getInt(BATTLE_STACKS_KEY), 0, stackLimit);
            if (stacks < stackLimit) {
                stacks++;
            }

            heartData.putInt(BATTLE_STACKS_KEY, stacks);
            heartData.putLong(LAST_STACK_TIME_KEY, gameTime);
            player.displayClientMessage(Component.translatable("message.ramesses_curse.heart_battle_stacks", stacks, stackLimit)
                    .withStyle(ChatFormatting.GOLD), true);
            player.level().playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.7F, 1.2F);

            if (stacks >= stackLimit) {
                if (!hasEmber(player)) {
                    player.displayClientMessage(Component.translatable("message.ramesses_curse.heart_ember_missing").withStyle(ChatFormatting.RED), true);
                    resetBattleStacks(heartData);
                    syncHeartState(player, heartData);
                    return;
                }

                heartData.putBoolean(WARD_READY_KEY, true);
                if (Config.HEART_WARD_MANUAL_TRIGGER.get()) {
                    player.displayClientMessage(Component.translatable("message.ramesses_curse.heart_ward_prompt")
                            .withStyle(ChatFormatting.GOLD), true);
                } else {
                    tryTriggerWard(player);
                }
            }

            syncHeartState(player, heartData);
        }

        private static boolean isWardReady(Player player) {
            return getHeartDataStatic(player).getBoolean(WARD_READY_KEY);
        }

        private static void clearWardCooldown(ServerPlayer player) {
            CompoundTag heartData = getHeartDataStatic(player);
            heartData.putLong(WARD_COOLDOWN_KEY, 0L);
            player.getCooldowns().removeCooldown(ModItems.HEART_OF_DUAT.get());
            syncHeartState(player, heartData);
        }

        private static void resetBattleStacks(CompoundTag heartData) {
            heartData.putBoolean(WARD_READY_KEY, false);
            heartData.putInt(BATTLE_STACKS_KEY, 0);
            heartData.putLong(LAST_STACK_TIME_KEY, 0L);
        }

        private static void syncHeartState(ServerPlayer player, CompoundTag heartData) {
            int battleStacks = Mth.clamp(heartData.getInt(BATTLE_STACKS_KEY), 0, getBattleStackLimit());
            int cooldownTicks = (int) Math.max(0L, heartData.getLong(WARD_COOLDOWN_KEY) - player.level().getGameTime());
            boolean wardReady = heartData.getBoolean(WARD_READY_KEY);
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new HeartStateSyncPacket(battleStacks, cooldownTicks, wardReady));
        }

        private static int getBattleStackLimit() {
            return Math.max(1, Config.HEART_BATTLE_STACK_LIMIT.get());
        }

        private static int getActiveXpCost() {
            return Math.max(0, Config.HEART_ACTIVE_XP_COST.get());
        }
    }
}
