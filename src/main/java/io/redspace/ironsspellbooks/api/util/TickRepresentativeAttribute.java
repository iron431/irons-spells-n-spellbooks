package io.redspace.ironsspellbooks.api.util;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TickRepresentativeAttribute extends RangedAttribute {
    public TickRepresentativeAttribute(String descriptionId, double defaultValue, double min, double max) {
        super(descriptionId, defaultValue, min, max);
    }

    @Override
    public @NotNull MutableComponent toBaseComponent(double value, double entityBase, boolean merged, @NotNull TooltipFlag flag) {
        return super.toBaseComponent(value / 20.0, entityBase, merged, flag);
    }

    @Override
    public @NotNull MutableComponent toValueComponent(@Nullable AttributeModifier.Operation op, double value, @NotNull TooltipFlag flag) {
        if (op == AttributeModifier.Operation.ADD_VALUE) {
            value = value / 20.0;
            return super.toValueComponent(op, value, flag).append("s");
        }
        return super.toValueComponent(op, value, flag);
    }
}
