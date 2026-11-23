package com.eldren.wutao.ramesses_curse.mixin;

import com.eldren.wutao.ramesses_curse.ModStructures;
import com.eldren.wutao.ramesses_curse.ModDimensions;
import com.eldren.wutao.ramesses_curse.GrandCathedralStructure;
import com.eldren.wutao.ramesses_curse.GrandCathedralWorldgenStructure;
import com.eldren.wutao.ramesses_curse.RamessesEndIslandPiece;
import com.eldren.wutao.ramesses_curse.RamessesEndIslandStructure;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorLocateMixin {
    @Inject(method = "findNearestMapStructure", at = @At("HEAD"), cancellable = true)
    private void ramessesCurse$locateFarEndIsland(
            ServerLevel level,
            HolderSet<Structure> structures,
            BlockPos origin,
            int searchRadius,
            boolean skipKnownStructures,
            CallbackInfoReturnable<Pair<BlockPos, Holder<Structure>>> callback) {
        if (skipKnownStructures || structures.size() != 1) {
            return;
        }
        Holder<Structure> structure = structures.iterator().next();
        if (Level.END.equals(level.dimension()) && structure.is(ModStructures.RAMESSES_END_ISLAND)) {
            callback.setReturnValue(Pair.of(
                    new BlockPos(
                            RamessesEndIslandStructure.CENTER_X,
                            RamessesEndIslandPiece.CENTER_Y,
                            RamessesEndIslandStructure.CENTER_Z),
                    structure));
            return;
        }
        if (structure.is(ModStructures.GRAND_CATHEDRAL)
                && (Level.NETHER.equals(level.dimension())
                || ModDimensions.RAMSES_VOID.equals(level.dimension()))) {
            int foundationY = Level.NETHER.equals(level.dimension())
                    ? GrandCathedralStructure.NETHER_FOUNDATION_Y
                    : 0;
            callback.setReturnValue(Pair.of(new BlockPos(
                    GrandCathedralWorldgenStructure.CENTER_X,
                    foundationY,
                    GrandCathedralWorldgenStructure.CENTER_Z), structure));
        }
    }
}
