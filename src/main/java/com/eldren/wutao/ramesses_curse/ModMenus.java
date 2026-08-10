package com.eldren.wutao.ramesses_curse;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, RamessesCurseMod.MODID);

    public static final RegistryObject<MenuType<InscriptionBagMenu>> INSCRIPTION_BAG =
            MENUS.register("inscription_bag", () -> IForgeMenuType.create(InscriptionBagMenu::fromNetwork));
    public static final RegistryObject<MenuType<CompoundBowAmmoMenu>> COMPOUND_BOW_AMMO =
            MENUS.register("compound_bow_ammo", () -> IForgeMenuType.create(CompoundBowAmmoMenu::fromNetwork));

    private ModMenus() {
    }
}
