package io.redspace.ironsspellbooks.spells;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.spells.AbstractSpellSkill;
import io.redspace.skillcastingapi.data.ICastContext;

public class NoneSpell extends AbstractSpellSkill {
    public NoneSpell() {
        this.baseManaCost = 0;
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 0;
        this.spellPowerPerLevel = 0;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        //This class is not discoverable, so this shouldn't ever be accessed :)
        return null;
    }

    @Override
    public io.redspace.skillcastingapi.core.CastType getCastType() {
        return io.redspace.skillcastingapi.core.CastType.INSTANT;
    }

    @Override
    protected void onCast(ICastContext castContext) {

    }

}
