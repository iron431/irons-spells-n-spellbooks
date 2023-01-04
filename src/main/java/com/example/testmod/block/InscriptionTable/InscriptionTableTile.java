package com.example.testmod.block.InscriptionTable;

import com.example.testmod.TestMod;
import com.example.testmod.gui.InscriptionTableMenu;
import com.example.testmod.gui.InscriptionTableScreen;
import com.example.testmod.item.SpellBook;
import com.example.testmod.item.Scroll;
import com.example.testmod.registries.BlockRegistry;
import com.example.testmod.registries.ItemRegistry;
import com.example.testmod.spells.SpellType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;


public class InscriptionTableTile extends BlockEntity implements MenuProvider {
    private AbstractContainerMenu menu;

    public static final int SPELLBOOK_SLOT = 36 + 0;
    public static final int SCROLL_SLOT = 36 + 1;
    public static final int EXTRACTION_SLOT = 36 + 2;

    private final ItemStackHandler itemHandler = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public InscriptionTableTile(BlockPos pWorldPosition, BlockState pBlockState) {
        super(BlockRegistry.INSCRIPTION_TABLE_TILE.get(), pWorldPosition, pBlockState);
    }

    @Override
    public Component getDisplayName() {
        return new TextComponent("Inscription Table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        menu = new InscriptionTableMenu(containerId, inventory, this);
        return menu;
    }

    public void doInscription(int selectedIndex) {
        // This method is called by the inscription packet
        var slots = this.menu.slots;

        ItemStack spellBookItemStack = slots.get(SPELLBOOK_SLOT).getItem();
        ItemStack scrollItemStack = slots.get(SCROLL_SLOT).getItem();

        if (spellBookItemStack.getItem() instanceof SpellBook && scrollItemStack.getItem() instanceof Scroll) {
            var spellBook = (SpellBook) spellBookItemStack.getItem();
            var scroll = (Scroll) scrollItemStack.getItem();

            var spellBookData = spellBook.getSpellBookData(spellBookItemStack);
            var scrollData = scroll.getScrollData(scrollItemStack);

            if (spellBookData.addSpell(scrollData.getSpell(), selectedIndex))
                slots.get(SCROLL_SLOT).remove(1);
        }
    }

    public void removeSelectedSpell(int selectedIndex) {
        // All data should have been validated by now
        TestMod.LOGGER.info("recieving request to destroy");
        var slots = this.menu.slots;

        ItemStack spellBookItemStack = slots.get(SPELLBOOK_SLOT).getItem();
        SpellBook spellBook = (SpellBook) spellBookItemStack.getItem();
        var spellBookData = spellBook.getSpellBookData(spellBookItemStack);

        var spellId = spellBookData.getInscribedSpells()[selectedIndex].getID();
        var spellLevel = spellBookData.getInscribedSpells()[selectedIndex].getLevel();
        generateScroll(spellId, spellLevel);
        spellBookData.removeSpell(selectedIndex);

    }

    public void generateScroll(int spellId, int spellLevel) {
        // This method is called by the generate scroll packet

        Slot extractionSlot = this.menu.slots.get(EXTRACTION_SLOT);

        //null check (empty spell slots send as -1)
        //if (spellId >= 0) {
        ItemStack scrollStack = new ItemStack(ItemRegistry.SCROLL.get());
        Scroll scroll = (Scroll) scrollStack.getItem();
        scroll.setSpellType(SpellType.values()[spellId]);
        scroll.setLevel(spellLevel);
        extractionSlot.set(scrollStack);
        //} else {
        //    extractionSlot.set(ItemStack.EMPTY);
        //}

    }


    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @javax.annotation.Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
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
        for (int i = 0; i < itemHandler.getSlots(); i++) {
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
