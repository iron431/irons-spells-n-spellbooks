package io.redspace.ironsspellbooks.item.curios;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.curios.api.SlotContext;

import java.util.HashMap;
import java.util.UUID;

public class SimpleAttributeCurio extends CurioBaseItem {

    private AttributeModifier attributeModifier;
    private RegistryObject<Attribute> attribute;
    LazyOptional<Multimap<Attribute, AttributeModifier>> lazyOptional;

    public SimpleAttributeCurio(Item.Properties properties, RegistryObject<Attribute> attribute, AttributeModifier attributeModifier) {
        super(properties);
        this.attribute = attribute;
        this.attributeModifier = attributeModifier;
        lazyOptional = LazyOptional.of(this::buildMap);
    }

    private Multimap<Attribute, AttributeModifier> buildMap() {
        Multimap<Attribute, AttributeModifier> attributeMap = HashMultimap.create();
        attributeMap.put(this.attribute.get(), this.attributeModifier);
        return attributeMap;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        var attributeMap = lazyOptional.resolve().get();
        if (!uuid.equals(attributeModifier.getId())) {
            attributeMap.remove(attribute.get(), attributeModifier);
            attributeModifier = new AttributeModifier(uuid, attributeModifier.getName(), attributeModifier.getAmount(), attributeModifier.getOperation());
            attributeMap.put(attribute.get(), attributeModifier);
        }
        return attributeMap;
    }
}
