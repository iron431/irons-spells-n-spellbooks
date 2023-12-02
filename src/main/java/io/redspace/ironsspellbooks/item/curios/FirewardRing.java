package io.redspace.ironsspellbooks.item.curios;

import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import io.redspace.ironsspellbooks.compat.Curios;

public class FirewardRing extends SimpleDescriptiveCurio {
    public FirewardRing() {
        super(ItemPropertiesHelper.equipment().stacksTo(1), Curios.RING_SLOT);
    }
}
