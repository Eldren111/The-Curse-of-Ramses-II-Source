package com.eldren.wutao.ramesses_curse.mixin.dynamictext;

import com.eldren.wutao.ramesses_curse.dynamictext.text.EffectStyle;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * ModernUI 3.12.x 会根据 Style.font 选择字体集合。
 * 动态效果仍保留在样式中用于逐字渲染，但查找字体资源时必须还原真实字体 ID。
 */
@Pseudo
@Mixin(targets = "icyllis.modernui.mc.text.TextLayoutEngine", remap = false, priority = 2)
public abstract class ModernUiTextLayoutEngineMixin {
    @ModifyVariable(method = "getFontCollection", at = @At("HEAD"), argsOnly = true, require = 0)
    private ResourceLocation dynamicTextEffects$decodeFont(ResourceLocation fontName) {
        return EffectStyle.baseFont(fontName);
    }
}
