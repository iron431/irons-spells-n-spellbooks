package io.redspace.ironsspellbooks.block;

import com.mojang.serialization.MapCodec;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import static net.minecraft.world.level.block.PipeBlock.PROPERTY_BY_DIRECTION;

public class VoidstoneBlock extends Block {
    public static final MapCodec<FenceBlock> CODEC = simpleCodec(FenceBlock::new);
    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;

    @Override
    public MapCodec<FenceBlock> codec() {
        return CODEC;
    }

    public VoidstoneBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(-1.0F, 3600000.8F)
                .mapColor(MapColor.NONE)
                .noLootTable()
                .isValidSpawn(Blocks::never)
                .pushReaction(PushReaction.BLOCK)
                .sound(SoundType.COPPER)
                .lightLevel(state -> 9));
        this.registerDefaultState(
                this.stateDefinition
                        .any()
                        .setValue(NORTH, Boolean.valueOf(false))
                        .setValue(EAST, Boolean.valueOf(false))
                        .setValue(SOUTH, Boolean.valueOf(false))
                        .setValue(WEST, Boolean.valueOf(false)));
    }

    public boolean connectsTo(BlockState state, Direction direction) {
        return state.is(BlockRegistry.VOIDSTONE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockGetter blockgetter = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        BlockPos blockpos1 = blockpos.north();
        BlockPos blockpos2 = blockpos.east();
        BlockPos blockpos3 = blockpos.south();
        BlockPos blockpos4 = blockpos.west();
        BlockState blockstate = blockgetter.getBlockState(blockpos1);
        BlockState blockstate1 = blockgetter.getBlockState(blockpos2);
        BlockState blockstate2 = blockgetter.getBlockState(blockpos3);
        BlockState blockstate3 = blockgetter.getBlockState(blockpos4);
        return super.getStateForPlacement(context)
                .setValue(NORTH, this.connectsTo(blockstate, Direction.SOUTH))
                .setValue(EAST, this.connectsTo(blockstate1, Direction.WEST))
                .setValue(SOUTH, this.connectsTo(blockstate2, Direction.NORTH))
                .setValue(WEST, this.connectsTo(blockstate3, Direction.EAST));
    }

    /**
     * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
     * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately returns its solidified counterpart.
     * Note that this method should ideally consider only the specific direction passed in.
     */
    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {

        return facing.getAxis().getPlane() == Direction.Plane.HORIZONTAL
                ? state.setValue(
                PROPERTY_BY_DIRECTION.get(facing),
                this.connectsTo(facingState, facing.getOpposite())
        )
                : super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, WEST, SOUTH);
    }
}