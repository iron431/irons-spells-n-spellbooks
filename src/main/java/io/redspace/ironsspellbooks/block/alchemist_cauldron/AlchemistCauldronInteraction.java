package io.redspace.ironsspellbooks.block.alchemist_cauldron;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

interface AlchemistCauldronInteraction{
    @Nullable
    ItemStack interact(BlockState blockState, Level level, BlockPos pos, int currentLevel, ItemStack itemStack);
}
