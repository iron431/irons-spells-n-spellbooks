package io.redspace.ironsspellbooks.block.scroll_forge;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.gui.scroll_forge.ScrollForgeMenu;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
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

    private final LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.of(() -> itemHandler);

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

//    @Override
//    public void onLoad() {
//        super.onLoad();
//        lazyItemHandler = LazyOptional.of(() -> itemHandler);
//        irons_spellbooks.LOGGER.debug("ScrollForgeTile.chunkOnLoad: {}", itemHandler.getSlots());
//
//    }


    public void drops() {
        SimpleContainer simpleContainer
                = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots() - 1; i++) {
            simpleContainer.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, simpleContainer);
    }

    @Override
    public void load(CompoundTag nbt) {
        //irons_spellbooks.LOGGER.debug("ScrollForgeTile.loadingFromNBT: {}", nbt);
        super.load(nbt);
        if (nbt.contains("inventory")) {
            itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        lazyItemHandler.invalidate();
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        //irons_spellbooks.LOGGER.debug("saveAdditional tag:{}", tag);
        tag.put("inventory", itemHandler.serializeNBT());
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.put("inventory", itemHandler.serializeNBT());
        //irons_spellbooks.LOGGER.debug("getUpdateTag tag:{}", tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        var packet = ClientboundBlockEntityDataPacket.create(this);
        //irons_spellbooks.LOGGER.debug("getUpdatePacket: packet.getTag:{}", packet.getTag());
        return packet;
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

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @javax.annotation.Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

//    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, InscriptionTableTile pBlockEntity) {
//        if(hasRecipe(pBlockEntity) && hasNotReachedStackLimit(pBlockEntity)) {
//            craftItem(pBlockEntity);
//        }
//    }
//
//    private static void craftItem(GemCuttingStationBlockEntity entity) {
//        entity.itemHandler.extractItem(0, 1, false);
//        entity.itemHandler.extractItem(1, 1, false);
//        entity.itemHandler.getStackInSlot(2).hurt(1, new Random(), null);
//
//        entity.itemHandler.setStackInSlot(3, new ItemStack(ModItems.CITRINE.get(),
//                entity.itemHandler.getStackInSlot(3).getCount() + 1));
//    }
//
//    private static boolean hasRecipe(GemCuttingStationBlockEntity entity) {
//        boolean hasItemInWaterSlot = PotionUtils.getPotion(entity.itemHandler.getStackInSlot(0)) == Potions.WATER;
//        boolean hasItemInFirstSlot = entity.itemHandler.getStackInSlot(1).getItem() == ModItems.RAW_CITRINE.get();
//        boolean hasItemInSecondSlot = entity.itemHandler.getStackInSlot(2).getItem() == ModItems.GEM_CUTTER_TOOL.get();
//
//        return hasItemInWaterSlot && hasItemInFirstSlot && hasItemInSecondSlot;
//    }
//
//    private static boolean hasNotReachedStackLimit(GemCuttingStationBlockEntity entity) {
//        return entity.itemHandler.getStackInSlot(3).getCount() < entity.itemHandler.getStackInSlot(3).getMaxStackSize();
//    }
}
