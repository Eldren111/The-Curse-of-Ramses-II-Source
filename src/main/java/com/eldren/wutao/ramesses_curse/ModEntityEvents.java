package com.eldren.wutao.ramesses_curse;

import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = RamessesCurseMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModEntityEvents {
    private ModEntityEvents() {
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.NEBUKA_GUARDIAN.get(), NebukaGuardianEntity.createAttributes().build());
        event.put(ModEntities.DUAT_SUN_EATER.get(), DuatSunEaterEntity.createAttributes().build());
    }
}

