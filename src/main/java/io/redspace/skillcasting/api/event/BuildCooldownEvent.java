package io.redspace.skillcasting.api.event;

import io.redspace.skillcasting.data.CastContext;
import net.neoforged.bus.api.Event;

public final class BuildCooldownEvent extends Event {
    private final CastContext context;
    private final int baseTicks;
    private int ticks;

    public BuildCooldownEvent(CastContext context, int baseTicks) {
        this.context = context;
        this.baseTicks = baseTicks;
        this.ticks = baseTicks;
    }

    public CastContext context() {
        return context;
    }

    public int baseTicks() {
        return baseTicks;
    }

    public int getTicks() {
        return ticks;
    }

    public void setTicks(int ticks) {
        this.ticks = ticks;
    }
}
