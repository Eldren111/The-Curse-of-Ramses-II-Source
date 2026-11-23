package com.eldren.explorerscompassdurability.mixin;

import com.eldren.explorerscompassdurability.Durability;
import com.eldren.explorerscompassdurability.DurabilityConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/** Only targets the addon host; no vanilla Item/ItemStack-wide injection. */
@Pseudo
@Mixin(targets = "com.chaosthedude.explorerscompass.items.ExplorersCompassItem", remap = false)
public abstract class CompassItemMixin extends Item {
    @Unique private final ThreadLocal<Boolean> ecd$request = new ThreadLocal<>();
    @Unique private final ThreadLocal<Integer> ecd$savedDamage = new ThreadLocal<>();

    protected CompassItemMixin(Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return DurabilityConfig.maxDurability();
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return true;
    }

    @Inject(method = {"searchForTargets", "searchForGroup", "searchForNext"}, at = @At("HEAD"))
    private void ecd$beginRequest(CallbackInfo ci) {
        ecd$request.set(true);
    }

    @Inject(method = {"searchForTargets", "searchForGroup", "searchForNext"}, at = @At("RETURN"))
    private void ecd$endRequest(CallbackInfo ci) {
        ecd$request.remove();
    }

    // Stable 1.20.1 search boundary, after the public methods' cooldown/eligibility checks.
    // Coerce avoids linking against a particular upstream SearchTarget class version.
    // Automatic startOver() fallback is outside a user request and is not charged twice.
    @Inject(method = "search", at = @At("RETURN"))
    private void ecd$chargeSearch(ServerLevel level, Player player, @Coerce Object searchTarget,
            List<ResourceLocation> keys, ResourceLocation displayKey, boolean isGroup,
            BlockPos pos, ItemStack stack, List<BlockPos> prevPos, boolean ignoreNearStart, CallbackInfo ci) {
        if (Boolean.TRUE.equals(ecd$request.get())) {
            ecd$request.set(false);
            Durability.consume(player, stack);
        }
    }

    @Inject(method = "cancelSearch", at = @At("HEAD"))
    private void ecd$saveDamage(Level level, Player player, ItemStack stack, CallbackInfo ci) {
        ecd$savedDamage.set(stack.getDamageValue());
    }

    @Inject(method = "cancelSearch", at = @At("RETURN"))
    private void ecd$restoreDamage(Level level, Player player, ItemStack stack, CallbackInfo ci) {
        Integer damage = ecd$savedDamage.get();
        ecd$savedDamage.remove();
        if (damage != null && !stack.isEmpty()) {
            stack.setDamageValue(damage);
        }
    }
}
