package com.eldren.wutao.ramesses_requiem;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class RequiemItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RequiemAddon.LEGACY_NAMESPACE);
    public static final RegistryObject<Item> CRIMSON_REQUIEM = ITEMS.register("crimson_requiem", CrimsonRequiemItem::new);
    private RequiemItems() {}
}
