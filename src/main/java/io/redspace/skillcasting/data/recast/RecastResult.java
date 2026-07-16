package io.redspace.skillcasting.data.recast;

/**
 * Why a recast window closed.
 */
public enum RecastResult {
    TIMEOUT,
    USED_ALL_RECASTS,
    DEATH,
    INTERRUPTED;
}
