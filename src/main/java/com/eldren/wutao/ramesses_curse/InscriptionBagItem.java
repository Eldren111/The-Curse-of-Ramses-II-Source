package com.eldren.wutao.ramesses_curse;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class InscriptionBagItem extends Item implements ICurioItem {
    public static final String SLOT_ID = "ramesses_curse_inscription_bag";
    private static final int CAPACITY = 16;
    private static final String INVENTORY_TAG = "RamessesInscriptionInventory";
    private static final UUID[] MODIFIER_IDS = createModifierIds();
    private static final String ACTIVE_MASK_KEY = "RamessesInscriptionBagMask";
    private static final String MASK_REFRESH_KEY = "RamessesInscriptionBagMaskRefresh";
    private static final String DODGE_READY_AT_KEY = "RamessesInscriptionDodgeReadyAt";
    private static final String BARRIER_READY_AT_KEY = "RamessesInscriptionBarrierReadyAt";
    private static final String REVENGE_UNTIL_KEY = "RamessesInscriptionRevengeUntil";
    private static final String STILL_TICKS_KEY = "RamessesInscriptionStillTicks";
    private static final String STILL_ACTIVE_KEY = "RamessesInscriptionStillActive";
    private static final Set<UUID> ECHO_DAMAGE_GUARD = new HashSet<>();

    public InscriptionBagItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(
                    serverPlayer,
                    new SimpleMenuProvider(
                            (containerId, inventory, ignored) ->
                                    new InscriptionBagMenu(containerId, inventory, stack, hand),
                            Component.translatable("screen.ramesses_curse.inscription_bag")),
                    buffer -> buffer.writeEnum(hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return SLOT_ID.equals(slotContext.identifier()) && slotContext.entity() instanceof Player;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext,
            UUID uuid,
            ItemStack stack) {
        if (!SLOT_ID.equals(slotContext.identifier())) {
            return ImmutableMultimap.of();
        }

        ImmutableMultimap.Builder<Attribute, AttributeModifier> modifiers = ImmutableMultimap.builder();
        stack.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            boolean[] applied = new boolean[26];
            int uniqueCount = 0;
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                int fragment = getFragmentIndex(handler.getStackInSlot(slot));
                if (fragment > 0 && !applied[fragment - 1]) {
                    applied[fragment - 1] = true;
                    uniqueCount++;
                    addFragmentModifier(modifiers, fragment);
                }
            }
            if (applied[25]) {
                double resonance = (uniqueCount / 4) * 0.03D;
                if (resonance > 0.0D) {
                    modifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                            MODIFIER_IDS[25], "ramesses_inscription_resonance_damage", resonance,
                            AttributeModifier.Operation.MULTIPLY_TOTAL));
                    modifiers.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(
                            MODIFIER_IDS[25], "ramesses_inscription_resonance_speed", resonance,
                            AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
            }
        });
        return modifiers.build();
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack previousStack, ItemStack stack) {
        if (slotContext.entity() instanceof Player player && !player.level().isClientSide) {
            updateActiveMask(player, stack);
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (slotContext.entity() instanceof Player player && !player.level().isClientSide) {
            clearActiveState(player);
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player) || player.level().isClientSide) {
            return;
        }
        long gameTime = player.level().getGameTime();
        CompoundTag data = player.getPersistentData();
        if (gameTime >= data.getLong(MASK_REFRESH_KEY)) {
            updateActiveMask(player, stack);
            data.putLong(MASK_REFRESH_KEY, gameTime + 20L);
        }
        tickStillness(player);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int stored = stack.getCapability(ForgeCapabilities.ITEM_HANDLER)
                .map(handler -> {
                    int count = 0;
                    for (int slot = 0; slot < handler.getSlots(); slot++) {
                        if (!handler.getStackInSlot(slot).isEmpty()) {
                            count++;
                        }
                    }
                    return count;
                })
                .orElse(0);
        tooltip.add(Component.translatable("item.ramesses_curse.inscription_bag.capacity", stored, CAPACITY)
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.ramesses_curse.inscription_bag.use")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.ramesses_curse.inscription_bag.unique")
                .withStyle(ChatFormatting.DARK_PURPLE));
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new BagCapabilityProvider(stack, nbt);
    }

    public static int getFragmentIndex(ItemStack stack) {
        if (stack.getItem() instanceof InscriptionFragmentItem fragment) {
            return fragment.getFragmentIndex();
        }
        return 0;
    }

    public static float modifyIncomingDamage(ServerPlayer player, DamageSource source, float damage) {
        if (damage <= 0.0F) {
            return damage;
        }
        CompoundTag data = player.getPersistentData();
        long gameTime = player.level().getGameTime();

        if (hasActiveFragment(player, 11)
                && gameTime >= data.getLong(DODGE_READY_AT_KEY)
                && player.getRandom().nextFloat() < 0.12F) {
            data.putLong(DODGE_READY_AT_KEY, gameTime + 20L);
            player.level().playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_LEATHER,
                    SoundSource.PLAYERS, 0.8F, 1.8F);
            return 0.0F;
        }

        float adjusted = damage;
        if (hasActiveFragment(player, 20) && source.is(DamageTypeTags.IS_PROJECTILE)) {
            adjusted *= 0.70F;
        }
        if (hasActiveFragment(player, 21)
                && (source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC))) {
            adjusted *= 0.75F;
        }
        if (hasActiveFragment(player, 22) && data.getBoolean(STILL_ACTIVE_KEY)) {
            adjusted *= 0.75F;
        }
        if (hasActiveFragment(player, 23)
                && player.getHealth() <= player.getMaxHealth() * 0.30F) {
            adjusted *= 0.75F;
        }

        if (hasActiveFragment(player, 17) && gameTime >= data.getLong(BARRIER_READY_AT_KEY)) {
            adjusted = Math.max(0.0F, adjusted - 12.0F);
            data.putLong(BARRIER_READY_AT_KEY, gameTime + 25L * 20L);
            player.level().playSound(null, player.blockPosition(), SoundEvents.SHIELD_BLOCK,
                    SoundSource.PLAYERS, 0.9F, 0.9F);
        }

        if (adjusted > 0.0F) {
            if (hasActiveFragment(player, 15)) {
                data.putLong(REVENGE_UNTIL_KEY, gameTime + 5L * 20L);
            }
            Entity attacker = source.getEntity();
            if (hasActiveFragment(player, 16)
                    && attacker instanceof LivingEntity livingAttacker
                    && source.getDirectEntity() == livingAttacker
                    && livingAttacker != player
                    && player.getRandom().nextFloat() < 0.25F) {
                livingAttacker.hurt(player.damageSources().thorns(player), 4.0F);
            }
        }
        return adjusted;
    }

    public static float modifyOutgoingDamage(
            ServerPlayer player,
            LivingEntity target,
            DamageSource source,
            float damage) {
        if (damage <= 0.0F || ECHO_DAMAGE_GUARD.contains(player.getUUID())) {
            return damage;
        }

        float adjusted = damage;
        boolean directMelee = source.getDirectEntity() == player;
        if (directMelee && hasActiveFragment(player, 12) && player.getRandom().nextFloat() < 0.15F) {
            adjusted *= 2.0F;
            player.level().playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT,
                    SoundSource.PLAYERS, 0.8F, 1.1F);
        }
        if (hasActiveFragment(player, 14)
                && target.getHealth() <= target.getMaxHealth() * 0.25F) {
            adjusted *= 1.25F;
        }
        CompoundTag data = player.getPersistentData();
        if (directMelee
                && hasActiveFragment(player, 15)
                && data.getLong(REVENGE_UNTIL_KEY) >= player.level().getGameTime()) {
            adjusted *= 1.30F;
            data.remove(REVENGE_UNTIL_KEY);
        }
        return adjusted;
    }

    public static void afterDamageDealt(
            ServerPlayer player,
            LivingEntity target,
            DamageSource source,
            float damage) {
        if (damage <= 0.0F || ECHO_DAMAGE_GUARD.contains(player.getUUID())) {
            return;
        }
        boolean directMelee = source.getDirectEntity() == player;
        if (directMelee && hasActiveFragment(player, 13)) {
            player.heal(Math.min(3.0F, damage * 0.05F));
        }
        if (directMelee
                && hasActiveFragment(player, 25)
                && player.getRandom().nextFloat() < 0.12F
                && player.getServer() != null) {
            float echoDamage = damage * 0.75F;
            player.getServer().execute(() -> {
                if (!player.isAlive() || !target.isAlive() || player.level() != target.level()
                        || !ECHO_DAMAGE_GUARD.add(player.getUUID())) {
                    return;
                }
                try {
                    target.invulnerableTime = 0;
                    target.hurt(player.damageSources().playerAttack(player), echoDamage);
                } finally {
                    ECHO_DAMAGE_GUARD.remove(player.getUUID());
                }
            });
        }
    }

    public static void onKill(ServerPlayer player, LivingEntity target) {
        if (!(target instanceof net.minecraft.world.entity.monster.Enemy)) {
            return;
        }
        if (hasActiveFragment(player, 18)) {
            player.heal(4.0F);
        }
        if (hasActiveFragment(player, 19)) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 5 * 20, 1));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 5 * 20, 0));
        }
    }

    public static int modifyExperience(ServerPlayer player, int experience) {
        return hasActiveFragment(player, 24)
                ? Math.max(0, (int) Math.floor(experience * 1.5D))
                : experience;
    }

    private static boolean hasActiveFragment(Player player, int fragment) {
        return (player.getPersistentData().getInt(ACTIVE_MASK_KEY) & (1 << (fragment - 1))) != 0;
    }

    private static void updateActiveMask(Player player, ItemStack stack) {
        int[] mask = {0};
        stack.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                int fragment = getFragmentIndex(handler.getStackInSlot(slot));
                if (fragment > 0) {
                    mask[0] |= 1 << (fragment - 1);
                }
            }
        });
        player.getPersistentData().putInt(ACTIVE_MASK_KEY, mask[0]);
    }

    private static void tickStillness(Player player) {
        CompoundTag data = player.getPersistentData();
        if (!hasActiveFragment(player, 22)) {
            data.remove(STILL_TICKS_KEY);
            data.remove(STILL_ACTIVE_KEY);
            return;
        }

        double horizontalSpeedSqr = player.getDeltaMovement().x * player.getDeltaMovement().x
                + player.getDeltaMovement().z * player.getDeltaMovement().z;
        if (horizontalSpeedSqr <= 0.0004D && player.onGround()) {
            int stillTicks = Math.min(40, data.getInt(STILL_TICKS_KEY) + 1);
            data.putInt(STILL_TICKS_KEY, stillTicks);
            data.putBoolean(STILL_ACTIVE_KEY, stillTicks >= 40);
        } else {
            data.putInt(STILL_TICKS_KEY, 0);
            data.putBoolean(STILL_ACTIVE_KEY, false);
        }
    }

    private static void clearActiveState(Player player) {
        CompoundTag data = player.getPersistentData();
        data.remove(ACTIVE_MASK_KEY);
        data.remove(MASK_REFRESH_KEY);
        data.remove(REVENGE_UNTIL_KEY);
        data.remove(STILL_TICKS_KEY);
        data.remove(STILL_ACTIVE_KEY);
    }

    private static void addFragmentModifier(
            ImmutableMultimap.Builder<Attribute, AttributeModifier> modifiers,
            int fragment) {
        Attribute attribute;
        double amount;
        AttributeModifier.Operation operation = AttributeModifier.Operation.ADDITION;
        switch (fragment) {
            case 1 -> { attribute = Attributes.MAX_HEALTH; amount = 6.0D; }
            case 2 -> { attribute = Attributes.ATTACK_DAMAGE; amount = 3.0D; }
            case 3 -> { attribute = Attributes.ATTACK_SPEED; amount = 0.15D; operation = AttributeModifier.Operation.MULTIPLY_TOTAL; }
            case 4 -> { attribute = Attributes.MOVEMENT_SPEED; amount = 0.12D; operation = AttributeModifier.Operation.MULTIPLY_TOTAL; }
            case 5 -> { attribute = Attributes.ARMOR; amount = 5.0D; }
            case 6 -> { attribute = Attributes.ARMOR_TOUGHNESS; amount = 3.0D; }
            case 7 -> { attribute = Attributes.KNOCKBACK_RESISTANCE; amount = 0.20D; }
            case 8 -> { attribute = Attributes.LUCK; amount = 3.0D; }
            case 9 -> { attribute = ForgeMod.ENTITY_REACH.get(); amount = 1.5D; }
            case 10 -> { attribute = ForgeMod.BLOCK_REACH.get(); amount = 1.5D; }
            default -> { return; }
        }
        modifiers.put(attribute, new AttributeModifier(
                MODIFIER_IDS[fragment - 1],
                "ramesses_inscription_fragment_" + fragment,
                amount,
                operation));
    }

    private static UUID[] createModifierIds() {
        UUID[] ids = new UUID[26];
        for (int index = 0; index < ids.length; index++) {
            ids[index] = UUID.nameUUIDFromBytes(
                    (RamessesCurseMod.MODID + ":inscription_fragment_" + (index + 1))
                            .getBytes(StandardCharsets.UTF_8));
        }
        return ids;
    }

    private static final class BagCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
        private final ItemStack owner;
        private final UniqueInscriptionHandler handler;
        private final LazyOptional<IItemHandler> optional;

        private BagCapabilityProvider(ItemStack owner, @Nullable CompoundTag legacyCapabilityNbt) {
            this.owner = owner;
            this.handler = new UniqueInscriptionHandler(owner);
            this.optional = LazyOptional.of(() -> handler);
            CompoundTag storedInventory = owner.getTagElement(INVENTORY_TAG);
            if (storedInventory != null) {
                handler.deserializeNBT(storedInventory);
            } else if (legacyCapabilityNbt != null && !legacyCapabilityNbt.isEmpty()) {
                handler.deserializeNBT(legacyCapabilityNbt);
            }
        }

        @Override
        public <T> @NotNull LazyOptional<T> getCapability(
                @NotNull Capability<T> capability,
                @Nullable net.minecraft.core.Direction side) {
            return capability == ForgeCapabilities.ITEM_HANDLER ? optional.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return handler.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            CompoundTag storedInventory = owner.getTagElement(INVENTORY_TAG);
            handler.deserializeNBT(storedInventory != null ? storedInventory : nbt);
        }
    }

    private static final class UniqueInscriptionHandler extends ItemStackHandler {
        private final ItemStack owner;
        private boolean loading;

        private UniqueInscriptionHandler(ItemStack owner) {
            super(CAPACITY);
            this.owner = owner;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            int fragment = getFragmentIndex(stack);
            if (fragment <= 0) {
                return false;
            }
            for (int index = 0; index < getSlots(); index++) {
                if (index != slot && getFragmentIndex(getStackInSlot(index)) == fragment) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            loading = true;
            try {
                super.deserializeNBT(nbt);
                boolean[] seen = new boolean[26];
                for (int slot = 0; slot < getSlots(); slot++) {
                    int fragment = getFragmentIndex(getStackInSlot(slot));
                    if (fragment <= 0 || seen[fragment - 1]) {
                        setStackInSlot(slot, ItemStack.EMPTY);
                        continue;
                    }
                    seen[fragment - 1] = true;
                    if (getStackInSlot(slot).getCount() > 1) {
                        getStackInSlot(slot).setCount(1);
                    }
                }
            } finally {
                loading = false;
            }
            saveToOwner();
        }

        @Override
        protected void onContentsChanged(int slot) {
            if (!loading) {
                saveToOwner();
            }
        }

        private void saveToOwner() {
            owner.getOrCreateTag().put(INVENTORY_TAG, serializeNBT());
        }
    }
}
