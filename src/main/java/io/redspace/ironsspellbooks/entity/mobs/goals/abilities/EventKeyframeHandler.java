package io.redspace.ironsspellbooks.entity.mobs.goals.abilities;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;

public class EventKeyframeHandler<T extends Mob & IAbilityHandler<T>> {
    final Int2ObjectMap<EventKeyframe<T>> events;

    public EventKeyframeHandler(Int2ObjectMap<EventKeyframe<T>> events) {
        this.events = events;
    }

    public @Nullable EventKeyframe<T> getEvent(int tick) {
        return events.get(tick);
    }

    @SafeVarargs
    public static <T extends Mob & IAbilityHandler<T>> EventKeyframeHandler<T> of(EventKeyframe<T>... keyframes) {
        Int2ObjectMap<EventKeyframe<T>> events = new Int2ObjectOpenHashMap<>();
        for (EventKeyframe<T> a : keyframes) {
            events.put(a.timestamp(), a);
        }
        return new EventKeyframeHandler<>(events);
    }

    @SafeVarargs
    public static <T extends Mob & IAbilityHandler<T>> EventKeyframeHandler<T> merge(EventKeyframeHandler<T>... handlers) {
        Int2ObjectMap<EventKeyframe<T>> events = new Int2ObjectOpenHashMap<>();
        for (EventKeyframeHandler<T> handler : handlers) {
            events.putAll(handler.events);
        }
        return new EventKeyframeHandler<>(events);
    }

    public static <T extends Mob & IAbilityHandler<T>> EventKeyframeHandler<T> ofRange(
            int startTick, int endTick, EventKeyframe<T> keyframe) {
        Int2ObjectMap<EventKeyframe<T>> events = new Int2ObjectOpenHashMap<>();
        for (int i = startTick; i <= endTick; i++) {
            events.put(i, keyframe);
        }
        return new EventKeyframeHandler<>(events);
    }
}
