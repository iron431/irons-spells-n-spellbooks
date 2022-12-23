package com.example.testmod.spells;

import net.minecraft.world.item.ItemStack;

public enum SpellType {
    FIREBALL_SPELL(0),
    BURNING_DASH_SPELL(1);

    private final int value;

    SpellType(final int newValue) {
        value = newValue;
    }

    public int getValue() {
        return value;
    }
}