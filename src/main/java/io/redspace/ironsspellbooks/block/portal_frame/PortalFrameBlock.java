package io.redspace.ironsspellbooks.block.portal_frame;

import com.mojang.serialization.MapCodec;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PortalFrameBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    protected static final VoxelShape SOUTH_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 3.0);
    protected static final VoxelShape NORTH_AABB = Block.box(0.0, 0.0, 13.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape WEST_AABB = Block.box(13.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape EAST_AABB = Block.box(0.0, 0.0, 0.0, 3.0, 16.0, 16.0);

    protected static final VoxelShape LOWER_SOUTH_COLLIDER_AABB = Shapes.join(SOUTH_AABB, Block.box(1.0, 0.0, 0.0, 15.0, 16.0, 3.0), BooleanOp.ONLY_FIRST);//Shapes.join(Shapes.join(SOUTH_AABB, Block.box(1.0, 0.0, 0.0, 15.0, 16.0, 3.0), BooleanOp.ONLY_FIRST), Block.box(1.0, 0.0, 0.1, 15.0, 16.0, 2.0), BooleanOp.OR);
    protected static final VoxelShape LOWER_NORTH_COLLIDER_AABB = Shapes.join(NORTH_AABB, Block.box(1.0, 0.0, 13.0, 15.0, 16.0, 16.0), BooleanOp.ONLY_FIRST);//Shapes.join(Shapes.join(NORTH_AABB, Block.box(1.0, 0.0, 13.0, 15.0, 16.0, 16.0), BooleanOp.ONLY_FIRST), Block.box(1.0, 0.0, 14.0, 15.0, 16.0, 15.0), BooleanOp.OR);
    protected static final VoxelShape LOWER_WEST_COLLIDER_AABB = Shapes.join(WEST_AABB, Block.box(13.0, 0.0, 1.0, 16.0, 16.0, 15.0), BooleanOp.ONLY_FIRST);//Shapes.join(Shapes.join(WEST_AABB, Block.box(13.0, 0.0, 1.0, 16.0, 16.0, 15.0), BooleanOp.ONLY_FIRST), Block.box(14.0, 0.0, 1.0, 15.0, 16.0, 15.0), BooleanOp.OR);
    protected static final VoxelShape LOWER_EAST_COLLIDER_AABB = Shapes.join(EAST_AABB, Block.box(0.0, 0.0, 1.0, 3.0, 16.0, 15.0), BooleanOp.ONLY_FIRST);//Shapes.join(Shapes.join(EAST_AABB, Block.box(0.0, 0.0, 1.0, 3.0, 16.0, 15.0), BooleanOp.ONLY_FIRST), Block.box(1.0, 0.0, 1.0, 2.0, 16.0, 15.0), BooleanOp.OR);

    protected static final VoxelShape UPPER_SOUTH_COLLIDER_AABB = Shapes.join(LOWER_SOUTH_COLLIDER_AABB, Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 3.0), BooleanOp.OR);
    protected static final VoxelShape UPPER_NORTH_COLLIDER_AABB = Shapes.join(LOWER_NORTH_COLLIDER_AABB, Block.box(0.0, 15.0, 13.0, 16.0, 16.0, 16.0), BooleanOp.OR);
    protected static final VoxelShape UPPER_WEST_COLLIDER_AABB = Shapes.join(LOWER_WEST_COLLIDER_AABB, Block.box(13.0, 15.0, 0.0, 16.0, 16.0, 16.0), BooleanOp.OR);
    protected static final VoxelShape UPPER_EAST_COLLIDER_AABB = Shapes.join(LOWER_EAST_COLLIDER_AABB, Block.box(0.0, 15.0, 0.0, 3.0, 16.0, 16.0), BooleanOp.OR);


    public PortalFrameBlock() {
        super(Properties.of().noOcclusion().isSuffocating((x, y, z) -> false).sound(SoundType.COPPER_GRATE).isViewBlocking((x, y, z) -> false).strength(10, 6));
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTicker(pLevel, pBlockEntityType, BlockRegistry.PORTAL_FRAME_BLOCK_ENTITY.get());
    }

    @javax.annotation.Nullable
    protected static <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level pLevel, BlockEntityType<T> pServerType, BlockEntityType<? extends PortalFrameBlockEntity> pClientType) {
        return pLevel.isClientSide ? null : createTickerHelper(pServerType, pClientType, PortalFrameBlockEntity::serverTick);
    }


    public BlockState updateShape(BlockState myState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos myPos, BlockPos pFacingPos) {
        var half = myState.getValue(HALF);
        BlockPos requiredNeighborPos = myPos.relative(half.getDirectionToOther());
        BlockState neighborState = pLevel.getBlockState(requiredNeighborPos);
        if (!neighborState.is(this)) {
            var air = Blocks.AIR.defaultBlockState();
            //manually set to prevent block from dropping
            pLevel.setBlock(myPos, air, 35);
            pLevel.levelEvent(null, 2001, myPos, Block.getId(air));
            return air;
        }
        return super.updateShape(myState, pFacing, pFacingState, pLevel, myPos, pFacingPos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction horizontalDir = context.getHorizontalDirection();
        var facing = horizontalDir.getOpposite();
        BlockPos blockPos = context.getClickedPos();
        var bottom = context.getClickedFace() != Direction.DOWN;
        BlockPos blockPos2 = bottom ? blockPos.above() : blockPos.below();
        Level level = context.getLevel();
        if (level.getBlockState(blockPos2).canBeReplaced(context) && level.getWorldBorder().isWithinBounds(blockPos2)) {
            return this.defaultBlockState().setValue(FACING, facing).setValue(HALF, bottom ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER);
        }
        return null;
    }

    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @javax.annotation.Nullable LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        if (!pLevel.isClientSide) {
            var half = pState.getValue(HALF);
            var facing = pState.getValue(FACING);
            BlockPos blockpos = pPos.relative(half.getDirectionToOther());
            pLevel.setBlock(blockpos, pState.setValue(HALF, half.getOtherHalf()).setValue(FACING, facing), 3);
//            pLevel.setBlock(pPos, pState.setValue(PART, ChestType.RIGHT), 3);
            pLevel.blockUpdated(pPos, Blocks.AIR);
            pState.updateNeighbourShapes(pLevel, pPos, 3);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Direction direction = pState.getValue(FACING);
        return switch (direction) {
            case NORTH -> NORTH_AABB;
            case SOUTH -> SOUTH_AABB;
            case WEST -> WEST_AABB;
            default -> EAST_AABB;
        };
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
//        if (!(pContext instanceof EntityCollisionContext entityCollision) || !(entityCollision.getEntity() instanceof LivingEntity)) {
//            //unless we are living entity, give full collider to collision contexts
//            return getShape(pState, pLevel, pPos, pContext);
//        }
        Direction direction = pState.getValue(FACING);
        boolean lower = pState.getValue(HALF).equals(DoubleBlockHalf.LOWER);
        return switch (direction) {
            case NORTH -> lower ? LOWER_NORTH_COLLIDER_AABB : UPPER_NORTH_COLLIDER_AABB;
            case SOUTH -> lower ? LOWER_SOUTH_COLLIDER_AABB : UPPER_SOUTH_COLLIDER_AABB;
            case WEST -> lower ? LOWER_WEST_COLLIDER_AABB : UPPER_WEST_COLLIDER_AABB;
            default -> lower ? LOWER_EAST_COLLIDER_AABB : UPPER_EAST_COLLIDER_AABB;
        };
    }


    @Override
    protected void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
        if (!pEntity.level.isClientSide) {
            VoxelShape voxelshape = pState.getShape(pLevel, pPos, CollisionContext.of(pEntity));
            VoxelShape voxelshape1 = voxelshape.move((double) pPos.getX(), (double) pPos.getY(), (double) pPos.getZ());
            if (/*pEntity.tickCount % 20 == 0 && */pEntity.getBoundingBox().intersects(voxelshape1.bounds())) {
                pLevel.getBlockEntity(pPos, BlockRegistry.PORTAL_FRAME_BLOCK_ENTITY.get()).ifPresent(tile -> tile.setActive()/*tile.teleport(pEntity)*/);
            }
        }

        //if (pEntity.getBoundingBox().intersects(getShape(pState, pLevel, pPos, CollisionContext.empty()).bounds())) {
        //}
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, BlockHitResult pHitResult) {
        ((PortalFrameBlockEntity) pLevel.getBlockEntity(pPos)).teleport(pPlayer);
        return super.useWithoutItem(pState, pLevel, pPos, pPlayer, pHitResult);
    }

    public static final MapCodec<PortalFrameBlock> CODEC = simpleCodec((t) -> new PortalFrameBlock());

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PortalFrameBlockEntity(pPos, pState);
    }

    @Override
    protected void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        if (pLevel.getBlockEntity(pPos) instanceof PortalFrameBlockEntity portalFrame) {
            portalFrame.breakPortalConnection();
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    @Override
    public @Nullable PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.IGNORE;
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        if (blockState.getValue(HALF).equals(DoubleBlockHalf.LOWER))
            return RenderShape.MODEL;
        else
            return RenderShape.INVISIBLE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }
}
