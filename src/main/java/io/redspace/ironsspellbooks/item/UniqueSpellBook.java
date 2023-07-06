package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.capabilities.spell.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;

import java.util.Arrays;

public class UniqueSpellBook extends SpellBook implements UniqueItem {

    SpellData[] spellData = null;
    SpellDataRegistryHolder[] spellDataRegistryHolders;

    public UniqueSpellBook(SpellRarity rarity, SpellDataRegistryHolder[] spellDataRegistryHolders) {
        super(spellDataRegistryHolders.length, rarity);
        this.spellDataRegistryHolders = spellDataRegistryHolders;
    }

    public SpellData[] getSpells() {
        if (spellData == null) {
            spellData = (SpellData[]) Arrays.stream(spellDataRegistryHolders).map(SpellDataRegistryHolder::getSpellData).toArray();
            spellDataRegistryHolders = null;
        }
        return spellData;
    }

    @Override
    public boolean isUnique() {
        return true;
    }
}
