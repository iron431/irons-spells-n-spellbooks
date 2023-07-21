package io.redspace.ironsspellbooks.block.alchemist_cauldron;

import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AlchemistCauldronBlock extends BaseEntityBlock {
    Object2ObjectOpenHashMap<Item, AlchemistCauldronInteraction> interactions = AlchemistCauldronTile.newInteractionMap();
    public AlchemistCauldronBlock() {
        super(Properties.copy(Blocks.CAULDRON));
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false).setValue(LEVEL, 0));

    }

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final int MAX_LEVELS = 4;
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, MAX_LEVELS);

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createFurnaceTicker(pLevel, pBlockEntityType, BlockRegistry.ALCHEMIST_CAULDRON_TILE.get());
    }

    @javax.annotation.Nullable
    protected static <T extends BlockEntity> BlockEntityTicker<T> createFurnaceTicker(Level pLevel, BlockEntityType<T> pServerType, BlockEntityType<? extends AlchemistCauldronTile> pClientType) {
        return pLevel.isClientSide ? null : createTickerHelper(pServerType, pClientType, AlchemistCauldronTile::serverTick);
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, LEVEL);
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockHit) {
        ItemStack itemStack = player.getItemInHand(hand);
        int currentLevel = blockState.getValue(LEVEL);
        var baseInteraction = interactions.get(itemStack.getItem()).interact(blockState, level, pos, player, hand, currentLevel, itemStack);
        if (baseInteraction != InteractionResult.PASS) {
            return baseInteraction;
        } else {
            if (itemStack.is(ItemRegistry.SCROLL.get())){
                AlchemistCauldronTile tile = (AlchemistCauldronTile) level.getBlockEntity(pos);
                if(!level.isClientSide && tile.addItem(itemStack)){
                    itemStack.shrink(1);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return super.use(blockState, level, pos, player, hand, blockHit);
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlchemistCauldronTile(pos, state);
    }

    @Override
    @SuppressWarnings({"all"})
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborstate, LevelAccessor level, BlockPos pos, BlockPos pNeighborPos) {
        if (direction.equals(Direction.DOWN)) {
            level.setBlock(pos, state.setValue(LIT, isFireSource(neighborstate)), 11);
        }
        return super.updateShape(state, direction, neighborstate, level, pos, pNeighborPos);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        LevelAccessor levelaccessor = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos().below();
        boolean flag = isFireSource(levelaccessor.getBlockState(blockpos));
        return this.defaultBlockState().setValue(LIT, flag);
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull RenderShape getRenderShape(@NotNull BlockState blockState) {
        return RenderShape.MODEL;
    }

    public boolean isFireSource(BlockState blockState) {
        //TODO: its a magic cauldron. why does it need a fire source?
        return true;//CampfireBlock.isLitCampfire(blockState);
    }

    public static boolean isLit(BlockState blockState){
        return blockState.hasProperty(LIT) && blockState.getValue(LIT);
    }

}
