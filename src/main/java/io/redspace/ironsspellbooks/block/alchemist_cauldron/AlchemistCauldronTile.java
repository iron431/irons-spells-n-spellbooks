package io.redspace.ironsspellbooks.block.alchemist_cauldron;

import io.redspace.ironsspellbooks.registries.BlockRegistry;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

import java.util.Stack;

import static io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronBlock.MAX_LEVELS;

public class AlchemistCauldronTile extends BlockEntity {
    private final NonNullList<ItemStack> floatingItems = NonNullList.withSize(MAX_LEVELS, ItemStack.EMPTY);
    private final Stack<ItemStack> storedItems = new Stack<>();
    public AlchemistCauldronTile(BlockPos pWorldPosition, BlockState pBlockState) {
        super(BlockRegistry.ALCHEMIST_CAULDRON_TILE.get(), pWorldPosition, pBlockState);
    }

    public boolean addItem(ItemStack newItem) {
        //TODO: figure out how this shit is actually going to work
        if (getLevel() == null || !getLevel().getBlockState(getBlockPos()).hasProperty(AlchemistCauldronBlock.LEVEL))
            return false;
        int level = Math.min(MAX_LEVELS, getLevel().getBlockState(getBlockPos()).getValue(AlchemistCauldronBlock.LEVEL));
        for (int i = 0; i < level; i++) {
            if (floatingItems.get(i).isEmpty()) {
                floatingItems.set(i, newItem);
                setChanged();
                return true;
            }
        }
        return false;
    }

    public int getItemWaterColor(ItemStack itemStack) {
        if (this.getLevel() == null)
            return 0;
        //TODO: ink/potion colors
        return BiomeColors.getAverageWaterColor(this.getLevel(), this.getBlockPos());
    }

    public Stack<ItemStack> getStoredItems(){
        return storedItems;
    }

    public int getAverageWaterColor() {
        //TODO: figure out how this shit is actually going to work
        return getItemWaterColor(null);
    }

    @Override
    public void load(CompoundTag tag) {
        ContainerHelper.loadAllItems(tag, this.floatingItems);
        super.load(tag);
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        ContainerHelper.saveAllItems(tag, this.floatingItems);
        super.saveAdditional(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        //var packet = ClientboundBlockEntityDataPacket.create(this);
        //irons_spellbooks.LOGGER.debug("getUpdatePacket: packet.getTag:{}", packet.getTag());
        CompoundTag nbt = getUpdateTag();
        return ClientboundBlockEntityDataPacket.create(this, (block) -> nbt);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        //irons_spellbooks.LOGGER.debug("onDataPacket: pkt.getTag:{}", pkt.getTag());
        handleUpdateTag(pkt.getTag());
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        //irons_spellbooks.LOGGER.debug("handleUpdateTag: tag:{}", tag);
        if (tag != null) {
            load(tag);
        }
    }
}
