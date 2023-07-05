package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.spells.SpellRarity;

public class UniqueSpellBook extends SpellBook implements UniqueItem {

    SpellData[] spellData;

    public UniqueSpellBook(SpellRarity rarity, SpellData[] spellData) {
        super(spellData.length, rarity);
        this.spellData = spellData;
    }

    public SpellData[] getSpells() {
        return this.spellData;
    }

    @Override
    public boolean isUnique() {
        return true;
    }
}
