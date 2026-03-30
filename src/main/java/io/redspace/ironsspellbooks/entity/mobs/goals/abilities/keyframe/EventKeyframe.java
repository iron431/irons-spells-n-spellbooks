package io.redspace.ironsspellbooks.entity.mobs.goals.abilities.keyframe;

import net.minecraft.world.entity.Mob;

public abstract class EventKeyframe {
    final int timestamp;

    public EventKeyframe(int timestamp) {
        this.timestamp = timestamp;
    }

    public abstract void onEvent(Mob actor);

    public int timestamp() {
        return timestamp;
    }
}
