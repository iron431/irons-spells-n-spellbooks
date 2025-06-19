package io.redspace.ironsspellbooks.block.portal_frame;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
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
                .isValidSpawn(Blocks::never)
                .noTerrainParticles()
                .pushReaction(PushReaction.BLOCK)
                .sound(SoundType.COPPER_GRATE));
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        // prevent dying interactions
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public boolean canTeleport(Entity entity) {
        return entity instanceof Player;
    }

    public static final MapCodec<PocketDimensionPortalFrameBlock> CODEC = simpleCodec((t) -> new PocketDimensionPortalFrameBlock());

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

}
