package io.redspace.ironsspellbooks.item.curios;

import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;

public class FirewardRing extends SimpleDescriptiveCurio {
    public FirewardRing() {
        super(ItemPropertiesHelper.equipment().stacksTo(1), "ring");
    }
}
