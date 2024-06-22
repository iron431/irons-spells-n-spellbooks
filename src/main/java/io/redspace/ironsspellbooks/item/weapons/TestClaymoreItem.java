package io.redspace.ironsspellbooks.item.weapons;

import io.redspace.ironsspellbooks.api.item.weapons.ExtendedSwordItem;
import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;

import java.util.Map;


public class TestClaymoreItem extends ExtendedSwordItem {
    public TestClaymoreItem() {
        super(ExtendedWeaponTiers.CLAYMORE, 9, -2.7, Map.of(), ItemPropertiesHelper.hidden(1));
    }
}
