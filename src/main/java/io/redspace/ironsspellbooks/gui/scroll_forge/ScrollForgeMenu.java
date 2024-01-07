package io.redspace.ironsspellbooks.gui.scroll_forge;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.block.scroll_forge.ScrollForgeTile;
import io.redspace.ironsspellbooks.item.InkItem;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.MenuRegistry;
import io.redspace.ironsspellbooks.util.ModTags;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import static io.redspace.ironsspellbooks.registries.BlockRegistry.SCROLL_FORGE_BLOCK;

public class ScrollForgeMenu extends AbstractContainerMenu {
    public final ScrollForgeTile blockEntity;
    private final Level level;

    public ScrollForgeMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    private final Slot inkSlot;
    private final Slot blankScrollSlot;
    private final Slot focusSlot;
    private final Slot resultSlot;

    private AbstractSpell spellRecipeSelection = SpellRegistry.none();

    //private List<SpellCardInfo> spellCards;

    public ScrollForgeMenu(int containerId, Inventory inv, BlockEntity entity) {
        //exists on server and render
        super(MenuRegistry.SCROLL_FORGE_MENU.get(), containerId);
        checkContainerSize(inv, 4);
        blockEntity = (ScrollForgeTile) entity;
        this.level = inv.player.level();

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
        IItemHandler itemHandler = this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().get();

        inkSlot = new SlotItemHandler(itemHandler, 0, 12, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof InkItem;
            }

        };
        blankScrollSlot = new SlotItemHandler(itemHandler, 1, 35, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.PAPER);
            }

        };
        focusSlot = new SlotItemHandler(itemHandler, 2, 58, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModTags.SCHOOL_FOCUS);
            }
        };
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
                level.playSound(null, blockEntity.getBlockPos(), SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.BLOCKS, .8f, 1.1f);
                super.onTake(player, stack);
            }
        };

        this.addSlot(inkSlot);
        this.addSlot(blankScrollSlot);
        this.addSlot(focusSlot);
        this.addSlot(resultSlot);
    }

    public void onSlotsChanged(int slot) {
        if (slot != 3) {
            //3 is the result slot
            setupResultSlot(spellRecipeSelection);
        }
        //IronsSpellbooks.LOGGER.debug("ScrollForgeMenu.slotsChanged {}", slot);
    }

    private void setupResultSlot(AbstractSpell spell) {
        //Ironsspellbooks.logger.debug("ScrollForgeMenu.setupResultSlot");

        ItemStack scrollStack = this.blankScrollSlot.getItem();
        ItemStack inkStack = this.inkSlot.getItem();
        ItemStack focusStack = this.focusSlot.getItem();
        ItemStack resultStack = ItemStack.EMPTY;
        if (!scrollStack.isEmpty() && !inkStack.isEmpty() && !focusStack.isEmpty() && !spell.equals(SpellRegistry.none())&& spell.getSchoolType() == SchoolRegistry.getSchoolFromFocus(focusStack)) {
            if (scrollStack.getItem().equals(Items.PAPER) && inkStack.getItem() instanceof InkItem inkItem) {
                resultStack = new ItemStack(ItemRegistry.SCROLL.get());
                resultStack.setCount(1);
                ISpellContainer.createScrollContainer(spell, spell.getMinLevelForRarity(inkItem.getRarity()), resultStack);
            }
        }

        if (!ItemStack.matches(resultStack, this.resultSlot.getItem())) {
            //IronsSpellbooks.LOGGER.debug("ScrollForgeMenu.setupResultSlot new result: {}", resultStack.getDisplayName().getString());
            if (resultStack.isEmpty()) {
                this.spellRecipeSelection = SpellRegistry.none();
            }
            this.resultSlot.set(resultStack);
        }
    }

    public void setRecipeSpell(AbstractSpell typeFromValue) {
        this.spellRecipeSelection = typeFromValue;
        //Ironsspellbooks.logger.debug("Setting selected Spell");
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
    public boolean canTakeItemForPickAll(ItemStack pStack, Slot pSlot) {
        return pSlot.container != this.resultSlot.container && super.canTakeItemForPickAll(pStack, pSlot);
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
