package io.redspace.ironsspellbooks.api.spells;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.List;

public interface ISpellSlotContainer extends INBTSerializable<CompoundTag> {
    SpellSlot[] getAllSpellSlots();
    List<SpellSlot> getActiveSpellSlots();
    int getMaxSlotCount();
    int getUsedSlotCount();
    int getNextAvailableSlot();
    boolean mustEquip();
    boolean spellWheel();
    SpellSlot getSlotAtIndex(int index);
    int getSlotIndexForSpell(AbstractSpell spell);
    boolean addSpellAtSlot(AbstractSpell spell, int level, int index, boolean locked, ItemStack itemStack);
    boolean addSpellToOpenSlot(AbstractSpell spell, int level, boolean locked, ItemStack itemStack);
    boolean removeSpellAtSlot(int index, ItemStack itemStack);
    boolean removeSpellSlot(AbstractSpell spell, ItemStack itemStack);
    boolean isEmpty();
    void save(ItemStack stack);
}
