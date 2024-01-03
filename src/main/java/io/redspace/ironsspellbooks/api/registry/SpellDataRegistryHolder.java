package io.redspace.ironsspellbooks.api.registry;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import net.minecraftforge.registries.RegistryObject;

public class SpellDataRegistryHolder {

    RegistryObject<AbstractSpell> registrySpell;
    int spellLevel;

    public SpellDataRegistryHolder(RegistryObject<AbstractSpell> registrySpell, int spellLevel) {
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
