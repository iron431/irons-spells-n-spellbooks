package io.redspace.ironsspellbooks.api.spells;

import net.minecraft.world.item.ItemStack;

public interface IHaveSpellList {
    ISpellList getSpellList(ItemStack itemStack);
}
