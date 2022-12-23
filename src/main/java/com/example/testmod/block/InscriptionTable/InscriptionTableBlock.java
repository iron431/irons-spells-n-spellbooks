package com.example.testmod.block.InscriptionTable;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import static net.minecraft.world.level.material.Material.WOOD;

public class InscriptionTableBlock extends Block {
    public InscriptionTableBlock() {
        super(BlockBehaviour.Properties.of(WOOD).strength(1));
    }
}
