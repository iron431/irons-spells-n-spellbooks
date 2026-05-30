package io.redspace.skillcasting.api.event;

import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CastEndReason;
import net.neoforged.bus.api.Event;

/**
 * Fired when a cast ends, with the reason it ended.
 */
public final class SkillCastCompleteEvent extends Event {
    private final CastContext context;
    private final CastEndReason reason;

    public SkillCastCompleteEvent(CastContext context, CastEndReason reason) {
        this.context = context;
        this.reason = reason;
    }

    public CastContext context() {
        return context;
    }

    public CastEndReason reason() {
        return reason;
    }
}
