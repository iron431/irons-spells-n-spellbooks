package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellRarity;

public class UniqueSpellBook extends SpellBook implements UniqueItem {

    AbstractSpell[] spells;

    public UniqueSpellBook(SpellRarity rarity, AbstractSpell[] spells) {
        super(spells.length, rarity);
        this.spells = spells;
    }

    @Override
    public boolean isUnique() {
        return true;
    }
}
