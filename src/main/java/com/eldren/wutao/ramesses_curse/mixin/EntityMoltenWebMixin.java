package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.ModEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMoltenWebMixin {
    @Inject(method = "makeStuckInBlock", at = @At("HEAD"), cancellable = true)
    private void ramessesCurse$ignoreCobwebForMolten(BlockState state, Vec3 multiplier, CallbackInfo callback) {
        Entity self = (Entity) (Object) this;
        if (state.is(Blocks.COBWEB)
                && self instanceof LivingEntity living
                && ModEvents.isDynastyMoltenActive(living)) {
            callback.cancel();
        }
    }
}
