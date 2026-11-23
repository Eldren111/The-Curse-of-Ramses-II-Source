package com.eldren.wutao.ramesses_requiem.boss;

import com.eldren.wutao.ramesses_requiem.RequiemAddon;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

public final class WusheContent {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, RequiemAddon.MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RequiemAddon.MODID);
    public static final RegistryObject<EntityType<WusheBoss>> WUSHE = ENTITIES.register("wushe", () ->
            EntityType.Builder.of(WusheBoss::new, MobCategory.MONSTER).sized(.75F, 2.2F)
                    .clientTrackingRange(96).updateInterval(1).fireImmune().build("ramesses_requiem:wushe"));
    public static final RegistryObject<EntityType<WusheWave>> WAVE = ENTITIES.register("wushe_wave", () ->
            EntityType.Builder.<WusheWave>of(WusheWave::new, MobCategory.MISC).sized(.25F, .25F)
                    .clientTrackingRange(80).updateInterval(1).fireImmune().build("ramesses_requiem:wushe_wave"));
    public static final RegistryObject<Item> EGG = ITEMS.register("wushe_spawn_egg", () ->
            new ForgeSpawnEggItem(WUSHE, 0x22152c, 0xb43765, new Item.Properties()));
    public static void register(IEventBus bus) {
        ENTITIES.register(bus); ITEMS.register(bus);
        bus.addListener((EntityAttributeCreationEvent event) -> event.put(WUSHE.get(), WusheBoss.attributes().build()));
    }
    private WusheContent() {}
}
