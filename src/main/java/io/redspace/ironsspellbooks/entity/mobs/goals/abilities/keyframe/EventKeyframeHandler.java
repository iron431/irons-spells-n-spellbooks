package io.redspace.ironsspellbooks.entity.mobs.goals.abilities.keyframe;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

public class EventKeyframeHandler {
    final Int2ObjectMap<EventKeyframe> events;

    public EventKeyframeHandler(Int2ObjectMap<EventKeyframe> events) {
        this.events = events;
    }

    public @Nullable EventKeyframe getEvent(int tick) {
        return events.get(tick);
    }

    public static EventKeyframeHandler of(EventKeyframe... eventKeyframes) {
        Int2ObjectMap<EventKeyframe> events = new Int2ObjectOpenHashMap<>();
        for (EventKeyframe a : eventKeyframes) {
            events.put(a.timestamp(), a);
        }
        return new EventKeyframeHandler(events);
    }
}
