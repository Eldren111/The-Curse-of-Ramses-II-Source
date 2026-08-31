package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.ModEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "fuzs.enchantinginfuser.world.inventory.InfuserMenu")
public abstract class InfuserMenuMixin {
    @Inject(method = "canEnchant", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void ramessesCurse$useAdjustedInfuserRequirement(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!ramessesCurse$getEnchantableStack(this).isEmpty() && ramessesCurse$enchantmentsChanged(this)) {
            int adjustedCost = ModEvents.getAdjustedEnchantLevelCost(player, ramessesCurse$getEnchantCost(this));
            cir.setReturnValue(player.experienceLevel >= adjustedCost || player.getAbilities().instabuild);
        }
    }

    @Inject(method = "clickEnchantButton", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void ramessesCurse$blockInsufficientInfuserXp(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (player.getAbilities().instabuild || ramessesCurse$getEnchantableStack(this).isEmpty()
                || !ramessesCurse$enchantmentsChanged(this)) {
            return;
        }

        int adjustedCost = ModEvents.getAdjustedEnchantLevelCost(player, ramessesCurse$getEnchantCost(this));
        if (adjustedCost > 0 && player.experienceLevel < adjustedCost) {
            player.displayClientMessage(ModEvents.getCurseThreeInsufficientXpMessage(), true);
            cir.setReturnValue(false);
        }
    }

    @Redirect(
            method = "clickEnchantButton",
            at = @At(
                    value = "INVOKE",
                    target = "Lfuzs/enchantinginfuser/world/inventory/InfuserMenu;calculateEnchantCost()I"),
            remap = false,
            require = 0)
    private int ramessesCurse$useAdjustedInfuserCost(@Coerce Object menu) {
        return ModEvents.getAdjustedEnchantLevelCost(ramessesCurse$getPlayer(menu), ramessesCurse$getEnchantCost(menu));
    }

    private static ItemStack ramessesCurse$getEnchantableStack(Object menu) {
        Object value = ramessesCurse$invoke(menu, "getEnchantableStack");
        return value instanceof ItemStack stack ? stack : ItemStack.EMPTY;
    }

    private static int ramessesCurse$getEnchantCost(Object menu) {
        Object value = ramessesCurse$invoke(menu, "getEnchantCost");
        return value instanceof Number number ? Math.max(0, number.intValue()) : 0;
    }

    private static boolean ramessesCurse$enchantmentsChanged(Object menu) {
        Object value = ramessesCurse$getField(menu, "enchantmentsChanged");
        return !(value instanceof Boolean changed) || changed;
    }

    private static Player ramessesCurse$getPlayer(Object menu) {
        Object value = ramessesCurse$getField(menu, "player");
        return value instanceof Player player ? player : null;
    }

    private static Object ramessesCurse$getField(Object target, String name) {
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            try {
                java.lang.reflect.Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(target);
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    private static Object ramessesCurse$invoke(Object target, String name) {
        try {
            return target.getClass().getMethod(name).invoke(target);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
