package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.ModEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.xiaoyue.celestial_enchantments.content.table.CelestialTableMenu")
public abstract class CelestialTableMenuMixin {
    @Unique
    private int ramessesCurse$celestialXpBefore = -1;

    @Inject(method = {"clickMenuButton", "m_6366_"}, at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void ramessesCurse$requireAdjustedCelestialLevel(Player player, int buttonId,
                                                             CallbackInfoReturnable<Boolean> cir) {
        int[] costs = ((CelestialTableMenuAccessor) this).ramessesCurse$getCelestialCosts();
        if (buttonId < 0 || buttonId >= costs.length || player.getAbilities().instabuild) {
            return;
        }

        int baseRequirement = Math.max(0, costs[buttonId] * (buttonId + 1));
        int adjustedRequirement = ModEvents.getAdjustedEnchantLevelCost(player, baseRequirement);
        if (adjustedRequirement > baseRequirement && player.experienceLevel < adjustedRequirement) {
            player.displayClientMessage(
                    Component.translatable(
                            "message.ramesses_curse.celestial_enchant_insufficient",
                            adjustedRequirement),
                    true);
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "onEnchant", at = @At("HEAD"), remap = false, require = 0)
    private void ramessesCurse$captureCelestialXp(Player player, ItemStack tool, ItemStack fuel,
                                                  int buttonId, Level level, BlockPos pos, CallbackInfo ci) {
        this.ramessesCurse$celestialXpBefore = player.getAbilities().instabuild
                ? -1
                : player.experienceLevel;
    }

    @Inject(method = "onEnchant", at = @At("TAIL"), remap = false, require = 0)
    private void ramessesCurse$chargeAdjustedCelestialXp(Player player, ItemStack tool, ItemStack fuel,
                                                         int buttonId, Level level, BlockPos pos, CallbackInfo ci) {
        int levelBefore = this.ramessesCurse$celestialXpBefore;
        this.ramessesCurse$celestialXpBefore = -1;
        if (levelBefore < 0 || player.getAbilities().instabuild) {
            return;
        }

        int baseSpend = Math.max(0, levelBefore - player.experienceLevel);
        int adjustedSpend = ModEvents.getAdjustedEnchantSpend(player, baseSpend);
        int extraSpend = Math.max(0, adjustedSpend - baseSpend);
        if (baseSpend <= 0 || extraSpend <= 0) {
            return;
        }

        player.giveExperienceLevels(-extraSpend);
        player.displayClientMessage(
                Component.translatable(
                        "message.ramesses_curse.celestial_enchant_charge",
                        extraSpend,
                        adjustedSpend),
                true);
    }
}
