package io.redspace.ironsspellbooks.block.transmog_table;

import io.redspace.ironsspellbooks.patreon.PatreonHandler;
import io.redspace.ironsspellbooks.patreon.PatreonPermissions;
import io.redspace.ironsspellbooks.patreon.transmog.TransmogHolder;
import io.redspace.ironsspellbooks.patreon.transmog.TransmogManager;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import io.redspace.ironsspellbooks.registries.MenuRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class TransmogTableMenu extends AbstractContainerMenu {
    private static final Map<EquipmentSlot, ResourceLocation> TEXTURE_EMPTY_SLOTS = Map.of(
            EquipmentSlot.FEET,
            InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS,
            EquipmentSlot.LEGS,
            InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS,
            EquipmentSlot.CHEST,
            InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE,
            EquipmentSlot.HEAD,
            InventoryMenu.EMPTY_ARMOR_SLOT_HELMET
    );
    private static final EquipmentSlot[] SLOT_IDS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    public TransmogTableMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, ContainerLevelAccess.NULL);
    }

    protected final ContainerLevelAccess access;
    protected final Container transmogContainer = new SimpleContainer(1)/* {
        @Override
        public void setChanged() {
            super.setChanged();
            InscriptionTableMenu.this.slotsChanged(this);
        }
    }*/;
    int selectedTransmogIndex = -1;
    final Slot transmogSlot;

    List<TransmogAction> /*accessibleTransmogs, lockedTransmogs, */transmogActions = new ArrayList<>();
    Runnable armorSlotsChangedCallback = () -> {
    };
    Runnable transmogSelectionChangedCallback = () -> {
    };

    public TransmogTableMenu(int containerId, Inventory inv, ContainerLevelAccess access) {
        super(MenuRegistry.TRANSMOG_TABLE_MENU.get(), containerId);
        this.access = access;
        addPlayerInventory(30, 120, inv);
        addPlayerHotbar(30, 120 + 18 * 3 + 4, inv);
        addPlayerArmor(8, 39, inv);

        transmogSlot = new Slot(transmogContainer, 0, 102, 59) {
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

        boolean canPerform(PatreonPermissions permissions) {
            return remove || permissions.canUse(holder);
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        // todo: use enums/constants for codes
        if (id == -99) {
            // code to inscribe transmog
            TransmogAction action = getSelectedTransmogAction();
            ItemStack transmogStack = transmogContainer.getItem(0);
            if (action != null) {
                if (action.remove) {
                    transmogStack.remove(ComponentRegistry.TRANSMOG);
                } else if (!transmogStack.isEmpty() && PatreonHandler.getPatreonPermissions(player).canUse(action.holder())) {
                    transmogStack.set(ComponentRegistry.TRANSMOG, action.holder());
                }
                return true;
            }
            return false;
        }
        //todo: do lack of permissions deny even previewing? prob not
        if (id < 0 || id >= transmogActions.size()) {
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
        for (int i = 0; i < 37; i++) {
            for (TransmogHolder holder : accessibleTransmogs) {
                transmogActions.add(new TransmogAction(holder));
            }
        }
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
            EquipmentSlot equipmentslot = SLOT_IDS[i];
            ResourceLocation resourcelocation = TEXTURE_EMPTY_SLOTS.get(equipmentslot);
            this.addSlot(new ArmorSlot(playerInventory, playerInventory.player, equipmentslot, 39 - i, x, y + i * 18, resourcelocation) {
                @Override
                public void setChanged() {
                    super.setChanged();
                    armorSlotsChangedCallback.run();
                }
            });
        }
    }

    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = 9 + 27;
    private static final int TE_INVENTORY_SLOT_COUNT = 1;

    @Override
    public ItemStack quickMoveStack(@NotNull Player playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        if (index < TE_INVENTORY_FIRST_SLOT_INDEX) {
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, 0, TE_INVENTORY_FIRST_SLOT_INDEX, false)) {
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
