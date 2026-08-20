package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.ModEvents;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MeleeAttackGoal.class)
public abstract class MeleeAttackGoalMixin {
    @Shadow
    @Final
    protected PathfinderMob mob;

    @Inject(method = "getAttackInterval", at = @At("RETURN"), cancellable = true)
    private void ramessesCurse$applyRageAttackSpeed(CallbackInfoReturnable<Integer> callback) {
        double bonus = ModEvents.getDynastyRageAttackSpeedBonus(mob);
        if (bonus <= 0.0D) {
            return;
        }
        callback.setReturnValue(Math.max(1, (int) Math.ceil(callback.getReturnValue() / (1.0D + bonus))));
    }
}
