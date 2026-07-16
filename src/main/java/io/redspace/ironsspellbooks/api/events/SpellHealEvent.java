package io.redspace.ironsspellbooks.api.events;


import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.skillcasting.data.cast.CasterRef;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

import javax.annotation.Nullable;


public class SpellHealEvent extends LivingEvent implements ICancellableEvent {
    private final @Nullable CasterRef caster;
    private float healAmount;
    private final AbstractSpell spell;

    public SpellHealEvent(@Nullable CasterRef caster, LivingEntity targetEntity, float healAmount, AbstractSpell spell) {
        super(targetEntity);
        this.caster = caster;
        this.healAmount = healAmount;
        this.spell = spell;
    }

    public SpellHealEvent(@Nullable LivingEntity caster, LivingEntity targetEntity, float healAmount, AbstractSpell spell) {
        this(caster == null ? null : CasterRef.entity(caster), targetEntity, healAmount, spell);
    }

    public LivingEntity getTargetEntity() {
        return this.getEntity();
    }

    public float getHealAmount() {
        return this.isCanceled() ? 0 : this.healAmount;
    }

    public void setHealAmount(float healAmount) {
        this.healAmount = healAmount;
    }

    public AbstractSpell getSpell() {
        return this.spell;
    }

    public @Nullable CasterRef getCaster() {
        return caster;
    }
}
