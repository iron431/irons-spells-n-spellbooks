package io.redspace.ironsspellbooks.block;

import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FireflyJar extends Block {
    public FireflyJar() {
        super(BlockBehaviour.Properties.copy(Blocks.GLASS).lightLevel((x) -> 8));
    }

    public static final VoxelShape SHAPE = Shapes.or(Block.box(4, 0, 4, 12, 13, 12),Block.box(6, 13, 6, 10, 16, 10));

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        double d0 = pPos.getX() + 0.5D;
        double d1 = pPos.getY();
        double d2 = pPos.getZ() + 0.5D;
        double d3 = pRandom.nextDouble() * 0.6D - 0.3D;
        double d4 = pRandom.nextDouble() * 0.6D;
        double d6 = pRandom.nextDouble() * 0.6D - 0.3D;

        pLevel.addParticle(ParticleHelper.FIREFLY, d0 + d3, d1 + d4, d2 + d6, 0.0D, 0.0D, 0.0D);
        pLevel.addParticle(ParticleHelper.FIREFLY, d0 + d3 * 2, d1 + d4 * 2, d2 + d6 * 2, 0.0D, 0.0D, 0.0D);

    }
}
