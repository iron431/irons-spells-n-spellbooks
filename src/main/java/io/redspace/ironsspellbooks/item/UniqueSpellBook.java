package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public class UniqueSpellBook extends SpellBook implements UniqueItem {

    List<SpellData> spellData = null;
    SpellDataRegistryHolder[] spellDataRegistryHolders;

    public UniqueSpellBook(SpellDataRegistryHolder[] spellDataRegistryHolders) {
        super(spellDataRegistryHolders.length);
        this.spellDataRegistryHolders = spellDataRegistryHolders;
    }

    public UniqueSpellBook(SpellDataRegistryHolder[] spellDataRegistryHolders, int additionalSlots) {
        super(spellDataRegistryHolders.length + additionalSlots);
        this.spellDataRegistryHolders = spellDataRegistryHolders;
    }

    public List<SpellData> getSpells() {
        if (spellData == null) {
            spellData = Arrays.stream(spellDataRegistryHolders).map(SpellDataRegistryHolder::getSpellData).toList();
            spellDataRegistryHolders = null;
        }
        return spellData;
    }

    @Override
    public Component getName(ItemStack stack) {
        return stack.has(ComponentRegistry.SPELL_CONTAINER) && stack.get(ComponentRegistry.SPELL_CONTAINER).improved() ? Component.translatable("tooltip.irons_spellbooks.improved_format", super.getName(stack)) : super.getName(stack);
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public void initializeSpellContainer(ItemStack itemStack) {
        if (itemStack == null) {
            return;
        }

        if (!ISpellContainer.isSpellContainer(itemStack)) {
            var spellContainer = ISpellContainer.create(getMaxSpellSlots(), true, true);
            getSpells().forEach(spellSlot -> spellContainer.addSpell(spellSlot.getSpell(), spellSlot.getLevel(), true, itemStack));
            itemStack.set(ComponentRegistry.SPELL_CONTAINER, spellContainer);
        }
    }
}
