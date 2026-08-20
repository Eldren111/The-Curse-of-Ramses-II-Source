package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.ModEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntitySealDrawSpeedMixin {
    @Shadow protected int useItemRemaining;

    @Unique
    private float ramessesCurse$drawSpeedProgress;

    @Inject(method = "updateUsingItem", at = @At("TAIL"))
    private void ramessesCurse$accelerateBowUse(ItemStack stack, CallbackInfo callback) {
        LivingEntity living = (LivingEntity) (Object) this;
        if (!(living instanceof Player player) || stack.getUseAnimation() != UseAnim.BOW) {
            ramessesCurse$drawSpeedProgress = 0.0F;
            return;
        }

        int speedPercent = ModEvents.getSealDrawSpeedPercent(player);
        if (speedPercent <= 0 || useItemRemaining <= 1) {
            ramessesCurse$drawSpeedProgress = 0.0F;
            return;
        }

        ramessesCurse$drawSpeedProgress += speedPercent / 100.0F;
        int extraTicks = (int) ramessesCurse$drawSpeedProgress;
        if (extraTicks > 0) {
            useItemRemaining = Math.max(1, useItemRemaining - extraTicks);
            ramessesCurse$drawSpeedProgress -= extraTicks;
        }
    }

    @Inject(method = "stopUsingItem", at = @At("HEAD"))
    private void ramessesCurse$resetBowUseProgress(CallbackInfo callback) {
        ramessesCurse$drawSpeedProgress = 0.0F;
    }
}
