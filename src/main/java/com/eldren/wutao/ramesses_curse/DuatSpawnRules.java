package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID)
public final class DuatSpawnRules {
    private static final TagKey<EntityType<?>> DUAT_SPAWN_ALLOWED = TagKey.create(
            Registries.ENTITY_TYPE,
            new ResourceLocation(RamessesCurseMod.MODID, "duat_spawn_allowed"));

    private DuatSpawnRules() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide
                || !ModDimensions.DUAT_DESERT.equals(event.getLevel().dimension())
                || !(event.getEntity() instanceof Mob mob)) {
            return;
        }

        if (mob.getMobType() != MobType.UNDEAD && !mob.getType().is(DUAT_SPAWN_ALLOWED)) {
            event.setCanceled(true);
        }
    }
}
