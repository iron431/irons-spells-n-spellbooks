package com.example.testmod.item;

import com.example.testmod.capabilities.scroll.ScrollData;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;

public interface IScroll {
    ScrollData getScrollData(ItemStack stack);
    LazyOptional<ScrollData> getScrollDataProvider(ItemStack stack);
}
