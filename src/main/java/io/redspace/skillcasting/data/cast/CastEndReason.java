package io.redspace.skillcasting.data.cast;

public enum CastEndReason {
    COMPLETED,
    INTERRUPTED;

    public boolean isCompletion() {
        return this == COMPLETED;
    }
}
