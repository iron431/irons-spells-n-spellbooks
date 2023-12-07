package io.redspace.ironsspellbooks.item.curios;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

import java.util.UUID;

public class SimpleAttributeCurio extends CurioBaseItem {

    private final AttributeModifier attributeModifier;
    Multimap<Attribute, AttributeModifier> attributeMap;

    public SimpleAttributeCurio(Item.Properties properties, Attribute attribute, AttributeModifier attributeModifier) {
        super(properties);
        this.attributeModifier = attributeModifier;
        attributeMap = HashMultimap.create();
        attributeMap.put(attribute, this.attributeModifier);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> attributeBuilder = new ImmutableMultimap.Builder<>();
        for (Attribute attribute : attributeMap.keySet()) {
            attributeBuilder.put(attribute, new AttributeModifier(uuid, attributeModifier.getName(), attributeModifier.getAmount(), attributeModifier.getOperation()));
        }
        return attributeBuilder.build();
    }

}
