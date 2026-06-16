package io.redspace.ironsspellbooks.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.neoforge.common.enums.BubbleColumnDirection;

public class GravityPlateBlock extends SlabBlock {
    public static final MapCodec<GravityPlateBlock> CODEC = simpleCodec(GravityPlateBlock::new);
    public static final BooleanProperty DIRECTION_DOWN = BooleanProperty.create("direction_down");
    public static final int BASE_STRENGTH = 5;
    private static final int BUBBLE_COLUMN_CHECK_DELAY = 20;

    public GravityPlateBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(DIRECTION_DOWN, false));
    }

    public GravityPlateBlock() {
        this(createProperties());
    }

    private static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(DyeColor.PURPLE)
                .strength(2.0F, 6.0F)
                .sound(SoundType.COPPER)
                .requiresCorrectToolForDrops();
    }

    @Override
    public MapCodec<? extends SlabBlock> codec() {
        return CODEC;
    }

    @Override
    public BubbleColumnDirection getBubbleColumnDirection(BlockState state) {
        return state.getValue(DIRECTION_DOWN) ? BubbleColumnDirection.DOWNWARD : BubbleColumnDirection.UPWARD;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        AntigravityAirBlock.updateColumn(level, pos.above(), state);
    }

    /**
     * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
     * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately returns its solidified counterpart.
     * Note that this method should ideally consider only the specific direction passed in.
     */
    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (facing == Direction.UP && AntigravityAirBlock.canExistIn(facingState)) {
            level.scheduleTick(currentPos, this, BUBBLE_COLUMN_CHECK_DELAY);
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        level.scheduleTick(pos, this, BUBBLE_COLUMN_CHECK_DELAY);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(DIRECTION_DOWN);
    }
}
