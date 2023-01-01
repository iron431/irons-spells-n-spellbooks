package com.example.testmod.spells;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public enum SpellType {
    /*
    When adding spell, add:
        Spell Type
        Cast Type
        Translation
        Abstract Spell "getSpell" entry
     */
    NONE_SPELL(0),
    FIREBALL_SPELL(1),
    BURNING_DASH_SPELL(2),
    TEST_SPELL(3),
    TELEPORT_SPELL(4),
    MAGIC_MISSILE_SPELL(5),
    ELECTROCUTE_SPELL(6);

    private final int value;

    SpellType(final int newValue) {
        value = newValue;
    }

    public int getValue() {
        return value;
    }

    public TranslatableComponent getDisplayName() {
        return new TranslatableComponent("spell." + TestMod.MODID + "." + this.toString().toLowerCase().replace("_spell", ""));
    }
    public CastType getCastType(){
        switch(this){
            case FIREBALL_SPELL:
            case TELEPORT_SPELL:
                return CastType.LONG;

            case ELECTROCUTE_SPELL:
                return CastType.CONTINUOUS;

            default: return CastType.INSTANT;
        }
    }
}