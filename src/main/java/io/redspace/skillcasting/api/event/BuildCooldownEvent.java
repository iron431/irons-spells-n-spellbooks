package io.redspace.skillcasting.api.event;

import io.redspace.skillcasting.api.cast.CastContext;
import net.neoforged.bus.api.Event;

/**
 * Fired when computing the cooldown to apply on completion. Listeners adjust the tick count (e.g. a
 * module halving cooldowns under a buff). The base is supplied explicitly to avoid circular reads.
 */
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
