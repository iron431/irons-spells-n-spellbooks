package io.redspace.ironsspellbooks.api.spells;

public interface ISpellContainerMutable {
    void setMaxSpellCount(int maxSpells);

    void setImproved(boolean improved);

    boolean addSpellAtIndex(AbstractSpell spell, int level, int index, boolean locked);

    boolean addSpell(AbstractSpell spell, int level, boolean locked);

    boolean removeSpellAtIndex(int index);

    boolean removeSpell(AbstractSpell spell);

    ISpellContainer toImmutable();
}

