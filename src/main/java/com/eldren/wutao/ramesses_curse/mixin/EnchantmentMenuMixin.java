package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.ModEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentMenu.class)
public class EnchantmentMenuMixin {
    @Shadow @Final public int[] costs;

    @Inject(method = "clickMenuButton", at = @At("HEAD"), cancellable = true)
    private void ramessesCurse$blockInsufficientCurseEnchant(Player player, int buttonId, CallbackInfoReturnable<Boolean> cir) {
        if (buttonId < 0 || buttonId >= this.costs.length || player.getAbilities().instabuild) {
            return;
        }

        int vanillaCost = this.costs[buttonId];
        int requiredCost = ModEvents.getAdjustedEnchantLevelCost(player, vanillaCost);
        if (requiredCost > vanillaCost && player.experienceLevel < requiredCost) {
            player.displayClientMessage(ModEvents.getCurseThreeInsufficientXpMessage(), true);
            cir.setReturnValue(false);
            return;
        }

        int vanillaSpend = buttonId + 1;
        ModEvents.setPendingEnchantCost(player, ModEvents.getAdjustedEnchantSpend(player, vanillaSpend));
    }
}
