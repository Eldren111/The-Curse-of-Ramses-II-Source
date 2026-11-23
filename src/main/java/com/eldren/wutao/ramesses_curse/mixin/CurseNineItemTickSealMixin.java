package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.CurseNineSeal;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class CurseNineItemTickSealMixin {
    @Inject(method = "inventoryTick", at = @At("HEAD"), cancellable = true)
    private void ramessesCurse$stopSealedItemTick(Level level, Entity entity, int slot, boolean selected,
                                               CallbackInfo callback) {
        if (entity instanceof Player player && CurseNineSeal.isSealed(player)) callback.cancel();
    }
}
