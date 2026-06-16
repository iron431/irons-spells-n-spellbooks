package io.redspace.ironsspellbooks.block;

import com.mojang.serialization.MapCodec;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.particle.TraceParticleOptions;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.enums.BubbleColumnDirection;
import org.joml.Vector3f;

public class AntigravityAirBlock extends Block {
    public static final MapCodec<AntigravityAirBlock> CODEC = simpleCodec(AntigravityAirBlock::new);
    public static final BooleanProperty DIRECTION_DOWN = BooleanProperty.create("direction_down");
    public static final IntegerProperty STRENGTH = IntegerProperty.create("strength", 1, GravityPlateBlock.BASE_STRENGTH);
    private static final int CHECK_PERIOD = 5;

    public AntigravityAirBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(DIRECTION_DOWN, false).setValue(STRENGTH, 1));
    }

    public AntigravityAirBlock() {
        this(createProperties());
    }

    private static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .noCollission()
                .noLootTable()
                .strength(0.0F)
                .noOcclusion()
                .replaceable();
    }

    @Override
    public MapCodec<AntigravityAirBlock> codec() {
        return CODEC;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        BlockState blockstate = level.getBlockState(pos.above());
        boolean dragDown = state.getValue(DIRECTION_DOWN);
        if (blockstate.isAir()) {
            entity.onAboveBubbleCol(dragDown);
            if (!level.isClientSide) {
                ServerLevel serverlevel = (ServerLevel) level;
                for (int i = 0; i < 2; i++) {
                    serverlevel.sendParticles(
                            ParticleHelper.UNSTABLE_ENDER,
                            (double) pos.getX() + level.random.nextDouble(),
                            (double) (pos.getY() + 1),
                            (double) pos.getZ() + level.random.nextDouble(),
                            1,
                            0.0,
                            0.01,
                            0.0,
                            0.02
                    );
                }
            }
        } else {
            entity.onInsideBubbleColumn(dragDown);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        updateColumn(level, pos, state, level.getBlockState(pos.below()));
    }

    public static void updateColumn(LevelAccessor level, BlockPos pos, BlockState state) {
        updateColumn(level, pos, level.getBlockState(pos), state);
    }

    public static void updateColumn(LevelAccessor level, BlockPos pos, BlockState fluid, BlockState state) {
        if (canExistIn(fluid)) {
            BlockState blockstate = getColumnState(state);
            if (!blockstate.is(BlockRegistry.ANTIGRAVITY_AIR.get())) {
                if (fluid.is(BlockRegistry.ANTIGRAVITY_AIR.get())) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
                return;
            }

            level.setBlock(pos, blockstate, Block.UPDATE_CLIENTS);
            BlockPos.MutableBlockPos blockpos$mutableblockpos = pos.mutable().move(Direction.UP);
            int strength = blockstate.getValue(STRENGTH) - 1;
            boolean directionDown = blockstate.getValue(DIRECTION_DOWN);

            while (strength >= 1 && canExistIn(level.getBlockState(blockpos$mutableblockpos))) {
                BlockState upState = BlockRegistry.ANTIGRAVITY_AIR.get().defaultBlockState()
                        .setValue(DIRECTION_DOWN, directionDown)
                        .setValue(STRENGTH, strength);
                if (!level.setBlock(blockpos$mutableblockpos, upState, Block.UPDATE_CLIENTS)) {
                    return;
                }
                blockpos$mutableblockpos.move(Direction.UP);
                strength--;
            }

            while (level.getBlockState(blockpos$mutableblockpos).is(BlockRegistry.ANTIGRAVITY_AIR.get())) {
                level.setBlock(blockpos$mutableblockpos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                blockpos$mutableblockpos.move(Direction.UP);
            }
        } else if (fluid.is(BlockRegistry.ANTIGRAVITY_AIR.get())) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public static boolean canExistIn(BlockState blockState) {
        return blockState.isAir() || blockState.is(BlockRegistry.ANTIGRAVITY_AIR.get());
    }

    private static BlockState getColumnState(BlockState blockState) {
        if (blockState.is(BlockRegistry.ANTIGRAVITY_AIR.get())) {
            int strength = blockState.getValue(STRENGTH) - 1;
            if (strength < 1) {
                return Blocks.AIR.defaultBlockState();
            }
            return BlockRegistry.ANTIGRAVITY_AIR.get().defaultBlockState()
                    .setValue(DIRECTION_DOWN, blockState.getValue(DIRECTION_DOWN))
                    .setValue(STRENGTH, strength);
        }

        if (blockState.is(BlockRegistry.GRAVITY_PLATE.get())) {
            return BlockRegistry.ANTIGRAVITY_AIR.get().defaultBlockState()
                    .setValue(DIRECTION_DOWN, blockState.getValue(GravityPlateBlock.DIRECTION_DOWN))
                    .setValue(STRENGTH, GravityPlateBlock.BASE_STRENGTH);
        }

        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5;
        double y = pos.getY();
        double z = pos.getZ() + 0.5;
        boolean down = state.getValue(DIRECTION_DOWN);
        Vec3 scatter = Utils.getRandomVec3(.4f);
        Vec3 start = new Vec3(0, -1, 0);
        Vec3 end = new Vec3(0, 1, 0);
        if (down) {
            start = start.scale(-1);

            end = start.scale(-1);
        }
        level.addAlwaysVisibleParticle(ParticleHelper.UNSTABLE_ENDER, x, y, z, 0, 0, 0);
        level.addAlwaysVisibleParticle(
                new TraceParticleOptions(Utils.v3f(new Vec3(x, y, z).add(scatter.add(end))), new Vector3f(1f, .333f, 1f)),
                x + scatter.x + start.x,
                y + scatter.y + start.y,
                z + scatter.z + start.z,
                0.0,
                0.5,
                0.0
        );
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (!state.canSurvive(level, currentPos)
                || facing == Direction.DOWN
                || facing == Direction.UP && !facingState.is(BlockRegistry.ANTIGRAVITY_AIR.get()) && canExistIn(facingState)) {
            level.scheduleTick(currentPos, this, CHECK_PERIOD);
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState blockstate = level.getBlockState(pos.below());
        return blockstate.is(BlockRegistry.ANTIGRAVITY_AIR.get()) || blockstate.getBubbleColumnDirection() != BubbleColumnDirection.NONE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DIRECTION_DOWN, STRENGTH);
    }
}
