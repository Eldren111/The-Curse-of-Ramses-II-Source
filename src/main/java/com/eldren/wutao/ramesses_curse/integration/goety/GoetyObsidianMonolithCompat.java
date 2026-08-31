package com.eldren.wutao.ramesses_curse.integration.goety;

import com.eldren.wutao.ramesses_curse.Config;
import com.eldren.wutao.ramesses_curse.DuatSunEaterEntity;
import com.eldren.wutao.ramesses_curse.GoetyMonolithProtectionPacket;
import com.eldren.wutao.ramesses_curse.ModNetwork;
import com.eldren.wutao.ramesses_curse.RamessesEndCrystalEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.fml.ModList;
import com.eldren.wutao.ramesses_curse.PacketDistributor;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class GoetyObsidianMonolithCompat {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String GOETY_MOD_ID = "goety";
    private static final ResourceLocation OBSIDIAN_MONOLITH_ID =
            ResourceLocation.fromNamespaceAndPath(GOETY_MOD_ID, "obsidian_monolith");
    private static final List<TagKey<EntityType<?>>> BOSS_TAGS = List.of(
            bossTag("forge", "bosses"),
            bossTag("forge", "boss"),
            bossTag("c", "bosses"),
            bossTag("c", "boss"));
    private static final String DYNASTY_SHIELD_ASSIGNED = "RamessesDynastyShieldAssigned";
    private static final String DYNASTY_SHIELD_LEVEL = "RamessesDynastyShieldLevel";
    private static final String ATTACK_ATTEMPTED = "RamessesGoetyMonolithAttackAttempted";
    private static final String LINKED_MONOLITH = "RamessesGoetyMonolithUuid";
    private static final String LINKED_MONOLITH_COUNT = "RamessesGoetyMonolithCount";
    private static final String MANAGED_MONOLITH = "RamessesManagedGoetyMonolith";
    private static final String ISLAND_MONOLITH = "RamessesEndIslandMonolith";
    private static final String ISLAND_CRYSTAL_OWNER = "RamessesEndIslandCrystalOwner";
    private static final String ISLAND_CRYSTAL_MONOLITH = "RamessesEndIslandCrystalMonolith";
    private static final String LINKED_OWNER = "RamessesGoetyMonolithOwnerUuid";
    private static final String REPOSITION_AT = "RamessesGoetyMonolithRepositionAt";
    public static final String CLIENT_PROTECTION_ACTIVE = "RamessesGoetyMonolithProtectionActive";
    private static final float TIER_NINE_TRIGGER_CHANCE = 0.05F;
    private static final float BOSS_TRIGGER_CHANCE = 0.25F;
    private static final int BOSS_MONOLITH_COUNT = 3;
    private static final double FORMATION_RADIUS = 8.0D;
    private static final double REPOSITION_DISTANCE_SQR = 18.0D * 18.0D;
    private static final int REPOSITION_DELAY_TICKS = 20;
    private GoetyObsidianMonolithCompat() {
    }

    public static void trySpawnIslandMonolith(
            ServerLevel level,
            BlockPos position,
            RamessesEndCrystalEntity crystal) {
        if (!ModList.get().isLoaded(GOETY_MOD_ID)) {
            return;
        }
        EntityType<?> monolithType = BuiltInRegistries.ENTITY_TYPE.getOptional(OBSIDIAN_MONOLITH_ID).orElse(null);
        if (monolithType == null) {
            return;
        }
        AABB searchBox = new AABB(position).inflate(3.0D, 5.0D, 3.0D);
        List<Mob> existing = level.getEntitiesOfClass(Mob.class, searchBox, mob ->
                mob.getType() == monolithType && mob.getPersistentData().getBoolean(ISLAND_MONOLITH));
        if (!existing.isEmpty()) {
            bindIslandMonolith(existing.get(0), crystal);
            return;
        }
        Entity created = monolithType.create(level);
        if (!(created instanceof Mob monolith)) {
            return;
        }
        monolith.moveTo(position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D, 0.0F, 0.0F);
        monolith.getPersistentData().putBoolean(ISLAND_MONOLITH, true);
        monolith.setPersistenceRequired();
        monolith.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(position),
                MobSpawnType.STRUCTURE,
                null);
        disableHereticSpawning(monolith);
        if (level.addFreshEntity(monolith)) {
            bindIslandMonolith(monolith, crystal);
        }
    }

    public static void bindExistingIslandMonolith(
            ServerLevel level,
            BlockPos position,
            RamessesEndCrystalEntity crystal) {
        AABB searchBox = new AABB(position).inflate(5.0D, 8.0D, 5.0D);
        List<Mob> monoliths = level.getEntitiesOfClass(Mob.class, searchBox,
                GoetyObsidianMonolithCompat::isIslandMonolith);
        if (!monoliths.isEmpty()) {
            bindIslandMonolith(monoliths.get(0), crystal);
        }
    }

    private static void bindIslandMonolith(Mob monolith, RamessesEndCrystalEntity crystal) {
        monolith.getPersistentData().putUUID(ISLAND_CRYSTAL_OWNER, crystal.getUUID());
        crystal.getPersistentData().putUUID(ISLAND_CRYSTAL_MONOLITH, monolith.getUUID());
        crystal.markTowerGuardianInitialized();
        setGoetyOwnerId(monolith, crystal.getUUID());
    }

    private static void setGoetyOwnerId(Mob monolith, UUID crystalUuid) {
        try {
            monolith.getClass().getMethod("setOwnerId", UUID.class).invoke(monolith, crystalUuid);
        } catch (ReflectiveOperationException exception) {
            LOGGER.warn("Unable to expose the island crystal through Goety's native owner data", exception);
        }
    }

    private static void disableHereticSpawning(Mob monolith) {
        try {
            monolith.getClass().getField("shouldSpawnHeretics").setBoolean(monolith, false);
        } catch (ReflectiveOperationException exception) {
            LOGGER.debug("Unable to set Goety island monolith guardian flag", exception);
        }
    }

    public static boolean isIslandMonolith(Entity entity) {
        return entity instanceof Mob
                && entity.getPersistentData().getBoolean(ISLAND_MONOLITH);
    }

    public static boolean hasLivingIslandMonoliths(ServerLevel level, BlockPos center, double radius) {
        if (!ModList.get().isLoaded(GOETY_MOD_ID)) {
            return false;
        }
        AABB islandBounds = new AABB(
                center.getX() - radius, level.getMinBuildHeight(), center.getZ() - radius,
                center.getX() + radius, level.getMaxBuildHeight(), center.getZ() + radius);
        return !level.getEntitiesOfClass(Mob.class, islandBounds,
                mob -> mob.isAlive() && isIslandMonolith(mob)).isEmpty();
    }

    public static boolean protectsIslandCrystal(ServerLevel level, RamessesEndCrystalEntity crystal) {
        CompoundTag crystalData = crystal.getPersistentData();
        if (crystalData.hasUUID(ISLAND_CRYSTAL_MONOLITH)) {
            Entity monolith = level.getEntity(crystalData.getUUID(ISLAND_CRYSTAL_MONOLITH));
            if (isLiveBinding(monolith, crystal.getUUID())) {
                return true;
            }
            crystalData.remove(ISLAND_CRYSTAL_MONOLITH);
        }
        AABB searchBox = crystal.getBoundingBox().inflate(8.0D, 12.0D, 8.0D);
        List<Mob> nearby = level.getEntitiesOfClass(Mob.class, searchBox,
                mob -> mob.isAlive() && isIslandMonolith(mob));
        if (nearby.isEmpty()) {
            return false;
        }
        bindIslandMonolith(nearby.get(0), crystal);
        return true;
    }

    public static boolean hasLivingIslandCrystalOwner(Entity monolith) {
        if (!isIslandMonolith(monolith)
                || !monolith.getPersistentData().hasUUID(ISLAND_CRYSTAL_OWNER)
                || !(monolith.level() instanceof ServerLevel level)) {
            return false;
        }
        Entity crystal = level.getEntity(monolith.getPersistentData().getUUID(ISLAND_CRYSTAL_OWNER));
        return crystal instanceof RamessesEndCrystalEntity && crystal.isAlive();
    }

    private static boolean isLiveBinding(Entity monolith, UUID crystalUuid) {
        return monolith instanceof Mob
                && monolith.isAlive()
                && isIslandMonolith(monolith)
                && monolith.getPersistentData().hasUUID(ISLAND_CRYSTAL_OWNER)
                && crystalUuid.equals(monolith.getPersistentData().getUUID(ISLAND_CRYSTAL_OWNER));
    }

    public static boolean shouldRejectIslandGuardian(Entity entity, Level level) {
        if (!(level instanceof ServerLevel serverLevel)
                || !(entity instanceof Mob)
                || isIslandMonolith(entity)
                || !isGoetyEntity(entity)) {
            return false;
        }
        AABB searchBox = entity.getBoundingBox().inflate(16.0D, 20.0D, 16.0D);
        return !serverLevel.getEntitiesOfClass(Mob.class, searchBox,
                GoetyObsidianMonolithCompat::isIslandMonolith).isEmpty();
    }

    public static void removeIslandGuardianMobs(ServerLevel level, BlockPos position) {
        AABB monolithSearch = new AABB(position).inflate(5.0D, 8.0D, 5.0D);
        if (level.getEntitiesOfClass(Mob.class, monolithSearch,
                GoetyObsidianMonolithCompat::isIslandMonolith).isEmpty()) {
            return;
        }
        AABB guardianSearch = new AABB(position).inflate(16.0D, 20.0D, 16.0D);
        level.getEntitiesOfClass(Mob.class, guardianSearch,
                        mob -> !isIslandMonolith(mob) && isGoetyEntity(mob))
                .forEach(Entity::discard);
    }

    private static boolean isGoetyEntity(Entity entity) {
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return GOETY_MOD_ID.equals(entityId.getNamespace());
    }

    private static TagKey<EntityType<?>> bossTag(String namespace, String path) {
        return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    public static boolean isManagedMonolith(LivingEntity entity) {
        return entity.getPersistentData().getBoolean(MANAGED_MONOLITH);
    }

    public static boolean isTaggedBoss(LivingEntity entity) {
        return BOSS_TAGS.stream().anyMatch(entity.getType()::is);
    }

    public static boolean isOwnerFriendlyFire(LivingEntity target, DamageSource source) {
        if (!isManagedMonolith(target)) {
            return false;
        }
        CompoundTag data = target.getPersistentData();
        if (!data.hasUUID(LINKED_OWNER)) {
            return false;
        }
        UUID ownerUuid = data.getUUID(LINKED_OWNER);
        if (matchesOwner(source.getEntity(), ownerUuid) || matchesOwner(source.getDirectEntity(), ownerUuid)) {
            return true;
        }
        return source.getDirectEntity() instanceof Projectile projectile
                && matchesOwner(projectile.getOwner(), ownerUuid);
    }

    public static boolean isIslandMonolithFriendlyFire(LivingEntity target, DamageSource source) {
        return isIslandMonolith(target)
                && (source.getEntity() instanceof DuatSunEaterEntity
                || source.getDirectEntity() instanceof DuatSunEaterEntity);
    }

    public static boolean isOwnedMonolith(LivingEntity target, LivingEntity owner) {
        CompoundTag data = target.getPersistentData();
        return isManagedMonolith(target)
                && data.hasUUID(LINKED_OWNER)
                && owner.getUUID().equals(data.getUUID(LINKED_OWNER));
    }

    private static boolean matchesOwner(Entity entity, UUID ownerUuid) {
        return entity != null && ownerUuid.equals(entity.getUUID());
    }

    public static void tickLinkedMonoliths(LivingEntity owner) {
        if (!(owner.level() instanceof ServerLevel level)) {
            return;
        }

        CompoundTag data = owner.getPersistentData();
        List<UUID> linked = data.hasUUID(LINKED_MONOLITH) || data.getInt(LINKED_MONOLITH_COUNT) > 0
                ? maintainLinks(level, owner)
                : List.of();
        syncProtectionVisual(owner, !linked.isEmpty() && isProtectionEnabled(owner), false);
    }

    public static void syncProtectionVisualToPlayer(LivingEntity owner, ServerPlayer player) {
        if (!(owner.level() instanceof ServerLevel level)) {
            return;
        }
        boolean active = isProtectionEnabled(owner) && readLinkedMonoliths(owner.getPersistentData()).stream()
                .map(level::getEntity)
                .anyMatch(monolith -> isValidLink(owner, monolith));
        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new GoetyMonolithProtectionPacket(owner.getId(), active));
    }

    private static void syncProtectionVisual(LivingEntity owner, boolean active, boolean force) {
        CompoundTag data = owner.getPersistentData();
        if (!force && data.contains(CLIENT_PROTECTION_ACTIVE)
                && data.getBoolean(CLIENT_PROTECTION_ACTIVE) == active) {
            return;
        }
        data.putBoolean(CLIENT_PROTECTION_ACTIVE, active);
        ModNetwork.CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> owner),
                new GoetyMonolithProtectionPacket(owner.getId(), active));
    }

    private static boolean isProtectionEnabled(LivingEntity owner) {
        CompoundTag data = owner.getPersistentData();
        return Config.DYNASTY_SHIELD_ENABLED.get()
                && data.getBoolean(DYNASTY_SHIELD_ASSIGNED)
                && (data.getInt(DYNASTY_SHIELD_LEVEL) == 9 || isTaggedBoss(owner));
    }

    public static void trySpawnAfterPlayerAttack(LivingEntity owner) {
        if (isManagedMonolith(owner)
                || owner.level().isClientSide
                || !(owner.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        CompoundTag ownerData = owner.getPersistentData();
        if (!ownerData.getBoolean(DYNASTY_SHIELD_ASSIGNED)) {
            return;
        }

        boolean boss = isTaggedBoss(owner);
        int shieldLevel = ownerData.getInt(DYNASTY_SHIELD_LEVEL);
        int desiredCount = boss ? BOSS_MONOLITH_COUNT : shieldLevel == 9 ? 1 : 0;
        if (desiredCount == 0) {
            return;
        }

        List<UUID> linked = maintainLinks(serverLevel, owner);
        if (linked.size() >= desiredCount
                || ownerData.getBoolean(ATTACK_ATTEMPTED)
                || !ModList.get().isLoaded(GOETY_MOD_ID)) {
            return;
        }

        ownerData.putBoolean(ATTACK_ATTEMPTED, true);
        float triggerChance = boss ? BOSS_TRIGGER_CHANCE : TIER_NINE_TRIGGER_CHANCE;
        if (owner.getRandom().nextFloat() >= triggerChance) {
            return;
        }

        while (linked.size() < desiredCount) {
            Mob monolith = createMonolith(serverLevel, owner, linked.size(), desiredCount);
            if (monolith == null) {
                break;
            }
            linked.add(monolith.getUUID());
        }
        writeLinkedMonoliths(ownerData, linked);
    }

    private static Mob createMonolith(ServerLevel level, LivingEntity owner, int formationIndex, int formationCount) {
        EntityType<?> monolithType = BuiltInRegistries.ENTITY_TYPE.getOptional(OBSIDIAN_MONOLITH_ID).orElse(null);
        if (monolithType == null) {
            return null;
        }
        Entity created = monolithType.create(level);
        if (!(created instanceof Mob monolith)
                || !placeInFormation(level, owner, monolith, formationIndex, formationCount, true)) {
            return null;
        }

        try {
            Method setTrueOwner = monolith.getClass().getMethod("setTrueOwner", LivingEntity.class);
            setTrueOwner.invoke(monolith, owner);
        } catch (ReflectiveOperationException exception) {
            LOGGER.warn("Unable to bind Goety obsidian monolith to dynasty shield owner {}", owner.getUUID(), exception);
            monolith.discard();
            return null;
        }

        CompoundTag monolithData = monolith.getPersistentData();
        monolithData.putBoolean(MANAGED_MONOLITH, true);
        monolithData.putUUID(LINKED_OWNER, owner.getUUID());
        monolith.setGlowingTag(true);
        monolith.setPersistenceRequired();
        monolith.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(monolith.blockPosition()),
                MobSpawnType.MOB_SUMMONED,
                null);
        if (monolith.isRemoved() || !level.addFreshEntity(monolith)) {
            return null;
        }
        return monolith;
    }

    public static boolean blocksDamage(LivingEntity owner) {
        if (owner.level().isClientSide || !(owner.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        CompoundTag ownerData = owner.getPersistentData();
        if (!Config.DYNASTY_SHIELD_ENABLED.get()
                || !ownerData.getBoolean(DYNASTY_SHIELD_ASSIGNED)
                || (ownerData.getInt(DYNASTY_SHIELD_LEVEL) != 9 && !isTaggedBoss(owner))) {
            return false;
        }

        List<UUID> linked = maintainLinks(serverLevel, owner);
        if (linked.isEmpty()) {
            return false;
        }
        for (UUID uuid : linked) {
            Entity monolith = serverLevel.getEntity(uuid);
            if (monolith != null) {
                serverLevel.broadcastEntityEvent(monolith, (byte) 6);
            }
        }
        serverLevel.playSound(
                null,
                owner.blockPosition(),
                SoundEvents.SHIELD_BLOCK,
                SoundSource.HOSTILE,
                0.85F,
                0.55F + owner.getRandom().nextFloat() * 0.15F);
        return true;
    }

    public static void removeLinkedMonolith(LivingEntity owner) {
        if (!(owner.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        CompoundTag ownerData = owner.getPersistentData();
        for (UUID uuid : readLinkedMonoliths(ownerData)) {
            Entity monolith = serverLevel.getEntity(uuid);
            if (isValidLink(owner, monolith)) {
                monolith.discard();
            }
        }
        writeLinkedMonoliths(ownerData, List.of());
        syncProtectionVisual(owner, false, false);
    }

    private static List<UUID> maintainLinks(ServerLevel level, LivingEntity owner) {
        List<UUID> valid = new ArrayList<>();
        List<UUID> linked = readLinkedMonoliths(owner.getPersistentData());
        boolean outsideRange = false;
        for (UUID uuid : linked) {
            Entity monolith = level.getEntity(uuid);
            if (!isValidLink(owner, monolith)) {
                continue;
            }
            monolith.setGlowingTag(true);
            outsideRange |= monolith.distanceToSqr(owner) > REPOSITION_DISTANCE_SQR;
            valid.add(uuid);
        }
        writeLinkedMonoliths(owner.getPersistentData(), valid);
        updateDelayedReposition(level, owner, valid, outsideRange);
        return valid;
    }

    private static void updateDelayedReposition(
            ServerLevel level,
            LivingEntity owner,
            List<UUID> linked,
            boolean outsideRange) {
        CompoundTag ownerData = owner.getPersistentData();
        if (linked.isEmpty() || !outsideRange) {
            ownerData.remove(REPOSITION_AT);
            return;
        }

        long gameTime = level.getGameTime();
        long repositionAt = ownerData.getLong(REPOSITION_AT);
        if (repositionAt <= 0L) {
            ownerData.putLong(REPOSITION_AT, gameTime + REPOSITION_DELAY_TICKS);
            return;
        }
        if (gameTime < repositionAt) {
            return;
        }

        for (int index = 0; index < linked.size(); index++) {
            Entity monolith = level.getEntity(linked.get(index));
            if (!isValidLink(owner, monolith)) {
                continue;
            }
            spawnTeleportParticles(level, monolith);
            if (placeInFormation(level, owner, monolith, index, linked.size(), true)) {
                monolith.setDeltaMovement(0.0D, 0.0D, 0.0D);
                spawnTeleportParticles(level, monolith);
            }
        }
        level.playSound(null, owner.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.HOSTILE, 0.8F, 0.65F);
        ownerData.remove(REPOSITION_AT);
    }

    private static void spawnTeleportParticles(ServerLevel level, Entity monolith) {
        level.sendParticles(
                ParticleTypes.REVERSE_PORTAL,
                monolith.getX(),
                monolith.getY() + monolith.getBbHeight() * 0.5D,
                monolith.getZ(),
                18,
                monolith.getBbWidth() * 0.45D,
                monolith.getBbHeight() * 0.45D,
                monolith.getBbWidth() * 0.45D,
                0.03D);
    }

    private static List<UUID> readLinkedMonoliths(CompoundTag ownerData) {
        List<UUID> linked = new ArrayList<>();
        int count = Math.max(ownerData.getInt(LINKED_MONOLITH_COUNT), ownerData.hasUUID(LINKED_MONOLITH) ? 1 : 0);
        for (int index = 0; index < count; index++) {
            String key = linkedMonolithKey(index);
            if (ownerData.hasUUID(key)) {
                UUID uuid = ownerData.getUUID(key);
                if (!linked.contains(uuid)) {
                    linked.add(uuid);
                }
            }
        }
        return linked;
    }

    private static void writeLinkedMonoliths(CompoundTag ownerData, List<UUID> linked) {
        int oldCount = Math.max(ownerData.getInt(LINKED_MONOLITH_COUNT), BOSS_MONOLITH_COUNT);
        for (int index = 0; index < oldCount; index++) {
            ownerData.remove(linkedMonolithKey(index));
        }
        ownerData.putInt(LINKED_MONOLITH_COUNT, linked.size());
        for (int index = 0; index < linked.size(); index++) {
            ownerData.putUUID(linkedMonolithKey(index), linked.get(index));
        }
    }

    private static String linkedMonolithKey(int index) {
        return index == 0 ? LINKED_MONOLITH : LINKED_MONOLITH + index;
    }

    private static boolean isValidLink(LivingEntity owner, Entity monolith) {
        return monolith != null
                && monolith.isAlive()
                && monolith.getPersistentData().getBoolean(MANAGED_MONOLITH)
                && monolith.getPersistentData().hasUUID(LINKED_OWNER)
                && owner.getUUID().equals(monolith.getPersistentData().getUUID(LINKED_OWNER));
    }

    private static boolean placeInFormation(
            ServerLevel level,
            LivingEntity owner,
            Entity monolith,
            int formationIndex,
            int formationCount,
            boolean allowFallback) {
        double baseAngle = formationAngle(owner, formationIndex, formationCount);
        int attempts = allowFallback ? 12 : 1;
        for (int attempt = 0; attempt < attempts; attempt++) {
            double angle = baseAngle + attempt * Math.PI * 2.0D / 12.0D;
            double radius = FORMATION_RADIUS + (attempt % 3);
            int x = owner.blockPosition().getX() + (int) Math.round(Math.cos(angle) * radius);
            int z = owner.blockPosition().getZ() + (int) Math.round(Math.sin(angle) * radius);
            int startY = Math.min(level.getMaxBuildHeight() - 2, owner.blockPosition().getY() + 3);
            int endY = Math.max(level.getMinBuildHeight() + 1, owner.blockPosition().getY() - 96);
            for (int y = startY; y >= endY; y--) {
                BlockPos position = new BlockPos(x, y, z);
                if (!level.getBlockState(position.below()).isFaceSturdy(level, position.below(), Direction.UP)) {
                    continue;
                }
                monolith.moveTo(x + 0.5D, y, z + 0.5D, owner.getYRot(), 0.0F);
                if (level.noCollision(monolith)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static double formationAngle(LivingEntity owner, int index, int count) {
        int safeCount = Math.max(1, count);
        double phase = Math.floorMod(owner.getUUID().hashCode(), 360) * Math.PI / 180.0D;
        return phase + index * Math.PI * 2.0D / safeCount;
    }
}
