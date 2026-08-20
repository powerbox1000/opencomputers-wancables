package io.github.powerbox1000.wancables.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.powerbox1000.wancables.Registry;
import io.github.powerbox1000.wancables.blocks.CableBlock;
import io.github.powerbox1000.wancables.blocks.blockentity.ModemBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(Level.class)
public abstract class LevelMixin {
    @Inject(
        method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;")
    )
    private void onSetBlock(BlockPos pos, BlockState newState, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> ci) {
        Level level = (Level)(Object)this;
        BlockState oldState = level.getBlockState(pos);

        if (oldState.is(Registry.CABLE_BLOCK.block()) && !newState.is(Registry.CABLE_BLOCK.block())) {
            ((CableBlock) oldState.getBlock()).blockBroken(oldState, newState, level, pos);
        } else if (oldState.is(Registry.MODEM_BLOCK.block()) && !newState.is(Registry.MODEM_BLOCK.block())) {
            ((ModemBlockEntity) level.getBlockEntity(pos)).blockBroken(oldState, newState, level, pos);
        }
    }
}
