package io.redspace.ironsspellbooks.api.backwards_compat;

import net.minecraft.world.item.ItemStack;

public interface IBackwardsCompatDefaultNbtItem {

    void setupItem(ItemStack stack);
}
