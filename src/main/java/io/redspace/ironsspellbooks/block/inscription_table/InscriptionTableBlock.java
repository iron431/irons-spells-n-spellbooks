package io.redspace.ironsspellbooks.block.inscription_table;

import com.mojang.blaze3d.shaders.Effect;
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
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

//https://youtu.be/CUHEKcaIpOk?t=451
public class InscriptionTableBlock extends HorizontalDirectionalBlock /*implements EntityBlock*/ {
    //Only use left/right
    public static final EnumProperty<ChestType> PART = BlockStateProperties.CHEST_TYPE;

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

//    @Override
//    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
//        IronsSpellbooks.LOGGER.debug("InscriptionTablePlaceholderBlock.playerWillDestroy: {} {}", pPos, pState);
//
//        if (!pLevel.isClientSide) {
//            var placeholderDirection = getPlaceholderDirection(pState.getValue(FACING));
//            var neighborPos = pPos.relative(placeholderDirection);
//            var blockstate = pLevel.getBlockState(neighborPos);
//            if (blockstate.is(BlockRegistry.INSCRIPTION_TABLE_PLACEHOLDER_BLOCK.get())) {
//                pLevel.setBlock(neighborPos, Blocks.AIR.defaultBlockState(), 35);
//                //TODO: research if we need to raise a levelEvent here
//                //pLevel.levelEvent(pPlayer, 2001, blockpos, Block.getId(blockstate));
//            }
//        }
//
//        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
//    }

    public void playerWillDestroy(Level pLevel, BlockPos pos1, BlockState state1, Player pPlayer) {
        if (!pLevel.isClientSide/* && pPlayer.isCreative()*/) {
            ChestType half = state1.getValue(PART);
            BlockPos pos2 = pos1.relative(getNeighbourDirection(half, state1.getValue(FACING)));
            BlockState state2 = pLevel.getBlockState(pos2);
            //IronsSpellbooks.LOGGER.debug("InscriptionTableBlock.playerWillDestory: mypos:{}, targted pos:{}", pos1, pos2);
            if (state2.is(this) && state2.getValue(PART) != state1.getValue(PART)) {
                pLevel.setBlock(pos2, Blocks.AIR.defaultBlockState(), 35);
                pLevel.levelEvent(pPlayer, 2001, pos2, Block.getId(state2));
            }
        }

        super.playerWillDestroy(pLevel, pos1, state1, pPlayer);
    }

    private static Direction getNeighbourDirection(ChestType pPart, Direction pDirection) {
        return pPart == ChestType.LEFT ? pDirection.getCounterClockWise() : pDirection.getClockWise();
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Direction direction = pState.getValue(PART).equals(ChestType.RIGHT) ? pState.getValue(FACING) : pState.getValue(FACING).getOpposite();
        return switch (direction) {
            case NORTH -> SHAPE_LEGS_WEST;
            case SOUTH -> SHAPE_LEGS_EAST;
            case WEST -> SHAPE_LEGS_NORTH;
            default -> SHAPE_LEGS_SOUTH;
        };
    }

    //    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
//        IronsSpellbooks.LOGGER.debug("updateShape: {} {} {} {} {}", pState, pFacing, pFacingState, pCurrentPos, pFacingPos);
//        return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
//    }
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
//    if (pFacing == getNeighbourDirection(pState.getValue(PART), pState.getValue(FACING))) {
//        return pFacingState.is(this) && pFacingState.getValue(PART) != pState.getValue(PART) ? pState.setValue(OCCUPIED, pFacingState.getValue(OCCUPIED)) : Blocks.AIR.defaultBlockState();
//    } else {
        return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
//    }
    }


//    @Nullable
//    public BlockState getStateForPlacement(BlockPlaceContext context) {
//        Direction placingDirection = context.getHorizontalDirection().getOpposite();
//        BlockPos blockpos = context.getClickedPos();
//        BlockPos blockpos1 = blockpos.relative(getPlaceholderDirection(placingDirection));
//        Level level = context.getLevel();
//
//        if (level.getBlockState(blockpos1).canBeReplaced(context) && level.getWorldBorder().isWithinBounds(blockpos1)) {
//            return this.defaultBlockState().setValue(FACING, placingDirection);
//        }
//
//        return null;
//    }

    @javax.annotation.Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        Direction direction = pContext.getHorizontalDirection();
        BlockPos blockpos = pContext.getClickedPos();
        BlockPos blockpos1 = blockpos.relative(direction.getCounterClockWise());
        Level level = pContext.getLevel();
        if (level.getBlockState(blockpos1).canBeReplaced(pContext) && level.getWorldBorder().isWithinBounds(blockpos1)) {
            return this.defaultBlockState().setValue(FACING, direction.getOpposite());
        }

        return null;
    }

//    private Direction getPlaceholderDirection(Direction facing) {
//        return switch (facing) {
//            case NORTH -> Direction.EAST;
//            case SOUTH -> Direction.WEST;
//            case WEST -> Direction.NORTH;
//            default -> Direction.SOUTH;
//        };
//    }

//    @Override
//    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @javax.annotation.Nullable LivingEntity pPlacer, ItemStack pStack) {
//        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
//        if (!pLevel.isClientSide) {
//            var placeholderDirection = getPlaceholderDirection(pState.getValue(FACING));
//            BlockPos blockpos = pPos.relative(placeholderDirection);
//            var state = BlockRegistry.INSCRIPTION_TABLE_PLACEHOLDER_BLOCK.get().defaultBlockState().setValue(FACING, placeholderDirection.getOpposite());
//            pLevel.setBlock(blockpos, state, 3);
//            // pLevel.setBlock(blockpos, pState.setValue(PART, InscriptionTablePart.PLACEHOLDER), 3);
//            //pLevel.blockUpdated(blockpos, BlockRegistry.INSCRIPTION_TABLE_PLACEHOLDER_BLOCK.get());
//            IronsSpellbooks.LOGGER.debug("setPlacedBy {} {}", pPos, blockpos);
//        }
//    }

    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        if (!pLevel.isClientSide) {
            BlockPos blockpos = pPos.relative(pState.getValue(FACING).getClockWise());
            pLevel.setBlock(blockpos, pState.setValue(PART, ChestType.LEFT), 3);
            pLevel.setBlock(pPos, pState.setValue(PART, ChestType.RIGHT), 3);
            pLevel.blockUpdated(pPos, Blocks.AIR);
            pState.updateNeighbourShapes(pLevel, pPos, 3);
        }

    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        if (blockState.getValue(PART).equals(ChestType.RIGHT))
            return RenderShape.MODEL;
        else
            return RenderShape.INVISIBLE;
    }

    public PushReaction getPistonPushReaction(BlockState pState) {
        return PushReaction.BLOCK;
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

}
