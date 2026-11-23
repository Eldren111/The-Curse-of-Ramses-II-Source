package com.eldren.wutao.ramesses_requiem.boss;

import net.minecraft.core.particles.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3f;
import java.util.*;

/** Shared visual and collision samples keep the damaging blade on the visible particle stroke. */
public final class WusheWave extends Projectile {
    private static final EntityDataAccessor<Integer> SHAPE = SynchedEntityData.defineId(WusheWave.class, EntityDataSerializers.INT);
    private final Set<UUID> hit = new HashSet<>();
    private float damage;
    public WusheWave(EntityType<? extends WusheWave> type, Level level) { super(type, level); setNoGravity(true); }
    public static void launch(WusheBoss boss, Vec3 origin, Vec3 velocity, int shape) {
        WusheWave wave = new WusheWave(WusheContent.WAVE.get(), boss.level());
        wave.setOwner(boss); wave.setPos(origin); wave.setDeltaMovement(velocity); wave.entityData.set(SHAPE, shape);
        wave.damage = (shape == 4 ? WusheConfig.SHARD_DAMAGE : WusheConfig.WAVE_DAMAGE).get().floatValue();
        boss.level().addFreshEntity(wave);
    }
    @Override protected void defineSynchedData() { entityData.define(SHAPE, 0); }
    public int shape() { return entityData.get(SHAPE); }
    public static List<Vec3> stroke(int shape, Vec3 velocity) {
        if (shape == 4) return List.of(Vec3.ZERO);
        Vec3 side = velocity.normalize().cross(new Vec3(0, 1, 0));
        if (side.lengthSqr() < .01) side = new Vec3(1, 0, 0);
        side = side.normalize(); Vec3 up = side.cross(velocity.normalize()).normalize();
        List<Vec3> points = new ArrayList<>();
        for (int i = -10; i <= 10; i++) {
            double t = i * .24;
            switch (shape) {
                case 0 -> points.add(side.scale(t).add(up.scale(.35 * (1 - t * t / 6))));
                case 1 -> { points.add(side.scale(t).add(up.scale(Math.abs(t) * .65 - .7))); }
                case 2 -> { points.add(side.scale(t).add(up.scale(t * .65))); points.add(side.scale(t).add(up.scale(-t * .65))); }
                default -> points.add(up.scale(t));
            }
        }
        return points;
    }
    @Override public void tick() {
        super.tick();
        if (tickCount > (shape() == 4 ? 45 : 32)) { discard(); return; }
        Vec3 velocity = getDeltaMovement(), next = position().add(velocity);
        List<Vec3> points = stroke(shape(), velocity);
        if (level().isClientSide) {
            ParticleOptions particle = shape() == 4 ? ParticleTypes.END_ROD : new DustParticleOptions(new Vector3f(.85F, .12F, .55F), 1.1F);
            for (Vec3 offset : points) {
                Vec3 p = position().add(offset);
                level().addParticle(particle, p.x, p.y, p.z, 0, 0, 0);
                if (shape() != 4 && tickCount % 2 == 0) level().addParticle(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 0, 0, 0);
            }
        } else {
            if (!(getOwner() instanceof WusheBoss owner) || !owner.isAlive()) { discard(); return; }
            for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().expandTowards(velocity).inflate(3), WusheBoss::eligible)) {
                if (hit.contains(target.getUUID())) continue;
                for (Vec3 offset : points) {
                    Vec3 from = position().add(offset), to = next.add(offset);
                    var contact = target.getBoundingBox().inflate(.14).clip(from, to);
                    if (contact.isEmpty() && !target.getBoundingBox().inflate(.14).contains(from)) continue;
                    Vec3 impact = contact.orElse(from);
                    if (level().clip(new ClipContext(from, impact, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() != HitResult.Type.MISS) continue;
                    DamageSource source = shape() == 4 ? new DamageSource(level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                            .getHolderOrThrow(WusheDamage.SHARD), this, owner) : damageSources().mobProjectile(this, owner);
                    if (shape() == 4) target.invulnerableTime = 0;
                    target.hurt(source, damage); hit.add(target.getUUID());
                    if (shape() == 4) { discard(); return; }
                    break;
                }
            }
            if (level().clip(new ClipContext(position(), next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() != HitResult.Type.MISS) { discard(); return; }
        }
        setPos(next);
        if (shape() == 4) setDeltaMovement(velocity.add(0, -.018, 0).scale(.985));
    }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); tag.putInt("Shape", shape()); tag.putFloat("Damage", damage); tag.putInt("Age", tickCount); }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag); entityData.set(SHAPE, tag.getInt("Shape")); damage = tag.getFloat("Damage"); tickCount = tag.getInt("Age"); }
    @Override public boolean shouldBeSaved() { return false; }
}
