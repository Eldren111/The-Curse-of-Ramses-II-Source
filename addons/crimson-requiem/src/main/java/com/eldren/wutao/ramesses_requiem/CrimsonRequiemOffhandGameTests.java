package com.eldren.wutao.ramesses_requiem;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(RequiemAddon.LEGACY_NAMESPACE)
@PrefixGameTestTemplate(false)
public final class CrimsonRequiemOffhandGameTests {
    private static ServerPlayer player(GameTestHelper helper) {
        var owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "requiem-offhand"));
        var pos = helper.absolutePos(new BlockPos(2, 2, 2));
        owner.setPos(pos.getX(), pos.getY(), pos.getZ());
        owner.setNoGravity(true);
        owner.setOnGround(true);
        var main = new ItemStack(Items.DIAMOND_SWORD);
        owner.setItemInHand(InteractionHand.MAIN_HAND, main);
        main.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE)
                .forEach(owner.getAttribute(Attributes.ATTACK_DAMAGE)::addTransientModifier);
        owner.setItemInHand(InteractionHand.OFF_HAND, RequiemItems.CRIMSON_REQUIEM.get().getDefaultInstance());
        return owner;
    }

    @GameTest(template = "crimson_test_empty")
    public static void companionAttackAndIndependentDamage(GameTestHelper helper) {
        var owner = player(helper);
        var main = owner.getMainHandItem();
        var offhand = owner.getOffhandItem();
        double mainDamage = owner.getAttributeValue(Attributes.ATTACK_DAMAGE);
        helper.assertTrue(Math.abs(CrimsonRequiemDamage.meleeAttackDamage(owner) - CrimsonRequiemConfig.ATTACK_DAMAGE.get()) < 0.01,
                "Offhand sword must use its own attack modifiers, not the main-hand weapon");
        var target = helper.spawn(EntityType.COW, new BlockPos(2, 2, 4));
        target.setNoAi(true);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);
        target.setHealth(1000);
        float strength = owner.getAttackStrengthScale(0.5F);
        float swordDamage = CrimsonRequiemDamage.meleeAttackDamage(owner) * (0.2F + strength * strength * 0.8F) + 10.5F * strength;
        float ordinaryDamage = (float)mainDamage * (0.2F + strength * strength * 0.8F);
        owner.attack(target);
        helper.assertTrue(Math.abs(target.getHealth() - (1000 - swordDamage - ordinaryDamage)) < 0.02,
                "Main strike and offhand sword must both land: actual=" + target.getHealth()
                        + " expected=" + (1000 - swordDamage - ordinaryDamage));
        var waves = helper.getLevel().getEntitiesOfClass(CrimsonRequiemProjectile.class, owner.getBoundingBox().inflate(5),
                blade -> blade.getOwner() == owner && blade.isWave());
        helper.assertTrue(waves.size() == 1 && waves.get(0).weapon().is(RequiemItems.CRIMSON_REQUIEM.get()),
                "One companion wave must use the offhand sword snapshot");
        helper.assertTrue(owner.getMainHandItem() == main && owner.getOffhandItem() == offhand
                && owner.getAttributeValue(Attributes.ATTACK_DAMAGE) == mainDamage, "Never swap or alter the main weapon's attributes");
        helper.assertTrue(!CrimsonRequiemDamage.bypassesAffixes(owner.damageSources().playerAttack(owner)),
                "Other main-hand weapon must not inherit Requiem damage exemptions");
        waves.forEach(CrimsonRequiemProjectile::discard);
        owner.discard();
        helper.succeed();
    }

    @GameTest(template = "crimson_test_empty")
    public static void offhandSkillsAndSlotReservation(GameTestHelper helper) {
        var owner = player(helper);
        var main = owner.getMainHandItem();
        var sword = owner.getOffhandItem();
        sword.enchant(ModEnchantments.SOUL_PURSUIT.get(), 1);
        sword.enchant(ModEnchantments.SWORD_STORM.get(), 1);
        sword.use(helper.getLevel(), owner, InteractionHand.OFF_HAND);
        var blades = helper.getLevel().getEntitiesOfClass(CrimsonRequiemProjectile.class, owner.getBoundingBox().inflate(5),
                blade -> blade.getOwner() == owner && !blade.isWave());
        helper.assertTrue(blades.size() == 1 && CrimsonRequiemFlight.locked(sword)
                && !CrimsonRequiemFlight.locked(main), "Throw must reserve only the offhand sword");
        var blade = blades.get(0);
        helper.assertTrue(blade.weapon().getEnchantmentLevel(ModEnchantments.SOUL_PURSUIT.get()) == 1,
                "Offhand pursuit enchantment must survive in the projectile");
        var storms = helper.getLevel().getEntitiesOfClass(CrimsonSwordStorm.class, owner.getBoundingBox().inflate(5),
                storm -> storm.getOwner() == owner);
        helper.assertTrue(storms.size() == 1, "Offhand throw must trigger its sword storm");
        var target = helper.spawn(EntityType.COW, new BlockPos(2, 2, 4));
        var attack = new net.minecraftforge.event.entity.player.AttackEntityEvent(owner, target);
        CrimsonRequiemEvents.lockedAttack(attack);
        helper.assertTrue(!attack.isCanceled(), "Offhand flight must not lock main-hand attacks");
        // Exercise the actual return path, not just the slot helper.
        var saved = new net.minecraft.nbt.CompoundTag();
        blade.addAdditionalSaveData(saved);
        saved.putBoolean("Returning", true);
        blade.readAdditionalSaveData(saved);
        blade.setOwner(owner);
        blade.setPos(owner.getEyePosition().add(0, -0.2, 0));
        blade.tick();
        helper.assertTrue(blade.isRemoved() && !CrimsonRequiemFlight.locked(sword)
                && owner.getOffhandItem() == sword && owner.getMainHandItem() == main,
                "Return must unlock the same offhand stack without moving either weapon");
        owner.setShiftKeyDown(true);
        var aim = target.getBoundingBox().getCenter().subtract(owner.getEyePosition());
        owner.setYRot((float)Math.toDegrees(Math.atan2(-aim.x, aim.z)));
        owner.setXRot((float)-Math.toDegrees(Math.atan2(aim.y, aim.horizontalDistance())));
        CrimsonRequiemActions.rightClick(owner);
        var executions = helper.getLevel().getEntitiesOfClass(CrimsonRiftExecution.class, owner.getBoundingBox().inflate(10),
                cast -> cast.getOwner() == owner);
        helper.assertTrue(executions.size() == 1, "Offhand Shift+right-click must start targeted execution");
        executions.forEach(CrimsonRiftExecution::discard);
        CrimsonRequiemActions.leftClick(owner);
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(CrimsonRequiemSkull.class, owner.getBoundingBox().inflate(5),
                skull -> skull.getOwner() == owner).size() == 1, "Offhand Shift+left-click must cast skull");
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(CrimsonRequiemFireball.class, owner.getBoundingBox().inflate(5),
                fireball -> fireball.getOwner() == owner).size() == 1, "Offhand Shift+left-click must cast fireball");
        owner.setItemInHand(InteractionHand.MAIN_HAND, RequiemItems.CRIMSON_REQUIEM.get().getDefaultInstance());
        helper.assertTrue(CrimsonRequiemActions.weaponHand(owner) == InteractionHand.MAIN_HAND, "Two Requiems must prioritize the main hand");
        storms.forEach(CrimsonSwordStorm::discard);
        owner.discard();
        helper.succeed();
    }
}
