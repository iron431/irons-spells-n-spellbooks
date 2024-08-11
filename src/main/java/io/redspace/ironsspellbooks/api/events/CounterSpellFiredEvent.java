package io.redspace.ironsspellbooks.api.events;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

public class CounterSpellFiredEvent extends Event {
    public LivingEntity caster;
    public HitResult target;

    @Override
    public boolean isCancelable() {
        return true;
    }

    public CounterSpellFiredEvent(LivingEntity caster, HitResult target) {
        this.caster = caster;
        this.target = target;
    }

    public static class Pre extends CounterSpellFiredEvent {
        public Pre(LivingEntity caster, HitResult target) {
            super(caster, target);
        }
    }

    public LivingEntity getCaster() {
        return caster;
    }

    public void setCaster(Player player) {
        this.caster = player;
    }

    public HitResult getTarget() {
        return target;
    }

    public void setTarget(HitResult target) {
        this.target = target;
    }

    public static void push(CounterSpellFiredEvent event) {
        MinecraftForge.EVENT_BUS.post(event);
    }
}
