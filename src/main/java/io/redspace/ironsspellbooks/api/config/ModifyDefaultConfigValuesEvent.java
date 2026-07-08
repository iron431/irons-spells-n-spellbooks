package io.redspace.ironsspellbooks.api.config;

import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import net.neoforged.bus.api.Event;

public class ModifyDefaultConfigValuesEvent extends Event {

    private final AbstractSpellSkill spell;
    private final SpellConfigHolder config;

    public ModifyDefaultConfigValuesEvent(AbstractSpellSkill spell, SpellConfigHolder spellConfigHolder) {
        this.spell = spell;
        this.config = spellConfigHolder;
    }

    public <T> void setDefaultValue(SpellConfigParameter<T> type, T value) {
        config.setDefaultValue(type, value);
    }

    public AbstractSpellSkill getSpell() {
        return spell;
    }
}
