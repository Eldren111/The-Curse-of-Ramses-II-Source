package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.ModEvents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LootTable.class)
public class LootTableMixin {
    @Inject(method = "fill", at = @At("TAIL"))
    private void ramessesCurse$markGeneratedEnchantedWeapons(Container container, LootParams params, long seed,
                                                              CallbackInfo ci) {
        Entity entity = params.getOptionalParameter(LootContextParams.THIS_ENTITY);
        if (!(entity instanceof Player player) || !ModEvents.isCurseSevenActive(player)) {
            return;
        }
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            ModEvents.markCurseSevenLootWeapon(player, stack);
        }
    }
}
