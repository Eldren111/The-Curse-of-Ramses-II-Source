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
                    .updateInterval(3)
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

    public static final RegistryObject<EntityType<NebukaGuardianEntity>> NEBUKA_GUARDIAN =
            ENTITY_TYPES.register("nebuka_guardian", () -> EntityType.Builder
                    .of(NebukaGuardianEntity::new, MobCategory.MONSTER)
                    .sized(1.6F, 4.8F)
                    .clientTrackingRange(12)
                    .updateInterval(1)
                    .fireImmune()
                    .build("nebuka_guardian"));

    public static final RegistryObject<EntityType<NebukaSummoningRitualEntity>> NEBUKA_SUMMONING_RITUAL =
            ENTITY_TYPES.register("nebuka_summoning_ritual", () -> EntityType.Builder
                    .<NebukaSummoningRitualEntity>of(NebukaSummoningRitualEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(96)
                    .updateInterval(1)
                    .build("nebuka_summoning_ritual"));

    public static final RegistryObject<EntityType<InscriptionFragmentEntity>> INSCRIPTION_FRAGMENT =
            ENTITY_TYPES.register("inscription_fragment", () -> EntityType.Builder
                    .<InscriptionFragmentEntity>of(InscriptionFragmentEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(10)
                    .updateInterval(Integer.MAX_VALUE)
                    .build("inscription_fragment"));

    public static final RegistryObject<EntityType<DragonBreathArrowEntity>> DRAGON_BREATH_ARROW =
            ENTITY_TYPES.register("dragon_breath_arrow", () -> EntityType.Builder
                    .<DragonBreathArrowEntity>of(DragonBreathArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("dragon_breath_arrow"));

    public static final RegistryObject<EntityType<FissionHeartDropEntity>> FISSION_HEART_DROP =
            ENTITY_TYPES.register("fission_heart_drop", () -> EntityType.Builder
                    .<FissionHeartDropEntity>of(FissionHeartDropEntity::new, MobCategory.MISC)
                    .sized(0.35F, 0.35F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("fission_heart_drop"));

    public static final RegistryObject<EntityType<DuatSunEaterCrownEntity>> DUAT_SUN_EATER_CROWN =
            ENTITY_TYPES.register("duat_sun_eater_crown", () -> EntityType.Builder
                    .<DuatSunEaterCrownEntity>of(DuatSunEaterCrownEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(96)
                    .updateInterval(1)
                    .fireImmune()
                    .build("duat_sun_eater_crown"));

    public static final RegistryObject<EntityType<RamessesEndCrystalEntity>> RAMESSES_END_CRYSTAL =
            ENTITY_TYPES.register("ramesses_end_crystal", () -> EntityType.Builder
                    .<RamessesEndCrystalEntity>of(RamessesEndCrystalEntity::new, MobCategory.MISC)
                    .sized(2.0F, 2.0F)
                    .clientTrackingRange(16)
                    .updateInterval(Integer.MAX_VALUE)
                    .fireImmune()
                    .build("ramesses_end_crystal"));

    public static final RegistryObject<EntityType<DuatSunEaterEntity>> DUAT_SUN_EATER =
            ENTITY_TYPES.register("duat_sun_eater", () -> EntityType.Builder
                    .<DuatSunEaterEntity>of(DuatSunEaterEntity::new, MobCategory.MONSTER)
                    .sized(16.0F, 8.0F)
                    .clientTrackingRange(16)
                    .updateInterval(1)
                    .fireImmune()
                    .build("duat_sun_eater"));
}
