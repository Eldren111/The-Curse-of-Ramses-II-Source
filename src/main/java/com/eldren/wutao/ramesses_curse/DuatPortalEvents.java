package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.Optional;

@EventBusSubscriber(modid = RamessesCurseMod.MODID)
public final class DuatPortalEvents {
    private DuatPortalEvents() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getItemStack().is(Items.FLINT_AND_STEEL)) {
            return;
        }

        BlockPos ignitionPos = event.getPos().relative(event.getFace());
        Optional<DuatPortalShape> shape = DuatPortalShape.findEmpty(event.getLevel(), ignitionPos);
        if (shape.isEmpty()) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        if (event.getLevel().isClientSide) {
            return;
        }

        shape.get().createPortalBlocks();
        event.getLevel().playSound(
                null,
                ignitionPos,
                SoundEvents.FLINTANDSTEEL_USE,
                SoundSource.BLOCKS,
                1.0F,
                0.8F + event.getLevel().random.nextFloat() * 0.4F);
        event.getEntity().awardStat(Stats.ITEM_USED.get(Items.FLINT_AND_STEEL));
        event.getItemStack().hurtAndBreak(1, event.getEntity(),
                event.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND
                        ? net.minecraft.world.entity.EquipmentSlot.MAINHAND
                        : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
    }
}

