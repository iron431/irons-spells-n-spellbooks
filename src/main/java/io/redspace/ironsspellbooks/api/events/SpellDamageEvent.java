package io.redspace.ironsspellbooks.api.events;


import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class SpellDamageEvent extends LivingEvent  {
    //TODO: pre and post?
    private final SpellDamageSource spellDamageSource;
    private final float baseAmount;
    private float amount;

    public SpellDamageEvent(LivingEntity livingEntity, float amount, SpellDamageSource spellDamageSource) {
        super(livingEntity);
        this.spellDamageSource = spellDamageSource;
        this.baseAmount = amount;
        this.amount = this.baseAmount;
    }

    public float getOriginalAmount() {
        return this.baseAmount;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public SpellDamageSource getSpellDamageSource() {
        return this.spellDamageSource;
    }
}
