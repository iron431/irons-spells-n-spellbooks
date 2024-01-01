package io.redspace.ironsspellbooks.api.spells;

import net.minecraft.world.item.ItemStack;

public interface IContainsSpells {
    ISpellSlotContainer getSpellSlotContainer(ItemStack itemStack);
}
