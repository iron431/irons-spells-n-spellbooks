package com.example.testmod.gui.slot;

import com.example.testmod.TestMod;
import com.example.testmod.block.InscriptionTable.InscriptionTableTile;
import com.example.testmod.gui.InscriptionTableScreen;
import com.example.testmod.gui.network.PacketRemoveSpell;
import com.example.testmod.setup.Messages;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ScrollExtractionSlot extends SlotItemHandler {

    public ScrollExtractionSlot(IItemHandler itemHandler, int index, int x, int y) {
        super(itemHandler, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

}
