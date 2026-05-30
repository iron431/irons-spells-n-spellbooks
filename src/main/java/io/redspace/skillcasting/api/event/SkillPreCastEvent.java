package io.redspace.skillcasting.api.event;

import io.redspace.skillcasting.api.cast.CastContext;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Fired at VALIDATE before a cast is committed. Cancelling aborts the cast.
 */
public final class SkillPreCastEvent extends Event implements ICancellableEvent {
    private final CastContext context;

    public SkillPreCastEvent(CastContext context) {
        this.context = context;
    }

    public CastContext context() {
        return context;
    }
}
