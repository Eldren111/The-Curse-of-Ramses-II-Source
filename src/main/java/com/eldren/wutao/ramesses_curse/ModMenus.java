package com.eldren.wutao.ramesses_curse;

import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(BuiltInRegistries.MENU, RamessesCurseMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<InscriptionBagMenu>> INSCRIPTION_BAG =
            MENUS.register("inscription_bag", () -> IMenuTypeExtension.create(InscriptionBagMenu::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<CompoundBowAmmoMenu>> COMPOUND_BOW_AMMO =
            MENUS.register("compound_bow_ammo", () -> IMenuTypeExtension.create(CompoundBowAmmoMenu::fromNetwork));

    private ModMenus() {
    }
}

