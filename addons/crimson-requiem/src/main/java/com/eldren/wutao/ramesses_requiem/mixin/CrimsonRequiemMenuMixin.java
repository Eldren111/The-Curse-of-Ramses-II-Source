package com.eldren.wutao.ramesses_requiem.mixin;

import com.eldren.wutao.ramesses_requiem.CrimsonRequiemFlight;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class CrimsonRequiemMenuMixin {
    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    private void crimsonRequiem$lockSlot(int slot, int button, ClickType type, Player player, CallbackInfo ci) {
        AbstractContainerMenu menu = (AbstractContainerMenu)(Object)this;
        boolean locked = slot >= 0 && slot < menu.slots.size() && CrimsonRequiemFlight.locked(menu.getSlot(slot).getItem());
        if (type == ClickType.SWAP && button >= 0 && button < player.getInventory().getContainerSize()) {
            locked |= CrimsonRequiemFlight.locked(player.getInventory().getItem(button));
        }
        if (locked || CrimsonRequiemFlight.locked(menu.getCarried())) ci.cancel();
    }
}
