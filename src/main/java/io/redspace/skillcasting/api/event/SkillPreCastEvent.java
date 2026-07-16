package io.redspace.skillcasting.api.event;

import io.redspace.skillcasting.data.CastContext;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public final class SkillPreCastEvent extends Event implements ICancellableEvent {
    private final CastContext context;

    public SkillPreCastEvent(CastContext context) {
        this.context = context;
    }

    public CastContext context() {
        return context;
    }
}
