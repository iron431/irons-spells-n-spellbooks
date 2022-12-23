package com.example.testmod.block.InscriptionTable;
import com.example.testmod.registries.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;



public class InscriptionTableTile extends BlockEntity {

    public InscriptionTableTile(BlockPos p_155501_, BlockState p_155502_) {
        super(BlockRegistry.INSCRIPTION_TABLE_TILE.get(), p_155501_, p_155502_);
    }

    @Override
    protected void saveAdditional(CompoundTag p_187471_) {
        super.saveAdditional(p_187471_);
    }
    @Override
    public void load(CompoundTag p_155245_) {
        super.load(p_155245_);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return super.getUpdateTag();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
    }
}
