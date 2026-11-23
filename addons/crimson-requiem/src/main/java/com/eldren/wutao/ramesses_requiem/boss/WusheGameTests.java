package com.eldren.wutao.ramesses_requiem.boss;

import com.eldren.wutao.ramesses_requiem.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder(RequiemAddon.LEGACY_NAMESPACE)
@PrefixGameTestTemplate(false)
public final class WusheGameTests {
    private static WusheBoss boss(GameTestHelper h) {
        WusheBoss boss = h.spawn(WusheContent.WUSHE.get(), new BlockPos(3, 2, 3));
        boss.setNoAi(true); boss.setNoGravity(true); return boss;
    }
    private static ServerPlayer player(ServerLevel level, Vec3 pos) {
        var player = new ServerPlayer(level.getServer(), level, new GameProfile(UUID.randomUUID(), "wushe-test"));
        player.connection = new net.minecraft.server.network.ServerGamePacketListenerImpl(level.getServer(),
                new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND), player) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet, net.minecraft.network.PacketSendListener listener) {}
        };
        player.setPos(pos); player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        // Real player damage pipeline, after the vanilla login protection expires.
        for (int i = 0; i < 61; i++) player.tick();
        level.addNewPlayer(player);
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000); player.setHealth(1000); return player;
    }
    @GameTest(template = "crimson_test_empty")
    public static void creativePlayersTriggerCombatAndControl(GameTestHelper h) {
        WusheBoss boss = boss(h);
        ServerPlayer target = player(h.getLevel(), boss.position().add(2, 0, 0));
        try {
            target.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
            boss.setNoAi(false);
            boss.tick();
            h.assertTrue(boss.getTarget() == target && boss.skill() == 1,
                    "Boss must acquire a creative player and initiate its ambush without being hit first");
            boss.finishSkill(); boss.configureTier(1);
            h.assertTrue(boss.startSkill(4, target), "Creative player can trigger the backstab sequence");
            WusheControl.freeze(target, boss);
            h.assertTrue(WusheControl.isFrozen(target), "Creative mode must not cancel cinematic control");
            float health = target.getHealth();
            target.hurt(boss.damageSources().mobAttack(boss), 12);
            h.assertTrue(target.getHealth() == health, "Attacking creative players must preserve vanilla creative damage immunity");
            target.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
            WusheControl.tick(new net.minecraftforge.event.TickEvent.PlayerTickEvent(net.minecraftforge.event.TickEvent.Phase.END, target));
            h.assertTrue(!WusheControl.isFrozen(target) && !WusheBoss.eligible(target),
                    "Spectators must be excluded and released immediately");
            boss.finishSkill(); boss.acquirePlayerTarget();
            h.assertTrue(boss.getTarget() != target, "Boss must release a target who switches to spectator");
        } finally {
            WusheControl.release(target, null); target.discard(); boss.discard();
        }
        h.succeed();
    }
    @GameTest(template = "crimson_test_empty")
    public static void dimensionHealthAndDefensivePriority(GameTestHelper h) {
        for (var dim : List.of(Level.OVERWORLD, Level.NETHER, Level.END)) {
            ServerLevel level = h.getLevel().getServer().getLevel(dim);
            WusheBoss boss = new WusheBoss(WusheContent.WUSHE.get(), level);
            float max = (float)WusheConfig.health(WusheBoss.tierFor(level));
            h.assertTrue(boss.getMaxHealth() == max && boss.getHealth() == max, "Full configured health in " + dim.location() + ": " + boss.getHealth() + "/" + boss.getMaxHealth());
            var blade = new CrimsonSwordStorm(RequiemEntities.CRIMSON_SWORD_STORM.get(), level);
            var source = new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(CrimsonRequiemDamage.TRUE), blade);
            boss.hurt(source, max);
            h.assertTrue(Math.abs(boss.getHealth() - (max - 100)) < .01, "Over-threshold final sword damage must be capped at 100");
            boss.invulnerableTime = 0;
            boss.hurt(source, 50); boss.hurt(boss.damageSources().fellOutOfWorld(), 999999);
            boss.setHealth(0); boss.die(source);
            h.assertTrue(boss.isAlive() && boss.getHealth() == max - 100, "Protection must beat bypass tags, void damage and direct execution");
            CompoundTag tag = new CompoundTag(); boss.saveWithoutId(tag);
            var restored = new WusheBoss(WusheContent.WUSHE.get(), level); restored.load(tag);
            h.assertTrue(restored.getHealth() == boss.getHealth() && restored.protectionActive(), "Save/load must retain current health and protection");
            boss.discard(); restored.discard();
        }
        h.succeed();
    }
    @GameTest(template = "crimson_test_empty", timeoutTicks = 35)
    public static void protectionExpiresAfterHalfSecond(GameTestHelper h) {
        WusheBoss boss = boss(h);
        boss.hurt(boss.damageSources().generic(), 1000);
        float after = boss.getHealth();
        h.runAtTickTime(8, () -> {
            boss.invulnerableTime = 0; boss.hurt(boss.damageSources().generic(), 10);
            h.assertTrue(boss.getHealth() == after, "All damage is blocked inside the 10-tick protection");
        });
        h.runAtTickTime(12, () -> {
            boss.invulnerableTime = 0; boss.hurt(boss.damageSources().generic(), 10);
            h.assertTrue(boss.getHealth() < after, "Normal damage works after protection expires");
            boss.discard(); h.succeed();
        });
    }
    @GameTest(template = "crimson_test_empty")
    public static void backstabTrueDamageAndShardDamage(GameTestHelper h) {
        WusheBoss boss = boss(h);
        ServerPlayer target = player(h.getLevel(), boss.position().add(2, 0, 0));
        target.setAbsorptionAmount(80);
        target.setItemSlot(EquipmentSlot.CHEST, Items.NETHERITE_CHESTPLATE.getDefaultInstance());
        target.invulnerableTime = 20;
        target.hurt(WusheDamage.backstab(boss), 50);
        h.assertTrue(Math.abs(target.getHealth() - 950) < .01 && target.getAbsorptionAmount() == 80,
                "Backstab must deal exactly 50 HP through armor, absorption and hurt frames: " + target.getHealth());
        target.setAbsorptionAmount(0); target.invulnerableTime = 0;
        WusheWave.launch(boss, target.getBoundingBox().getCenter(), new Vec3(.1, 0, 0), 4);
        var shards = h.getLevel().getEntitiesOfClass(WusheWave.class, target.getBoundingBox().inflate(2));
        h.assertTrue(!shards.isEmpty(), "Shard must be registered and spawned");
        shards.forEach(Entity::tick);
        h.assertTrue(Math.abs(target.getHealth() - 945) < .01, "Each ordinary burst shard deals 5 magic damage to players: health=" + target.getHealth() + " shards=" + shards.size() + " difficulty=" + h.getLevel().getDifficulty());
        target.discard(); boss.discard(); h.succeed();
    }
    @GameTest(template = "crimson_test_empty")
    public static void bladeDanceTimingAndShapes(GameTestHelper h) {
        WusheBoss boss = boss(h);
        var cow = h.spawn(EntityType.COW, new BlockPos(8, 2, 3)); cow.setNoAi(true); cow.setNoGravity(true);
        h.assertTrue(boss.startSkill(3, cow), "Overworld must have skill three");
        for (int tick = 1; tick <= 599; tick++) boss.tick();
        h.assertTrue(boss.skill() == 3, "Dance must still run before 30 seconds");
        boss.tick();
        h.assertTrue(boss.skill() == 0, "Dance ends at 600 ticks");
        CompoundTag save = new CompoundTag(); boss.saveWithoutId(save);
        h.assertTrue(save.getLongArray("WusheCooldowns")[3] == h.getLevel().getGameTime() + 600, "Dance cooldown starts after completion");
        Vec3 forward = new Vec3(0, 0, 1);
        h.assertTrue(WusheWave.stroke(0, forward).size() == 21 && WusheWave.stroke(2, forward).size() == 42, "Horizontal and X blades have distinct collision strokes");
        h.assertTrue(!boss.causeFallDamage(100, 1, boss.damageSources().fall()), "Boss is immune to falling");
        h.getLevel().getEntitiesOfClass(WusheWave.class, boss.getBoundingBox().inflate(60)).forEach(Entity::discard);
        cow.discard(); boss.discard(); h.succeed();
    }
    @GameTest(template = "crimson_test_empty")
    public static void ambushSweepAndBackstabSequence(GameTestHelper h) {
        for (int x = 0; x <= 12; x++) for (int z = 0; z <= 12; z++) h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
        WusheBoss boss = boss(h);
        ServerPlayer target = player(h.getLevel(), boss.position().add(2, 0, 0));
        h.assertTrue(boss.startSkill(1, target), "First skill starts against a player");
        for (int i = 0; i < 16; i++) boss.tick();
        h.assertTrue(target.getHealth() < 1000 && boss.getY() > target.getY() + 2, "Ambush hits first, then teleports above the player");
        for (int i = 16; i < 42; i++) boss.tick();
        h.assertTrue(boss.skill() == 0 && !boss.isNoGravity(), "Ambush finishes and restores gravity");
        boss.teleportTo(target.getX() - 2, target.getY(), target.getZ());
        h.assertTrue(boss.startSkill(2, target), "Second skill starts in melee range");
        for (int i = 0; i < 40; i++) boss.tick();
        h.assertTrue(boss.skill() == 0, "Kick and sweep complete before another skill starts");
        boss.configureTier(1);
        h.assertTrue(boss.startSkill(4, target), "Nether tier unlocks the backstab");
        for (int i = 0; i < 59; i++) boss.tick();
        h.assertTrue(!WusheControl.isFrozen(target), "Player may move during the first three seconds");
        boss.tick();
        h.assertTrue(WusheControl.isFrozen(target), "At three seconds movement and jumping are server-locked");
        Vec3 anchor = target.position();
        target.connection.handleMovePlayer(new net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos(anchor.x + 10, anchor.y + 2, anchor.z, false));
        h.assertTrue(target.position().distanceToSqr(anchor) < .01, "Frozen movement packets cannot move the player");
        float before = target.getHealth();
        for (int i = 60; i < 70; i++) boss.tick();
        h.assertTrue(Math.abs(target.getHealth() - (before - 50)) < .01, "Backstab sequence delivers the configured true damage");
        h.assertTrue(!WusheControl.isFrozen(target), "Control releases after impact");
        var shards = h.getLevel().getEntitiesOfClass(WusheWave.class, target.getBoundingBox().inflate(3), e -> e.getOwner() == boss && e.shape() == 4);
        h.assertTrue(shards.size() == 24, "Backstab spawns 24 damaging burst fragments");
        shards.forEach(Entity::discard); target.discard(); boss.finishSkill(); boss.discard(); h.succeed();
    }
    @GameTest(template = "crimson_test_empty")
    public static void controlAndArenaRecoverWhenBossDisappears(GameTestHelper h) {
        WusheBoss boss = boss(h); boss.configureTier(1);
        ServerPlayer target = player(h.getLevel(), boss.position().add(2, 0, 0));
        h.assertTrue(boss.startSkill(4, target), "Backstab begins");
        WusheControl.freeze(target, boss);
        h.assertTrue(WusheControl.isFrozen(target), "Owner's active cast holds the player");
        WusheBoss other = boss(h); other.configureTier(1);
        h.assertTrue(!other.startSkill(4, target), "Second boss cannot take over the locked player");
        boss.discard();
        WusheControl.tick(new net.minecraftforge.event.TickEvent.PlayerTickEvent(net.minecraftforge.event.TickEvent.Phase.END, target));
        h.assertTrue(!WusheControl.isFrozen(target), "Missing boss clears the lock");
        Vec3 origin = target.position();
        h.assertTrue(AstralArena.enter(other, target), "Recovery test enters arena");
        var tag = target.getPersistentData().getCompound(AstralArena.RETURN);
        Entity moved = target.serverLevel().getEntity(tag.getUUID("Boss"));
        moved.discard();
        AstralArena.tickPlayer(target);
        h.assertTrue(target.level() == h.getLevel() && target.position().distanceToSqr(origin) < 1, "Missing arena boss returns the player immediately");
        h.assertTrue(!target.getPersistentData().contains(AstralArena.RETURN), "Return clears persisted recovery data");
        target.discard(); h.succeed();
    }
    @GameTest(template = "crimson_test_empty")
    public static void astralDuelReturnsPlayerAndBoss(GameTestHelper h) {
        WusheBoss boss = boss(h);
        ServerPlayer target = player(h.getLevel(), boss.position().add(2, 0, 0));
        Vec3 origin = target.position(); java.util.UUID uuid = boss.getUUID();
        h.assertTrue(h.getLevel().getServer().getLevel(AstralArena.DIMENSION) != null, "Independent astral dimension must load without Bosses Unleashed");
        h.assertTrue(!boss.startSkill(5, target), "Overworld cannot select the fifth skill");
        boss.configureTier(1);
        h.assertTrue(boss.startSkill(5, target), "Fifth skill starts after the Nether tier unlock");
        for (int tick = 0; tick < 34; tick++) boss.tick();
        h.assertTrue(AstralArena.isArena(target.level()), "Player enters the arena dimension");
        CompoundTag data = target.getPersistentData().getCompound(AstralArena.RETURN);
        h.assertTrue(data.getLong("Deadline") - h.getLevel().getServer().overworld().getGameTime() == 3600, "Duel duration is 180 seconds");
        ServerLevel arena = target.serverLevel();
        WusheBoss moved = (WusheBoss)arena.getEntity(uuid);
        h.assertTrue(moved != null && moved.getHealth() == 666 && moved.tier() == 0, "Transfer keeps boss health and original dimension tier");
        var drops = new ArrayList<net.minecraft.world.entity.item.ItemEntity>();
        drops.add(new net.minecraft.world.entity.item.ItemEntity(arena, target.getX(), target.getY(), target.getZ(), Items.DIAMOND.getDefaultInstance()));
        AstralArena.returnDeathDrops(target, drops);
        h.assertTrue(drops.isEmpty() && !h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                new AABB(origin.subtract(2, 2, 2), origin.add(2, 2, 2)), item -> item.getItem().is(Items.DIAMOND)).isEmpty(), "Death drops return to original world");
        data.putLong("Deadline", 0); AstralArena.tickPlayer(target);
        h.assertTrue(target.level() == h.getLevel() && target.position().distanceToSqr(origin) < 1 && !target.getPersistentData().contains(AstralArena.RETURN), "Timeout restores player origin and clears session data");
        AstralArena.tickBoss(moved);
        h.assertTrue(h.getLevel().getEntity(uuid) instanceof WusheBoss, "Boss returns when duel ends");
        h.getLevel().getEntity(uuid).discard(); target.discard(); h.succeed();
    }
}
