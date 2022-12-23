package com.example.testmod.block.InscriptionTable;


import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static com.example.testmod.registries.BlockRegistry.INSCRIPTION_TABLE_TILE;

public class InscriptionTableTile extends BlockEntity {


    public InscriptionTableTile(BlockPos p_155501_, BlockState p_155502_) {
        super(INSCRIPTION_TABLE_TILE, p_155501_, p_155502_);
    }
}
