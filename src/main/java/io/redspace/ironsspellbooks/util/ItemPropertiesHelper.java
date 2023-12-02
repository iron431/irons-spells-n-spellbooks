package io.redspace.ironsspellbooks.util;

import net.minecraft.world.item.Item;

public class ItemPropertiesHelper {
    public static Item.Properties equipment() {
        return new Item.Properties().tab(SpellbookModCreativeTabs.SPELL_EQUIPMENT_TAB);
    }

    public static Item.Properties material() {
        return new Item.Properties().tab(SpellbookModCreativeTabs.SPELL_MATERIALS_TAB);
    }
}
