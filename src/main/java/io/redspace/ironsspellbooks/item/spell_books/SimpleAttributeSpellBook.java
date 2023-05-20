package io.redspace.ironsspellbooks.item.spell_books;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.spells.SpellRarity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.RegistryObject;

import java.util.UUID;

public class SimpleAttributeSpellBook extends SpellBook {
    private final LazyOptional<Multimap<Attribute, AttributeModifier>> lazyOptional;
    private final RegistryObject<Attribute> attribute;
    private final double value;

    public SimpleAttributeSpellBook(int spellSlots, SpellRarity rarity, RegistryObject<Attribute> attribute, double value) {
        super(spellSlots, rarity);
        this.attribute = attribute;
        this.value = value;
        lazyOptional = LazyOptional.of(this::buildMap);
    }

    private Multimap<Attribute, AttributeModifier> buildMap() {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(attribute.get(), new AttributeModifier(UUID.fromString("667ad88f-901d-4691-b2a2-3664e42026d3"), "Weapon modifier", value, AttributeModifier.Operation.MULTIPLY_BASE));
        return builder.build();
    }

    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot pEquipmentSlot) {
        return pEquipmentSlot == EquipmentSlot.MAINHAND ? this.lazyOptional.resolve().get() : super.getDefaultAttributeModifiers(pEquipmentSlot);
    }
}
