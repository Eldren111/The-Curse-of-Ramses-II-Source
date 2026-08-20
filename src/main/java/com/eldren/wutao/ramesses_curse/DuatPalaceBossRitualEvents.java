package com.eldren.wutao.ramesses_curse;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = RamessesCurseMod.MODID)
public final class DuatPalaceBossRitualEvents {
    private DuatPalaceBossRitualEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickSkull(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        BlockPos pos = event.getPos();
        if (!level.getBlockState(pos).is(Blocks.SKELETON_SKULL)
                || !ModStructures.isNebukaSummoningSkull(level, pos)) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.getEntity().swing(event.getHand(), true);

        AABB ritualArea = new AABB(pos).inflate(2.0D);
        boolean ritualActive = !level.getEntitiesOfClass(
                NebukaSummoningRitualEntity.class,
                ritualArea,
                ritual -> !ritual.isRemoved()).isEmpty();
        if (ritualActive) {
            return;
        }

        AABB palaceArea = new AABB(pos).inflate(
                DuatPalaceStructure.PALACE_SIZE_X,
                DuatPalaceStructure.PALACE_SIZE_Y,
                DuatPalaceStructure.PALACE_SIZE_Z);
        boolean bossAlive = !level.getEntitiesOfClass(
                NebukaGuardianEntity.class,
                palaceArea,
                boss -> boss.isAlive() && !boss.isRemoved()).isEmpty();
        if (bossAlive) {
            return;
        }

        NebukaSummoningRitualEntity.spawn(level, pos);
    }
}

