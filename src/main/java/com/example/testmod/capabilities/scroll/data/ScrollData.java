package com.example.testmod.capabilities.scroll.data;

import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.nbt.CompoundTag;

public class ScrollData {

    public static final String SPELL_ID = "spellId";
    public static final String LEVEL = "LEVEL";
    private AbstractSpell spell;
    private CompoundTag tag = new CompoundTag();

    public ScrollData(SpellType spellType, int level) {
        this.spell = AbstractSpell.getSpell(spellType, level);
        this.tag = saveNBTData();
    }

    public AbstractSpell getSpell() {
        return this.spell;
    }

    public CompoundTag saveNBTData() {
        CompoundTag compound = new CompoundTag();
        compound.putInt(SPELL_ID, this.spell.getID());
        compound.putInt(LEVEL, this.spell.getLevel());
        return (compound);
    }

    public void loadNBTData(CompoundTag compound) {
        int spellId = compound.getInt(SPELL_ID);
        int spellLevel = compound.getInt(LEVEL);
        this.spell = AbstractSpell.getSpell(spellId, spellLevel);
        this.tag = saveNBTData();
    }
}
