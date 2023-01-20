package com.example.testmod.item;

import com.example.testmod.spells.SpellRarity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public class InkItem extends Item {
    private SpellRarity rarity;

    public InkItem(SpellRarity rarity) {
        super(new Item.Properties().tab(CreativeModeTab.TAB_MATERIALS));
        this.rarity = rarity;
    }

    public SpellRarity getRarity(){
        return rarity;
    }
}
