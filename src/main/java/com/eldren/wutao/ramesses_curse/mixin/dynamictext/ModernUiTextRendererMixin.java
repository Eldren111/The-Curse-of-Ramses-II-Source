package com.eldren.wutao.ramesses_curse.mixin.dynamictext;

import com.eldren.wutao.ramesses_curse.dynamictext.client.DynamicTextRuntime;
import com.eldren.wutao.ramesses_curse.dynamictext.client.ModernUiDynamicTextRenderer;
import com.eldren.wutao.ramesses_curse.dynamictext.text.DynamicTextParser;
import com.eldren.wutao.ramesses_curse.dynamictext.text.EffectStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.renderer.MultiBufferSource;

/** Routes ModernUI text calls containing effect styles through the vanilla-compatible renderer. */
@Pseudo
@Mixin(targets = "icyllis.modernui.mc.text.ModernTextRenderer", remap = false, priority = 2)
public abstract class ModernUiTextRendererMixin {
    @Inject(
            method = "drawText(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)F",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void ramessesCurse$drawString(
            String text, float x, float y, int color, boolean shadow, Matrix4f pose,
            MultiBufferSource buffers, Font.DisplayMode mode, int backgroundColor, int packedLight,
            CallbackInfoReturnable<Float> callback
    ) {
        if (!DynamicTextRuntime.isEffectRendererActive() && DynamicTextParser.containsCodes(text)) {
            callback.setReturnValue(ramessesCurse$draw(DynamicTextParser.parse(text).getVisualOrderText(),
                    x, y, color, shadow, pose, buffers, mode, backgroundColor, packedLight));
        }
    }

    @Inject(
            method = "drawText(Lnet/minecraft/network/chat/FormattedText;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)F",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void ramessesCurse$drawFormattedText(
            FormattedText text, float x, float y, int color, boolean shadow, Matrix4f pose,
            MultiBufferSource buffers, Font.DisplayMode mode, int backgroundColor, int packedLight,
            CallbackInfoReturnable<Float> callback
    ) {
        if (!DynamicTextRuntime.isEffectRendererActive() && DynamicTextParser.needsParsing(text)) {
            Component parsed = DynamicTextParser.parse(text);
            if (EffectStyle.hasEffects(parsed)) {
                callback.setReturnValue(ramessesCurse$draw(parsed.getVisualOrderText(),
                        x, y, color, shadow, pose, buffers, mode, backgroundColor, packedLight));
            }
        }
    }

    @Inject(
            method = "drawText(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)F",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void ramessesCurse$drawSequence(
            FormattedCharSequence text, float x, float y, int color, boolean shadow, Matrix4f pose,
            MultiBufferSource buffers, Font.DisplayMode mode, int backgroundColor, int packedLight,
            CallbackInfoReturnable<Float> callback
    ) {
        if (!DynamicTextRuntime.isEffectRendererActive() && EffectStyle.hasEffects(text)) {
            callback.setReturnValue(ramessesCurse$draw(text,
                    x, y, color, shadow, pose, buffers, mode, backgroundColor, packedLight));
        }
    }

    private static float ramessesCurse$draw(
            FormattedCharSequence text, float x, float y, int color, boolean shadow, Matrix4f pose,
            MultiBufferSource buffers, Font.DisplayMode mode, int backgroundColor, int packedLight
    ) {
        DynamicTextRuntime.beginEffectRender();
        try {
            return ModernUiDynamicTextRenderer.draw(
                    Minecraft.getInstance().font, text, x, y, color, shadow, pose, buffers,
                    mode, backgroundColor, packedLight
            );
        } finally {
            DynamicTextRuntime.endEffectRender();
        }
    }
}
