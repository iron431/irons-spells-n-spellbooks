package io.redspace.ironsspellbooks.api.spells;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SpellSlotContainer implements ISpellSlotContainer {
    public static final String SPELL_SLOT_CONTAINER = "ISB_Spells";
    public static final String SLOTS = "slots";
    public static final String MAX_SLOTS = "maxSlots";
    public static final String SLOT_INDEX = "index";
    public static final String SPELL_ID = "id";
    public static final String SPELL_LEVEL = "level";
    public static final String SPELL_LOCKED = "locked";

    private SpellSlot[] slots;
    private int maxSlots;
    private int activeSlots = 0;

    public SpellSlotContainer() {
    }

    public SpellSlotContainer(int maxSpellSlots) {
        this.maxSlots = maxSpellSlots;
        this.slots = new SpellSlot[this.maxSlots];
    }

    public SpellSlotContainer(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTagElement(SPELL_SLOT_CONTAINER);
        if (tag != null) {
            deserializeNBT(tag);
        }
    }

    @Override
    public int getMaxSlotCount() {
        return maxSlots;
    }

    @Override
    public int getUsedSlotCount() {
        return activeSlots;
    }

    @Override
    public boolean isEmpty() {
        return activeSlots == 0;
    }

    @Override
    public void save(ItemStack stack) {
        if (stack != null) {
            setNbtOnStack(stack, this);
        }
    }

    @Override
    public SpellSlot[] getAllSpellSlots() {
        var result = new SpellSlot[this.maxSlots];
        System.arraycopy(slots, 0, result, 0, slots.length);
        return result;
    }

    @Override
    public List<SpellSlot> getActiveSpellSlots() {
        return Arrays.stream(this.slots).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public int getNextAvailableSlot() {
        return ArrayUtils.indexOf(this.slots, null);
    }

    @Override
    public SpellSlot getSlotAtIndex(int index) {
        if (index >= 0 && index < slots.length) {
            var result = slots[index];
            if (result != null) {
                return slots[index];
            }
        }
        return SpellSlot.EMPTY;
    }

    @Override
    public int getSlotIndexForSpell(AbstractSpell spell) {
        for (int i = 0; i < slots.length; i++) {
            var s = slots[i];

            if (s != null && s.equals(spell)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean addSpellAtSlot(AbstractSpell spell, int level, int index, boolean locked, ItemStack itemStack) {
        if (index > -1 && index < slots.length &&
                slots[index] == null &&
                Arrays.stream(slots).noneMatch(s -> s != null && s.getSpell().equals(spell))) {
            slots[index] = new SpellSlot(spell, level, locked);
            activeSlots++;
            save(itemStack);
            return true;
        }
        return false;
    }

    @Override
    public boolean addSpellToOpenSlot(AbstractSpell spell, int level, boolean locked, ItemStack itemStack) {
        return addSpellAtSlot(spell, level, getNextAvailableSlot(), locked, itemStack);
    }

    @Override
    public boolean removeSpellAtSlot(int index, ItemStack itemStack) {
        if (index > -1 && index < slots.length && slots[index] != null) {
            slots[index] = null;
            activeSlots--;
            save(itemStack);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeSpellSlot(AbstractSpell spell, ItemStack itemStack) {
        if (spell == null) {
            return false;
        }

        for (int i = 0; i < slots.length; i++) {
            var spellSLot = slots[i];
            if (spellSLot != null && spell.equals(spellSLot.spell)) {
                return removeSpellAtSlot(i, itemStack);
            }
            break;
        }
        return false;
    }

    @Override
    public CompoundTag serializeNBT() {
        var rootTag = new CompoundTag();
        rootTag.putInt(MAX_SLOTS, maxSlots);
        var listTag = new ListTag();
        for (int i = 0; i < slots.length; i++) {
            var spellSlot = slots[i];
            if (spellSlot != null) {
                CompoundTag slotTag = new CompoundTag();
                slotTag.putString(SPELL_ID, spellSlot.spell.getSpellId());
                slotTag.putInt(SPELL_LEVEL, spellSlot.spellLevel);
                slotTag.putBoolean(SPELL_LOCKED, spellSlot.locked);
                slotTag.putInt(SLOT_INDEX, i);
                listTag.add(slotTag);
            }
        }
        rootTag.put(SLOTS, listTag);
        return rootTag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.maxSlots = nbt.getInt(MAX_SLOTS);
        this.slots = new SpellSlot[maxSlots];

        ListTag listTagSpells = (ListTag) nbt.get(SLOTS);

        activeSlots = 0;
        if (listTagSpells != null && !listTagSpells.isEmpty()) {
            listTagSpells.forEach(tagSlot -> {
                CompoundTag t = (CompoundTag) tagSlot;
                String id = t.getString(SPELL_ID);
                int level = t.getInt(SPELL_LEVEL);
                boolean locked = t.getBoolean(SPELL_LOCKED);
                int index = t.getInt(SLOT_INDEX);
                slots[index] = new SpellSlot(SpellRegistry.getSpell(id), level, locked);
                activeSlots++;
            });
        }
    }

    public static void setNbtOnStack(ItemStack stack, ISpellSlotContainer spellSlotContainer) {
        if (stack != null && spellSlotContainer != null) {
            stack.addTagElement(SPELL_SLOT_CONTAINER, spellSlotContainer.serializeNBT());
        }
    }

    public static ISpellSlotContainer getSpellSlotContainer(ItemStack itemStack) {
        return getSpellSlotContainer(itemStack, false);
    }

    public static ISpellSlotContainer getSpellSlotContainer(ItemStack itemStack, boolean nbtOnly) {
        CompoundTag tag = itemStack.getTagElement(SPELL_SLOT_CONTAINER);
        if (tag != null) {
            var ssc = new SpellSlotContainer();
            ssc.deserializeNBT(tag);
            return ssc;
        } else if (!nbtOnly && itemStack.getItem() instanceof IContainSpells iContainsSpells) {
            return iContainsSpells.getSpellSlotContainer(itemStack);
        }

        return new SpellSlotContainer(0);
    }

    public static boolean isSpellContainer(ItemStack itemStack) {
        if (itemStack != null) {
            var tag = itemStack.getTag();
            if (tag != null && tag.contains(SPELL_SLOT_CONTAINER)) {
                return true;
            }
        }
        return false;
    }
}
