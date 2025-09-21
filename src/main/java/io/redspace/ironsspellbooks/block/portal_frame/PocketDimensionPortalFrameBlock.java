package io.redspace.ironsspellbooks.block.portal_frame;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;

public class PocketDimensionPortalFrameBlock extends PortalFrameBlock {
    public PocketDimensionPortalFrameBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(-1.0F, 3600000.8F)
                .mapColor(MapColor.NONE)
                .noLootTable()
                .noOcclusion()
                .isValidSpawn(PocketDimensionPortalFrameBlock::never)
//                .noTerrainParticles()
                .noParticlesOnBreak()
                .pushReaction(PushReaction.BLOCK)
                .sound(SoundType.COPPER));
    }

    private static Boolean never(BlockState p_50779_, BlockGetter p_50780_, BlockPos p_50781_, EntityType<?> p_50782_) {
        return (boolean) false;
    }

    @Override
    public InteractionResult use(BlockState pState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult pHit) {
        return InteractionResult.PASS;
    }

    @Override
    public boolean canTeleport(Entity entity) {
        return entity instanceof Player;
    }

//    public static final MapCodec<PocketDimensionPortalFrameBlock> CODEC = simpleCodec((t) -> new PocketDimensionPortalFrameBlock());
//
//    @Override
//    protected MapCodec<? extends BaseEntityBlock> codec() {
//        return CODEC;
//    }

}
