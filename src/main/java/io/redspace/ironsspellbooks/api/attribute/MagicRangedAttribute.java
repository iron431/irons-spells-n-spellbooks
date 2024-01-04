package io.redspace.ironsspellbooks.api.attribute;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class MagicRangedAttribute extends RangedAttribute implements IMagicAttribute {
    public MagicRangedAttribute(String pDescriptionId, double pDefaultValue, double pMin, double pMax) {
        super(pDescriptionId, pDefaultValue, pMin, pMax);
    }
}
