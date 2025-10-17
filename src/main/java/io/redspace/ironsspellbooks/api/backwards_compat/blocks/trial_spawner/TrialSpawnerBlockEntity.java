package io.redspace.ironsspellbooks.api.backwards_compat.blocks.trial_spawner;

import com.mojang.logging.LogUtils;
import io.redspace.ironsspellbooks.api.backwards_compat.blocks.trial_spawner.spawning.PlayerDetector;
import io.redspace.ironsspellbooks.api.backwards_compat.blocks.trial_spawner.spawning.TrialSpawner;
import io.redspace.ironsspellbooks.api.backwards_compat.blocks.trial_spawner.spawning.TrialSpawnerState;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class TrialSpawnerBlockEntity extends BlockEntity implements /*Spawner,*/ TrialSpawner.StateAccessor {
    private static final Logger LOGGER = LogUtils.getLogger();
    private TrialSpawner trialSpawner;

    public TrialSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockRegistry.TRIAL_SPAWNER_BLOCK_ENTITY.get(), pos, state);
        PlayerDetector playerdetector = PlayerDetector.NO_CREATIVE_PLAYERS;
        PlayerDetector.EntitySelector playerdetector$entityselector = PlayerDetector.EntitySelector.SELECT_FROM_LEVEL;
        this.trialSpawner = new TrialSpawner(this, playerdetector, playerdetector$entityselector);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("normal_config")) {
            CompoundTag compoundtag = tag.getCompound("normal_config").copy();
            tag.put("ominous_config", compoundtag.merge(tag.getCompound("ominous_config")));
        }

        this.trialSpawner.codec().parse(NbtOps.INSTANCE, tag).resultOrPartial(LOGGER::error).ifPresent(p_311911_ -> this.trialSpawner = p_311911_);
        if (this.level != null) {
            this.markUpdated();
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        this.trialSpawner
                .codec()
                .encodeStart(NbtOps.INSTANCE, this.trialSpawner)
                .get()
                .ifLeft(p_312175_ -> tag.merge((CompoundTag) p_312175_))
                .ifRight(p_338001_ -> LOGGER.warn("Failed to encode TrialSpawner {}", p_338001_.message()))
            /*.ifSuccess(p_312175_ -> tag.merge((CompoundTag)p_312175_))
            .ifError(p_338001_ -> LOGGER.warn("Failed to encode TrialSpawner {}", p_338001_.message()))*/;
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.trialSpawner.getData().getUpdateTag(this.getBlockState().getValue(TrialSpawnerBlock.STATE));
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    // Implementation of 1.21.1 `Spawner` interface. Seems to only be used by code-based structure placement.
//    @Override
//    public void setEntityId(EntityType<?> entityType, RandomSource random) {
//        this.trialSpawner.getData().setEntityId(this.trialSpawner, random, entityType);
//        this.setChanged();
//    }

    public TrialSpawner getTrialSpawner() {
        return this.trialSpawner;
    }

    @Override
    public TrialSpawnerState getState() {
        return !this.getBlockState().hasProperty(TrialSpawnerBlock.STATE)
                ? TrialSpawnerState.INACTIVE
                : this.getBlockState().getValue(TrialSpawnerBlock.STATE);
    }

    @Override
    public void setState(Level level, TrialSpawnerState state) {
        this.setChanged();
        level.setBlockAndUpdate(this.worldPosition, this.getBlockState().setValue(TrialSpawnerBlock.STATE, state));
    }

    @Override
    public void markUpdated() {
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }
}
