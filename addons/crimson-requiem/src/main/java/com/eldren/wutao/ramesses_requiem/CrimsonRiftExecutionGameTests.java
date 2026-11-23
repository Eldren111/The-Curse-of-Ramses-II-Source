package com.eldren.wutao.ramesses_requiem;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(RequiemAddon.LEGACY_NAMESPACE)
@PrefixGameTestTemplate(false)
public final class CrimsonRiftExecutionGameTests {
    private static ServerPlayer owner(GameTestHelper helper) {
        var owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "rift-test"));
        var origin = helper.absolutePos(new BlockPos(2, 2, 2));
        owner.setPos(origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5);
        owner.setItemInHand(InteractionHand.MAIN_HAND, RequiemItems.CRIMSON_REQUIEM.get().getDefaultInstance());
        owner.setNoGravity(true);
        owner.setShiftKeyDown(true);
        return owner;
    }

    private static void aim(ServerPlayer owner, Vec3 point) {
        Vec3 direction = point.subtract(owner.getEyePosition());
        owner.setYRot((float)Math.toDegrees(Math.atan2(-direction.x, direction.z)));
        owner.setXRot((float)-Math.toDegrees(Math.atan2(direction.y, direction.horizontalDistance())));
    }

    @GameTest(template = "crimson_test_empty")
    public static void lockRequiresSightAndSuccessfulCastConsumesCooldown(GameTestHelper helper) {
        var owner = owner(helper);
        owner.setXRot(-90);
        CrimsonRequiemActions.rightClick(owner); // Miss must not consume cooldown.
        var target = helper.spawn(EntityType.PIG, new BlockPos(2, 2, 7));
        target.setNoAi(true);
        aim(owner, target.getBoundingBox().getCenter());
        helper.assertTrue(CrimsonRiftExecution.aimedTarget(owner) == target, "Crosshair must select the aimed entity");
        helper.setBlock(new BlockPos(2, 2, 4), Blocks.STONE);
        helper.setBlock(new BlockPos(2, 3, 4), Blocks.STONE);
        helper.assertTrue(CrimsonRiftExecution.aimedTarget(owner) == null, "Cannot target through walls");
        helper.setBlock(new BlockPos(2, 2, 4), Blocks.AIR);
        helper.setBlock(new BlockPos(2, 3, 4), Blocks.AIR);
        CrimsonRequiemActions.rightClick(owner);
        var casts = helper.getLevel().getEntitiesOfClass(CrimsonRiftExecution.class, target.getBoundingBox().inflate(4),
                cast -> cast.getOwner() == owner);
        helper.assertTrue(casts.size() == 1, "A missed cast must not consume the successful cast cooldown");
        helper.assertTrue(!CrimsonRiftExecution.start(owner), "Cannot stack two executions on the same owner/target");
        casts.get(0).discard();
        CrimsonRequiemActions.rightClick(owner);
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(CrimsonRiftExecution.class, target.getBoundingBox().inflate(4),
                cast -> cast.getOwner() == owner && !cast.isRemoved()).isEmpty(), "Successful cast must consume cooldown");
        helper.assertTrue(!owner.hasEffect(RequiemEffects.CRIMSON_WARD.get()), "The old absorption skill must not be granted");
        owner.discard();
        helper.succeed();
    }

    @GameTest(template = "crimson_test_empty")
    public static void acceleratedSequenceExecutesOnceAndPreservesLoot(GameTestHelper helper) {
        var owner = owner(helper);
        var target = helper.spawn(EntityType.PIG, new BlockPos(2, 2, 7));
        target.setNoAi(true); target.setNoGravity(true);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);
        target.setHealth(1000);
        target.setAbsorptionAmount(200);
        String rewardToken = UUID.randomUUID().toString();
        var reward = new ItemStack(Items.EMERALD);
        reward.getOrCreateTag().putString("RiftTest", rewardToken);
        target.setItemSlot(EquipmentSlot.MAINHAND, reward);
        target.setDropChance(EquipmentSlot.MAINHAND, 2F);
        var nearby = helper.spawn(EntityType.COW, new BlockPos(3, 2, 7));
        nearby.setNoAi(true); nearby.setNoGravity(true);
        nearby.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);
        nearby.setHealth(1000);
        var item = new ItemEntity(helper.getLevel(), target.getX(), target.getY(), target.getZ(), new ItemStack(Items.DIAMOND));
        helper.getLevel().addFreshEntity(item);
        helper.setBlock(new BlockPos(3, 2, 8), Blocks.STONE);
        var oldItems = helper.getLevel().getEntitiesOfClass(ItemEntity.class, target.getBoundingBox().inflate(4)).stream()
                .map(ItemEntity::getUUID).collect(java.util.stream.Collectors.toSet());
        var oldXp = helper.getLevel().getEntitiesOfClass(ExperienceOrb.class, target.getBoundingBox().inflate(4)).stream()
                .map(ExperienceOrb::getUUID).collect(java.util.stream.Collectors.toSet());
        aim(owner, target.getBoundingBox().getCenter());
        helper.assertTrue(CrimsonRiftExecution.start(owner), "Aimed cast should start");
        var cast = helper.getLevel().getEntitiesOfClass(CrimsonRiftExecution.class, target.getBoundingBox().inflate(3),
                entity -> entity.getOwner() == owner).get(0);
        helper.assertTrue(CrimsonRiftExecution.flightTicks(0) > CrimsonRiftExecution.flightTicks(12)
                && CrimsonRiftExecution.launchTick(1) - CrimsonRiftExecution.launchTick(0)
                    > CrimsonRiftExecution.launchTick(23) - CrimsonRiftExecution.launchTick(22), "Blades must accelerate and arrive more frequently");
        for (int tick = 1; tick < CrimsonRiftExecution.FINAL_TICK; tick++) {
            cast.tick();
            if (tick == CrimsonRiftExecution.impactTick(0)) {
                float expected = 1000 - CrimsonRequiemDamage.boomerangTrueDamage(owner)
                        - CrimsonRequiemDamage.boomerangMagicDamage(owner) - CrimsonRequiemConfig.EXECUTION_BLAST_DAMAGE.get().floatValue();
                helper.assertTrue(Math.abs(target.getHealth() - expected) < 0.01, "First blade must damage, with its small blast: " + target.getHealth());
            }
            if (tick == 100) {
                CompoundTag saved = new CompoundTag();
                cast.addAdditionalSaveData(saved);
                var restored = new CrimsonRiftExecution(RequiemEntities.CRIMSON_RIFT_EXECUTION.get(), helper.getLevel());
                restored.readAdditionalSaveData(saved);
                restored.setOwner(owner);
                restored.setPos(cast.position());
                cast.discard();
                helper.getLevel().addFreshEntity(restored);
                cast = restored;
            }
        }
        helper.assertTrue(target.isAlive() && target.getHealth() == 1, "Staged attacks must leave one health until the giant lands");
        helper.assertTrue(target.getAbsorptionAmount() == 200 && nearby.getHealth() < 1000,
                "Blades bypass absorption and small explosions damage nearby enemies");
        target.setInvulnerable(true);
        target.setHealth(1000); // Finisher must still execute a healed, invulnerable target.
        cast.tick();
        helper.assertTrue(target.isDeadOrDying() && !target.isRemoved(), "Finale must invoke death, not discard the target");
        var drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, target.getBoundingBox().inflate(4),
                drop -> !oldItems.contains(drop.getUUID()));
        long emeralds = drops.stream().filter(drop -> drop.getItem().is(Items.EMERALD)).mapToInt(drop -> drop.getItem().getCount()).sum();
        helper.assertTrue(emeralds == 1 && drops.stream().anyMatch(drop -> drop.getItem().is(Items.PORKCHOP)),
                "Execution must keep normal equipment and loot-table drops, without storm reward doubling: "
                        + drops.stream().map(drop -> drop.getItem().toString()).toList());
        helper.assertTrue(!helper.getLevel().getEntitiesOfClass(ExperienceOrb.class, target.getBoundingBox().inflate(4),
                orb -> !oldXp.contains(orb.getUUID())).isEmpty(),
                "Execution must preserve player kill XP credit");
        helper.assertTrue(item.isAlive() && helper.getBlockState(new BlockPos(3, 2, 8)).is(Blocks.STONE)
                && owner.getHealth() == owner.getMaxHealth(), "Blasts must preserve items, blocks and caster");
        CompoundTag completed = new CompoundTag();
        cast.addAdditionalSaveData(completed);
        cast.readAdditionalSaveData(completed);
        cast.setOwner(owner);
        for (int i = 0; i < 16; i++) cast.tick();
        long after = helper.getLevel().getEntitiesOfClass(ItemEntity.class, target.getBoundingBox().inflate(4)).stream()
                .filter(drop -> drop.getItem().is(Items.EMERALD) && drop.getItem().hasTag()
                        && rewardToken.equals(drop.getItem().getTag().getString("RiftTest")))
                .mapToInt(drop -> drop.getItem().getCount()).sum();
        helper.assertTrue(after == 1 && cast.isRemoved(), "Saved finished cast must not duplicate drops and must expire");
        owner.discard();
        helper.succeed();
    }
}
