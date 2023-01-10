package com.example.testmod.block.InscriptionTable;

import com.example.testmod.registries.BlockRegistry;
import it.unimi.dsi.fastutil.ints.IntSets;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.material.Material.WOOD;

//https://youtu.be/CUHEKcaIpOk?t=451
public class InscriptionTableBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public InscriptionTableBlock() {
        super(BlockBehaviour.Properties.of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD).noOcclusion());
    }

//    private static final VoxelShape LEG_NE = Block.box(12, 0, 12, 3, 10, 3);
//    private static final VoxelShape LEG_NW = Block.box(1, 0, 12, 3, 10, 3);
//    private static final VoxelShape LEG_SE = Block.box(12, 0, 1, 3, 10, 3);
//    private static final VoxelShape LEG_SW = Block.box(1, 0, 1, 3, 10, 3);
//    private static final VoxelShape TABLE_TOP = Block.box(0, 10, 0, 16, 4, 16);
    //public static final VoxelShape SHAPE_COMMON = Shapes.or(SHAPE_WEST, SHAPE_POST);
public static final VoxelShape SHAPE_TABLETOP = Block.box(0,10,0,16,14,16);
    public static final VoxelShape SHAPE_LEG_1 = Block.box(1,0,1,4,10,4);
    public static final VoxelShape SHAPE_LEG_2 = Block.box(12,0,1,15,10,4);
    public static final VoxelShape SHAPE_LEG_3 = Block.box(1,0,12,4,10,15);
    public static final VoxelShape SHAPE_LEG_4 = Block.box(12,0,12,15,10,15);

    public static final VoxelShape SHAPE = Shapes.or(SHAPE_LEG_1,SHAPE_LEG_2,SHAPE_LEG_3,SHAPE_LEG_4, SHAPE_TABLETOP);

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        //return Shapes.or(LEG_NE,LEG_NW,LEG_SE,LEG_SW,TABLE_TOP);
        return SHAPE;
    }

    /* FACING */

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
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
        builder.add(FACING);
    }

    /* BLOCK ENTITY */

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof InscriptionTableTile) {
                ((InscriptionTableTile) blockEntity).drops();
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level pLevel, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!pLevel.isClientSide()) {
            BlockEntity entity = pLevel.getBlockEntity(pos);
            if (entity instanceof InscriptionTableTile) {
                NetworkHooks.openScreen(((ServerPlayer) player), (InscriptionTableTile) entity, pos) ;
            } else {
                throw new IllegalStateException("Our Container provider is missing!");
            }
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InscriptionTableTile(pos, state);
    }

//    @Nullable
//    @Override
//    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
//        return createTickerHelper(pBlockEntityType, BlockRegistry.INSCRIPTION_TABLE_TILE.get(),
//                InscriptionTableTile::tick);
//    }
}
