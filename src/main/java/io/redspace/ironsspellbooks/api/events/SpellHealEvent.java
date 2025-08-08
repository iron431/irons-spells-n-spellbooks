package io.redspace.ironsspellbooks.api.events;


import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;


public class SpellHealEvent extends LivingEvent {
    private final LivingEntity targetEntity;
    private final float healAmount;
    private SchoolType schoolType;

    public SpellHealEvent(LivingEntity castingEntity, LivingEntity targetEntity, float healAmount, SchoolType schoolType) {
        super(castingEntity);
        this.targetEntity = targetEntity;
        this.healAmount = healAmount;
        this.schoolType = schoolType;
    }

    public LivingEntity getTargetEntity() {
        return this.targetEntity;
    }

    public float getHealAmount() {
        return this.healAmount;
    }

    public SchoolType getSchoolType() {
        return this.schoolType;
    }
}
