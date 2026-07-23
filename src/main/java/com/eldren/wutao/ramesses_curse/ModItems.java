package com.eldren.wutao.ramesses_curse;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.advancements.CriteriaTriggers;
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
import net.minecraft.stats.Stats;
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
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SmithingTemplateItem;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
import java.util.ArrayList;

import vazkii.patchouli.api.PatchouliAPI;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, RamessesCurseMod.MODID);

    public static final RegistryObject<Item> SEAL_OF_KADESH =
            ITEMS.register("seal_of_kadesh", SealOfKadesh::new);
    public static final RegistryObject<Item> PURIFICATION_SCROLL =
            ITEMS.register("purification_scroll", PurificationScroll::new);
    public static final RegistryObject<Item> DYNASTY_PRESSURE_POTION =
            ITEMS.register("dynasty_pressure_potion", DynastyPressurePotion::new);
    public static final RegistryObject<Item> SATIETY_CRYSTAL =
            ITEMS.register("satiety_crystal", SatietyCrystal::new);
    public static final RegistryObject<Item> EMBER_OF_DUAT =
            ITEMS.register("ember_of_duat", EmberOfDuat::new);
    public static final RegistryObject<Item> ETERNAL_SILENCE_GRANULE =
            ITEMS.register("eternal_silence_granule", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> ETERNAL_SILENCE_INGOT =
            ITEMS.register("eternal_silence_ingot", GoldenNamedItem::new);
    public static final RegistryObject<Item> ETERNAL_SILENCE_TOTEM =
            ITEMS.register("eternal_silence_totem", EternalSilenceTotem::new);
    public static final RegistryObject<Item> ETERNAL_SILENCE_REBIRTH_TOTEM =
            ITEMS.register("eternal_silence_rebirth_totem", EternalSilenceRebirthTotem::new);
    public static final RegistryObject<Item> TWILIGHT_CRYSTAL_RING =
            ITEMS.register("twilight_crystal_ring", TwilightCrystalRing::new);
    public static final RegistryObject<Item> SILENT_SEPULCHER_WITHERED_VINE =
            ITEMS.register("silent_sepulcher_withered_vine", () -> new BlockItem(ModBlocks.SILENT_SEPULCHER_WITHERED_VINE.get(), new Item.Properties()));
    public static final RegistryObject<Item> ETERNAL_SILENCE_RITUAL_STAFF =
            ITEMS.register("eternal_silence_ritual_staff", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ETERNAL_SILENCE_UPGRADE_SMITHING_TEMPLATE =
            ITEMS.register("eternal_silence_upgrade_smithing_template", EternalSilenceUpgradeTemplate::new);
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
    public static final RegistryObject<Item> DUAT_DUNGEON_TILE =
            ITEMS.register("duat_dungeon_tile", () -> new BlockItem(ModBlocks.DUAT_DUNGEON_TILE.get(), new Item.Properties()));
    public static final RegistryObject<Item> DUAT_DUNGEON_TILE_WALL =
            ITEMS.register("duat_dungeon_tile_wall", () -> new BlockItem(ModBlocks.DUAT_DUNGEON_TILE_WALL.get(), new Item.Properties()));
    public static final RegistryObject<Item> DUAT_DUNGEON_TILE_SLAB =
            ITEMS.register("duat_dungeon_tile_slab", () -> new BlockItem(ModBlocks.DUAT_DUNGEON_TILE_SLAB.get(), new Item.Properties()));
    public static final RegistryObject<Item> DUAT_DUNGEON_TILE_STAIRS =
            ITEMS.register("duat_dungeon_tile_stairs", () -> new BlockItem(ModBlocks.DUAT_DUNGEON_TILE_STAIRS.get(), new Item.Properties()));
    public static final RegistryObject<Item> DUAT_DUNGEON_PILLAR =
            ITEMS.register("duat_dungeon_pillar", () -> new BlockItem(ModBlocks.DUAT_DUNGEON_PILLAR.get(), new Item.Properties()));
    public static final RegistryObject<Item> SOLAR_RITE_BRICK =
            ITEMS.register("solar_rite_brick", () -> new BlockItem(ModBlocks.SOLAR_RITE_BRICK.get(), new Item.Properties()));
    public static final RegistryObject<Item> DATE_PALM_LOG =
            ITEMS.register("date_palm_log", () -> new BlockItem(ModBlocks.DATE_PALM_LOG.get(), new Item.Properties()));
    public static final RegistryObject<Item> DATE_PALM_LEAVES =
            ITEMS.register("date_palm_leaves", () -> new BlockItem(ModBlocks.DATE_PALM_LEAVES.get(), new Item.Properties()));
    public static final RegistryObject<Item> DATE_PALM_SAPLING =
            ITEMS.register("date_palm_sapling", () -> new BlockItem(ModBlocks.DATE_PALM_SAPLING.get(), new Item.Properties()));
    public static final RegistryObject<Item> DATE_PALM_FRUIT =
            ITEMS.register("date_palm_fruit", DatePalmFruit::new);
    public static final RegistryObject<Item> INSCRIPTION_BAG =
            ITEMS.register("inscription_bag", InscriptionBagItem::new);
    public static final List<RegistryObject<Item>> INSCRIPTION_FRAGMENTS = registerInscriptionFragments();

    private static List<RegistryObject<Item>> registerInscriptionFragments() {
        List<RegistryObject<Item>> fragments = new ArrayList<>(26);
        for (int index = 1; index <= 26; index++) {
            int fragmentIndex = index;
            fragments.add(ITEMS.register(
                    "inscription_fragment_" + fragmentIndex,
                    () -> new InscriptionFragmentItem(new Item.Properties(), fragmentIndex)));
        }
        return List.copyOf(fragments);
    }

    public static int getHeartOfDuatCooldownTicks(Player player) {
        return HeartOfDuat.getCooldownTicks(player);
    }

    public static class GoldenNamedItem extends Item {
        public GoldenNamedItem() {
            super(new Item.Properties());
        }

        @Override
        public Component getName(ItemStack stack) {
            return super.getName(stack).copy().withStyle(ChatFormatting.GOLD);
        }
    }

    public static class TwilightCrystalRing extends Item implements ICurioItem {
        public static final String RING_SLOT_ID = "ring";
        private static final String DATA_KEY = "RamessesTwilightCrystalRing";
        private static final String TARGET_KEY = "Target";
        private static final String HIT_COUNT_KEY = "HitCount";
        private static final String LAST_HIT_TIME_KEY = "LastHitTime";
        private static final int COMBO_WINDOW_TICKS = 4 * 20;
        private static final float BONUS_MAGIC_DAMAGE = 2.0F;
        private static final UUID MOVEMENT_SPEED_MODIFIER_ID =
                UUID.fromString("cc141dea-741e-4c28-95d9-909715930d22");
        private static final UUID ATTACK_SPEED_MODIFIER_ID =
                UUID.fromString("f15c9cb8-c1ec-4bec-bd15-b3bd4327716f");

        public TwilightCrystalRing() {
            super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
        }

        @Override
        public Component getName(ItemStack stack) {
            return super.getName(stack).copy().withStyle(ChatFormatting.LIGHT_PURPLE);
        }

        @Override
        public boolean canEquip(SlotContext slotContext, ItemStack stack) {
            return RING_SLOT_ID.equals(slotContext.identifier()) && slotContext.entity() instanceof Player;
        }

        @Override
        public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
                SlotContext slotContext,
                UUID uuid,
                ItemStack stack) {
            if (!RING_SLOT_ID.equals(slotContext.identifier())) {
                return ImmutableMultimap.of();
            }
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(
                    MOVEMENT_SPEED_MODIFIER_ID,
                    "twilight_crystal_ring_movement_speed",
                    0.05D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
            builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(
                    ATTACK_SPEED_MODIFIER_ID,
                    "twilight_crystal_ring_attack_speed",
                    0.08D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
            return builder.build();
        }

        @Override
        public void appendHoverText(
                ItemStack stack,
                @Nullable Level level,
                List<Component> tooltip,
                TooltipFlag flag) {
            tooltip.add(Component.translatable("item.ramesses_curse.twilight_crystal_ring.combo")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(Component.translatable("item.ramesses_curse.twilight_crystal_ring.reset")
                    .withStyle(ChatFormatting.GRAY));
        }

        public static void tickEquippedAttributes(Player player) {
            if (isEquipped(player)) {
                maintainModifier(
                        player,
                        Attributes.MOVEMENT_SPEED,
                        MOVEMENT_SPEED_MODIFIER_ID,
                        "twilight_crystal_ring_movement_speed",
                        0.05D);
                maintainModifier(
                        player,
                        Attributes.ATTACK_SPEED,
                        ATTACK_SPEED_MODIFIER_ID,
                        "twilight_crystal_ring_attack_speed",
                        0.08D);
            } else {
                removeModifier(player, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_MODIFIER_ID);
                removeModifier(player, Attributes.ATTACK_SPEED, ATTACK_SPEED_MODIFIER_ID);
                clearCombo(player);
            }
        }

        public static void onMeleeHit(ServerPlayer player, LivingEntity target) {
            if (!isEquipped(player)) {
                return;
            }

            CompoundTag data = getData(player);
            long gameTime = player.level().getGameTime();
            boolean sameTarget = data.hasUUID(TARGET_KEY)
                    && data.getUUID(TARGET_KEY).equals(target.getUUID());
            boolean withinWindow = gameTime - data.getLong(LAST_HIT_TIME_KEY) <= COMBO_WINDOW_TICKS;
            int hitCount = sameTarget && withinWindow ? data.getInt(HIT_COUNT_KEY) + 1 : 1;
            data.putUUID(TARGET_KEY, target.getUUID());
            data.putLong(LAST_HIT_TIME_KEY, gameTime);

            if (hitCount < 3) {
                data.putInt(HIT_COUNT_KEY, hitCount);
                return;
            }

            clearCombo(player);
            if (player.getServer() != null) {
                player.getServer().execute(() -> {
                    if (target.isAlive() && player.level() == target.level()) {
                        target.invulnerableTime = 0;
                        target.hurt(target.damageSources().magic(), BONUS_MAGIC_DAMAGE);
                    }
                });
            }
        }

        private static boolean isEquipped(Player player) {
            return CuriosApi.getCuriosInventory(player)
                    .resolve()
                    .flatMap(inventory -> inventory.getStacksHandler(RING_SLOT_ID))
                    .map(handler -> {
                        top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler stacks = handler.getStacks();
                        for (int slot = 0; slot < stacks.getSlots(); slot++) {
                            if (stacks.getStackInSlot(slot).is(TWILIGHT_CRYSTAL_RING.get())) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .orElse(false);
        }

        private static void maintainModifier(
                Player player,
                Attribute attribute,
                UUID id,
                String name,
                double amount) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance == null) {
                return;
            }
            AttributeModifier existing = instance.getModifier(id);
            if (existing != null
                    && existing.getAmount() == amount
                    && existing.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL) {
                return;
            }
            instance.removeModifier(id);
            instance.addTransientModifier(new AttributeModifier(
                    id,
                    name,
                    amount,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        private static void removeModifier(Player player, Attribute attribute, UUID id) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) {
                instance.removeModifier(id);
            }
        }

        private static CompoundTag getData(Player player) {
            CompoundTag persistent = player.getPersistentData();
            if (!persistent.contains(Player.PERSISTED_NBT_TAG)) {
                persistent.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
            }
            CompoundTag persisted = persistent.getCompound(Player.PERSISTED_NBT_TAG);
            if (!persisted.contains(DATA_KEY)) {
                persisted.put(DATA_KEY, new CompoundTag());
            }
            return persisted.getCompound(DATA_KEY);
        }

        private static void clearCombo(Player player) {
            CompoundTag data = getData(player);
            data.remove(TARGET_KEY);
            data.remove(HIT_COUNT_KEY);
            data.remove(LAST_HIT_TIME_KEY);
        }
    }

    public static class DatePalmFruit extends Item {
        private static final int EFFECT_DURATION_TICKS = 10 * 20;

        public DatePalmFruit() {
            super(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(2)
                    .saturationMod(0.2F)
                    .build()));
        }

        @Override
        public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
            ItemStack result = super.finishUsingItem(stack, level, living);
            if (!level.isClientSide && living instanceof Player player) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, EFFECT_DURATION_TICKS, 0));
                ModMobEffects.grantDroughtImmunity(player, EFFECT_DURATION_TICKS);
            }
            return result;
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable("item.ramesses_curse.date_palm_fruit.desc")
                    .withStyle(ChatFormatting.GOLD));
        }
    }

    public static class EternalSilenceUpgradeTemplate extends SmithingTemplateItem {
        private static final List<ResourceLocation> BASE_SLOT_ICONS = List.of(
                new ResourceLocation("item/empty_armor_slot_helmet"),
                new ResourceLocation("item/empty_slot_sword"),
                new ResourceLocation("item/empty_armor_slot_chestplate"),
                new ResourceLocation("item/empty_slot_pickaxe"),
                new ResourceLocation("item/empty_armor_slot_leggings"),
                new ResourceLocation("item/empty_slot_axe"),
                new ResourceLocation("item/empty_armor_slot_boots"),
                new ResourceLocation("item/empty_slot_hoe"),
                new ResourceLocation("item/empty_slot_shovel"));
        private static final List<ResourceLocation> ADDITIONAL_SLOT_ICONS = List.of(
                new ResourceLocation("item/empty_slot_ingot"));

        public EternalSilenceUpgradeTemplate() {
            super(
                    Component.translatable("item.ramesses_curse.eternal_silence_upgrade.applies_to").withStyle(ChatFormatting.BLUE),
                    Component.translatable("item.ramesses_curse.eternal_silence_upgrade.ingredients").withStyle(ChatFormatting.BLUE),
                    Component.translatable("upgrade.ramesses_curse.eternal_silence").withStyle(ChatFormatting.GRAY),
                    Component.translatable("item.ramesses_curse.eternal_silence_upgrade.base_slot_description"),
                    Component.translatable("item.ramesses_curse.eternal_silence_upgrade.additions_slot_description"),
                    BASE_SLOT_ICONS,
                    ADDITIONAL_SLOT_ICONS);
        }

        @Override
        public String getDescriptionId() {
            return "item.ramesses_curse.eternal_silence_upgrade_smithing_template";
        }
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

    public static boolean isUraeusNecklaceStackEffect(LivingEntity target, MobEffect effect) {
        return UraeusNecklace.isStackEffect(target, effect);
    }

    public static int getUraeusStormPassiveCooldownTicks(Player player) {
        return UraeusNecklace.getStormPassiveCooldownTicks(player);
    }

    public static void setUraeusStormPassiveCooldown(ServerPlayer player) {
        UraeusNecklace.setStormPassiveCooldown(player);
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
    public static int getEdictOfTwoLandsGuiltCost(boolean domain) {
        return domain ? EdictOfTwoLands.getDomainGuiltCost() : EdictOfTwoLands.getCastGuiltCost();
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

    private static MutableComponent goldText(int value) {
        return Component.literal(Integer.toString(value)).withStyle(ChatFormatting.GOLD);
    }

    private static MutableComponent goldText(String value) {
        return Component.literal(value).withStyle(ChatFormatting.GOLD);
    }

    private static MutableComponent goldText(double value) {
        if (Math.abs(value - Math.rint(value)) < 1.0E-6D) {
            return goldText(Long.toString(Math.round(value)));
        }
        String formatted = String.format(Locale.ROOT, "%.2f", value)
                .replaceAll("0+$", "")
                .replaceAll("\\.$", "");
        return goldText(formatted);
    }

    private static MutableComponent goldKey(String key) {
        return Component.translatable(key).withStyle(ChatFormatting.GOLD);
    }

    private static MutableComponent aquaText(int value) {
        return Component.literal(Integer.toString(value)).withStyle(ChatFormatting.AQUA);
    }

    private static MutableComponent aquaText(String value) {
        return Component.literal(value).withStyle(ChatFormatting.AQUA);
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
            return wearer instanceof Player player
                    && (player.getAbilities().instabuild || ModEvents.areAllSealCursesReversed(player));
        }

        @Override
        public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
            LivingEntity wearer = slotContext.entity();
            if (wearer instanceof Player player) {
                removeLuckPenalty(player);
                ModEvents.clearSealUnequipEffects(player);
                if (!player.level().isClientSide && ModEvents.areAllSealCursesReversed(player)) {
                    ModEvents.dropEquippedRamessesCurios(player);
                }
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
        @OnlyIn(Dist.CLIENT)
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(purpleKey("item.ramesses_curse.seal_of_kadesh.desc"));
            tooltip.add(purpleKey("item.ramesses_curse.seal_of_kadesh.hint_shift"));
            tooltip.add(purpleKey("item.ramesses_curse.seal_of_kadesh.hint_ctrl"));
            tooltip.add(purpleKey("item.ramesses_curse.seal_of_kadesh.hint_alt"));
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                tooltip.add(Component.translatable("item.ramesses_curse.seal_of_kadesh.dynasty_threat",
                        Component.literal(Integer.toString(ModEvents.ClientOverlayEvents.getClientDynastyThreat())).withStyle(ChatFormatting.RED))
                        .withStyle(ChatFormatting.GOLD));
            }
            boolean curseOneReversed = minecraft.player != null
                    && ModEvents.isCurseOneReversed(minecraft.player);
            boolean curseFiveReversed = minecraft.player != null
                    && ModEvents.isCurseFiveReversed(minecraft.player);
            boolean curseFiveDeclined = minecraft.player != null
                    && ModEvents.isCurseFiveDeclined(minecraft.player);
            boolean curseSixReversed = minecraft.player != null
                    && ModEvents.isCurseSevenReversed(minecraft.player);
            boolean curseEightReversed = minecraft.player != null
                    && ModEvents.isNetherAwakened(minecraft.player)
                    && ModEvents.isCurseEightReversed(minecraft.player);
            boolean curseNineReversed = minecraft.player != null
                    && ModEvents.isCurseNineReversed(minecraft.player);
            boolean curseThreeFourLifted = minecraft.player != null
                    && ModEvents.areCurseThreeAndFourLifted(minecraft.player);
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
                tooltip.add(purpleKey("item.ramesses_curse.seal_of_kadesh.section_curses"));
                tooltip.add(curseLine("item.ramesses_curse.seal_of_kadesh.curse_1_title",
                        curseOneReversed ? "item.ramesses_curse.seal_of_kadesh.curse_1_intro_reversed" : "item.ramesses_curse.seal_of_kadesh.curse_1_intro",
                        curseOneReversed));
                tooltip.add(curseLine("item.ramesses_curse.seal_of_kadesh.curse_2_title",
                        minecraft.player != null && ModEvents.isCurseTwoReversed(minecraft.player) ? "item.ramesses_curse.seal_of_kadesh.curse_2_intro_reversed" : "item.ramesses_curse.seal_of_kadesh.curse_2_intro",
                        minecraft.player != null && ModEvents.isCurseTwoReversed(minecraft.player)));
                if (!curseThreeFourLifted) {
                    tooltip.add(curseLine("item.ramesses_curse.seal_of_kadesh.curse_3_title", "item.ramesses_curse.seal_of_kadesh.curse_3_intro", false));
                    tooltip.add(curseLine("item.ramesses_curse.seal_of_kadesh.curse_4_title", "item.ramesses_curse.seal_of_kadesh.curse_4_intro", false));
                }
                if (curseFiveReversed) {
                    tooltip.add(curseLine("item.ramesses_curse.seal_of_kadesh.curse_5_title", "item.ramesses_curse.seal_of_kadesh.curse_5_intro_reversed", true));
                } else if (curseFiveDeclined) {
                    tooltip.add(curseLine("item.ramesses_curse.seal_of_kadesh.curse_5_title", "item.ramesses_curse.seal_of_kadesh.curse_5_declined_intro", true));
                } else {
                    tooltip.add(curseLine("item.ramesses_curse.seal_of_kadesh.curse_5_title", "item.ramesses_curse.seal_of_kadesh.curse_5_intro", false));
                }
                tooltip.add(curseLine("item.ramesses_curse.seal_of_kadesh.curse_6_title",
                        curseSixReversed ? "item.ramesses_curse.seal_of_kadesh.curse_6_intro_reversed" : "item.ramesses_curse.seal_of_kadesh.curse_6_intro",
                        curseSixReversed));
                tooltip.add(curseLine("item.ramesses_curse.seal_of_kadesh.curse_7_title", "item.ramesses_curse.seal_of_kadesh.curse_7_intro", false));
                tooltip.add(curseLine("item.ramesses_curse.seal_of_kadesh.curse_8_title",
                        curseEightReversed ? "item.ramesses_curse.seal_of_kadesh.curse_8_intro_reversed" : "item.ramesses_curse.seal_of_kadesh.curse_8_intro",
                        curseEightReversed));
                tooltip.add(curseLine("item.ramesses_curse.seal_of_kadesh.curse_9_title",
                        curseNineReversed ? "item.ramesses_curse.seal_of_kadesh.curse_9_intro_reversed" : "item.ramesses_curse.seal_of_kadesh.curse_9_intro",
                        curseNineReversed));
            }
            if (Screen.hasControlDown()) {
                tooltip.add(Component.empty());
                if (curseOneReversed) {
                    tooltip.add(curseTemplateLine("item.ramesses_curse.seal_of_kadesh.curse_1_title", "item.ramesses_curse.seal_of_kadesh.curse_1_detail_reversed", true,
                            tooltipArgument("count", reversedNumber(Math.max(1, Config.CURSE_1_REVERSED_EFFECT_COUNT.get()))),
                            tooltipArgument("duration", reversedNumber(formatTicksAsSeconds(Config.CURSE_1_REVERSED_EFFECT_DURATION.get()))),
                            tooltipArgument("min_level", reversedNumber(Math.min(Config.CURSE_1_REVERSED_AMPLIFIER_MIN.get(), Config.CURSE_1_REVERSED_AMPLIFIER_MAX.get()) + 1)),
                            tooltipArgument("max_level", reversedNumber(Math.max(Config.CURSE_1_REVERSED_AMPLIFIER_MIN.get(), Config.CURSE_1_REVERSED_AMPLIFIER_MAX.get()) + 1)),
                            tooltipArgument("cooldown", reversedNumber(formatTicksAsSeconds(Config.CURSE_1_REVERSED_TRIGGER_COOLDOWN.get())))));
                } else {
                    tooltip.add(curseTemplateLine("item.ramesses_curse.seal_of_kadesh.curse_1_title", "item.ramesses_curse.seal_of_kadesh.curse_1_detail", false,
                            tooltipArgument("interval", yellowNumber(formatTicksAsMinutes(Config.RANDOM_CURSE_INTERVAL.get()))),
                            tooltipArgument("count", yellowNumber(1)),
                            tooltipArgument("duration", yellowNumber(formatTicksAsSeconds(Config.RANDOM_CURSE_DURATION.get()))),
                            tooltipArgument("min_level", yellowNumber(Math.min(Config.RANDOM_CURSE_AMPLIFIER_MIN.get(), Config.RANDOM_CURSE_AMPLIFIER_MAX.get()) + 1)),
                            tooltipArgument("max_level", yellowNumber(Math.max(Config.RANDOM_CURSE_AMPLIFIER_MIN.get(), Config.RANDOM_CURSE_AMPLIFIER_MAX.get()) + 1))));
                }
                if (minecraft.player != null && ModEvents.isCurseTwoReversed(minecraft.player)) {
                    tooltip.add(curseTemplateLine("item.ramesses_curse.seal_of_kadesh.curse_2_title", "item.ramesses_curse.seal_of_kadesh.curse_2_detail_reversed", true,
                            tooltipArgument("luck", reversedNumber(formatNumber(Config.CURSE_2_REVERSED_LUCK_BONUS.get()))),
                            tooltipArgument("interval", reversedNumber(formatTicksAsMinutes(Config.CURSE_2_REVERSED_SPAWN_INTERVAL.get()))),
                            tooltipArgument("chance", reversedPercent(Config.CURSE_2_REVERSED_SPAWN_CHANCE.get()))));
                } else {
                    tooltip.add(curseTemplateLine("item.ramesses_curse.seal_of_kadesh.curse_2_title", "item.ramesses_curse.seal_of_kadesh.curse_2_detail", false,
                            tooltipArgument("luck", yellowNumber(formatNumber(Config.CURSE_2_LUCK_PENALTY.get()))),
                            tooltipArgument("interval", yellowNumber(formatTicksAsMinutes(Config.CURSE_2_SPAWN_INTERVAL.get()))),
                            tooltipArgument("chance", yellowPercent(Config.CURSE_2_SPAWN_CHANCE.get())),
                            tooltipArgument("count", yellowNumber(1)),
                            tooltipArgument("radius", yellowNumber(Config.CURSE_2_SPAWN_RADIUS.get()))));
                }
                if (!curseThreeFourLifted) {
                    tooltip.add(curseTemplateLine("item.ramesses_curse.seal_of_kadesh.curse_3_title", "item.ramesses_curse.seal_of_kadesh.curse_3_detail", false,
                            tooltipArgument("xp", yellowPercentInverse(Config.CURSE_3_XP_MULTIPLIER.get())),
                            tooltipArgument("enchant", yellowNumber(formatNumber(Config.CURSE_3_ENCHANT_COST_MULTIPLIER.get()))),
                            tooltipArgument("anvil", yellowNumber(formatNumber(Config.CURSE_3_ANVIL_COST_MULTIPLIER.get())))));
                    tooltip.add(curseTemplateLine("item.ramesses_curse.seal_of_kadesh.curse_4_title", "item.ramesses_curse.seal_of_kadesh.curse_4_detail", false,
                            tooltipArgument("surcharge", yellowPercent(Config.CURSE_4_TRADE_SURCHARGE.get()))));
                }
                if (curseFiveReversed) {
                    tooltip.add(curseTemplateLine("item.ramesses_curse.seal_of_kadesh.curse_5_title", "item.ramesses_curse.seal_of_kadesh.curse_5_detail_reversed", true,
                            tooltipArgument("kills", reversedNumber(1)),
                            tooltipArgument("target", reversedKey("item.ramesses_curse.keyword.hostile_monsters")),
                            tooltipArgument("gain", reversedNumber(1)),
                            tooltipArgument("mercy", reversedKey("item.ramesses_curse.keyword.mercy")),
                            tooltipArgument("mercy_max", fixedNumber(1299)),
                            tooltipArgument("charges", reversedNumber(1)),
                            tooltipArgument("reversal", reversedKey("item.ramesses_curse.keyword.death_reversal")),
                            tooltipArgument("invulnerable_seconds", reversedNumber(10)),
                            tooltipArgument("invulnerability", reversedKey("item.ramesses_curse.keyword.invulnerability")),
                            tooltipArgument("surge_seconds", reversedNumber(30)),
                            tooltipArgument("surge", reversedKey("item.ramesses_curse.keyword.partial_attribute_doubling"))));
                } else if (curseFiveDeclined) {
                    tooltip.add(curseTemplateLine("item.ramesses_curse.seal_of_kadesh.curse_5_title", "item.ramesses_curse.seal_of_kadesh.curse_5_declined_detail", true,
                            tooltipArgument("attributes", reversedKey("item.ramesses_curse.keyword.war_blood_armor_steps")),
                            tooltipArgument("harvest", reversedKey("item.ramesses_curse.keyword.doubled_harvest"))));
                } else {
                    tooltip.add(curseTemplateLine("item.ramesses_curse.seal_of_kadesh.curse_5_title", "item.ramesses_curse.seal_of_kadesh.curse_5_detail", false,
                            tooltipArgument("kills", yellowNumber(1)),
                            tooltipArgument("guilt_gain", yellowNumber(1)),
                            tooltipArgument("guilt_max", yellowNumber(Config.CURSE_5_MAX_GUILT.get())),
                            tooltipArgument("lock_minutes", yellowNumber(formatTicksAsMinutes(Config.CURSE_5_LOCK_DURATION.get()))),
                            tooltipArgument("lightning", yellowNumber(Config.CURSE_5_LIGHTNING_STRIKES.get()))));
                }
                if (curseSixReversed) {
                    tooltip.add(curseLine("item.ramesses_curse.seal_of_kadesh.curse_6_title", "item.ramesses_curse.seal_of_kadesh.curse_6_detail_reversed", true));
                } else {
                    tooltip.add(curseTemplateLine("item.ramesses_curse.seal_of_kadesh.curse_6_title", "item.ramesses_curse.seal_of_kadesh.curse_6_detail", false,
                            tooltipArgument("wear_delay", yellowNumber(formatTicksAsMinutes(Config.MAIN_HAND_LOCK_DELAY.get()))),
                            tooltipArgument("main_lock", yellowNumber(formatTicksAsMinutes(Config.MAIN_HAND_LOCK_DURATION.get()))),
                            tooltipArgument("interval", yellowNumber(formatTicksAsMinutes(Config.INVENTORY_LOCK_INTERVAL.get()))),
                            tooltipArgument("count", yellowNumber(1)),
                            tooltipArgument("duration", yellowNumber(formatTicksAsMinutes(Config.INVENTORY_LOCK_DURATION.get())))));
                }
                tooltip.add(curseTemplateLine("item.ramesses_curse.seal_of_kadesh.curse_7_title", "item.ramesses_curse.seal_of_kadesh.curse_7_detail", false,
                        tooltipArgument("weakness", yellowNumber(Config.END_WEAKNESS_AMPLIFIER.get() + 1))));
                if (curseEightReversed) {
                    tooltip.add(curseTemplateLine("item.ramesses_curse.seal_of_kadesh.curse_8_title", "item.ramesses_curse.seal_of_kadesh.curse_8_detail_reversed", true,
                            tooltipArgument("chance", reversedPercent(Config.HELL_REBOUND_CHANCE.get()))));
                } else {
                    tooltip.add(curseTemplateLine("item.ramesses_curse.seal_of_kadesh.curse_8_title", "item.ramesses_curse.seal_of_kadesh.curse_8_detail", false,
                            tooltipArgument("chance", yellowPercent(Config.HELL_REBOUND_CHANCE.get())),
                            tooltipArgument("extra_damage", yellowPercent(Config.HELL_REBOUND_DAMAGE_MULTIPLIER.get() - 1.0D))));
                }
                if (curseNineReversed) {
                    tooltip.add(curseLine("item.ramesses_curse.seal_of_kadesh.curse_9_title", "item.ramesses_curse.seal_of_kadesh.curse_9_detail_reversed", true));
                } else {
                    tooltip.add(curseTemplateLine("item.ramesses_curse.seal_of_kadesh.curse_9_title", "item.ramesses_curse.seal_of_kadesh.curse_9_detail", false,
                            tooltipArgument("hunger_loss", blueKey("item.ramesses_curse.keyword.two_hunger_points")),
                            tooltipArgument("crystal", blueKey("item.ramesses_curse.keyword.one_satiety_crystal")),
                            tooltipArgument("debt", blueKey("item.ramesses_curse.keyword.hunger_debt")),
                            tooltipArgument("debt_max", blueText(Integer.toString(Config.CURSE_9_MAX_SATIETY.get()))),
                            tooltipArgument("drain_rate", blueKey("item.ramesses_curse.keyword.five_seconds_one_point")),
                            tooltipArgument("penalty_duration", blueKey("item.ramesses_curse.keyword.three_minutes")),
                            tooltipArgument("food_types", blueText(Integer.toString(Config.CURSE_9_REVERSED_FOOD_TYPES.get())))));
                }
            }
            if (Screen.hasAltDown()) {
                tooltip.add(Component.empty());
                tooltip.add(blueKey("item.ramesses_curse.seal_of_kadesh.section_story"));
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

        private static MutableComponent purpleKey(String key, Object... args) {
            return Component.translatable(key, args).withStyle(ChatFormatting.DARK_PURPLE);
        }

        private static MutableComponent blueKey(String key, Object... args) {
            return Component.translatable(key, args).withStyle(ChatFormatting.AQUA);
        }

        private static MutableComponent curseLine(String titleKey, String descKey, boolean reversed, Object... args) {
            ChatFormatting descColor = reversed ? ChatFormatting.GREEN : ChatFormatting.DARK_PURPLE;
            return purpleText("- ")
                    .append(blueKey(titleKey))
                    .append(Component.translatable(descKey, args).withStyle(descColor));
        }

        private static MutableComponent curseTemplateLine(String titleKey, String descKey, boolean reversed, TooltipArgument... arguments) {
            ChatFormatting descColor = reversed ? ChatFormatting.GREEN : ChatFormatting.DARK_PURPLE;
            Object[] values = new Object[arguments.length];
            for (int i = 0; i < arguments.length; i++) {
                values[i] = arguments[i].value();
            }
            return purpleText("- ")
                    .append(blueKey(titleKey))
                    .append(Component.translatable(descKey, values).withStyle(descColor));
        }

        private static TooltipArgument tooltipArgument(String name, Component value) {
            return new TooltipArgument(name, value);
        }

        private record TooltipArgument(String name, Component value) {
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

        private static MutableComponent reversedText(String text) {
            return Component.literal(text).withStyle(ChatFormatting.GREEN);
        }

        private static MutableComponent reversedKeyword(String text) {
            return Component.literal(text).withStyle(ChatFormatting.GREEN);
        }

        private static MutableComponent reversedKey(String key) {
            return Component.translatable(key).withStyle(ChatFormatting.GREEN);
        }

        private static MutableComponent reversedNumber(double value) {
            return Component.literal(formatNumber(value)).withStyle(ChatFormatting.YELLOW);
        }

        private static MutableComponent reversedNumber(int value) {
            return Component.literal(Integer.toString(value)).withStyle(ChatFormatting.YELLOW);
        }

        private static MutableComponent reversedNumber(String value) {
            return Component.literal(value).withStyle(ChatFormatting.YELLOW);
        }

        private static MutableComponent reversedPercent(double value) {
            return Component.literal(formatNumber(formatPercent(value)) + "%").withStyle(ChatFormatting.YELLOW);
        }

        private static MutableComponent fixedNumber(int value) {
            return Component.literal(Integer.toString(value)).withStyle(ChatFormatting.GOLD);
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

    public static class DynastyPressurePotion extends Item {

        public DynastyPressurePotion() {
            super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
            ItemStack stack = player.getItemInHand(usedHand);
            player.startUsingItem(usedHand);
            return InteractionResultHolder.consume(stack);
        }

        @Override
        public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
            if (livingEntity instanceof ServerPlayer player) {
                int pressure = ModEvents.drinkDynastyPressurePotion(player);
                player.displayClientMessage(Component.translatable("message.ramesses_curse.dynasty_pressure_increased", pressure)
                        .withStyle(ChatFormatting.GOLD), true);
                level.playSound(null, player.blockPosition(), SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 0.75F, 1.15F);
            }

            if (livingEntity instanceof Player player && player.getAbilities().instabuild) {
                return stack;
            }

            stack.shrink(1);
            ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
            if (stack.isEmpty()) {
                return bottle;
            }
            if (livingEntity instanceof Player player && !player.getInventory().add(bottle)) {
                player.drop(bottle, false);
            }
            return stack;
        }

        @Override
        public int getUseDuration(ItemStack stack) {
            return 32;
        }

        @Override
        public UseAnim getUseAnimation(ItemStack stack) {
            return UseAnim.DRINK;
        }

        @Override
        public boolean isFoil(ItemStack stack) {
            return true;
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable("item.ramesses_curse.dynasty_pressure_potion.effect",
                    Component.literal("+100").withStyle(ChatFormatting.RED)).withStyle(ChatFormatting.YELLOW));
        }
    }

    public static class SatietyCrystal extends Item {

        public SatietyCrystal() {
            super(new Item.Properties().stacksTo(64).rarity(Rarity.RARE));
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
            ItemStack stack = player.getItemInHand(usedHand);
            if (level.isClientSide) {
                return InteractionResultHolder.pass(stack);
            }
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResultHolder.pass(stack);
            }
            if (!ModEvents.tryUseSatietyCrystalFromInventory(serverPlayer)) {
                return InteractionResultHolder.pass(stack);
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResultHolder.success(stack);
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
                    openRiddleScreen();
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

        @OnlyIn(Dist.CLIENT)
        private static void openRiddleScreen() {
            Minecraft.getInstance().setScreen(new EmberRiddleInputScreen());
        }

        @Override
        @OnlyIn(Dist.CLIENT)
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
                return;
            }

            MusicPlaybackState playbackState = ModEvents.findNearbyTrackedDisc(player, DETECTION_RADIUS);
            if (playbackState == null) {
                removeBuffs(player);
                syncAwakened(player, false, false);
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
            boolean changed = data.getBoolean(ACTIVATED_KEY)
                    || data.getBoolean(LAST_SYNC_ACTIVATED_KEY)
                    || !data.getString(LAST_JUKEBOX_KEY).isEmpty();
            data.putBoolean(ACTIVATED_KEY, false);
            clearTracking(data);
            if (changed) {
                syncAwakened(player, false, true);
                refreshMusicSlot(player);
            }
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

    public static class EternalSilenceTotem extends Item implements ICurioItem {
        public static final String BODY_SLOT_ID = "body";
        private static final String DATA_KEY = "RamessesEternalSilenceTotem";
        private static final String INVULNERABLE_UNTIL_KEY = "InvulnerableUntil";
        private static final String SURGE_UNTIL_KEY = "SurgeUntil";
        private static final int MAX_DURABILITY = 30;
        private static final int DURABILITY_PER_USE = 6;
        private static final int INVULNERABLE_TICKS = 30 * 20;
        private static final int SURGE_TICKS = 15 * 20;
        private static final UUID MAX_HEALTH_MODIFIER_ID = UUID.fromString("5676b94b-02fc-480a-8d39-840df3dd299f");
        private static final UUID EQUIPPED_DAMAGE_MODIFIER_ID = UUID.fromString("3bd798e5-70a5-4daa-8b31-9b69a44bb957");
        private static final UUID SURGE_DAMAGE_MODIFIER_ID = UUID.fromString("5205285f-ac80-44d6-9287-7b7bda7da39f");
        private static final UUID SURGE_ATTACK_SPEED_MODIFIER_ID = UUID.fromString("01617d45-6924-45c0-9a7c-3cb8f86d235f");

        public EternalSilenceTotem() {
            this(new Item.Properties()
                    .durability(MAX_DURABILITY)
                    .rarity(Rarity.EPIC));
        }

        protected EternalSilenceTotem(Item.Properties properties) {
            super(properties);
        }

        @Override
        public Component getName(ItemStack stack) {
            return super.getName(stack).copy().withStyle(ChatFormatting.GOLD);
        }

        @Override
        public boolean canEquip(SlotContext slotContext, ItemStack stack) {
            return BODY_SLOT_ID.equals(slotContext.identifier()) && slotContext.entity() instanceof Player;
        }

        @Override
        public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
            return false;
        }

        @Override
        public boolean isRepairable(ItemStack stack) {
            return false;
        }

        @Override
        public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
                SlotContext slotContext,
                UUID uuid,
                ItemStack stack) {
            if (!BODY_SLOT_ID.equals(slotContext.identifier())) {
                return ImmutableMultimap.of();
            }
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.put(Attributes.MAX_HEALTH, new AttributeModifier(
                    MAX_HEALTH_MODIFIER_ID,
                    "eternal_silence_totem_health",
                    -5.0D,
                    AttributeModifier.Operation.ADDITION));
            builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                    EQUIPPED_DAMAGE_MODIFIER_ID,
                    "eternal_silence_totem_damage",
                    0.5D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
            return builder.build();
        }

        @Override
        public void appendHoverText(
                ItemStack stack,
                @Nullable Level level,
                List<Component> tooltip,
                TooltipFlag flag) {
            tooltip.add(Component.translatable("item.ramesses_curse.eternal_silence_totem.double_strike")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("item.ramesses_curse.eternal_silence_totem.revive")
                    .withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.translatable("item.ramesses_curse.eternal_silence_totem.surge")
                    .withStyle(ChatFormatting.AQUA));
        }

        public static boolean isEquipped(Player player) {
            return !findEquippedStack(player).isEmpty();
        }

        public static boolean canRevive(Player player) {
            ItemStack stack = findEquippedStack(player);
            return !stack.isEmpty()
                    && (!stack.is(ETERNAL_SILENCE_REBIRTH_TOTEM.get())
                    || !EternalSilenceRebirthTotem.isCoolingDown(player));
        }

        public static boolean tryRevive(ServerPlayer player) {
            ItemStack stack = findEquippedStack(player);
            if (stack.isEmpty()
                    || (stack.is(ETERNAL_SILENCE_REBIRTH_TOTEM.get())
                    && EternalSilenceRebirthTotem.isCoolingDown(player))) {
                return false;
            }

            player.setHealth(1.0F);
            player.removeAllEffects();
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 45 * 20, 1));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 5 * 20, 1));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40 * 20, 0));

            long gameTime = player.level().getGameTime();
            CompoundTag data = getData(player);
            data.putLong(INVULNERABLE_UNTIL_KEY, gameTime + INVULNERABLE_TICKS);
            data.putLong(SURGE_UNTIL_KEY, gameTime + SURGE_TICKS);
            player.invulnerableTime = Math.max(player.invulnerableTime, INVULNERABLE_TICKS);
            applySurgeModifiers(player);

            CriteriaTriggers.USED_TOTEM.trigger(player, stack);
            player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            player.level().broadcastEntityEvent(player, (byte) 35);
            ModNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new EternalSilenceTotemTriggeredPacket(stack.is(ETERNAL_SILENCE_REBIRTH_TOTEM.get())));
            if (stack.is(ETERNAL_SILENCE_REBIRTH_TOTEM.get())) {
                EternalSilenceRebirthTotem.startCooldown(player);
            } else {
                consumeUse(player, stack);
            }
            return true;
        }

        public static boolean isRevivalInvulnerable(Player player) {
            return getData(player).getLong(INVULNERABLE_UNTIL_KEY) > player.level().getGameTime();
        }

        public static void tickRevivalEffects(Player player) {
            maintainEquippedModifiers(player);
            CompoundTag data = getData(player);
            long gameTime = player.level().getGameTime();
            long invulnerableUntil = data.getLong(INVULNERABLE_UNTIL_KEY);
            if (invulnerableUntil > gameTime) {
                int remaining = (int) Math.min(Integer.MAX_VALUE, invulnerableUntil - gameTime);
                player.invulnerableTime = Math.max(player.invulnerableTime, remaining);
            } else if (invulnerableUntil != 0L) {
                data.remove(INVULNERABLE_UNTIL_KEY);
            }

            long surgeUntil = data.getLong(SURGE_UNTIL_KEY);
            if (surgeUntil > gameTime) {
                applySurgeModifiers(player);
            } else {
                if (surgeUntil != 0L) {
                    data.remove(SURGE_UNTIL_KEY);
                }
                removeSurgeModifiers(player);
            }

            if (player instanceof ServerPlayer serverPlayer) {
                EternalSilenceRebirthTotem.tickCooldown(serverPlayer);
            }
        }

        private static ItemStack findEquippedStack(Player player) {
            return CuriosApi.getCuriosInventory(player)
                    .resolve()
                    .flatMap(inventory -> inventory.getStacksHandler(BODY_SLOT_ID))
                    .map(handler -> {
                        top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler stacks = handler.getStacks();
                        for (int slot = 0; slot < stacks.getSlots(); slot++) {
                            ItemStack stack = stacks.getStackInSlot(slot);
                            if (stack.is(ETERNAL_SILENCE_REBIRTH_TOTEM.get())) {
                                return stack;
                            }
                            if (stack.is(ETERNAL_SILENCE_TOTEM.get())) {
                                return stack;
                            }
                        }
                        return ItemStack.EMPTY;
                    })
                    .orElse(ItemStack.EMPTY);
        }

        private static void consumeUse(ServerPlayer player, ItemStack stack) {
            int nextDamage = stack.getDamageValue() + DURABILITY_PER_USE;
            if (nextDamage >= stack.getMaxDamage()) {
                stack.shrink(1);
                player.level().playSound(
                        null,
                        player.blockPosition(),
                        SoundEvents.ITEM_BREAK,
                        SoundSource.PLAYERS,
                        0.8F,
                        0.85F);
            } else {
                stack.setDamageValue(nextDamage);
            }
            refreshBodySlot(player);
        }

        private static void applySurgeModifiers(Player player) {
            applyModifier(
                    player,
                    Attributes.ATTACK_SPEED,
                    SURGE_ATTACK_SPEED_MODIFIER_ID,
                    "eternal_silence_totem_surge_speed",
                    1.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(
                    player,
                    Attributes.ATTACK_DAMAGE,
                    SURGE_DAMAGE_MODIFIER_ID,
                    "eternal_silence_totem_surge_damage",
                    0.5D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
        }

        private static void maintainEquippedModifiers(Player player) {
            if (isEquipped(player)) {
                applyModifier(
                        player,
                        Attributes.MAX_HEALTH,
                        MAX_HEALTH_MODIFIER_ID,
                        "eternal_silence_totem_health",
                        -5.0D,
                        AttributeModifier.Operation.ADDITION);
                applyModifier(
                        player,
                        Attributes.ATTACK_DAMAGE,
                        EQUIPPED_DAMAGE_MODIFIER_ID,
                        "eternal_silence_totem_damage",
                        0.5D,
                        AttributeModifier.Operation.MULTIPLY_TOTAL);
            } else {
                removeModifier(player, Attributes.MAX_HEALTH, MAX_HEALTH_MODIFIER_ID);
                removeModifier(player, Attributes.ATTACK_DAMAGE, EQUIPPED_DAMAGE_MODIFIER_ID);
            }
        }

        private static void applyModifier(
                Player player,
                Attribute attribute,
                UUID modifierId,
                String name,
                double amount,
                AttributeModifier.Operation operation) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance == null) {
                return;
            }
            AttributeModifier existing = instance.getModifier(modifierId);
            if (existing != null
                    && existing.getAmount() == amount
                    && existing.getOperation() == operation) {
                return;
            }
            instance.removeModifier(modifierId);
            instance.addTransientModifier(new AttributeModifier(
                    modifierId,
                    name,
                    amount,
                    operation));
        }

        private static void removeSurgeModifiers(Player player) {
            removeModifier(player, Attributes.ATTACK_SPEED, SURGE_ATTACK_SPEED_MODIFIER_ID);
            removeModifier(player, Attributes.ATTACK_DAMAGE, SURGE_DAMAGE_MODIFIER_ID);
        }

        private static void removeModifier(Player player, Attribute attribute, UUID modifierId) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) {
                instance.removeModifier(modifierId);
            }
        }

        private static CompoundTag getData(Player player) {
            CompoundTag persistent = player.getPersistentData();
            if (!persistent.contains(Player.PERSISTED_NBT_TAG)) {
                persistent.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
            }
            CompoundTag persisted = persistent.getCompound(Player.PERSISTED_NBT_TAG);
            if (!persisted.contains(DATA_KEY)) {
                persisted.put(DATA_KEY, new CompoundTag());
            }
            return persisted.getCompound(DATA_KEY);
        }

        private static void refreshBodySlot(Player player) {
            CuriosApi.getCuriosInventory(player)
                    .resolve()
                    .flatMap(inventory -> inventory.getStacksHandler(BODY_SLOT_ID))
                    .ifPresent(handler -> {
                        handler.clearCachedModifiers();
                        handler.update();
                    });
        }
    }

    public static class EternalSilenceRebirthTotem extends EternalSilenceTotem {
        public static final int COOLDOWN_TICKS = 90 * 20;
        private static final String COOLDOWN_UNTIL_KEY = "RebirthCooldownUntil";
        private static long clientCooldownUntilGameTime;

        public EternalSilenceRebirthTotem() {
            super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
        }

        @Override
        public void appendHoverText(
                ItemStack stack,
                @Nullable Level level,
                List<Component> tooltip,
                TooltipFlag flag) {
            tooltip.add(Component.translatable("item.ramesses_curse.eternal_silence_rebirth_totem.double_strike")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("item.ramesses_curse.eternal_silence_rebirth_totem.revive")
                    .withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.translatable("item.ramesses_curse.eternal_silence_rebirth_totem.surge")
                    .withStyle(ChatFormatting.AQUA));

            int remainingTicks = getClientCooldownTicks();
            if (remainingTicks > 0) {
                int seconds = Math.max(1, Mth.ceil(remainingTicks / 20.0F));
                tooltip.add(Component.translatable(
                                "item.ramesses_curse.eternal_silence_rebirth_totem.cooldown_remaining",
                                seconds)
                        .withStyle(ChatFormatting.RED));
            } else {
                tooltip.add(Component.translatable("item.ramesses_curse.eternal_silence_rebirth_totem.ready")
                        .withStyle(ChatFormatting.GREEN));
            }
        }

        private static boolean isCoolingDown(Player player) {
            CompoundTag data = EternalSilenceTotem.getData(player);
            long until = data.getLong(COOLDOWN_UNTIL_KEY);
            if (until <= player.level().getGameTime()) {
                data.remove(COOLDOWN_UNTIL_KEY);
                return false;
            }
            return true;
        }

        private static void startCooldown(ServerPlayer player) {
            EternalSilenceTotem.getData(player).putLong(
                    COOLDOWN_UNTIL_KEY,
                    player.level().getGameTime() + COOLDOWN_TICKS);
            player.getCooldowns().addCooldown(ETERNAL_SILENCE_REBIRTH_TOTEM.get(), COOLDOWN_TICKS);
            syncCooldown(player);
        }

        private static void tickCooldown(ServerPlayer player) {
            CompoundTag data = EternalSilenceTotem.getData(player);
            long remaining = data.getLong(COOLDOWN_UNTIL_KEY) - player.level().getGameTime();
            if (remaining <= 0L) {
                if (data.contains(COOLDOWN_UNTIL_KEY)) {
                    data.remove(COOLDOWN_UNTIL_KEY);
                    player.getCooldowns().removeCooldown(ETERNAL_SILENCE_REBIRTH_TOTEM.get());
                    syncCooldown(player);
                }
                return;
            }

            int remainingTicks = (int) Math.min(COOLDOWN_TICKS, remaining);
            if (!player.getCooldowns().isOnCooldown(ETERNAL_SILENCE_REBIRTH_TOTEM.get())) {
                player.getCooldowns().addCooldown(ETERNAL_SILENCE_REBIRTH_TOTEM.get(), remainingTicks);
            }
            if (player.level().getGameTime() % 20L == 0L) {
                syncCooldown(player);
            }
        }

        public static void syncCooldown(ServerPlayer player) {
            long remaining = EternalSilenceTotem.getData(player).getLong(COOLDOWN_UNTIL_KEY)
                    - player.level().getGameTime();
            int remainingTicks = (int) Mth.clamp(remaining, 0L, COOLDOWN_TICKS);
            ModNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new EternalSilenceRebirthCooldownPacket(remainingTicks));
        }

        @OnlyIn(Dist.CLIENT)
        public static void setClientCooldownTicks(int remainingTicks) {
            Minecraft minecraft = Minecraft.getInstance();
            long gameTime = minecraft.level == null ? 0L : minecraft.level.getGameTime();
            clientCooldownUntilGameTime = gameTime + Math.max(0, remainingTicks);
        }

        @OnlyIn(Dist.CLIENT)
        private static int getClientCooldownTicks() {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null) {
                return 0;
            }
            return (int) Mth.clamp(
                    clientCooldownUntilGameTime - minecraft.level.getGameTime(),
                    0L,
                    COOLDOWN_TICKS);
        }
    }

    public static class UraeusNecklace extends Item implements ICurioItem {
        private static final String BURST_GUARD_KEY = "RamessesUraeusBurst";
        private static final String STACK_LEVEL_KEY = "RamessesUraeusStacks";
        private static final String STACK_EXPIRES_AT_KEY = "RamessesUraeusStacksExpireAt";
        private static final String STORM_COOLDOWN_UNTIL_KEY = "RamessesUraeusStormCooldownUntil";
        private static final int STORM_COOLDOWN_TICKS = 30 * 20;
        public UraeusNecklace() {
            super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
        }

        @Override
        public boolean canEquip(SlotContext slotContext, ItemStack stack) {
            return slotContext.entity() instanceof Player;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            if (appendConfiguredTooltip(tooltip)) {
                appendStormTooltip(level, tooltip);
                return;
            }
            tooltip.add(Component.translatable("item.ramesses_curse.uraeus_necklace.tooltip_stack_default", goldText(1), goldText(10))
                    .withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(Component.translatable("item.ramesses_curse.uraeus_necklace.tooltip_burst_default", goldText(8), goldText("300%"), goldText("+50%"), goldText("V"), goldText("V"))
                    .withStyle(ChatFormatting.DARK_PURPLE));
            appendStormTooltip(level, tooltip);
        }

        private static float onAttack(Player player, LivingEntity target, float damage) {
            if (player.level().isClientSide
                    || target instanceof Player
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

            long gameTime = player.level().getGameTime();
            int currentLevel = getStoredStackLevel(target, effect, gameTime);
            int nextLevel = currentLevel + 1;

            if (nextLevel >= Config.NECKLACE_MAX_STACKS.get()) {
                target.getPersistentData().remove(STACK_LEVEL_KEY);
                target.getPersistentData().remove(STACK_EXPIRES_AT_KEY);
                target.removeEffect(effect);

                float burstMultiplier = Config.NECKLACE_BURST_DAMAGE_MULTIPLIER.get().floatValue();
                boolean rainEmpowered = isRainEmpowered(player);
                if (rainEmpowered) {
                    burstMultiplier += (float) (Config.NECKLACE_RAIN_BONUS_MULTIPLIER.get() - 1.0D);
                    rewardRainBurst(player);
                }

                if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    UraeusFlareEntity.spawn(serverLevel, player, target, UraeusFlareEntity.CHAIN_DAMAGE, rainEmpowered);
                }
                return damage * burstMultiplier;
            }

            target.getPersistentData().putInt(STACK_LEVEL_KEY, nextLevel);
            target.getPersistentData().putLong(STACK_EXPIRES_AT_KEY, gameTime + Config.NECKLACE_STACK_TIMEOUT.get());
            target.getPersistentData().putLong(ModEvents.URAEUS_NECKLACE_EFFECT_BYPASS_UNTIL, gameTime);
            target.addEffect(new MobEffectInstance(effect, Config.NECKLACE_STACK_TIMEOUT.get(), nextLevel - 1, false, true, true));
            return damage;
        }

        private static int getStoredStackLevel(LivingEntity target, MobEffect effect, long gameTime) {
            if (target.getPersistentData().getLong(STACK_EXPIRES_AT_KEY) > gameTime) {
                return target.getPersistentData().getInt(STACK_LEVEL_KEY);
            }

            target.getPersistentData().remove(STACK_LEVEL_KEY);
            target.getPersistentData().remove(STACK_EXPIRES_AT_KEY);

            MobEffectInstance existing = target.getEffect(effect);
            return existing == null ? 0 : existing.getAmplifier() + 1;
        }

        private static boolean isStackEffect(LivingEntity target, MobEffect effect) {
            MobEffect necklaceEffect = Config.getNecklaceEffect();
            if (necklaceEffect == null || effect != necklaceEffect) {
                return false;
            }
            return target.getPersistentData().getLong(STACK_EXPIRES_AT_KEY) > target.level().getGameTime()
                    && target.getPersistentData().getInt(STACK_LEVEL_KEY) > 0;
        }

        private static boolean isRainEmpowered(Player player) {
            return player.level().isRaining() && player.level().canSeeSky(player.blockPosition().above());
        }

        private static void rewardRainBurst(Player player) {
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, Config.NECKLACE_RAIN_REWARD_DURATION.get(), Config.NECKLACE_RAIN_ABSORPTION_AMPLIFIER.get(), false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, Config.NECKLACE_RAIN_REWARD_DURATION.get(), Config.NECKLACE_RAIN_RESISTANCE_AMPLIFIER.get(), false, false, true));
        }

        private static int getStormPassiveCooldownTicks(Player player) {
            if (player.level().isClientSide) {
                return ModEvents.ClientOverlayEvents.getClientUraeusStormCooldownTicks();
            }
            long cooldownUntil = player.getPersistentData().getLong(STORM_COOLDOWN_UNTIL_KEY);
            long remaining = cooldownUntil - player.level().getGameTime();
            return remaining > 0L ? (int) Math.min(Integer.MAX_VALUE, remaining) : 0;
        }

        private static void setStormPassiveCooldown(ServerPlayer player) {
            player.getPersistentData().putLong(STORM_COOLDOWN_UNTIL_KEY, player.level().getGameTime() + STORM_COOLDOWN_TICKS);
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new UraeusStormCooldownSyncPacket(STORM_COOLDOWN_TICKS));
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
            tooltip.add(Component.translatable(
                            "item.ramesses_curse.uraeus_necklace.tooltip_stack",
                            effectName.copy().withStyle(ChatFormatting.GOLD),
                            Component.literal(formatSeconds(Config.NECKLACE_STACK_TIMEOUT.get())).withStyle(ChatFormatting.GOLD))
                    .withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(Component.translatable(
                            "item.ramesses_curse.uraeus_necklace.tooltip_burst",
                            Component.literal(Integer.toString(Config.NECKLACE_MAX_STACKS.get())).withStyle(ChatFormatting.GOLD),
                            Component.literal(formatPercent(Config.NECKLACE_BURST_DAMAGE_MULTIPLIER.get())).withStyle(ChatFormatting.GOLD),
                            Component.literal("+" + formatPercentDelta(Config.NECKLACE_RAIN_BONUS_MULTIPLIER.get())).withStyle(ChatFormatting.GOLD),
                            Component.literal(getRomanLevel(Config.NECKLACE_RAIN_ABSORPTION_AMPLIFIER.get() + 1)).withStyle(ChatFormatting.GOLD),
                            Component.literal(getRomanLevel(Config.NECKLACE_RAIN_RESISTANCE_AMPLIFIER.get() + 1)).withStyle(ChatFormatting.GOLD))
                    .withStyle(ChatFormatting.DARK_PURPLE));
            return true;
        }

        @OnlyIn(Dist.CLIENT)
        private static void appendStormTooltip(@Nullable Level level, List<Component> tooltip) {
            tooltip.add(Component.translatable(
                            "item.ramesses_curse.uraeus_necklace.tooltip_storm_star",
                            goldText(50),
                            goldText(5),
                            goldText(3),
                            goldText(30))
                    .withStyle(ChatFormatting.AQUA));

            Player player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }
            int cooldownTicks = getStormPassiveCooldownTicks(player);
            if (cooldownTicks > 0) {
                tooltip.add(Component.translatable(
                                "item.ramesses_curse.uraeus_necklace.tooltip_storm_cooldown",
                                Component.literal(formatSeconds(cooldownTicks)).withStyle(ChatFormatting.GOLD))
                        .withStyle(ChatFormatting.GRAY));
            }
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
        @OnlyIn(Dist.CLIENT)
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable("item.ramesses_curse.edict_of_two_lands.desc")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(Component.translatable("item.ramesses_curse.edict_of_two_lands.hint_shift")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            if (Screen.hasShiftDown()) {
                tooltip.add(Component.empty());
                tooltip.add(Component.translatable("item.ramesses_curse.edict_of_two_lands.tooltip_x", aquaText("X"), aquaText(3))
                        .withStyle(ChatFormatting.DARK_PURPLE));
                tooltip.add(Component.translatable("item.ramesses_curse.edict_of_two_lands.tooltip_y", aquaText("Y"), aquaText(3), aquaText(15))
                        .withStyle(ChatFormatting.DARK_PURPLE));
                tooltip.add(Component.translatable(
                                "item.ramesses_curse.edict_of_two_lands.tooltip_cost",
                                aquaText(GUILT_COST),
                                aquaText(COOLDOWN_TICKS / 20),
                                aquaText(DOMAIN_GUILT_COST),
                                aquaText(DOMAIN_COOLDOWN_TICKS / 20))
                        .withStyle(ChatFormatting.DARK_PURPLE));
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
            ModNetwork.CHANNEL.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new EdictWorldEffectPacket(
                            player.getX(),
                            player.getY() + 0.08D,
                            player.getZ(),
                            domain ? 15.0F : 6.0F,
                            domain ? 180 : 90,
                            domain));
            return EdictActivationResult.SUCCESS;
        }
        private static int getCooldownTicks(Player player) {
            return getSharedCooldownTicks(player);
        }
        private static int getCastGuiltCost() {
            return GUILT_COST;
        }
        private static int getDomainGuiltCost() {
            return DOMAIN_GUILT_COST;
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
        @OnlyIn(Dist.CLIENT)
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable("item.ramesses_curse.heart_of_duat.skill_active_detail",
                            goldText(Config.HEART_TRIGGER_RADIUS.get()),
                            goldText(Config.HEART_TRIGGER_HOSTILE_COUNT.get()),
                            goldKey("item.ramesses_curse.keyword.strength_iii"),
                            goldKey("item.ramesses_curse.keyword.highlight_hostiles"),
                            goldText(Config.HEART_ACTIVE_XP_COST.get()))
                    .withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(Component.translatable("item.ramesses_curse.heart_of_duat.skill_passive_detail",
                            goldKey("item.ramesses_curse.keyword.deserts"),
                            goldKey("item.ramesses_curse.keyword.badlands"),
                            goldKey("item.ramesses_curse.keyword.luck_ii"),
                            Component.translatable("item.ramesses_curse.keyword.entity_reach",
                                    goldText(Config.HEART_DESERT_REACH_BONUS.get())).withStyle(ChatFormatting.GOLD))
                    .withStyle(ChatFormatting.DARK_PURPLE));
            int battleStackLimit = Math.max(1, Config.HEART_BATTLE_STACK_LIMIT.get());
            double damagePerStack = Config.HEART_BATTLE_DAMAGE_PER_STACK.get();
            tooltip.add(Component.translatable("item.ramesses_curse.heart_of_duat.skill_battle_detail",
                            goldText(1),
                            goldText(battleStackLimit),
                            goldText(Config.HEART_BATTLE_STACK_TIMEOUT.get() / 20.0D),
                            goldText(damagePerStack),
                            goldText(damagePerStack * battleStackLimit),
                            goldText(battleStackLimit),
                            goldText(Config.HEART_WARD_INVULNERABILITY.get() / 20.0D),
                            goldText(1),
                            goldText(Config.HEART_WARD_COOLDOWN.get() / 20.0D))
                    .withStyle(ChatFormatting.DARK_PURPLE));

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
