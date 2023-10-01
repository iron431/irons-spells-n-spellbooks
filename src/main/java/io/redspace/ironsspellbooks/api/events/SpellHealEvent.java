package io.redspace.ironsspellbooks.api.events;


import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * SpellHealEvent is fired whenever a spell heals a player.<br>
 * <br>
 * This event is not {@link Cancelable}.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.<br>
 **/
public class SpellHealEvent extends LivingEvent {
    private final LivingEntity targetEntity;
    private final float healAmount;
    public SpellHealEvent(LivingEntity castingEntity, LivingEntity targetEntity, float healAmount)
    {
        super(castingEntity);
        this.targetEntity = targetEntity;
        this.healAmount = healAmount;
    }

    public LivingEntity getTargetEntity() { return this.targetEntity; }
    public float getHealAmount() { return this.healAmount; }
}
