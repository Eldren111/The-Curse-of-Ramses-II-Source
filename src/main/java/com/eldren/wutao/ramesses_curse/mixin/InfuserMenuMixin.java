package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.ModEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "fuzs.enchantinginfuser.world.inventory.InfuserMenu")
public abstract class InfuserMenuMixin {
    @Shadow private Player player;
    @Shadow private boolean enchantmentsChanged;

    @Shadow public abstract ItemStack getEnchantableStack();
    @Shadow public abstract int getEnchantCost();

    @Inject(method = "canEnchant", at = @At("HEAD"), cancellable = true, remap = false)
    private void ramessesCurse$useAdjustedInfuserRequirement(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!this.getEnchantableStack().isEmpty() && this.enchantmentsChanged) {
            int adjustedCost = ModEvents.getAdjustedEnchantLevelCost(player, this.getEnchantCost());
            cir.setReturnValue(player.experienceLevel >= adjustedCost || player.getAbilities().instabuild);
        }
    }

    @Inject(method = "clickEnchantButton", at = @At("HEAD"), cancellable = true, remap = false)
    private void ramessesCurse$blockInsufficientInfuserXp(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (player.getAbilities().instabuild || this.getEnchantableStack().isEmpty() || !this.enchantmentsChanged) {
            return;
        }

        int adjustedCost = this.getEnchantCost();
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
            remap = false)
    private int ramessesCurse$useAdjustedInfuserCost(@Coerce Object menu) {
        return ModEvents.getAdjustedEnchantLevelCost(this.player, this.getEnchantCost());
    }
}
