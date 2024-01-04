package io.redspace.ironsspellbooks.api.spells;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.UniqueItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SpellList implements ISpellList {
    //Container Root
    public static final String SPELL_SLOT_CONTAINER = "ISB_Spells";
    public static final String SPELL_DATA = "data";
    public static final String MAX_SLOTS = "maxSpells";
    public static final String MUST_EQUIP = "mustEquip";
    public static final String SPELL_WHEEL = "spellWheel";

    //Slot Data
    public static final String SLOT_INDEX = "index";
    public static final String SPELL_ID = "id";
    public static final String SPELL_LEVEL = "level";
    public static final String SPELL_LOCKED = "locked";

    private SpellData[] slots;
    private int maxSpells = 0;
    private int activeSlots = 0;
    private boolean spellWheel = false;
    private boolean mustEquip = true;

    public SpellList() {
    }

    public SpellList(int maxSpells, boolean spellWheel, boolean mustEquip) {
        this.maxSpells = maxSpells;
        this.slots = new SpellData[this.maxSpells];
        this.spellWheel = spellWheel;
        this.mustEquip = mustEquip;
    }

    public SpellList(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTagElement(SPELL_SLOT_CONTAINER);
        if (tag != null) {
            deserializeNBT(tag);
        } else {
            convertLegacyData(itemStack);
        }
    }

    @Override
    public int getMaxSpellCount() {
        return maxSpells;
    }

    @Override
    public int getActiveSpellCount() {
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
    public SpellData[] getAllSpells() {
        var result = new SpellData[maxSpells];
        if (maxSpells > 0) {
            System.arraycopy(slots, 0, result, 0, slots.length);
        }
        return result;
    }

    @Override
    public @NotNull List<SpellData> getActiveSpells() {
        return Arrays.stream(this.slots).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public int getNextAvailableIndex() {
        return ArrayUtils.indexOf(this.slots, null);
    }

    @Override
    public boolean mustEquip() {
        return mustEquip;
    }

    @Override
    public boolean spellWheel() {
        return spellWheel;
    }

    @Override
    public @NotNull SpellData getSpellAtIndex(int index) {
        if (index >= 0 && index < maxSpells) {
            var result = slots[index];
            if (result != null) {
                return slots[index];
            }
        }
        return SpellData.EMPTY;
    }

    @Override
    public int getIndexForSpell(AbstractSpell spell) {
        for (int i = 0; i < maxSpells; i++) {
            var s = slots[i];

            if (s != null && s.spell.equals(spell)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean addSpellAtIndex(AbstractSpell spell, int level, int index, boolean locked, ItemStack itemStack) {
        if (index > -1 && index < maxSpells &&
                slots[index] == null &&
                Arrays.stream(slots).noneMatch(s -> s != null && s.getSpell().equals(spell))) {
            slots[index] = new SpellData(spell, level, locked);
            activeSlots++;
            save(itemStack);
            return true;
        }
        return false;
    }

    @Override
    public boolean addSpell(AbstractSpell spell, int level, boolean locked, ItemStack itemStack) {
        return addSpellAtIndex(spell, level, getNextAvailableIndex(), locked, itemStack);
    }

    @Override
    public boolean removeSpellAtIndex(int index, ItemStack itemStack) {
        if (index > -1 && index < maxSpells && slots[index] != null) {
            slots[index] = null;
            activeSlots--;
            save(itemStack);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeSpell(AbstractSpell spell, ItemStack itemStack) {
        if (spell == null) {
            return false;
        }

        for (int i = 0; i < maxSpells; i++) {
            var spellData = slots[i];
            if (spellData != null && spell.equals(spellData.spell)) {
                return removeSpellAtIndex(i, itemStack);
            }
            break;
        }
        return false;
    }

    @Override
    public CompoundTag serializeNBT() {
        var rootTag = new CompoundTag();
        rootTag.putInt(MAX_SLOTS, maxSpells);
        rootTag.putBoolean(MUST_EQUIP, mustEquip);
        rootTag.putBoolean(SPELL_WHEEL, spellWheel);
        var listTag = new ListTag();
        for (int i = 0; i < maxSpells; i++) {
            var spellData = slots[i];
            if (spellData != null) {
                CompoundTag slotTag = new CompoundTag();
                slotTag.putString(SPELL_ID, spellData.spell.getSpellId());
                slotTag.putInt(SPELL_LEVEL, spellData.spellLevel);
                slotTag.putBoolean(SPELL_LOCKED, spellData.locked);
                slotTag.putInt(SLOT_INDEX, i);
                listTag.add(slotTag);
            }
        }
        rootTag.put(SPELL_DATA, listTag);
        return rootTag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.maxSpells = nbt.getInt(MAX_SLOTS);
        this.mustEquip = nbt.getBoolean(MUST_EQUIP);
        this.spellWheel = nbt.getBoolean(SPELL_WHEEL);
        this.slots = new SpellData[maxSpells];
        activeSlots = 0;
        ListTag listTagSpells = (ListTag) nbt.get(SPELL_DATA);
        if (listTagSpells != null && !listTagSpells.isEmpty()) {
            listTagSpells.forEach(tagSlot -> {
                CompoundTag t = (CompoundTag) tagSlot;
                String id = t.getString(SPELL_ID);
                int level = t.getInt(SPELL_LEVEL);
                boolean locked = t.getBoolean(SPELL_LOCKED);
                int index = t.getInt(SLOT_INDEX);
                if (index < slots.length) {
                    slots[index] = new SpellData(SpellRegistry.getSpell(id), level, locked);
                    activeSlots++;
                } else {
                    int x = 0;
                }
            });
        }
    }

    public static void setNbtOnStack(ItemStack stack, ISpellList spellList) {
        if (stack != null && spellList != null) {
            stack.addTagElement(SPELL_SLOT_CONTAINER, spellList.serializeNBT());
        }
    }

    public static boolean isSpellList(ItemStack itemStack) {
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

    private void convertLegacyData(ItemStack itemStack) {
        var tag = itemStack.getTag();
        if (tag != null && isLegacyTagFormat(tag)) {
            convertTag(tag, itemStack);
            CompoundTag convertedTag = itemStack.getTagElement(SPELL_SLOT_CONTAINER);
            if (convertedTag != null) {
                deserializeNBT(convertedTag);
            }
        }
    }

    private static void convertTag(CompoundTag tag, ItemStack itemStack) {
        if (tag.contains(LegacySpellData.ISB_SPELL)) {
            var legacySpellData = LegacySpellData.getSpellData(itemStack);
            var spellList = new SpellList(1, !(itemStack.getItem() instanceof Scroll), false);
            spellList.addSpellAtIndex(legacySpellData.spell, legacySpellData.spellLevel, 0, itemStack.getItem() instanceof UniqueItem, null);
            itemStack.addTagElement(SPELL_SLOT_CONTAINER, spellList.serializeNBT());
            itemStack.removeTagKey(LegacySpellData.ISB_SPELL);
        } else if (tag.contains(LegacySpellBookData.ISB_SPELLBOOK)) {
            if (itemStack.getItem() instanceof SpellBook spellBookItem) {
                var legcySpellBookData = LegacySpellBookData.getSpellBookData(itemStack);
                var newSize = spellBookItem.getMaxSpellSlots();
                var spellList = new SpellList(newSize, true, true);
                var unique = itemStack.getItem() instanceof UniqueItem;
                for (int i = 0; i < legcySpellBookData.transcribedSpells.length; i++) {
                    var legacySpellData = legcySpellBookData.transcribedSpells[i];
                    if (legacySpellData != null) {
                        spellList.addSpellAtIndex(legacySpellData.spell, legacySpellData.spellLevel, i, unique, null);
                    }
                }
                itemStack.addTagElement(SPELL_SLOT_CONTAINER, spellList.serializeNBT());
            }
            itemStack.removeTagKey(LegacySpellBookData.ISB_SPELLBOOK);
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
