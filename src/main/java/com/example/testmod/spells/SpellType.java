package com.example.testmod.spells;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public enum SpellType {
    NONE(0),
    FIREBALL_SPELL(1),
    BURNING_DASH_SPELL(2),
    TEST_SPELL(3),
    TELEPORT(4),
    MAGIC_MISSILE(5);

    private final int value;

    SpellType(final int newValue) {
        value = newValue;
    }

    public int getValue() {
        return value;
    }

    public TranslatableComponent getDisplayName() {
        switch (this) {
            case FIREBALL_SPELL:
                return new TranslatableComponent("spell.fire.fireball");
            default:
                return new TranslatableComponent("spell.none");
        }
    }

    public String getIdentifier() {
        return this.toString().toLowerCase().replace("_spell", "");
    }
}