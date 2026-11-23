package com.eldren.wutao.ramesses_curse.api;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/** Optional integrations. With no provider installed, dynasty mechanics retain their normal rules. */
public final class DynastyCombatHooks {
    private static final CopyOnWriteArrayList<Predicate<DamageSource>> DAMAGE = new CopyOnWriteArrayList<>();
    private static final CopyOnWriteArrayList<Predicate<ItemStack>> SILENCE = new CopyOnWriteArrayList<>();
    private static final CopyOnWriteArrayList<Predicate<MobEffect>> PURGE = new CopyOnWriteArrayList<>();
    private DynastyCombatHooks() {}

    public static void register(Predicate<DamageSource> damage, Predicate<ItemStack> silence, Predicate<MobEffect> purge) {
        DAMAGE.add(Objects.requireNonNull(damage));
        SILENCE.add(Objects.requireNonNull(silence));
        PURGE.add(Objects.requireNonNull(purge));
    }

    public static boolean bypassesAffixes(DamageSource source) {
        return source != null && DAMAGE.stream().anyMatch(rule -> rule.test(source));
    }
    public static boolean bypassesSilence(ItemStack stack) {
        return SILENCE.stream().anyMatch(rule -> rule.test(stack));
    }
    public static boolean preventsPurge(MobEffect effect) {
        return PURGE.stream().anyMatch(rule -> rule.test(effect));
    }
}
