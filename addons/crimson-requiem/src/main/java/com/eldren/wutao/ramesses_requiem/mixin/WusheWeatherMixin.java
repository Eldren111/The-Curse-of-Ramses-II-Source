package com.eldren.wutao.ramesses_requiem.mixin;

import com.eldren.wutao.ramesses_requiem.boss.WusheWeather;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class WusheWeatherMixin {
    @Inject(method = {"renderSnowAndRain", "tickRain"}, at = @At("HEAD"), cancellable = true)
    private void requiem$replaceLocalWeather(CallbackInfo callback) {
        if (WusheWeather.active()) callback.cancel();
    }
}
