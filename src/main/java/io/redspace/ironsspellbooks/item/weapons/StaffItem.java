package io.redspace.ironsspellbooks.item.weapons;

import io.redspace.ironsspellbooks.item.CastingItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class StaffItem extends CastingItem {

    public StaffItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean isEnchantable(ItemStack pStack) {
        return true;
    }

    public int getEnchantmentValue() {
        return 20;
    }

    public boolean hasCustomRendering(){
        return false;
    }
}
