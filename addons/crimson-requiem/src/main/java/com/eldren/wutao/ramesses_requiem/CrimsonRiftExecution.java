package com.eldren.wutao.ramesses_requiem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;

/** One synchronized timeline drives all rifts, blades and damage, including the final execution. */
public final class CrimsonRiftExecution extends Projectile {
    public static final int BLADES = 24;
    public static final int GIANT_START = impactTick(BLADES - 1) + 14;
    public static final int GIANT_CHARGE_TICKS = 10;
    public static final int GIANT_DIVE_TICKS = 10;
    public static final int FINAL_TICK = GIANT_START + GIANT_CHARGE_TICKS + GIANT_DIVE_TICKS;
    public static final ResourceKey<DamageType> EXECUTION = damageKey("crimson_rift_execution");
    public static final ResourceKey<DamageType> BLAST = damageKey("crimson_rift_blast");
    private static final EntityDataAccessor<Integer> AGE = SynchedEntityData.defineId(CrimsonRiftExecution.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TARGET = SynchedEntityData.defineId(CrimsonRiftExecution.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<ItemStack> WEAPON = SynchedEntityData.defineId(CrimsonRiftExecution.class, EntityDataSerializers.ITEM_STACK);
    private UUID targetUuid;
    private int elapsed;
    private boolean finished;
    private long ageReceivedAt;

    public CrimsonRiftExecution(EntityType<? extends CrimsonRiftExecution> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    private static ResourceKey<DamageType> damageKey(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(RequiemAddon.LEGACY_NAMESPACE, name));
    }

    // First four blades are deliberate; later portals open at two-tick intervals.
    public static int launchTick(int blade) {
        if (blade < 4) return 12 + blade * 15;
        if (blade < 12) return 72 + (blade - 4) * 5;
        return 112 + (blade - 12) * 2;
    }
    public static int flightTicks(int blade) { return blade < 4 ? 30 : blade < 12 ? 18 : 9; }
    public static int impactTick(int blade) { return launchTick(blade) + flightTicks(blade); }
    public int age() { return entityData.get(AGE); }
    public float visualAge(float partial) {
        return age() + Math.min(2, Math.max(0, level().getGameTime() - ageReceivedAt)) + partial;
    }
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (AGE.equals(key)) ageReceivedAt = level().getGameTime();
    }
    public ItemStack weapon() { return entityData.get(WEAPON); }
    public Entity visualTarget() { return level().getEntity(entityData.get(TARGET)); }
    public boolean isTarget(Entity entity) { return entity.getUUID().equals(targetUuid); }

    public static boolean eligible(ServerPlayer owner, LivingEntity target) {
        return target != owner && target.isAlive() && !target.isSpectator() && target.isPickable()
                && !(target instanceof net.minecraft.world.entity.decoration.ArmorStand)
                && !owner.isAlliedTo(target) && !target.isAlliedTo(owner)
                && !(target instanceof TamableAnimal pet && pet.isOwnedBy(owner))
                && !(target instanceof net.minecraft.world.entity.animal.horse.AbstractHorse horse
                    && owner.getUUID().equals(horse.getOwnerUUID()))
                && (!(target instanceof Player other) || !other.isCreative() && owner.canHarmPlayer(other));
    }

    /** Server raycast: no client-supplied target, no locking through solid blocks. */
    public static LivingEntity aimedTarget(ServerPlayer owner) {
        Vec3 start = owner.getEyePosition();
        Vec3 end = start.add(owner.getLookAngle().scale(CrimsonRequiemConfig.EXECUTION_RANGE.get()));
        var wall = owner.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
        if (wall.getType() != HitResult.Type.MISS) end = wall.getLocation();
        double nearest = start.distanceToSqr(end);
        LivingEntity result = null;
        for (Entity entity : owner.level().getEntities(owner, new AABB(start, end).inflate(1))) {
            Entity root = entity instanceof net.minecraftforge.entity.PartEntity<?> part ? part.getParent() : entity;
            if (!(root instanceof LivingEntity living) || !eligible(owner, living)) continue;
            var box = entity.getBoundingBox().inflate(0.1);
            Vec3 hit = box.contains(start) ? start : box.clip(start, end).orElse(null);
            if (hit != null && start.distanceToSqr(hit) < nearest) {
                nearest = start.distanceToSqr(hit);
                result = living;
            }
        }
        return result;
    }

    public static boolean start(ServerPlayer owner) {
        LivingEntity target = aimedTarget(owner);
        if (target == null) {
            owner.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                    "message.ramesses_curse.crimson_requiem.need_target", CrimsonRequiemConfig.EXECUTION_RANGE.get().intValue()), true);
            return false;
        }
        if (!owner.level().getEntitiesOfClass(CrimsonRiftExecution.class, owner.getBoundingBox().inflate(96),
                cast -> !cast.finished && (cast.getOwner() == owner || cast.isTarget(target))).isEmpty()) return false;
        var cast = new CrimsonRiftExecution(RequiemEntities.CRIMSON_RIFT_EXECUTION.get(), owner.level());
        cast.setOwner(owner);
        cast.targetUuid = target.getUUID();
        cast.entityData.set(TARGET, target.getId());
        cast.entityData.set(WEAPON, CrimsonRequiemActions.weapon(owner).copy());
        CrimsonRequiemFlight.clear(cast.weapon());
        cast.setPos(target.getBoundingBox().getCenter());
        if (!owner.level().addFreshEntity(cast)) return false;
        cast.sound(SoundEvents.END_PORTAL_SPAWN, 0.45F, 1.35F);
        return true;
    }

    @Override protected void defineSynchedData() {
        entityData.define(AGE, 0);
        entityData.define(TARGET, -1);
        entityData.define(WEAPON, ItemStack.EMPTY);
    }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }

    @Override public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (finished) {
            entityData.set(AGE, ++elapsed);
            if (elapsed > FINAL_TICK + 14) discard();
            return;
        }
        if (!(getOwner() instanceof ServerPlayer owner) || !owner.isAlive() || owner.isSpectator()
                || owner.level() != level()) { discard(); return; }
        Entity found = targetUuid == null ? null : ((ServerLevel)level()).getEntity(targetUuid);
        if (!(found instanceof LivingEntity target) || !eligible(owner, target)
                || owner.distanceToSqr(target) > 96 * 96 || !level().hasChunkAt(target.blockPosition())) { discard(); return; }
        setPos(target.getBoundingBox().getCenter());
        entityData.set(TARGET, target.getId());
        entityData.set(AGE, ++elapsed);
        for (int i = 0; i < BLADES; i++) {
            if (elapsed == launchTick(i) - 8 && i < 4) sound(SoundEvents.ENDERMAN_TELEPORT, 0.6F, 0.6F + i * 0.1F);
            if (elapsed == launchTick(i)) sound(SoundEvents.TRIDENT_THROW, 0.65F, 0.8F + i * 0.035F);
            if (elapsed == impactTick(i)) strike(owner, target);
        }
        if (elapsed == GIANT_START) sound(SoundEvents.RESPAWN_ANCHOR_CHARGE, 1F, 0.65F);
        if (elapsed == GIANT_START + GIANT_CHARGE_TICKS) {
            sound(SoundEvents.TRIDENT_RIPTIDE_3, 1F, 0.7F);
            sound(SoundEvents.PLAYER_ATTACK_SWEEP, 0.8F, 0.55F);
        }
        if (elapsed >= FINAL_TICK) {
            // Mark before death events to make saving/re-entry unable to execute a second time.
            finished = true;
            blast(owner, target, true);
            execute(owner, target);
        }
    }

    private DamageSource source(ResourceKey<DamageType> key, ServerPlayer owner) {
        return new DamageSource(level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key), this, owner);
    }

    private void strike(ServerPlayer owner, LivingEntity target) {
        float trueDamage = CrimsonRequiemDamage.boomerangTrueDamage(owner);
        float magicDamage = CrimsonRequiemDamage.boomerangMagicDamage(owner);
        stagedHit(target, source(CrimsonRequiemDamage.TRUE, owner), trueDamage);
        stagedHit(target, source(CrimsonRequiemDamage.MAGIC, owner), magicDamage);
        owner.setLastHurtMob(target);
        sound(SoundEvents.TRIDENT_HIT, 0.55F, 1.2F);
        blast(owner, target, false);
    }

    private void stagedHit(LivingEntity target, DamageSource source, float damage) {
        float amount = Math.min(damage, Math.max(0, target.getHealth() - 1));
        if (amount > 0) target.hurt(source, amount);
    }

    private void blast(ServerPlayer owner, LivingEntity target, boolean finale) {
        ServerLevel server = (ServerLevel)level();
        Vec3 center = target.getBoundingBox().getCenter();
        server.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y, center.z, finale ? 4 : 1, 0.2, 0.25, 0.2, 0);
        server.sendParticles(ParticleTypes.ENCHANTED_HIT, center.x, center.y, center.z, finale ? 32 : 6, 0.4, 0.4, 0.4, 0.08);
        sound(SoundEvents.GENERIC_EXPLODE, finale ? 1.2F : 0.4F, finale ? 0.65F : 1.4F);
        if (finale) sound(SoundEvents.TRIDENT_THUNDER, 0.9F, 0.7F);
        double radius = CrimsonRequiemConfig.EXECUTION_BLAST_RADIUS.get();
        float damage = CrimsonRequiemConfig.EXECUTION_BLAST_DAMAGE.get().floatValue() * (finale ? 2 : 1);
        var source = source(BLAST, owner);
        // Living-only area damage preserves terrain and items dropped during this sequence.
        for (LivingEntity victim : level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(radius),
                entity -> eligible(owner, entity) && entity.getBoundingBox().getCenter().distanceToSqr(center) <= radius * radius)) {
            if (victim == target) { if (!finale) stagedHit(victim, source, damage); }
            else victim.hurt(source, damage);
        }
    }

    private void execute(ServerPlayer owner, LivingEntity target) {
        if (!target.isAlive()) return;
        DamageSource source = source(EXECUTION, owner);
        if (target instanceof com.eldren.wutao.ramesses_requiem.boss.WusheBoss) {
            // Wushe's defensive rules have priority over the sword's direct execution.
            target.hurt(source, target.getMaxHealth());
            return;
        }
        owner.setLastHurtMob(target);
        target.setLastHurtByPlayer(owner);
        ((com.eldren.wutao.ramesses_requiem.mixin.CrimsonExecutionCreditAccessor)target).crimsonRequiem$setPlayerCreditTime(100);
        target.getCombatTracker().recordDamage(source, target.getHealth());
        target.setHealth(0);
        // The normal death pipeline awards kill credit, loot, Looting and XP; never discard the victim.
        target.die(source);
    }

    private void sound(net.minecraft.sounds.SoundEvent sound, float volume, float pitch) {
        // A 32-block lock can be outside the old 16-block sound radius. Give the caster
        // a direct cue, while other players still hear the positional world sound once.
        Player caster = getOwner() instanceof Player player ? player : null;
        if (caster instanceof ServerPlayer player) player.playNotifySound(sound, SoundSource.PLAYERS, volume, pitch);
        level().playSound(caster, getX(), getY(), getZ(), sound, SoundSource.PLAYERS, volume, pitch);
    }

    /** Stable local positions shared by all viewers, without creating 24 network entities. */
    public Vec3 portalOffset(int index) {
        double angle = index * 2.399963229728653 + (getUUID().hashCode() & 255) * Math.PI / 128;
        double radius = 5.5 + (index % 4) * 0.55;
        return new Vec3(Math.cos(angle) * radius, 0.4 + (index % 5) * 0.55, Math.sin(angle) * radius);
    }
    public Vec3 bodyOffset(int index) {
        Entity target = visualTarget();
        double width = target == null ? 0.6 : Math.min(3, target.getBbWidth());
        double height = target == null ? 1.8 : Math.min(6, target.getBbHeight());
        return new Vec3(Math.cos(index * 2.4) * width * 0.2, ((index % 5) / 4D - 0.5) * height * 0.5,
                Math.sin(index * 2.4) * width * 0.2);
    }

    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (targetUuid != null) tag.putUUID("Target", targetUuid);
        tag.putInt("Elapsed", elapsed);
        tag.putBoolean("Finished", finished);
        tag.put("Weapon", weapon().save(new CompoundTag()));
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        targetUuid = tag.hasUUID("Target") ? tag.getUUID("Target") : null;
        elapsed = Math.max(0, Math.min(FINAL_TICK + 15, tag.getInt("Elapsed")));
        finished = tag.getBoolean("Finished") || elapsed >= FINAL_TICK;
        entityData.set(AGE, elapsed);
        entityData.set(WEAPON, ItemStack.of(tag.getCompound("Weapon")));
    }
}
