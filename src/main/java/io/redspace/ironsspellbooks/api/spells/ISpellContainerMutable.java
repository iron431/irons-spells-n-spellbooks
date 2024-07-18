package io.redspace.ironsspellbooks.api.spells;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ISpellContainerMutable {
    void setMaxSpellCount(int maxSpells);

    void setImproved(boolean improved);

    boolean addSpellAtIndex(AbstractSpell spell, int level, int index, boolean locked);

    boolean addSpell(AbstractSpell spell, int level, boolean locked);

    boolean removeSpellAtIndex(int index);

    boolean removeSpell(AbstractSpell spell);

    @NotNull SpellSlot[] getAllSpells();

    @NotNull List<SpellSlot> getActiveSpells();

    /*
    Getters. Currently, a copy of ISpellContainer
     */

    int getMaxSpellCount();


    int getActiveSpellCount();

    int getNextAvailableIndex();

    boolean mustEquip();

    boolean isImproved();

    boolean isSpellWheel();

    @NotNull SpellData getSpellAtIndex(int index);

    int getIndexForSpell(AbstractSpell spell);

    boolean isEmpty();

    ISpellContainer toImmutable();
}

