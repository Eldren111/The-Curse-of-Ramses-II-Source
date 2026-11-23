package com.eldren.wutao.ramesses_requiem.boss;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class WusheSkills {
    public static void tick(WusheBoss boss, LivingEntity target) {
        int age = boss.skillAge();
        switch (boss.skill()) {
            case 1 -> ambush(boss, target, age);
            case 2 -> sweep(boss, target, age);
            case 3 -> dance(boss, target, age);
            case 4 -> backstab(boss, (ServerPlayer)target, age);
            case 5 -> capture(boss, (ServerPlayer)target, age);
        }
    }
    private static void ambush(WusheBoss boss, LivingEntity target, int age) {
        if (age == 8) {
            Vec3 direction = boss.position().subtract(target.position()).multiply(1, 0, 1).normalize();
            blink(boss, target.position().add(direction.scale(1.7)), false);
            boss.lookAtTarget(target);
            hit(boss, target, 1, 3);
        }
        if (age == 16) {
            boss.strikePoint(target.position());
            if (blink(boss, target.position().add(0, 3, 0), true)) boss.setNoGravity(true);
            boss.sound(SoundEvents.ENDERMAN_TELEPORT, 1, .7F);
        }
        if (age >= 24 && age < 30) {
            Vec3 next = boss.position().lerp(boss.strikePoint(), .5);
            if (boss.level().noCollision(boss, boss.getBoundingBox().move(next.subtract(boss.position())))) boss.teleportTo(next.x, next.y, next.z);
        }
        if (age == 30) {
            boss.setNoGravity(false); boss.swing(InteractionHand.MAIN_HAND);
            for (LivingEntity victim : boss.level().getEntitiesOfClass(LivingEntity.class, boss.getBoundingBox().inflate(2.8), WusheBoss::eligible)) hit(boss, victim, 1.5F, 3.2);
            particles(boss, boss.position(), 24);
            boss.sound(SoundEvents.PLAYER_ATTACK_STRONG, 1, .65F);
        }
        if (age >= 42) boss.finishSkill();
    }
    private static void sweep(WusheBoss boss, LivingEntity target, int age) {
        if (age == 8) {
            boss.lookAtTarget(target); hit(boss, target, .6F, 3);
            if (boss.distanceToSqr(target) < 9) target.knockback(.35, boss.getX() - target.getX(), boss.getZ() - target.getZ());
        }
        if (age == 22) {
            boss.swing(InteractionHand.MAIN_HAND);
            Vec3 forward = boss.getLookAngle().multiply(1, 0, 1).normalize();
            for (LivingEntity victim : boss.level().getEntitiesOfClass(LivingEntity.class, boss.getBoundingBox().inflate(4), WusheBoss::eligible)) {
                if (victim.position().subtract(boss.position()).normalize().dot(forward) > -.2) hit(boss, victim, 1.25F, 4);
            }
            ((ServerLevel)boss.level()).sendParticles(ParticleTypes.SWEEP_ATTACK, boss.getX(), boss.getY() + 1, boss.getZ(), 5, 1.3, .1, 1.3, 0);
            boss.sound(SoundEvents.PLAYER_ATTACK_SWEEP, 1, .6F);
        }
        if (age >= 40) boss.finishSkill();
    }
    private static void dance(WusheBoss boss, LivingEntity target, int age) {
        // Alternate horizontal, diagonal fork, X and vertical slashes. Launch first, then blink.
        if (age % 20 == 10) {
            boss.lookAtTarget(target); boss.swing(InteractionHand.MAIN_HAND);
            Vec3 direction = target.getBoundingBox().getCenter().subtract(boss.getEyePosition()).normalize();
            WusheWave.launch(boss, boss.getEyePosition().add(0, -.35, 0), direction.scale(.85), (age / 20) % 4);
            boss.sound(SoundEvents.PLAYER_ATTACK_SWEEP, .9F, 1.2F);
            double angle = boss.getRandom().nextDouble() * Math.PI * 2;
            for (int i = 0; i < 8; i++) {
                double a = angle + i * Math.PI / 4;
                Vec3 destination = target.position().add(Math.cos(a) * 7, 1, Math.sin(a) * 7);
                if (blink(boss, destination, false)) break;
            }
            boss.lookAtTarget(target);
        }
        if (age >= WusheConfig.DANCE_DURATION.get() * 20) boss.finishSkill();
    }
    private static void backstab(WusheBoss boss, ServerPlayer target, int age) {
        // First three seconds only lock the camera; walking/jumping remain available.
        if (age == 60) {
            WusheControl.freeze(target, boss);
            Vec3 backward = target.getLookAngle().multiply(1, 0, 1).normalize().scale(-1.5);
            if (!blink(boss, target.position().add(backward), false)) { boss.finishSkill(); return; }
            boss.lookAtTarget(target);
            boss.sound(SoundEvents.ENDERMAN_TELEPORT, 1.2F, .55F);
            target.playNotifySound(SoundEvents.WITHER_SPAWN, net.minecraft.sounds.SoundSource.HOSTILE, .55F, 1.7F);
        }
        if (age == 70) {
            boss.swing(InteractionHand.MAIN_HAND);
            if (boss.distanceToSqr(target) <= 16 && boss.hasLineOfSight(target)) {
                target.hurt(WusheDamage.backstab(boss), WusheConfig.BACKSTAB_DAMAGE.get().floatValue());
                Vec3 center = target.position().add(0, .9, 0);
                WusheScenePacket.burst((ServerLevel)boss.level(), center);
                for (int i = 0; i < 24; i++) {
                    double a = i * Math.PI * 2 / 24;
                    Vec3 velocity = new Vec3(Math.cos(a) * .32, .12 + (i % 4) * .055, Math.sin(a) * .32);
                    WusheWave.launch(boss, center, velocity, 4);
                }
                boss.sound(SoundEvents.GENERIC_EXPLODE, .8F, 1.15F);
            }
            WusheControl.release(target, boss.getUUID());
        }
        if (age >= 84) boss.finishSkill();
    }
    private static void capture(WusheBoss boss, ServerPlayer target, int age) {
        if (age < 18) {
            Vec3 front = boss.position().add(boss.getLookAngle().multiply(1, 0, 1).normalize().scale(1.7));
            Vec3 next = target.position().lerp(front, .28);
            if (target.level().noCollision(target, target.getBoundingBox().move(next.subtract(target.position()))))
                target.teleportTo(next.x, next.y, next.z);
            boss.lookAtTarget(target);
        }
        if (age == 18) { WusheControl.freeze(target, boss); WusheScenePacket.send(target, new WusheScenePacket(1, boss.getId(), 38, Vec3.ZERO)); }
        if (age == 34) {
            boss.finishSkill();
            AstralArena.enter(boss, target);
        }
    }
    public static boolean blink(WusheBoss boss, Vec3 desired, boolean aerial) {
        ServerLevel level = (ServerLevel)boss.level();
        Vec3 destination = desired;
        if (!aerial) {
            BlockPos ground = BlockPos.containing(desired).above(2);
            int steps = 0;
            while (steps++ < 9 && ground.getY() > level.getMinBuildHeight() && level.getBlockState(ground.below()).getCollisionShape(level, ground.below()).isEmpty()) ground = ground.below();
            if (level.getBlockState(ground.below()).getCollisionShape(level, ground.below()).isEmpty()) return false;
            destination = new Vec3(desired.x, ground.getY(), desired.z);
        }
        if (!level.hasChunkAt(BlockPos.containing(destination)) || !level.getWorldBorder().isWithinBounds(BlockPos.containing(destination))
                || !level.noCollision(boss, boss.getBoundingBox().move(destination.subtract(boss.position())))
                || level.containsAnyLiquid(boss.getBoundingBox().move(destination.subtract(boss.position())))) return false;
        particles(boss, boss.position(), 12);
        boss.teleportTo(destination.x, destination.y, destination.z); boss.setDeltaMovement(Vec3.ZERO); boss.fallDistance = 0;
        particles(boss, destination, 12); return true;
    }
    private static void hit(WusheBoss boss, LivingEntity target, float multiplier, double reach) {
        if (boss.distanceToSqr(target) <= reach * reach && boss.hasLineOfSight(target)) {
            boss.swing(InteractionHand.MAIN_HAND);
            target.hurt(boss.damageSources().mobAttack(boss), WusheConfig.MELEE_DAMAGE.get().floatValue() * multiplier);
        }
    }
    private static void particles(WusheBoss boss, Vec3 p, int count) {
        ((ServerLevel)boss.level()).sendParticles(ParticleTypes.REVERSE_PORTAL, p.x, p.y + 1, p.z, count, .45, .7, .45, .07);
    }
    private WusheSkills() {}
}
