package io.redspace.ironsspellbooks.block.statue;

import com.mojang.serialization.MapCodec;
import io.redspace.ironsspellbooks.patreon.PatreonHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class StatueBlock extends BaseEntityBlock {
    public static final int MAX = RotationSegment.getMaxSegmentIndex();
    private static final int ROTATIONS = MAX + 1;
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;

    public StatueBlock() {
        super(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).noOcclusion());
        this.registerDefaultState(this.defaultBlockState().setValue(ROTATION, 0));
    }

    /* ----------------------------------- *
     * Codec
     * -----------------------------------*/
    public static final MapCodec<StatueBlock> CODEC = simpleCodec((t) -> new StatueBlock());

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /* ----------------------------------- *
     * Block Entity Handling
     * -----------------------------------*/
    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new StatueBlockEntity(pos, state);
    }

    /* ----------------------------------- *
     * Gameplay
     * -----------------------------------*/

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        if (stack.is(Items.NAME_TAG) && stack.has(DataComponents.CUSTOM_NAME)) {
            String username = stack.get(DataComponents.CUSTOM_NAME).getString();
            if (level.getBlockEntity(pos) instanceof StatueBlockEntity statueBlockEntity &&
                    PatreonHandler.getPatreonPermissionsByUsername(username).supportsStatues()) {
                UUID uuid = PatreonHandler.profileFromUsername(username);
                if (uuid != null) {
                    statueBlockEntity.setPlayerUuid(uuid);
                    statueBlockEntity.setChanged();
                    if (!player.hasInfiniteMaterials()) {
                        stack.shrink(1);

                    }
                    return ItemInteractionResult.SUCCESS;
                }
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    /* ----------------------------------- *
     * Rotation
     * -----------------------------------*/
    @Override
    protected @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(ROTATION, rotation.rotate(state.getValue(ROTATION), ROTATIONS));
    }

    @Override
    protected @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(ROTATION, mirror.mirror(state.getValue(ROTATION), ROTATIONS));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ROTATION);
    }

    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return this.defaultBlockState().setValue(ROTATION, RotationSegment.convertToSegment(context.getRotation()));
    }

    /* ----------------------------------- *
     * Rendering
     * -----------------------------------*/
    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
}
