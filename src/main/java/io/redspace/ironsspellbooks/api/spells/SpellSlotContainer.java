package io.redspace.ironsspellbooks.api.spells;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.UniqueItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SpellSlotContainer implements ISpellSlotContainer {
    //Container Root
    public static final String SPELL_SLOT_CONTAINER = "ISB_Spells";
    public static final String SLOTS = "slots";
    public static final String MAX_SLOTS = "maxSlots";
    public static final String CAST_SOURCE = "source";

    //Slot Data
    public static final String SLOT_INDEX = "index";
    public static final String SPELL_ID = "id";
    public static final String SPELL_LEVEL = "level";
    public static final String SPELL_LOCKED = "locked";

    private SpellSlot[] slots;
    private int maxSlots = 0;
    private int activeSlots = 0;
    private CastSource castSource = CastSource.NONE;

    public SpellSlotContainer() {
    }

    public SpellSlotContainer(int maxSpellSlots, CastSource castSource) {
        this.maxSlots = maxSpellSlots;
        this.slots = new SpellSlot[this.maxSlots];
        this.castSource = castSource;
    }

    public SpellSlotContainer(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTagElement(SPELL_SLOT_CONTAINER);
        if (tag != null) {
            deserializeNBT(tag);
        } else {
            tag = itemStack.getTag();
            if (tag != null && isLegacyTagFormat(tag)) {
                convertTag(tag, itemStack);
                deserializeNBT(tag);
            }
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
        var result = new SpellSlot[maxSlots];
        if (maxSlots > 0) {
            System.arraycopy(slots, 0, result, 0, slots.length);
        }
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
        if (index >= 0 && index < maxSlots) {
            var result = slots[index];
            if (result != null) {
                return slots[index];
            }
        }
        return SpellSlot.EMPTY;
    }

    @Override
    public int getSlotIndexForSpell(AbstractSpell spell) {
        for (int i = 0; i < maxSlots; i++) {
            var s = slots[i];

            if (s != null && s.equals(spell)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean addSpellAtSlot(AbstractSpell spell, int level, int index, boolean locked, ItemStack itemStack) {
        if (index > -1 && index < maxSlots &&
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
        if (index > -1 && index < maxSlots && slots[index] != null) {
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

        for (int i = 0; i < maxSlots; i++) {
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
        rootTag.putString(CAST_SOURCE, castSource.toString());
        var listTag = new ListTag();
        for (int i = 0; i < maxSlots; i++) {
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
        this.castSource = CastSource.valueOf(nbt.getString(CAST_SOURCE));
        this.slots = new SpellSlot[maxSlots];
        activeSlots = 0;
        ListTag listTagSpells = (ListTag) nbt.get(SLOTS);
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

    public static boolean isSpellContainer(ItemStack itemStack) {
        if (itemStack != null) {
            var tag = itemStack.getTag();
            if (tag != null && (tag.contains(SPELL_SLOT_CONTAINER) || isLegacyTagFormat(tag))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isLegacyTagFormat(CompoundTag tag) {
        return tag.contains(LegacySpellData.ISB_SPELL) || tag.contains(LegacySpellBookData.ISB_SPELLBOOK);
    }

    private static void convertTag(CompoundTag tag, ItemStack itemStack) {
        if (tag.contains(LegacySpellData.ISB_SPELL)) {
            var legacySpellData = LegacySpellData.getSpellData(itemStack);
            var ssc = new SpellSlotContainer(1, itemStack.getItem() instanceof Scroll ? CastSource.SCROLL : CastSource.SWORD);
            ssc.addSpellAtSlot(legacySpellData.spell, legacySpellData.spellLevel, 0, itemStack.getItem() instanceof UniqueItem, null);
            tag.put(SPELL_SLOT_CONTAINER, ssc.serializeNBT());
            tag.remove(LegacySpellData.ISB_SPELL);
        } else if (tag.contains(LegacySpellBookData.ISB_SPELLBOOK)) {
            var legcySpellBookData = LegacySpellBookData.getSpellBookData(itemStack);
            var newSize = ((SpellBook)itemStack.getItem()).getMaxSpellSlots();
            var ssc = new SpellSlotContainer(newSize, CastSource.SPELLBOOK);
            var unique = itemStack.getItem() instanceof UniqueItem;
            for (int i = 0; i < legcySpellBookData.transcribedSpells.length; i++) {
                var legacySpellData = legcySpellBookData.transcribedSpells[i];
                if (legacySpellData != null) {
                    ssc.addSpellAtSlot(legacySpellData.spell, legacySpellData.spellLevel, i, unique, null);
                }
            }
            tag.put(SPELL_SLOT_CONTAINER, ssc.serializeNBT());
            tag.remove(LegacySpellBookData.ISB_SPELLBOOK);
        }
    }

    //    public static ISpellSlotContainer getSpellSlotContainer(ItemStack itemStack) {
    //        return getSpellSlotContainer(itemStack, false);
    //    }
    //
    //    public static ISpellSlotContainer getSpellSlotContainer(ItemStack itemStack, boolean nbtOnly) {
    //        CompoundTag tag = itemStack.getTagElement(SPELL_SLOT_CONTAINER);
    //        if (tag != null) {
    //            var ssc = new SpellSlotContainer();
    //            ssc.deserializeNBT(tag);
    //            return ssc;
    //        } else if (!nbtOnly && itemStack.getItem() instanceof IContainSpells iContainsSpells) {
    //            return iContainsSpells.getSpellSlotContainer(itemStack);
    //        }
    //
    //        return new SpellSlotContainer();
    //    }
}
