package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.tags.EntityTypeTags;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = RamessesCurseMod.MODID)
public final class DuatSpawnRules {
    private static final TagKey<EntityType<?>> DUAT_SPAWN_ALLOWED = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(RamessesCurseMod.MODID, "duat_spawn_allowed"));

    private DuatSpawnRules() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide
                || !ModDimensions.DUAT_DESERT.equals(event.getLevel().dimension())
                || !(event.getEntity() instanceof Mob mob)) {
            return;
        }

        if (!mob.getType().is(EntityTypeTags.UNDEAD) && !mob.getType().is(DUAT_SPAWN_ALLOWED)) {
            event.setCanceled(true);
        }
    }
}

