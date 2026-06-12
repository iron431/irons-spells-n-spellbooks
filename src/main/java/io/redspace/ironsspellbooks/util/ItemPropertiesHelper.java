package io.redspace.ironsspellbooks.util;

import net.minecraft.world.item.Item;

/**
 * 1.19.2 Platform Helper. Stop using this, especially with upcoming 26.1.2 changes
 */
@Deprecated(forRemoval = true)
public class ItemPropertiesHelper {
    /**
     * 1.19.2 Platform Helper. Stop using this, especially with upcoming 26.1.2 changes
     */
    @Deprecated(forRemoval = true)
    public static Item.Properties equipment() {
        return new Item.Properties();
    }

    /**
     * 1.19.2 Platform Helper. Stop using this, especially with upcoming 26.1.2 changes
     */
    @Deprecated(forRemoval = true)
    public static Item.Properties equipment(int stackSize) {
        return equipment().stacksTo(stackSize);
    }

    /**
     * 1.19.2 Platform Helper. Stop using this, especially with upcoming 26.1.2 changes
     */
    @Deprecated(forRemoval = true)
    public static Item.Properties material() {
        return new Item.Properties();
    }

    /**
     * 1.19.2 Platform Helper. Stop using this, especially with upcoming 26.1.2 changes
     */
    @Deprecated(forRemoval = true)
    public static Item.Properties material(int stackSize) {
        return material().stacksTo(stackSize);
    }

    /**
     * 1.19.2 Platform Helper. Stop using this, especially with upcoming 26.1.2 changes
     */
    @Deprecated(forRemoval = true)
    public static Item.Properties hidden() {
        return new Item.Properties();
    }

    /**
     * 1.19.2 Platform Helper. Stop using this, especially with upcoming 26.1.2 changes
     */
    @Deprecated(forRemoval = true)
    public static Item.Properties hidden(int stackSize) {
        return hidden().stacksTo(stackSize);
    }
}
