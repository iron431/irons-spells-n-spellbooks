package io.redspace.ironsspellbooks.item;

import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

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

    /**
     * Backwards compat for 1.20.1 addons. Use {@link SpellBook#withSpellbookAttributes(AttributeContainer...)} to assign attributes instead.
     */
    @Deprecated(forRemoval = true)
    public UniqueSpellBook(SpellRarity rarity, SpellDataRegistryHolder[] spellDataRegistryHolders, int additionalSlots, Supplier<Multimap<Attribute, AttributeModifier>> defaultModifiers) {
        this(spellDataRegistryHolders, additionalSlots);
        AttributeContainer[] ary = defaultModifiers.get().entries().stream().map(entry -> new AttributeContainer(entry::getKey, entry.getValue().getAmount(), entry.getValue().getOperation())).toArray(AttributeContainer[]::new);
        withSpellbookAttributes(ary);
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
        return ISpellContainer.isSpellContainer(stack) && ISpellContainer.get(stack).isImproved() ? Component.translatable("tooltip.irons_spellbooks.improved_format", super.getName(stack)) : super.getName(stack);
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
            var spellContainer = ISpellContainer.create(getMaxSpellSlots(), true, true).mutableCopy();
            getSpells().forEach(spellSlot -> spellContainer.addSpell(spellSlot.getSpell(), spellSlot.getLevel(), true));
            ISpellContainer.set(itemStack, spellContainer.toImmutable());
        }
    }
}
