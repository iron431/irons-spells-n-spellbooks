package io.redspace.ironsspellbooks.item.curios;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

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

    @NotNull
    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(SoundEvents.ARMOR_EQUIP_CHAIN, 1.0f, 1.0f);
    }
}
