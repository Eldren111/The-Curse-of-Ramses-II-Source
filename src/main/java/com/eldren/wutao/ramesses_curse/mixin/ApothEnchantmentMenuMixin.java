package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.ModEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "dev.shadowsoffire.apotheosis.ench.table.ApothEnchantmentMenu", remap = false)
public class ApothEnchantmentMenuMixin {
    @Inject(method = "clickMenuButton", at = @At("HEAD"), cancellable = true, require = 0)
    private void ramessesCurse$blockInsufficientApothEnchant(Player player, int buttonId, CallbackInfoReturnable<Boolean> cir) {
        int[] costs = ramessesCurse$getCosts(this);
        if (buttonId < 0 || buttonId >= costs.length || player.getAbilities().instabuild) {
            return;
        }

        int vanillaLevelCost = costs[buttonId];
        if (vanillaLevelCost <= 0) {
            return;
        }

        int adjustedLevelCost = ModEvents.getAdjustedEnchantLevelCost(player, vanillaLevelCost);
        if (player.experienceLevel < adjustedLevelCost) {
            player.displayClientMessage(ModEvents.getCurseThreeInsufficientXpMessage(), true);
            cir.setReturnValue(false);
        }
    }

    @Redirect(
            method = "lambda$clickMenuButton$0",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/shadowsoffire/placebo/util/EnchantmentUtils;chargeExperience(Lnet/minecraft/world/entity/player/Player;I)Z"),
            remap = false,
            require = 0)
    private boolean ramessesCurse$chargeAdjustedApothXp(Player player, int vanillaXpCost, ItemStack stack, int slot, Player enchantingPlayer, int vanillaLevelCost, ItemStack lapisStack, int lapisCost, Level level, BlockPos pos) {
        int adjustedLevelCost = ModEvents.getAdjustedEnchantLevelCost(player, vanillaLevelCost);
        int adjustedXpCost = ModEvents.getAdjustedEnchantPointCost(player, vanillaXpCost);
        if (player.getAbilities().instabuild) {
            return true;
        }

        if (player.experienceLevel < adjustedLevelCost || ramessesCurse$getCurrentExperience(player) < adjustedXpCost) {
            return false;
        }

        player.giveExperiencePoints(-adjustedXpCost);
        if (ramessesCurse$getCurrentExperience(player) <= 0) {
            player.experienceProgress = 0.0F;
        }
        return true;
    }

    private static int[] ramessesCurse$getCosts(Object menu) {
        for (Class<?> type = menu.getClass(); type != null; type = type.getSuperclass()) {
            try {
                java.lang.reflect.Field field = type.getDeclaredField("costs");
                field.setAccessible(true);
                Object value = field.get(menu);
                if (value instanceof int[] costs) {
                    return costs;
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return new int[0];
    }

    private static int ramessesCurse$getCurrentExperience(Player player) {
        int xpForCurrentLevel = ramessesCurse$getTotalExperienceForLevel(player.experienceLevel);
        int xpForNextLevel = player.getXpNeededForNextLevel();
        return xpForCurrentLevel + (int) (player.experienceProgress * xpForNextLevel);
    }

    private static int ramessesCurse$getTotalExperienceForLevel(int level) {
        int total = 0;
        for (int current = 1; current <= level; current++) {
            total += ramessesCurse$getExperienceForLevel(current);
        }
        return total;
    }

    private static int ramessesCurse$getExperienceForLevel(int level) {
        if (level <= 0) {
            return 0;
        }
        if (level > 30) {
            return 112 + (level - 31) * 9;
        }
        if (level > 15) {
            return 37 + (level - 16) * 5;
        }
        return 7 + (level - 1) * 2;
    }
}
