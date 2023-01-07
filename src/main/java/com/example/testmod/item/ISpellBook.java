package com.example.testmod.item;

import com.example.testmod.capabilities.spellbook.SpellBookData;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;

public interface ISpellBook {
    SpellBookData getSpellBookData(ItemStack stack);
    LazyOptional<SpellBookData> getSpellBookDataProvider(ItemStack stack);
}
