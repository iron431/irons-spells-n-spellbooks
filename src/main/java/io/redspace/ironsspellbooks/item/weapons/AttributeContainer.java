package io.redspace.ironsspellbooks.item.weapons;


import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.backwards_compat.AttributeHelper;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.function.Supplier;

public record AttributeContainer(Supplier<Attribute> attribute, double value, AttributeModifier.Operation operation) {
    public AttributeModifier createModifier(String slot) {
        var attribute = attribute().get();
        var attributeName = attribute.getDescriptionId();
        var id = IronsSpellbooks.id(String.format("%s_%s_modifier", slot, attributeName));
        return new AttributeModifier(AttributeHelper.uuidFromId(id), id.toString(), value, operation);
    }
}
