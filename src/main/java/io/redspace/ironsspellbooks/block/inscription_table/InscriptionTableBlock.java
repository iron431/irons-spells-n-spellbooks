package io.redspace.ironsspellbooks.block.inscription_table;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.gui.inscription_table.InscriptionTableMenu;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
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
import org.jetbrains.annotations.Nullable;

//https://youtu.be/CUHEKcaIpOk?t=451
public class InscriptionTableBlock extends Block /*implements EntityBlock*/ {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 18, 16);
    //    private static final VoxelShape LEG_NE = Block.box(12, 0, 12, 3, 10, 3);
//    private static final VoxelShape LEG_NW = Block.box(1, 0, 12, 3, 10, 3);
//    private static final VoxelShape LEG_SE = Block.box(12, 0, 1, 3, 10, 3);
//    private static final VoxelShape LEG_SW = Block.box(1, 0, 1, 3, 10, 3);
//    private static final VoxelShape TABLE_TOP = Block.box(0, 10, 0, 16, 4, 16);
    //public static final VoxelShape SHAPE_COMMON = Shapes.or(SHAPE_WEST, SHAPE_POST);
    public static final VoxelShape SHAPE_TABLETOP = Block.box(0, 10, 0, 16, 14, 16);
    public static final VoxelShape SHAPE_LEG_1 = Block.box(1, 0, 1, 4, 10, 4);
    public static final VoxelShape SHAPE_LEG_2 = Block.box(12, 0, 1, 15, 10, 4);
    public static final VoxelShape SHAPE_LEG_3 = Block.box(1, 0, 12, 4, 10, 15);
    public static final VoxelShape SHAPE_LEG_4 = Block.box(12, 0, 12, 15, 10, 15);
    public static final VoxelShape SHAPE_LEGS_EAST = Shapes.or(SHAPE_LEG_2, SHAPE_LEG_4, SHAPE_TABLETOP);
    public static final VoxelShape SHAPE_LEGS_WEST = Shapes.or(SHAPE_LEG_1, SHAPE_LEG_3, SHAPE_TABLETOP);
    public static final VoxelShape SHAPE_LEGS_NORTH = Shapes.or(SHAPE_LEG_3, SHAPE_LEG_4, SHAPE_TABLETOP);
    public static final VoxelShape SHAPE_LEGS_SOUTH = Shapes.or(SHAPE_LEG_1, SHAPE_LEG_2, SHAPE_TABLETOP);



    public InscriptionTableBlock() {
        super(BlockBehaviour.Properties.of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD).noOcclusion());
    }

    @Override
    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        IronsSpellbooks.LOGGER.debug("InscriptionTablePlaceholderBlock.playerWillDestroy: {} {}", pPos, pState);

        if (!pLevel.isClientSide) {
            var placeholderDirection = getPlaceholderDirection(pState.getValue(FACING));
            var neighborPos = pPos.relative(placeholderDirection);
            var blockstate = pLevel.getBlockState(neighborPos);
            if (blockstate.is(BlockRegistry.INSCRIPTION_TABLE_PLACEHOLDER_BLOCK.get())) {
                pLevel.setBlock(neighborPos, Blocks.AIR.defaultBlockState(), 35);
                //TODO: research if we need to raise a levelEvent here
                //pLevel.levelEvent(pPlayer, 2001, blockpos, Block.getId(blockstate));
            }
        }

        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Direction direction = pState.getValue(FACING);
        return switch (direction) {
            case NORTH -> SHAPE_LEGS_WEST;
            case SOUTH -> SHAPE_LEGS_EAST;
            case WEST -> SHAPE_LEGS_NORTH;
            default -> SHAPE_LEGS_SOUTH;
        };
    }

    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        IronsSpellbooks.LOGGER.debug("updateShape: {} {} {} {} {}", pState, pFacing, pFacingState, pCurrentPos, pFacingPos);
        return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction placingDirection = context.getHorizontalDirection().getOpposite();
        BlockPos blockpos = context.getClickedPos();
        BlockPos blockpos1 = blockpos.relative(getPlaceholderDirection(placingDirection));
        Level level = context.getLevel();

        if (level.getBlockState(blockpos1).canBeReplaced(context) && level.getWorldBorder().isWithinBounds(blockpos1)) {
            return this.defaultBlockState().setValue(FACING, placingDirection);
        }

        return null;
    }

    private Direction getPlaceholderDirection(Direction facing) {
        return switch (facing) {
            case NORTH -> Direction.EAST;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
            default -> Direction.SOUTH;
        };
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @javax.annotation.Nullable LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        if (!pLevel.isClientSide) {
            var placeholderDirection = getPlaceholderDirection(pState.getValue(FACING));
            BlockPos blockpos = pPos.relative(placeholderDirection);
            var state = BlockRegistry.INSCRIPTION_TABLE_PLACEHOLDER_BLOCK.get().defaultBlockState().setValue(FACING, placeholderDirection.getOpposite());
            pLevel.setBlock(blockpos, state, 3);
            // pLevel.setBlock(blockpos, pState.setValue(PART, InscriptionTablePart.PLACEHOLDER), 3);
            //pLevel.blockUpdated(blockpos, BlockRegistry.INSCRIPTION_TABLE_PLACEHOLDER_BLOCK.get());
            IronsSpellbooks.LOGGER.debug("setPlacedBy {} {}", pPos, blockpos);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        //TODO: this might need to deal with the double block
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        //TODO: this might need to deal with the double block
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

//    @Override
//    @SuppressWarnings("deprecation")
//    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
//        if (pState.getBlock() != pNewState.getBlock()) {
//            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
//            if (blockEntity instanceof InscriptionTableTile) {
//                ((InscriptionTableTile) blockEntity).drops();
//            }
//        }
//        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
//    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level pLevel, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
//        if (!pLevel.isClientSide()) {
//            BlockEntity entity = pLevel.getBlockEntity(pos);
//            if (entity instanceof InscriptionTableTile) {
//                NetworkHooks.openScreen(((ServerPlayer) player), (InscriptionTableTile) entity, pos);
//            } else {
//                throw new IllegalStateException("Our Container provider is missing!");
//            }
//        }
//
//        return InteractionResult.sidedSuccess(pLevel.isClientSide());
        if (pLevel.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            player.openMenu(state.getMenuProvider(pLevel, pos));
            return InteractionResult.CONSUME;
        }
    }
    @Override
    @javax.annotation.Nullable
    public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
        return new SimpleMenuProvider((i, inventory, player) ->
                new InscriptionTableMenu(i, inventory, ContainerLevelAccess.create(pLevel, pPos)), Component.translatable("block.irons_spellbooks.inscription_table"));
    }
//    @Nullable
//    @Override
//    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
//        return new InscriptionTableTile(pos, state);
//    }
}
