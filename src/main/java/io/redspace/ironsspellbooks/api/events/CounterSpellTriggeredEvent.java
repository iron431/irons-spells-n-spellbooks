package io.redspace.ironsspellbooks.api.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

public class CounterSpellTriggeredEvent extends Event {
    EntityHitResult result;

    public CounterSpellTriggeredEvent(Entity caster, EntityHitResult target){}

    public static void Post(CounterSpellTriggeredEvent event){
        MinecraftForge.EVENT_BUS.post(event);
    }
}
