package io.redspace.ironsspellbooks.gui.inscription_table;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.block.inscription_table.InscriptionTableTile;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.MenuRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import static io.redspace.ironsspellbooks.registries.BlockRegistry.INSCRIPTION_TABLE_BLOCK;

public class InscriptionTableMenu extends AbstractContainerMenu {
    public final InscriptionTableTile blockEntity;
    private final Level level;
    private final Slot spellBookSlot;
    private final Slot scrollSlot;
    private final Slot resultSlot;
    private int selectedSpellIndex = -1;

    public InscriptionTableMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level.getBlockEntity(extraData.readBlockPos()));
    }


    public InscriptionTableMenu(int containerId, Inventory inv, BlockEntity entity) {
        //exists on server and render
        super(MenuRegistry.INSCRIPTION_TABLE_MENU.get(), containerId);
        checkContainerSize(inv, 3);
        blockEntity = (InscriptionTableTile) entity;
        this.level = inv.player.level;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
        IItemHandler itemHandler = this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().get();

        spellBookSlot = new SlotItemHandler(itemHandler, 0, 17, 21);
        scrollSlot = new SlotItemHandler(itemHandler, 1, 17, 53);
        resultSlot = new SlotItemHandler(itemHandler, 2, 208, 136) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                IronsSpellbooks.LOGGER.debug("InscriptionTableMenu.take spell!");
                var spellBookData = ((SpellBook) spellBookSlot.getItem().getItem()).getSpellBookData(spellBookSlot.getItem());
                spellBookData.removeSpell(selectedSpellIndex);
                super.onTake(player, stack);
            }
        };

        this.addSlot(spellBookSlot);
        this.addSlot(scrollSlot);
        this.addSlot(resultSlot);
//        this.blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handler -> {
//            this.addSlot(new SlotItemHandler(handler, 0, 17, 21));
//            this.addSlot(new SlotItemHandler(handler, 1, 17, 53));
//            this.addSlot(new ScrollExtractionSlot(handler, 2, 208, 136));
//        });
    }

    public Slot getSpellBookSlot() {
        return spellBookSlot;
    }


    public Slot getScrollSlot() {
        return scrollSlot;
    }

    public Slot getResultSlot() {
        return resultSlot;
    }

    public void onSlotsChanged() {
        IronsSpellbooks.LOGGER.debug("InscriptionTableMenu.slotsChanged");
        setupResultSlot();
    }

    public void setSelectedSpell(int index) {
        selectedSpellIndex = index;
        setupResultSlot();
    }

    private void setupResultSlot() {
        IronsSpellbooks.LOGGER.debug("InscriptionTableMenu.setupResultSlot");
        IronsSpellbooks.LOGGER.debug("InscriptionTableMenu.selected spell index: {}", selectedSpellIndex);

        ItemStack resultStack = ItemStack.EMPTY;
        if (spellBookSlot.getItem().getItem() instanceof SpellBook spellBook) {
            if(!spellBook.isUnique()){
                var spellBookData = spellBook.getSpellBookData(spellBookSlot.getItem());
                if (selectedSpellIndex >= 0 && spellBookData.getSpell(selectedSpellIndex) != null) {
                    resultStack = new ItemStack(ItemRegistry.SCROLL.get());
                    resultStack.setCount(1);
                    Scroll scroll = (Scroll) resultStack.getItem();
                    var scrollData = scroll.getScrollData(resultStack);
                    scrollData.setData(spellBookData.getSpell(selectedSpellIndex));
                }
            }
        }

        if (!ItemStack.matches(resultStack, this.resultSlot.getItem())) {
            this.resultSlot.set(resultStack);
        }

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

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                pPlayer, INSCRIPTION_TABLE_BLOCK.get());
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
