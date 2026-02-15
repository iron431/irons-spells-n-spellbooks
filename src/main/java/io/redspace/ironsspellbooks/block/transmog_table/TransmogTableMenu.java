package io.redspace.ironsspellbooks.block.transmog_table;

import io.redspace.ironsspellbooks.patreon.PatreonHandler;
import io.redspace.ironsspellbooks.patreon.PatreonPermissions;
import io.redspace.ironsspellbooks.patreon.transmog.TransmogHolder;
import io.redspace.ironsspellbooks.patreon.transmog.TransmogItemData;
import io.redspace.ironsspellbooks.patreon.transmog.TransmogManager;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.registries.MenuRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TransmogTableMenu extends AbstractContainerMenu {

    public static final EquipmentSlot[] HUMANOID_ARMOR_SLOTS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    public TransmogTableMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, ContainerLevelAccess.NULL);
    }

    protected final ContainerLevelAccess access;
    protected final Container transmogContainer = new SimpleContainer(1);
    int selectedTransmogIndex = -1;
    final Slot transmogSlot;

    List<TransmogAction> transmogActions = new ArrayList<>();
    Runnable armorSlotsChangedCallback = () -> {
    };
    Runnable transmogSelectionChangedCallback = () -> {
    };

    public TransmogTableMenu(int containerId, Inventory inv, ContainerLevelAccess access) {
        super(MenuRegistry.TRANSMOG_TABLE_MENU.get(), containerId);
        this.access = access;
        addPlayerInventory(19, 118, inv);
        addPlayerHotbar(19, 118 + 18 * 3 + 4, inv);
        addPlayerArmor(6, 26, inv);

        transmogSlot = new Slot(transmogContainer, 0, 33, 54) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.getItem() instanceof Equipable equipable && equipable.getEquipmentSlot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
            }

            @Override
            public void setChanged() {
                super.setChanged();
                armorSlotsChangedCallback.run();
            }
        };
        this.addSlot(transmogSlot);
        createTransmogList(inv.player);
    }

    @Override
    public void setData(int id, int data) {
        super.setData(id, data);
    }

    public record TransmogAction(boolean remove, TransmogHolder holder) {
        public TransmogAction(TransmogHolder holder) {
            this(false, holder);
        }

        boolean canPerform(ItemStack itemStack, PatreonPermissions permissions) {
            return remove || (permissions.canUse(holder) && holder.supportsSlot(itemStack));
        }
    }

    public int packTransmogRequest(int dyeColor) {
        dyeColor &= 0x00FFFFFF;
        dyeColor = -dyeColor;
        return dyeColor;
    }

    public int unpackTransmogColorFromRequest(int request) {
        request = -request;
        request |= 0xFF000000;
        return request;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id < 0) {
            // code to inscribe transmog
            int dyeColor = unpackTransmogColorFromRequest(id);
            TransmogAction action = getSelectedTransmogAction();
            ItemStack transmogStack = transmogContainer.getItem(0);
            if (action != null) {
                if (action.remove) {
                    TransmogItemData.remove(transmogStack);
                    return true;
                } else if (!transmogStack.isEmpty() && action.canPerform(transmogStack, PatreonHandler.getPatreonPermissions(player))) {
                    TransmogItemData.set(transmogStack, new TransmogItemData(action.holder(), dyeColor));
                    return true;
                }
            }
            return false;
        }
        if (id >= transmogActions.size()) {
            return false;
        }
        //todo: way to reset/unselect?
        setSelectedTransmogIndex(id);
        return true;
    }

    private void setSelectedTransmogIndex(int id) {
        selectedTransmogIndex = id;
        transmogSelectionChangedCallback.run();
    }

    private void createTransmogList(Player player) {
        List<TransmogHolder> accessibleTransmogs = new ArrayList<>();
        List<TransmogHolder> lockedTransmogs = new ArrayList<>();
        PatreonPermissions permissions = PatreonHandler.getPatreonPermissions(player);
        for (TransmogHolder holder : TransmogManager.getAllTransmogs()) {
            if (permissions.canUse(holder)) {
                accessibleTransmogs.add(holder);
            } else {
                lockedTransmogs.add(holder);
            }
        }
        accessibleTransmogs.sort(Comparator.comparing(TransmogHolder::requiredPermission).reversed());
        lockedTransmogs.sort(Comparator.comparing(TransmogHolder::requiredPermission).reversed());
        transmogActions.add(new TransmogAction(true, null));
        for (TransmogHolder holder : accessibleTransmogs) {
            transmogActions.add(new TransmogAction(holder));
        }
        for (TransmogHolder holder : lockedTransmogs) {
            transmogActions.add(new TransmogAction(holder));
        }
    }

    private void addPlayerInventory(int x, int y, Inventory playerInventory) {
        //todo: fix slot indexes
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, x + l * 18, y + i * 18));
            }
        }
    }

    private void addPlayerHotbar(int x, int y, Inventory playerInventory) {
        //todo: fix slot indexes
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, x + i * 18, y));
        }
    }

    private void addPlayerArmor(int x, int y, Inventory playerInventory) {
        //todo: fix slot indexes
        for (int i = 0; i < 4; i++) {
            EquipmentSlot equipmentslot = HUMANOID_ARMOR_SLOTS[i];
            this.addSlot(new TransmogArmorSlot(playerInventory, playerInventory.player, equipmentslot, 39 - i, x, y + i * 18) {
                @Override
                public void setChanged() {
                    super.setChanged();
                    armorSlotsChangedCallback.run();
                }
            });
        }
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;  //EMPTY_ITEM
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();
        boolean fromHotbar = index < 9;
        boolean fromInventory = index < 36;
        boolean fromArmor = !fromInventory && index < 36 + 4;
        boolean fromTransmogSlot = !fromInventory && !fromArmor;
        int transmogSlot = 36 + 4;

        // Check if the slot clicked is one of the vanilla container slots
        if (fromArmor) {
            // try to move armor into transmog slot
            if (!moveItemStackTo(sourceStack, transmogSlot, transmogSlot + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (fromInventory) {
            // Try to move into transmog slot, then try to move into armor slot
            if (!moveItemStackTo(sourceStack, transmogSlot, transmogSlot + 1, false) && !moveItemStackTo(sourceStack, 36, 36 + 4, false)) {
                return ItemStack.EMPTY;
            }
        } else if (fromTransmogSlot) {
            // Try to move into armor slots, then into inventory
            if (!moveItemStackTo(sourceStack, 36, 36 + 4, false) && !moveItemStackTo(sourceStack, 0, 36, false)) {
                return ItemStack.EMPTY;
            }
        } else {
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
    public boolean stillValid(Player player) {
        return this.access.evaluate((level, blockPos) -> level.getBlockState(blockPos).is(BlockRegistry.TRANSMOG_TABLE.get()) && player.distanceToSqr((double) blockPos.getX() + 0.5D, (double) blockPos.getY() + 0.5D, (double) blockPos.getZ() + 0.5D) <= 64.0D, true);
    }

    @Override
    public void removed(@NotNull Player player) {
        if (player instanceof ServerPlayer) {
            super.removed(player);
            this.access.execute((p_39796_, p_39797_) -> {
                ItemStack storedItem = this.transmogContainer.getItem(0);
                if (!storedItem.isEmpty() &&
                        storedItem.getItem() instanceof Equipable equipable &&
                        equipable.getEquipmentSlot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR &&
                        player.getItemBySlot(equipable.getEquipmentSlot()).isEmpty() &&
                        storedItem.canEquip(equipable.getEquipmentSlot(), player)
                ) {
                    // quick equip last stored transmog item if applicable slot is open
                    // todo: might also want to check for curse of binding here. lmao.
                    player.setItemSlot(equipable.getEquipmentSlot(), this.transmogContainer.removeItemNoUpdate(0));
                } else {
                    this.clearContainer(player, this.transmogContainer);
                }
            });
        }
    }

    public @Nullable TransmogAction getSelectedTransmogAction() {
        if (selectedTransmogIndex < 0 || selectedTransmogIndex >= transmogActions.size()) {
            return null;
        } else {
            return transmogActions.get(selectedTransmogIndex);
        }
    }
}
