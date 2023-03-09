package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.capabilities.spellbook.SpellBookDataProvider;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellRarity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

public class UniqueSpellBook extends SpellBook implements UniqueItem {

    AbstractSpell[] spells;

    public UniqueSpellBook(SpellRarity rarity, AbstractSpell[] spells) {
        super(spells.length, rarity);
        this.spells = spells;
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        var spellBookDataProvider = new SpellBookDataProvider();

        if (nbt != null) {
            //irons_spellbooks.LOGGER.debug("SpellBook.initCapabilities.1: {}, {}", spellSlots, nbt);
            spellBookDataProvider.deserializeNBT(nbt.getCompound(PARENT));
        } else {
            //irons_spellbooks.LOGGER.debug("SpellBook.initCapabilities.2: {}", spellSlots);
            var spellBookData = spellBookDataProvider.getOrCreateSpellBookData(spellSlots);
            for (AbstractSpell spell : spells) {
                spellBookData.addSpell(spell);
            }
        }
        return spellBookDataProvider;
    }
}
