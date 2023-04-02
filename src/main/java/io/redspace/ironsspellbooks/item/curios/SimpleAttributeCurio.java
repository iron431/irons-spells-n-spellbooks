package io.redspace.ironsspellbooks.item.curios;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

import java.util.UUID;

public class SimpleAttributeCurio extends CurioBaseItem {

    private AttributeModifier attributeModifier;
    private Attribute attribute;
    Multimap<Attribute, AttributeModifier> attributeMap;

    public SimpleAttributeCurio(Item.Properties properties, Attribute attribute, AttributeModifier attributeModifier) {
        super(properties);
        this.attribute = attribute;
        this.attributeModifier = attributeModifier;
        attributeMap = HashMultimap.create();
        attributeMap.put(this.attribute, this.attributeModifier);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        if (!uuid.equals(attributeModifier.getId())) {
            attributeMap.remove(attribute, attributeModifier);
            attributeModifier = new AttributeModifier(uuid, attributeModifier.getName(), attributeModifier.getAmount(), attributeModifier.getOperation());
            attributeMap.put(attribute, attributeModifier);
        }
        return attributeMap;
    }

}
