package com.eldren.wutao.ramesses_requiem;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** TEMPORARY: remove after Rift Execution playtesting is complete. */
@Mod.EventBusSubscriber(modid = RequiemAddon.MODID)
public final class CrimsonRequiemDebugCommands {
    private CrimsonRequiemDebugCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("requiem_reset")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    var player = context.getSource().getPlayerOrException();
                    CrimsonRequiemActions.resetExecutionCooldown(player);
                    context.getSource().sendSuccess(() -> Component.translatable(
                            "message.ramesses_curse.crimson_requiem.execution_reset"), false);
                    return 1;
                }));
    }
}
