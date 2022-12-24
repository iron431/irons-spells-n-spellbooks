package com.example.testmod.block.InscriptionTable;
import com.example.testmod.gui.InscriptionTableMenu;
import com.example.testmod.registries.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;


public class InscriptionTableTile extends BlockEntity implements MenuProvider {

    public InscriptionTableTile(BlockPos p_155501_, BlockState p_155502_) {
        super(BlockRegistry.INSCRIPTION_TABLE_TILE.get(), p_155501_, p_155502_);
    }

    @Override
    protected void saveAdditional(CompoundTag p_187471_) {
        super.saveAdditional(p_187471_);
    }
    @Override
    public void load(CompoundTag p_155245_) {
        super.load(p_155245_);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return super.getUpdateTag();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
    }

    @Override
    public Component getDisplayName() {
        return new TextComponent("Inscription Table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerID, Inventory inventory, Player player) {
        return new InscriptionTableMenu(containerID,inventory,this);
    }
}
