package io.redspace.ironsspellbooks.api.config;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.neoforged.bus.api.Event;

public class ModifyDefaultConfigValuesEvent extends Event {

    private final AbstractSpell spell;
    private final SpellConfigHolder config;

    public ModifyDefaultConfigValuesEvent(AbstractSpell spell, SpellConfigHolder spellConfigHolder) {
        this.spell = spell;
        this.config = spellConfigHolder;
    }

    public <T> void setDefaultValue(SpellConfigParameter<T> type, T value) {
        config.setDefaultValue(type, value);
    }

    public AbstractSpell getSpell() {
        return spell;
    }
}
