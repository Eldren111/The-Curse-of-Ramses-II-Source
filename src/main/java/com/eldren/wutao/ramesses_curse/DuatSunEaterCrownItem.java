package com.eldren.wutao.ramesses_curse;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class DuatSunEaterCrownItem extends Item implements ICurioItem {
    private static final String CHARM_SLOT_ID = "charm";
    private static final String DRAGON_DROP_KEY = "RamessesSunEaterDragonDrop";
    private static final String POWER_KEY = "RamessesSunEaterCrownPower";
    private static final float TRIGGER_CHANCE = 0.25F;
    private static final float HEALTH_DAMAGE_RATIO = 0.25F;
    private static final Set<UUID> ACTIVE_TARGETS = new HashSet<>();
    private static final List<CrownPower> POWERS = List.of(
            new CrownPower("physical", DamageTypes.PLAYER_ATTACK),
            new CrownPower("projectile", DamageTypes.ARROW),
            new CrownPower("explosion", DamageTypes.PLAYER_EXPLOSION),
            new CrownPower("fire", DamageTypes.ON_FIRE),
            new CrownPower("lava", DamageTypes.LAVA),
            new CrownPower("lightning", DamageTypes.LIGHTNING_BOLT),
            new CrownPower("magic", DamageTypes.MAGIC),
            new CrownPower("wither", DamageTypes.WITHER),
            new CrownPower("dragon_breath", DamageTypes.DRAGON_BREATH),
            new CrownPower("sonic_boom", DamageTypes.SONIC_BOOM),
            new CrownPower("freeze", DamageTypes.FREEZE),
            new CrownPower("drown", DamageTypes.DROWN),
            new CrownPower("suffocation", DamageTypes.IN_WALL),
            new CrownPower("cramming", DamageTypes.CRAMMING),
            new CrownPower("fall", DamageTypes.FALL),
            new CrownPower("void", DamageTypes.FELL_OUT_OF_WORLD),
            new CrownPower("world_border", DamageTypes.OUTSIDE_BORDER),
            new CrownPower("thorns", DamageTypes.THORNS),
            new CrownPower("true_damage", DamageTypes.GENERIC_KILL));

    public DuatSunEaterCrownItem() {
        super(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1));
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return CHARM_SLOT_ID.equals(slotContext.identifier()) && slotContext.entity() instanceof Player;
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack previousStack, ItemStack stack) {
        if (!slotContext.entity().level().isClientSide) {
            assignPowerIfMissing(stack, slotContext.entity());
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!slotContext.entity().level().isClientSide) {
            assignPowerIfMissing(stack, slotContext.entity());
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        appendPowerTooltip(tooltip, stack);
    }

    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        return tooltips;
    }

    private static void appendPowerTooltip(List<Component> tooltip, ItemStack stack) {
        if (!isDragonDrop(stack)) {
            return;
        }
        Optional<CrownPower> selectedPower = getPower(stack);
        Component powerName = selectedPower.map(CrownPower::displayName)
                .orElseGet(() -> Component.translatable("item.ramesses_curse.duat_sun_eater_crown.power.unbound")
                        .withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("item.ramesses_curse.duat_sun_eater_crown.current_power", powerName)
                .withStyle(ChatFormatting.GOLD));
        if (!Screen.hasShiftDown()) {
            tooltip.add(Component.translatable(
                            "item.ramesses_curse.duat_sun_eater_crown.hold_shift",
                            Component.literal("Shift").withStyle(ChatFormatting.GOLD))
                    .withStyle(ChatFormatting.DARK_PURPLE));
            return;
        }

        addSpacedLine(tooltip, Component.translatable("item.ramesses_curse.duat_sun_eater_crown.effect")
                .withStyle(ChatFormatting.RED));
        if (selectedPower.isPresent()) {
            CrownPower power = selectedPower.get();
            addSpacedLine(tooltip, Component.translatable(
                            "item.ramesses_curse.duat_sun_eater_crown.damage_type",
                            power.displayName())
                    .withStyle(ChatFormatting.DARK_PURPLE));
            addSpacedLine(tooltip, Component.translatable(
                            "item.ramesses_curse.duat_sun_eater_crown.trigger_value")
                    .withStyle(ChatFormatting.GOLD));
            addSpacedLine(tooltip, Component.translatable(
                            "item.ramesses_curse.duat_sun_eater_crown.damage_value")
                    .withStyle(ChatFormatting.GOLD));
            addSpacedLine(tooltip, Component.translatable(
                            "item.ramesses_curse.duat_sun_eater_crown.power." + power.id() + ".detail")
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    private static void addSpacedLine(List<Component> tooltip, Component line) {
        tooltip.add(Component.empty());
        tooltip.add(line);
    }

    public static void tryTrigger(ServerPlayer player, LivingEntity target) {
        if (!(target instanceof Enemy)
                || ACTIVE_TARGETS.contains(target.getUUID())
                || player.getRandom().nextFloat() >= TRIGGER_CHANCE) {
            return;
        }

        Optional<ItemStack> equipped = getEquippedStack(player);
        if (equipped.isEmpty() || !isDragonDrop(equipped.get())) {
            return;
        }
        CrownPower power = getOrAssignPower(equipped.get(), player);
        float damage = player.getHealth() * HEALTH_DAMAGE_RATIO;
        MinecraftServer server = player.getServer();
        if (server == null || damage <= 0.0F) {
            return;
        }

        UUID targetId = target.getUUID();
        server.execute(() -> {
            if (!player.isAlive() || !target.isAlive() || player.level() != target.level()) {
                return;
            }
            Optional<ItemStack> currentCrown = getEquippedStack(player);
            if (currentCrown.isEmpty() || getPower(currentCrown.get()).filter(power::equals).isEmpty()
                    || !ACTIVE_TARGETS.add(targetId)) {
                return;
            }
            try {
                target.invulnerableTime = 0;
                target.hurt(createDamageSource(player, power), damage);
            } finally {
                ACTIVE_TARGETS.remove(targetId);
            }
        });
    }

    private static Optional<ItemStack> getEquippedStack(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(inventory -> inventory.findFirstCurio(ModItems.DUAT_SUN_EATER_CROWN.get()))
                .map(SlotResult::stack)
                .filter(stack -> !stack.isEmpty());
    }

    private static CrownPower getOrAssignPower(ItemStack stack, LivingEntity wearer) {
        assignPowerIfMissing(stack, wearer);
        return getPower(stack).orElse(POWERS.get(0));
    }

    private static void assignPowerIfMissing(ItemStack stack, LivingEntity wearer) {
        if (isDragonDrop(stack) && getPower(stack).isEmpty()) {
            CrownPower selected = POWERS.get(wearer.getRandom().nextInt(POWERS.size()));
            LegacyItemData.getOrCreate(stack).putString(POWER_KEY, selected.id());
        }
    }

    private static Optional<CrownPower> getPower(ItemStack stack) {
        if (!isDragonDrop(stack) || !LegacyItemData.has(stack)) {
            return Optional.empty();
        }
        String id = LegacyItemData.get(stack).getString(POWER_KEY);
        return POWERS.stream().filter(power -> power.id().equals(id)).findFirst();
    }

    static ItemStack createDragonDropStack() {
        ItemStack stack = new ItemStack(ModItems.DUAT_SUN_EATER_CROWN.get());
        LegacyItemData.getOrCreate(stack).putBoolean(DRAGON_DROP_KEY, true);
        return stack;
    }

    static ItemStack ensureDragonDropStack(ItemStack stack) {
        if (isDragonDrop(stack)) {
            return stack;
        }
        ItemStack authenticated = stack.copy();
        LegacyItemData.getOrCreate(authenticated).putBoolean(DRAGON_DROP_KEY, true);
        return authenticated;
    }

    private static boolean isDragonDrop(ItemStack stack) {
        return LegacyItemData.has(stack) && LegacyItemData.get(stack).getBoolean(DRAGON_DROP_KEY);
    }

    private static DamageSource createDamageSource(Player player, CrownPower power) {
        return new DamageSource(player.level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(power.damageType()), player);
    }

    private record CrownPower(String id, ResourceKey<DamageType> damageType) {
        private Component displayName() {
            return Component.translatable("item.ramesses_curse.duat_sun_eater_crown.power." + this.id)
                    .withStyle(ChatFormatting.DARK_PURPLE);
        }
    }
}
