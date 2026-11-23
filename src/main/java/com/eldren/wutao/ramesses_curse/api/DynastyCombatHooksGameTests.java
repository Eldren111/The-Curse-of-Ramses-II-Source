package com.eldren.wutao.ramesses_curse.api;

import com.eldren.wutao.ramesses_curse.RamessesCurseMod;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(RamessesCurseMod.MODID)
@PrefixGameTestTemplate(false)
public final class DynastyCombatHooksGameTests {
    @GameTest(template = "integration_test_empty")
    public static void ordinaryCombatKeepsDynastyRules(GameTestHelper helper) {
        helper.assertTrue(!DynastyCombatHooks.bypassesAffixes(helper.getLevel().damageSources().generic()),
                "Ordinary damage must not acquire an affix bypass");
        helper.assertTrue(!DynastyCombatHooks.bypassesSilence(Items.DIAMOND_SWORD.getDefaultInstance()),
                "Ordinary weapons must still obey silence");
        helper.assertTrue(!DynastyCombatHooks.preventsPurge(MobEffects.WITHER),
                "Vanilla wither must still be removable");
        helper.succeed();
    }
}
