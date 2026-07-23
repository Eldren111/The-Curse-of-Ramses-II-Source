package com.eldren.wutao.ramesses_curse;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = RamessesCurseMod.MODID)
public final class DuatPalaceTitleEvents {
    private static final ResourceLocation VISIT_PALACE_ADVANCEMENT =
            new ResourceLocation(RamessesCurseMod.MODID, "visit_duat_palace");
    private static final String PALACE_HINT_SHOWN = "RamessesCurseDuatPalaceHintShown";
    private static final int PALACE_HINT_DURATION = 180;

    private DuatPalaceTitleEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || event.player.level().isClientSide
                || event.player.tickCount % 10 != 0
                || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        boolean inside = ModStructures.isInsideDuatPalace(player.level(), player.blockPosition());
        if (!inside || !ModEvents.isSealEquippedForPalace(player)) {
            return;
        }

        awardVisitAdvancement(player);
        showPalaceHintOnce(player);
    }

    private static void showPalaceHintOnce(ServerPlayer player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(Player.PERSISTED_NBT_TAG)) {
            persistent.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }

        CompoundTag persisted = persistent.getCompound(Player.PERSISTED_NBT_TAG);
        if (persisted.getBoolean(PALACE_HINT_SHOWN)) {
            return;
        }
        persisted.putBoolean(PALACE_HINT_SHOWN, true);

        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new CurseSevenHintPacket(
                        "beneath the dust, the court of eternal silence opens before you; before the throne, only the victor shall earn a name",
                        "尘沙之下，永寂王庭已向你敞开。王座之前，唯有胜者得以留名。",
                        "gold_omen",
                        PALACE_HINT_DURATION));
    }

    private static void awardVisitAdvancement(ServerPlayer player) {
        Advancement advancement = player.server.getAdvancements().getAdvancement(VISIT_PALACE_ADVANCEMENT);
        if (advancement == null) {
            return;
        }

        PlayerAdvancements advancements = player.getAdvancements();
        AdvancementProgress progress = advancements.getOrStartProgress(advancement);
        for (String criterion : progress.getRemainingCriteria()) {
            advancements.award(advancement, criterion);
        }
    }

}
