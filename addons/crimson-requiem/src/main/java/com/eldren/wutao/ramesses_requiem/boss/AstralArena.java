package com.eldren.wutao.ramesses_requiem.boss;

import com.eldren.wutao.ramesses_requiem.RequiemAddon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.util.ITeleporter;
import java.util.function.Function;

/** A real isolated dimension; only the sky is cosmetic. Return data survives saves/reconnects. */
public final class AstralArena {
    public static final ResourceLocation EFFECTS = new ResourceLocation(RequiemAddon.MODID, "astral_prison");
    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION, EFFECTS);
    public static final String RETURN = "WusheAstralReturn";
    private static final String CENTER_X = "CenterX", CENTER_Z = "CenterZ";
    public static boolean isArena(Level level) { return level.dimension().equals(DIMENSION); }
    public static CompoundTag origin(Entity entity) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Dimension", entity.level().dimension().location().toString());
        tag.putDouble("X", entity.getX()); tag.putDouble("Y", entity.getY()); tag.putDouble("Z", entity.getZ());
        tag.putFloat("Yaw", entity.getYRot()); tag.putFloat("Pitch", entity.getXRot());
        return tag;
    }
    public static boolean enter(WusheBoss boss, ServerPlayer player) {
        if (boss.getServer() == null || isArena(boss.level()) || !player.isAlive() || player.getPersistentData().contains(RETURN)) return false;
        ServerLevel arena = boss.getServer().getLevel(DIMENSION);
        if (arena == null) return false;
        Allocation slots = arena.getDataStorage().computeIfAbsent(Allocation::load, Allocation::new, "wushe_arenas");
        int index = slots.allocate();
        int x = (index % 128) * 160, z = (index / 128) * 160;
        // Compact arena floor and low retaining edge. Never touch blocks in the original world.
        for (int dx = -18; dx <= 18; dx++) for (int dz = -18; dz <= 18; dz++) {
            BlockPos floor = new BlockPos(x + dx, 64, z + dz);
            arena.setBlock(floor, (Math.abs(dx) == 18 || Math.abs(dz) == 18 ? Blocks.CRYING_OBSIDIAN : Blocks.POLISHED_BLACKSTONE).defaultBlockState(), 18);
            if (Math.abs(dx) == 18 || Math.abs(dz) == 18) arena.setBlock(floor.above(), Blocks.OBSIDIAN.defaultBlockState(), 18);
        }
        long deadline = boss.getServer().overworld().getGameTime() + WusheConfig.ARENA_DURATION.get() * 20L;
        CompoundTag bossTag = origin(boss), playerTag = origin(player);
        for (CompoundTag tag : new CompoundTag[]{bossTag, playerTag}) {
            tag.putUUID("Boss", boss.getUUID()); tag.putLong("Deadline", deadline); tag.putInt(CENTER_X, x); tag.putInt(CENTER_Z, z);
        }
        boss.getPersistentData().put(RETURN, bossTag);
        WusheControl.release(player, boss.getUUID());
        Entity transferred = boss.changeDimension(arena, teleporter(new Vec3(x, 65, z + 5), boss.getYRot(), 0));
        if (!(transferred instanceof WusheBoss moved)) { boss.getPersistentData().remove(RETURN); return false; }
        player.getPersistentData().put(RETURN, playerTag);
        player.stopRiding();
        player.teleportTo(arena, x, 65, z - 5, player.getYRot(), 0);
        player.fallDistance = 0; player.setDeltaMovement(Vec3.ZERO);
        moved.setTarget(player);
        return true;
    }
    public static void tickPlayer(ServerPlayer player) {
        if (!player.getPersistentData().contains(RETURN)) {
            if (isArena(player.level()) && !player.isSpectator() && !player.isCreative()) {
                ServerLevel overworld = player.server.overworld(); BlockPos spawn = overworld.getSharedSpawnPos();
                player.teleportTo(overworld, spawn.getX() + .5, spawn.getY() + 1, spawn.getZ() + .5, 0, 0);
            }
            return;
        }
        if (!player.isAlive()) return; // Clone/respawn restores the return tag and then exits.
        CompoundTag tag = player.getPersistentData().getCompound(RETURN);
        ServerLevel arena = player.server.getLevel(DIMENSION);
        Entity boss = arena == null || !tag.hasUUID("Boss") ? null : arena.getEntity(tag.getUUID("Boss"));
        // Vanilla drops XP near the end of the death animation. Wait for it before collecting arena rewards.
        if (isArena(player.level()) && boss instanceof WusheBoss dying && !dying.isAlive() && !dying.isRemoved() && dying.deathTime < 20) return;
        if (!isArena(player.level()) || player.server.overworld().getGameTime() >= tag.getLong("Deadline") || boss == null || !boss.isAlive()) {
            returnPlayer(player); return;
        }
        int x = tag.getInt(CENTER_X), z = tag.getInt(CENTER_Z);
        if (Math.abs(player.getX() - x) > 17.5 || Math.abs(player.getZ() - z) > 17.5 || player.getY() < 64 || player.getY() > 100) {
            player.teleportTo(x, 65, z - 5); player.fallDistance = 0; player.setDeltaMovement(Vec3.ZERO);
        }
    }
    public static boolean tickBoss(WusheBoss boss) {
        if (!isArena(boss.level()) || !boss.getPersistentData().contains(RETURN)) return false;
        CompoundTag tag = boss.getPersistentData().getCompound(RETURN);
        boolean participant = boss.getServer().getPlayerList().getPlayers().stream().anyMatch(p -> p.isAlive() && isArena(p.level())
                && p.getPersistentData().getCompound(RETURN).hasUUID("Boss") && p.getPersistentData().getCompound(RETURN).getUUID("Boss").equals(boss.getUUID()));
        if (!participant || boss.getServer().overworld().getGameTime() >= tag.getLong("Deadline")) {
            for (ServerPlayer player : boss.getServer().getPlayerList().getPlayers()) {
                CompoundTag returnTag = player.getPersistentData().getCompound(RETURN);
                if (returnTag.hasUUID("Boss") && returnTag.getUUID("Boss").equals(boss.getUUID())) returnPlayer(player);
            }
            boss.finishSkill();
            ServerLevel origin = destination(boss.getServer(), tag);
            boss.getPersistentData().remove(RETURN);
            boss.changeDimension(origin, teleporter(safeReturn(origin, boss, tag), tag.getFloat("Yaw"), tag.getFloat("Pitch")));
            return true;
        }
        int x = tag.getInt(CENTER_X), z = tag.getInt(CENTER_Z);
        if (Math.abs(boss.getX() - x) > 17 || Math.abs(boss.getZ() - z) > 17 || boss.getY() < 64) boss.teleportTo(x, 65, z + 5);
        return false;
    }
    public static void returnPlayer(ServerPlayer player) {
        if (!player.getPersistentData().contains(RETURN) || !player.isAlive()) return;
        CompoundTag tag = player.getPersistentData().getCompound(RETURN).copy();
        ServerLevel destination = destination(player.server, tag);
        Vec3 point = safeReturn(destination, player, tag);
        ServerLevel arena = player.server.getLevel(DIMENSION);
        if (arena != null) {
            // Bring arena drops/XP back so a completed or interrupted duel doesn't strand rewards.
            var box = new AABB(tag.getInt(CENTER_X) - 20, 50, tag.getInt(CENTER_Z) - 20, tag.getInt(CENTER_X) + 20, 100, tag.getInt(CENTER_Z) + 20);
            for (Entity loot : arena.getEntities((Entity)null, box, e -> e instanceof ItemEntity || e instanceof ExperienceOrb))
                loot.changeDimension(destination, teleporter(point, 0, 0));
        }
        WusheControl.release(player, null);
        player.stopRiding();
        player.teleportTo(destination, point.x, point.y, point.z, tag.getFloat("Yaw"), tag.getFloat("Pitch"));
        player.setDeltaMovement(Vec3.ZERO); player.fallDistance = 0;
        player.getPersistentData().remove(RETURN);
        WusheScenePacket.send(player, new WusheScenePacket(2, -1, 0, Vec3.ZERO));
    }
    public static void returnDeathDrops(ServerPlayer player, java.util.Collection<ItemEntity> drops) {
        if (!isArena(player.level()) || !player.getPersistentData().contains(RETURN)) return;
        CompoundTag tag = player.getPersistentData().getCompound(RETURN);
        ServerLevel destination = destination(player.server, tag);
        Vec3 point = safeReturn(destination, player, tag);
        // LivingDropsEvent is before the original item entities are added to the arena.
        for (ItemEntity drop : drops) {
            ItemEntity moved = new ItemEntity(destination, point.x, point.y, point.z, drop.getItem().copy());
            moved.setDefaultPickUpDelay(); destination.addFreshEntity(moved);
        }
        drops.clear();
    }
    private static ServerLevel destination(net.minecraft.server.MinecraftServer server, CompoundTag tag) {
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("Dimension"));
        ServerLevel result = id == null ? null : server.getLevel(ResourceKey.create(Registries.DIMENSION, id));
        return result == null || isArena(result) ? server.overworld() : result;
    }
    private static Vec3 safeReturn(ServerLevel destination, Entity entity, CompoundTag tag) {
        Vec3 point = new Vec3(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z"));
        destination.getChunk(BlockPos.containing(point));
        for (int i = 0; i < 16; i++) {
            Vec3 candidate = point.add(0, i, 0);
            AABB box = entity.getBoundingBox().move(candidate.subtract(entity.position()));
            if (candidate.y < destination.getMaxBuildHeight() - 3 && destination.getWorldBorder().isWithinBounds(BlockPos.containing(candidate))
                    && destination.noCollision(entity, box) && !destination.containsAnyLiquid(box)) return candidate;
        }
        BlockPos spawn = destination.getSharedSpawnPos();
        return new Vec3(spawn.getX() + .5, destination.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn.getX(), spawn.getZ()) + 1, spawn.getZ() + .5);
    }
    private static ITeleporter teleporter(Vec3 position, float yaw, float pitch) {
        return new ITeleporter() {
            @Override public PortalInfo getPortalInfo(Entity entity, ServerLevel level, Function<ServerLevel, PortalInfo> defaults) { return new PortalInfo(position, Vec3.ZERO, yaw, pitch); }
            @Override public Entity placeEntity(Entity entity, ServerLevel from, ServerLevel to, float rotation, Function<Boolean, Entity> reposition) { return reposition.apply(false); }
        };
    }
    public static final class Allocation extends SavedData {
        private int next;
        public static Allocation load(CompoundTag tag) { Allocation data = new Allocation(); data.next = Math.max(0, tag.getInt("Next")); return data; }
        public int allocate() { int slot = next++; setDirty(); return slot; }
        @Override public CompoundTag save(CompoundTag tag) { tag.putInt("Next", next); return tag; }
    }
    private AstralArena() {}
}
