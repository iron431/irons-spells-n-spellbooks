package io.redspace.ironsspellbooks.block.pedestal;

import io.redspace.ironsspellbooks.registries.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class PedestalTile extends BlockEntity {
    private static final String NBT_HELD_ITEM = "heldItem";

    private ItemStack heldItem = ItemStack.EMPTY;

    public PedestalTile(BlockPos pWorldPosition, BlockState pBlockState) {
        super(BlockRegistry.PEDESTAL_TILE.get(), pWorldPosition, pBlockState);
    }

    public ItemStack getHeldItem() {
        return heldItem;
    }

    public void setHeldItem(ItemStack newItem) {
        heldItem = newItem;
        setChanged();

    }

    public void drops() {
        SimpleContainer simpleContainer = new SimpleContainer(heldItem);

        Containers.dropContents(this.level, this.worldPosition, simpleContainer);
    }

    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);
        readNBT(pTag, pRegistries);

    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag, HolderLookup.Provider registryAccess) {
        //irons_spellbooks.LOGGER.debug("saveAdditional tag:{}", tag);
        //tag.put("inventory", itemHandler.serializeNBT());
        writeNBT(tag, registryAccess);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        CompoundTag tag = new CompoundTag();
        //tag.put("inventory", itemHandler.serializeNBT());
        writeNBT(tag, pRegistries);
        //irons_spellbooks.LOGGER.debug("getUpdateTag tag:{}", tag);
        return tag;
    }

    @Override
    public boolean triggerEvent(int pId, int pType) {
        return super.triggerEvent(pId, pType);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        var packet = ClientboundBlockEntityDataPacket.create(this);
        //irons_spellbooks.LOGGER.debug("getUpdatePacket: packet.getTag:{}", packet.getTag());
        return packet;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider pRegistries) {
        //irons_spellbooks.LOGGER.debug("onDataPacket: pkt.getTag:{}", pkt.getTag());
        handleUpdateTag(pkt.getTag(), pRegistries);
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider pRegistries) {
        //irons_spellbooks.LOGGER.debug("handleUpdateTag: tag:{}", tag);
        if (tag != null) {
            loadAdditional(tag, pRegistries);
        }
    }

    //    @Nonnull
//    @Override
//    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @javax.annotation.Nullable Direction side) {
//        if (cap == ForgeCapabilities.ITEM_HANDLER) {
//            return lazyItemHandler.cast();
//        }
//
//        return super.getCapability(cap, side);
//    }

    private CompoundTag writeNBT(CompoundTag nbt, HolderLookup.Provider pRegistries) {
        if (!heldItem.isEmpty()) {
            nbt.put(NBT_HELD_ITEM, heldItem.save(pRegistries));
        }
        //irons_spellbooks.LOGGER.debug("getUpdateTag tag:{}", tag);
        return nbt;
    }

    private CompoundTag readNBT(CompoundTag nbt, HolderLookup.Provider pRegistries) {
        if (nbt.contains(NBT_HELD_ITEM)) {
            //itemHandler.deserializeNBT(nbt.getCompound("inventory"));
            //Ironsspellbooks.logger.debug("Pedestal NBT contains held item ({})", nbt.getCompound(NBT_HELD_ITEM));

            //heldItem.deserializeNBT(nbt.getCompound(NBT_HELD_ITEM));
            heldItem = ItemStack.parseOptional(pRegistries, nbt.getCompound(NBT_HELD_ITEM));
            //Ironsspellbooks.logger.debug("Held Item: {}", heldItem);

        }
        return nbt;
    }
}