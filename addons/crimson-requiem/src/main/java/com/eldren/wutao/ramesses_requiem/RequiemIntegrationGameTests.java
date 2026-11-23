package com.eldren.wutao.ramesses_requiem;

import com.eldren.wutao.ramesses_curse.api.DynastyCombatHooks;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

@GameTestHolder(RequiemAddon.LEGACY_NAMESPACE)
@PrefixGameTestTemplate(false)
public final class RequiemIntegrationGameTests {
    @GameTest(template = "crimson_test_empty")
    public static void legacyIdsAndOptionalHooksRemainConnected(GameTestHelper helper) {
        var sword = RequiemItems.CRIMSON_REQUIEM.get().getDefaultInstance();
        helper.assertTrue(new ResourceLocation("ramesses_curse", "crimson_requiem")
                        .equals(ForgeRegistries.ITEMS.getKey(sword.getItem())),
                "Existing sword stacks must keep their registry ID");
        helper.assertTrue(DynastyCombatHooks.bypassesSilence(sword), "Addon sword must bypass dynasty silence");
        helper.assertTrue(DynastyCombatHooks.preventsPurge(RequiemEffects.CRIMSON_DECAY.get()),
                "Addon decay must keep its purge protection");
        var key = ResourceKey.create(Registries.DAMAGE_TYPE,
                new ResourceLocation("ramesses_curse", "crimson_requiem_true"));
        var projectile = new CrimsonRequiemProjectile(RequiemEntities.CRIMSON_REQUIEM_PROJECTILE.get(), helper.getLevel());
        var source = new DamageSource(helper.getLevel().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(key), projectile);
        helper.assertTrue(DynastyCombatHooks.bypassesAffixes(source), "Addon damage must bypass dynasty affixes");
        helper.assertTrue(EnchantmentHelper.getEnchantments(ModEnchantments.pursuitBook())
                .containsKey(ModEnchantments.SOUL_PURSUIT.get()), "Pursuit book must use the registered enchantment");
        helper.assertTrue(EnchantmentHelper.getEnchantments(ModEnchantments.stormBook())
                .containsKey(ModEnchantments.SWORD_STORM.get()), "Storm book must use the registered enchantment");
        for (var stack : java.util.List.of(sword, ModEnchantments.pursuitBook(), ModEnchantments.stormBook())) {
            helper.assertTrue(RequiemAddon.MODID.equals(stack.getItem().getCreatorModId(stack)),
                    "Addon item tooltips must identify Requiem despite legacy registry IDs");
        }
        var vanillaBook = net.minecraft.world.item.EnchantedBookItem.createForEnchantment(
                new net.minecraft.world.item.enchantment.EnchantmentInstance(
                        net.minecraft.world.item.enchantment.Enchantments.SHARPNESS, 1));
        helper.assertTrue("minecraft".equals(vanillaBook.getItem().getCreatorModId(vanillaBook)),
                "Vanilla enchanted books must keep their original attribution");
        helper.assertTrue(RequiemAddon.REQUIEM_TAB.getId().equals(new ResourceLocation("ramesses_requiem", "requiem"))
                && RequiemAddon.REQUIEM_TAB.get().getIconItem().is(sword.getItem()),
                "Addon must have its own registered creative tab with a sword icon");
        helper.succeed();
    }
}
