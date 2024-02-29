package io.redspace.ironsspellbooks.item.armor;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorMaterial;

import java.util.Map;

public interface IronsExtendedArmorMaterial extends ArmorMaterial {
    public Map<Attribute, AttributeModifier> getAdditionalAttributes();

}
