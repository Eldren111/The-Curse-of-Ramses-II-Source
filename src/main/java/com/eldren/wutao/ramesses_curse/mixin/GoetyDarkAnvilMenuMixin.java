package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.Config;
import com.eldren.wutao.ramesses_curse.ModEvents;
import com.eldren.wutao.ramesses_curse.integration.goety.GoetyDarkAnvilMenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.Polarice3.Goety.client.inventory.container.DarkAnvilMenu")
public abstract class GoetyDarkAnvilMenuMixin implements GoetyDarkAnvilMenuAccess {
    @Override
    public int ramessesCurse$getDarkAnvilCost() {
        return ramessesCurse$readCost(this);
    }

    @Override
    public void ramessesCurse$setDarkAnvilCost(int value) {
        ramessesCurse$writeMaximumCost(this, value);
    }

    @Inject(method = {"createResult", "m_6640_"}, at = @At("TAIL"), remap = false, require = 0)
    private void ramessesCurse$applyCurseThreeDarkAnvilCost(CallbackInfo ci) {
        Player player = ((ItemCombinerMenuAccessor) this).ramessesCurse$getPlayer();
        int baseCost = Math.max(0, ramessesCurse$readCost(this));

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
            ramessesCurse$writeMaximumCost(this, adjustedCost);
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

        int actualCost = Math.max(0, ramessesCurse$readCost(this));
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

    @Unique
    private static int ramessesCurse$readCost(Object menu) {
        try {
            java.lang.reflect.Method method = menu.getClass().getMethod("getCost");
            return ((Number) method.invoke(menu)).intValue();
        } catch (ReflectiveOperationException | ClassCastException ignored) {
            return 0;
        }
    }

    @Unique
    private static void ramessesCurse$writeMaximumCost(Object menu, int value) {
        try {
            java.lang.reflect.Method method = menu.getClass().getMethod("setMaximumCost", int.class);
            method.invoke(menu, value);
        } catch (ReflectiveOperationException ignored) {
            // Goety versions without a mutable cost simply keep their native value.
        }
    }

}
