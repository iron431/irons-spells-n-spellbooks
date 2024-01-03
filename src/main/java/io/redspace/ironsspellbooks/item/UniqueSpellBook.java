package io.redspace.ironsspellbooks.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.item.spell_books.SimpleAttributeSpellBook;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class UniqueSpellBook extends SimpleAttributeSpellBook implements UniqueItem {

    List<SpellSlot> spellSlots = null;
    SpellDataRegistryHolder[] spellDataRegistryHolders;

    public UniqueSpellBook(SpellRarity rarity, SpellDataRegistryHolder[] spellDataRegistryHolders, Supplier<Multimap<Attribute, AttributeModifier>> defaultModifiers) {
        super(spellDataRegistryHolders.length, rarity, defaultModifiers.get());
        this.spellDataRegistryHolders = spellDataRegistryHolders;
    }

    public UniqueSpellBook(SpellRarity rarity, SpellDataRegistryHolder[] spellDataRegistryHolders) {
        this(rarity, spellDataRegistryHolders, HashMultimap::create);
    }

    public List<SpellSlot> getSpells() {
        if (spellSlots == null) {
            spellSlots = Arrays.stream(spellDataRegistryHolders).map(SpellDataRegistryHolder::getSpellSlot).toList();
            spellDataRegistryHolders = null;
        }
        return spellSlots;
    }

    @Override
    public Component getName(ItemStack pStack) {
        var name = super.getName(pStack);
        if (pStack.hasTag() && pStack.getTag().getBoolean("Improved")) {
            return Component.translatable("tooltip.irons_spellbooks.improved_format", name);
        } else {
            return name;
        }
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    public ISpellSlotContainer getSpellSlotContainer(ItemStack itemStack) {
        if (itemStack == null) {
            return new SpellSlotContainer();
        }

        CompoundTag tag = itemStack.getTagElement(SpellSlotContainer.SPELL_SLOT_CONTAINER);

        if (tag != null) {
            return new SpellSlotContainer(itemStack);
        } else {
            var ssc = new SpellSlotContainer(getMaxSpellSlots(), CastSource.SPELLBOOK);
            getSpells().forEach(spellSlot -> ssc.addSpellToOpenSlot(spellSlot.getSpell(), spellSlot.getLevel(), true, null));
            ssc.save(itemStack);
            return ssc;
        }
    }
}
