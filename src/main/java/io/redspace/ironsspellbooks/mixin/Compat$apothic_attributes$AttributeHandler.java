package io.redspace.ironsspellbooks.mixin;

import dev.shadowsoffire.apothic_attributes.api.IFormattableAttribute;
import io.redspace.ironsspellbooks.api.attribute.MagicPercentAttribute;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MagicPercentAttribute.class)
public class Compat$apothic_attributes$AttributeHandler implements IFormattableAttribute {

    public MutableComponent toValueComponent(AttributeModifier.Operation op, double value, TooltipFlag flag) {
        return Component.translatable("apothic_attributes.value.percent", FORMAT.format(value * 100.0));
    }
}
