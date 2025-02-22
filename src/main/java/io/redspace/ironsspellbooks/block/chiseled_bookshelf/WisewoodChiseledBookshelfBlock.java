package io.redspace.ironsspellbooks.block.chiseled_bookshelf;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class WisewoodChiseledBookshelfBlock extends ChiseledBookShelfBlock {
    public WisewoodChiseledBookshelfBlock(Properties properties) {
        super(properties);
    }

    /**
     * new strict block entity typing means we cannot reuse vanilla's classes, must override with custom entity class
     */
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WisewoodChiseledBookShelfBlockEntity(pos, state);
    }
}
