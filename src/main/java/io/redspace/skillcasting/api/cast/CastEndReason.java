package io.redspace.skillcasting.api.cast;

public enum CastEndReason {
    COMPLETED,
    INTERRUPTED;

    public boolean isCompletion() {
        return this == COMPLETED;
    }
}
