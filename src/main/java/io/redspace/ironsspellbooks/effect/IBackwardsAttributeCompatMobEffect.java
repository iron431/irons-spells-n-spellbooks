package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.api.backwards_compat.AttributeHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.function.Supplier;

public interface IBackwardsAttributeCompatMobEffect {

    default IBackwardsAttributeCompatMobEffect addAttributeModifier(Supplier<Attribute> attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        return addAttributeModifier(attribute.get(), id, amount, operation);
    }

    default IBackwardsAttributeCompatMobEffect addAttributeModifier(Attribute attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        cast().addAttributeModifier(attribute, AttributeHelper.uuidFromId(id).toString(), amount, operation);
        return this;
    }

    default MobEffect cast() {
        return (MobEffect) this;
    }
}
