package com.example.testmod.capabilities.spellbook.data;

import com.example.testmod.spells.AbstractSpell;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.apache.commons.lang3.ArrayUtils;

public class SpellBookData {
    private AbstractSpell[] transcribedSpells;
    private AbstractSpell activeSpell = null;
    private int spellSlots = 0;
    private int spellCount = 0;
    private boolean dirty = true;
    private CompoundTag tag = new CompoundTag();

    public SpellBookData(int spellSlots) {
        this.spellSlots = spellSlots;
        this.transcribedSpells = new AbstractSpell[this.spellSlots];
    }

    public AbstractSpell getActiveSpell() {
        return activeSpell;
    }

    public boolean setActiveSpell(AbstractSpell spell) {
        var index = ArrayUtils.indexOf(transcribedSpells, spell);

        if (index > -1) {
            this.activeSpell = transcribedSpells[index];
            setDirty(true);
            return true;
        }
        return false;
    }

    public boolean setActiveSpell(int index) {
        if (index < transcribedSpells.length && transcribedSpells[index] != null) {
            this.activeSpell = transcribedSpells[index];
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

    public boolean isDirty() {
        return dirty;
    }

    private void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public int getSpellSlots() {
        return spellSlots;
    }

    public boolean addSpell(AbstractSpell spell, int index) {
        if (index < transcribedSpells.length && transcribedSpells[index] == null) {
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
        return replaceSpell(transcribedSpells[index], newSpell);
    }

    public boolean removeSpell(AbstractSpell spell) {
        return removeSpell(ArrayUtils.indexOf(transcribedSpells, spell));
    }

    public boolean removeSpell(int index) {
        if (index < transcribedSpells.length && transcribedSpells[index] == null) {
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
        compound.putInt("spellSlots", spellSlots);

        for (int i = 0; i < transcribedSpells.length; i++) {
            var spell = transcribedSpells[i];
            CompoundTag ct = new CompoundTag();
            ct.putInt("id", spell.getID());
            ct.putInt("level", spell.getLevel());
            ct.putInt("index", i);
            listTagSpells.add(ct);
        }

        compound.put("spells", listTagSpells);

        if (this.activeSpell == null) {
            compound.putInt("activeSpellId", -1);
        } else {
            compound.putInt("activeSpellId", this.activeSpell.getID());
        }
        this.tag = compound;
        setDirty(false);
        return (this.tag);
    }

    public void loadNBTData(CompoundTag compound) {
        spellSlots = compound.getInt("spellSlots");
        transcribedSpells = new AbstractSpell[spellSlots];
        int activeSpellId = compound.getInt("activeSpellId");

        ListTag listTagSpells = (ListTag) compound.get("spells");
        if (listTagSpells != null) {
            listTagSpells.forEach(tag -> {
                CompoundTag t = (CompoundTag) tag;
                int id = t.getInt("id");
                int level = t.getInt("level");
                int index = t.getInt("index");
                AbstractSpell s = AbstractSpell.getSpell(id, level);
                transcribedSpells[index] = s;
                if (activeSpellId == s.getID()) {
                    setActiveSpell(s);
                }
            });
        }
    }
}
