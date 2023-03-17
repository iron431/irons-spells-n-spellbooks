package io.redspace.ironsspellbooks.item;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;

public class UpgradeOrbItem extends Item {
    private final Attribute attribute;

    public UpgradeOrbItem(Attribute attribute, Properties pProperties) {
        super(pProperties);
        this.attribute = attribute;
    }

    public Attribute getUpgradeAttribute() {
        return this.attribute;
    }
}
