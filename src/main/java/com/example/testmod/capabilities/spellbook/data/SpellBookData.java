package com.example.testmod.capabilities.spellbook.data;

import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.apache.commons.lang3.ArrayUtils;

public class SpellBookData {

    public static final String SPELL_SLOTS = "spellSlots";
    public static final String ACTIVE_SPELL_INDEX = "activeSpellIndex";
    public static final String SPELLS = "spells";
    public static final String ID = "id";
    public static final String LEVEL = "level";
    public static final String SLOT = "slot";

    private AbstractSpell[] transcribedSpells;
    private int activeSpellIndex = -1;
    private int spellSlots = 0;
    private int spellCount = 0;
    private boolean dirty = true;
    private CompoundTag tag = new CompoundTag();

    public SpellBookData(int spellSlots) {
        this.spellSlots = spellSlots;
        this.transcribedSpells = new AbstractSpell[this.spellSlots];
    }

    public AbstractSpell getActiveSpell() {
        if (activeSpellIndex < 0) {
            return AbstractSpell.getSpell(SpellType.NONE_SPELL, 0);
        }

        AbstractSpell spell = transcribedSpells[activeSpellIndex];

        if (spell == null) {
            return AbstractSpell.getSpell(SpellType.NONE_SPELL, 0);
        }

        return transcribedSpells[activeSpellIndex];
    }

    public boolean setActiveSpell(AbstractSpell spell) {
        var index = ArrayUtils.indexOf(transcribedSpells, spell);
        return setActiveSpell(index);
    }

    public boolean setActiveSpell(int index) {
        if (index > -1 && index < transcribedSpells.length && transcribedSpells[index] != null) {
            this.activeSpellIndex = index;
            setDirty(true);
            return true;
        }
        return false;
    }

    public AbstractSpell[] getInscribedSpells() {
        var result = new AbstractSpell[this.spellSlots];
        System.arraycopy(transcribedSpells, 0, result, 0, transcribedSpells.length);
        return result;
    }

    private void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public int getSpellSlots() {
        return spellSlots;
    }

    public boolean addSpell(AbstractSpell spell, int index) {
        if (index > -1 && index < transcribedSpells.length && transcribedSpells[index] == null) {
            transcribedSpells[index] = spell;
            spellCount++;
            if (spellCount == 1) {
                setActiveSpell(index);
            }
            setDirty(true);
            return true;
        }
        return false;
    }

    public boolean addSpell(AbstractSpell spell) {
        int index = getNextSpellIndex();
        if (index > -1) {
            return addSpell(spell, index);
        }
        return false;
    }

    private int getNextSpellIndex() {
        return ArrayUtils.indexOf(this.transcribedSpells, null);
    }

    public boolean replaceSpell(AbstractSpell oldSpell, AbstractSpell newSpell) {
        if (oldSpell != null && newSpell != null) {
            int index = ArrayUtils.indexOf(transcribedSpells, oldSpell);
            if (index > -1 && removeSpell(index)) {
                return addSpell(newSpell, index);
            }
        }

        return false;
    }

    public boolean replaceSpell(int index, AbstractSpell newSpell) {
        if (index > -1 && index < transcribedSpells.length) {
            return replaceSpell(transcribedSpells[index], newSpell);
        }
        return false;
    }

    public boolean removeSpell(AbstractSpell spell) {
        return removeSpell(ArrayUtils.indexOf(transcribedSpells, spell));
    }

    public boolean removeSpell(int index) {
        if (index < transcribedSpells.length && transcribedSpells[index] != null) {
            transcribedSpells[index] = null;
            spellCount--;
            if (spellCount == 0) {
                setActiveSpell(null);
            }
            setDirty(true);
            return true;
        }

        return false;
    }

    public CompoundTag saveNBTData() {
        if (!dirty) {
            return this.tag;
        }

        ListTag listTagSpells = new ListTag();
        CompoundTag compound = new CompoundTag();
        compound.putInt(SPELL_SLOTS, spellSlots);
        compound.putInt(ACTIVE_SPELL_INDEX, activeSpellIndex);

        for (int i = 0; i < transcribedSpells.length; i++) {
            var spell = transcribedSpells[i];
            if (spell != null) {
                CompoundTag ct = new CompoundTag();
                ct.putInt(ID, spell.getID());
                ct.putInt(LEVEL, spell.getLevel());
                ct.putInt(SLOT, i);
                listTagSpells.add(ct);
            }
        }

        compound.put(SPELLS, listTagSpells);
        this.tag = compound;
        setDirty(false);

        return (this.tag);
    }

    public void loadNBTData(CompoundTag compound) {
        this.spellSlots = compound.getInt(SPELL_SLOTS);
        this.transcribedSpells = new AbstractSpell[spellSlots];
        this.activeSpellIndex = compound.getInt(ACTIVE_SPELL_INDEX);

        ListTag listTagSpells = (ListTag) compound.get(SPELLS);
        if (listTagSpells != null) {
            listTagSpells.forEach(tag -> {
                CompoundTag t = (CompoundTag) tag;
                int id = t.getInt(ID);
                int level = t.getInt(LEVEL);
                int index = t.getInt(SLOT);
                AbstractSpell s = AbstractSpell.getSpell(id, level);
                transcribedSpells[index] = s;
                spellCount++;
            });
        }
        setDirty(false);
    }
}
