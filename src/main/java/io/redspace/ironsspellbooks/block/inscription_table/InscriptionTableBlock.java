package io.redspace.ironsspellbooks.block.inscription_table;

import com.mojang.serialization.MapCodec;
import io.redspace.ironsspellbooks.gui.inscription_table.InscriptionTableMenu;
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
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

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
        super(BlockBehaviour.Properties.of().strength(2.5F).sound(SoundType.WOOD).noOcclusion());
    }

    @Override
    public void destroy(LevelAccessor pLevel, BlockPos pos1, BlockState state1) {
        super.destroy(pLevel, pos1, state1);
        if (!pLevel.isClientSide()/* && pPlayer.isCreative()*/) {
            ChestType half = state1.getValue(PART);
            BlockPos pos2 = pos1.relative(getNeighbourDirection(half, state1.getValue(FACING)));
            BlockState state2 = pLevel.getBlockState(pos2);
            //IronsSpellbooks.LOGGER.debug("InscriptionTableBlock.playerWillDestory: mypos:{}, targted pos:{}", pos1, pos2);
            if (state2.is(this) && state2.getValue(PART) != state1.getValue(PART)) {
                pLevel.setBlock(pos2, Blocks.AIR.defaultBlockState(), 35);
                pLevel.levelEvent(null, 2001, pos2, Block.getId(state2));
            }
        }
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

    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

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

    @Override
    protected InteractionResult useWithoutItem(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, BlockHitResult pHitResult) {
        if (pLevel.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            pPlayer.openMenu(pState.getMenuProvider(pLevel, pPos));
            return InteractionResult.CONSUME;
        }
    }

    @Override
    @javax.annotation.Nullable
    public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
        return new SimpleMenuProvider((i, inventory, player) ->
                new InscriptionTableMenu(i, inventory, ContainerLevelAccess.create(pLevel, pPos)), Component.translatable("block.irons_spellbooks.inscription_table"));
    }

    public static final MapCodec<InscriptionTableBlock> CODEC = simpleCodec((t) -> new InscriptionTableBlock());

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }
}
