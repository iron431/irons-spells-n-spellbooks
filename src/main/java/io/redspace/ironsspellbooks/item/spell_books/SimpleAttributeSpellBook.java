package io.redspace.ironsspellbooks.item.spell_books;

import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * Legacy hardcoded-attribute class should no longer be used. Attributes can now be customized per-spellbook via {@link SpellBook#withSpellbookAttributes(AttributeContainer...)}
 */
@Deprecated(forRemoval = true)
public class SimpleAttributeSpellBook extends SpellBook {
    public SimpleAttributeSpellBook(int spellSlots, SpellRarity rarity, Attribute attribute, double value) {
        super(spellSlots);
        withAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute), value);
    }

    public SimpleAttributeSpellBook(int spellSlots, SpellRarity rarity, Attribute attribute, double value, double mana) {
        super(spellSlots);
        withSpellbookAttributes(new AttributeContainer(() -> attribute, value, AttributeModifier.Operation.MULTIPLY_BASE), new AttributeContainer(AttributeRegistry.MAX_MANA, mana, AttributeModifier.Operation.ADDITION));
    }

    public SimpleAttributeSpellBook(int spellSlots, SpellRarity rarity, Multimap<Attribute, AttributeModifier> defaultModifiers) {
        super(spellSlots);
        AttributeContainer[] ary = defaultModifiers.entries().stream().map(entry -> new AttributeContainer(entry::getKey, entry.getValue().getAmount(), entry.getValue().getOperation())).toArray(AttributeContainer[]::new);
        withSpellbookAttributes(ary);
    }

    public SimpleAttributeSpellBook(int spellSlots, SpellRarity rarity, Multimap<Attribute, AttributeModifier> defaultModifiers, Properties properties) {
        super(spellSlots, properties);
        AttributeContainer[] ary = defaultModifiers.entries().stream().map(entry -> new AttributeContainer(entry::getKey, entry.getValue().getAmount(), entry.getValue().getOperation())).toArray(AttributeContainer[]::new);
        withSpellbookAttributes(ary);
    }
}
