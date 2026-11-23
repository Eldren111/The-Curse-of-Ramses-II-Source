package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.DuatCuriosSealManager;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.common.event.CuriosEventHandler;

@Mixin(value = CuriosEventHandler.class, remap = false)
public abstract class CuriosTickSealMixin {
    @Redirect(
            // Curios renumbered this synthetic lambda across its 1.20.1 releases.
            method = {
                    "lambda$tick$23",
                    "lambda$tick$24",
                    "lambda$tick$29",
                    "lambda$tick$30",
                    "lambda$tick$31"
            },
            at = @At(value = "INVOKE",
                    target = "Ltop/theillusivec4/curios/api/type/capability/ICurio;curioTick(Ltop/theillusivec4/curios/api/SlotContext;)V"),
            require = 1,
            remap = false)
    private static void ramessesCurse$suppressSealedCurioTick(ICurio curio, SlotContext context) {
        if (!DuatCuriosSealManager.suppressesTick(context.entity(), curio.getStack())) {
            curio.curioTick(context);
        }
    }

    @Redirect(
            method = {
                    "lambda$tick$32",
                    "lambda$tick$33",
                    "lambda$tick$34"
            },
            at = @At(value = "INVOKE",
                    target = "Ltop/theillusivec4/curios/api/type/capability/ICurio;onEquip(Ltop/theillusivec4/curios/api/SlotContext;Lnet/minecraft/world/item/ItemStack;)V"),
            require = 0,
            remap = false)
    private static void ramessesCurse$suppressEquipCallback(ICurio curio, SlotContext context,
                                                             ItemStack previousStack) {
        if (!DuatCuriosSealManager.suppressesMechanics(context.entity(), curio.getStack())) {
            curio.onEquip(context, previousStack);
        }
    }

    @Redirect(
            method = {
                    "lambda$curioRightClick$22",
                    "lambda$curioRightClick$23",
                    "lambda$curioRightClick$24"
            },
            at = @At(value = "INVOKE",
                    target = "Ltop/theillusivec4/curios/api/type/capability/ICurio;onEquipFromUse(Ltop/theillusivec4/curios/api/SlotContext;)V"),
            require = 0,
            remap = false)
    private static void ramessesCurse$suppressEquipFromUseCallback(ICurio curio, SlotContext context) {
        if (!DuatCuriosSealManager.suppressesMechanics(context.entity(), curio.getStack())) {
            curio.onEquipFromUse(context);
        }
    }

    @Redirect(
            method = {
                    "lambda$onBreakBlock$25",
                    "lambda$onBreakBlock$26"
            },
            at = @At(value = "INVOKE",
                    target = "Ltop/theillusivec4/curios/api/type/capability/ICurio;getFortuneLevel(Ltop/theillusivec4/curios/api/SlotContext;Lnet/minecraft/world/level/storage/loot/LootContext;)I"),
            require = 0,
            remap = false)
    private static int ramessesCurse$suppressFortune(ICurio curio, SlotContext context,
                                                      LootContext lootContext) {
        return DuatCuriosSealManager.suppressesMechanics(context.entity(), curio.getStack())
                ? 0 : curio.getFortuneLevel(context, lootContext);
    }

    @Redirect(
            method = {
                    "lambda$enderManAnger$27",
                    "lambda$enderManAnger$28"
            },
            at = @At(value = "INVOKE",
                    target = "Ltop/theillusivec4/curios/api/type/capability/ICurio;isEnderMask(Ltop/theillusivec4/curios/api/SlotContext;Lnet/minecraft/world/entity/monster/EnderMan;)Z"),
            require = 0,
            remap = false)
    private static boolean ramessesCurse$suppressEnderMask(ICurio curio, SlotContext context,
                                                            EnderMan enderMan) {
        return !DuatCuriosSealManager.suppressesMechanics(context.entity(), curio.getStack())
                && curio.isEnderMask(context, enderMan);
    }
}
