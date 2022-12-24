package com.example.testmod.gui.slot;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class TestSlot extends SlotItemHandler {
    public TestSlot(IItemHandler itemHandler, int index, int x, int y){
        super(itemHandler,index,x,y);
    }
    @Override
    public boolean mayPlace(ItemStack stack){
        return false;
    }
}
