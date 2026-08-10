package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.UUID;

public final class DragonBreathBowRitualEvents {
    private static final String ACTIVE = "RamessesDragonBreathBowRitualActive";
    private static final String BREATH_UUID = "RamessesDragonBreathBowRitualBreathUuid";
    private static final String STARTED_AT = "RamessesDragonBreathBowRitualStartedAt";
    private static final String OWNER_UUID = "RamessesDragonBreathBowRitualOwner";
    private static final String CRYSTAL_OWNER = "RamessesDragonBreathBowRitualCrystalOwner";
    private static final int RITUAL_TICKS = 30 * 20;
    private static final AABB MAIN_ISLAND = new AABB(-192.0D, -64.0D, -192.0D,
            192.0D, 128.0D, 192.0D);

    private DragonBreathBowRitualEvents() {
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        MinecraftServer server = event.getServer();
        for (ServerLevel level : server.getAllLevels()) {
            if (level.dimension() == Level.END && canStart(level)) {
                tickEnd(level);
            }
        }
    }

    private static void tickEnd(ServerLevel level) {
        List<ItemEntity> active = level.getEntitiesOfClass(ItemEntity.class, MAIN_ISLAND,
                entity -> entity.isAlive() && entity.getPersistentData().getBoolean(ACTIVE));
        if (!active.isEmpty()) {
            for (ItemEntity bow : active) {
                tickActive(level, bow);
            }
            return;
        }

        List<ItemEntity> bows = level.getEntitiesOfClass(ItemEntity.class, MAIN_ISLAND,
                entity -> entity.isAlive() && entity.tickCount > 10
                        && entity.getItem().is(ModItems.ETERNAL_SILENCE_COMPOUND_BOW.get())
                        && !entity.getPersistentData().getBoolean(ACTIVE));
        for (ItemEntity bow : bows) {
            ItemEntity breath = findOffering(level, bow, Items.DRAGON_BREATH);
            if (breath != null) {
                start(level, bow, breath);
                return;
            }
        }
    }

    private static boolean canStart(ServerLevel level) {
        EndDragonFight dragonFight = level.getDragonFight();
        return dragonFight != null && dragonFight.hasPreviouslyKilledDragon()
                && !getMainIslandCrystals(level).isEmpty();
    }

    private static void start(ServerLevel level, ItemEntity bow, ItemEntity breath) {
        CompoundTag data = bow.getPersistentData();
        data.putBoolean(ACTIVE, true);
        data.putUUID(BREATH_UUID, breath.getUUID());
        data.putLong(STARTED_AT, level.getGameTime());
        UUID owner = nearestPlayer(level, bow.position());
        if (owner != null) {
            data.putUUID(OWNER_UUID, owner);
        }
        bindItem(breath, bow.getUUID());
        prepareItem(bow);
        prepareItem(breath);
        updateCrystalBeams(level, bow);
        level.playSound(null, bow.blockPosition(), SoundEvents.BEACON_ACTIVATE,
                SoundSource.BLOCKS, 0.8F, 1.2F);
        sendMessage(level, data, "message.ramesses_curse.dragon_breath_bow.ritual_started");
    }

