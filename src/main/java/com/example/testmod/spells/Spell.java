package com.example.testmod.spells;

import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class Spell {

    protected int baseManaCost;
    protected int manaCostPerLevel;
    protected int level;
    protected int baseSpellPower;
    protected int spellPowerPerLevel;
    public int getManaCost(){
        return baseManaCost + manaCostPerLevel * (level-1);
    }
    public int getSpellPower(){
        return baseSpellPower+spellPowerPerLevel*(level-1);
    }
    public abstract InteractionResultHolder<ItemStack> onUse(ItemStack stack, Level level, Player player);
}
