package com.eldren.wutao.ramesses_curse;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class InscriptionFragmentEntity extends ItemFrame {
    private boolean fragmentDropped;

    public InscriptionFragmentEntity(EntityType<? extends ItemFrame> entityType, Level level) {
        super(entityType, level);
    }

    public InscriptionFragmentEntity(Level level, BlockPos position, Direction direction) {
        super(ModEntities.INSCRIPTION_FRAGMENT.get(), level, position, direction);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) {
            return false;
        }
        if (!level().isClientSide) {
            breakFragment(source.getEntity(), source.isCreativePlayer());
        }
        return true;
    }

    @Override
    public void dropItem(@Nullable Entity breaker) {
        if (!level().isClientSide) {
            breakFragment(breaker, breaker instanceof net.minecraft.world.entity.player.Player player
                    && player.getAbilities().instabuild);
        }
    }

    private void breakFragment(@Nullable Entity breaker, boolean suppressDrop) {
        if (fragmentDropped) {
            return;
        }
        fragmentDropped = true;
        ItemStack fragment = getItem().copy();
        setItem(ItemStack.EMPTY, false);
        if (!suppressDrop
                && !fragment.isEmpty()
                && level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            spawnAtLocation(fragment);
        }
        playSound(SoundEvents.ITEM_FRAME_BREAK, 1.0F, 1.0F);
        gameEvent(GameEvent.BLOCK_CHANGE, breaker);
        discard();
    }
}
