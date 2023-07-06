package io.redspace.ironsspellbooks.capabilities.spell;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
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
}
