package io.redspace.ironsspellbooks.block.ice_spider_egg;

import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CookedIceSpiderEggBlock extends Block {
    public static final int MAX_BITES = 3;
    public static final IntegerProperty BITES = IntegerProperty.create("bites", 0, MAX_BITES);
    private static final int FROSTBITE_DURATION = 45 * 20;
    private static final int FROSTBITE_AMPLIFIER = 9;

    private static final VoxelShape PLATE = Block.box(1, 0, 1, 15, 1, 15);
    private static final VoxelShape EGG_0 = Block.box(3, 1, 2, 13, 15, 14);
    private static final VoxelShape SHAPE = Shapes.join(PLATE, EGG_0, BooleanOp.OR);

    public CookedIceSpiderEggBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BITES, 0));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (level.isClientSide) {
            if (eat(level, pos, state, player).consumesAction()) {
                return InteractionResult.SUCCESS;
            }
            if (itemStack.isEmpty()) {
                return InteractionResult.CONSUME;
            }
        }
        return eat(level, pos, state, player);
    }

    protected static InteractionResult eat(LevelAccessor level, BlockPos pos, BlockState state, Player player) {
        if (!player.canEat(false)) {
            return InteractionResult.PASS;
        }
        player.getFoodData().eat(4, 1F);
        if (!level.isClientSide()) {
            MobEffectInstance mobEffectInstance = player.getEffect(MobEffectRegistry.FROSTBITTEN_STRIKES.get());
            if (mobEffectInstance == null) {
                mobEffectInstance = new MobEffectInstance(MobEffectRegistry.FROSTBITTEN_STRIKES.get(), FROSTBITE_DURATION, FROSTBITE_AMPLIFIER, false, false, true);
            } else {
                mobEffectInstance = new MobEffectInstance(MobEffectRegistry.FROSTBITTEN_STRIKES.get(), mobEffectInstance.getDuration() + FROSTBITE_DURATION, FROSTBITE_AMPLIFIER, false, false, true);
            }
            player.addEffect(mobEffectInstance);
            level.playSound(null, pos, SoundEvents.GENERIC_EAT, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.playSound(null, pos, SoundEvents.HONEY_DRINK, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        if (level instanceof Level particleLevel) {
            int color = 0xd0f9ff;
            double d0 = (double) (color >> 16 & 255) / 255.0D;
            double d1 = (double) (color >> 8 & 255) / 255.0D;
            double d2 = (double) (color & 255) / 255.0D;
            for (int i = 0; i < 5; i++) {
                particleLevel.addParticle(ParticleTypes.ENTITY_EFFECT,
                        pos.getX() + (2 + particleLevel.getRandom().nextFloat() * 10) / 16f,
                        pos.getY() + 15 / 16f,
                        pos.getZ() + (2 + particleLevel.getRandom().nextFloat() * 14) / 16f,
                        d0, d1, d2);
            }
        }
        int bites = state.getValue(BITES);
        level.gameEvent(player, GameEvent.EAT, pos);
        if (bites < MAX_BITES) {
            level.setBlock(pos, state.setValue(BITES, bites + 1), 3);
        } else {
            level.destroyBlock(pos, false);
            level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
            level.playSound(null, pos, SoundEvents.SNIFFER_EGG_HATCH, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        return facing == Direction.DOWN && !state.canSurvive(level, currentPos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isSolid();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BITES);
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        return (4 - blockState.getValue(BITES)) * 2;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType pathComputationType) {
        return false;
    }
}
