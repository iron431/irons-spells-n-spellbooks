package com.example.testmod.capabilities.mana.data;

import net.minecraft.advancements.critereon.ItemUsedOnBlockTrigger;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static com.example.testmod.registries.AttributeRegistry.MAX_MANA;

public class Mana {
    private int mana;

    public Mana(int mana) {
        this.mana = mana;
    }

    public int getMana() {
        return mana;
    }

    public void setMana(int mana) {
        this.mana = mana;
    }

    public void incrementMana(int increment){
        mana+=increment;
    }
}