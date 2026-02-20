package io.redspace.ironsspellbooks.block.statue;

import com.mojang.serialization.MapCodec;
import io.redspace.ironsspellbooks.patreon.PatreonHandler;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import net.minecraft.core.BlockBox;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public class StatueBlock extends BaseEntityBlock {
    public static final IntegerProperty X_POS = IntegerProperty.create("x_offset", 0, 3);
    public static final IntegerProperty Y_POS = IntegerProperty.create("y_offset", 0, 3);
    public static final IntegerProperty Z_POS = IntegerProperty.create("z_offset", 0, 3);

    public static final int MAX = RotationSegment.getMaxSegmentIndex();
    private static final int ROTATIONS = MAX + 1;
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;

    public final int xSize, ySize, zSize;
    private final Map<BlockState, VoxelShape> shapesCache;

    public StatueBlock(int xSize, int ySize, int zSize) {
        super(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).noOcclusion());
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
        this.registerDefaultState(this.stateDefinition.any().setValue(ROTATION, 0));
        this.shapesCache = getShapeForEachState(this::makeShape);
    }

    private VoxelShape makeShape(BlockState state) {
        int margin = 2;
        int x = -16 * state.getValue(X_POS) + margin;
        int y = -16 * state.getValue(Y_POS);
        int z = -16 * state.getValue(Z_POS) + margin;
        return Block.box(x, y, z, x + xSize * 16 - margin * 2, y + ySize * 16, z + zSize * 16 - margin * 2);
    }

    public StatueBlock() {
        this(1, 2, 1);
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
     * Multiblock Handling
     * -----------------------------------*/
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        Level level = context.getLevel();
        // find origin pos base on block pos clicked and the player's direction
        BlockPos clickedPos = context.getClickedPos();
        BlockPos.MutableBlockPos originPos = clickedPos.mutable();
        float rotation = Mth.wrapDegrees(context.getRotation());
        Direction clickedFace = context.getClickedFace();
        if (clickedFace == Direction.DOWN) {
            originPos.move(Direction.DOWN, ySize - 1);
        }
        if (clickedFace.getAxis() == Direction.Axis.X ^ rotation > 0) {
            // if looking negative X (0,180] offset x placement to place away from character
            originPos.move(Direction.WEST, xSize - 1);
        }
        if (clickedFace.getAxis() == Direction.Axis.Z ^ Mth.abs(rotation) > 90) {
            // if looking negative Z (-90,-180] U (90,180] offset z placement to place away from character
            originPos.move(Direction.NORTH, zSize - 1);
        }

        // define extents containing all blocks are statue will take up
        BlockBox extents = BlockBox.of(originPos, originPos.offset(xSize - 1, ySize - 1, zSize - 1));
        // check if each block within our extent is a valid location
        for (var pos : extents) {
            if (!(level.getBlockState(pos).canBeReplaced(context) && level.getWorldBorder().isWithinBounds(pos))) {
                return null;
            }
        }
        // solve for relative position, and allow block placement to pass
        int xoff = clickedPos.getX() - originPos.getX();
        int yoff = clickedPos.getY() - originPos.getY();
        int zoff = clickedPos.getZ() - originPos.getZ();
        return this.defaultBlockState()
                .setValue(ROTATION, RotationSegment.convertToSegment(context.getRotation()))
                .setValue(X_POS, xoff)
                .setValue(Y_POS, yoff)
                .setValue(Z_POS, zoff);
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            // find origin pos and rotation
            int originalX = state.getValue(X_POS);
            int originalY = state.getValue(Y_POS);
            int originalZ = state.getValue(Z_POS);
            int originalRotation = state.getValue(ROTATION);
            BlockPos originPos = pos.offset(-originalX, -originalY, -originalZ);
            // from origin pos, fill in extents with statue blocks
            for (int x = 0; x < xSize; x++) {
                for (int y = 0; y < ySize; y++) {
                    for (int z = 0; z < zSize; z++) {
                        if (x == originalX && y == originalY && z == originalZ) continue;
                        BlockPos fillPos = originPos.offset(x, y, z);
                        BlockState fillState = this.defaultBlockState()
                                .setValue(ROTATION, originalRotation)
                                .setValue(X_POS, x)
                                .setValue(Y_POS, y)
                                .setValue(Z_POS, z);
                        level.setBlock(fillPos, fillState, 3);
                        level.blockUpdated(fillPos, Blocks.AIR);
                        if (x == 0 && y == 0 && z == 0 && level.getBlockEntity(pos) instanceof StatueBlockEntity self && level.getBlockEntity(fillPos) instanceof StatueBlockEntity controller) {
                            controller.setControllerFrom(self);
                        }
//                        fillState.updateNeighbourShapes(level, fillPos, 3);
                    }
                }
            }
        }
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return shapesCache.get(state);
    }

    @Override
    public @NotNull BlockState updateShape(BlockState myState, @NotNull Direction pFacing, @NotNull BlockState pFacingState, LevelAccessor pLevel, BlockPos myPos, @NotNull BlockPos pFacingPos) {
        // find original block pos
        int originalX = myState.getValue(X_POS);
        int originalY = myState.getValue(Y_POS);
        int originalZ = myState.getValue(Z_POS);
        // find full statue extents
        BlockPos originPos = myPos.offset(-originalX, -originalY, -originalZ);
        BlockBox extents = BlockBox.of(originPos, originPos.offset(xSize - 1, ySize - 1, zSize - 1));
        // check to make sure our entire statue is still valid
        for (var pos : extents) {
            BlockState neighborState = pLevel.getBlockState(pos);
            if (!neighborState.is(this)) {
                // statue is not valid, destroy self
                //manually set to prevent block from dropping
                var air = Blocks.AIR.defaultBlockState();
                pLevel.setBlock(myPos, air, Block.UPDATE_SUPPRESS_DROPS | Block.UPDATE_ALL);
                BlockPos particlePos = originPos.offset(originalX * 2, originalY * 2, originalZ * 2); // i have no clue why this seems to fix the particles being in the wrong spot, but ok
                pLevel.levelEvent(null, LevelEvent.PARTICLES_DESTROY_BLOCK, particlePos, Block.getId(myState));
                return air;
            }
        }
        return super.updateShape(myState, pFacing, pFacingState, pLevel, myPos, pFacingPos);
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
                // todo: ensure we cant set statue name to what it already is
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

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (player.isCrouching()) {
            MinecraftInstanceHelper.instance.openStatueScreen(pos);
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
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
        builder.add(ROTATION, X_POS, Y_POS, Z_POS);
    }

    /* ----------------------------------- *
     * Rendering
     * -----------------------------------*/
    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
}
