package io.redspace.ironsspellbooks.block.chiseled_bookshelf;

import io.redspace.ironsspellbooks.registries.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WisewoodChiseledBookShelfBlockEntity extends ChiseledBookShelfBlockEntity {
    public WisewoodChiseledBookShelfBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    /**
     * new strict block entity typing means we cannot reuse vanilla's classes, must override with custom type
     */
    @Override
    public BlockEntityType<?> getType() {
        return BlockRegistry.WISEWOOD_CHISELED_BOOKSHELF_ENTITY.get();
    }
}
