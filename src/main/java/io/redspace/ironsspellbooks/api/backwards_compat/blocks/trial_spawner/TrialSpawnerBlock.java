package io.redspace.ironsspellbooks.api.backwards_compat.blocks.trial_spawner;

import io.redspace.ironsspellbooks.api.backwards_compat.blocks.trial_spawner.spawning.TrialSpawnerState;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nullable;

public class TrialSpawnerBlock extends BaseEntityBlock {
//    public static final EnumProperty<VaultState> VAULT_STATE = EnumProperty.create("vault_state", VaultState.class);

    public static final EnumProperty<TrialSpawnerState> STATE = EnumProperty.create("trial_spawner_state", TrialSpawnerState.class);
    public static final BooleanProperty OMINOUS = BooleanProperty.create("ominous");


    public TrialSpawnerBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .lightLevel(p_311743_ -> p_311743_.getValue(TrialSpawnerBlock.STATE).lightLevel())
                .strength(50.0F)
                .sound(new SoundType(
                        1.0F, 1.0F, SoundRegistry.TRIAL_SPAWNER_BREAK.get(), SoundRegistry.TRIAL_SPAWNER_STEP.get(), SoundRegistry.TRIAL_SPAWNER_PLACE.get(), SoundRegistry.TRIAL_SPAWNER_BREAK.get(), SoundRegistry.TRIAL_SPAWNER_FALL.get()
                ))
//                .isViewBlocking(Blocks::never)
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(STATE, TrialSpawnerState.INACTIVE).setValue(OMINOUS, Boolean.valueOf(false)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_312785_) {
        p_312785_.add(STATE, OMINOUS);
    }

    @Override
    public RenderShape getRenderShape(BlockState p_312710_) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_311941_, BlockState p_312821_) {
        return new TrialSpawnerBlockEntity(p_311941_, p_312821_);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_311756_, BlockState p_312797_, BlockEntityType<T> p_312122_) {
        return p_311756_ instanceof ServerLevel serverlevel
                ? createTickerHelper(
                p_312122_,
                BlockRegistry.TRIAL_SPAWNER_BLOCK_ENTITY.get(),
                (p_337976_, p_337977_, p_337978_, p_337979_) -> p_337979_.getTrialSpawner()
                        .tickServer(serverlevel, p_337977_, p_337978_.getOptionalValue(TrialSpawnerBlock.OMINOUS).orElse(false))
        )
                : createTickerHelper(
                p_312122_,
                BlockRegistry.TRIAL_SPAWNER_BLOCK_ENTITY.get(),
                (p_337980_, p_337981_, p_337982_, p_337983_) -> p_337983_.getTrialSpawner()
                        .tickClient(p_337980_, p_337981_, p_337982_.getOptionalValue(TrialSpawnerBlock.OMINOUS).orElse(false))
        );
    }

//    @Override
//    public void appendHoverText(ItemStack p_312446_, Item.TooltipContext p_339621_, List<Component> p_312088_, TooltipFlag p_311895_) {
//        super.appendHoverText(p_312446_, p_339621_, p_312088_, p_311895_);
//        Spawner.appendHoverText(p_312446_, p_312088_, "spawn_data");
//    }
}
