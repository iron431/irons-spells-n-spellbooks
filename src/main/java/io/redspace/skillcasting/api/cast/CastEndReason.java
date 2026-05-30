package io.redspace.skillcasting.api.cast;

/**
 * Why a cast ended. Drives completion vs. cancellation handling and, for modules, cleanup policy.
 */
@Deprecated
public enum CastEndReason {
    /** Cast ran to its natural conclusion. */
    COMPLETED,
    /** Player or AI explicitly cancelled. */
    MANUAL,
    /** A new cast replaced this one (one-cast-at-a-time invariant). */
    REPLACED,
    /** Conditions ceased to hold mid-cast (validation, target lost, etc.). */
    INVALIDATED,
    /** Caster entity changed state: death, logout, respawn, dimension change, UI open. */
    ENTITY_STATE_CHANGE,
    /** Engine/system level forced stop. */
    SYSTEM;

    public boolean isCompletion() {
        return this == COMPLETED;
    }
}
