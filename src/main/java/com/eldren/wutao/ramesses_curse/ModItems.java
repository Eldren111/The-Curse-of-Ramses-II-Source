package com.eldren.wutao.ramesses_curse;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import vazkii.patchouli.api.PatchouliAPI;

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
    public static final RegistryObject<Item> HYMN_OF_THE_TWO_LANDS =
            ITEMS.register("hymn_of_the_two_lands", HymnOfTheTwoLands::new);
    public static final RegistryObject<Item> RESONANT_SCARAB =
            ITEMS.register("resonant_scarab", ResonantScarab::new);
    public static final RegistryObject<Item> URAEUS_NECKLACE =
            ITEMS.register("uraeus_necklace", UraeusNecklace::new);
    public static final RegistryObject<Item> EDICT_OF_TWO_LANDS =
            ITEMS.register("edict_of_two_lands", EdictOfTwoLands::new);
    public static final RegistryObject<Item> RAMASSES_MANUAL =
            ITEMS.register("ramesses_manual", RamessesManual::new);

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

    public static void tickMusicRelic(Player player) {
        ResonantScarab.tickRelic(player);
    }

    public static boolean isMusicRelicAwakened(Player player) {
        return ResonantScarab.isAwakened(player);
    }

    public static boolean hasMusicAttackModifier(Player player) {
        return ResonantScarab.hasAttackModifier(player);
    }

    public static float getMusicAttackMultiplier() {
        return ResonantScarab.getAttackMultiplier();
    }

    public static float onUraeusNecklaceAttack(Player player, LivingEntity target, float damage) {
        return UraeusNecklace.onAttack(player, target, damage);
    }

    public static boolean isEdictOfTwoLandsEquipped(Player player) {
        return EdictOfTwoLands.isEquipped(player);
    }

    public static boolean tryActivateEdictOfTwoLands(ServerPlayer player) {
        return EdictOfTwoLands.tryActivate(player);
    }

    public static int getEdictOfTwoLandsCooldownTicks(Player player) {
        return EdictOfTwoLands.getCooldownTicks(player);
    }
    public static void clearEdictCooldown(ServerPlayer player) {
        EdictOfTwoLands.clearCooldown(player);
    }
    public static EdictActivationResult tryActivateEdictOfTwoLandsDomain(ServerPlayer player) {
        return EdictOfTwoLands.tryActivateDomain(player);
    }
    public static int getEdictOfTwoLandsDomainCooldownTicks(Player player) {
        return EdictOfTwoLands.getDomainCooldownTicks(player);
    }
    public static boolean isEdictOfTwoLandsDomainActive(Player player) {
        return EdictOfTwoLands.isDomainActive(player);
    }
    public enum EdictActivationResult {
        SUCCESS,
        NOT_EQUIPPED,
        COOLDOWN,
        NO_GUILT,
        NO_TARGETS,
        OTHER_MODE_ACTIVE
    }

    public static class RamessesManual extends Item {
        private static final ResourceLocation BOOK_ID = new ResourceLocation(RamessesCurseMod.MODID, "ember_of_duat_codex");

        public RamessesManual() {
            super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
            ItemStack stack = player.getItemInHand(usedHand);
            if (level.isClientSide) {
                if (!ModList.get().isLoaded("patchouli") || PatchouliAPI.get().isStub()) {
                    player.displayClientMessage(Component.translatable("message.ramesses_curse.manual_patchouli_missing").withStyle(ChatFormatting.RED), true);
                    return InteractionResultHolder.fail(stack);
                }

                PatchouliAPI.get().openBookGUI(BOOK_ID);
                return InteractionResultHolder.sidedSuccess(stack, true);
            }

            return InteractionResultHolder.sidedSuccess(stack, false);
        }
    }

    public static class SealOfKadesh extends Item implements ICurioItem {
        private static final UUID CURSE_2_LUCK_MODIFIER_ID = UUID.fromString("d5b8dc2a-6541-46b2-8d8a-f642f6f40b91");

        public SealOfKadesh() {
            super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
        }

        @Override
        public boolean canEquip(SlotContext slotContext, ItemStack stack) {
            LivingEntity wearer = slotContext.entity();
            return RamessesCurseMod.CURSE_SLOT_ID.equals(slotContext.identifier())
                    && wearer instanceof Player player
                    && !ModEvents.hasSealAnywhere(player);
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
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(purpleText("一枚承载王朝威令与余烬的古老咒印，佩戴者将逐步承受九重诅咒。"));
            tooltip.add(purpleText("按住 ").append(blueText("Shift")).append(purpleText(" 查看当前诅咒状态")));
            tooltip.add(purpleText("按住 ").append(blueText("Ctrl")).append(purpleText(" 查看当前故事状态")));
            Minecraft minecraft = Minecraft.getInstance();
            boolean curseEightReversed = minecraft.player != null
                    && ModEvents.isNetherAwakened(minecraft.player)
                    && ModEvents.isCurseEightReversed(minecraft.player);
            if (minecraft.player != null) {
                if (curseEightReversed) {
                    tooltip.add(Component.translatable("item.ramesses_curse.seal_of_kadesh.nether_reversed")
                            .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
                } else if (ModEvents.isNetherAwakened(minecraft.player)) {
                    tooltip.add(Component.translatable("item.ramesses_curse.seal_of_kadesh.nether_awakened")
                            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                }
            }
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
                MutableComponent curseTwoLine = purpleText("- ").append(blueText("摄政王冠"));
                if (ModEvents.isCurseTwoReversed(minecraft.player)) {
                    curseTwoLine.append(purpleText("：反转后，冠影不再只夺走幸运，而会补回 "))
                            .append(yellowNumber(formatNumber(Config.CURSE_2_REVERSED_LUCK_BONUS.get())))
                            .append(purpleText(" 点。每 "))
                            .append(yellowNumber(formatTicksAsMinutes(Config.CURSE_2_REVERSED_SPAWN_INTERVAL.get())))
                            .append(purpleText(" 分钟有 "))
                            .append(yellowPercent(Config.CURSE_2_REVERSED_SPAWN_CHANCE.get()))
                            .append(purpleText(" 的机会遣下一名短暂听命的旧臣。"));
                } else {
                    curseTwoLine.append(purpleText("：持续降低 "))
                            .append(yellowNumber(formatNumber(Config.CURSE_2_LUCK_PENALTY.get())))
                            .append(purpleText(" 点幸运值，每 "))
                            .append(yellowNumber(formatTicksAsMinutes(Config.CURSE_2_SPAWN_INTERVAL.get())))
                            .append(purpleText(" 分钟有 "))
                            .append(yellowPercent(Config.CURSE_2_SPAWN_CHANCE.get()))
                            .append(purpleText(" 在半径 "))
                            .append(yellowNumber(Config.CURSE_2_SPAWN_RADIUS.get()))
                            .append(purpleText(" 格内召来 "))
                            .append(yellowNumber(1))
                            .append(purpleText(" 个敌对生物。"));
                }
                tooltip.add(curseTwoLine);
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
                MutableComponent curseEightLine = purpleText("- ").append(blueText("地狱烙印"));
                if (curseEightReversed) {
                    curseEightLine.append(purpleText("：反转后，主手的击打有 "))
                            .append(yellowPercent(Config.HELL_REBOUND_CHANCE.get()))
                            .append(purpleText(" 的概率将同等伤害助推回去。"));
                } else {
                    curseEightLine.append(purpleText("：首次进入地狱后永久生效，攻击怪物时有 "))
                            .append(yellowPercent(Config.HELL_REBOUND_CHANCE.get()))
                            .append(purpleText(" 反噬自身，承受本次伤害并额外追加 "))
                            .append(yellowPercent(Config.HELL_REBOUND_DAMAGE_MULTIPLIER.get() - 1.0D))
                            .append(purpleText(" 伤害。"));
                }
                tooltip.add(curseEightLine);
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
        private static void applyLuckPenalty(Player player) {
            AttributeInstance luck = player.getAttribute(Attributes.LUCK);
            if (luck == null) {
                return;
            }

            double amount = ModEvents.isCurseTwoReversed(player)
                    ? Config.CURSE_2_REVERSED_LUCK_BONUS.get()
                    : -Config.CURSE_2_LUCK_PENALTY.get();
            AttributeModifier existing = luck.getModifier(CURSE_2_LUCK_MODIFIER_ID);
            if (existing != null) {
                if (Math.abs(existing.getAmount() - amount) < 1.0E-6D) {
                    return;
                }
                luck.removeModifier(CURSE_2_LUCK_MODIFIER_ID);
            }

            luck.addTransientModifier(new AttributeModifier(
                    CURSE_2_LUCK_MODIFIER_ID,
                    "seal_of_kadesh_curse_2_luck",
                    amount,
                    AttributeModifier.Operation.ADDITION));
        }

        private static void removeLuckPenalty(Player player) {
            AttributeInstance luck = player.getAttribute(Attributes.LUCK);
            if (luck != null) {
                luck.removeModifier(CURSE_2_LUCK_MODIFIER_ID);
            }
        }

        public static void tickSealCurses(ServerPlayer player, int equipTicks) {
            applyLuckPenalty(player);
            applyFavorOrDamnation(player, equipTicks);
            applyCrownOfRegency(player, equipTicks);
        }

        public static void clearSealCurses(Player player) {
            removeLuckPenalty(player);
        }

        private static void applyFavorOrDamnation(ServerPlayer player, int equipTicks) {
            int interval = Config.RANDOM_CURSE_INTERVAL.get();
            if (equipTicks <= 0 || equipTicks % interval != 0) {
                return;
            }

            MobEffect effect = Config.getRandomNegativeEffect(player.getRandom().nextInt());

            if (effect == null || effect == MobEffects.DARKNESS || effect == MobEffects.DIG_SLOWDOWN || effect == MobEffects.CONFUSION) {
                return;
            }

            int minAmp = Config.RANDOM_CURSE_AMPLIFIER_MIN.get();
            int maxAmp = Math.max(minAmp, Config.RANDOM_CURSE_AMPLIFIER_MAX.get());
            int amplifier = minAmp + player.getRandom().nextInt(maxAmp - minAmp + 1);
            player.addEffect(new MobEffectInstance(effect, Config.RANDOM_CURSE_DURATION.get(), amplifier, false, false, true));
        }

        private static void applyCrownOfRegency(ServerPlayer player, int equipTicks) {
            boolean reversed = ModEvents.isCurseTwoReversed(player);
            int interval = reversed
                    ? Config.CURSE_2_REVERSED_SPAWN_INTERVAL.get()
                    : Config.CURSE_2_SPAWN_INTERVAL.get();
            if (equipTicks <= 0 || equipTicks % interval != 0) {
                return;
            }

            double chance = reversed
                    ? Config.CURSE_2_REVERSED_SPAWN_CHANCE.get()
                    : Config.CURSE_2_SPAWN_CHANCE.get();
            if (player.getRandom().nextDouble() >= chance) {
                return;
            }

            if (reversed) {
                ModEvents.spawnCurseTwoMinion(player);
                return;
            }

            ModEvents.spawnCurseTwoMob(player);
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
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
            ItemStack stack = player.getItemInHand(usedHand);
            if (level.isClientSide) {
                if (!ModEvents.ClientOverlayEvents.isClientEmberRiddleUnlocked()) {
                    Minecraft.getInstance().setScreen(new EmberRiddleInputScreen());
                }
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
            }
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResultHolder.sidedSuccess(stack, false);
            }

            if (!ModEvents.canUseEmberOfDuat(player)) {
                return InteractionResultHolder.fail(stack);
            }

            ModEvents.EmberTrialActivationResult result = ModEvents.tryActivateEmberTrial(serverPlayer);
            if (result == ModEvents.EmberTrialActivationResult.ALREADY_ACTIVE) {
                player.displayClientMessage(Component.translatable("message.ramesses_curse.ember_trial_active").withStyle(ChatFormatting.RED), false);
                return InteractionResultHolder.fail(stack);
            }
            if (result == ModEvents.EmberTrialActivationResult.RIDDLE_LOCKED) {
                return InteractionResultHolder.fail(stack);
            }

            player.displayClientMessage(Component.translatable("message.ramesses_curse.ember_army_pending").withStyle(ChatFormatting.GOLD), false);
            return InteractionResultHolder.success(stack);
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable("item.ramesses_curse.ember_of_duat.chance").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
            tooltip.add(Component.translatable("item.ramesses_curse.ember_of_duat.riddle").withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    public static class HymnOfTheTwoLands extends RecordItem {

        public HymnOfTheTwoLands() {
            super(14, ModSounds.MUSIC_DISC_HYMN_OF_THE_TWO_LANDS.get(), new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 60);
        }

        @Override
        public MutableComponent getDisplayName() {
            return Component.translatable(this.getDescriptionId());
        }

        @Override
        public InteractionResult useOn(UseOnContext context) {
            Player player = context.getPlayer();
            if (player != null && !ModEvents.isMusicRelicEquipped(player)) {
                if (!context.getLevel().isClientSide) {
                    player.displayClientMessage(Component.translatable("message.ramesses_curse.music_relic_required").withStyle(ChatFormatting.DARK_PURPLE), true);
                }
                return InteractionResult.FAIL;
            }
            if (player != null && ResonantScarab.isAwakened(player)) {
                return InteractionResult.FAIL;
            }

            InteractionResult result = super.useOn(context);
            if (!result.consumesAction()) {
                return result;
            }

            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            if (!level.isClientSide
                    && level.getBlockState(pos).is(Blocks.JUKEBOX)
                    && level.getBlockState(pos).hasProperty(JukeboxBlock.HAS_RECORD)
                    && level.getBlockState(pos).getValue(JukeboxBlock.HAS_RECORD)) {
                ModEvents.registerMusicDiscPlayback(level, pos, getLengthInTicks());
            }
            return result;
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable("item.ramesses_curse.hymn_of_the_two_lands.desc").withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    public static class ResonantScarab extends Item implements ICurioItem {
        private static final String MUSIC_DATA_KEY = "RamessesMusicRelic";
        private static final String ACTIVATED_KEY = "Activated";
        private static final String LAST_SYNC_ACTIVATED_KEY = "LastSyncActivated";
        private static final String LAST_JUKEBOX_KEY = "LastJukebox";
        private static final UUID MUSIC_ATTACK_MODIFIER_ID = UUID.fromString("7f29fc7c-ff31-4a60-9b81-379b5b5f6cc1");
        private static final UUID MUSIC_ARMOR_MODIFIER_ID = UUID.fromString("4ce69522-f43a-47c0-8a5b-21608e91711d");
        private static final UUID MUSIC_BLOCK_REACH_MODIFIER_ID = UUID.fromString("c19a9385-c1d7-4c0b-9ce0-d857791c38ce");
        private static final UUID MUSIC_ENTITY_REACH_MODIFIER_ID = UUID.fromString("3eeb5184-4fb6-48e1-83bb-ff040f235418");
        private static final double BONUS_ATTACK = 0.2D;
        private static final double BONUS_ARMOR = 0.15D;
        private static final double BONUS_REACH = 3.0D;
        private static final int DETECTION_RADIUS = 8;
        private static boolean clientAwakened;

        public ResonantScarab() {
            super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
        }

        @Override
        public boolean canEquip(SlotContext slotContext, ItemStack stack) {
            return RamessesCurseMod.MUSIC_SLOT_ID.equals(slotContext.identifier()) && slotContext.entity() instanceof Player;
        }

        @Override
        public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
            return true;
        }

        @Override
        public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
            if (!newStack.isEmpty() && newStack.is(stack.getItem())) {
                return;
            }
            if (slotContext.entity() instanceof ServerPlayer player) {
                boolean wasAwakened = isAwakened(player);
                ModEvents.cancelNearbyTrackedMusicDisc(player, DETECTION_RADIUS);
                removeBuffs(player);
                resetActivation(player);
                player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
                if (wasAwakened) {
                    player.displayClientMessage(Component.translatable("message.ramesses_curse.music_relic_removed").withStyle(ChatFormatting.RED), true);
                }
            }
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable("item.ramesses_curse.resonant_scarab.desc").withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(Component.translatable("item.ramesses_curse.resonant_scarab.effect_1").withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(Component.translatable("item.ramesses_curse.resonant_scarab.effect_2").withStyle(ChatFormatting.DARK_PURPLE));
            if (isClientAwakened(level)) {
                tooltip.add(Component.translatable("item.ramesses_curse.resonant_scarab.awakened").withStyle(ChatFormatting.GOLD));
            }
        }

        @Override
        public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            if (RamessesCurseMod.MUSIC_SLOT_ID.equals(slotContext.identifier())
                    && slotContext.entity() instanceof Player player
                    && isAwakened(player)) {
                builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(MUSIC_ATTACK_MODIFIER_ID, "resonant_scarab_attack", BONUS_ATTACK, AttributeModifier.Operation.MULTIPLY_TOTAL));
                builder.put(Attributes.ARMOR, new AttributeModifier(MUSIC_ARMOR_MODIFIER_ID, "resonant_scarab_armor", BONUS_ARMOR, AttributeModifier.Operation.MULTIPLY_TOTAL));
                builder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(MUSIC_BLOCK_REACH_MODIFIER_ID, "resonant_scarab_block_reach", BONUS_REACH, AttributeModifier.Operation.ADDITION));
                builder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(MUSIC_ENTITY_REACH_MODIFIER_ID, "resonant_scarab_entity_reach", BONUS_REACH, AttributeModifier.Operation.ADDITION));
            }
            return builder.build();
        }

        private static void tickRelic(Player player) {
            if (player.level().isClientSide) {
                return;
            }

            if (!ModEvents.isMusicRelicEquipped(player)) {
                removeBuffs(player);
                resetActivation(player);
                return;
            }

            CompoundTag data = getMusicData(player);
            if (data.getBoolean(ACTIVATED_KEY)) {
                applyBuffs(player);
                syncAwakened(player, true, false);
                refreshMusicSlot(player);
                return;
            }

            MusicPlaybackState playbackState = ModEvents.findNearbyTrackedDisc(player, DETECTION_RADIUS);
            if (playbackState == null) {
                removeBuffs(player);
                syncAwakened(player, false, false);
                refreshMusicSlot(player);
                clearTracking(data);
                return;
            }

            String currentKey = playbackState.key();
            String lastKey = data.getString(LAST_JUKEBOX_KEY);
            if (!currentKey.equals(lastKey)) {
                data.putString(LAST_JUKEBOX_KEY, currentKey);
                data.putBoolean(ACTIVATED_KEY, false);
                syncAwakened(player, false, false);
                removeBuffs(player);
                refreshMusicSlot(player);
            }

            long elapsedTicks = player.level().getGameTime() - playbackState.startTick();

            if (!data.getBoolean(ACTIVATED_KEY) && elapsedTicks >= playbackState.requiredTicks()) {
                data.putBoolean(ACTIVATED_KEY, true);
                syncAwakened(player, true, true);
                refreshMusicSlot(player);
                player.displayClientMessage(Component.translatable("message.ramesses_curse.music_relic_awakened").withStyle(ChatFormatting.GOLD), true);
                player.level().playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.9F, 1.1F);
                ModEvents.consumeTrackedMusicDisc(player.level(), playbackState.pos());
            }

            if (data.getBoolean(ACTIVATED_KEY)) {
                applyBuffs(player);
            } else {
                removeBuffs(player);
            }
        }

        private static CompoundTag getMusicData(Player player) {
            CompoundTag persistent = player.getPersistentData();
            if (!persistent.contains(Player.PERSISTED_NBT_TAG)) {
                persistent.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
            }
            CompoundTag persisted = persistent.getCompound(Player.PERSISTED_NBT_TAG);
            if (!persisted.contains(MUSIC_DATA_KEY)) {
                persisted.put(MUSIC_DATA_KEY, new CompoundTag());
            }
            return persisted.getCompound(MUSIC_DATA_KEY);
        }

        public static boolean isAwakened(Player player) {
            return getMusicData(player).getBoolean(ACTIVATED_KEY);
        }

        private static void resetActivation(Player player) {
            CompoundTag data = getMusicData(player);
            data.putBoolean(ACTIVATED_KEY, false);
            syncAwakened(player, false, true);
            refreshMusicSlot(player);
            clearTracking(data);
        }

        private static void clearTracking(CompoundTag data) {
            data.putString(LAST_JUKEBOX_KEY, "");
        }

        private static void applyBuffs(Player player) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 220, 0, false, false, true));
            applyModifier(player, Attributes.ATTACK_DAMAGE, MUSIC_ATTACK_MODIFIER_ID, "resonant_scarab_attack", BONUS_ATTACK, AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(player, Attributes.ARMOR, MUSIC_ARMOR_MODIFIER_ID, "resonant_scarab_armor", BONUS_ARMOR, AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyReach(player, ForgeMod.BLOCK_REACH.get(), MUSIC_BLOCK_REACH_MODIFIER_ID);
            applyReach(player, ForgeMod.ENTITY_REACH.get(), MUSIC_ENTITY_REACH_MODIFIER_ID);
        }

        private static void removeBuffs(Player player) {
            removeModifier(player, Attributes.ATTACK_DAMAGE, MUSIC_ATTACK_MODIFIER_ID);
            removeModifier(player, Attributes.ARMOR, MUSIC_ARMOR_MODIFIER_ID);
            removeModifier(player, ForgeMod.BLOCK_REACH.get(), MUSIC_BLOCK_REACH_MODIFIER_ID);
            removeModifier(player, ForgeMod.ENTITY_REACH.get(), MUSIC_ENTITY_REACH_MODIFIER_ID);
        }

        private static void applyReach(Player player, net.minecraft.world.entity.ai.attributes.Attribute attribute, UUID id) {
            applyModifier(player, attribute, id, "resonant_scarab_reach", BONUS_REACH, AttributeModifier.Operation.ADDITION);
        }

        private static void applyModifier(Player player, net.minecraft.world.entity.ai.attributes.Attribute attribute, UUID id, String name, double amount, AttributeModifier.Operation operation) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance == null) {
                return;
            }
            AttributeModifier existing = instance.getModifier(id);
            if (existing != null && existing.getAmount() == amount && existing.getOperation() == operation) {
                return;
            }
            instance.removeModifier(id);
            instance.addTransientModifier(new AttributeModifier(id, name, amount, operation));
        }

        private static void removeModifier(Player player, net.minecraft.world.entity.ai.attributes.Attribute attribute, UUID id) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) {
                instance.removeModifier(id);
            }
        }

        private static boolean hasAttackModifier(Player player) {
            AttributeInstance instance = player.getAttribute(Attributes.ATTACK_DAMAGE);
            return instance != null && instance.getModifier(MUSIC_ATTACK_MODIFIER_ID) != null;
        }

        private static float getAttackMultiplier() {
            return (float) (1.0D + BONUS_ATTACK);
        }

        public static void updateClientAwakened(boolean awakened) {
            clientAwakened = awakened;
        }

        private static void syncAwakened(Player player, boolean awakened, boolean force) {
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return;
            }
            CompoundTag data = getMusicData(player);
            if (!force && data.contains(LAST_SYNC_ACTIVATED_KEY) && data.getBoolean(LAST_SYNC_ACTIVATED_KEY) == awakened) {
                return;
            }
            data.putBoolean(LAST_SYNC_ACTIVATED_KEY, awakened);
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new MusicRelicSyncPacket(awakened));
        }

        private static void refreshMusicSlot(Player player) {
            CuriosApi.getCuriosInventory(player)
                    .resolve()
                    .flatMap(inv -> inv.getStacksHandler(RamessesCurseMod.MUSIC_SLOT_ID))
                    .ifPresent(handler -> {
                        handler.clearCachedModifiers();
                        handler.update();
                    });
        }

        private static boolean isClientAwakened(@Nullable Level level) {
            return level != null && level.isClientSide && clientAwakened;
        }
    }

    public static class UraeusNecklace extends Item implements ICurioItem {
        private static final String BURST_GUARD_KEY = "RamessesUraeusBurst";
        public UraeusNecklace() {
            super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
        }

        @Override
        public boolean canEquip(SlotContext slotContext, ItemStack stack) {
            return slotContext.entity() instanceof Player;
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            if (appendConfiguredTooltip(tooltip)) {
                return;
            }
            tooltip.add(Component.literal("-命中敌对怪物时，附着 ")
                    .withStyle(ChatFormatting.DARK_PURPLE)
                    .append(Component.literal("1").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(" 种配置中的咒效，").withStyle(ChatFormatting.DARK_PURPLE))
                    .append(Component.literal("10").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(" 秒内继续命中会逐层加深。").withStyle(ChatFormatting.DARK_PURPLE)));
            tooltip.add(Component.literal("-叠至 ")
                    .withStyle(ChatFormatting.DARK_PURPLE)
                    .append(Component.literal("8").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(" 层时崩解为 ").withStyle(ChatFormatting.DARK_PURPLE))
                    .append(Component.literal("300%").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(" 伤害；雨天额外 ").withStyle(ChatFormatting.DARK_PURPLE))
                    .append(Component.literal("+50%").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("，并赐予 ").withStyle(ChatFormatting.DARK_PURPLE))
                    .append(Component.literal("伤害吸收V").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(" 与 ").withStyle(ChatFormatting.DARK_PURPLE))
                    .append(Component.literal("抗性提升V").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("。").withStyle(ChatFormatting.DARK_PURPLE)));
        }

        private static float onAttack(Player player, LivingEntity target, float damage) {
            if (player.level().isClientSide
                    || !(target instanceof Enemy)
                    || damage <= 0.0F
                    || !hasNecklaceEquipped(player)
                    || !ModEvents.canUseSealBoundItem(player)
                    || target.getPersistentData().getBoolean(BURST_GUARD_KEY)) {
                return damage;
            }

            MobEffect effect = Config.getNecklaceEffect();
            if (effect == null) {
                return damage;
            }

            MobEffectInstance existing = target.getEffect(effect);
            int nextLevel = existing == null ? 1 : existing.getAmplifier() + 2;

            if (nextLevel >= Config.NECKLACE_MAX_STACKS.get()) {
                target.removeEffect(effect);

                float burstMultiplier = Config.NECKLACE_BURST_DAMAGE_MULTIPLIER.get().floatValue();
                if (isRainEmpowered(player)) {
                    burstMultiplier += (float) (Config.NECKLACE_RAIN_BONUS_MULTIPLIER.get() - 1.0D);
                    rewardRainBurst(player);
                }

                return damage * burstMultiplier;
            }

            target.addEffect(new MobEffectInstance(effect, Config.NECKLACE_STACK_TIMEOUT.get(), nextLevel - 1, false, true, true));
            return damage;
        }

        private static boolean isRainEmpowered(Player player) {
            return player.level().isRaining() && player.level().canSeeSky(player.blockPosition().above());
        }

        private static void rewardRainBurst(Player player) {
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, Config.NECKLACE_RAIN_REWARD_DURATION.get(), Config.NECKLACE_RAIN_ABSORPTION_AMPLIFIER.get(), false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, Config.NECKLACE_RAIN_REWARD_DURATION.get(), Config.NECKLACE_RAIN_RESISTANCE_AMPLIFIER.get(), false, false, true));
        }

        private static boolean hasNecklaceEquipped(Player player) {
            return CuriosApi.getCuriosInventory(player)
                    .resolve()
                    .flatMap(inv -> inv.findFirstCurio(URAEUS_NECKLACE.get()))
                    .map(SlotResult::stack)
                    .filter(stack -> !stack.isEmpty())
                    .isPresent();
        }

        private static boolean appendConfiguredTooltip(List<Component> tooltip) {
            Component effectName = getConfiguredEffectName();
            tooltip.add(Component.literal("-命中敌对怪物时，为其附着 ")
                    .withStyle(ChatFormatting.DARK_PURPLE)
                    .append(effectName.copy().withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("，并在 ").withStyle(ChatFormatting.DARK_PURPLE))
                    .append(Component.literal(formatSeconds(Config.NECKLACE_STACK_TIMEOUT.get())).withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(" 秒内继续叠层。").withStyle(ChatFormatting.DARK_PURPLE)));
            tooltip.add(Component.literal("-叠满 ")
                    .withStyle(ChatFormatting.DARK_PURPLE)
                    .append(Component.literal(Integer.toString(Config.NECKLACE_MAX_STACKS.get())).withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(" 层后爆裂为 ").withStyle(ChatFormatting.DARK_PURPLE))
                    .append(Component.literal(formatPercent(Config.NECKLACE_BURST_DAMAGE_MULTIPLIER.get())).withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(" 伤害；雨天额外 ").withStyle(ChatFormatting.DARK_PURPLE))
                    .append(Component.literal("+" + formatPercentDelta(Config.NECKLACE_RAIN_BONUS_MULTIPLIER.get())).withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("，并赐予 ").withStyle(ChatFormatting.DARK_PURPLE))
                    .append(Component.literal("伤害吸收" + getRomanLevel(Config.NECKLACE_RAIN_ABSORPTION_AMPLIFIER.get() + 1)).withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(" 与 ").withStyle(ChatFormatting.DARK_PURPLE))
                    .append(Component.literal("抗性提升" + getRomanLevel(Config.NECKLACE_RAIN_RESISTANCE_AMPLIFIER.get() + 1)).withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("。").withStyle(ChatFormatting.DARK_PURPLE)));
            return true;
        }

        private static Component getConfiguredEffectName() {
            MobEffect effect = Config.getNecklaceEffect();
            return effect != null ? Component.translatable(effect.getDescriptionId()) : Component.literal(Config.NECKLACE_EFFECT_ID.get());
        }

        private static String formatSeconds(int ticks) {
            double seconds = ticks / 20.0D;
            if (Math.abs(seconds - Math.rint(seconds)) < 1.0E-6D) {
                return Integer.toString((int) Math.rint(seconds));
            }
            return String.format(Locale.ROOT, "%.1f", seconds);
        }

        private static String formatPercent(double multiplier) {
            return formatNumber(multiplier * 100.0D) + "%";
        }

        private static String formatPercentDelta(double multiplier) {
            return formatNumber((multiplier - 1.0D) * 100.0D) + "%";
        }

        private static String formatNumber(double value) {
            if (Math.abs(value - Math.rint(value)) < 1.0E-6D) {
                return Integer.toString((int) Math.rint(value));
            }
            return String.format(Locale.ROOT, "%.1f", value);
        }

        private static String getRomanLevel(int level) {
            return switch (level) {
                case 1 -> "I";
                case 2 -> "II";
                case 3 -> "III";
                case 4 -> "IV";
                case 5 -> "V";
                case 6 -> "VI";
                case 7 -> "VII";
                case 8 -> "VIII";
                case 9 -> "IX";
                case 10 -> "X";
                default -> Integer.toString(level);
            };
        }
    }

    public static class EdictOfTwoLands extends Item implements ICurioItem {
        private static final String DATA_KEY = "RamessesEdictOfTwoLands";
        private static final String COOLDOWN_UNTIL_KEY = "CooldownUntil";
        private static final int GUILT_COST = 20;
        private static final int DOMAIN_GUILT_COST = 30;
        private static final int COOLDOWN_TICKS = 90 * 20;
        private static final int DOMAIN_COOLDOWN_TICKS = 360 * 20;
        public EdictOfTwoLands() {
            super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
        }
        @Override
        public boolean canEquip(SlotContext slotContext, ItemStack stack) {
            return slotContext.entity() instanceof Player
                    && isValidHandSlot(slotContext.identifier());
        }
        @Override
        public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
            if (!newStack.isEmpty() && newStack.is(stack.getItem())) {
                return;
            }
            if (slotContext.entity().level().isClientSide) {
                ModEvents.ClientOverlayEvents.cancelEdictCharge();
            }
        }
        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("两地敕印不急于开口，它更愿意先让四周的喧声沉到底。")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(Component.literal("按住 Shift 查看技能键位与敕令说明。")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            if (Screen.hasShiftDown()) {
                tooltip.add(Component.empty());
                tooltip.add(Component.literal("- 长按 ").withStyle(ChatFormatting.DARK_PURPLE)
                        .append(Component.literal("X").withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(" 凝聚 ").withStyle(ChatFormatting.DARK_PURPLE))
                        .append(Component.literal("3").withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(" s，将周围敌群掀离立足之处，再令其坠回尘土。")
                                .withStyle(ChatFormatting.DARK_PURPLE)));
                tooltip.add(Component.literal("- 长按 ").withStyle(ChatFormatting.DARK_PURPLE)
                        .append(Component.literal("Y").withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(" 凝聚 ").withStyle(ChatFormatting.DARK_PURPLE))
                        .append(Component.literal("3").withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(" s，于原地展开 ").withStyle(ChatFormatting.DARK_PURPLE))
                        .append(Component.literal("15").withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(" 格矩阵，缓缓收束踏入者，终以尽数归寂的方式落幕。")
                                .withStyle(ChatFormatting.DARK_PURPLE)));
                tooltip.add(Component.literal("- X 索取 ").withStyle(ChatFormatting.DARK_PURPLE)
                        .append(Component.literal(Integer.toString(GUILT_COST)).withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(" 点王罪，余响静默 ").withStyle(ChatFormatting.DARK_PURPLE))
                        .append(Component.literal("90").withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(" s；Y 索取 ").withStyle(ChatFormatting.DARK_PURPLE))
                        .append(Component.literal(Integer.toString(DOMAIN_GUILT_COST)).withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(" 点王罪，余响静默 ").withStyle(ChatFormatting.DARK_PURPLE))
                        .append(Component.literal("360").withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(" s。两道敕令共用冷却。")
                                .withStyle(ChatFormatting.DARK_PURPLE)));
            }
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null) {
                return;
            }
            int cooldownTicks = getSharedCooldownTicks(minecraft.player);
            if (cooldownTicks > 0) {
                tooltip.add(Component.translatable("message.ramesses_curse.edict_cooldown", String.format(Locale.ROOT, "%.1f", cooldownTicks / 20.0F))
                        .withStyle(ChatFormatting.GRAY));
            }
        }
        private static boolean tryActivate(ServerPlayer player) {
            return tryActivateInternal(player, false) == EdictActivationResult.SUCCESS;
        }
        private static EdictActivationResult tryActivateDomain(ServerPlayer player) {
            return tryActivateInternal(player, true);
        }
        private static EdictActivationResult tryActivateInternal(ServerPlayer player, boolean domain) {
            if (!isEquipped(player)) {
                return EdictActivationResult.NOT_EQUIPPED;
            }
            int cooldownTicks = getSharedCooldownTicks(player);
            if (cooldownTicks > 0) {
                return EdictActivationResult.COOLDOWN;
            }
            int guiltCost = domain ? DOMAIN_GUILT_COST : GUILT_COST;
            if (ModEvents.getGuiltProgress(player) < guiltCost) {
                return EdictActivationResult.NO_GUILT;
            }
            ModEvents.EdictCastResult castResult = domain
                    ? ModEvents.activateEdictOfTwoLandsDomain(player)
                    : ModEvents.activateEdictOfTwoLands(player);
            if (castResult == ModEvents.EdictCastResult.NO_TARGETS) {
                return EdictActivationResult.NO_TARGETS;
            }
            if (castResult == ModEvents.EdictCastResult.OTHER_MODE_ACTIVE) {
                return EdictActivationResult.OTHER_MODE_ACTIVE;
            }
            ModEvents.setGuiltProgress(player, ModEvents.getGuiltProgress(player) - guiltCost);
            int appliedCooldown = domain ? DOMAIN_COOLDOWN_TICKS : COOLDOWN_TICKS;
            getData(player).putLong(COOLDOWN_UNTIL_KEY, player.level().getGameTime() + appliedCooldown);
            player.getCooldowns().addCooldown(ModItems.EDICT_OF_TWO_LANDS.get(), appliedCooldown);
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new EdictCooldownSyncPacket(appliedCooldown));
            return EdictActivationResult.SUCCESS;
        }
        private static int getCooldownTicks(Player player) {
            return getSharedCooldownTicks(player);
        }
        private static int getDomainCooldownTicks(Player player) {
            return getSharedCooldownTicks(player);
        }
        private static int getSharedCooldownTicks(Player player) {
            if (player.level().isClientSide) {
                return ModEvents.ClientOverlayEvents.getClientEdictCooldownTicks();
            }
            return (int) Math.max(0L, getData(player).getLong(COOLDOWN_UNTIL_KEY) - player.level().getGameTime());
        }
        private static void clearCooldown(ServerPlayer player) {
            getData(player).putLong(COOLDOWN_UNTIL_KEY, 0L);
            player.getCooldowns().removeCooldown(ModItems.EDICT_OF_TWO_LANDS.get());
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new EdictCooldownSyncPacket(0));
        }
        private static boolean isDomainActive(Player player) {
            return player.getPersistentData().getLong("RamessesEdictDomainUntil") > player.level().getGameTime();
        }
        private static boolean isEquipped(Player player) {
            return CuriosApi.getCuriosInventory(player)
                    .resolve()
                    .flatMap(inv -> inv.findFirstCurio(EDICT_OF_TWO_LANDS.get()))
                    .map(SlotResult::stack)
                    .filter(stack -> !stack.isEmpty())
                    .isPresent();
        }
        private static CompoundTag getData(Player player) {
            CompoundTag persisted = player.getPersistentData();
            if (!persisted.contains(Player.PERSISTED_NBT_TAG)) {
                persisted.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
            }
            CompoundTag root = persisted.getCompound(Player.PERSISTED_NBT_TAG);
            if (!root.contains(DATA_KEY)) {
                root.put(DATA_KEY, new CompoundTag());
            }
            return root.getCompound(DATA_KEY);
        }
        private static boolean isValidHandSlot(String identifier) {
            if (identifier == null) {
                return false;
            }
            return RamessesCurseMod.HAND_SLOT_ID.equals(identifier)
                    || "hand".equals(identifier)
                    || "hands".equals(identifier)
                    || identifier.endsWith("_hand")
                    || identifier.endsWith("_hands");
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
