package com.eldren.wutao.ramesses_requiem;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class RequiemEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, RequiemAddon.LEGACY_NAMESPACE);
    public static final RegistryObject<MobEffect> CRIMSON_WARD = EFFECTS.register("crimson_ward", CrimsonWardEffect::new);
    public static final RegistryObject<MobEffect> CRIMSON_DECAY = EFFECTS.register("crimson_decay", CrimsonDecayEffect::new);
    private RequiemEffects() {}
}
