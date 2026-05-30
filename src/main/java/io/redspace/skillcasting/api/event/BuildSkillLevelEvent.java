package io.redspace.skillcasting.api.event;

import io.redspace.skillcasting.api.cast.CastContext;
import net.neoforged.bus.api.Event;

/**
 * Fired during INIT with the base skill level as an explicit input. Listeners adjust the level
 * additively/multiplicatively; the resolved value is snapshotted into the effective-level component.
 * Passing the base in explicitly avoids the get/set circularity of the old prototype.
 */
public final class BuildSkillLevelEvent extends Event {
    private final CastContext context;
    private final int baseLevel;
    private int level;

    public BuildSkillLevelEvent(CastContext context, int baseLevel) {
        this.context = context;
        this.baseLevel = baseLevel;
        this.level = baseLevel;
    }

    public CastContext context() {
        return context;
    }

    public int baseLevel() {
        return baseLevel;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
