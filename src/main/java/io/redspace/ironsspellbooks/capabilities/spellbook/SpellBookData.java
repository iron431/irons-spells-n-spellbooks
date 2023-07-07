package io.redspace.ironsspellbooks.capabilities.spellbook;

import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.UniqueSpellBook;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SpellBookData {

    public static final String ISB_SPELLBOOK = "ISB_spellbook";
    public static final String SPELL_SLOTS = "spellSlots";
    public static final String ACTIVE_SPELL_INDEX = "activeSpellIndex";
    public static final String SPELLS = "spells";
    public static final String ID = "id";
    public static final String LEVEL = "level";
    public static final String SLOT = "slot";

    private AbstractSpell[] transcribedSpells;
    private int activeSpellIndex = -1;
    private int spellSlots;
    private int spellCount = 0;
    private boolean dirty = true;
    private List<Component> hoverText;

    public SpellBookData(CompoundTag tag) {
        loadFromNBT(tag);
    }

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

    public boolean setActiveSpellIndex(AbstractSpell spell, ItemStack stack) {
        var index = ArrayUtils.indexOf(transcribedSpells, spell);
        return setActiveSpellIndex(index, stack);
    }

    public boolean setActiveSpellIndex(int index, ItemStack stack) {
        if (index > -1 && index < transcribedSpells.length && transcribedSpells[index] != null) {
            this.activeSpellIndex = index;
            handleDirty(stack);
            return true;
        }
        return false;
    }

    public AbstractSpell[] getInscribedSpells() {
        var result = new AbstractSpell[this.spellSlots];
        System.arraycopy(transcribedSpells, 0, result, 0, transcribedSpells.length);
        return result;
    }


    public List<AbstractSpell> getActiveInscribedSpells() {
        return Arrays.stream(this.transcribedSpells).filter(Objects::nonNull).toList();
    }


    private void handleDirty(ItemStack stack) {
        if (stack != null) {
            SpellBookData.setSpellBookData(stack, this);
        }
    }

    public int getSpellSlots() {
        return spellSlots;
    }

    public int getActiveSpellIndex() {
        return activeSpellIndex;
    }

    public int getSpellCount() {
        return spellCount;
    }

    @Nullable
    public AbstractSpell getSpell(int index) {
        if (index >= 0 && index < transcribedSpells.length)
            return transcribedSpells[index];
        else
            return null;
    }

    public boolean addSpell(AbstractSpell spell, int index, ItemStack stack) {
        if (index > -1 && index < transcribedSpells.length &&
                transcribedSpells[index] == null &&
                Arrays.stream(transcribedSpells).noneMatch(s -> s != null && s.getSpellType() == spell.getSpellType())) {
            transcribedSpells[index] = spell;
            spellCount++;
            if (spellCount == 1) {
                setActiveSpellIndex(index, null);
            }
            handleDirty(stack);
            return true;
        }
        return false;
    }

    public boolean addSpell(AbstractSpell spell, ItemStack stack) {
        int index = getNextSpellIndex();
        if (index > -1) {
            return addSpell(spell, index, stack);
        }
        return false;
    }

    private int getNextSpellIndex() {
        return ArrayUtils.indexOf(this.transcribedSpells, null);
    }

    public boolean replaceSpell(AbstractSpell oldSpell, AbstractSpell newSpell, ItemStack stack) {
        if (oldSpell != null && newSpell != null) {
            int index = ArrayUtils.indexOf(transcribedSpells, oldSpell);
            if (index > -1 && removeSpell(index, null)) {
                return addSpell(newSpell, index, stack);
            }
        }

        return false;
    }

    public boolean replaceSpell(int index, AbstractSpell newSpell, ItemStack stack) {
        if (index > -1 && index < transcribedSpells.length) {
            return replaceSpell(transcribedSpells[index], newSpell, stack);
        }
        return false;
    }

    public boolean removeSpell(AbstractSpell spell, ItemStack stack) {
        return removeSpell(ArrayUtils.indexOf(transcribedSpells, spell), stack);
    }

    public boolean removeSpell(int index, ItemStack stack) {
        if (index > -1 && index < transcribedSpells.length && transcribedSpells[index] != null) {
            transcribedSpells[index] = null;
            spellCount--;

            if (spellCount == 0) {
                activeSpellIndex = -1;
            } else {
                for (int i = 0; i < transcribedSpells.length; i++) {
                    if (transcribedSpells[i] != null) {
                        activeSpellIndex = i;
                        break;
                    }
                }
            }

            handleDirty(stack);
            return true;
        }

        return false;
    }

    public CompoundTag getNBT() {
        CompoundTag compound = new CompoundTag();
        compound.putInt(SPELL_SLOTS, spellSlots);
        compound.putInt(ACTIVE_SPELL_INDEX, activeSpellIndex);

        ListTag listTagSpells = new ListTag();
        for (int i = 0; i < transcribedSpells.length; i++) {
            var spell = transcribedSpells[i];
            if (spell != null) {
                CompoundTag ct = new CompoundTag();
                ct.putInt(ID, spell.getID());
                ct.putInt(LEVEL, spell.getLevel(null));
                ct.putInt(SLOT, i);
                listTagSpells.add(ct);
            }
        }

        compound.put(SPELLS, listTagSpells);
        return compound;
    }

    public void loadFromNBT(CompoundTag compound) {
        this.spellSlots = compound.getInt(SPELL_SLOTS);
        this.transcribedSpells = new AbstractSpell[spellSlots];
        this.activeSpellIndex = compound.getInt(ACTIVE_SPELL_INDEX);

        ListTag listTagSpells = (ListTag) compound.get(SPELLS);
        spellCount = 0;
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
    }

    public static SpellBookData getSpellBookData(ItemStack stack) {
        if(stack == null){
            return new SpellBookData(0);
        }

        CompoundTag tag = stack.getTagElement(ISB_SPELLBOOK);

        if (tag != null) {
            return new SpellBookData(tag);
        } else if (stack.getItem() instanceof SpellBook spellBook) {
            var spellBookData = new SpellBookData(spellBook.getSpellSlots());

            if (spellBook instanceof UniqueSpellBook uniqueSpellBook) {
                Arrays.stream(uniqueSpellBook.getSpells()).forEach(spell -> spellBookData.addSpell(spell, null));
            }

            setSpellBookData(stack, spellBookData);
            return spellBookData;
        } else {
            return new SpellBookData(0);
        }
    }

    public static void setSpellBookData(ItemStack stack, SpellBookData spellBookData) {
        if (spellBookData != null) {
            stack.addTagElement(ISB_SPELLBOOK, spellBookData.getNBT());
        }
    }
}
