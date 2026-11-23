package com.eldren.wutao.ramesses_requiem.mixin;

import com.eldren.wutao.ramesses_requiem.CrimsonRequiemFlight;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class CrimsonRequiemSlotMixin {
    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void crimsonRequiem$noPickup(Player player, CallbackInfoReturnable<Boolean> ci) {
        if (CrimsonRequiemFlight.locked(((Slot)(Object)this).getItem())) ci.setReturnValue(false);
    }
}
