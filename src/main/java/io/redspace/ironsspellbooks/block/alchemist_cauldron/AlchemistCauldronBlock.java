package io.redspace.ironsspellbooks.block.alchemist_cauldron;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AlchemistCauldronBlock extends BaseEntityBlock {
    Object2ObjectOpenHashMap<Item, AlchemistCauldronInteraction> interactions = newInteractionMap();
    public AlchemistCauldronBlock() {
        super(Properties.copy(Blocks.CAULDRON));
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false).setValue(LEVEL, 0));

    }

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final int MAX_LEVELS = 4;
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, MAX_LEVELS);


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, LEVEL);
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockHit) {
        ItemStack itemStack = player.getItemInHand(hand);
        int currentLevel = blockState.getValue(LEVEL);
        if (itemStack.is(Items.WATER_BUCKET)) {
            if (currentLevel < MAX_LEVELS) {
                this.createFilledResult(player, hand, level, blockState, pos, MAX_LEVELS, new ItemStack(Items.BUCKET), SoundEvents.BUCKET_EMPTY);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        } else if (currentLevel > 0 && itemStack.is(Items.GLASS_BOTTLE)) {
            //TODO: safety checks?
            var storedItems = ((AlchemistCauldronTile) level.getBlockEntity(pos)).getStoredItems();
            if (storedItems.empty()) {
                //No items means we only hold water, so we should create a water bottle and decrement level
                this.createFilledResult(player, hand, level, blockState, pos, currentLevel - 1, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER), SoundEvents.BOTTLE_FILL);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return super.use(blockState, level, pos, player, hand, blockHit);
    }

    private void createFilledResult(Player player, InteractionHand hand, Level level, BlockState blockState, BlockPos blockPos, int newLevel, ItemStack resultItem, SoundEvent soundEvent) {
        player.setItemInHand(hand, ItemUtils.createFilledResult(player.getItemInHand(hand), player, resultItem));
        level.setBlock(blockPos, blockState.setValue(LEVEL, newLevel), 3);
        level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
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

    interface AlchemistCauldronInteraction{
        InteractionResult interact(BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, int currentLevel);
    }
    static Object2ObjectOpenHashMap<Item, AlchemistCauldronInteraction> newInteractionMap() {
        return Util.make(new Object2ObjectOpenHashMap<>(), (p_175646_) -> {
            p_175646_.defaultReturnValue((p_175739_, p_175740_, p_175741_, p_175742_, p_175743_, p_175744_) -> {
                return InteractionResult.PASS;
            });
        });
    }

}
