package com.eldren.wutao.ramesses_curse.mixin.dynamictext;

import com.eldren.wutao.ramesses_curse.dynamictext.text.DynamicTextParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 兜底处理所有 Component 绘制入口，包括 FTB Library 的 TextField。 */
@Mixin(MutableComponent.class)
public abstract class MutableComponentMixin {
    @Inject(method = "getVisualOrderText", at = @At("HEAD"), cancellable = true)
    private void dynamicTextEffects$parseBeforeVisualOrder(
            CallbackInfoReturnable<FormattedCharSequence> callback
    ) {
        Component self = (Component) (Object) this;
        if (DynamicTextParser.needsParsing(self)) {
            callback.setReturnValue(DynamicTextParser.parse(self).getVisualOrderText());
        }
    }
}
