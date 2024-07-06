package io.redspace.ironsspellbooks.api.spells;

/**
 * (Spell Data + Index) for serialization
 */
public record SpellSlot(SpellData spellData, int index) {

    public AbstractSpell getSpell() {
        return spellData.getSpell();
    }

    public int getLevel() {
        return spellData.getLevel();
    }

    public boolean isLocked() {
        return spellData.isLocked();
    }

    public int index() {
        return index;
    }

    public static SpellSlot of(SpellData data, int index) {
        return new SpellSlot(data, index);
    }
}
