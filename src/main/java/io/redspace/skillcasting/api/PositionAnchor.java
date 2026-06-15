package io.redspace.skillcasting.api;

public enum PositionAnchor {
    /**
     * The raw position of the caster
     */
    ORIGIN,
    /**
     * The centermost point of the caster's hitbox/collision area
     */
    CENTER,
    /**
     * The logical place skillcasts project from for the caster
     */
    CASTING_POSITION
}
