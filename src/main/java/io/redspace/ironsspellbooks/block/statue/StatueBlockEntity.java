package io.redspace.ironsspellbooks.block.statue;

import io.redspace.ironsspellbooks.patreon.statue.PlayerStatuePose;
import io.redspace.ironsspellbooks.patreon.statue.StatueData;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import net.minecraft.core.BlockBox;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class StatueBlockEntity extends BlockEntity {

    public static StatueBlockEntity renderable(StatueData statueData) {
        StatueBlockEntity fakeBlock = new StatueBlockEntity(BlockPos.ZERO, BlockRegistry.PLAYER_STATUE_BLOCK.get().defaultBlockState());
        fakeBlock.statueData = statueData;
        return fakeBlock;
    }

    public StatueBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockRegistry.STATUE_BLOCK_ENTITY.get(), pos, blockState);
    }

    /*----------------------------------
     * State Fields
     *----------------------------------*/
//    @Nullable
//    private UUID playerUuid;
//    @NotNull
//    private PlayerStatuePose pose = PlayerStatuePose.DEFAULT;
    @Nullable
    private StatueData statueData = null;

//    @Nullable
//    public UUID getPlayerUuid() {
//        StatueBlockEntity statue = getPrimaryController();
//        return statue == null ? null : statue.playerUuid;
//    }
//
//    public void setPlayerUuid(@Nullable UUID playerUuid) {
//        getPrimaryControllerOpt().ifPresent(statue -> statue.setControllerUUID(playerUuid));
//    }
//
//    private void setControllerUUID(@Nullable UUID playerUuid) {
//        this.playerUuid = playerUuid;
//        // propagate to children
//        if (level == null || !(this.getBlockState().getBlock() instanceof StatueBlock statueBlock)) return;
//        for (BlockPos childPos : BlockBox.of(this.getBlockPos(), this.getBlockPos().offset(statueBlock.xSize, statueBlock.ySize, statueBlock.zSize))) {
//            if (!(level.getBlockEntity(childPos) instanceof StatueBlockEntity child)) continue;
//            child.playerUuid = this.playerUuid;
//            setChanged(this.level, childPos, level.getBlockState(childPos));
//        }
//    }

    @Nullable
    public StatueData getStatueData() {
        StatueBlockEntity statue = getPrimaryController();
        return statue == null ? null : statue.statueData;
    }

    public void setStatueData(@Nullable StatueData statueData) {
        getPrimaryControllerOpt().ifPresent(statue -> statue.setControllerStatueData(statueData));
    }

    private void setControllerStatueData(@Nullable StatueData statueData) {
        this.statueData = statueData;
        // propagate to children
        if (level == null || !(this.getBlockState().getBlock() instanceof StatueBlock statueBlock)) return;
        for (BlockPos childPos : BlockBox.of(this.getBlockPos(), this.getBlockPos().offset(statueBlock.xSize, statueBlock.ySize, statueBlock.zSize))) {
            if (!(level.getBlockEntity(childPos) instanceof StatueBlockEntity child)) continue;
            child.statueData = this.statueData;
            setChanged(this.level, childPos, level.getBlockState(childPos));
        }
    }

    public void setControllerFrom(StatueBlockEntity other) {
        setControllerStatueData(other.statueData);
    }

    public void setPlayerUuid(UUID uuid) {
        if (this.statueData == null) {
            setStatueData(new StatueData(uuid, PlayerStatuePose.defaultPose()));
        } else {
            setStatueData(new StatueData(uuid, this.statueData.pose()));
        }
    }

    /*----------------------------------
     * Multiblock Handling
     *----------------------------------*/
    public boolean isPrimary() {
        var state = this.getBlockState();
        return state.getValue(StatueBlock.X_POS) == 0 && state.getValue(StatueBlock.Y_POS) == 0 && state.getValue(StatueBlock.Z_POS) == 0;
    }

    public @NotNull Optional<StatueBlockEntity> getPrimaryControllerOpt() {
        return Optional.ofNullable(getPrimaryController());
    }

    @Nullable
    public StatueBlockEntity getPrimaryController() {
        var state = this.getBlockState();
        int xPos = state.getValue(StatueBlock.X_POS);
        int yPos = state.getValue(StatueBlock.Y_POS);
        int zPos = state.getValue(StatueBlock.Z_POS);
        if (xPos == 0 && yPos == 0 && zPos == 0) {
            return this;
        } else if (level != null && level.getBlockEntity(this.getBlockPos().offset(-xPos, -yPos, -zPos)) instanceof StatueBlockEntity statueBlock) {
            return statueBlock;
        }
        return null;
    }

    /*----------------------------------
     * Client Syncing and Serialization
     *----------------------------------*/
    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide) {
            var state = this.getBlockState();
            level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        if (statueData != null) {
            tag.putUUID("playerUuid", statueData.uuid());
            tag.putString("pose", statueData.pose().getSerializedName());
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("playerUuid")) {
            UUID uuid = tag.getUUID("playerUuid");
            PlayerStatuePose pose = PlayerStatuePose.defaultPose();
            if (tag.contains("pose")) {
                pose = PlayerStatuePose.fromString(tag.getString("pose"));
            }
            this.statueData = new StatueData(uuid, pose);
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        var tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void applyImplicitComponents(@NotNull DataComponentInput componentInput) {
        StatueData data = componentInput.get(ComponentRegistry.STATUE_ITEM_DATA.get());
        if (data != null) {
            this.statueData = data;
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.@NotNull Builder components) {
        if (this.statueData != null) {
            components.set(ComponentRegistry.STATUE_ITEM_DATA.get(), statueData);
        }
    }
}
