package io.redspace.ironsspellbooks.block.statue;

import io.redspace.ironsspellbooks.registries.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class StatueBlockEntity extends BlockEntity {
    public StatueBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockRegistry.STATUE_BLOCK_ENTITY.get(), pos, blockState);
    }

}
