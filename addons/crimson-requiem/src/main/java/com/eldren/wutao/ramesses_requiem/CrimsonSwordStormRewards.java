package com.eldren.wutao.ramesses_requiem;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RequiemAddon.MODID)
public final class CrimsonSwordStormRewards {
    private static final String MARK = "CrimsonSwordStormFinalBlow";
    private static final String DROPS = "CrimsonSwordStormDropsPaid";
    private static final String XP = "CrimsonSwordStormXpPaid";
    private CrimsonSwordStormRewards() {}

    private static boolean isFinalSwordHit(net.minecraft.world.damagesource.DamageSource source) {
        return source.getDirectEntity() instanceof CrimsonSwordStorm
                && (source.is(CrimsonRequiemDamage.TRUE) || source.is(CrimsonRequiemDamage.MAGIC)
                    || source.is(CrimsonSwordStorm.DAMAGE));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void mark(LivingDeathEvent event) {
        // XP has no DamageSource, so record this death's source, never an earlier nonlethal hit.
        var data = event.getEntity().getPersistentData();
        data.putBoolean(MARK, isFinalSwordHit(event.getSource()));
        data.remove(DROPS);
        data.remove(XP);
    }

    private static boolean claim(LivingEntity entity, String reward) {
        var data = entity.getPersistentData();
        if (!data.getBoolean(MARK) || data.getBoolean(reward)) return false;
        data.putBoolean(reward, true);
        return true;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void drops(LivingDropsEvent event) {
        if (!isFinalSwordHit(event.getSource())) return;
        if (!claim(event.getEntity(), DROPS)) return;
        // Copy final stacks (including NBT and Looting) without overflowing their stack limits.
        var extra = new java.util.ArrayList<ItemEntity>();
        for (ItemEntity original : event.getDrops()) {
            if (original.getItem().isEmpty()) continue;
            ItemEntity copy = new ItemEntity(original.level(), original.getX(), original.getY(), original.getZ(), original.getItem().copy());
            copy.setDeltaMovement(original.getDeltaMovement());
            copy.setDefaultPickUpDelay();
            extra.add(copy);
        }
        event.getDrops().addAll(extra);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void experience(LivingExperienceDropEvent event) {
        if (claim(event.getEntity(), XP)) {
            event.setDroppedExperience((int)Math.min(Integer.MAX_VALUE, Math.max(0L, event.getDroppedExperience()) * 2));
        }
    }
}
