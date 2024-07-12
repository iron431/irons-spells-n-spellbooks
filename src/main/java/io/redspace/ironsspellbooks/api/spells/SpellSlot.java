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

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof SpellSlot o && o.spellData.equals(this.spellData) && o.index == this.index);
    }

    @Override
    public int hashCode() {
        return spellData.hashCode() * 31 + index;
    }
}
