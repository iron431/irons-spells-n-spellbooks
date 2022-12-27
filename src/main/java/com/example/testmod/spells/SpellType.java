package com.example.testmod.spells;

import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public enum SpellType {
    FIREBALL_SPELL(0),
    BURNING_DASH_SPELL(1),
    TEST_SPELL(2);

    private final int value;

    SpellType(final int newValue) {
        value = newValue;
    }

    public int getValue() {
        return value;
    }
    public String getId(){return this.toString().toLowerCase().replace("_spell","");}
}