package com.eldren.wutao.ramesses_curse;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, RamessesCurseMod.MODID);

    public static final RegistryObject<EntityType<UraeusFlareEntity>> URAEUS_FLARE =
            ENTITY_TYPES.register("uraeus_flare", () -> EntityType.Builder
                    .<UraeusFlareEntity>of(UraeusFlareEntity::new, MobCategory.MISC)
                    .sized(0.45F, 0.45F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("uraeus_flare"));

    public static final RegistryObject<EntityType<UraeusRainShardEntity>> URAEUS_RAIN_SHARD =
            ENTITY_TYPES.register("uraeus_rain_shard", () -> EntityType.Builder
                    .<UraeusRainShardEntity>of(UraeusRainShardEntity::new, MobCategory.MISC)
                    .sized(0.22F, 0.22F)
                    .clientTrackingRange(48)
                    .updateInterval(1)
                    .build("uraeus_rain_shard"));

    public static final RegistryObject<EntityType<UraeusStormStarEntity>> URAEUS_STORM_STAR =
            ENTITY_TYPES.register("uraeus_storm_star", () -> EntityType.Builder
                    .<UraeusStormStarEntity>of(UraeusStormStarEntity::new, MobCategory.MISC)
                    .sized(0.2F, 0.2F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("uraeus_storm_star"));

    public static final RegistryObject<EntityType<SatietyCrystalEntity>> SATIETY_CRYSTAL =
            ENTITY_TYPES.register("satiety_crystal", () -> EntityType.Builder
                    .<SatietyCrystalEntity>of(SatietyCrystalEntity::new, MobCategory.MISC)
                    .sized(0.45F, 0.45F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("satiety_crystal"));
}
