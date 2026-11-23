package com.eldren.wutao.ramesses_requiem.boss;

import com.eldren.wutao.ramesses_requiem.RequiemAddon;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.*;

public final class WusheDamage {
    public static final ResourceKey<DamageType> BACKSTAB = ResourceKey.create(Registries.DAMAGE_TYPE,
            new ResourceLocation(RequiemAddon.MODID, "wushe_backstab"));
    public static final ResourceKey<DamageType> SHARD = ResourceKey.create(Registries.DAMAGE_TYPE,
            new ResourceLocation(RequiemAddon.MODID, "wushe_shard"));
    public static DamageSource backstab(WusheBoss boss) {
        return new DamageSource(boss.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(BACKSTAB), boss);
    }
    public static boolean fromBoss(DamageSource source) {
        return source.getEntity() instanceof WusheBoss || source.getDirectEntity() instanceof WusheWave;
    }
    public static boolean trueDamage(DamageSource source) { return source.is(BACKSTAB); }
    private WusheDamage() {}
}
