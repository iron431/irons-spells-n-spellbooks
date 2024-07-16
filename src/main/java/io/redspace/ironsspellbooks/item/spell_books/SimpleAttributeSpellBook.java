package io.redspace.ironsspellbooks.item.spell_books;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.item.SpellBook;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

import java.util.UUID;

public class SimpleAttributeSpellBook extends SpellBook {
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public SimpleAttributeSpellBook(int spellSlots, SpellRarity rarity, Attribute attribute, double value) {
        this(spellSlots, rarity, attribute, value, 0);
    }

    public SimpleAttributeSpellBook(int spellSlots, SpellRarity rarity, Attribute attribute, double value, double mana) {
        this(spellSlots, rarity, createMultimap(attribute, new AttributeModifier(UUID.fromString("667ad88f-901d-4691-b2a2-3664e42026d3"), "Weapon modifier", value, AttributeModifier.Operation.MULTIPLY_BASE), mana));
    }

    public SimpleAttributeSpellBook(int spellSlots, SpellRarity rarity, Multimap<Attribute, AttributeModifier> defaultModifiers) {
        super(spellSlots, rarity);
        this.defaultModifiers = defaultModifiers;
    }

    public SimpleAttributeSpellBook(int spellSlots, SpellRarity rarity, Multimap<Attribute, AttributeModifier> defaultModifiers, Item.Properties properties) {
        super(spellSlots, rarity, properties);
        this.defaultModifiers = defaultModifiers;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> attributeBuilder = new ImmutableMultimap.Builder<>();
        for (Attribute attribute : defaultModifiers.keySet()) {
            var modifiers = defaultModifiers.get(attribute);
            for (AttributeModifier attributeModifier : modifiers) {
                attributeBuilder.put(attribute, new AttributeModifier(uuid, attributeModifier.getName(), attributeModifier.getAmount(), attributeModifier.getOperation()));
            }
        }
        return attributeBuilder.build();
    }

    private static Multimap<Attribute, AttributeModifier> createMultimap(Attribute attribute, AttributeModifier modifier, double mana) {
        Multimap<Attribute, AttributeModifier> map = HashMultimap.create();
        map.put(attribute, modifier);
        if(mana > 0){
            map.put(AttributeRegistry.MAX_MANA.get(), new AttributeModifier(UUID.fromString("667ad88f-901d-4691-b2a2-3664e88826d3"), "Weapon modifier", mana, AttributeModifier.Operation.ADDITION));
        }
        return map;
    }
}
