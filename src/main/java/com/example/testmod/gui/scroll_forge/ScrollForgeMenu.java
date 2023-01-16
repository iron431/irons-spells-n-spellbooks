package com.example.testmod.gui.scroll_forge;

import com.example.testmod.TestMod;
import com.example.testmod.block.scroll_forge.ScrollForgeTile;
import com.example.testmod.gui.inscription_table.ScrollExtractionSlot;
import com.example.testmod.item.Scroll;
import com.example.testmod.registries.ItemRegistry;
import com.example.testmod.registries.MenuRegistry;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SchoolType;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.ModTags;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.testmod.registries.BlockRegistry.SCROLL_FORGE_BLOCK;
import static net.minecraft.world.item.Items.*;

public class ScrollForgeMenu extends AbstractContainerMenu {
    public final ScrollForgeTile blockEntity;
    private final Level level;

    public ScrollForgeMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level.getBlockEntity(extraData.readBlockPos()));
    }

    private final Slot inkSlot;
    private final Slot blankScrollSlot;
    private final Slot focusSlot;
    private final Slot resultSlot;

    private SpellType spellRecipeSelection = SpellType.NONE_SPELL;

    //private List<SpellCardInfo> spellCards;

    public ScrollForgeMenu(int containerId, Inventory inv, BlockEntity entity) {
        //exists on server and render
        super(MenuRegistry.SCROLL_FORGE_MENU.get(), containerId);
        checkContainerSize(inv, 4);
        blockEntity = (ScrollForgeTile) entity;
        this.level = inv.player.level;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
        IItemHandler itemHandler = this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().get();

        inkSlot = new SlotItemHandler(itemHandler, 0, 12, 17);
        blankScrollSlot = new SlotItemHandler(itemHandler, 1, 35, 17);
        focusSlot = new SlotItemHandler(itemHandler, 2, 58, 17);
        resultSlot = new SlotItemHandler(itemHandler, 3, 35, 47) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                inkSlot.remove(1);
                blankScrollSlot.remove(1);
                focusSlot.remove(1);
                super.onTake(player, stack);
            }
        };

        this.addSlot(inkSlot);
        this.addSlot(blankScrollSlot);
        this.addSlot(focusSlot);
        this.addSlot(resultSlot);

    }

    public void onSlotsChanged() {
        TestMod.LOGGER.debug("{}", this.hashCode());

        setupResultSlot(spellRecipeSelection);
        TestMod.LOGGER.debug("ScrollForgeMenu.slotsChanged");
    }

    private void setupResultSlot(SpellType selectedSpellType) {
        ItemStack scrollStack = this.blankScrollSlot.getItem();
        ItemStack inkStack = this.inkSlot.getItem();
        ItemStack focusStack = this.focusSlot.getItem();
        ItemStack resultStack = ItemStack.EMPTY;
        if (!scrollStack.isEmpty() && !inkStack.isEmpty() && !focusStack.isEmpty() && selectedSpellType != SpellType.NONE_SPELL) {
            //if (scrollStack.is(PAPER) && inkStack.is(INK_SAC) && focusStack.is(BLAZE_POWDER)) {
            resultStack = new ItemStack(ItemRegistry.SCROLL.get());
            resultStack.setCount(1);
            Scroll scroll = (Scroll) resultStack.getItem();
            var scrollData = scroll.getScrollData(resultStack);
            scrollData.setData(selectedSpellType.getValue(), 1);
            //}
        }

        if (!ItemStack.matches(resultStack, this.resultSlot.getItem())) {
            this.resultSlot.set(resultStack);
        }

    }

    public void setRecipeSpell(SpellType typeFromValue) {
        this.spellRecipeSelection = typeFromValue;
        setupResultSlot(typeFromValue);
    }

    public Slot getInkSlot() {
        return inkSlot;
    }

    public Slot getBlankScrollSlot() {
        return blankScrollSlot;
    }

    public Slot getFocusSlot() {
        return focusSlot;
    }

    public Slot getResultSlot() {
        return resultSlot;
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
    private static final int TE_INVENTORY_SLOT_COUNT = 4;  // must be the number of slots you have!

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

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                pPlayer, SCROLL_FORGE_BLOCK.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18 + 21, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18 + 21, 142));
        }
    }


//    private class SpellCardInfo {
//        public SpellType spell;
//        public int index;
//        public Button button;
//
//        SpellCardInfo(SpellType spell, int index, Button button) {
//            this.spell = spell;
//            this.index = index;
//            this.button = button;
//        }
//    }
}
