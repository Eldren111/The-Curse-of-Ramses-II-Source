package com.eldren.wutao.ramesses_requiem;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class RequiemEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, RequiemAddon.LEGACY_NAMESPACE);
    public static final RegistryObject<EntityType<CrimsonRiftExecution>> CRIMSON_RIFT_EXECUTION =
            ENTITY_TYPES.register("crimson_rift_execution", () -> EntityType.Builder
                    .<CrimsonRiftExecution>of(CrimsonRiftExecution::new, MobCategory.MISC)
                    .sized(1F, 1F).clientTrackingRange(96).updateInterval(1).fireImmune().build("crimson_rift_execution"));

    public static final RegistryObject<EntityType<CrimsonSwordStorm>> CRIMSON_SWORD_STORM =
            ENTITY_TYPES.register("crimson_sword_storm", () -> EntityType.Builder
                    .<CrimsonSwordStorm>of(CrimsonSwordStorm::new, MobCategory.MISC)
                    .sized(0.25F, 0.8F).clientTrackingRange(64).updateInterval(1).fireImmune().build("crimson_sword_storm"));

    public static final RegistryObject<EntityType<CrimsonRequiemProjectile>> CRIMSON_REQUIEM_PROJECTILE =
            ENTITY_TYPES.register("crimson_requiem_projectile", () -> EntityType.Builder
                    .<CrimsonRequiemProjectile>of(CrimsonRequiemProjectile::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).clientTrackingRange(64).updateInterval(1).fireImmune().build("crimson_requiem_projectile"));
    public static final RegistryObject<EntityType<CrimsonRequiemSkull>> CRIMSON_REQUIEM_SKULL =
            ENTITY_TYPES.register("crimson_requiem_skull", () -> EntityType.Builder
                    .<CrimsonRequiemSkull>of(CrimsonRequiemSkull::new, MobCategory.MISC)
                    .sized(0.3125F, 0.3125F).clientTrackingRange(64).updateInterval(1).fireImmune().build("crimson_requiem_skull"));
    public static final RegistryObject<EntityType<CrimsonRequiemFireball>> CRIMSON_REQUIEM_FIREBALL =
            ENTITY_TYPES.register("crimson_requiem_fireball", () -> EntityType.Builder
                    .<CrimsonRequiemFireball>of(CrimsonRequiemFireball::new, MobCategory.MISC)
                    .sized(1F, 1F).clientTrackingRange(64).updateInterval(1).fireImmune().build("crimson_requiem_fireball"));

}
