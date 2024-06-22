package io.redspace.ironsspellbooks.api.registry;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellData;

import java.util.function.Supplier;


public class SpellDataRegistryHolder {

    Supplier<AbstractSpell> registrySpell;
    int spellLevel;

    public SpellDataRegistryHolder(Supplier<AbstractSpell> registrySpell, int spellLevel) {
        this.registrySpell = registrySpell;
        this.spellLevel = spellLevel;
    }

    public SpellData getSpellData() {
        return new SpellData(registrySpell.get(), spellLevel);
    }

    public static SpellDataRegistryHolder[] of(SpellDataRegistryHolder... args) {
        return args;
    }
}
