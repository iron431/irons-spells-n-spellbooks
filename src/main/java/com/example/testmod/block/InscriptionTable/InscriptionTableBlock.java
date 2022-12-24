package com.example.testmod.block.InscriptionTable;

import it.unimi.dsi.fastutil.ints.IntSets;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.material.Material.WOOD;

//https://youtu.be/CUHEKcaIpOk?t=451
public class InscriptionTableBlock extends Block implements EntityBlock{
    public InscriptionTableBlock() {
        super(BlockBehaviour.Properties.of(WOOD).strength(1));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return new InscriptionTableTile(p_153215_,p_153216_);
    }
//    @Override
//    public RenderShape getRenderShape(BlockState p_49232_) {
//        return RenderShape.MODEL;
//    }


//    @Override
//    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos){
//        System.out.println("I've been clicked!");
//
//        return InteractionResult.SUCCESS;
//    }
}
