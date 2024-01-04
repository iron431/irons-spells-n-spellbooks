package io.redspace.ironsspellbooks.api.spells;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ISpellList extends INBTSerializable<CompoundTag> {
    @NotNull SpellData[] getAllSpells();
    @NotNull List<SpellData> getActiveSpells();
    int getMaxSpellCount();
    int getActiveSpellCount();
    int getNextAvailableIndex();
    boolean mustEquip();
    boolean spellWheel();
    @NotNull SpellData getSpellAtIndex(int index);
    int getIndexForSpell(AbstractSpell spell);
    boolean addSpellAtIndex(AbstractSpell spell, int level, int index, boolean locked, ItemStack itemStack);
    boolean addSpell(AbstractSpell spell, int level, boolean locked, ItemStack itemStack);
    boolean removeSpellAtIndex(int index, ItemStack itemStack);
    boolean removeSpell(AbstractSpell spell, ItemStack itemStack);
    boolean isEmpty();
    void save(ItemStack stack);
}
