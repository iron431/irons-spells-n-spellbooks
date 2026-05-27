package io.redspace.ironsspellbooks.api.events;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired on the game event bus after {@link io.redspace.ironsspellbooks.capabilities.magic.SummonManager#setOwner}
 * has updated ownership maps. Not cancellable.
 */
public class SetSummonOwnerEvent extends Event {
    private final Entity owner;
    private final Entity summon;

    public SetSummonOwnerEvent(Entity owner, Entity summon) {
        this.owner = owner;
        this.summon = summon;
    }

    public Entity getOwner() {
        return owner;
    }

    public Entity getSummon() {
        return summon;
    }
}
