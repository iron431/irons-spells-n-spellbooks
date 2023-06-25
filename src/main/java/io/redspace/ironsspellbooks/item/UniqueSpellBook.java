package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellRarity;

public class UniqueSpellBook extends SpellBook implements UniqueItem {

    AbstractSpell[] spells;

    public UniqueSpellBook(SpellRarity rarity, AbstractSpell[] spells) {
        super(spells.length, rarity);
        this.spells = spells;
    }

    public AbstractSpell[] getSpells() {
        return this.spells;
    }

    @Override
    public boolean isUnique() {
        return true;
    }
}
