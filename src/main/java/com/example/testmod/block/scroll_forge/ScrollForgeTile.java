package com.example.testmod.block.scroll_forge;

import com.example.testmod.TestMod;
import com.example.testmod.gui.scroll_forge.ScrollForgeMenu;
import com.example.testmod.item.Scroll;
import com.example.testmod.item.SpellBook;
import com.example.testmod.registries.BlockRegistry;
import com.example.testmod.registries.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
            updateMenuSlots();
            setChanged();
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    private void updateMenuSlots() {
        if (menu != null)
            menu.onSlotsChanged();
    }

    public ScrollForgeTile(BlockPos pWorldPosition, BlockState pBlockState) {
        super(BlockRegistry.SCROLL_FORGE_TILE.get(), pWorldPosition, pBlockState);
    }

    @Override
    public MutableComponent getDisplayName() {
        return Component.literal("Scroll Forge");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        menu = new ScrollForgeMenu(containerId, inventory, this);
        return menu;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @javax.annotation.Nullable Direction side) {
        TestMod.LOGGER.debug("ScrollForgeTile: getting ItemHandler Capability");
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            TestMod.LOGGER.debug("ScrollForgeTile: size: {}", itemHandler.getSlots());

            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }


    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots() - 1; i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
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
