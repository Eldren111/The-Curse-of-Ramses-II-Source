package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.Config;
import com.eldren.wutao.ramesses_curse.ModEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.Polarice3.Goety.client.inventory.container.DarkAnvilMenu")
public abstract class GoetyDarkAnvilMenuMixin {
    @Inject(method = {"createResult", "m_6640_"}, at = @At("TAIL"), remap = false, require = 0)
    private void ramessesCurse$applyCurseThreeDarkAnvilCost(CallbackInfo ci) {
        AnvilMenu menu = (AnvilMenu) (Object) this;
        Player player = ((ItemCombinerMenuAccessor) this).ramessesCurse$getPlayer();
        int baseCost = Math.max(0, menu.getCost());

        if (!ModEvents.canUseSealBoundItem(player)
                || ModEvents.areCurseThreeAndFourLifted(player)
                || baseCost <= 0) {
            ModEvents.clearCurseThreeAnvilCosts(player);
            return;
        }

        int adjustedCost = Math.max(baseCost,
                Mth.ceil(baseCost * Config.CURSE_3_ANVIL_COST_MULTIPLIER.get()));
        ModEvents.storeCurseThreeAnvilCosts(player, baseCost, adjustedCost);
        if (adjustedCost != baseCost) {
            menu.setMaximumCost(adjustedCost);
        }
    }

    @Inject(method = {"onTake", "m_142365_"}, at = @At("HEAD"), remap = false, require = 0)
    private void ramessesCurse$showDarkAnvilCharge(Player player, ItemStack result, CallbackInfo ci) {
        if (player.level().isClientSide
                || player.getAbilities().instabuild
                || !ModEvents.canUseSealBoundItem(player)
                || ModEvents.areCurseThreeAndFourLifted(player)) {
            return;
        }

        int actualCost = Math.max(0, ((AnvilMenu) (Object) this).getCost());
        int baseCost = ModEvents.getDisplayedAnvilBaseCost(player, actualCost);
        int extraCost = Math.max(0, actualCost - baseCost);
        if (actualCost > 0 && extraCost > 0) {
            player.displayClientMessage(
                    Component.translatable(
                            "message.ramesses_curse.goety_dark_anvil_charge",
                            extraCost,
                            actualCost),
                    true);
        }
    }
}
