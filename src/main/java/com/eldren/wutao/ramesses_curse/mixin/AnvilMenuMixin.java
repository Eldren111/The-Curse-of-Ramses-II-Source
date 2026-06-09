package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.Config;
import com.eldren.wutao.ramesses_curse.ModEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {
    @Shadow public abstract int getCost();

    @Inject(method = "createResult", at = @At("TAIL"))
    private void ramessesCurse$applyCurseThreeAnvilCost(CallbackInfo ci) {
        AnvilMenu menu = (AnvilMenu) (Object) this;
        Player player = ((ItemCombinerMenuAccessor) this).ramessesCurse$getPlayer();
        int baseCost = Math.max(0, this.getCost());

        if (!ModEvents.canUseSealBoundItem(player) || baseCost <= 0) {
            ModEvents.clearCurseThreeAnvilCosts(player);
            return;
        }

        int adjustedCost = Math.max(baseCost, Mth.ceil(baseCost * Config.CURSE_3_ANVIL_COST_MULTIPLIER.get()));
        ModEvents.storeCurseThreeAnvilCosts(player, baseCost, adjustedCost);
        if (adjustedCost != baseCost) {
            menu.setMaximumCost(adjustedCost);
        }
    }
}
