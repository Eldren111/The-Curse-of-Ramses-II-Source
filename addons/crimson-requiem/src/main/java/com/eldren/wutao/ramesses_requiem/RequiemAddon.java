package com.eldren.wutao.ramesses_requiem;

import com.eldren.wutao.ramesses_curse.api.DynastyCombatHooks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod(RequiemAddon.MODID)
public final class RequiemAddon {
    public static final String MODID = "ramesses_requiem";
    /** Preserve old item/entity/enchantment/resource IDs when moving existing worlds to the addon. */
    public static final String LEGACY_NAMESPACE = "ramesses_curse";
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final RegistryObject<CreativeModeTab> REQUIEM_TAB = TABS.register("requiem", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.ramesses_requiem"))
            .icon(() -> RequiemItems.CRIMSON_REQUIEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(RequiemItems.CRIMSON_REQUIEM.get().getDefaultInstance());
                output.accept(ModEnchantments.pursuitBook());
                output.accept(ModEnchantments.stormBook());
                output.accept(com.eldren.wutao.ramesses_requiem.boss.WusheContent.EGG.get());
            }).build());

    public RequiemAddon() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        com.eldren.wutao.ramesses_requiem.boss.WusheContent.register(bus);
        RequiemItems.ITEMS.register(bus);
        RequiemEntities.ENTITY_TYPES.register(bus);
        RequiemEffects.EFFECTS.register(bus);
        ModEnchantments.ENCHANTMENTS.register(bus);
        TABS.register(bus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, CrimsonRequiemConfig.SPEC,
                "ramesses_curse-crimson-requiem-server.toml");
        RequiemNetwork.register();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, com.eldren.wutao.ramesses_requiem.boss.WusheConfig.SPEC,
                "ramesses_requiem-wushe-server.toml");
        DynastyCombatHooks.register(com.eldren.wutao.ramesses_requiem.boss.WusheDamage::trueDamage, stack -> false, effect -> false);
        DynastyCombatHooks.register(CrimsonRequiemDamage::bypassesAffixes,
                stack -> stack.is(RequiemItems.CRIMSON_REQUIEM.get()), effect -> effect == RequiemEffects.CRIMSON_DECAY.get());
    }
}
