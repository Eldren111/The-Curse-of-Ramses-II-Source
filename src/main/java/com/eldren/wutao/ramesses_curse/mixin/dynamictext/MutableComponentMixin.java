package com.eldren.wutao.ramesses_curse.mixin.dynamictext;

import com.eldren.wutao.ramesses_curse.dynamictext.text.DynamicTextParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 兜底处理动态文本入口；原有私用区碑文字形必须保持原字体，不做字符转码。 */
@Mixin(MutableComponent.class)
public abstract class MutableComponentMixin {
    @Inject(method = "getVisualOrderText", at = @At("HEAD"), cancellable = true)
    private void dynamicTextEffects$parseBeforeVisualOrder(
            CallbackInfoReturnable<FormattedCharSequence> callback
    ) {
        Component self = (Component) (Object) this;
        boolean dynamic = DynamicTextParser.needsParsing(self);
        if (dynamic) {
            callback.setReturnValue(DynamicTextParser.parse(self).getVisualOrderText());
        }
    }
}
