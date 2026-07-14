package io.redspace.ironsspellbooks.api.events;

import io.redspace.skillcasting.api.cast.CasterRef;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public abstract class CounterSpellEvent extends Event {
    private final CasterRef caster;

    private final CasterRef target;

    public CasterRef getTarget() {
        return target;
    }

    public CasterRef getCaster() {
        return caster;
    }

    public CounterSpellEvent(CasterRef caster, CasterRef target) {
        this.caster = caster;
        this.target = target;
    }

    public static class Pre extends CounterSpellEvent implements ICancellableEvent {
        public Pre(CasterRef caster, CasterRef target) {
            super(caster, target);
        }
    }

    public static class Post extends CounterSpellEvent {
        public Post(CasterRef caster, CasterRef target) {
            super(caster, target);
        }
    }
}
