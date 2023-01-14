package com.example.testmod.gui.scroll_forge;

import com.example.testmod.block.scroll_forge.ScrollForgeTile;
import com.example.testmod.item.Scroll;
import com.example.testmod.registries.BlockRegistry;
import com.example.testmod.registries.ItemRegistry;
import com.example.testmod.registries.MenuRegistry;
import com.example.testmod.spells.SpellType;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.List;

public class ScrollForgeMenu extends AbstractContainerMenu {
    public final ScrollForgeTile blockEntity;
    private final Level level;

    private final Slot inkSlot;
    private final Slot blankScrollSlot;
    private final Slot focusSlot;
    private final Slot resultSlot;

    Runnable slotUpdateListener = () -> {
    };

    private final Container inputContainer = new SimpleContainer(3) {
        /**
         * For block entities, ensures the chunk containing the block entity is saved to disk later - the game won't think
         * it hasn't changed and skip it.
         */
        public void setChanged() {
            super.setChanged();
            ScrollForgeMenu.this.slotsChanged(this);
            ScrollForgeMenu.this.slotUpdateListener.run();
        }
    };
    private final Container outputContainer = new SimpleContainer(1) {
        /**
         * For block entities, ensures the chunk containing the block entity is saved to disk later - the game won't think
         * it hasn't changed and skip it.
         */
        public void setChanged() {
            super.setChanged();
            ScrollForgeMenu.this.slotUpdateListener.run();
        }
    };

    public ScrollForgeMenu(int containerId, Inventory inv, BlockEntity entity) {
        super(MenuRegistry.SCROLL_FORGE_MENU.get(), containerId);

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        inkSlot = this.addSlot(new Slot(this.inputContainer, 0, 0, 0));
        focusSlot = this.addSlot(new Slot(this.inputContainer, 1, 20, 0));
        blankScrollSlot = this.addSlot(new Slot(this.inputContainer, 2, 40, 0));
        resultSlot = this.addSlot(new Slot(this.outputContainer, 0, 60, 0));

        this.level = inv.player.getLevel();
        this.blockEntity = (ScrollForgeTile) entity;
    }

    public ScrollForgeMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level.getBlockEntity(extraData.readBlockPos()));
    }

    @Override
    public void slotsChanged(Container pInventory) {
        setupResultSlot(SpellType.FIREBALL_SPELL);
        super.slotsChanged(pInventory);
    }

    private void setupResultSlot(SpellType selectedSpellType) {
        ItemStack scrollStack = this.blankScrollSlot.getItem();
        ItemStack inkStack = this.inkSlot.getItem();
        ItemStack resultStack = ItemStack.EMPTY;
        if (!scrollStack.isEmpty() && !inkStack.isEmpty()) {
            resultStack = new ItemStack(ItemRegistry.SCROLL.get());
            resultStack.setCount(1);
            Scroll scroll = (Scroll) scrollStack.getItem();
            var scrollData = scroll.getScrollData(resultStack);
            scrollData.setData(selectedSpellType.getValue(), 1);
        }

        if (!ItemStack.matches(resultStack, this.resultSlot.getItem())) {
            this.resultSlot.set(resultStack);
        }

    }

    public Slot getResultSlot() {
        return this.resultSlot;
    }

    //TODO: register me in screen (see loom)
    public void registerUpdateListener(Runnable pListener) {
        this.slotUpdateListener = pListener;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, BlockRegistry.SCROLL_FORGE_BLOCK.get());
    }

    // CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
    // must assign a slot number to each of the slots used by the GUI.
    // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
    // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
    //  0 - 8 = hotbar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
    //  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
    //  36 - 44 = TileInventory slots, which map to our TileEntity slot numbers 0 - 8)
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    // THIS YOU HAVE TO DEFINE!
    private static final int TE_INVENTORY_SLOT_COUNT = 3;  // must be the number of slots you have!

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;  //EMPTY_ITEM
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (index < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // This is a vanilla container slot so merge the stack into the tile inventory
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;  // EMPTY_ITEM
            }
        } else if (index < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            // This is a TE slot so merge the stack into the players inventory
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.out.println("Invalid slotIndex:" + index);
            return ItemStack.EMPTY;
        }
        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}
