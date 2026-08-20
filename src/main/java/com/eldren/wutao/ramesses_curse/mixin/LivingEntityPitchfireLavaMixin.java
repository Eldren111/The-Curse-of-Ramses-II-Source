package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.ModEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityPitchfireLavaMixin {
    @Inject(method = "canStandOnFluid", at = @At("HEAD"), cancellable = true)
    private void ramessesCurse$standOnLava(FluidState fluidState, CallbackInfoReturnable<Boolean> callback) {
        LivingEntity living = (LivingEntity) (Object) this;
        if (fluidState.is(FluidTags.LAVA) && ModEvents.isPitchfireLavaWalkingActive(living)) {
            callback.setReturnValue(true);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void ramessesCurse$floatOnLava(CallbackInfo callback) {
        LivingEntity living = (LivingEntity) (Object) this;
        if (!ModEvents.isPitchfireLavaWalkingActive(living) || !living.isInLava()) {
            return;
        }

        BlockPos pos = living.blockPosition();
        if (CollisionContext.of(living).isAbove(LiquidBlock.STABLE_SHAPE, pos, true)
                && !living.level().getFluidState(pos.above()).is(FluidTags.LAVA)) {
            Vec3 movement = living.getDeltaMovement();
            if (movement.y < 0.0D) {
                living.setDeltaMovement(movement.x, 0.0D, movement.z);
                living.setOnGround(true);
            } else if (movement.y > 0.0D) {
                living.setOnGround(false);
            } else {
                living.setOnGround(true);
            }
            return;
        }

        living.setDeltaMovement(living.getDeltaMovement().scale(0.5D).add(0.0D, 0.05D, 0.0D));
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void ramessesCurse$travelOnLava(Vec3 travelVector, CallbackInfo callback) {
        LivingEntity living = (LivingEntity) (Object) this;
        if (!ModEvents.isPitchfireLavaWalkingActive(living) || !living.isInLava()) {
            return;
        }

        BlockPos pos = living.blockPosition();
        if (!CollisionContext.of(living).isAbove(LiquidBlock.STABLE_SHAPE, pos, true)
                || living.level().getFluidState(pos.above()).is(FluidTags.LAVA)) {
            return;
        }

        living.moveRelative(0.1F, travelVector);
        Vec3 movement = living.getDeltaMovement();
        double verticalMovement = Math.max(0.0D, movement.y);
        living.move(MoverType.SELF, new Vec3(movement.x, verticalMovement, movement.z));
        Vec3 afterMove = living.getDeltaMovement();
        living.setDeltaMovement(afterMove.x * 0.91D, verticalMovement, afterMove.z * 0.91D);
        living.setOnGround(verticalMovement <= 0.0D);
        living.fallDistance = 0.0F;
        callback.cancel();
    }
}
