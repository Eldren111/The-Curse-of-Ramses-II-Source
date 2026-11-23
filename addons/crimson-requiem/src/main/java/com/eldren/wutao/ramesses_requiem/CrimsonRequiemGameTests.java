package com.eldren.wutao.ramesses_requiem;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(RequiemAddon.LEGACY_NAMESPACE)
@PrefixGameTestTemplate(false)
public final class CrimsonRequiemGameTests {
    @GameTest(template = "crimson_test_empty")
    public static void flightUsesCurrentCasterStats(GameTestHelper helper) {
        var owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "requiem-stats"));
        var origin = helper.absolutePos(new BlockPos(2, 2, 2));
        owner.setPos(origin.getX(), origin.getY(), origin.getZ());
        owner.setItemInHand(InteractionHand.MAIN_HAND, RequiemItems.CRIMSON_REQUIEM.get().getDefaultInstance());
        owner.setHealth(20);
        owner.getAttribute(Attributes.LUCK).setBaseValue(0);
        var blade = CrimsonRequiemProjectile.create(owner, false);
        CrimsonRequiemFlight.begin(owner.getMainHandItem(), blade.getUUID(), helper.getLevel());
        // A single throw must read changed stats at each contact, rather than snapshotting at launch.
        float base = CrimsonRequiemConfig.TRUE_DAMAGE.get().floatValue() + CrimsonRequiemConfig.MAGIC_DAMAGE.get().floatValue();
        for (int pass = 0; pass < 2; pass++) {
            owner.setHealth(pass == 0 ? 12.5F : 6F);
            owner.getAttribute(Attributes.LUCK).setBaseValue(pass == 0 ? 2.5D : 4D);
            var target = helper.spawn(EntityType.COW, new BlockPos(5 + pass * 4, 2, 5));
            target.setNoAi(true);
            target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);
            target.setHealth(1000);
            target.setAbsorptionAmount(200);
            target.invulnerableTime = 20;
            blade.setPos(target.getBoundingBox().getCenter());
            blade.setDeltaMovement(new Vec3(0.1, 0, 0));
            blade.tick();
            float expected = base + (pass == 0 ? 50F : 38F);
            helper.assertTrue(Math.abs(target.getHealth() - (1000F - expected)) < 0.01F,
                    "Both damage packets must use live health and luck on contact " + pass + ": health=" + target.getHealth());
            helper.assertTrue(target.getAbsorptionAmount() == 200, "Scaled damage must still bypass absorption");
        }
        CrimsonRequiemFlight.clear(owner.getMainHandItem());
        blade.discard();
        owner.discard();
        helper.succeed();
    }
}
