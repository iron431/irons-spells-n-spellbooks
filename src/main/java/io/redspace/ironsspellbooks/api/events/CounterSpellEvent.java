package io.redspace.ironsspellbooks.api.events;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Event;

public class CounterSpellEvent extends Event {
    public final Entity caster;
    public final Entity target;

    public CounterSpellEvent(Entity caster, Entity target){
        this.caster = caster;
        this.target = target;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }
}
