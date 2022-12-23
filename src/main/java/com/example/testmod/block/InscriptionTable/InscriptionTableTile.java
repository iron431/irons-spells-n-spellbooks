package com.example.testmod.block.InscriptionTable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;



public class InscriptionTableTile extends BlockEntity {
    public static BlockEntityType<InscriptionTableTile> type;

    public InscriptionTableTile(BlockPos p_155501_, BlockState p_155502_) {
        super(type, p_155501_, p_155502_);
    }
}
