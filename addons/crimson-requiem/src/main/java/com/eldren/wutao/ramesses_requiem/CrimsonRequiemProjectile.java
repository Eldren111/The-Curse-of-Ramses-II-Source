package com.eldren.wutao.ramesses_requiem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.*;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3f;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class CrimsonRequiemProjectile extends Projectile {
    private static final EntityDataAccessor<Boolean> WAVE = SynchedEntityData.defineId(CrimsonRequiemProjectile.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ItemStack> WEAPON = SynchedEntityData.defineId(CrimsonRequiemProjectile.class, EntityDataSerializers.ITEM_STACK);
    private final Set<UUID> hit = new HashSet<>();
    private boolean returning;
    private boolean blockBreakingDisabled;
    private double travelled;
    private float attackDamage;
    private float strength;
    private boolean critical;
    private boolean ownsItem;
    private int originalSlot;
    private UUID trackingTarget;
    private final java.util.Map<UUID, Integer> pursuitHits = new java.util.HashMap<>();
    private int flyThroughUntil;
    private UUID flyThroughTarget;

    public CrimsonRequiemProjectile(EntityType<? extends CrimsonRequiemProjectile> type, Level level) {
        super(type, level); noPhysics = true; setNoGravity(true);
    }

    public static CrimsonRequiemProjectile create(ServerPlayer player, boolean wave) {
        CrimsonRequiemProjectile result = new CrimsonRequiemProjectile(RequiemEntities.CRIMSON_REQUIEM_PROJECTILE.get(), player.level());
        result.setOwner(player);
        result.originalSlot = CrimsonRequiemActions.weaponHand(player) == net.minecraft.world.InteractionHand.OFF_HAND
                ? 40 : player.getInventory().selected;
        result.entityData.set(WAVE, wave);
        result.entityData.set(WEAPON, CrimsonRequiemActions.weapon(player).copy());
        result.setPos(player.getEyePosition().add(0, -0.2, 0));
        result.shoot(player.getLookAngle().x, player.getLookAngle().y, player.getLookAngle().z, wave ? 1.8F : 1.4F, 0);
        result.strength = player.getAttackStrengthScale(0.5F);
        result.attackDamage = CrimsonRequiemDamage.meleeAttackDamage(player);
        result.critical = result.strength > 0.9F && player.fallDistance > 0 && !player.onGround()
                && !player.onClimbable() && !player.isInWater() && !player.isPassenger() && !player.isSprinting()
                && !player.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS);
        return result;
    }

    public boolean isWave() { return entityData.get(WAVE); }
    public ItemStack weapon() { return entityData.get(WEAPON); }
    private boolean hasPursuit() { return !isWave() && weapon().getEnchantmentLevel(ModEnchantments.SOUL_PURSUIT.get()) > 0; }
    @Override protected void defineSynchedData() { entityData.define(WAVE, false); entityData.define(WEAPON, ItemStack.EMPTY); }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }

    @Override public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (isWave()) {
                CrimsonRequiemClient.emitSteamCross(this);
                return;
            }
            level().addParticle(new DustParticleOptions(new Vector3f(0.72F, 0.06F, 0.42F), isWave() ? 1.5F : 1F),
                    getX(), getY(), getZ(), 0, 0, 0);
            return;
        }
        if (!(getOwner() instanceof ServerPlayer owner) || !owner.isAlive() || owner.isSpectator() || owner.level() != level()) {
            finish(null); return;
        }
        if (!isWave() && !ownsItem && !CrimsonRequiemFlight.active(owner, getUUID())) { discard(); return; }
        boolean pursuit = hasPursuit();
        if (tickCount > (isWave() ? 80 : pursuit ? 240 : 100)) { finish(owner); return; }
        if (!isWave() && !returning && (pursuit ? tickCount >= 200 : travelled >= CrimsonRequiemConfig.THROW_RANGE.get())) {
            returning = true; hit.clear();
        }
        if (pursuit && !returning) trackNearby(owner);
        Vec3 start = position();
        if (returning) {
            Vec3 destination = owner.getEyePosition().add(0, -0.2, 0);
            if (destination.distanceTo(start) < 1.8) { finish(owner); return; }
            setDeltaMovement(destination.subtract(start).normalize().scale(1.8));
        }
        Vec3 end = start.add(getDeltaMovement());
        // Never force-load terrain. Return the item instead of stranding it on a chunk boundary.
        if (!level().hasChunkAt(BlockPos.containing(end))) { finish(owner); return; }
        boolean stopped = false;
        if (isWave()) {
            BlockHitResult block = level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (block.getType() != HitResult.Type.MISS) { end = block.getLocation(); stopped = true; }
        }
        java.util.List<PathHit> collisions = new java.util.ArrayList<>();
        for (Entity target : level().getEntities(this, new AABB(start, end).inflate(1), e ->
                e != owner && e.isAlive() && !e.isSpectator() && e.isPickable()
                        && !(e instanceof net.minecraft.world.entity.item.ItemEntity) && !(e instanceof Projectile))) {
            AABB box = target.getBoundingBox().inflate(isWave() ? 0.3 : 0.4);
            Vec3 contact = box.contains(start) ? start : box.clip(start, end).orElse(null);
            if (contact == null) continue;
            if (target instanceof Player other && !owner.canHarmPlayer(other)) continue;
            if (pursuit && (owner.isAlliedTo(target) || target.isAlliedTo(owner)
                    || target instanceof TamableAnimal pet && pet.isOwnedBy(owner)
                    || target instanceof net.minecraft.world.entity.animal.horse.AbstractHorse horse
                        && owner.getUUID().equals(horse.getOwnerUUID())
                    || target instanceof Player other && other.isCreative())) continue;
            collisions.add(new PathHit(target, start.distanceTo(contact)));
        }
        collisions.sort(java.util.Comparator.comparingDouble(PathHit::distance));
        if (!isWave() && !blockBreakingDisabled) {
            double firstEntityDistance = collisions.isEmpty() ? Double.POSITIVE_INFINITY : collisions.get(0).distance();
            if (CrimsonRequiemConfig.BREAK_BLOCKS.get()) breakPath(start, end, firstEntityDistance);
            // A contact permanently ends terrain destruction, including on the return trip.
            blockBreakingDisabled = !collisions.isEmpty();
        }
        for (PathHit collision : collisions) {
            Entity target = collision.target();
            Entity identity = target instanceof net.minecraftforge.entity.PartEntity<?> part ? part.getParent() : target;
            if (pursuit && !returning) {
                // Separate repeated passes from consecutive collision ticks inside the same body.
                if (tickCount - pursuitHits.getOrDefault(identity.getUUID(), -100) < 10) continue;
                pursuitHits.put(identity.getUUID(), tickCount);
                trackingTarget = null;
                flyThroughTarget = identity.getUUID();
                flyThroughUntil = tickCount + 4;
            } else if (!hit.add(identity.getUUID())) continue;
            if (isWave()) hitWave(owner, target); else hitBoomerang(owner, target);
        }
        travelled += start.distanceTo(end);
        setPos(end);
        if (stopped || isWave() && travelled >= CrimsonRequiemConfig.WAVE_RANGE.get()) finish(owner);
    }

    private DamageSource source(String id, Player owner) {
        return new DamageSource(level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(
                ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(RequiemAddon.LEGACY_NAMESPACE, id))), this, owner);
    }

    private void hitBoomerang(ServerPlayer owner, Entity target) {
        CrimsonRequiemDamage.hitScalingBlade(owner, this, target);
    }

    void hitWave(ServerPlayer owner, Entity target) {
        float damage = attackDamage * (0.2F + strength * strength * 0.8F);
        var crit = net.minecraftforge.common.ForgeHooks.getCriticalHit(owner, target, critical, critical ? 1.5F : 1F);
        if (crit != null) damage *= crit.getDamageModifier();
        damage += EnchantmentHelper.getDamageBonus(weapon(), target instanceof LivingEntity living ? living.getMobType() : MobType.UNDEFINED) * strength;
        if (target.hurt(source("crimson_requiem_wave", owner), damage)) {
            owner.setLastHurtMob(target);
            int fire = weapon().getEnchantmentLevel(Enchantments.FIRE_ASPECT);
            if (fire > 0) target.setSecondsOnFire(fire * 4);
            if (target instanceof LivingEntity living) {
                EnchantmentHelper.doPostHurtEffects(living, owner);
                int knockback = weapon().getEnchantmentLevel(Enchantments.KNOCKBACK);
                if (knockback > 0) living.knockback(knockback * 0.5, -getDeltaMovement().x, -getDeltaMovement().z);
            }
        }
    }

    private record PathHit(Entity target, double distance) {}

    private boolean canTrack(ServerPlayer owner, LivingEntity target) {
        return target != owner && target.isAlive() && !target.isSpectator() && target.isPickable()
                && !(target instanceof net.minecraft.world.entity.decoration.ArmorStand)
                && !owner.isAlliedTo(target) && !target.isAlliedTo(owner)
                && !(target instanceof TamableAnimal pet && pet.isOwnedBy(owner))
                && !(target instanceof net.minecraft.world.entity.animal.horse.AbstractHorse horse
                    && owner.getUUID().equals(horse.getOwnerUUID()))
                && (!(target instanceof Player other) || !other.isCreative() && owner.canHarmPlayer(other))
                && owner.distanceToSqr(target) <= 20 * 20;
    }

    private void trackNearby(ServerPlayer owner) {
        Entity passed = flyThroughTarget == null ? null : ((ServerLevel)level()).getEntity(flyThroughTarget);
        if (tickCount < flyThroughUntil || passed != null && passed.isAlive()
                && passed.getBoundingBox().inflate(0.5).contains(position())) {
            // Pass out of the target before turning around instead of stopping at its centre.
            setDeltaMovement(getDeltaMovement().normalize().scale(1.4));
            return;
        }
        flyThroughTarget = null;
        Entity previous = trackingTarget == null ? null : ((ServerLevel)level()).getEntity(trackingTarget);
        LivingEntity target = previous instanceof LivingEntity living && canTrack(owner, living) ? living : null;
        if (target == null) {
            target = level().getEntitiesOfClass(LivingEntity.class, owner.getBoundingBox().inflate(20),
                    entity -> canTrack(owner, entity)).stream()
                    .min(java.util.Comparator.<LivingEntity>comparingInt(
                            entity -> pursuitHits.getOrDefault(entity.getUUID(), -100))
                            .thenComparingDouble(this::distanceToSqr)).orElse(null);
        }
        trackingTarget = target == null ? null : target.getUUID();
        Vec3 destination;
        if (target != null) {
            destination = target.getBoundingBox().getCenter();
        } else {
            // Stay nearby and continue acquiring new targets until the ten-second window ends.
            double angle = tickCount * 0.16;
            destination = owner.position().add(Math.cos(angle) * 3, 1.8, Math.sin(angle) * 3);
        }
        Vec3 direction = destination.subtract(position());
        setDeltaMovement(direction.normalize().scale(target != null ? 1.4 : Math.min(1.4, direction.length())));
    }

    private void breakPath(Vec3 start, Vec3 end, double firstEntityDistance) {
        if (com.eldren.wutao.ramesses_requiem.boss.AstralArena.isArena(level())) return;
        Set<BlockPos> visited = new HashSet<>();
        int steps = Math.max(1, (int)Math.ceil(start.distanceTo(end) / 0.2));
        for (int i = 0; i <= steps; i++) {
            Vec3 point = start.lerp(end, i / (double)steps);
            for (BlockPos mutable : BlockPos.betweenClosed(BlockPos.containing(point.add(-0.3, -0.3, -0.3)),
                    BlockPos.containing(point.add(0.3, 0.3, 0.3)))) {
                BlockPos pos = mutable.immutable();
                if (!visited.add(pos) || !level().hasChunkAt(pos) || level().isOutsideBuildHeight(pos)) continue;
                // Compare swept contacts, not entity iteration order or end-of-tick positions.
                AABB blockBox = new AABB(pos).inflate(0.3);
                Vec3 contact = blockBox.contains(start) ? start : blockBox.clip(start, end).orElse(null);
                if (contact == null || start.distanceTo(contact) >= firstEntityDistance - 1.0E-6) continue;
                var state = level().getBlockState(pos);
                if (state.isAir() || state.liquid()) continue;
                ItemStack drop = new ItemStack(state.getBlock().asItem());
                var blockEntity = level().getBlockEntity(pos);
                // Keep block-entity contents in the item; remove it before onRemove can also spill contents.
                if (blockEntity != null && !drop.isEmpty()) blockEntity.saveToItem(drop);
                if (blockEntity != null && !drop.isEmpty()) level().removeBlockEntity(pos);
                if (level().setBlock(pos, state.getFluidState().createLegacyBlock(), 3)) {
                    if (!drop.isEmpty() && !drop.is(Items.AIR)) Block.popResource(level(), pos, drop);
                } else if (blockEntity != null) { level().setBlockEntity(blockEntity); }
            }
        }
    }

    private void finish(ServerPlayer owner) {
        boolean released = !isWave() && owner != null && CrimsonRequiemFlight.release(owner, getUUID());
        boolean legacy = ownsItem;
        // Compatibility with projectiles saved before inventory slot reservation was introduced.
        if (ownsItem) {
            ownsItem = false;
            ItemStack stack = weapon().copy();
            if (owner != null && originalSlot >= 0 && originalSlot < 9 && owner.getInventory().getItem(originalSlot).isEmpty()) {
                owner.getInventory().setItem(originalSlot, stack);
                stack = ItemStack.EMPTY;
            }
            if (owner == null || !owner.getInventory().add(stack)) {
                if (owner != null) owner.drop(stack, false); else spawnAtLocation(stack);
            }
        }
        if (!isWave() && owner != null && (released || legacy)) CrimsonRequiemActions.returned(owner);
        discard();
    }

    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Wave", isWave()); tag.put("Weapon", weapon().save(new CompoundTag()));
        tag.putBoolean("Returning", returning); tag.putBoolean("OwnsItem", ownsItem);
        tag.putBoolean("BlockBreakingDisabled", blockBreakingDisabled);
        tag.putInt("OriginalSlot", originalSlot);
        tag.putDouble("Travelled", travelled); tag.putInt("Age", tickCount);
        tag.putFloat("AttackDamage", attackDamage); tag.putFloat("Strength", strength); tag.putBoolean("Critical", critical);
        net.minecraft.nbt.ListTag hits = new net.minecraft.nbt.ListTag();
        for (UUID uuid : hit) hits.add(net.minecraft.nbt.StringTag.valueOf(uuid.toString()));
        tag.put("Hits", hits);
        net.minecraft.nbt.ListTag pursuitHistory = new net.minecraft.nbt.ListTag();
        pursuitHits.forEach((uuid, age) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Target", uuid); entry.putInt("Age", age);
            pursuitHistory.add(entry);
        });
        tag.put("PursuitHits", pursuitHistory);
        tag.putInt("FlyThroughUntil", flyThroughUntil);
        if (flyThroughTarget != null) tag.putUUID("FlyThroughTarget", flyThroughTarget);
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(WAVE, tag.getBoolean("Wave")); entityData.set(WEAPON, ItemStack.of(tag.getCompound("Weapon")));
        returning = tag.getBoolean("Returning"); ownsItem = tag.getBoolean("OwnsItem");
        blockBreakingDisabled = tag.getBoolean("BlockBreakingDisabled");
        originalSlot = tag.getInt("OriginalSlot");
        travelled = tag.getDouble("Travelled"); tickCount = tag.getInt("Age");
        attackDamage = tag.getFloat("AttackDamage"); strength = tag.getFloat("Strength"); critical = tag.getBoolean("Critical");
        for (var entry : tag.getList("Hits", 8)) { try { hit.add(UUID.fromString(entry.getAsString())); } catch (IllegalArgumentException ignored) {} }
        pursuitHits.clear();
        for (var entry : tag.getList("PursuitHits", 10)) {
            CompoundTag record = (CompoundTag)entry;
            if (record.hasUUID("Target")) pursuitHits.put(record.getUUID("Target"), record.getInt("Age"));
        }
        flyThroughUntil = tag.getInt("FlyThroughUntil");
        flyThroughTarget = tag.hasUUID("FlyThroughTarget") ? tag.getUUID("FlyThroughTarget") : null;
    }
}
