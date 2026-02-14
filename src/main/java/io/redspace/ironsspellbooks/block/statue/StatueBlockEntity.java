package io.redspace.ironsspellbooks.block.statue;

import io.redspace.ironsspellbooks.patreon.statue.StatueItemData;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
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

    public StatueBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockRegistry.STATUE_BLOCK_ENTITY.get(), pos, blockState);
    }

    /*----------------------------------
     * State Fields
     *----------------------------------*/
    @Nullable
    private UUID playerUuid;

    @Nullable
    public UUID getPlayerUuid() {
        StatueBlockEntity statue = getPrimaryController();
        return statue == null ? null : statue.playerUuid;
    }

    public void setPlayerUuid(@Nullable UUID playerUuid) {
        getPrimaryControllerOpt().ifPresent(statue -> statue.playerUuid = playerUuid);
    }

    public void setControllerFrom(StatueBlockEntity other) {
        this.playerUuid = other.playerUuid;
        other.playerUuid = null;
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
        if (playerUuid != null) {
            tag.putUUID("playerUuid", playerUuid);
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("playerUuid")) {
            this.playerUuid = tag.getUUID("playerUuid");
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
        StatueItemData data = componentInput.get(ComponentRegistry.STATUE_ITEM_DATA.get());
        if (data != null) {
            // set data raw
            // this happens in the middle of the placement flow, so we need this data for later, even if we are not the primary controller
            this.playerUuid = data.uuid();
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.@NotNull Builder components) {
        UUID uuid = getPlayerUuid();
        if (uuid != null) {
            components.set(ComponentRegistry.STATUE_ITEM_DATA.get(), new StatueItemData(uuid));
        }
    }
}
