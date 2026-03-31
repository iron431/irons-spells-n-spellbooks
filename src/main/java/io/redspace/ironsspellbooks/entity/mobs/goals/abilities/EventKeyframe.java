package io.redspace.ironsspellbooks.entity.mobs.goals.abilities;

import net.minecraft.world.entity.Mob;

public abstract class EventKeyframe<T extends Mob & IAbilityHandler<T>> {
    final int timestamp;

    public EventKeyframe(int timestamp) {
        this.timestamp = timestamp;
    }

    public abstract void onEvent(T mob);

    public int timestamp() {
        return timestamp;
    }
}
