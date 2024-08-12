package io.redspace.ironsspellbooks.block.portal_frame;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class PortalFrameBlock extends BaseEntityBlock {
    public PortalFrameBlock() {
        super(Properties.of());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, BlockHitResult pHitResult) {
        if (!pLevel.isClientSide)
            if (pLevel.getBlockEntity(pPos) instanceof PortalFrameBlockEntity portalFrame) {
                portalFrame.teleport(pPlayer);
            }
        return super.useWithoutItem(pState, pLevel, pPos, pPlayer, pHitResult);
    }

    public static final MapCodec<PortalFrameBlock> CODEC = simpleCodec((t) -> new PortalFrameBlock());

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PortalFrameBlockEntity(pPos, pState);
    }

    @Override
    protected void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        if (pLevel.getBlockEntity(pPos) instanceof PortalFrameBlockEntity portalFrame) {
            portalFrame.breakPortalConnection();
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    @Override
    public @Nullable PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.IGNORE;
    }

    //
//    @Override
//    protected void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
//        super.tick(pState, pLevel, pPos, pRandom);
//        if (((PortalFrameBlockEntity) pLevel.getBlockEntity(pPos)).isPortalConnected()) {
//            MagicManager.spawnParticles(pLevel, ParticleHelper.UNSTABLE_ENDER, pPos.getX(), pPos.getY(), pPos.getZ(), 1, 0.75, 0.75, 0.75, 0.05, true);
//        }
//    }
}
