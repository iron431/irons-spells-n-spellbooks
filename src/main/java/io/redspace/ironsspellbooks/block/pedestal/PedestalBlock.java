package io.redspace.ironsspellbooks.block.pedestal;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.Nullable;

import java.util.List;

//https://youtu.be/CUHEKcaIpOk?t=451
public class PedestalBlock extends BaseEntityBlock {
    public static final VoxelShape SHAPE_COLUMN = Block.box(3, 4, 3, 13, 12, 13);
    public static final VoxelShape SHAPE_BOTTOM = Block.box(0, 0, 0, 16, 4, 16);
    public static final VoxelShape SHAPE_TOP = Block.box(0, 12, 0, 16, 16, 16);

    public static final VoxelShape SHAPE = Shapes.or(SHAPE_BOTTOM, SHAPE_TOP, SHAPE_COLUMN);

    //Copied from enchantment table block
    public static final List<BlockPos> BOOKSHELF_OFFSETS = BlockPos.betweenClosedStream(-3, -1, -3, 3, 1, 3).filter((blockPos) -> {
        return Math.abs(blockPos.getX()) == 3 || Math.abs(blockPos.getZ()) == 3;
    }).map(BlockPos::immutable).toList();

    public PedestalBlock() {
        super(Properties.copy(Blocks.LODESTONE).noOcclusion());
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        //return Shapes.or(LEG_NE,LEG_NW,LEG_SE,LEG_SW,TABLE_TOP);
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level pLevel, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!pLevel.isClientSide()) {
            BlockEntity entity = pLevel.getBlockEntity(pos);
 //Ironsspellbooks.logger.debug("PedestalBlock.use");
            if (entity instanceof PedestalTile pedestalTile) {

                ItemStack currentPedestalItem = pedestalTile.getHeldItem();
                ItemStack handItem = player.getItemInHand(hand);

                //Drop Current Item
                ItemStack playerItem = currentPedestalItem.copy();
                if (handItem.isEmpty() || handItem.getCount() == 1) {
                    player.setItemInHand(hand, playerItem);
                } else {
                    dropItem(playerItem, player);
                }
                pedestalTile.setHeldItem(ItemStack.EMPTY);


                //Place a singular new Item
                currentPedestalItem = handItem.copy();
                if (!currentPedestalItem.isEmpty()) {
                    currentPedestalItem.setCount(1);
                    pedestalTile.setHeldItem(currentPedestalItem);
                    handItem.shrink(1);
                }
                //Let clients know to update rendered item
                pLevel.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
                //handItem.setCount(1);
                //player.setItemInHand(hand,currentPedestalItem);
            }
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        super.animateTick(pState, pLevel, pPos, pRandom);
        //irons_spellbooks.LOGGER.debug("Pedestal Block: animate tick");
        for (BlockPos blockpos : BOOKSHELF_OFFSETS) {
            //irons_spellbooks.LOGGER.debug("Pedestal Block: {}", blockpos);

            if (pRandom.nextInt(16) == 0 && pLevel.getBlockState(pPos.offset(blockpos)).is(Tags.Blocks.BOOKSHELVES)) {
                pLevel.addParticle(ParticleTypes.ENCHANT, (double) pPos.getX() + 0.5D, (double) pPos.getY() + 2.0D, (double) pPos.getZ() + 0.5D, (double) ((float) blockpos.getX() + pRandom.nextFloat()) - 0.5D, (double) ((float) blockpos.getY() - pRandom.nextFloat() - 1.0F), (double) ((float) blockpos.getZ() + pRandom.nextFloat()) - 0.5D);
            }
        }

    }

    private void dropItem(ItemStack itemstack, Player owner) {
        if (owner instanceof ServerPlayer serverplayer) {
            ItemEntity itementity = serverplayer.drop(itemstack, false);
            if (itementity != null) {
                itementity.setNoPickUpDelay();
                itementity.setThrower(serverplayer.getUUID());
            }
        }
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof PedestalTile) {
                ((PedestalTile) blockEntity).drops();
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PedestalTile(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }
}
