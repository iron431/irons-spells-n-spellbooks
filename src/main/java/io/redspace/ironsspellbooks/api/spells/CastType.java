package io.redspace.ironsspellbooks.api.spells;

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

    @Deprecated(forRemoval = true)
    public boolean holdToCast() {
        return false;//this == CONTINUOUS/* || this == LONG*/;
    }

    public boolean immediatelySuppressRightClicks() {
        return this == LONG || this == CONTINUOUS;
    }
}