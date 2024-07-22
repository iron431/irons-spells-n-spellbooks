package io.redspace.ironsspellbooks.block.scroll_forge;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.gui.scroll_forge.ScrollForgeMenu;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class ScrollForgeTile extends BlockEntity implements MenuProvider {
    private ScrollForgeMenu menu;

    private final ItemStackHandler itemHandler = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            updateMenuSlots(slot);
            setChanged();
        }
    };

    public ScrollForgeTile(BlockPos pWorldPosition, BlockState pBlockState) {
        super(BlockRegistry.SCROLL_FORGE_TILE.get(), pWorldPosition, pBlockState);
    }

    private void updateMenuSlots(int slot) {
        if (menu != null) {
            menu.onSlotsChanged(slot);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public ItemStack getStackInSlot(int slot) {
        return itemHandler.getStackInSlot(slot);
    }

    @Override
    public MutableComponent getDisplayName() {
        return Component.translatable("ui.irons_spellbooks.scroll_forge_title");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        //TODO: only allow one player to access at a time?
        menu = new ScrollForgeMenu(containerId, inventory, this);
        return menu;
    }

    public void setRecipeSpell(String spellId) {
        menu.setRecipeSpell(SpellRegistry.getSpell(spellId));
    }

    public void drops() {
        SimpleContainer simpleContainer
                = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots() - 1; i++) {
            simpleContainer.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, simpleContainer);
    }

    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);
        if (pTag.contains("inventory")) {
            itemHandler.deserializeNBT(pRegistries, pTag.getCompound("inventory"));
        }
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag, HolderLookup.Provider registryAccess) {
        //irons_spellbooks.LOGGER.debug("saveAdditional tag:{}", tag);
        tag.put("inventory", itemHandler.serializeNBT(registryAccess));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        CompoundTag tag = new CompoundTag();
        tag.put("inventory", itemHandler.serializeNBT(pRegistries));
        //irons_spellbooks.LOGGER.debug("getUpdateTag tag:{}", tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        loadAdditional(tag, lookupProvider);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        var packet = ClientboundBlockEntityDataPacket.create(this);
        //irons_spellbooks.LOGGER.debug("getUpdatePacket: packet.getTag:{}", packet.getTag());
        return packet;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        handleUpdateTag(pkt.getTag(), lookupProvider);
        //FIXME: 1.21: (not a 1.21 thing: why is the scroll forge implementation so different than the other blocks? do the other blocks need to be fixed up?)
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }
}
