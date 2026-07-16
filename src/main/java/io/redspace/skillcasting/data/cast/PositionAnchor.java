package io.redspace.skillcasting.data.cast;

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
    CASTING_POSITION,
    /**
     * The centermost point, at the bottommost y-level
     */
    BOTTOM_CENTER,
    /**
     * The centermost point, at the casting position y-level
     */
    CASTING_POSITION_CENTER,
    ;
}
