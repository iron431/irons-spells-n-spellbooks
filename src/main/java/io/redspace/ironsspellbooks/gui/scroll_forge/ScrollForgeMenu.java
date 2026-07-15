package io.redspace.ironsspellbooks.gui.scroll_forge;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.block.scroll_forge.ScrollForgeTile;
import io.redspace.ironsspellbooks.item.InkItem;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.MenuRegistry;
import io.redspace.ironsspellbooks.util.ModTags;
import io.redspace.skillcasting.data.ISkillContainer;
import io.redspace.skillcasting.data.SkillData;
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
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    private AbstractSpell spellRecipeSelection;

    public ScrollForgeMenu(int containerId, Inventory inv, BlockEntity entity) {
        super(MenuRegistry.SCROLL_FORGE_MENU.get(), containerId);
        checkContainerSize(inv, 4);
        blockEntity = (ScrollForgeTile) entity;
        this.level = inv.player.level();

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
        IItemHandler itemHandler = this.blockEntity.getItemHandler();

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
            setupResultSlot(spellRecipeSelection);
        }
    }

    private void setupResultSlot(@Nullable AbstractSpell spell) {
        ItemStack scrollStack = this.blankScrollSlot.getItem();
        ItemStack inkStack = this.inkSlot.getItem();
        ItemStack focusStack = this.focusSlot.getItem();
        ItemStack resultStack = ItemStack.EMPTY;
        if (!scrollStack.isEmpty() && !inkStack.isEmpty() && !focusStack.isEmpty() &&
                spell != null &&
                spell.allowCrafting() &&
                SchoolRegistry.getSchoolsFromFocus(focusStack).contains(spell.getSchoolType())) {
            if (scrollStack.getItem().equals(Items.PAPER) && inkStack.getItem() instanceof InkItem inkItem) {
                resultStack = new ItemStack(ItemRegistry.SCROLL.get());
                resultStack.setCount(1);
                ISkillContainer.set(resultStack, Scroll.createScrollContainer(new SkillData(spell, spell.getMinLevelForRarity(inkItem.getRarity()))));
            }
        }

        if (!ItemStack.matches(resultStack, this.resultSlot.getItem())) {
            if (resultStack.isEmpty()) {
                this.spellRecipeSelection = null;
            }
            this.resultSlot.set(resultStack);
        }
    }

    public void setRecipeSpell(@Nullable AbstractSpell spell) {
        this.spellRecipeSelection = spell;
        setupResultSlot(spell);
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

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
    private static final int TE_INVENTORY_SLOT_COUNT = 4;

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        if (index < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
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
}
