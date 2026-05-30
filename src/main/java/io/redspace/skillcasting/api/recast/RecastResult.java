package io.redspace.skillcasting.api.recast;

/**
 * Why a recast window closed.
 */
public enum RecastResult {
    TIMEOUT,
    USED_ALL_RECASTS,
    DEATH,
    CANCELLED;
}
