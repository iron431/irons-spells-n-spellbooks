package com.example.testmod.gui;

import com.example.testmod.block.InscriptionTable.InscriptionTableTile;
import com.example.testmod.registries.MenuRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.Nullable;

import static com.example.testmod.registries.BlockRegistry.INSCRIPTION_TABLE_BLOCK;

public class InscriptionTableMenu extends AbstractContainerMenu {
    private final InscriptionTableTile blockEntity;
    private final Level level;

    public InscriptionTableMenu(int containerID, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerID, inventory, inventory.player.level.getBlockEntity(extraData.readBlockPos()));
    }

    public InscriptionTableMenu(int containerID, Inventory inventory, BlockEntity blockEntity) {
        super(MenuRegistry.INSCRIPTION_TABLE_MENU.get(), containerID);
        checkContainerSize(inventory, 2);
        this.blockEntity = (InscriptionTableTile) blockEntity;
        this.level = inventory.player.level;

        addPlayerInventory(inventory);
        addPlayerHotbar(inventory);

        //this.blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handler);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level,blockEntity.getBlockPos()),player,INSCRIPTION_TABLE_BLOCK.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; i++) {
            for (int x = 0; x < 9; x++) {
                this.addSlot(new Slot(playerInventory, x + i * 9 + 9, 8 + x * 18, 86 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 144));
        }
    }
}
