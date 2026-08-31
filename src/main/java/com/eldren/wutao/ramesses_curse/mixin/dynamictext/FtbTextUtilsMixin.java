package com.eldren.wutao.ramesses_curse.mixin.dynamictext;

import com.eldren.wutao.ramesses_curse.dynamictext.text.DynamicTextParser;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 精确兼容 FTB Quests 2001.4.20 的标题、描述、副标题和奖励文本解析入口。 */
@Pseudo
@Mixin(targets = "dev.ftb.mods.ftbquests.util.TextUtils", remap = false)
public abstract class FtbTextUtilsMixin {
    @ModifyVariable(method = "parseRawText", at = @At("HEAD"), argsOnly = true, require = 0)
    private static String dynamicTextEffects$prepareRawText(String rawText) {
        return DynamicTextParser.prepareForFtb(rawText);
    }

    @Inject(method = "parseRawText", at = @At("RETURN"), cancellable = true, require = 0)
    private static void dynamicTextEffects$parseResult(
            String rawText,
            CallbackInfoReturnable<Component> callback
    ) {
        Component result = callback.getReturnValue();
        if (result != null && DynamicTextParser.containsCodes(result.getString())) {
            callback.setReturnValue(DynamicTextParser.parse(result));
        }
    }
}
