package com.eldren.wutao.ramesses_requiem;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RequiemAddon.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class RequiemClientSetup {
    @SubscribeEvent public static void sky(net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent event) {
        event.register(com.eldren.wutao.ramesses_requiem.boss.AstralArena.EFFECTS, new com.eldren.wutao.ramesses_requiem.boss.AstralSkyEffects());
    }
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(com.eldren.wutao.ramesses_requiem.boss.WusheContent.WUSHE.get(), com.eldren.wutao.ramesses_requiem.boss.WusheRenderer::new);
        event.registerEntityRenderer(com.eldren.wutao.ramesses_requiem.boss.WusheContent.WAVE.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);
        event.registerEntityRenderer(RequiemEntities.CRIMSON_REQUIEM_PROJECTILE.get(), CrimsonRequiemRenderer::new);
        event.registerEntityRenderer(RequiemEntities.CRIMSON_SWORD_STORM.get(), CrimsonSwordStormRenderer::new);
        event.registerEntityRenderer(RequiemEntities.CRIMSON_RIFT_EXECUTION.get(), CrimsonRiftExecutionRenderer::new);
        event.registerEntityRenderer(RequiemEntities.CRIMSON_REQUIEM_SKULL.get(), net.minecraft.client.renderer.entity.WitherSkullRenderer::new);
        event.registerEntityRenderer(RequiemEntities.CRIMSON_REQUIEM_FIREBALL.get(),
                context -> new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(context, 3F, true));
    }
}
