package io.redspace.ironsspellbooks.item.weapons;


import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record AttributeContainer(Holder<Attribute> attribute, double value, AttributeModifier.Operation operation) {
    public AttributeModifier createModifier(String slot) {
        var attributeName = ResourceLocation.parse(attribute.getRegisteredName()).getPath();
        return new AttributeModifier(IronsSpellbooks.id(String.format("%s_%s_modifier", slot, attributeName)), value, operation);
    }
}
