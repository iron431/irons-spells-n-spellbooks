package io.redspace.ironsspellbooks.block.alchemist_cauldron;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public interface AlchemistCauldronInteraction {
    @Nullable
    ItemStack interact(AlchemistCauldronTile blockEntity, BlockState blockState, Level level, BlockPos pos, ItemStack itemStack);
}