    private static void tickActive(ServerLevel level, ItemEntity bow) {
        CompoundTag data = bow.getPersistentData();
        ItemEntity breath = getBoundItem(level, data, BREATH_UUID, Items.DRAGON_BREATH);
        if (breath == null || !bow.isAlive() || !canStart(level)) {
            cancel(level, bow, breath);
            return;
        }

        prepareItem(bow);
        prepareItem(breath);
        updateCrystalBeams(level, bow);
        if (level.getGameTime() % 5L == 0L) {
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.DRAGON_BREATH,
                    bow.getX(), bow.getY() + 0.3D, bow.getZ(), 8,
                    0.35D, 0.35D, 0.35D, 0.01D);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                    bow.getX(), bow.getY() + 0.3D, bow.getZ(), 2,
                    0.18D, 0.18D, 0.18D, 0.01D);
        }
        if (level.getGameTime() - data.getLong(STARTED_AT) >= RITUAL_TICKS) {
            complete(level, bow, breath);
        }
    }

    private static void complete(ServerLevel level, ItemEntity bow, ItemEntity breath) {
        CompoundTag data = bow.getPersistentData().copy();
        clearCrystalBeams(level, bow.getUUID());
        ItemStack infused = ModItems.DRAGON_BREATH_COMPOUND_BOW.get().getDefaultInstance();
        infused.setDamageValue(Math.min(bow.getItem().getDamageValue(), infused.getMaxDamage() - 1));
        ItemEntity result = new ItemEntity(level, bow.getX(), bow.getY(), bow.getZ(), infused);
        result.setDefaultPickUpDelay();
        level.addFreshEntity(result);
        bow.discard();
        breath.discard();
        level.playSound(null, result.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL,
                SoundSource.BLOCKS, 0.7F, 1.4F);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.DRAGON_BREATH,
                result.getX(), result.getY() + 0.4D, result.getZ(), 40,
                0.8D, 0.6D, 0.8D, 0.03D);
        sendMessage(level, data, "message.ramesses_curse.dragon_breath_bow.ritual_complete");
    }

    private static void cancel(ServerLevel level, ItemEntity bow, ItemEntity breath) {
        if (bow != null) {
            clearCrystalBeams(level, bow.getUUID());
        }
        restoreItem(bow);
        restoreItem(breath);
        if (bow != null) {
            bow.getPersistentData().remove(ACTIVE);
            bow.getPersistentData().remove(BREATH_UUID);
            bow.getPersistentData().remove(STARTED_AT);
            bow.getPersistentData().remove(OWNER_UUID);
        }
    }

    private static ItemEntity findOffering(ServerLevel level, ItemEntity bow, net.minecraft.world.item.Item item) {
        AABB area = new AABB(bow.blockPosition()).inflate(2.5D);
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area,
                entity -> entity.isAlive() && entity.tickCount > 10 && entity.getItem().is(item)
                        && !entity.getPersistentData().getBoolean(ACTIVE)
                        && !entity.getPersistentData().hasUUID(OWNER_UUID));
        return items.isEmpty() ? null : items.get(0);
    }

    private static ItemEntity getBoundItem(ServerLevel level, CompoundTag data, String key,
                                           net.minecraft.world.item.Item item) {
        if (!data.hasUUID(key)) {
            return null;
        }
        Entity entity = level.getEntity(data.getUUID(key));
        return entity instanceof ItemEntity itemEntity && itemEntity.isAlive()
                && itemEntity.getItem().is(item) ? itemEntity : null;
    }

    private static void bindItem(ItemEntity entity, UUID bowUuid) {
        entity.getPersistentData().putUUID(OWNER_UUID, bowUuid);
    }

    private static void prepareItem(ItemEntity entity) {
        entity.setNoGravity(true);
        entity.setDeltaMovement(Vec3.ZERO);
        entity.setPickUpDelay(32767);
        entity.setUnlimitedLifetime();
        entity.setGlowingTag(true);
    }

    private static void restoreItem(ItemEntity entity) {
        if (entity == null || !entity.isAlive()) {
            return;
        }
        entity.setNoGravity(false);
        entity.setDeltaMovement(Vec3.ZERO);
        entity.setPickUpDelay(20);
        entity.setGlowingTag(false);
        entity.getPersistentData().remove(OWNER_UUID);
    }

    private static UUID nearestPlayer(ServerLevel level, Vec3 position) {
        Player player = level.getNearestPlayer(position.x, position.y, position.z, 16.0D, false);
        return player instanceof ServerPlayer serverPlayer ? serverPlayer.getUUID() : null;
    }

    private static void sendMessage(ServerLevel level, CompoundTag data, String key) {
        if (!data.hasUUID(OWNER_UUID)) {
            return;
        }
        Entity owner = level.getServer().getPlayerList().getPlayer(data.getUUID(OWNER_UUID));
        if (owner instanceof ServerPlayer player) {
            player.displayClientMessage(Component.translatable(key), true);
        }
    }

    private static List<EndCrystal> getMainIslandCrystals(ServerLevel level) {
        return level.getEntitiesOfClass(EndCrystal.class, MAIN_ISLAND, Entity::isAlive);
    }

    private static void updateCrystalBeams(ServerLevel level, ItemEntity bow) {
        for (EndCrystal crystal : getMainIslandCrystals(level)) {
            CompoundTag data = crystal.getPersistentData();
            if (crystal.getBeamTarget() == null
                    || data.hasUUID(CRYSTAL_OWNER) && bow.getUUID().equals(data.getUUID(CRYSTAL_OWNER))) {
                data.putUUID(CRYSTAL_OWNER, bow.getUUID());
                crystal.setBeamTarget(BlockPos.containing(bow.position()));
            }
        }
    }

    private static void clearCrystalBeams(ServerLevel level, UUID bowUuid) {
        for (EndCrystal crystal : getMainIslandCrystals(level)) {
            CompoundTag data = crystal.getPersistentData();
            if (data.hasUUID(CRYSTAL_OWNER) && bowUuid.equals(data.getUUID(CRYSTAL_OWNER))) {
                crystal.setBeamTarget(null);
                data.remove(CRYSTAL_OWNER);
            }
        }
    }
}
