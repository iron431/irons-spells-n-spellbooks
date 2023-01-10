package com.example.testmod.spells;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

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

    public MutableComponent getDisplayName() {
        switch (this) {
            case INSTANT:
                return Component.translatable("cast_type.testmod.instant");
            case LONG:
                return Component.translatable("cast_type.testmod.long");
            case CONTINUOUS:
                return Component.translatable("cast_type.testmod.continuous");
            default:
                return Component.translatable("cast_type.testmod.none");
        }
    }
}