package io.redspace.ironsspellbooks.capabilities.magic;

public enum RecastResult {
    TIMEOUT,
    COUNTERSPELL,
    DEATH,
    USED_ALL_RECASTS,
    COMMAND,
    USER_CANCEL;

    public boolean isFailure() {
        return this == DEATH || this == COUNTERSPELL;
    }

    public boolean isSuccess() {
        return this == USED_ALL_RECASTS;
    }
}
