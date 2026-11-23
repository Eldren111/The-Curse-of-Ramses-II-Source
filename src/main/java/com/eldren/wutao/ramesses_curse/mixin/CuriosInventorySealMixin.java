package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.DuatCuriosSealManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.common.capability.CurioInventoryCapability;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Mixin(value = CurioInventoryCapability.CurioInventoryWrapper.class, remap = false)
public abstract class CuriosInventorySealMixin {
    @org.spongepowered.asm.mixin.injection.Redirect(
            method = "lambda$getFortuneLevel$6",
            at = @At(value = "INVOKE",
                    target = "Ltop/theillusivec4/curios/api/type/capability/ICurio;getFortuneLevel(Ltop/theillusivec4/curios/api/SlotContext;Lnet/minecraft/world/level/storage/loot/LootContext;)I"),
            require = 0,
            remap = false)
    private int ramessesCurse$suppressFortune(ICurio curio, SlotContext context,
                                               LootContext lootContext) {
        return DuatCuriosSealManager.suppressesMechanics(context.entity(), curio.getStack())
                ? 0 : curio.getFortuneLevel(context, lootContext);
    }

    @org.spongepowered.asm.mixin.injection.Redirect(
            method = "lambda$getLootingLevel$7",
            at = @At(value = "INVOKE",
                    target = "Ltop/theillusivec4/curios/api/type/capability/ICurio;getLootingLevel(Ltop/theillusivec4/curios/api/SlotContext;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/entity/LivingEntity;I)I"),
            require = 0,
            remap = false)
    private int ramessesCurse$suppressLooting(ICurio curio, SlotContext context,
                                               DamageSource source, LivingEntity target,
                                               int baseLooting) {
        return DuatCuriosSealManager.suppressesMechanics(context.entity(), curio.getStack())
                ? 0 : curio.getLootingLevel(context, source, target, baseLooting);
    }

    @Inject(method = "findFirstCurio(Lnet/minecraft/world/item/Item;)Ljava/util/Optional;",
            at = @At("RETURN"), cancellable = true, require = 0, remap = false)
    private void ramessesCurse$filterFirstItem(Item item,
                                                CallbackInfoReturnable<Optional<SlotResult>> cir) {
        filterOptional(cir);
    }

    @Inject(method = "findFirstCurio(Ljava/util/function/Predicate;)Ljava/util/Optional;",
            at = @At("RETURN"), cancellable = true, require = 0, remap = false)
    private void ramessesCurse$filterFirstPredicate(Predicate<ItemStack> predicate,
                                                     CallbackInfoReturnable<Optional<SlotResult>> cir) {
        filterOptional(cir);
    }

    @Inject(method = "findCurio(Ljava/lang/String;I)Ljava/util/Optional;",
            at = @At("RETURN"), cancellable = true, require = 0, remap = false)
    private void ramessesCurse$filterSlot(String identifier, int index,
                                           CallbackInfoReturnable<Optional<SlotResult>> cir) {
        filterOptional(cir);
    }

    @Inject(method = "findCurios(Lnet/minecraft/world/item/Item;)Ljava/util/List;",
            at = @At("RETURN"), cancellable = true, require = 0, remap = false)
    private void ramessesCurse$filterItemList(Item item,
                                               CallbackInfoReturnable<List<SlotResult>> cir) {
        filterList(cir);
    }

    @Inject(method = "findCurios(Ljava/util/function/Predicate;)Ljava/util/List;",
            at = @At("RETURN"), cancellable = true, require = 0, remap = false)
    private void ramessesCurse$filterPredicateList(Predicate<ItemStack> predicate,
                                                    CallbackInfoReturnable<List<SlotResult>> cir) {
        filterList(cir);
    }

    @Inject(method = "findCurios([Ljava/lang/String;)Ljava/util/List;",
            at = @At("RETURN"), cancellable = true, require = 0, remap = false)
    private void ramessesCurse$filterIdentifierList(String[] identifiers,
                                                     CallbackInfoReturnable<List<SlotResult>> cir) {
        filterList(cir);
    }

    private void filterOptional(CallbackInfoReturnable<Optional<SlotResult>> cir) {
        LivingEntity wearer = ((ICuriosItemHandler) (Object) this).getWearer();
        if (cir.getReturnValue().filter(result ->
                DuatCuriosSealManager.suppressesMechanics(wearer, result.stack())).isPresent()) {
            cir.setReturnValue(Optional.empty());
        }
    }

    private void filterList(CallbackInfoReturnable<List<SlotResult>> cir) {
        LivingEntity wearer = ((ICuriosItemHandler) (Object) this).getWearer();
        cir.setReturnValue(cir.getReturnValue().stream()
                .filter(result -> !DuatCuriosSealManager.suppressesMechanics(wearer, result.stack()))
                .toList());
    }
}
