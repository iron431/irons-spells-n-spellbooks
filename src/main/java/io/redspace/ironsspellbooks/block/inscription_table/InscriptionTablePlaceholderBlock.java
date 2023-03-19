package io.redspace.ironsspellbooks.block.inscription_table;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import static io.redspace.ironsspellbooks.block.inscription_table.InscriptionTableBlock.FACING;

public class InscriptionTablePlaceholderBlock extends Block {
    public InscriptionTablePlaceholderBlock() {
        super(BlockBehaviour.Properties.of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD).noOcclusion());
    }

    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Direction direction = pState.getValue(FACING);
        return switch (direction) {
            case NORTH -> InscriptionTableBlock.SHAPE_LEGS_NORTH;
            case SOUTH -> InscriptionTableBlock.SHAPE_LEGS_SOUTH;
            case WEST -> InscriptionTableBlock.SHAPE_LEGS_EAST;
            default -> InscriptionTableBlock.SHAPE_LEGS_WEST;
        };
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level pLevel, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
//        if (!pLevel.isClientSide()) {
//            var neighborPos = pos.relative(state.getValue(FACING));
//            var blockstate = pLevel.getBlockState(neighborPos);
//            if (blockstate.is(BlockRegistry.INSCRIPTION_TABLE_BLOCK.get())) {
//                BlockEntity entity = pLevel.getBlockEntity(neighborPos);
//                if (entity instanceof InscriptionTableTile) {
//                    NetworkHooks.openScreen(((ServerPlayer) player), (InscriptionTableTile) entity, neighborPos);
//                } else {
//                    throw new IllegalStateException("Our Container provider is missing!");
//                }
//            }
//        }
//
//        return InteractionResult.sidedSuccess(pLevel.isClientSide());
        if (pLevel.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            var neighborPos = pos.relative(state.getValue(FACING));
            var blockstate = pLevel.getBlockState(neighborPos);
            if (blockstate.is(BlockRegistry.INSCRIPTION_TABLE_BLOCK.get())) {
                player.openMenu(blockstate.getMenuProvider(pLevel, neighborPos));
            }
            return InteractionResult.CONSUME;
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public float getDestroyProgress(BlockState pState, Player player, BlockGetter pLevel, BlockPos pPos) {
        IronsSpellbooks.LOGGER.debug("InscriptionTablePlaceholderBlock.getDestroyProgress: {} {}", pPos, pState);
        if (player.getItemInHand(player.getUsedItemHand()).getItem() instanceof AxeItem) {
            return 1;
        }
        return super.getDestroyProgress(pState, player, pLevel, pPos);
    }

    @Override
    protected void spawnDestroyParticles(Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState) {
        //This is intentionally blank. Do not remove
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public boolean skipRendering(BlockState pState, BlockState pAdjacentBlockState, Direction pDirection) {
        return true;
    }

    @Override
    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        IronsSpellbooks.LOGGER.debug("InscriptionTablePlaceholderBlock.playerWillDestroy: {} {}", pPos, pState);

        if (!pLevel.isClientSide) {
            var neighborPos = pPos.relative(pState.getValue(FACING));
            var blockstate = pLevel.getBlockState(neighborPos);
            if (blockstate.is(BlockRegistry.INSCRIPTION_TABLE_BLOCK.get())) {
                pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(), 35);
                pLevel.destroyBlock(neighborPos, !pPlayer.isCreative());
                //TODO: research if we need to raise a levelEvent here
                //pLevel.levelEvent(pPlayer, 2001, blockpos, Block.getId(blockstate));
            }
        }

        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
    }
}