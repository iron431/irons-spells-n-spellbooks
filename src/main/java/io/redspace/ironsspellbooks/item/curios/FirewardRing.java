package io.redspace.ironsspellbooks.item.curios;

import io.redspace.ironsspellbooks.compat.Curios;
import net.minecraft.world.item.Item;

public class FirewardRing extends SimpleDescriptiveCurio {
    public FirewardRing() {
        super(new Item.Properties().stacksTo(1), Curios.RING_SLOT);
    }
}
