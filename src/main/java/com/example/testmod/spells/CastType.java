package com.example.testmod.spells;

import net.minecraft.network.chat.TranslatableComponent;

public enum CastType {
    NONE(0),
    INSTANT(1),
    LONG(2),
    CONTINUOUS(3);

    private final int value;

    CastType(final int newValue) {
        value = newValue;
    }

    public int getValue() {
        return value;
    }

    public TranslatableComponent getDisplayName() {
        switch (this) {
            case INSTANT: return new TranslatableComponent("cast_type.testmod.instant");
            case LONG: return new TranslatableComponent("cast_type.testmod.long");
            case CONTINUOUS: return new TranslatableComponent("cast_type.testmod.continuous");
            default: return new TranslatableComponent("cast_type.testmod.none");
        }
    }
}