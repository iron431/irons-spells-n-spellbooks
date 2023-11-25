package io.redspace.ironsspellbooks.damage;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.world.damagesource.DamageSource;

/**
 * While only a burden on 1.20.1, this interface makes 1.19.2 and 1.20.1 code be able to mesh together seamlessly
 */
public interface ISpellDamageSource {

    DamageSource get();
    AbstractSpell spell();
    default SchoolType schoolType(){
        return spell().getSchoolType();
    }
    float getLifestealPercent();
    int getFireTime();
    int getFreezeTicks();

    ISpellDamageSource setLifestealPercent(float lifesteal);
    ISpellDamageSource setFireTime(int fireTime);
    ISpellDamageSource setFreezeTicks(int freezeTime);

    default boolean hasPostHitEffects(){
        return getLifestealPercent() > 0 || getFireTime() > 0 || getFreezeTicks() > 0;
    }
}
