package com.eldren.wutao.ramesses_requiem;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(RequiemAddon.LEGACY_NAMESPACE)
@PrefixGameTestTemplate(false)
public final class CrimsonSwordStormGameTests {
    @GameTest(template = "crimson_test_empty")
    public static void strikesUseLiveHealthAndLuck(GameTestHelper helper) {
        var owner = net.minecraftforge.common.util.FakePlayerFactory.get(helper.getLevel(),
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "storm-scaling"));
        var origin = helper.absolutePos(new BlockPos(2, 2, 2));
        owner.setPos(origin.getX(), origin.getY(), origin.getZ());
        owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(999);
        for (int pass = 0; pass < 2; pass++) {
            var target = helper.spawn(EntityType.COW, new BlockPos(5 + pass * 4, 2, 5));
            target.setNoAi(true);
            target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);
            target.setHealth(1000);
            target.setAbsorptionAmount(200);
            target.invulnerableTime = 20;
            var sword = new CrimsonSwordStorm(RequiemEntities.CRIMSON_SWORD_STORM.get(), helper.getLevel());
            var saved = new net.minecraft.nbt.CompoundTag();
            saved.putInt("Mode", 1);
            saved.putUUID("Target", target.getUUID());
            saved.putFloat("AttackDamage", 999); // Old saves must not restore the obsolete damage snapshot.
            sword.readAdditionalSaveData(saved);
            sword.setOwner(owner);
            sword.setPos(target.getBoundingBox().getCenter());
            owner.setHealth(pass == 0 ? 12.5F : 6F);
            owner.getAttribute(Attributes.LUCK).setBaseValue(pass == 0 ? 2.5D : 4D);
            sword.tick();
            float expected = CrimsonRequiemConfig.TRUE_DAMAGE.get().floatValue()
                    + CrimsonRequiemConfig.MAGIC_DAMAGE.get().floatValue() + (pass == 0 ? 50F : 38F);
            helper.assertTrue(Math.abs(target.getHealth() - (1000 - expected)) < 0.01F,
                    "Storm contact must use current health and luck, not attack panel: " + target.getHealth());
            helper.assertTrue(target.getAbsorptionAmount() == 200, "Both components must bypass absorption");
            helper.assertTrue(sword.isRemoved(), "Blade must disappear immediately on contact");
        }
        owner.discard();
        helper.succeed();
    }

    @GameTest(template = "crimson_test_empty", timeoutTicks = 260)
    public static void sixVolleysAndScalingDamage(GameTestHelper helper) {
        var owner = net.minecraftforge.common.util.FakePlayerFactory.get(helper.getLevel(),
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "storm-test"));
        var origin = helper.absolutePos(new BlockPos(5, 2, 5));
        owner.setPos(origin.getX(), origin.getY(), origin.getZ());
        owner.setNoGravity(true);
        var stack = RequiemItems.CRIMSON_REQUIEM.get().getDefaultInstance();
        stack.enchant(ModEnchantments.SWORD_STORM.get(), 1);
        stack.enchant(ModEnchantments.SOUL_PURSUIT.get(), 1);
        owner.setItemInHand(InteractionHand.MAIN_HAND, stack);
        // Deliberately unrelated to the blade formula.
        owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(2);
        var target = helper.spawn(EntityType.IRON_GOLEM, new BlockPos(5, 2, 12));
        target.setNoAi(true); target.setNoGravity(true);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);
        target.setHealth(1000);
        target.setAbsorptionAmount(200);
        float[] damageTaken = {0};
        // Restore health each tick so all six volleys can hit one target despite the larger damage.
        for (int tick = 4; tick < 250; tick++) helper.runAtTickTime(tick, () -> {
            damageTaken[0] += 1000 - target.getHealth();
            target.setHealth(1000);
        });
        helper.runAtTickTime(3, () -> {
            float damage = CrimsonRequiemDamage.boomerangTrueDamage(owner) + CrimsonRequiemDamage.boomerangMagicDamage(owner);
            CrimsonSwordStorm.start(owner);
            var storms = helper.getLevel().getEntitiesOfClass(CrimsonSwordStorm.class, owner.getBoundingBox().inflate(30));
            helper.assertTrue(storms.size() == 1 && storms.get(0).remaining() == 6, "Cast must create exactly six orbit swords");
            CrimsonSwordStorm.start(owner);
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(CrimsonSwordStorm.class, owner.getBoundingBox().inflate(30),
                    CrimsonSwordStorm::isController).size() == 1, "Cannot stack active controllers");
            helper.runAtTickTime(36, () -> {
                helper.assertTrue(storms.get(0).remaining() == 5, "First lock consumes exactly one sword: remaining="
                        + storms.get(0).remaining() + " age=" + storms.get(0).tickCount + " removed=" + storms.get(0).isRemoved()
                        + " distance=" + owner.distanceTo(target));
                helper.assertTrue(damageTaken[0] > 0, "Rain must damage its target");
                helper.assertTrue(target.getAbsorptionAmount() == 200, "Rain must bypass absorption without consuming it");
                float hits = (damageTaken[0] + 1000 - target.getHealth()) / damage;
                helper.assertTrue(Math.abs(hits - Math.round(hits)) < 0.01 && hits >= 1 && hits <= 15,
                        "Each first-volley sword deals both scaled damage components, independent of armor and hurt frames");
                target.setPos(target.position().add(3, 0, 0));
            });
            helper.runAtTickTime(250, () -> {
                helper.assertTrue(storms.get(0).isRemoved() && storms.get(0).remaining() == 0, "Six volleys exhaust the controller");
                helper.assertTrue(target.isAlive() && damageTaken[0] >= damage * 6, "A single surviving target must receive all six volleys");
                helper.assertTrue(helper.getLevel().getEntitiesOfClass(CrimsonSwordStorm.class, target.getBoundingBox().inflate(20),
                        sword -> !sword.isController()).isEmpty(), "All queued blades must finish and disappear");
                helper.assertTrue(owner.getHealth() == owner.getMaxHealth(), "Caster must not be hit");
                owner.discard();
                helper.succeed();
            });
        });
    }

    @GameTest(template = "crimson_test_empty")
    public static void rewardsAndExclusiveEnchantment(GameTestHelper helper) {
        var storm = ModEnchantments.SWORD_STORM.get();
        helper.assertTrue(storm.canEnchant(RequiemItems.CRIMSON_REQUIEM.get().getDefaultInstance()), "Must apply to Requiem");
        helper.assertTrue(!storm.canEnchant(new ItemStack(Items.NETHERITE_SWORD)), "Must reject other swords");
        helper.assertTrue(storm.isCompatibleWith(ModEnchantments.SOUL_PURSUIT.get()), "Must combine with Soul Pursuit");
        for (var damageType : java.util.List.of(CrimsonRequiemDamage.TRUE, CrimsonRequiemDamage.MAGIC)) {
            var target = helper.spawn(EntityType.COW, new BlockPos(2, 2, 2));
            var blade = new CrimsonSwordStorm(RequiemEntities.CRIMSON_SWORD_STORM.get(), helper.getLevel());
            var source = new net.minecraft.world.damagesource.DamageSource(helper.getLevel().registryAccess()
                    .registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(damageType), blade);
            helper.assertTrue(source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_COOLDOWN), "Damage must bypass hurt frames");
            helper.assertTrue(CrimsonRequiemDamage.bypassesAffixes(source), "Damage must bypass dynamic affix reduction");
            CrimsonSwordStormRewards.mark(new net.minecraftforge.event.entity.living.LivingDeathEvent(target, source));
            var stack = new ItemStack(Items.DIAMOND, 3);
            stack.getOrCreateTag().putString("RewardTest", "preserved");
            var drops = new java.util.ArrayList<ItemEntity>();
            drops.add(new ItemEntity(helper.getLevel(), target.getX(), target.getY(), target.getZ(), stack));
            var event = new net.minecraftforge.event.entity.living.LivingDropsEvent(target, source, drops, 10, true);
            CrimsonSwordStormRewards.drops(event);
            CrimsonSwordStormRewards.drops(event);
            helper.assertTrue(drops.size() == 2 && drops.get(1).getItem().getCount() == 3
                    && "preserved".equals(drops.get(1).getItem().getTag().getString("RewardTest")), "Drops double once and preserve NBT");
            var xp = new net.minecraftforge.event.entity.living.LivingExperienceDropEvent(target, null, 7);
            CrimsonSwordStormRewards.experience(xp);
            CrimsonSwordStormRewards.experience(xp);
            helper.assertTrue(xp.getDroppedExperience() == 14, "XP doubles exactly once");
            // A later death to any other source must invalidate even an older eligible death/revival.
            var ordinary = target.damageSources().generic();
            CrimsonSwordStormRewards.mark(new net.minecraftforge.event.entity.living.LivingDeathEvent(target, ordinary));
            var normalDrops = new java.util.ArrayList<ItemEntity>();
            normalDrops.add(new ItemEntity(helper.getLevel(), target.getX(), target.getY(), target.getZ(), new ItemStack(Items.DIAMOND, 3)));
            CrimsonSwordStormRewards.drops(new net.minecraftforge.event.entity.living.LivingDropsEvent(target, ordinary, normalDrops, 0, true));
            var normalXp = new net.minecraftforge.event.entity.living.LivingExperienceDropEvent(target, null, 7);
            CrimsonSwordStormRewards.experience(normalXp);
            helper.assertTrue(normalDrops.size() == 1 && normalXp.getDroppedExperience() == 7, "Other final blows must never double rewards");
        }
        helper.succeed();
    }
}
