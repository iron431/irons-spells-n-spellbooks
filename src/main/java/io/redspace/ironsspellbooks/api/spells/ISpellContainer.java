package io.redspace.ironsspellbooks.api.spells;

import io.redspace.ironsspellbooks.capabilities.magic.SpellContainer;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

public interface ISpellContainer/* extends INBTSerializable<CompoundTag> */ {
    //TODO: 1.21: remove item stack parameters from methods?
    // components are now stored as references, and game has separated the data class from the itemstack
    // thus, itemstack.save is not necesary nor expected


    @NotNull SpellSlot[] getAllSpells();

    @NotNull List<SpellSlot> getActiveSpells();

    int getMaxSpellCount();

    void setMaxSpellCount(int maxSpells);

    int getActiveSpellCount();

    int getNextAvailableIndex();

    boolean mustEquip();

    boolean improved();
    void setImproved(boolean improved);

    boolean spellWheel();

    @NotNull SpellData getSpellAtIndex(int index);

    int getIndexForSpell(AbstractSpell spell);

    boolean addSpellAtIndex(AbstractSpell spell, int level, int index, boolean locked, @Nullable ItemStack itemStack);

    boolean addSpell(AbstractSpell spell, int level, boolean locked, ItemStack itemStack);

    boolean removeSpellAtIndex(int index, ItemStack itemStack);

    boolean removeSpell(AbstractSpell spell, ItemStack itemStack);

    boolean isEmpty();

//    void save(ItemStack stack);

    static boolean isSpellContainer(ItemStack itemStack) {
        return itemStack != null && !itemStack.isEmpty() && itemStack.has(ComponentRegistry.SPELL_CONTAINER);
    }

    static ISpellContainer create(int maxSpells, boolean addsToSpellWheel, boolean mustBeEquipped) {
        return new SpellContainer(maxSpells, addsToSpellWheel, mustBeEquipped);
    }

    static ISpellContainer createScrollContainer(AbstractSpell spell, int spellLevel, ItemStack itemStack) {
        var spellContainer = create(1, false, false);
        spellContainer.addSpellAtIndex(spell, spellLevel, 0, true, itemStack);
        return spellContainer;
    }

    static ISpellContainer createImbuedContainer(AbstractSpell spell, int spellLevel, ItemStack itemStack) {
        var spellContainer = create(1, true, (itemStack.getItem() instanceof ArmorItem || itemStack.getItem() instanceof ICurioItem));
        spellContainer.addSpellAtIndex(spell, spellLevel, 0, true, itemStack);
        return spellContainer;
    }

    static ISpellContainer get(ItemStack itemStack) {
        return itemStack.get(ComponentRegistry.SPELL_CONTAINER);
    }

    static ISpellContainer getOrCreate(ItemStack itemStack) {
        return itemStack.getOrDefault(ComponentRegistry.SPELL_CONTAINER, new SpellContainer(1, true, false));
    }
}
