package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.item.armor.UpgradeType;
import net.minecraft.world.item.Item;

public class UpgradeOrbItem extends Item {
    private final UpgradeType upgrade;

    public UpgradeOrbItem(UpgradeType upgrade, Properties pProperties) {
        super(pProperties);
        this.upgrade = upgrade;
    }

    public UpgradeType getUpgradeType() {
        return this.upgrade;
    }
}
