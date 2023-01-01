package com.example.testmod.spells;

import net.minecraft.network.chat.TranslatableComponent;

public enum SpellType {
    /*
    When adding spell, add:
        Spell Type
        Cast Type
        Translation
        Abstract Spell "getSpell" entry
     */

    NONE(0),
    FIREBALL_SPELL(1),
    BURNING_DASH_SPELL(2),
    TEST_SPELL(3),
    TELEPORT_SPELL(4),
    ELECTROCUTE_SPELL(5);

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
            case ELECTROCUTE_SPELL: return new TranslatableComponent("spell.lightning.electrocute");
            default:
                return new TranslatableComponent("spell.none");
        }
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

    public String getIdentifier() {
        return this.toString().toLowerCase().replace("_spell", "");
    }
}