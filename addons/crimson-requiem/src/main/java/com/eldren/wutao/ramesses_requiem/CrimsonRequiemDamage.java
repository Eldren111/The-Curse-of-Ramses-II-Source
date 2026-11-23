package com.eldren.wutao.ramesses_requiem;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/** Identify this weapon's attacks without granting exemptions to unrelated projectiles. */
public final class CrimsonRequiemDamage {
    private CrimsonRequiemDamage() {}

    public static float meleeAttackDamage(Player player) {
        var current = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (CrimsonRequiemActions.weaponHand(player) == net.minecraft.world.InteractionHand.MAIN_HAND) {
            return (float)current.getValue();
        }
        // Calculate as if the sword were in the attacking hand without swapping inventory or
        // adding its modifiers to the other weapon. Preserve effects and equipment modifiers.
        var swordAttack = new net.minecraft.world.entity.ai.attributes.AttributeInstance(Attributes.ATTACK_DAMAGE, ignored -> {});
        swordAttack.replaceFrom(current);
        player.getMainHandItem().getAttributeModifiers(net.minecraft.world.entity.EquipmentSlot.MAINHAND)
                .get(Attributes.ATTACK_DAMAGE).forEach(modifier -> swordAttack.removeModifier(modifier.getId()));
        CrimsonRequiemActions.weapon(player).getAttributeModifiers(net.minecraft.world.entity.EquipmentSlot.MAINHAND)
                .get(Attributes.ATTACK_DAMAGE).forEach(modifier -> {
                    swordAttack.removeModifier(modifier.getId());
                    swordAttack.addTransientModifier(modifier);
                });
        return (float)swordAttack.getValue();
    }

    /** Read live caster stats on both the server hit path and the client tooltip path. */
    public static float boomerangTrueDamage(Player player) {
        return (float)Math.max(0D, CrimsonRequiemConfig.TRUE_DAMAGE.get()
                + (player == null ? 0D : player.getHealth() * 3D));
    }

    public static float boomerangMagicDamage(Player player) {
        return (float)Math.max(0D, CrimsonRequiemConfig.MAGIC_DAMAGE.get()
                + (player == null ? 0D : player.getAttributeValue(Attributes.LUCK) * 5D));
    }

    public static final net.minecraft.resources.ResourceKey<net.minecraft.world.damagesource.DamageType> TRUE =
            net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,
                    new net.minecraft.resources.ResourceLocation(RequiemAddon.LEGACY_NAMESPACE, "crimson_requiem_true"));
    public static final net.minecraft.resources.ResourceKey<net.minecraft.world.damagesource.DamageType> MAGIC =
            net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,
                    new net.minecraft.resources.ResourceLocation(RequiemAddon.LEGACY_NAMESPACE, "crimson_requiem_magic"));

    /** Both components use stats from the same contact and retain the actual projectile as source. */
    public static void hitScalingBlade(Player owner, Entity projectile, Entity target) {
        float trueDamage = boomerangTrueDamage(owner);
        float magicDamage = boomerangMagicDamage(owner);
        var types = projectile.level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE);
        if (trueDamage > 0) target.hurt(new DamageSource(types.getHolderOrThrow(TRUE), projectile, owner), trueDamage);
        if (target.isAlive() && magicDamage > 0) {
            target.hurt(new DamageSource(types.getHolderOrThrow(MAGIC), projectile, owner), magicDamage);
        }
    }

    public static boolean isProjectile(Entity entity) {
        return entity instanceof CrimsonRequiemProjectile
                || entity instanceof CrimsonRiftExecution
                || entity instanceof CrimsonSwordStorm
                || entity instanceof CrimsonRequiemSkull
                || entity instanceof CrimsonRequiemFireball;
    }

    public static boolean bypassesAffixes(DamageSource source) {
        if (source == null) return false;
        if (source.is(CrimsonDecayEffect.DAMAGE)) return true;
        if (isProjectile(source.getDirectEntity())) return true;
        // Restrict the held-item fallback to actual melee damage. An old arrow is not this sword.
        return source.is(DamageTypes.PLAYER_ATTACK)
                && source.getDirectEntity() == source.getEntity()
                && source.getEntity() instanceof LivingEntity attacker
                && attacker.getMainHandItem().is(RequiemItems.CRIMSON_REQUIEM.get());
    }
}
